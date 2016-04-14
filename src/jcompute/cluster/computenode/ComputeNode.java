package jcompute.cluster.computenode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import jcompute.JComputeEventBus;
import jcompute.cluster.computenode.nodedetails.NodeAveragedStats;
import jcompute.cluster.computenode.nodedetails.NodeInfo;
import jcompute.cluster.computenode.nodedetails.NodeStatsSample;
import jcompute.cluster.computenode.weightingbenchmark.NodeWeightingBenchmark;
import jcompute.cluster.ncp.NCP;
import jcompute.cluster.ncp.NCP.ProtocolState;
import jcompute.cluster.ncp.message.command.AddSimReply;
import jcompute.cluster.ncp.message.command.AddSimReq;
import jcompute.cluster.ncp.message.command.SimulationStatsReply;
import jcompute.cluster.ncp.message.command.SimulationStatsRequest;
import jcompute.cluster.ncp.message.monitoring.NodeStatsReply;
import jcompute.cluster.ncp.message.monitoring.NodeStatsRequest;
import jcompute.cluster.ncp.message.notification.SimulationStatChanged;
import jcompute.cluster.ncp.message.notification.SimulationStateChanged;
import jcompute.cluster.ncp.message.registration.ConfigurationAck;
import jcompute.cluster.ncp.message.registration.ConfigurationRequest;
import jcompute.cluster.ncp.message.registration.RegistrationReqAck;
import jcompute.cluster.ncp.message.registration.RegistrationRequest;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.simulationmanager.SimulationsManager;
import jcompute.stats.StatExporter;
import jcompute.stats.StatExporter.ExportFormat;
import jcompute.util.JVMInfo;
import jcompute.util.NumericConstants;
import jcompute.util.OSInfo;

