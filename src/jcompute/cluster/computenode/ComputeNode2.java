package jcompute.cluster.computenode;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import jcompute.JComputeEventBus;
import jcompute.cluster.computenode.nodedetails.NodeAveragedStats;
import jcompute.cluster.computenode.nodedetails.NodeInfo;
import jcompute.cluster.computenode.nodedetails.NodeStatsSample;
import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.NCPSocket;
import jcompute.cluster.ncp.message.NCPMessage;
import jcompute.cluster.ncp.message.command.AddSimReq;
import jcompute.cluster.ncp.message.command.SimulationStatsRequest;
import jcompute.cluster.ncp.message.monitoring.NodeStatsRequest;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.simulationmanager.SimulationsManager;
import jcompute.stats.StatExporter;
import jcompute.stats.StatExporter.ExportFormat;
import jcompute.util.JVMInfo;
import jcompute.util.OSInfo;

public class ComputeNode2
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ComputeNode2.class);
	
	// NCP Protocol Socket
	private NCPSocket ncpSocket;
	private boolean lock = false;
	
	// Simulation Manager
	private SimulationsManager simulationsManager;
	
	// Cache of Statistics from finished simulations
	private ProcessedItemStatCache statCache;
	
	// Averaged Node Statistics
	private NodeAveragedStats nodeAveragedStats;
	
	// Counters
	private LongAdder simulationsProcessed;
	
	// ComputeNode Info
	private NodeInfo nodeInfo;
	private JVMInfo jvmInfo;
	private OSInfo osInfo;
	
	// Node Logic
	private AtomicBoolean nodeStarted = new AtomicBoolean(false);
	
	public ComputeNode2(String nodeDesc, final SimulationsManager simulationsManager)
	{
		log.info("Created ComputeNode");
		
		this.simulationsManager = simulationsManager;
		
		// Info Helpers
		jvmInfo = JVMInfo.getInstance();
		osInfo = OSInfo.getInstance();
		
		// Our configuration info
		nodeInfo = new NodeInfo();
		
		nodeInfo.setMaxSims(simulationsManager.getMaxSims());
		
		nodeInfo.setDescription(nodeDesc);
		
		nodeInfo.setOperatingSystem(osInfo.getOSName());
		nodeInfo.setMaxJVMMemory(jvmInfo.getMaxMemory());
		nodeInfo.setSystemArch(osInfo.getSystemArch());
		nodeInfo.setHWThreads(osInfo.getHWThreads());
		nodeInfo.setTotalOSMemory(osInfo.getSystemPhysicalMemorySize());
	}
	
	/*
	 * ***************************************************************************************************
	 * Node entry
	 *****************************************************************************************************/
	
	public boolean start(final int maxConnectionAttempts, final String address, final int socketTX, final int socketRX, final boolean tcpNoDelay,
	final int txFreq)
	{
		if(nodeStarted.getAndSet(true))
		{
			log.error("Node already Started");
			
			return false;
		}
		
		// Register on the event bus
		JComputeEventBus.register(this);
		
		// New thread so we don't block Launcher and allow it to exit the same way as the GUI modes do.
		Thread computeNode = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Record remote address
				nodeInfo.setAddress(address);
				
				log.info("Starting");
				
				statCache = new ProcessedItemStatCache();
				log.info("Created ComputeNode Statistics Cache");
				
				// Average over 60 Seconds
				nodeAveragedStats = new NodeAveragedStats(60);
				
				simulationsProcessed = new LongAdder();
				
				Thread processing = new Thread(new Runnable()
				{
					// Timing Frequencies
					final int RX_FREQUENCY = 10;
					final int STATISTICS_UPDATE_FREQUENCY = 1000;
					
					@Override
					public void run()
					{
						log.info("Started");
						
						int connectionAttempts = 0;
						
						boolean connected = false;
						boolean registered = false;
						
						long currentStatisticsTime = System.currentTimeMillis();
						long lastStatisticsTime = currentStatisticsTime;
						
						// Retry connection loop - exit on fatal error or orderly shutdown
						Shutdown :
						while(true)
						{
							if(!connected)
							{
								log.info("processing");
								
								// Busy wait
								waitOnSocketLock();
								
								// NCP Socket
								ncpSocket = new NCPSocket();
								
								releaseSocketLock();
								
								// Reset UID
								nodeInfo.setUid(-1);
								
								// Reset Averaged statistics on re/connection.
								nodeAveragedStats.reset();
								
								// Reset Instant statistics on re/connection.
								simulationsProcessed.reset();
								
								// Remove all current simulations.
								simulationsManager.removeAll();
								log.info("Simulations manager cleared");
								
								// Any statistics in the cache are not going to be requested so remove them too.
								statCache = new ProcessedItemStatCache();
								log.info("Statistics cache cleared");
								
								// Log connection parameters
								log.info("Address       : " + address);
								log.info("TCP TX Buffer : " + socketTX);
								log.info("TCP RX Buffer : " + socketRX);
								log.info("TCP No Delay  : " + tcpNoDelay);
								log.info("TX Frequency  : " + txFreq);
								
								// Connect
								connected = ncpSocket.connect(address, socketRX, socketTX, tcpNoDelay, txFreq);
								
								if(connected)
								{
									// Register
									registered = ncpSocket.initiateRegistration(nodeInfo);
									
									if(!registered)
									{
										// Fatal
										log.error("Failed to register with " + address);
										
										break Shutdown;
									}
									else
									{
										log.info("Registration completed");
										
										// We have connected - reset the attempts
										connectionAttempts = 0;
									}
								}
								else
								{
									connectionAttempts++;
									
									log.warn("Failed to connect to " + address + " Attempt " + connectionAttempts);
									
									if(connectionAttempts == maxConnectionAttempts)
									{
										log.info("Reached max connection attempts " + connectionAttempts);
										
										break Shutdown;
									}
								}
							}
							
							// Get an NCP message
							NCPMessage message = ncpSocket.getReadyStateMessage();
							
							if(message != null)
							{
								int type = message.getType();
								
								switch(type)
								{
									case NCP.AddSimReq:
									{
										AddSimReq req = (AddSimReq) message;
										
										log.info("AddSimReq " + req.getRequestId());
										
										int simId = simulationsManager.addSimulation(req.getScenarioText(), -1);
										
										ncpSocket.sendAddSimReply(req, simId);
										
										// If the simulation was added successfully then start it
										if(simId > 0)
										{
											simulationsManager.startSim(simId);
										}
									}
									break;
									case NCP.NodeStatsRequest:
									{
										NodeStatsRequest req = (NodeStatsRequest) message;
										
										log.info("NodeStatsRequest " + req.getSequenceNum());
										
										NodeStatsSample nodeStatsSample = new NodeStatsSample();
										
										// Averaged statistics
										nodeAveragedStats.populateStatSample(nodeStatsSample);
										
										// Get sum since last NodeStatsRequest then reset statistics
										nodeStatsSample.setSimulationsProcessed(simulationsProcessed.sumThenReset());
										
										// The socket will fill it its own statistics
										ncpSocket.sendNodeStatsReply(req, nodeStatsSample);
									}
									break;
									case NCP.SimStatsReq:
									{
										SimulationStatsRequest req = (SimulationStatsRequest) message;
										
										int simId = req.getSimId();
										
										log.info("SimStatsReq " + simId);
										
										// Is this an active simulation we have got a statistics request for
										if(simulationsManager.hasSimWithId(simId))
										{
											// Simulations are auto removed when finished but this simulation was not finished
											simulationsManager.removeSimulation(simId);
											
											// We assume remote did not want the result.
											statCache.remove(simId);
											
											log.warn("Got statistics request for active simulation " + simId
											+ " - removing simulation/statistics - NOT sending SimStats!!!");
										}
										else
										{
											// Get the statistics exporter for this simId and send a statistics reply
											ncpSocket.sendSimulationStatsReply(req, statCache.remove(simId));
										}
									}
									break;
									case NCP.NodeOrderlyShutdownRequest:
									{
										// No Data - comment here for consistency
										// NodeOrderlyShutdownRequest req = (NodeOrderlyShutdownRequest) message;
										
										log.info("Received NodeOrderlyShutdownRequest");
										
										int activeSims = simulationsManager.getActiveSims();
										int statsOutStanding = statCache.getStatsStore();
										
										// If there are no active simulations and no outstanding statistics needing fetched.
										if((activeSims == 0) && (statsOutStanding == 0))
										{
											try
											{
												// Sends a reply that we are shutting down.
												ncpSocket.sendNodeOrderlyShutdownReply();
												
												ncpSocket.close();
											}
											catch(IOException e)
											{
												e.printStackTrace();
											}
											
											connected = false;
											registered = false;
											
											// Exit
											break Shutdown;
										}
										else
										{
											log.warn("Refusing NodeOrderlyShutdownRequest due to active simulations " + activeSims
											+ " and statistics outstanding " + statsOutStanding);
										}
									}
									break;
									// Default / Invalid
									case NCP.INVALID:
									default:
									{
										log.error("Invalid NCP Message received - Type " + type);
										
										try
										{
											ncpSocket.close();
										}
										catch(IOException e)
										{
											e.printStackTrace();
										}
										
										connected = false;
										registered = false;
										
										// Exit
										break Shutdown;
									}
								}
							} // End if (message)
							else
							{
								if(ncpSocket.isConnected())
								{
									currentStatisticsTime = System.currentTimeMillis();
									boolean requestStatistics = ((currentStatisticsTime - lastStatisticsTime) >= STATISTICS_UPDATE_FREQUENCY);
									
									if(requestStatistics)
									{
										nodeAveragedStats.update(osInfo.getSystemCpuUsage(), simulationsManager.getActiveSims(), statCache.getStatsStore(),
										jvmInfo.getUsedJVMMemoryPercentage());
										
										lastStatisticsTime = currentStatisticsTime;
									}
									
									try
									{
										Thread.sleep(RX_FREQUENCY);
									}
									catch(InterruptedException e)
									{
										log.error(e.getMessage());
										
										e.printStackTrace();
										
										break Shutdown;
									}
								}
								else
								{
									log.error("NCP not connected restarting");
									
									try
									{
										ncpSocket.close();
									}
									catch(IOException e)
									{
										e.printStackTrace();
									}
									
									connected = false;
									registered = false;
									
									// Delay the retry.
									try
									{
										Thread.sleep(1000);
									}
									catch(InterruptedException e)
									{
										log.error(e.getMessage());
										
										e.printStackTrace();
										
										break Shutdown;
									}
								}
							}
						} // End Processing Loop
						
						// We have shutdown - restart not tested
						nodeStarted.set(false);
						
						// Last message from processing
						log.warn("Shutdown");
					} // Run
				});
				processing.setName("Processing");
				processing.start();
				
				try
				{
					processing.join();
				}
				catch(InterruptedException e)
				{
					log.info(e.getMessage());
					e.printStackTrace();
				}
				
				// We may have exited with the socket locked.
				// Forcefully release it as the event bus subscriber may be waiting on the socket.
				releaseSocketLock();
				
				// We need to shutdown the event bus for a clean exit, all event posting threads must also have exited.
				JComputeEventBus.shutdown();
				
				// Last message from compute node
				log.info("Exited");
			}
		});
		computeNode.setName("ComputeNode");
		computeNode.start();
		
		return true;
	}
	
	/*
	 * ***************************************************************************************************
	 * Event Subscribers
	 *****************************************************************************************************/
	
	// Used to protect the Subscribers from a null socket and also to reduce the time blocking the event bus to as short as possible
	// This approach is only usable if the calling thread do not repeatedly need the lock and release the lock very quickly.
	private void waitOnSocketLock()
	{
		// Busy wait
		while(true)
		{
			// Try grab lock
			if(testAndSetSocketLock())
			{
				// We got the lock, now exit.
				return;
			}
			
			// The lock is not available
			// Spin on lock read until it changes.
			// This is a multi-core optimisation - read cost is reduced until the local cache is invalidated by the lock value changing in another thread.
			while(!lock)
			{
				// This is here for dead lock detection not lock contention debugging.
				// In the case where this lock is required - the socket is gone thus overhead of this warning should be minimal.
				log.warn("Spinning waitOnSocketLock");
			}
			// Lock is now false...loop and retry.
		}
	}
	
	// Do not call directly.
	private synchronized boolean testAndSetSocketLock()
	{
		boolean cVal = lock;
		lock = true;
		return cVal;
	}
	
	private void releaseSocketLock()
	{
		lock = false;
	}
	
	@Subscribe
	public void SimulationStatChangedEvent(SimulationStatChangedEvent e)
	{
		waitOnSocketLock();
		
		ncpSocket.sendSimulationStatChanged(e);
		
		releaseSocketLock();
	}
	
	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		if(e.getState() == SimState.FINISHED)
		{
			int simId = e.getSimId();
			
			StatExporter exporter = simulationsManager.getStatExporter(simId, "", ExportFormat.CSV);
			
			// Check for simulations with no statistics enabled
			if(exporter.getSize() > 0)
			{
				log.info("Cached statistics for simulation " + simId);
				statCache.put(simId, exporter);
			}
			else
			{
				log.info("No statistics enabled for simulation " + simId);
			}
			
			simulationsManager.removeSimulation(simId);
			log.info("Removed Finished Simulation");
			
			simulationsProcessed.increment();
		}
		
		// Busy wait
		waitOnSocketLock();
		
		ncpSocket.sendSimulationStateChanged(e);
		
		releaseSocketLock();
		
		log.info("Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
	}
}
