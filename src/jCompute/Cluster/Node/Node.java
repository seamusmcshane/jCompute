package jCompute.Cluster.Node;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Protocol.NCP;
import jCompute.Cluster.Protocol.Command.AddSimReply;
import jCompute.Cluster.Protocol.Command.AddSimReq;
import jCompute.Cluster.Protocol.Command.RemoveSimAck;
import jCompute.Cluster.Protocol.Command.RemoveSimReq;
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
import jCompute.SimulationManager.SimulationsManagerInf;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.util.OSInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class Node
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(Node.class);

	// Simulation Manager
	private SimulationsManagerInf simsManager;

	// Protect the send socket
	private Semaphore cmdTxLock = new Semaphore(1, true);

	// To ensure receive frames and events are processed atomically
	private Semaphore rxLockEvents = new Semaphore(1, true);

	// Command Output Stream
	private DataOutputStream commandOutput;

	// Transfer Streams
	private DataOutputStream transferOutput;
	private DataInputStream transferInput;
	private Semaphore transTxLock = new Semaphore(1, true);

	/* Node Cmd Socket */
	private Socket cmdSocket;

	/* Node Transfer Socket */
	private ServerSocket listenNodeTransferSocket;

	// Is the remote node active. (connection up)
	private boolean active = false;

	// Transfer recieve thread
	private Thread transferRecieveThread;

	// ProtocolState
	private ProtocolState state = ProtocolState.CON;

	// Cache of Stats from finished simulations
	private NodeStatCache statCache;

	private NodeStats nodeStats;
	private long simulationsProcessed;

	public Node(String address, SimulationsManagerInf simsManager)
	{
		log.info("Starting Node");
		simulationsProcessed = 0;

		nodeStats = new NodeStats();

		nodeStats.setCpuUsage(OSInfo.getSystemCpuUsage());
		nodeStats.setFreeMemory(OSInfo.getSystemFreeMemory());
		nodeStats.setSimulationsProcessed(simulationsProcessed);

		/* Our Configuration */
		NodeInfo nodeInfo = new NodeInfo();

		nodeInfo.setOperatingSystem(OSInfo.getOSName());
		nodeInfo.setSystemArch(OSInfo.getSystemArch());
		nodeInfo.setHWThreads(OSInfo.getHWThreads());
		nodeInfo.setTotalMemory(OSInfo.getSystemTotalMemory());

		int port = NCP.StandardServerPort;

		this.simsManager = simsManager;

		JComputeEventBus.register(this);

		statCache = new NodeStatCache();
		log.info("Created Node Stat Cache");

		// Disconnect Recovery Loop
		while(true)
		{
			// Connecting to Server
			cmdSocket = null;

			try
			{
				// Only attempt to connect every 5 seconds
				Thread.sleep(5000);

				log.info("Connecting to : " + address + "@" + port);

				// clientSocket = new Socket(address, port);
				cmdSocket = new Socket();
				cmdSocket.connect(new InetSocketAddress(address, port), 1000);

				if(!cmdSocket.isClosed())
				{
					log.info("Connected to : " + cmdSocket.getRemoteSocketAddress());
					log.info("We are : " + cmdSocket.getLocalSocketAddress());

					// Main
					process(nodeInfo, cmdSocket);
				}

				// Close Connection
				if(!cmdSocket.isClosed())
				{
					cmdSocket.close();
				}

				// Close Connection
				if(!listenNodeTransferSocket.isClosed())
				{
					listenNodeTransferSocket.close();
				}

			}
			catch(IOException e)
			{
				log.warn("Connection to " + address + " failed");
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
			final DataInputStream commandInput = new DataInputStream(new BufferedInputStream(
					clientSocket.getInputStream()));

			doRegistration(nodeInfo, commandInput);

			// if Registered successfully
			if(state == ProtocolState.RDY)
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
					}

					switch(type)
					{
						case NCP.AddSimReq:
						{
							rxLockEvents.acquireUninterruptibly();

							AddSimReq req = new AddSimReq(data);

							int simId = simsManager.addSimulation(req.getScenarioText(), req.getInitialStepRate());

							log.info("Added Sim " + simId);

							sendCommandMessage(new AddSimReply(simId).toBytes());

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
						case NCP.RemSimReq:
						{
							RemoveSimReq removeSimReq = new RemoveSimReq(data);

							int simId = removeSimReq.getSimid();

							simsManager.removeSimulation(simId);

							log.info("RemoveSimReq " + simId);

							sendCommandMessage(new RemoveSimAck(simId).toBytes());
						}
						break;
						case NCP.NodeStatsRequest:
						{
							// Read here
							int sequenceNum = data.getInt();

							nodeStats.setCpuUsage(OSInfo.getSystemCpuUsage());
							nodeStats.setFreeMemory(OSInfo.getSystemFreeMemory());
							nodeStats.setSimulationsProcessed(simulationsProcessed);

							NodeStatsReply NodeStatsReply = new NodeStatsReply(sequenceNum, nodeStats);

							log.debug("Node " + nodeInfo.getUid() + " NodeStatsRequest : " + sequenceNum);

							sendCommandMessage(NodeStatsReply.toBytes());

						}
						break;
						// Default / Invalid
						case NCP.INVALID:
						default:
						{
							log.error("Invalid NCP Message Recieved");

							state = ProtocolState.DIS;

							log.error("Error Type " + type + " len " + len);
						}
						break;
					}

					if(state == ProtocolState.DIS)
					{
						clientSocket.close();
						listenNodeTransferSocket.close();
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
			statCache = new NodeStatCache();
			log.info("Recreated Node Stat Cache");
		}

	}

	// RX/TX on transfer socket
	private boolean doTransferSocketSetup(NodeInfo nodeInfo, ServerSocket listenNodeTransferSocket) throws IOException
	{

		boolean finished = false;
		log.info("Waiting for connection to transfer socket");
		Socket nodeTransferSocket = listenNodeTransferSocket.accept();
		log.info("Transfer socket now connected");
		nodeTransferSocket.setSendBufferSize(1048576);

		transferInput = new DataInputStream(new BufferedInputStream(nodeTransferSocket.getInputStream()));
		transferOutput = new DataOutputStream(new BufferedOutputStream(nodeTransferSocket.getOutputStream()));

		int type = -1;
		int len = -1;
		byte[] backingArray = null;
		ByteBuffer data = null;

		// Read the replies
		while(!finished)
		{
			type = transferInput.readInt();
			len = transferInput.readInt();

			// Allocate here to avoid duplication of allocation code
			if(len > 0)
			{
				// Destination
				backingArray = new byte[len];

				// Copy from the socket
				transferInput.readFully(backingArray, 0, len);

				// Wrap the backingArray
				data = ByteBuffer.wrap(backingArray);
			}

			switch(type)
			{
				case NCP.ConfReq:

					ConfigurationRequest confReq = new ConfigurationRequest(data);

					// Set our max sims now
					nodeInfo.setMaxSims(simsManager.getMaxSims());
					log.info("ConfReq Recieved");

					int benchMark = confReq.getBench();

					if(benchMark > 0)
					{
						log.info("Running Weighting Benchmark");
						NodeWeightingBenchmark bench = new NodeWeightingBenchmark(confReq.getObjects(),
								confReq.getIterations());
						bench.warmUp(confReq.getWarmup());
						long weighting = bench.weightingBenchmark(confReq.getRuns());
						nodeInfo.setWeighting(weighting);
						log.info("Weighting\t " + weighting);
					}

					// Create and send the Configuration ack via TransferSocket
					sendTransferMessage(new ConfigurationAck(nodeInfo).toBytes());

					log.info("Sent Conf Ack : Max Sims " + nodeInfo.getMaxSims());

					state = ProtocolState.RDY;

					createTransferRecieveThread(nodeInfo.getUid());

					// Now Registered
					finished = true;

				break;
				case NCP.INVALID:
				default:
					log.error("Recieved Invalid Frame Type " + type);
					state = ProtocolState.DIS;
				break;

			}

			if(state == ProtocolState.DIS)
			{
				log.info("Protocol State : " + state.toString());
				finished = true;
			}

		}

		return true;
	}

	private void doRegistration(NodeInfo nodeInfo, DataInputStream regInput) throws IOException
	{
		boolean finished = false;

		log.info("Attempting Registration");

		state = ProtocolState.REG;

		// Create a registration request and send it
		sendCommandMessage(new RegistrationRequest().toBytes());
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
			}

			switch(type)
			{
				case NCP.RegAck:

					RegistrationReqAck reqAck = new RegistrationReqAck(data);

					nodeInfo.setUid(reqAck.getUid());

					log.info("RegAck Recieved UID : " + nodeInfo.getUid());

					// Create the transferSocket
					listenNodeTransferSocket = new ServerSocket();
					log.info("Created transfer socket");
					listenNodeTransferSocket.bind(new InetSocketAddress("0.0.0.0", NCP.NodeTransferPort));

					// We Ack the Ack
					sendCommandMessage(reqAck.toBytes());

					finished = doTransferSocketSetup(nodeInfo, listenNodeTransferSocket);

				break;
				case NCP.INVALID:
				default:
					log.error("Recieved Invalid Frame Type " + type);
					state = ProtocolState.DIS;
				break;

			}

			if(state == ProtocolState.DIS)
			{
				log.info("Protocol State : " + state.toString());
				finished = true;
			}

		}

	}

	private void createTransferRecieveThread(int nodeUid)
	{
		final int uid = nodeUid;
		// The Transfer Receive Thread
		transferRecieveThread = new Thread(new Runnable()
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

					active = true;

					while(active)
					{
						// Detect Frame
						type = transferInput.readInt();
						len = transferInput.readInt();

						// Allocate here to avoid duplication of allocation code
						if(len > 0)
						{
							// Destination
							backingArray = new byte[len];

							// Copy from the socket
							transferInput.readFully(backingArray, 0, len);

							// Wrap the backingArray
							data = ByteBuffer.wrap(backingArray);

							log.debug("Type " + type + " len " + len);
						}

						switch(type)
						{
							case NCP.SimStatsReq:
							{
								SimulationStatsRequest statsReq = new SimulationStatsRequest(data);

								log.info("SimStatsReq " + statsReq.getSimId());

								// Get the stat exporter for this simId and
								// create a Stats Reply
								SimulationStatsReply statsReply = new SimulationStatsReply(statsReq.getSimId(),
										statCache.remove(statsReq.getSimId()));

								// NCP.SimStats
								sendTransferMessage(statsReply.toBytes());

								log.info("Sent SimStats " + statsReq.getSimId());
							}
							break;
							default:
								log.error("Recieved Invalid Frame");
								state = ProtocolState.DIS;

								log.error("Error Type " + type + " len " + len);

							break;

						}

						if(state == ProtocolState.DIS)
						{
							log.info("Protocol State : " + state.toString());
							active = false;

							// Close Connection
							if(!cmdSocket.isClosed())
							{
								cmdSocket.close();
							}

							// Close Connection
							if(!listenNodeTransferSocket.isClosed())
							{
								listenNodeTransferSocket.close();
							}
						}
					}
					// Exit // Do Node Shutdown

				}
				catch(IOException e1)
				{
					log.warn("Node " + uid + " Recieve Thread exited");
					// Exit // Do Node Shutdown

					active = false;

					state = ProtocolState.DIS;
				}

			}

		});

		transferRecieveThread.setName("Node " + uid + " Transfer Recieve");

		// Start Processing
		transferRecieveThread.start();
	}

	private void sendCommandMessage(byte[] bytes) throws IOException
	{
		cmdTxLock.acquireUninterruptibly();

		commandOutput.write(bytes);
		commandOutput.flush();

		cmdTxLock.release();
	}

	private void sendTransferMessage(byte[] bytes) throws IOException
	{
		transTxLock.acquireUninterruptibly();

		transferOutput.write(bytes);
		transferOutput.flush();

		transTxLock.release();
	}

	@Subscribe
	public void SimulationStatChangedEvent(SimulationStatChangedEvent e)
	{
		try
		{
			rxLockEvents.acquireUninterruptibly();

			sendCommandMessage(new SimulationStatChanged(e).toBytes());

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

				StatExporter exporter = simsManager.getStatExporter(simId, "", ExportFormat.ZXML);

				log.info("Stored Stats for Simulation " + simId);
				statCache.put(simId, exporter);

				simsManager.removeSimulation(simId);
				log.info("Removed Finished Simulation");

				simulationsProcessed++;
			}

			sendCommandMessage(new SimulationStateChanged(e).toBytes());
			log.info("Sent Simulation State Changed " + e.getSimId() + " " + e.getState().toString());

			rxLockEvents.release();
		}
		catch(IOException e1)
		{
			log.error("Failed Sending Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
		}
	}
}
