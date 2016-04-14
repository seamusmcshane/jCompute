package jcompute.cluster.controlnode.computenodemanager;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import jcompute.JComputeEventBus;
import jcompute.cluster.computenode.nodedetails.NodeInfo;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeManagerItemStateEvent;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeManagerStateChange;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeManagerStateChangeRequest;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeStatsUpdate;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest.NodeItemRequestOperation;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest.NodeItemRequestResult;
import jcompute.cluster.controlnode.mapping.RemoteSimulationMapping;
import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.NCP.ProtocolState;
import jcompute.cluster.ncp.message.command.AddSimReply;
import jcompute.cluster.ncp.message.command.AddSimReq;
import jcompute.cluster.ncp.message.command.SimulationStatsReply;
import jcompute.cluster.ncp.message.command.SimulationStatsRequest;
import jcompute.cluster.ncp.message.control.NodeOrderlyShutdown;
import jcompute.cluster.ncp.message.monitoring.ActivityTestReply;
import jcompute.cluster.ncp.message.monitoring.ActivityTestRequest;
import jcompute.cluster.ncp.message.monitoring.NodeStatsReply;
import jcompute.cluster.ncp.message.monitoring.NodeStatsRequest;
import jcompute.cluster.ncp.message.notification.SimulationStatChanged;
import jcompute.cluster.ncp.message.notification.SimulationStateChanged;
import jcompute.cluster.ncp.message.registration.ConfigurationAck;
import jcompute.cluster.ncp.message.registration.ConfigurationRequest;
import jcompute.cluster.ncp.message.registration.RegistrationReqAck;
import jcompute.cluster.ncp.message.registration.RegistrationReqNack;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.simulationmanager.event.SimulationsManagerEvent;
import jcompute.simulationmanager.event.SimulationsManagerEventType;
import jcompute.stats.StatExporter;