public class ComputeNode
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ComputeNode.class);
	
	// Simulation Manager
	private SimulationsManager simsManager;
	
	// TX Pending Message List
	private ArrayList<byte[]> txPendingList;
	private int pendingByteCount;
	
	// Command Output Stream
	private DataOutputStream commandOutput;
	private DataInputStream commandInput;
	
	/* ComputeNode Socket + SocketOpts */
	private Socket socket;
	private int socketTX;
	private int socketRX;
	private boolean tcpNoDelay;
	
	// ProtocolState
	private ProtocolState protocolState;
	
	// Cache of Stats from finished simulations
	private ProcessedItemStatCache statCache;
	
	private Timer nodeStatsUpdateTimer;
	private NodeAveragedStats nodeAveragedStats;
	
	// Counters
	private LongAdder simulationsProcessed;
	private LongAdder bytesTX;
	private LongAdder txS;
	private LongAdder bytesRX;
	private LongAdder rxS;
	
	/* RegLoop */
	boolean regLoopExit = false;
	
	/* Shutdown ComputeNode */
	private boolean shutdown = false;
	
	/* Benchmark */
	private NodeWeightingBenchmark nodeWeightingBenchmark;
	
	/* NCP Ready State management */
	private Timer ncpReadyStateTimer;
	private final int ncpTimerVal = 5;
	private int ncpTimerCount;
	
	/* ComputeNode Info */
	private NodeInfo nodeInfo;
	private JVMInfo jvmInfo;
	private OSInfo osInfo;
	
	private final int TX_FREQUENCY;
	
	public ComputeNode(final String address, String desc, final SimulationsManager simsManager, int socketTX, int socketRX, boolean tcpNoDelay, int txFreq)
	{
		log.info("Created ComputeNode");
		
		this.socketTX = socketTX;
		this.socketRX = socketRX;
		this.tcpNoDelay = tcpNoDelay;
		
		this.TX_FREQUENCY = txFreq;
		
		log.info("TCP TX Buffer : " + this.socketTX);
		log.info("TCP RX Buffer : " + this.socketRX);
		log.info("TCP No Delay : " + this.tcpNoDelay);
		log.info("TX Freq : " + this.TX_FREQUENCY);
		
		this.simsManager = simsManager;
		
		// Info Helpers
		jvmInfo = JVMInfo.getInstance();
		osInfo = OSInfo.getInstance();
		
		/* Our Configuration */
		nodeInfo = new NodeInfo();
		
		nodeInfo.setAddress(address);
		nodeInfo.setDescription(desc);
		
		nodeInfo.setOperatingSystem(osInfo.getOSName());
		nodeInfo.setMaxJVMMemory(jvmInfo.getMaxMemory());
		nodeInfo.setSystemArch(osInfo.getSystemArch());
		nodeInfo.setHWThreads(osInfo.getHWThreads());
		nodeInfo.setTotalOSMemory(osInfo.getSystemPhysicalMemorySize());
	}
	
	/*
	 * ***************************************************************************************************
	 * Local Control Methods
	 *****************************************************************************************************/
	
	// Starts the node
	public void start()
	{
		JComputeEventBus.register(this);
		
		// Node thread so we don't block Launcher and allow it to exit the same way as the GUI modes.
		Thread computeNode = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				log.info("Starting ComputeNode");
				
				statCache = new ProcessedItemStatCache();
				log.info("Created ComputeNode Stat Cache");
				
				// Average over 60 Seconds
				nodeAveragedStats = new NodeAveragedStats(60);
				
				nodeStatsUpdateTimer = new Timer("ComputeNode Stats Update Timer");
				nodeStatsUpdateTimer.scheduleAtFixedRate(new TimerTask()
				{
					@Override
					public void run()
					{
						nodeAveragedStats.update(osInfo.getSystemCpuUsage(), simsManager.getActiveSims(), statCache.getStatsStore(), jvmInfo
						.getUsedJVMMemoryPercentage());
					}
				}, 0, 1000);
				log.info("ComputeNode Stats Update Timer Started");
				
				// TX Pending Message List
				txPendingList = new ArrayList<byte[]>();
				pendingByteCount = 0;
				
				log.info("Created TX Pending Message List");
				
				// We are in the connecting state
				protocolState = ProtocolState.CON;
				
				ncpTimerCount = 0;
				ncpReadyStateTimer = new Timer("NCP Timer");
				ncpReadyStateTimer.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						doNCPTick();
					}
					
				}, 0, ncpTimerVal * 1000);
				
				Thread txData = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						// Disconnect Recovery Loop
						while(!shutdown)
						{
							try
							{
								txPendingData();
								Thread.sleep(TX_FREQUENCY);
							}
							catch(InterruptedException e)
							{
								log.info(e.getMessage());
								
								doShutdownCleanUp();
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
				txData.setName("TXData");
				txData.start();
				
				Thread processing = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						// Disconnect Recovery Loop
						while(!shutdown)
						{
							try
							{
								switch(protocolState)
								{
									case CON:
									{
										connect(nodeInfo.getAddress());
									}
									break;
									case REG:
									{
										register();
									}
									break;
									case RDY:
									{
										process();
									}
									break;
									case DIS:
									{
										// Reconnect if not shutting down
										if(!shutdown)
										{
											// Clean up any previous socket
											if(socket != null)
											{
												// Close Connection
												if(!socket.isClosed())
												{
													socket.close();
													
													log.info("Socket Closed");
												}
											}
											
											// Our connection to the remote manager is
											// gone.
											simsManager.removeAll();
											log.info("Removed all Simulations");
											
											// Any stats in the cache are not going to
											// be requested.
											statCache = new ProcessedItemStatCache();
											log.info("Recreated ComputeNode Stat Cache");
											
											// Clear Pending Messages
											clearPendingTXList();
											
											protocolState = ProtocolState.CON;
										}
									}
									break;
									default:
									{
										// Not Possible unless a new state is not
										// handled correctly.
										log.error("protocolState : " + protocolState + " NOT VALID");
										protocolState = ProtocolState.DIS;
									}
									break;
								}
								
							}
							catch(SocketTimeoutException e)
							{
								log.info(e.getMessage());
							}
							catch(ConnectException e)
							{
								log.info(e.getMessage());
							}
							catch(IOException e)
							{
								log.info(e.getMessage());
								
								doShutdownCleanUp();
								
								e.printStackTrace();
							}
							
							if(protocolState == ProtocolState.CON)
							{
								try
								{
									int sleep = (ThreadLocalRandom.current().nextInt(4750)) + 250;
									log.info("Waiting " + sleep + " milliseconds");
									Thread.sleep(sleep);
								}
								catch(InterruptedException e)
								{
									Thread.currentThread().interrupt();
									
									log.info(e.getMessage());
									
									doShutdownCleanUp();
									
									e.printStackTrace();
								}
							}
							
						}
						
						// Exiting RX Thread
						doShutdownCleanUp();
					}
				});
				processing.setName("Processing");
				processing.start();
				
				try
				{
					txData.join();
					processing.join();
				}
				catch(InterruptedException e)
				{
					log.info(e.getMessage());
					e.printStackTrace();
				}
				
				log.info("ComputeNode Exited");
				
				System.exit(0);
			}
		});
		computeNode.setName("ComputeNode");
		computeNode.start();
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP
	 *****************************************************************************************************/
	
	// Ready State Timer
	private void doNCPTick()
	{
		if(protocolState != ProtocolState.RDY)
		{
			ncpTimerCount += ncpTimerVal;
			
			log.info("NCP TimeOut@" + ncpTimerCount);
		}
		
		if(ncpTimerCount == NCP.ReadyStateTimeOut)
		{
			log.info("Ready State Timeout");
			
			if(nodeWeightingBenchmark.running())
			{
				log.info("Cancelling Weighting benchmark");
				nodeWeightingBenchmark.cancel();
			}
			
			protocolState = ProtocolState.DIS;
		}
	}
	
	// Handles NCP registration
	private void register()
	{
		regLoopExit = false;
		
		log.info("Attempting Registration");
		
		log.info("Sending Registration Request");
		
		// Create a registration request and send it
		txDataEnqueue(new RegistrationRequest().toBytes());
		
		// Read the replies
		while(!regLoopExit)
		{
			int type = -1;
			int len = -1;
			ByteBuffer data = null;
			
			try
			{
				type = commandInput.readInt();
				len = commandInput.readInt();
				
				log.debug("Type " + type + " len " + len);
				
				// Get the message data
				data = readBytesToByteBuffer(len);
			}
			catch(IOException e)
			{
				protocolState = ProtocolState.DIS;
				
				log.info(e.getMessage());
				
				break;
			}
			
			switch(type)
			{
				case NCP.RegAck:
					
					RegistrationReqAck reqAck = new RegistrationReqAck(data);
					
					nodeInfo.setUid(reqAck.getUid());
					
					log.info("RegAck Recieved UID : " + nodeInfo.getUid());
					
					// We Ack the Ack
					txDataEnqueue(reqAck.toBytes());
					
				break;
				case NCP.RegNack:
					
					int reason = data.getInt();
					int value = data.getInt();
					
					switch(reason)
					{
						case NCP.ProtocolVersionMismatch:
							
							log.error("RegNack : Protocol Version Mismatch");
							log.error("Local " + NCP.NCP_PROTOCOL_VERSION + " Remote " + value);
							
						break;
						default:
							
							log.error("RegNack : Unknown Reason " + reason + " value " + value);
							
						break;
					}
					
					// Unrecoverable
					protocolState = ProtocolState.DIS;
					shutdown = true;
					log.info("Shuting Down due to RegNack");
					
				break;
				case NCP.ConfReq:
					
					ConfigurationRequest confReq = new ConfigurationRequest(data);
					
					// Set our max sims now
					nodeInfo.setMaxSims(simsManager.getMaxSims());
					log.info("ConfReq Recieved");
					
					int benchMark = confReq.getBench();
					
					if(benchMark > 0)
					{
						log.info("Running Weighting Benchmark");
						nodeWeightingBenchmark = new NodeWeightingBenchmark(confReq.getObjects(), confReq.getIterations());
						nodeWeightingBenchmark.warmUp(confReq.getWarmup());
						long weighting = nodeWeightingBenchmark.weightingBenchmark(confReq.getRuns());
						nodeInfo.setWeighting(weighting);
						log.info("Weighting\t " + weighting);
					}
					
					// If nothing untoward has happened during the benchmark
					if(protocolState == ProtocolState.REG)
					{
						// Create and send the Configuration ack via
						// TransferSocket
						// + Initial NodeState
						log.info("Sending Conf Ack : Max Sims " + nodeInfo.getMaxSims());
						
						txDataEnqueue(new ConfigurationAck(nodeInfo).toBytes());
						
						protocolState = ProtocolState.RDY;
						
						log.info("Registration complete");
					}
					
					// Now Registered
					regLoopExit = true;
					
				break;
				case NCP.INVALID:
				default:
					log.error("Recieved Invalid Frame Type " + type);
					protocolState = ProtocolState.DIS;
				break;
				
			}
			
			// Exit the loop
			if(protocolState == ProtocolState.DIS)
			{
				log.info("Protocol State : " + protocolState.toString());
				regLoopExit = true;
			}
		}
	}
	
	// NCP Ready State Processing
	private void process()
	{
		boolean processing = true;
		
		// While we have a connection
		while(processing)
		{
			int type = -1;
			int len = -1;
			ByteBuffer data = null;
			
			try
			{
				type = commandInput.readInt();
				len = commandInput.readInt();
				
				log.debug("Type " + type + " len " + len);
				
				// Get the message data
				data = readBytesToByteBuffer(len);
			}
			catch(IOException e)
			{
				processing = false;
				protocolState = ProtocolState.DIS;
				log.info(e.getMessage());
				break;
			}
			
			switch(type)
			{
				case NCP.AddSimReq:
				{
					AddSimReq req = new AddSimReq(data);
					
					int simId = simsManager.addSimulation(req.getScenarioText(), -1);
					long requestId = req.getRequestId();
					
					log.info("Request " + requestId + " Add Sim - Result " + simId);
					
					txDataEnqueue(new AddSimReply(requestId, simId).toBytes());
					
					// If the sim was added sucessfully then start it
					if(simId > 0)
					{
						simsManager.startSim(simId);
					}
					
				}
				break;
				case NCP.NodeStatsRequest:
				{
					NodeStatsRequest req = new NodeStatsRequest(data);
					
					// Read here
					int sequenceNum = req.getSequenceNum();
					
					NodeStatsSample nodeStatsSample = new NodeStatsSample();
					
					// Averaged Stats
					nodeAveragedStats.populateStatSample(nodeStatsSample);
					
					// Get sum since last NodeStatsRequest then Reset Stats
					nodeStatsSample.setSimulationsProcessed(simulationsProcessed.sumThenReset());
					nodeStatsSample.setBytesTX(bytesTX.sumThenReset());
					nodeStatsSample.setBytesRX(bytesRX.sumThenReset());
					nodeStatsSample.setTXS(txS.sumThenReset());
					nodeStatsSample.setRXS(rxS.sumThenReset());
					
					NodeStatsReply NodeStatsReply = new NodeStatsReply(sequenceNum, nodeStatsSample);
					
					log.debug("ComputeNode " + nodeInfo.getUid() + " NodeStatsRequest : " + sequenceNum);
					
					txDataEnqueue(NodeStatsReply.toBytes());
					
				}
				break;
				case NCP.SimStatsReq:
				{
					SimulationStatsRequest statsReq = new SimulationStatsRequest(data);
					
					int simId = statsReq.getSimId();
					
					log.info("SimStatsReq " + simId);
					
					// Is this an active simulation we have got a stat
					// request for
					if(simsManager.hasSimWithId(simId))
					{
						// Simulations are autoremoved when finished but
						// this simulation was not finished
						simsManager.removeSimulation(simId);
						
						// Remove it from the statCache (to cover a
						// possible memory leak with the race - has
						// sim/sim-finished/remove sim)
						statCache.remove(simId);
						
						log.warn("Got Stat request for active simulation " + simId + " - removing simulation/stats - NOT sending SimStats!!!");
					}
					else
					{
						// Get the stat exporter for this simId and
						// create a Stats Reply
						SimulationStatsReply statsReply = new SimulationStatsReply(simId, statCache.remove(simId));
						
						// NCP.SimStats
						byte[] tempB = statsReply.toBytes();
						
						log.info("SimStats Created " + statsReq.getSimId() + " File Size " + (tempB.length
						/ NumericConstants.BinaryPrefix.JDEC_KILOBYTE.byteValue) + NumericConstants.BinaryPrefix.JDEC_KILOBYTE.prefix + "(s)");
						
						txDataEnqueue(tempB);
					}
				}
				break;
				case NCP.NodeOrderlyShutdown:
				{
					log.info("Recieved NodeOrderlyShutdown");
					
					int activeSims = simsManager.getActiveSims();
					int statsOutStanding = statCache.getStatsStore();
					
					// If there are no active sim and no outstanding
					// stats needing fetched.
					if((activeSims == 0) && (statsOutStanding == 0))
					{
						// Ensure the node does not attempt to
						// reconnect.
						shutdown = true;
						
						// Enter disconnect state
						protocolState = ProtocolState.DIS;
					}
					else
					{
						log.warn("Refusing NodeOrderlyShutdown due to active sims " + activeSims + " & stats outstanding " + statsOutStanding);
					}
				}
				break;
				// Default / Invalid
				case NCP.INVALID:
				default:
				{
					log.error("Invalid NCP Message Recieved");
					
					protocolState = ProtocolState.DIS;
					
					log.error("Error Type " + type + " len " + len);
				}
				break;
			}
			
			if(protocolState == ProtocolState.DIS)
			{
				processing = false;
			}
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Socket Control
	 *****************************************************************************************************/
	
	// Performs a socket connection attempt
	private boolean connect(String address) throws IOException
	{
		// Reset the timer
		ncpTimerCount = 0;
		
		// Connecting to Server
		socket = null;
		
		// Reset Averaged statistics on reconnection.
		nodeAveragedStats.reset();
		
		// Reset Instant statistics on reconnection.
		simulationsProcessed = new LongAdder();
		bytesTX = new LongAdder();
		bytesRX = new LongAdder();
		txS = new LongAdder();
		rxS = new LongAdder();
		
		log.info("Connecting to : " + address + "@" + NCP.StandardServerPort);
		
		// clientSocket = new Socket(address, port);
		socket = new Socket();
		
		// Set Socket opts
		socket.setReceiveBufferSize(socketRX);
		socket.setSendBufferSize(socketTX);
		socket.setTcpNoDelay(tcpNoDelay);
		
		socket.connect(new InetSocketAddress(address, NCP.StandardServerPort), 10000);
		// Link up Output Stream
		commandOutput = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		
		// Link up Input Stream
		commandInput = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		
		// Enter the reg state
		protocolState = ProtocolState.REG;
		
		log.info("Connected to : " + socket.getRemoteSocketAddress());
		log.info("We are : " + socket.getLocalSocketAddress());
		
		return true;
	}
	
	// Performs A socket Shutdown
	private void doShutdownCleanUp()
	{
		// Clean up any previous socket
		if(socket != null)
		{
			// Close Connection
			if(!socket.isClosed())
			{
				try
				{
					socket.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		ncpReadyStateTimer.cancel();
		
		shutdown = true;
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
	
	/*
	 * ***************************************************************************************************
	 * RX Transfer
	 *****************************************************************************************************/
	
	// Reads n bytes from the socket and returns them in a byte buffer.
	private ByteBuffer readBytesToByteBuffer(int len) throws IOException
	{
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		// Allocate here to avoid duplication of allocation code
		if(len > 0)
		{
			// Destination
			backingArray = new byte[len];
			
			// Copy from the socket
			commandInput.readFully(backingArray, 0, len);
			
			// Wrap the backingArray
			data = ByteBuffer.wrap(backingArray);
			
			bytesRX.add(backingArray.length);
			
			rxS.increment();
		}
		
		return data;
	}
	
	/*
	 * ***************************************************************************************************
	 * Event Subscribers
	 *****************************************************************************************************/
	
	@Subscribe
	public void SimulationStatChangedEvent(SimulationStatChangedEvent e)
	{
		txDataEnqueue(new SimulationStatChanged(e).toBytes());
	}
	
	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		if(e.getState() == SimState.FINISHED)
		{
			int simId = e.getSimId();
			
			StatExporter exporter = simsManager.getStatExporter(simId, "", ExportFormat.CSV);
			
			// Check for simulations with no stats enabled
			
			if(exporter.getSize() > 0)
			{
				log.info("Stored Stats for Simulation " + simId);
				statCache.put(simId, exporter);
			}
			else
			{
				log.info("No Stats enabled for Simulation " + simId);
			}
			
			simsManager.removeSimulation(simId);
			log.info("Removed Finished Simulation");
			
			simulationsProcessed.increment();
		}
		
		txDataEnqueue(new SimulationStateChanged(e).toBytes());
		log.info("Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
	}
}
