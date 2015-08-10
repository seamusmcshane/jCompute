package jCompute.Cluster.Controller;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Event.NodeManagerStateChange;
import jCompute.Cluster.Controller.Event.NodeManagerStateChangeRequest;
import jCompute.Cluster.Controller.Event.NodeStatsUpdate;
import jCompute.Cluster.Controller.Mapping.RemoteSimulationMapping;
import jCompute.Cluster.Node.NodeDetails.NodeInfo;
import jCompute.Cluster.Protocol.NCP;
import jCompute.Cluster.Protocol.Command.AddSimReply;
import jCompute.Cluster.Protocol.Command.AddSimReq;
import jCompute.Cluster.Protocol.Command.RemoveSimAck;
import jCompute.Cluster.Protocol.Command.SimulationStatsReply;
import jCompute.Cluster.Protocol.Command.SimulationStatsRequest;
import jCompute.Cluster.Protocol.Command.StartSimCMD;
import jCompute.Cluster.Protocol.Control.NodeOrderlyShutdown;
import jCompute.Cluster.Protocol.NCP.ProtocolState;
import jCompute.Cluster.Protocol.Monitoring.NodeStatsReply;
import jCompute.Cluster.Protocol.Monitoring.NodeStatsRequest;
import jCompute.Cluster.Protocol.Notification.SimulationStatChanged;
import jCompute.Cluster.Protocol.Notification.SimulationStateChanged;
import jCompute.Cluster.Protocol.Registration.ConfigurationAck;
import jCompute.Cluster.Protocol.Registration.ConfigurationRequest;
import jCompute.Cluster.Protocol.Registration.RegistrationReqAck;
import jCompute.Cluster.Protocol.Registration.RegistrationReqNack;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class NodeManager
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(NodeManager.class);
	
	// Locks the node
	private Semaphore nodeLock = new Semaphore(1, false);
	
	// Node configuration
	private NodeInfo nodeInfo;
	
	private int activeSims = 0;
	private Semaphore activeSimsLock = new Semaphore(1, false);
	
	// This node cmd socket
	private final Socket cmdSocket;
	
	// Output Stream
	private DataOutputStream cmdOutput;
	private DataInputStream cmdInput;
	private Semaphore cmdTxLock = new Semaphore(1, false);
	private Thread cmdRecieveThread;
	
	private ProtocolState protocolState;
	
	// Counter for NCP state machine
	private int NSMCPReadyTimeOut;
	
	// Has the node failed Registration
	private boolean NCPRegFailed = false;
	private int reason = 0;
	private int value = 0;
	
	// Is the remote node active. (connection up)
	// private boolean nodeManagerRunnning = false;
	
	private NodeManagerState nodeState;
	
	// Semaphores for methods to wait on
	private Semaphore addSimWait = new Semaphore(0, false);
	private Semaphore remSimWait = new Semaphore(0, false);
	
	// Add Sim MSG box Vars
	private int addSimId = -1;
	
	/*
	 * Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (REMOTE)
	 * simId
	 */
	private ConcurrentHashMap<Integer, RemoteSimulationMapping> remoteSimulationMap;
	
	public NodeManager(int uid, Socket cmdSocket) throws IOException
	{
		nodeState = NodeManagerState.STARTING;
		
		nodeInfo = new NodeInfo();
		
		remoteSimulationMap = new ConcurrentHashMap<Integer, RemoteSimulationMapping>(8);
		
		NSMCPReadyTimeOut = 0;
		
		log.info("New Node Manager " + uid);
		
		// Internal Connection ID
		nodeInfo.setUid(uid);
		
		// Node Address
		nodeInfo.setAddress(cmdSocket.getInetAddress().getHostAddress());
		
		// A connected socket
		this.cmdSocket = cmdSocket;
		
		// Cmd Output Stream
		cmdOutput = new DataOutputStream(new BufferedOutputStream(cmdSocket.getOutputStream()));
		
		// Cmd Input Stream
		cmdInput = new DataInputStream(new BufferedInputStream(cmdSocket.getInputStream()));
		
		protocolState = ProtocolState.CON;
		
		JComputeEventBus.register(this);
		
		Thread nodeManager = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(handleRegistration())
					{
						log.info("Registration Succeeded");
						createCMDRecieveThread();
					}
					else
					{
						log.info("Registration Failed");
						nodeState = NodeManagerState.SHUTDOWN;
					}
					
				}
				catch(IOException e)
				{
					nodeState = NodeManagerState.SHUTDOWN;
				}
			}
		});
		
		nodeManager.setName("NodeManager");
		nodeManager.start();
	}
	
	private boolean handleRegistration() throws IOException
	{
		log.info("Awaiting Registration");
		
		boolean registered = false;
		boolean finished = false;
		
		int type = -1;
		int len = -1;
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		while(!finished)
		{
			// Detect Frame
			type = cmdInput.readInt();
			len = cmdInput.readInt();
			
			// Allocate here to avoid duplication of allocation code
			if(len > 0)
			{
				// Destination
				backingArray = new byte[len];
				
				// Copy from the socket
				cmdInput.readFully(backingArray, 0, len);
				
				// Wrap the backingArray
				data = ByteBuffer.wrap(backingArray);
				
				log.info("Type " + type + " len " + len);
			}
			
			switch(type)
			{
				case NCP.RegReq:
					log.info("Recieved Registration Request");
					
					/*
					 * A socket has been connected and we have just received a
					 * registration request
					 */
					if(protocolState == ProtocolState.CON)
					{
						// Use to determine if to send a reg ack
						boolean ackReg = true;
						
						// Check the protocl versions match
						int remoteProtocolVersion = data.getInt();
						if(remoteProtocolVersion != NCP.NCP_PROTOCOL_VERSION)
						{
							log.warn("Protocol Version Mismatch");
							
							sendCMDMessage(new RegistrationReqNack(NCP.ProtocolVersionMismatch, NCP.NCP_PROTOCOL_VERSION).toBytes());
							
							log.info("Sent Registration nack");
							
							protocolState = ProtocolState.DIS;
							
							ackReg = false;
							
							// Record that reg has failed
							NCPRegFailed = true;
							
							reason = NCP.ProtocolVersionMismatch;
							value = remoteProtocolVersion;
						}
						else
						{
							log.info("Protocol Version OK!");
						}
						
						if(ackReg)
						{
							
							// If we are ok to proceed the ack the reg
							sendCMDMessage(new RegistrationReqAck(nodeInfo.getUid()).toBytes());
							
							log.info("Sent Registration Ack");
							
							protocolState = ProtocolState.REG;
						}
					}
					else
					{
						log.error("Registration Request for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
						
						// invalid sequence
						protocolState = ProtocolState.DIS;
					}
				break;
				case NCP.RegAck:
					
					/*
					 * A socket has been connected, the remove node has already
					 * sent us a reg req we have sent a reg ack and are awaiting
					 * confirmation. - We get confirmation and request the node
					 * configuration.
					 */
					if(protocolState == ProtocolState.REG)
					{
						RegistrationReqAck reqAck = new RegistrationReqAck(data);
						
						int ruid = reqAck.getUid();
						
						// Check the node is sane (UID should be
						// identical to the one we sent)
						if(nodeInfo.getUid() == ruid)
						{
							log.info("Node registration ok");
							
							log.info("Now requesting node configuration and weighting");
							sendCMDMessage(new ConfigurationRequest(0, 2048, 5000, 1000, 5).toBytes());
						}
						else
						{
							log.error("Node registration not ok " + ruid);
							
							protocolState = ProtocolState.DIS;
						}
						
					}
				break;
				case NCP.RegNack:
					
					/*
					 * A socket has been connected, Remote node has decided to
					 * cancel the registration
					 */
					if(protocolState == ProtocolState.CON || protocolState == ProtocolState.REG)
					{
						log.info("Node registration nack");
						protocolState = ProtocolState.DIS;
					}
				
				break;
				case NCP.ConfAck:
					
					if(protocolState == ProtocolState.REG)
					{
						log.info("Recieved Conf Ack");
						
						ConfigurationAck reqAck = new ConfigurationAck(data);
						
						nodeInfo.setMaxSims(reqAck.getMaxSims());
						nodeInfo.setWeighting(reqAck.getWeighting());
						
						nodeInfo.setHWThreads(reqAck.getHwThreads());
						nodeInfo.setOperatingSystem(reqAck.getOs());
						nodeInfo.setSystemArch(reqAck.getArch());
						nodeInfo.setTotalOSMemory(reqAck.getTotalOSMemory());
						nodeInfo.setMaxJVMMemory(reqAck.getMaxJVMMemory());
						nodeInfo.setDescription(reqAck.getDescription());
						
						log.debug("Node " + nodeInfo.getUid() + " Max Sims   : " + nodeInfo.getMaxSims());
						log.debug("Node " + nodeInfo.getUid() + " HW Threads : " + nodeInfo.getHWThreads());
						log.debug("Node " + nodeInfo.getUid() + " Weighting  : " + nodeInfo.getWeighting());
						log.debug("Node " + nodeInfo.getUid() + " OS         : " + nodeInfo.getOperatingSystem());
						log.debug("Node " + nodeInfo.getUid() + " Arch       : " + nodeInfo.getSystemArch());
						log.debug("Node " + nodeInfo.getUid() + " TotalMem   : " + nodeInfo.getTotalOSMemory());
						log.debug("Node " + nodeInfo.getUid() + " Description: " + nodeInfo.getDescription());
						
						protocolState = ProtocolState.RDY;
						
					}
					else
					{
						log.error("ConfAck for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
					}
				
				break;
				// Test Frame or Garbage
				case NCP.INVALID:
				default:
					log.error("Recieved Invalid Frame");
					protocolState = ProtocolState.DIS;
					log.error("Error Type " + type + " len " + len);
				break;
			}
			
			// Check Node State
			switch(protocolState)
			{
				case DIS:
					log.info("Protocol State DIS : Closing Socket");
					cmdSocket.close();
					finished = true;
					registered = false;
				break;
				case RDY:
					log.info("Protocol State RDY");
					finished = true;
					registered = true;
					ChangeManagerState(NodeManagerState.RUNNING);
				break;
			}
		}
		
		return registered;
	}
	
	private void createCMDRecieveThread()
	{
		// The Command Receive Thread
		cmdRecieveThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					int type = -1;
					int len = -1;
					byte[] backingArray = null;
					ByteBuffer data = null;
					
					while(nodeState != NodeManagerState.SHUTDOWN)
					{
						if(nodeState == NodeManagerState.PAUSING)
						{
							if(activeSims == 0)
							{
								ChangeManagerState(NodeManagerState.PAUSED);
							}
						}
						
						// Detect Frame
						type = cmdInput.readInt();
						len = cmdInput.readInt();
						
						// Allocate here to avoid duplication of allocation code
						if(len > 0)
						{
							// Destination
							backingArray = new byte[len];
							
							// Copy from the socket
							cmdInput.readFully(backingArray, 0, len);
							
							// Wrap the backingArray
							data = ByteBuffer.wrap(backingArray);
							
							log.debug("Type " + type + " len " + len);
						}
						
						switch(type)
						{
							case NCP.AddSimReply:
								
								if(protocolState == ProtocolState.RDY)
								{
									AddSimReply addSimReply = new AddSimReply(data);
									addSimId = addSimReply.getSimId();
									
									log.info("AddSimReply : " + addSimId);
									
									addSimWait.release();
								}
								else
								{
									log.error("AddSimReply for node " + nodeInfo.getUid() + " not valid in state "
											+ protocolState.toString());
								}
							
							break;
							case NCP.SimStateNoti:
								
								if(protocolState == ProtocolState.RDY)
								{
									// Create the state object
									SimulationStateChanged stateChanged = new SimulationStateChanged(data);
									
									// find the mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.get(stateChanged.getSimId());
									
									// Debug as these are excessive output
									log.info(stateChanged.info());
									log.debug("New " + mapping.info());
									
									if(stateChanged.getState() == SimState.FINISHED)
									{
										// Track the final state
										mapping.setFinalStateChanged(stateChanged);
										
										// Send a stats request
										sendCMDMessage(new SimulationStatsRequest(mapping).toBytes());
										
										// The finished state is not posted yet
										// - stats have to be fetched first
										
									}
									else
									{
										// Post the event as if from a local
										// simulation
										JComputeEventBus.post(new SimulationStateChangedEvent(mapping.getLocalSimId(), stateChanged
												.getState(), stateChanged.getRunTime(), stateChanged.getStepCount(), stateChanged
												.getEndEvent(), null));
									}
									
								}
								else
								{
									log.error("SimStateNoti for node " + nodeInfo.getUid() + " not valid in state "
											+ protocolState.toString());
								}
							
							break;
							case NCP.SimStatNoti:
								
								if(protocolState == ProtocolState.RDY)
								{
									// Create the state object
									SimulationStatChanged statChanged = new SimulationStatChanged(data);
									
									log.debug(statChanged.info());
									
									// find the mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.get(statChanged.getSimId());
									
									// We can get stat changes during add sim.
									// (ie when mapping created)
									if(mapping != null)
									{
										log.debug("New " + mapping.info());
										
										// Post the event as if from a local
										// simulation
										JComputeEventBus.post(new SimulationStatChangedEvent(mapping.getLocalSimId(),
												statChanged.getTime(), statChanged.getStepNo(), statChanged.getProgress(), statChanged
														.getAsps()));
										
									}
									else
									{
										log.warn("No mapping found for " + statChanged.info());
										
									}
								}
								else
								{
									log.error("SimStatNoti for node " + nodeInfo.getUid() + " not valid in state "
											+ protocolState.toString());
								}
							
							break;
							case NCP.RemSimAck:
								if(protocolState == ProtocolState.RDY)
								{
									RemoveSimAck removeSimAck = new RemoveSimAck(data);
									
									log.info("Recieved RemSimAck : " + removeSimAck.getSimId());
									
									remSimWait.release();
								}
								else
								{
									log.error("RemSimAck for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
								}
							break;
							case NCP.NodeStatsReply:
								if(protocolState == ProtocolState.RDY)
								{
									NodeStatsReply nodeStatsReply = new NodeStatsReply(data);
									
									log.debug("Recieved NodeStatsReply");
									
									JComputeEventBus.post(new NodeStatsUpdate(nodeInfo.getUid(), nodeStatsReply.getSequenceNum(),
											nodeStatsReply.getNodeStats()));
									
								}
								else
								{
									log.error("NodeStatsReply for node " + nodeInfo.getUid() + " not valid in state "
											+ protocolState.toString());
								}
							break;
							case NCP.SimStats:
								
								if(protocolState == ProtocolState.RDY)
								{
									log.info("Recieved Sim Stats");
									
									// Needed for the mapping lookup
									int simId = data.getInt();
									
									// find and remove mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.remove(simId);
									
									SimulationStatsReply statsReply = new SimulationStatsReply(simId, data, mapping.getExportFormat(),
											mapping.getFileNameSuffix());
									
									activeSimsLock.acquireUninterruptibly();
									
									// Remote Sim is auto-removed when
									// finished
									activeSims--;
									
									activeSimsLock.release();
									
									// Post the event as if from a local
									// simulation
									SimulationStateChanged finalStateChanged = mapping.getFinalStateChanged();
									
									JComputeEventBus.post(new SimulationStateChangedEvent(mapping.getLocalSimId(), finalStateChanged
											.getState(), finalStateChanged.getRunTime(), finalStateChanged.getStepCount(),
											finalStateChanged.getEndEvent(), statsReply.getStatExporter()));
								}
								else
								{
									log.error("SimStats for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
								}
							
							break;
							// Test Frame or Garbage
							case NCP.INVALID:
							default:
								log.error("Recieved Invalid Frame");
								protocolState = ProtocolState.DIS;
								
								log.error("Error Type " + type + " len " + len);
							
							break;
						
						}
						
						if(protocolState == ProtocolState.DIS)
						{
							log.info("Protocol State : " + protocolState.toString());
							nodeState = NodeManagerState.SHUTDOWN;
						}
					}
					// Exit // Do Node Shutdown
					
				}
				catch(IOException e)
				{
					log.warn("Node " + nodeInfo.getUid() + " Recieve Thread exited");
					// Exit // Do Node Shutdown
					
					protocolState = ProtocolState.DIS;
					nodeState = NodeManagerState.SHUTDOWN;
					
					// Explicit release of all semaphores
					addSimWait.release();
					remSimWait.release();
				}
				
			}
		});
		
		cmdRecieveThread.setName("Node " + nodeInfo.getUid() + " Command Recieve");
		
		// Start Processing
		cmdRecieveThread.start();
		
	}
	
	private void sendCMDMessage(byte[] bytes) throws IOException
	{
		cmdTxLock.acquireUninterruptibly();
		
		cmdOutput.write(bytes);
		cmdOutput.flush();
		
		cmdTxLock.release();
	}
	
	/**
	 * Returns if the node is in the ready state.
	 * @return
	 */
	public boolean isReady()
	{
		if(protocolState == ProtocolState.RDY)
		{
			return true;
		}
		
		return false;
	}
	
	public boolean hasFailedReg()
	{
		return NCPRegFailed;
	}
	
	public String getRegFailedReason()
	{
		switch(reason)
		{
			case NCP.ProtocolVersionMismatch:
				
				return "Protocol Version Mismatch - Local " + NCP.NCP_PROTOCOL_VERSION + " Remote " + value;
				
			default:
				
				return "RegNack : Unknown Reason " + reason + " value " + value;
		}
	}
	
	public void incrementTimeOut(int value)
	{
		NSMCPReadyTimeOut += value;
		log.info("Node " + nodeInfo.getUid() + " TimeOut@" + NSMCPReadyTimeOut);
	}
	
	public int getReadyStateTimeOutValue()
	{
		return NSMCPReadyTimeOut;
	}
	
	public ArrayList<Integer> getRecoverableSimsIds()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		Iterator<Entry<Integer, RemoteSimulationMapping>> itr = remoteSimulationMap.entrySet().iterator();
		
		while(itr.hasNext())
		{
			int simId = itr.next().getValue().getLocalSimId();
			
			list.add(simId);
			
			JComputeEventBus.post(new SimulationsManagerEvent(simId, SimulationsManagerEventType.RemovedSim));
			
			itr.remove();
		}
		
		return list;
	}
	
	public void destroy(String reason)
	{
		log.info("Removing Node Manager for Node " + nodeInfo.getUid() + " Reason : " + reason);
		
		try
		{
			if(cmdSocket != null)
			{
				cmdSocket.close();
				log.info("Node " + nodeInfo.getUid() + " Command socket closed");
			}
			
		}
		catch(IOException e)
		{
			log.error("Node " + nodeInfo.getUid() + " Command socket already closed");
		}
		
	}
	
	public int getUid()
	{
		return nodeInfo.getUid();
	}
	
	public int getMaxSims()
	{
		return nodeInfo.getMaxSims();
	}
	
	public boolean isShutdown()
	{
		return (nodeState == NodeManagerState.SHUTDOWN) ? true : false;
	}
	
	public boolean isRunning()
	{
		return (nodeState == NodeManagerState.RUNNING) ? true : false;
	}
	
	public boolean hasFreeSlot()
	{
		int tActive = 0;
		
		if(isRunning())
		{
			activeSimsLock.acquireUninterruptibly();
			
			tActive = activeSims;
			
			activeSimsLock.release();
			
			if(tActive < nodeInfo.getMaxSims())
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Add a Simulation - Blocking
	 * @param scenarioText
	 * @param initialStepRate
	 * @param mapping
	 * @return
	 */
	public int addSim(String scenarioText, RemoteSimulationMapping mapping)
	{
		nodeLock.acquireUninterruptibly();
		
		log.info("Node " + nodeInfo.getUid() + " AddSim");
		
		try
		{
			// addSimMsgBoxVarLock.acquireUninterruptibly();
			
			// Shared variable
			addSimId = -1;
			
			// Create and Send add Sim Req - simulation Max speed
			sendCMDMessage(new AddSimReq(scenarioText, -1).toBytes());
			
			// addSimMsgBoxVarLock.release();
			
			// Wait until we are released (by timer or receive thread)
			addSimWait.acquireUninterruptibly();
			
			// addSimMsgBoxVarLock.acquireUninterruptibly();
			
			if(addSimId == -1)
			{
				// addSimMsgBoxVarLock.release();
				nodeLock.release();
				
				return -1;
			}
			else
			{
				
				mapping.setRemoteSimId(addSimId);
				
				remoteSimulationMap.put(addSimId, mapping);
				
				activeSimsLock.acquireUninterruptibly();
				
				activeSims++;
				
				activeSimsLock.release();
				
				// addSimMsgBoxVarLock.release();
				nodeLock.release();
				
				return addSimId;
			}
			
		}
		catch(IOException e)
		{
			nodeLock.release();
			
			// Connection is gone add sim failed
			log.error("Node " + nodeInfo.getUid() + " Error in add Sim");
			
			return -1;
		}
		
	}
	
	public void startSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();
		
		try
		{
			sendCMDMessage(new StartSimCMD(remoteSimId).toBytes());
		}
		catch(IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeInfo.getUid() + " Error in Start Sim");
			
		}
		
		nodeLock.release();
	}
	
	public long getWeighting()
	{
		return nodeInfo.getWeighting();
	}
	
	public int getActiveSims()
	{
		int tActive = 0;
		
		activeSimsLock.acquireUninterruptibly();
		
		tActive = activeSims;
		
		activeSimsLock.release();
		
		return tActive;
	}
	
	public String getAddress()
	{
		return nodeInfo.getAddress();
	}
	
	public NodeInfo getNodeConfig()
	{
		return nodeInfo;
	}
	
	public void triggerNodeStatRequest(int id)
	{
		try
		{
			sendCMDMessage(new NodeStatsRequest(id).toBytes());
		}
		catch(IOException e)
		{
			log.error("Fail to send Node stats request for Node " + nodeInfo.getUid());
		}
	}
	
	/**
	 * Removes a simulation.
	 * @param remoteSimId
	 */
	public void removeSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();
		
		// NA - Finished Simulation are auto-removed when stats are fetched,
		// calling this will remove the mapping.
		// - we assume calling this method means you do not want stats or the
		// simulation.
		
		RemoteSimulationMapping mapping = remoteSimulationMap.remove(remoteSimId);
		
		try
		{
			// Stats have already been recieved if the mapping is null
			if(mapping != null)
			{
				// Send a stats request
				sendCMDMessage(new SimulationStatsRequest(mapping).toBytes());
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		nodeLock.release();
	}
	
	/**
	 * Check for valid orderly state transitions.
	 * Direct changes to SHUTDOWN state are handled programmatically as error
	 * conditions.
	 * @param newState
	 */
	private void ChangeManagerState(NodeManagerState newState)
	{
		NodeManagerState[] validStates = null;
		
		switch(nodeState)
		{
			case STARTING:
				validStates = new NodeManagerState[]
				{
					NodeManagerState.RUNNING
				};
			break;
			case RUNNING:
				validStates = new NodeManagerState[]
				{
					NodeManagerState.PAUSING
				};
			break;
			case PAUSING:
				validStates = new NodeManagerState[]
				{
					NodeManagerState.PAUSED
				};
			break;
			case PAUSED:
				
				// Now that the NodeManager is Paused it can resume or do a
				// complete shutdown.
				validStates = new NodeManagerState[]
				{
					NodeManagerState.RUNNING, NodeManagerState.SHUTDOWN
				};
			break;
			default:
				validStates = new NodeManagerState[]{};
			break;
		}
		
		// Check Valid Trans
		if(containsState(validStates, newState))
		{
			// The remote node will perform an orderly shutdown if it has no
			// outstanding stats or running simulations. After which it
			// disconnects and the NodeManager will enter the shutdown state.
			if(newState == NodeManagerState.SHUTDOWN)
			{
				try
				{
					log.info("Sending NodeOrderlyShutdown");
					
					sendCMDMessage(new NodeOrderlyShutdown().toBytes());
				}
				catch(IOException e)
				{
					log.error("Error Sending node shutdown - node " + nodeInfo.getUid());
					e.printStackTrace();
				}
			}
			else
			{
				nodeState = newState;
				
				log.info("now " + nodeState.toString());
			}
			
			JComputeEventBus.post(new NodeManagerStateChange(nodeInfo.getUid(), newState));
		}
		else
		{
			log.error("Invalid Transition Attempted - from state " + nodeState.toString() + " to invalid state - " + newState.toString());
		}
	}
	
	private boolean containsState(NodeManagerState[] states, NodeManagerState target)
	{
		for(NodeManagerState state : states)
		{
			if(state == target)
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Subscribe
	public void NodeManagerStateChangeRequest(NodeManagerStateChangeRequest e)
	{
		if(nodeInfo.getUid() == e.getUid())
		{
			ChangeManagerState(e.getState());
		}
	}
	
	/** State Enum */
	public enum NodeManagerState
	{
		STARTING("Starting"), RUNNING("Running"), PAUSING("Pausing"), PAUSED("Paused"), SHUTDOWN("Shutdown");
		
		private final String name;
		
		private NodeManagerState(String name)
		{
			this.name = name;
		}
		
		public String toString()
		{
			return name;
		}
		
		public static NodeManagerState fromInt(int v)
		{
			NodeManagerState state = null;
			switch(v)
			{
				case 0:
					state = NodeManagerState.STARTING;
				break;
				case 1:
					state = NodeManagerState.RUNNING;
				break;
				case 2:
					state = NodeManagerState.PAUSING;
				break;
				case 3:
					state = NodeManagerState.PAUSED;
				break;
				case 4:
					state = NodeManagerState.SHUTDOWN;
				break;
				default:
					/* Invalid Usage */
					state = null;
			}
			
			return state;
		}
	};
	
}
