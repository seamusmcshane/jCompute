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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
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
	
	// Protect the send socket
	private Semaphore cmdTxLock = new Semaphore(1, true);
	
	// To ensure receive frames and events are processed atomically
	private Semaphore rxLockEvents = new Semaphore(1, true);
	
	// Command Output Stream
	private DataOutputStream commandOutput;
	
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
	
	/* Shutdown Node */
	private boolean shutdown = false;
	
	/* Benchmark */
	private NodeWeightingBenchmark nodeWeightingBenchmark;
	
	/* NCP Ready State management */
	private Timer ncpReadyStateTimer;
	private final int ncpTimerVal = 5;
	private int timerCount;
	
	
	public Node(String address, String desc, final SimulationsManager simsManager)
	{
		log.info("Starting Node");
		
		simulationsProcessed = 0;
		this.simsManager = simsManager;
		
		/* Our Configuration */
		NodeInfo nodeInfo = new NodeInfo();
		
		nodeInfo.setAddress(address);
		nodeInfo.setDescription(desc);
		
		nodeInfo.setOperatingSystem(OSInfo.getOSName());
		nodeInfo.setMaxJVMMemory(JVMInfo.getMaxMemory());
		nodeInfo.setSystemArch(OSInfo.getSystemArch());
		nodeInfo.setHWThreads(OSInfo.getHWThreads());
		nodeInfo.setTotalOSMemory(OSInfo.getSystemTotalMemory());
		
		int port = NCP.StandardServerPort;
		
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
				nodeAveragedStats.update(OSInfo.getSystemCpuUsage(), simsManager.getActiveSims(), statCache.getStatsStore(), JVMInfo.getUsedJVMMemoryPercentage());
			}
		}, 0, 1000);
		log.info("Node Stats Update Timer Started");
		
		// Disconnect Recovery Loop
		while(!shutdown)
		{
			// We are in the connecting state
			protocolState = ProtocolState.CON;
			
			// Connecting to Server
			socket = null;
			
			// Handle a loop when ready state is not reached
			if(ncpReadyStateTimer != null)
			{
				log.info("Stoping existing NCP timer");
				ncpReadyStateTimer.cancel();
			}
			
			timerCount = 0;
			
			ncpReadyStateTimer = new Timer("NCP Timer");
			ncpReadyStateTimer.schedule(new TimerTask()
			{
				boolean timerEnd = false;
				
				@Override
				public void run()
				{
					if(protocolState == ProtocolState.RDY)
					{
						log.info("Reached Ready State stoping NCP timer");
						// End the ready state timer
						ncpReadyStateTimer.cancel();
					}
					else
					{
						timerCount += ncpTimerVal;
						
						log.info("NCP TimeOut@" + timerCount);
					}
					
					if(timerCount == NCP.ReadyStateTimeOut)
					{
						log.info("Ready State Timeout");
						
						timerEnd = true;
					}
					
					if(timerEnd)
					{
						ncpReadyStateTimer.cancel();
						
						if(nodeWeightingBenchmark.running())
						{
							log.info("Cancelling Weighting benchmark");
							nodeWeightingBenchmark.cancel();
						}
						
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
								}
							}
						}
						
					}
					
				}
			}, 0, ncpTimerVal * 1000);
			
			try
			{
				// Reset Average stats on reconnection.
				nodeAveragedStats.reset();

				// Reset Instant stats on reconnection.
				simulationsProcessed = 0;
				bytesTX = 0;
				bytesRX = 0;
				
				
				log.info("Connecting to : " + address + "@" + port);
				
				// clientSocket = new Socket(address, port);
				socket = new Socket();
				socket.connect(new InetSocketAddress(address, port), 1000);
				
				if(!socket.isClosed())
				{
					log.info("Connected to : " + socket.getRemoteSocketAddress());
					log.info("We are : " + socket.getLocalSocketAddress());
					
					// Main
					process(nodeInfo, socket);
				}
				
				// Close Connection
				if(!socket.isClosed())
				{
					socket.close();
				}
				
				// We are in the disconnect state
				protocolState = ProtocolState.DIS;
				
			}
			catch(IOException e)
			{
				log.warn("Connection to " + address + " failed");
				
				protocolState = ProtocolState.DIS;
				
				if(ncpReadyStateTimer != null)
				{
					log.info("Stoping existing NCP timer");
					ncpReadyStateTimer.cancel();
				}
				
			}
			
			// Re-attempt to connect
			try
			{
				int sleep = (ThreadLocalRandom.current().nextInt(4750)) + 250;
				
				log.info("Waiting " + sleep);
				
				Thread.sleep(sleep);
			}
			catch(InterruptedException e)
			{
				log.warn("Sleep interupted");
			}
			
		}
		
	}
	
	// RX/TX on Command socket
	private void process(NodeInfo nodeInfo, Socket clientSocket)
	{
		try
		{
			// Link up output
			commandOutput = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
			
			// Input Stream
			final DataInputStream commandInput = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			
			doRegistration(nodeInfo, commandInput);
			
			// if Registered successfully
			if(protocolState == ProtocolState.RDY)
			{
				log.info("Registration complete");
				
				int type = -1;
				int len = -1;
				byte[] backingArray = null;
				ByteBuffer data = null;
				
				// While we have a connection
				while(!clientSocket.isClosed())
				{
					type = commandInput.readInt();
					len = commandInput.readInt();
					
					// Allocate here to avoid duplication of allocation code
					if(len > 0)
					{
						// Destination
						backingArray = new byte[len];
						
						// Copy from the socket
						commandInput.readFully(backingArray, 0, len);
						
						// Wrap the backingArray
						data = ByteBuffer.wrap(backingArray);
						
						log.debug("Type " + type + " len " + len);
						
						bytesRX += backingArray.length;
						
					}
					
					switch(type)
					{
						case NCP.AddSimReq:
						{
							rxLockEvents.acquireUninterruptibly();
							
							AddSimReq req = new AddSimReq(data);
							
							int simId = simsManager.addSimulation(req.getScenarioText(), req.getInitialStepRate());
							
							log.info("Added Sim " + simId);
							
							sendMessage(new AddSimReply(simId).toBytes());
							
							rxLockEvents.release();
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
							
							sendMessage(NodeStatsReply.toBytes());
							
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
								
								sendMessage(tempB);
								
								log.info("Sent SimStats " + statsReq.getSimId() + " Size " + (int) Math.ceil(tempB.length / 1024) + "kB");
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
						clientSocket.close();
					}
				}
				
			}
			else
			{
				log.error("Registration failed");
			}
		}
		catch(IOException e)
		{
			
			// Our connection to the remote manager is gone.
			simsManager.removeAll();
			log.info("Removed all Simulations as connection lost");
			
			// Any stats in the cache are not going to be requested.
			statCache = new NodeProcessedItemStatCache();
			log.info("Recreated Node Stat Cache");
		}
		
	}
	
	private void doRegistration(NodeInfo nodeInfo, DataInputStream regInput) throws IOException
	{
		boolean finished = false;
		
		log.info("Attempting Registration");
		
		protocolState = ProtocolState.REG;
		
		// Create a registration request and send it
		sendMessage(new RegistrationRequest().toBytes());
		log.info("Sent Registration Request");
		
		int type = -1;
		int len = -1;
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		// Read the replies
		while(!finished)
		{
			type = regInput.readInt();
			len = regInput.readInt();
			
			// Allocate here to avoid duplication of allocation code
			if(len > 0)
			{
				// Destination
				backingArray = new byte[len];
				
				// Copy from the socket
				regInput.readFully(backingArray, 0, len);
				
				// Wrap the backingArray
				data = ByteBuffer.wrap(backingArray);
				
				bytesRX += backingArray.length;
			}
			
			switch(type)
			{
				case NCP.RegAck:
					
					RegistrationReqAck reqAck = new RegistrationReqAck(data);
					
					nodeInfo.setUid(reqAck.getUid());
					
					log.info("RegAck Recieved UID : " + nodeInfo.getUid());
					
					// We Ack the Ack
					sendMessage(reqAck.toBytes());
				
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
					
					// Create and send the Configuration ack via TransferSocket
					// + Initial NodeState
					sendMessage(new ConfigurationAck(nodeInfo).toBytes());
					
					log.info("Sent Conf Ack : Max Sims " + nodeInfo.getMaxSims());
					
					protocolState = ProtocolState.RDY;
					
					// Now Registered
					finished = true;
				
				break;
				case NCP.INVALID:
				default:
					log.error("Recieved Invalid Frame Type " + type);
					protocolState = ProtocolState.DIS;
				break;
			
			}
			
			if(protocolState == ProtocolState.DIS)
			{
				log.info("Protocol State : " + protocolState.toString());
				finished = true;
			}
			
		}
		
	}
	
	private void sendMessage(byte[] bytes) throws IOException
	{
		cmdTxLock.acquireUninterruptibly();
		
		commandOutput.write(bytes);
		commandOutput.flush();
		
		bytesTX += bytes.length;
		
		cmdTxLock.release();
	}
	
	@Subscribe
	public void SimulationStatChangedEvent(SimulationStatChangedEvent e)
	{
		try
		{
			rxLockEvents.acquireUninterruptibly();
			
			sendMessage(new SimulationStatChanged(e).toBytes());
			
			rxLockEvents.release();
		}
		catch(IOException e1)
		{
			log.error("Failed Sending Simulation Stat Changed " + e.getSimId());
		}
	}
	
	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		try
		{
			rxLockEvents.acquireUninterruptibly();
			
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
			
			sendMessage(new SimulationStateChanged(e).toBytes());
			log.info("Sent Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
			
			rxLockEvents.release();
		}
		catch(IOException e1)
		{
			log.error("Failed Sending Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
		}
	}
}
