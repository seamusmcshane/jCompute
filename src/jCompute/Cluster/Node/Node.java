package jCompute.Cluster.Node;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Node.NodeDetails.NodeAveragedStats;
import jCompute.Cluster.Node.NodeDetails.NodeInfo;
import jCompute.Cluster.Node.NodeDetails.NodeStatsSample;
import jCompute.Cluster.Protocol.NCP;
import jCompute.Cluster.Protocol.Command.AddSimReply;
import jCompute.Cluster.Protocol.Command.AddSimReq;
import jCompute.Cluster.Protocol.Command.SimulationStatsReply;
import jCompute.Cluster.Protocol.Command.SimulationStatsRequest;
import jCompute.Cluster.Protocol.Command.StartSimCMD;
import jCompute.Cluster.Protocol.NCP.ProtocolState;
import jCompute.Cluster.Protocol.Monitoring.NodeStatsReply;
import jCompute.Cluster.Protocol.Notification.SimulationStatChanged;
import jCompute.Cluster.Protocol.Notification.SimulationStateChanged;
import jCompute.Cluster.Protocol.Registration.ConfigurationAck;
import jCompute.Cluster.Protocol.Registration.ConfigurationRequest;
import jCompute.Cluster.Protocol.Registration.RegistrationReqAck;
import jCompute.Cluster.Protocol.Registration.RegistrationRequest;
import jCompute.Datastruct.knn.benchmark.NodeWeightingBenchmark;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.SimulationManager.SimulationsManager;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.util.JVMInfo;
import jCompute.util.OSInfo;

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
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class Node
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(Node.class);
	
	// Simulation Manager
	private SimulationsManager simsManager;
	
	// TX Message Queue
	private LinkedTransferQueue<byte[]> txQueue;
	
	// Command Output Stream
	private DataOutputStream commandOutput;
	private DataInputStream commandInput;
	
	/* Node Socket */
	private Socket socket;
	
	// ProtocolState
	private ProtocolState protocolState;
	
	// Cache of Stats from finished simulations
	private NodeProcessedItemStatCache statCache;
	
	private Timer nodeStatsUpdateTimer;
	private NodeAveragedStats nodeAveragedStats;
	private long simulationsProcessed;
	private long bytesTX;
	private long bytesRX;
	
	/* RegLoop */
	boolean regLoopExit = false;
	
	/* Shutdown Node */
	private boolean shutdown = false;
	
	/* Benchmark */
	private NodeWeightingBenchmark nodeWeightingBenchmark;
	
	/* NCP Ready State management */
	private Timer ncpReadyStateTimer;
	private final int ncpTimerVal = 5;
	private int ncpTimerCount;
	
	/* Node Info */
	private NodeInfo nodeInfo;
	
	private final int NODE_TX_FREQUENCY = 10;
	
	public Node(final String address, String desc, final SimulationsManager simsManager)
	{
		log.info("Created Node");
		
		simulationsProcessed = 0;
		this.simsManager = simsManager;
		
		/* Our Configuration */
		nodeInfo = new NodeInfo();
		
		nodeInfo.setAddress(address);
		nodeInfo.setDescription(desc);
		
		nodeInfo.setOperatingSystem(OSInfo.getOSName());
		nodeInfo.setMaxJVMMemory(JVMInfo.getMaxMemory());
		nodeInfo.setSystemArch(OSInfo.getSystemArch());
		nodeInfo.setHWThreads(OSInfo.getHWThreads());
		nodeInfo.setTotalOSMemory(OSInfo.getSystemTotalMemory());
	}
	
	public void start()
	{
		log.info("Starting Node");
		
		JComputeEventBus.register(this);
		
		statCache = new NodeProcessedItemStatCache();
		log.info("Created Node Stat Cache");
		
		// Average over 60 Seconds
		nodeAveragedStats = new NodeAveragedStats(60);
		
		nodeStatsUpdateTimer = new Timer("Node Stats Update Timer");
		nodeStatsUpdateTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				nodeAveragedStats.update(OSInfo.getSystemCpuUsage(), simsManager.getActiveSims(), statCache.getStatsStore(),
						JVMInfo.getUsedJVMMemoryPercentage());
			}
		}, 0, 1000);
		log.info("Node Stats Update Timer Started");
		
		// TX Message Queue
		txQueue = new LinkedTransferQueue<byte[]>();
		log.info("Created TX Queue");
		
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
		
		Thread txThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Disconnect Recovery Loop
				while(!shutdown)
				{
					try
					{
						txData();
						Thread.sleep(NODE_TX_FREQUENCY);
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
		txThread.setName("TX Thread");
		txThread.start();
		
		Thread rxThread = new Thread(new Runnable()
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
								connect(nodeInfo.getAddress());
							break;
							case REG:
								register();
							break;
							case RDY:
								process();
							break;
							case DIS:
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
									statCache = new NodeProcessedItemStatCache();
									log.info("Recreated Node Stat Cache");
									
									// Clear TX Message Queue
									txQueue = new LinkedTransferQueue<byte[]>();
									log.info("Cleared TX Queue");
									
									protocolState = ProtocolState.CON;
								}
							break;
							default:
								// Not Possible unless a new state is not
								// handled correctly.
								log.error("protocolState : " + protocolState + " NOT VALID");
								protocolState = ProtocolState.DIS;
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
		rxThread.setName("RX Thread");
		rxThread.start();
		
		try
		{
			txThread.join();
			rxThread.join();
			
		}
		catch(InterruptedException e)
		{
			log.info(e.getMessage());
			e.printStackTrace();
		}
	}
	
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
	
	private boolean connect(String address) throws IOException, SocketTimeoutException
	{
		// Reset the timer
		ncpTimerCount = 0;
		
		// Connecting to Server
		socket = null;
		
		// Reset Averaged statistics on reconnection.
		nodeAveragedStats.reset();
		
		// Reset Instant statistics on reconnection.
		simulationsProcessed = 0;
		bytesTX = 0;
		bytesRX = 0;
		
		log.info("Connecting to : " + address + "@" + NCP.StandardServerPort);
		
		// clientSocket = new Socket(address, port);
		socket = new Socket();
		socket.connect(new InetSocketAddress(address, NCP.StandardServerPort), 1000);
		
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
	
	// RX/TX on Command socket
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
					
					int simId = simsManager.addSimulation(req.getScenarioText(), req.getInitialStepRate());
					
					log.info("Added Sim " + simId);
					
					txDataEnqueue(new AddSimReply(simId).toBytes());
				}
				break;
				case NCP.StartSimCMD:
				{
					StartSimCMD cmd = new StartSimCMD(data);
					
					log.info("StartSimCMD " + cmd.getSimid());
					
					simsManager.startSim(cmd.getSimid());
				}
				break;
				/*
				 * case NCP.RemSimReq:
				 * {
				 * RemoveSimReq removeSimReq = new RemoveSimReq(data);
				 * int simId = removeSimReq.getSimid();
				 * simsManager.removeSimulation(simId);
				 * log.info("RemoveSimReq " + simId);
				 * sendMessage(new RemoveSimAck(simId).toBytes());
				 * }
				 * break;
				 */
				case NCP.NodeStatsRequest:
				{
					// Read here
					int sequenceNum = data.getInt();
					
					NodeStatsSample nodeStatsSample = new NodeStatsSample();
					
					// Averaged Stats
					nodeAveragedStats.populateStatSample(nodeStatsSample);
					
					// Instant Stats
					nodeStatsSample.setSimulationsProcessed(simulationsProcessed);
					nodeStatsSample.setBytesTX(bytesTX);
					nodeStatsSample.setBytesRX(bytesRX);
					
					NodeStatsReply NodeStatsReply = new NodeStatsReply(sequenceNum, nodeStatsSample);
					
					log.debug("Node " + nodeInfo.getUid() + " NodeStatsRequest : " + sequenceNum);
					
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
						
						log.warn("Got Stat request for active simulation " + simId
								+ " - removing simulation/stats - NOT sending SimStats!!!");
					}
					else
					{
						// Get the stat exporter for this simId and
						// create a Stats Reply
						SimulationStatsReply statsReply = new SimulationStatsReply(simId, statCache.remove(simId));
						
						// NCP.SimStats
						byte[] tempB = statsReply.toBytes();
						
						log.info("Sending SimStats " + statsReq.getSimId() + " Size " + (int) Math.ceil(tempB.length / 1024) + "kB");
						
						txDataEnqueue(tempB);
					}
				}
				break;
				case NCP.NodeOrderlyShutdown:
					
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
						log.warn("Refusing NodeOrderlyShutdown due to active sims " + activeSims + " & stats outstanding "
								+ statsOutStanding);
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
	
	/*
	 * Enqueue Messages to be sent
	 */
	private void txDataEnqueue(byte[] bytes)
	{
		txQueue.add(bytes);
	}
	
	private void txData() throws IOException
	{
		Iterator<byte[]> itr = txQueue.iterator();
		
		while(itr.hasNext())
		{
			byte[] bytes = itr.next();
			itr.remove();
			
			commandOutput.write(bytes);
			bytesTX += bytes.length;
			log.debug(bytes.length + " Bytes Sent");
		}
		commandOutput.flush();
	}
	
	/*
	 * Reads n bytes from the socket and returns them in a byte buffer.
	 */
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
			
			bytesRX += backingArray.length;
			
		}
		
		return data;
	}
	
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
			
			log.info("Stored Stats for Simulation " + simId);
			statCache.put(simId, exporter);
			
			simsManager.removeSimulation(simId);
			log.info("Removed Finished Simulation");
			
			simulationsProcessed++;
		}
		
		txDataEnqueue(new SimulationStateChanged(e).toBytes());
		log.info("Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
	}
}
