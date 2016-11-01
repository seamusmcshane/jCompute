package jcompute.cluster.controlnode.computenodemanager;

import java.io.IOException;
import java.net.Socket;
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
import jcompute.cluster.controlnode.NodeManagerStateMachine;
import jcompute.cluster.controlnode.NodeManagerStateMachine.NodeManagerState;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeManagerItemStateEvent;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeManagerStateChangeRequest;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeStatsUpdate;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest.NodeItemRequestOperation;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest.NodeItemRequestResult;
import jcompute.cluster.controlnode.mapping.RemoteSimulationMapping;
import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.NCPSocket;
import jcompute.cluster.ncp.message.NCPMessage;
import jcompute.cluster.ncp.message.command.AddSimReply;
import jcompute.cluster.ncp.message.command.SimulationResultsReply;
import jcompute.cluster.ncp.message.monitoring.NodeStatsReply;
import jcompute.cluster.ncp.message.notification.SimulationStatChanged;
import jcompute.cluster.ncp.message.notification.SimulationStateChanged;
import jcompute.results.export.Result;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.simulationmanager.event.SimulationsManagerEvent;
import jcompute.simulationmanager.event.SimulationsManagerEventType;

public class ComputeNodeManager2
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ComputeNodeManager2.class);
	
	// Locks the node
	private Semaphore nodeLock = new Semaphore(1, false);
	
	// Manager State
	private NodeManagerStateMachine sm;
	
	// ComputeNode configuration
	private NodeInfo nodeInfo;
	
	private int activeSims = 0;
	private Semaphore activeSimsLock = new Semaphore(1, false);
	
	// This ComputeNodeManagers NCP socket
	private final NCPSocket ncpSocket;
	
	// ComputeNodeManagerStateChangeRequest
	private NodeManagerState newState = null;
	
	// Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (REMOTE-simId)
	private ConcurrentHashMap<Integer, RemoteSimulationMapping> remoteSimulationMap;
	
	// Request Map
	private ConcurrentHashMap<Long, NodeItemRequest> remoteRequestMap;
	private long requestNum = 0;
	
	// Benchmark Configuration
	private final int BENCHMARK = 0;
	private final int NUM_OBJECTS = 1024;
	private final int ITERATIONS = 10000;
	private final int WARM_UP_ITERATIONS = 10000;
	private final int NUM_RUNS = 6;
	
	public ComputeNodeManager2(int uid, Socket socket, int socketTXBuffer, boolean tcpNoDelay, int txFreq) throws IOException
	{
		sm = new NodeManagerStateMachine(uid);
		
		nodeInfo = new NodeInfo();
		
		remoteSimulationMap = new ConcurrentHashMap<Integer, RemoteSimulationMapping>(8, 0.8f, 2);
		
		remoteRequestMap = new ConcurrentHashMap<Long, NodeItemRequest>(8, 0.8f, 2);
		
		log.info("New ComputeNodeManager for connecting ComputeNode " + uid);
		
		// Internal Connection ID
		nodeInfo.setUid(uid);
		
		// ComputeNode Address
		nodeInfo.setAddress(socket.getInetAddress().getHostAddress());
		
		// Wrap an NCP socked around a connected TCP socket
		ncpSocket = new NCPSocket(socket, socketTXBuffer, tcpNoDelay, txFreq);
	}
	
	/*
	 * ************************************************************************************************************************************************************
	 * Start Processing
	 **************************************************************************************************************************************************************/
	
	public void start()
	{
		JComputeEventBus.register(this);
		
		Thread computeNodeManager = new Thread(new Runnable()
		{
			// Thread rx sleep period
			final int RX_FREQUENCY = 10;
			
			@Override
			public void run()
			{
				log.info("Starting");
				
				// Node info with UID filled receive
				boolean registered = ncpSocket.receiveRegistration(nodeInfo, BENCHMARK, NUM_OBJECTS, ITERATIONS, WARM_UP_ITERATIONS, NUM_RUNS);
				
				// Have we registered
				if(registered)
				{
					sm.transitionToState(NodeManagerState.RUNNING);
				} // Registration if
				else
				{
					log.error("Failed to register  node " + nodeInfo.getAddress());
					
					sm.transitionToState(NodeManagerState.SHUTDOWN);
				}
				
				// Running loop - exit on fatal error or orderly shutdown
				// We still enter if we failed to register as it collates shutdown processing in one place.
				Shutdown :
				while(true)
				{
					// Check we have not had a external state change request (ie from the pause / stop buttons)
					if(newState != null)
					{
						// We have been requested to pause or return to running
						if((newState == NodeManagerState.PAUSING) || (newState == NodeManagerState.RUNNING))
						{
							log.info("ComputeNodeManager2 " + nodeInfo.getUid() + " " + nodeInfo.getAddress() + " " + newState);
							
							sm.transitionToState(newState);
							
							newState = null;
						}
						
						/* We have been requested to shutdown - shutdowns via this method are treated as clean shutdown requests.
						 * We send an NCP request which requests the remote node finishes computation and disconnect.
						 * This will indirectly move us to the shutdown state once the node disconnects.
						 */
						if(newState == NodeManagerState.SHUTDOWN)
						{
							// We must first be paused, otherwise we will be accepting new items to process
							if(sm.getState() == NodeManagerState.PAUSED)
							{
								newState = null;
								
								// We move to the shutting down state - we can only move to shutdown after this
								sm.transitionToState(NodeManagerState.SHUTTINGDOWN);
								
								log.info("ComputeNodeManager2 " + nodeInfo.getUid() + " Requesting remote node perform orderly shutdown");
								
								ncpSocket.sendNodeOrderlyShutdownRequest();
							}
						}
					}
					
					// Check if our current state is pausing
					if(sm.getState() == NodeManagerState.PAUSING)
					{
						// We check that there are no active simulations before we switch to paused
						if(activeSims == 0)
						{
							sm.transitionToState(NodeManagerState.PAUSED);
						}
					}
					
					// Have we switched to the shutdown state
					if(sm.getState() == NodeManagerState.SHUTDOWN)
					{
						log.info("Processing shutting down due to node manager in shutdown state.");
						
						break Shutdown;
					}
					
					// Get an NCP message
					NCPMessage message = ncpSocket.getReadyStateMessage();
					
					if(message != null)
					{
						int type = message.getType();
						
						switch(type)
						{
							case NCP.AddSimReply:
							{
								AddSimReply reply = (AddSimReply) message;
								
								long requestId = reply.getRequestId();
								int simId = reply.getSimId();
								
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
							break;
							case NCP.SimStateNoti:
							{
								// Create the state object
								SimulationStateChanged notification = (SimulationStateChanged) message;
								
								// Find the mapping
								RemoteSimulationMapping mapping = remoteSimulationMap.get(notification.getSimId());
								
								// Debug as these are excessive output
								log.info(notification.info());
								log.debug("New " + mapping.info());
								
								// Intercept finished states
								if(notification.getState() == SimState.FINISHED)
								{
									// Track the final state
									mapping.setFinalStateChanged(notification);
									
									// The finished state is not posted onwards yet simulation statistics have to be fetched first
									if(mapping.getBatchItem().hasStatsEnabled())
									{
										// Send a statistics request
										ncpSocket.sendSimulationStatisticsRequest(mapping.getRemoteSimId(), mapping.getExportFormat());
									}
									else
									{
										// This simulation is finished processing - there are no statistics
										processFinishedSimulation(mapping, null);
									}
								}
								else
								{
									// Forward SimulationStateChangedEvent encapsulated in a ComputeNodeManagerItemStateEvent - to maintain event order
									JComputeEventBus.post(new ComputeNodeManagerItemStateEvent(new SimulationStateChangedEvent(mapping.getLocalSimId(),
									notification.getState(), notification.getRunTime(), notification.getStepCount(), notification.getEndEvent(), null)));
								}
							}
							break;
							case NCP.SimStatNoti:
							{
								// Create the state object
								SimulationStatChanged notification = (SimulationStatChanged) message;
								
								log.debug(notification.info());
								
								// Find the mapping
								RemoteSimulationMapping mapping = remoteSimulationMap.get(notification.getSimId());
								
								// We can get statistics changes during add simulation.
								// i.e when mapping created
								if(mapping != null)
								{
									log.debug("New " + mapping.info());
									
									// Post the event as if from a local simulation
									// This is unsafe but fast as this event can and WILL arrive before add/remove/finished events.
									// Listeners as such need to deal with null lookups.
									JComputeEventBus.post(new SimulationStatChangedEvent(mapping.getLocalSimId(), notification.getTime(), notification
									.getStepNo(), notification.getProgress(), notification.getAsps()));
									
								}
								else
								{
									log.warn("No mapping found for " + notification.info());
								}
							}
							break;
							case NCP.NodeStatsReply:
							{
								NodeStatsReply reply = (NodeStatsReply) message;
								
								log.debug("Received NodeStatsReply");
								
								JComputeEventBus.post(new ComputeNodeStatsUpdate(nodeInfo.getUid(), reply.getSequenceNum(), reply.getNodeStats()));
							}
							break;
							case NCP.SimResultsReq:
							{
								log.info("Received simulation results reply");
								
								SimulationResultsReply reply = (SimulationResultsReply) message;
								
								// Simulation Id
								int simId = reply.getSimId();
								
								// Find the mapping
								RemoteSimulationMapping mapping = remoteSimulationMap.get(simId);
								
								processFinishedSimulation(mapping, reply.getStatExporter(mapping.getExportFormat(), mapping.getFileNameSuffix()));
							}
							break;
							case NCP.NodeOrderlyShutdownReply:
							{
								// We have been informed the remote node is shutting down as requested and we can shutdown now
								log.info("Node has sent us a NodeOrderlyShutdownReply");
								
								sm.transitionToState(NodeManagerState.SHUTDOWN);
							}
							break;
							// Test Frame or Garbage
							case NCP.INVALID:
							default:
							{
								log.error("Received invalid or unknown message by NCP");
								
								sm.transitionToState(NodeManagerState.SHUTDOWN);
							}
							break;
						}
					}
					else
					{
						// If there are no messages check the socket is connected
						if(!ncpSocket.isConnected())
						{
							log.warn("NCP not connected");
							
							sm.transitionToState(NodeManagerState.SHUTDOWN);
						}
						else
						{
							// No messages and we are connected - sleep
							try
							{
								Thread.sleep(RX_FREQUENCY);
							}
							catch(InterruptedException e)
							{
								// Thread interrupted - in this case we enter shutdown
								log.error(e.getMessage());
								
								e.printStackTrace();
								
								sm.transitionToState(NodeManagerState.SHUTDOWN);
							}
						}
					} // If message=null
				} // Processing loop
				
				// Ensure the socket is closed if not already
				try
				{
					ncpSocket.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				
				log.info("Exiting");
			} // Run
		});
		
		computeNodeManager.setName("ComputeNodeManager " + nodeInfo.getUid());
		computeNodeManager.start();
	}
	
	/*
	 * ************************************************************************************************************************************************************
	 * Control Node
	 **************************************************************************************************************************************************************/
	
	// Unique Id
	public int getUid()
	{
		return nodeInfo.getUid();
	}
	
	// Support max simulations
	public int getMaxSims()
	{
		return nodeInfo.getMaxSims();
	}
	
	// Weighting
	public long getWeighting()
	{
		return nodeInfo.getWeighting();
	}
	
	// Remote node address
	public String getAddress()
	{
		return nodeInfo.getAddress();
	}
	
	public NodeInfo getNodeConfig()
	{
		return nodeInfo;
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
	
	public int getActiveSims()
	{
		int tActive = 0;
		
		activeSimsLock.acquireUninterruptibly();
		
		tActive = activeSims;
		
		activeSimsLock.release();
		
		return tActive;
	}
	
	/**
	 * Add a Simulation
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
		
		// Send an add simulation request - with the request id and configuration text.
		ncpSocket.sendAddSimulationRequest(requestNum, scenarioText);
		
		activeSimsLock.acquireUninterruptibly();
		
		activeSims++;
		
		activeSimsLock.release();
		
		nodeLock.release();
	}
	
	/**
	 * Causes this node manager to send a NodeStatisticsRequest to its associated compute node.
	 * 
	 * @param id
	 * The time base of the statistic request
	 */
	public void triggerNodeStatRequest(int id)
	{
		// Sends a request for node statistics associated with this node manager.
		ncpSocket.sendNodeStatisticsRequest(id);
	}
	
	// Allows retrieving outstanding simulations.
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
	
	/*
	 * ************************************************************************************************************************************************************
	 * Internal State Getters
	 **************************************************************************************************************************************************************/
	
	public boolean isShutdown()
	{
		return (sm.getState() == NodeManagerState.SHUTDOWN) ? true : false;
	}
	
	public boolean isRunning()
	{
		return (sm.getState() == NodeManagerState.RUNNING) ? true : false;
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
			// Do not overwrite a previous states request.
			if(newState == null)
			{
				newState = e.getState();
			}
		}
	}
	
	/*
	 * ************************************************************************************************************************************************************
	 * Internal
	 **************************************************************************************************************************************************************/
	
	/**
	 * Processes a finished simulation.
	 * 
	 * @param mapping
	 * @param exporter
	 */
	private void processFinishedSimulation(RemoteSimulationMapping mapping, Result exporter)
	{
		int simId = mapping.getRemoteSimId();
		
		// Remove mapping...
		remoteSimulationMap.remove(simId);
		
		activeSimsLock.acquireUninterruptibly();
		
		// Remote simulation is auto-removed when finished
		activeSims--;
		
		activeSimsLock.release();
		
		// Post the event as if from a local Simulation
		SimulationStateChanged finalStateChanged = mapping.getFinalStateChanged();
		
		// Forward an encapsulated simstate event
		JComputeEventBus.post(new ComputeNodeManagerItemStateEvent(new SimulationStateChangedEvent(mapping.getLocalSimId(), finalStateChanged.getState(),
		finalStateChanged.getRunTime(), finalStateChanged.getStepCount(), finalStateChanged.getEndEvent(), exporter)));
	}
}
