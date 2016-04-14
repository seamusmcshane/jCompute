package jcompute.cluster.ncp;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.cluster.computenode.nodedetails.NodeInfo;
import jcompute.cluster.computenode.nodedetails.NodeStatsSample;
import jcompute.cluster.computenode.weightingbenchmark.NodeWeightingBenchmark;
import jcompute.cluster.ncp.NCP.ProtocolMode;
import jcompute.cluster.ncp.NCP.ProtocolState;
import jcompute.cluster.ncp.message.NCPMessage;
import jcompute.cluster.ncp.message.command.AddSimReq;
import jcompute.cluster.ncp.message.command.SimulationStatsRequest;
import jcompute.cluster.ncp.message.monitoring.NodeStatsRequest;
import jcompute.cluster.ncp.message.registration.ConfigurationRequest;
import jcompute.cluster.ncp.message.registration.RegistrationReqAck;
import jcompute.cluster.ncp.message.registration.RegistrationReqNack;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.stats.StatExporter;

/**
 * NCP Socket Implementation
 *
 * @author Seamus McShane
 */
public class NCPSocket implements Closeable
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(NCPSocket.class);
	
	// NCP Message Manager
	private MessageManager ncpMM;
	
	// NCP ready state timeout logic
	private long ncpStartTime;
	
	// Mode is locked on construction
	private final ProtocolMode pMode;
	
	// Current NCP State
	private ProtocolState ncpState;
	
	/**
	 * Create an NCP for a Node
	 */
	public NCPSocket()
	{
		this(ProtocolMode.NODE);
	}
	
	/**
	 * Create an NCP socket for an NCP Controller.
	 *
	 * @param controller
	 * @throws SocketException
	 */
	public NCPSocket(Socket socket, int socketTXBuffer, boolean tcpNoDelay, int txFreq) throws SocketException
	{
		this(ProtocolMode.CONTROLLER);
		
		// Common TCP Socket opts
		setCommonSocketOpts(socket, socketTXBuffer, tcpNoDelay);
		
		// Recorded for ready state timeout
		ncpStartTime = System.currentTimeMillis();
		
		// Attach a MessageManager to the controller socket
		ncpMM = new MessageManager(socket);
		
		// Start the MessageManager
		ncpMM.start(txFreq);
	}
	
	private void setCommonSocketOpts(Socket socket, int socketTXBuffer, boolean tcpNoDelay) throws SocketException
	{
		// Set Socket opts
		socket.setSendBufferSize(socketTXBuffer);
		socket.setTcpNoDelay(tcpNoDelay);
	}
	
	/**
	 * The socket type created determines protocol mode.
	 *
	 * @param pMode
	 */
	private NCPSocket(ProtocolMode pMode)
	{
		this.pMode = pMode;
		
		ncpState = ProtocolState.DIS;
	}
	
	/**
	 * Initiate the TCP connection sequence to the specified address.<br>
	 * Note - this method does not initiate the NCP registration process for that you must call {@link #register}
	 *
	 * @param address
	 * The Internet address to connected to.
	 * @param socketRXBuffer
	 * Socket receive buffer size
	 * @param socketTXBuffer
	 * Socket send buffer size
	 * @param tcpNoDelay
	 * Disable/enable Nagle's algorithm
	 * @param txFreq
	 * The minimum delay before sending a message.
	 * Lower delay can messages to can be sent individually for better individually message latency but greater interrupt overhead,
	 * With a higher delays message can be sent in bulk for greater throughput efficiency much lower interrupt but worse individual message latency.
	 * @return
	 * True if the TCP connection is successful.<br>
	 * False if it failed for any reason.
	 * @see {@link java.net.Socket#setReceiveBufferSize}
	 * @see {@link java.net.Socket#setSendBufferSize}
	 * @see {@link java.net.Socket#setTcpNoDelay}
	 */
	@SuppressWarnings("resource")
	public boolean connect(String address, int socketRXBuffer, int socketTXBuffer, boolean tcpNoDelay, int txFreq)
	{
		if(pMode != ProtocolMode.NODE)
		{
			// Connect cannot be initiated by controller or a distribution node
			log.error("Controller cannot initiate NCP");
			
			return false;
		}
		
		// Cannot call connect if not disconnected
		if(ncpState != ProtocolState.DIS)
		{
			log.error("Connect called when NCP Socket already connected");
			
			return false;
		}
		
		// Success/Fail status of connect
		boolean status = false;
		
		log.info("Connecting to : " + address + "@" + NCP.StandardServerPort);
		
		// Create a TCP Client Socket
		Socket socket = new Socket();
		
		try
		{
			// Can set receive buffer before creating the socket.
			socket.setReceiveBufferSize(socketRXBuffer);
			
			// Common TCP Socket opts
			setCommonSocketOpts(socket, socketTXBuffer, tcpNoDelay);
			
			// Time out after 5 seconds
			socket.connect(new InetSocketAddress(address, NCP.StandardServerPort), 5000);
			
			// Status is set true if there are no exceptions thus connection is successful .
			status = true;
		}
		catch(SocketException | IllegalArgumentException e)
		{
			// If any of the parameters are invalid.
			log.error("TCP error when connecting to " + address + " " + e.getMessage());
			
			status = false;
		}
		catch(SocketTimeoutException e)
		{
			// No Response - anything from a fire wall to the host offline
			log.warn("Timeout occurred when connecting to " + address + " " + e.getMessage());
			
			status = false;
		}
		catch(IOException e)
		{
			// Such as the connection being refused.
			log.error("Failed to connect to " + address + " " + e.getMessage());
			
			status = false;
		}
		
		// If connected
		if(status)
		{
			// Recorded for ready state timeout
			ncpStartTime = System.currentTimeMillis();
			
			// Attach a MessageManager to the connected socket
			ncpMM = new MessageManager(socket);
			
			// Start the MessageManager
			ncpMM.start(txFreq);
			
			log.info("Connected to : " + socket.getRemoteSocketAddress());
			log.info("We are : " + socket.getLocalSocketAddress());
			
			// Now connected ready for NCP to register
			ncpState = ProtocolState.CON;
		}
		
		return status;
	}
	
	/**
	 * Handles the NCP registration process.
	 *
	 * @return
	 * True if NCP registered successfully and is in the ready state<br>
	 * False if it failed for any reason.
	 */
	public boolean register(NodeInfo nodeInfo)
	{
		// Validate State Transition
		if(ncpState != ProtocolState.CON)
		{
			log.error("Registration only possible in NCP connect state");
			
			return false;
		}
		
		// Enter the registration state
		ncpState = ProtocolState.REG;
		
		log.info("Sending Registration Request");
		
		// Send an NCP registration request via the MessageManager
		ncpMM.sendRegistrationRequest();
		
		// Registration loop - exit controlled via break Registration
		Registration :
		while(true)
		{
			// Wait on an NCP message reply.
			NCPMessage message = ncpMM.getMessage(true);
			
			// Have we got a message - a null message is an unrecoverable failure during registration.
			if(message == null)
			{
				// NCP is gone
				ncpState = ProtocolState.DIS;
				
				// All the different sequence errors are logged individually versus one generic disconnect error
				log.error(NCP.DisconnectReason.NullNCPRecv);
				
				// Exit Loop here.
				break Registration;
			}
			
			// Got a message but has an NCPReadyState timeout occurred
			if(isReadyStateTimeOut())
			{
				ncpState = ProtocolState.DIS;
				
				log.error(NCP.DisconnectReason.ReadyStateTimeout);
				
				// Exit the loop
				break Registration;
			}
			
			// Which message type.
			switch(message.getType())
			{
				case NCP.RegAck:
				{
					// Invalid Sequence Check - we must in the ProtocolState.REG
					if(ncpState != ProtocolState.REG)
					{
						// Log the state and message type
						log.error(NCP.DisconnectReason.InvRegSeq.appendfromStateInfoAndLastNCPType(ncpState, message.getType()));
						
						// NCP is gone
						ncpState = ProtocolState.DIS;
						
						// Exit Loop here.
						break Registration;
					}
					
					RegistrationReqAck reqAck = (RegistrationReqAck) message;
					
					// Store the id given to use as our node unique id.
					nodeInfo.setUid(reqAck.getUid());
					
					log.info("Registration acknowledgement received UID : " + nodeInfo.getUid());
					
					// Sanity check we acknowledge the acknowledgement - confirm our UID to remote node
					ncpMM.sendRegistrationReqAck(reqAck.getUid());
					
					// State REG_ACK
					ncpState = ProtocolState.REG_ACK;
				}
				break;
				case NCP.RegNack:
				{
					// Invalid Sequence Check - we have already got beyond ProtocolState.REG
					if(ncpState != ProtocolState.REG_ACK)
					{
						// Log the state and message type
						log.error(NCP.DisconnectReason.InvRegSeq.appendfromStateInfoAndLastNCPType(ncpState, message.getType()));
						
						// NCP is gone
						ncpState = ProtocolState.DIS;
						
						// Exit Loop here.
						break Registration;
					}
					
					// Remote does no like us.
					RegistrationReqNack reqNack = (RegistrationReqNack) message;
					
					// Get the reason and any value
					// These values may not make any sense if the reasons are not in our protocol version.
					int reason = reqNack.getReason();
					int value = reqNack.getValue();
					
					switch(reason)
					{
						// The reason for a REG_NACK is a mismatch between NCP versions
						case NCP.ProtocolVersionMismatch:
							
							log.error("Registration negative acknowledgement : Protocol Version Mismatch");
							log.error("Local " + NCP.NCP_PROTOCOL_VERSION + " Remote " + value);
							
						break;
						default:
							
							// The reason for a REG_NACK is something we do not understand - log the raw values.
							log.error("Registration negative acknowledgement : Unknown Reason " + reason + " Value " + value);
							
						break;
					}
					
					log.warn(NCP.DisconnectReason.NCPNack.appendfromStateInfoAndLastNCPType(ncpState, message.getType()));
					
					// Unrecoverable - don't keep trying as the result will be the same.
					ncpState = ProtocolState.DIS;
					
					// Exit Loop here
					break Registration;
				}
				case NCP.ConfReq:
				{
					// Invalid Sequence Check - we need to have just had the registration acknowledged
					if(ncpState != ProtocolState.REG_ACK)
					{
						// Log the state and message type
						log.error(NCP.DisconnectReason.InvRegSeq.appendfromStateInfoAndLastNCPType(ncpState, message.getType()));
						
						// NCP is gone
						ncpState = ProtocolState.DIS;
						
						// Exit Loop here.
						break Registration;
					}
					
					// Remote node wants our configuration details
					ConfigurationRequest confReq = (ConfigurationRequest) message;
					
					// Log what the controller requested Warm up
					log.info("Configuration request received :  Benchmark " + confReq.getBench() + " Object " + confReq.getObjects() + " Iterations " + confReq
					.getIterations() + " Warmup " + confReq.getWarmup() + " Runs " + confReq.getRuns());
					
					// Run the benchmark - note NCPReadyStateTimer is still in effect so can act as a way to prohibit slow nodes
					// (Assuming they actually run the benchmark as requested)
					int benchMark = confReq.getBench();
					
					if(benchMark > 0)
					{
						log.info("Running Weighting Benchmark");
						/* Benchmark */
						NodeWeightingBenchmark nodeWeightingBenchmark = new NodeWeightingBenchmark(confReq.getObjects(), confReq.getIterations());
						nodeWeightingBenchmark.warmUp(confReq.getWarmup());
						long weighting = nodeWeightingBenchmark.weightingBenchmark(confReq.getRuns());
						nodeInfo.setWeighting(weighting);
						log.info("Weighting\t " + weighting);
					}
					
					// If nothing untoward has happened during the benchmark (i.e. we didn't get disconnected or an NCP timeout)
					if(ncpState == ProtocolState.REG_ACK)
					{
						log.info("Sending configuration acknowledgement : Max simulations " + nodeInfo.getMaxSims());
						
						// Send the Configuration acknowledgement with our node info
						ncpMM.sendConfigurationAck(nodeInfo);
						
						// Now Registered enter ready State
						ncpState = ProtocolState.RDY;
					}
					
					// Exit Loop here.
					break Registration;
				}
				// Fall through for type 0 or anything type not valid in registration - this should never happen unless modify NCP
				case NCP.INVALID:
				default:
				{
					// Log the state and message type
					log.error(NCP.DisconnectReason.UnknownNCPType.appendfromStateInfoAndLastNCPType(ncpState, message.getType()));
					
					// NCP is gone
					ncpState = ProtocolState.DIS;
					
					// Exit Loop here.
					break Registration;
				}
			}
		}
		
		// NCP in ready state?
		if(ncpState != ProtocolState.RDY)
		{
			// Then registration is not complete - shutdown the NCP socket (and underlying TCP)
			shutdown(NCP.DisconnectReason.RegNotCompleted.toString());
			
			return false;
		}
		
		try
		{
			// Switch from the ready state timeout value to the NCP inactivity timeout value
			ncpMM.setTimeOut(NCP.Timeout.Inactivity);
		}
		catch(SocketException e)
		{
			// Failed to apply NCP Timeout
			shutdown(e.getMessage());
			
			return false;
		}
		
		// True only if ready state reached
		return true;
	}
	
	/**
	 * Retrieves any NCP Message from the socket<br>
	 * Only a complete NCP message will be returned<br>
	 * Note non-blocking if there is no data transfer in progress.<br>
	 *
	 * @return
	 * An NCP message<br>
	 * Null if there is no NCP message.
	 */
	public NCPMessage getReadyStateMessage()
	{
		// Cannot call this method outside of ready state
		if(ncpState != ProtocolState.RDY)
		{
			// NCP socket needs to be connected and registered
			shutdown(NCP.DisconnectReason.NCPNotReady.appendfromStateInfo(ncpState));
			
			return null;
		}
		
		// Get the message from MessageManager
		NCPMessage message = ncpMM.getMessage(false);
		
		// Has the MessageManager given us a message
		if(message == null)
		{
			// Is the MessageManager connected
			if(!ncpMM.isConnected())
			{
				// MessageManager is reporting it can no longer can use the underlying TCP socket
				shutdown(NCP.DisconnectReason.TCPDisconnected.appendfromStateInfo(ncpState));
			}
			
			// No message.
			return null;
		}
		
		// Got a message inspect type.
		int type = message.getType();
		
		// MessageManager does not know or care about NCP states so check now for message types are invalid for ready state
		if((type == NCP.RegReq) || (type == NCP.RegAck) || (type == NCP.RegNack) || (type == NCP.ConfReq) || (type == NCP.ConfAck))
		{
			// Invalid to register again when in the ready state.
			shutdown(NCP.DisconnectReason.InvRegSeq.appendfromStateInfoAndLastNCPType(ncpState, type));
			
			// No message
			return null;
		}
		
		// The valid message
		return message;
	}
	
	/**
	 * Reply to an AddSimReq.
	 *
	 * @param req
	 * The request to reply to.
	 * @param simId
	 * The added simulation id.
	 */
	public void sendAddSimReply(AddSimReq req, int simId)
	{
		ncpMM.sendAddSimReply(req, simId);
	}
	
	/**
	 * Reply to a NodeStatsRequest.
	 *
	 * @param req
	 * The request to reply to.
	 * @param nodeStatsSample
	 * The sample to use for the message.
	 */
	public void sendNodeStatsReply(NodeStatsRequest req, NodeStatsSample nodeStatsSample)
	{
		ncpMM.sendNodeStatsReply(req, nodeStatsSample);
	}
	
	/**
	 * Reply to a SimulationStatsRequest
	 *
	 * @param req
	 * The request to reply to.
	 * @param exporter
	 * The exporter to use for the message.
	 */
	public void sendSimulationStatsReply(SimulationStatsRequest req, StatExporter exporter)
	{
		ncpMM.sendSimulationStatsReply(req, exporter);
	}
	
	/**
	 * Notification based message.
	 * Note this is simulation manager information not scenario.
	 *
	 * @param e
	 * A SimulationStateChangedEvent with details about which simulation and state.
	 */
	public void sendSimulationStateChanged(SimulationStateChangedEvent e)
	{
		ncpMM.sendSimulationStateChanged(e);
	}
	
	/**
	 * Notification based message.
	 * Note this is simulation manager information not scenario.
	 *
	 * @param e
	 * A SimulationStatChangedEvent with details about which simulation and latest stats.
	 */
	public void sendSimulationStatChanged(SimulationStatChangedEvent e)
	{
		ncpMM.sendSimulationStatChanged(e);
	}
	
	/**
	 * Reports if the socket is connected.
	 *
	 * @return
	 * True if connected<br>
	 * False not.
	 */
	public boolean isConnected()
	{
		// MessageManager will be set to null once any call to shutdown() is complete.
		return(ncpMM != null);
	}
	
	/**
	 * Calls {@link #shutdown}
	 * Note IOException will never be thrown.
	 */
	@Override
	public void close() throws IOException
	{
		shutdown("Socket close requested");
	}
	
	/**
	 * Shuts down the NCP socket, MessageManager and underlying TCP socket.
	 * Also cleans up the ready state timer if it still exists.
	 */
	private void shutdown(String reason)
	{
		if(ncpMM != null)
		{
			ncpMM.shutdown(reason);
			
			ncpMM = null;
		}
	}
	
	private boolean isReadyStateTimeOut()
	{
		return NCP.Timeout.ReadyState.isTimedout((int) (System.currentTimeMillis() - ncpStartTime));
	}
}