public class ComputeNodeManager
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ComputeNodeManager.class);
	
	// Locks the node
	private Semaphore nodeLock = new Semaphore(1, false);
	
	// ComputeNode configuration
	private NodeInfo nodeInfo;
	
	private int activeSims = 0;
	private Semaphore activeSimsLock = new Semaphore(1, false);
	
	// This node cmd socket
	private final Socket cmdSocket;
	
	// Output Stream
	private DataOutputStream commandOutput;
	private DataInputStream cmdInput;
	private Thread ncpRX;
	
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
	
	private final int TX_FREQUENCY;
	
	// TX Message List
	private ArrayList<byte[]> txPendingList;
	private int pendingByteCount;
	
	/*
	 * Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (REMOTE)
	 * simId
	 */
	private ConcurrentHashMap<Integer, RemoteSimulationMapping> remoteSimulationMap;
	
	// Request Map
	private ConcurrentHashMap<Long, NodeItemRequest> remoteRequestMap;
	private long requestNum = 0;
	
	// Benchmark Configuration
	private final int BENCHMARK = 1;
	private final int NUM_OBJECTS = 1024;
	private final int ITERATIONS = 10000;
	private final int WARM_UP_ITERATIONS = 10000;
	private final int NUM_RUNS = 6;
	
	public ComputeNodeManager(int uid, Socket cmdSocket, int txFreq) throws IOException
	{
		nodeState = NodeManagerState.STARTING;
		
		nodeInfo = new NodeInfo();
		
		remoteSimulationMap = new ConcurrentHashMap<Integer, RemoteSimulationMapping>(8, 0.8f, 2);
		
		remoteRequestMap = new ConcurrentHashMap<Long, NodeItemRequest>(8, 0.8f, 2);
		
		NSMCPReadyTimeOut = 0;
		
		log.info("New ComputeNodeManager for connecting ComputeNode " + uid);
		
		// Internal Connection ID
		nodeInfo.setUid(uid);
		
		// ComputeNode Address
		nodeInfo.setAddress(cmdSocket.getInetAddress().getHostAddress());
		
		// A connected socket
		this.cmdSocket = cmdSocket;
		
		// Cmd Output Stream
		commandOutput = new DataOutputStream(new BufferedOutputStream(cmdSocket.getOutputStream()));
		
		// Cmd Input Stream
		cmdInput = new DataInputStream(new BufferedInputStream(cmdSocket.getInputStream()));
		
		// TX Pending Message List
		txPendingList = new ArrayList<byte[]>();
		pendingByteCount = 0;
		
		this.TX_FREQUENCY = txFreq;
		
		protocolState = ProtocolState.CON;
	}
	
	public void start()
	{
		JComputeEventBus.register(this);
		
		Thread registrationThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(handleRegistration())
					{
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
		
		registrationThread.setName("ComputeNodeManager " + nodeInfo.getUid() + " NCP Reg");
		registrationThread.start();
		
		Thread ncpTXThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				log.info("TX Ready");
				
				// Disconnect Recovery Loop
				while(nodeState != NodeManagerState.SHUTDOWN)
				{
					try
					{
						txPendingData();
						
						Thread.sleep(TX_FREQUENCY);
					}
					catch(InterruptedException e)
					{
						log.info(e.getMessage());
					}
					catch(IOException e)
					{
						log.info(e.getMessage());
						
						// Socket is closed but node will remain up to reconnect
						protocolState = ProtocolState.DIS;
					}
				}
			}
		});
		ncpTXThread.setName("ComputeNodeManager " + nodeInfo.getUid() + " NCP TX");
		ncpTXThread.start();
	}
	
	private boolean handleRegistration() throws IOException
	{
		log.info("Awaiting ComputeNode Registration");
		
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
				
				log.debug("Type " + type + " len " + len);
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
							log.warn("Sent Registration nack Protocol Version Mismatch - expected " + NCP.NCP_PROTOCOL_VERSION + " found "
							+ remoteProtocolVersion);
							
							txDataEnqueue(new RegistrationReqNack(NCP.ProtocolVersionMismatch, NCP.NCP_PROTOCOL_VERSION).toBytes());
							
							protocolState = ProtocolState.DIS;
							
							ackReg = false;
							
							// Record that reg has failed
							NCPRegFailed = true;
							
							reason = NCP.ProtocolVersionMismatch;
							value = remoteProtocolVersion;
						}
						else
						{
							log.info("Protocol Version is " + NCP.NCP_PROTOCOL_VERSION);
						}
						
						if(ackReg)
						{
							
							// If we are ok to proceed the ack the reg
							txDataEnqueue(new RegistrationReqAck(nodeInfo.getUid()).toBytes());
							
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
							log.info("ComputeNode registration ok");
							
							log.info("Now requesting node configuration and weighting");
							
							txDataEnqueue(new ConfigurationRequest(BENCHMARK, NUM_OBJECTS, ITERATIONS, WARM_UP_ITERATIONS, NUM_RUNS).toBytes());
						}
						else
						{
							log.error("ComputeNode registration not ok " + ruid);
							
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
						log.info("ComputeNode registration nack");
						protocolState = ProtocolState.DIS;
					}
					
				break;
				case NCP.ConfAck:
					
					if(protocolState == ProtocolState.REG)
					{
						log.info("Recieved Conf Ack");
						
						ConfigurationAck reqAck = new ConfigurationAck(data);
						
						nodeInfo.setMaxSims(reqAck.getMaxSims());
						
						// Set weighting if it is was requested
						if(BENCHMARK == 1)
						{
							nodeInfo.setWeighting(reqAck.getWeighting());
						}
						else
						{
							nodeInfo.setWeighting(Long.MAX_VALUE);
						}
						
						nodeInfo.setHWThreads(reqAck.getHwThreads());
						nodeInfo.setOperatingSystem(reqAck.getOs());
						nodeInfo.setSystemArch(reqAck.getArch());
						nodeInfo.setTotalOSMemory(reqAck.getTotalOSMemory());
						nodeInfo.setMaxJVMMemory(reqAck.getMaxJVMMemory());
						nodeInfo.setDescription(reqAck.getDescription());
						
						// Due to the work involved here its best to check debug is enabled before creating all the strings.
						if(log.isDebugEnabled())
						{
							log.debug("ComputeNode " + nodeInfo.getUid() + " Max Sims   : " + nodeInfo.getMaxSims());
							log.debug("ComputeNode " + nodeInfo.getUid() + " HW Threads : " + nodeInfo.getHWThreads());
							log.debug("ComputeNode " + nodeInfo.getUid() + " Weighting  : " + nodeInfo.getWeighting());
							log.debug("ComputeNode " + nodeInfo.getUid() + " OS         : " + nodeInfo.getOperatingSystem());
							log.debug("ComputeNode " + nodeInfo.getUid() + " Arch       : " + nodeInfo.getSystemArch());
							log.debug("ComputeNode " + nodeInfo.getUid() + " TotalMem   : " + nodeInfo.getTotalOSMemory());
							log.debug("ComputeNode " + nodeInfo.getUid() + " Description: " + nodeInfo.getDescription());
						}
						
						protocolState = ProtocolState.RDY;
						
					}
					else
					{
						log.error("ConfAck for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
					}
				break;
				case NCP.ActivityTestRequest:
				{
					ActivityTestRequest req = new ActivityTestRequest(data);
					txDataEnqueue(new ActivityTestReply(req, System.nanoTime()).toBytes());
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
			
			// Check NCP State
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
		ncpRX = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				log.info("RX Ready");
				
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
									
									long requestId = addSimReply.getRequestId();
									int simId = addSimReply.getSimId();
									
									log.info("AddSimReply : " + simId + " Request " + requestId);
									
									NodeItemRequest req = remoteRequestMap.remove(requestId);
									
									if(simId > 0)
									{
										req.setResult(NodeItemRequestResult.SUCESSFUL);
										
										RemoteSimulationMapping mapping = req.getMapping();
										
										mapping.setRemoteSimId(simId);
										
										remoteSimulationMap.put(simId, mapping);
										
										JComputeEventBus.post(new SimulationsManagerEvent(mapping.getLocalSimId(), SimulationsManagerEventType.AddedSim));
									}
									else
									{
										req.setResult(NodeItemRequestResult.FAILED);
										
										activeSimsLock.acquireUninterruptibly();
										
										activeSims--;
										
										activeSimsLock.release();
									}
									
									JComputeEventBus.post(req);
								}
								else
								{
									log.error("AddSimReply for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
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
										
										// The finished state is not posted yet stats have to be fetched first
										if(mapping.getBatchItem().hasStatsEnabled())
										{
											// Send a stats request
											txDataEnqueue(new SimulationStatsRequest(mapping).toBytes());
										}
										else
										{
											// Simulation Finished - there are no stats
											processFinishedSimulation(mapping, null);
										}
									}
									else
									{
										// Forward an encapsulated simstate event
										JComputeEventBus.post(new ComputeNodeManagerItemStateEvent(new SimulationStateChangedEvent(mapping.getLocalSimId(),
										stateChanged.getState(), stateChanged.getRunTime(), stateChanged.getStepCount(), stateChanged.getEndEvent(), null)));
									}
									
								}
								else
								{
									log.error("SimStateNoti for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
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
										
										// Post the event as if from a local simulation
										// This is unsafe but fast as this event can and WILL arrive before add/remove/finished events.
										// Listeners as such need to deal with null lookups.
										JComputeEventBus.post(new SimulationStatChangedEvent(mapping.getLocalSimId(), statChanged.getTime(), statChanged
										.getStepNo(), statChanged.getProgress(), statChanged.getAsps()));
										
									}
									else
									{
										log.warn("No mapping found for " + statChanged.info());
										
									}
								}
								else
								{
									log.error("SimStatNoti for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
								}
								
							break;
							case NCP.NodeStatsReply:
								if(protocolState == ProtocolState.RDY)
								{
									NodeStatsReply nodeStatsReply = new NodeStatsReply(data);
									
									log.debug("Recieved NodeStatsReply");
									
									JComputeEventBus.post(new ComputeNodeStatsUpdate(nodeInfo.getUid(), nodeStatsReply.getSequenceNum(), nodeStatsReply
									.getNodeStats()));
									
								}
								else
								{
									log.error("NodeStatsReply for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
								}
							break;
							case NCP.SimStatsReply:
							{
								if(protocolState == ProtocolState.RDY)
								{
									log.info("Recieved Sim Stats Reply");
									
									SimulationStatsReply statsReply = new SimulationStatsReply(data);
									
									// SimId
									int simId = statsReply.getSimId();
									
									// find the mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.get(simId);
									
									processFinishedSimulation(mapping, statsReply.getStatExporter(mapping.getExportFormat(), mapping.getFileNameSuffix()));
								}
								else
								{
									log.error("SimStats for node " + nodeInfo.getUid() + " not valid in state " + protocolState.toString());
								}
							}
							break;
							case NCP.ActivityTestRequest:
							{
								ActivityTestRequest req = new ActivityTestRequest(data);
								txDataEnqueue(new ActivityTestReply(req, System.nanoTime()).toBytes());
							}
							break;
							case NCP.ActivityTestReply:
							{
								// TODO - Manager has sent a request
								// ConnectionTestRequest req = new ConnectionTestRequest(data);
								// txDataEnqueue(new ConnectionTestReply(req).toBytes());
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
					// Exit // Do ComputeNode Shutdown
					
				}
				catch(IOException e)
				{
					log.warn("ComputeNodeManager " + nodeInfo.getUid() + " NCP RX exited");
					// Exit // Do ComputeNodeManager Shutdown
					
					protocolState = ProtocolState.DIS;
					nodeState = NodeManagerState.SHUTDOWN;
				}
				
			}
		});
		ncpRX.setName("ComputeNodeManager " + nodeInfo.getUid() + " NCP RX");
		
		// Start Processing
		ncpRX.start();
	}
	
	/*
	 * *****************************************************************************************************
	 * Internal Methods
	 *****************************************************************************************************/
	/*
	 * Process finished simulation
	 */
	private void processFinishedSimulation(RemoteSimulationMapping mapping, StatExporter exporter)
	{
		int simId = mapping.getRemoteSimId();
		
		// Remove mapping...
		remoteSimulationMap.remove(simId);
		
		activeSimsLock.acquireUninterruptibly();
		
		// Remote Sim is auto-removed when
		// finished
		activeSims--;
		
		activeSimsLock.release();
		
		// Post the event as if from a local
		// simulation
		SimulationStateChanged finalStateChanged = mapping.getFinalStateChanged();
		
		// Forward an encapsulated simstate event
		JComputeEventBus.post(new ComputeNodeManagerItemStateEvent(new SimulationStateChangedEvent(mapping.getLocalSimId(), finalStateChanged.getState(),
		finalStateChanged.getRunTime(), finalStateChanged.getStepCount(), finalStateChanged.getEndEvent(), exporter)));
	}
	
	/*
	 * ***************************************************************************************************
	 * TX Transfer
	 *****************************************************************************************************/
	
	// Enqueue Messages to be sent
	private synchronized void txDataEnqueue(byte[] bytes)
	{
		txPendingList.add(bytes);
		
		// Byte count pending
		pendingByteCount += bytes.length;
	}
	
	// Clears All Pending Messages
	private synchronized void clearPendingTXList()
	{
		// TX Message List
		txPendingList.clear();
		
		pendingByteCount = 0;
		
		log.info("Cleared Pending TX List");
	}
	
	// Send Pending Messages
	private synchronized void txPendingData() throws IOException
	{
		if(pendingByteCount == 0)
		{
			return;
		}
		
		// The backing array.
		byte[] concatenated = new byte[pendingByteCount];
		
		// Create a byte buffer to concatenate the data.
		ByteBuffer databuffer = ByteBuffer.wrap(concatenated);
		
		for(byte[] bytes : txPendingList)
		{
			databuffer.put(bytes);
		}
		
		// Data Cleared
		txPendingList.clear();
		
		// Count reset
		pendingByteCount = 0;
		
		// Send the concatenated data in bulk
		commandOutput.write(concatenated);
		
		// Flush the data
		commandOutput.flush();
	}
	
	/**
	 * Returns if the node is in the ready state.
	 * 
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
		log.debug("ComputeNode " + nodeInfo.getUid() + " TimeOut@" + NSMCPReadyTimeOut);
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
		log.info("Removing ComputeNode Manager for ComputeNode " + nodeInfo.getUid() + " Reason : " + reason);
		
		try
		{
			if(cmdSocket != null)
			{
				cmdSocket.close();
				log.info("ComputeNode " + nodeInfo.getUid() + " Command socket closed");
			}
			
		}
		catch(IOException e)
		{
			log.error("ComputeNode " + nodeInfo.getUid() + " Command socket already closed");
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
	 * 
	 * @param scenarioText
	 * @param initialStepRate
	 * @param mapping
	 * @return
	 */
	public void addSimulation(String scenarioText, RemoteSimulationMapping mapping)
	{
		nodeLock.acquireUninterruptibly();
		
		log.debug("ComputeNode " + nodeInfo.getUid() + " AddSim");
		
		// Increment the request id
		requestNum++;
		
		// Create the request
		NodeItemRequest request = new NodeItemRequest(mapping, NodeItemRequestOperation.ADD);
		
		// Map it for later look up
		remoteRequestMap.put(requestNum, request);
		
		// Enqueue an add sim req - with the requestNum
		txDataEnqueue(new AddSimReq(requestNum, scenarioText).toBytes());
		
		activeSimsLock.acquireUninterruptibly();
		
		activeSims++;
		
		activeSimsLock.release();
		
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
		txDataEnqueue(new NodeStatsRequest(id).toBytes());
	}
	
	/**
	 * Check for valid orderly state transitions. Direct changes to SHUTDOWN state are handled programmatically as error conditions.
	 * 
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
				
				// Now that the ComputeNodeManager is Paused it can resume or do a
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
			// disconnects and the ComputeNodeManager will enter the shutdown state.
			if(newState == NodeManagerState.SHUTDOWN)
			{
				log.info("Sending NodeOrderlyShutdown");
				
				txDataEnqueue(new NodeOrderlyShutdown().toBytes());
			}
			else
			{
				nodeState = newState;
				
				log.info("NodeManagerState changed to " + nodeState.toString());
			}
			
			JComputeEventBus.post(new ComputeNodeManagerStateChange(nodeInfo.getUid(), newState));
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
	
	/*
	 * ************************************************************************************************************************************************************
	 * Event Bus Subscribers
	 **************************************************************************************************************************************************************/
	
	@Subscribe
	public void ComputeNodeManagerStateChangeRequest(ComputeNodeManagerStateChangeRequest e)
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
		
		@Override
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
	}
	
}
