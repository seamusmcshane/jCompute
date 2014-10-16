package jCompute.Simulation.SimulationManager.Network.Node;

import jCompute.JComputeEventBus;
import jCompute.Datastruct.knn.benchmark.NodeWeightingBenchmark;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP.ProtocolState;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationReqAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification.SimulationStatChanged;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification.SimulationStateChanged;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReply;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.SimulationStatsRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.StartSimCMD;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;

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
	private ProtocolState state = ProtocolState.NEW;
	
	// Cache of Stats from finished simulations
	private NodeStatCache statCache;
	
	public Node(String address, SimulationsManagerInf simsManager)
	{
		log.info("Starting Node");

		/* Our Configuration */
		NodeConfiguration nodeConfig = new NodeConfiguration();

		int port = NSMCP.StandardServerPort;

		this.simsManager = simsManager;

		JComputeEventBus.register(this);
		log.info("Registered on event bus");

		statCache = new NodeStatCache();
		log.info("Created Node Stat Cache");

		// Disconnect Recovery Loop
		while (true)
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

				if (!cmdSocket.isClosed())
				{
					log.info("Connected to : " + cmdSocket.getRemoteSocketAddress());
					log.info("We are : " + cmdSocket.getLocalSocketAddress());

					// Main
					process(nodeConfig, cmdSocket);
				}

				// Close Connection
				if (!cmdSocket.isClosed())
				{
					cmdSocket.close();
				}
				
				// Close Connection
				if (!listenNodeTransferSocket.isClosed())
				{
					listenNodeTransferSocket.close();
				}
				
			}
			catch (IOException e)
			{
				log.warn("Connection to " + address + " failed");
			}
			catch (InterruptedException e)
			{
				log.warn("Sleep interupted");
			}

		}

	}

	// RX/TX on Command socket 
	private void process(NodeConfiguration nodeConfig, Socket clientSocket)
	{
		try
		{
			// Link up output
			commandOutput = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

			// Input Stream
			final DataInputStream commandInput = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

			doRegistration(nodeConfig, commandInput);

			// if Registered successfully
			if (state == ProtocolState.READY)
			{
				log.info("Registration complete");

				int type = -1;
				int len = -1;
				byte[] backingArray = null;
				ByteBuffer data = null;
				
				// While we have a connection
				while (!clientSocket.isClosed())
				{
					type = commandInput.readInt();
					len = commandInput.readInt();

					// Allocate here to avoid duplication of allocation code
					if(len > 0 )
					{
						// Destination
						backingArray = new byte[len];
						
						// Copy from the socket
						commandInput.readFully(backingArray, 0, len);
						
						// Wrap the backingArray
						data = ByteBuffer.wrap(backingArray);	
						
						log.debug("Type " + type+ " len " + len);
					}

					switch (type)
					{
						case NSMCP.AddSimReq :
						{
							rxLockEvents.acquireUninterruptibly();
							
							AddSimReq req = new AddSimReq(data);

							int simId = simsManager.addSimulation(req.getScenarioText(), req.getInitialStepRate());

							log.info("Added Sim " + simId);

							sendCommandMessage(new AddSimReply(simId).toBytes());

							rxLockEvents.release();
						}
							break;
						case NSMCP.StartSimCMD :
						{
							StartSimCMD cmd = new StartSimCMD(data);

							log.info("StartSimCMD " + cmd.getSimid());
							
							simsManager.startSim(cmd.getSimid());
						}
							break;
						case NSMCP.RemSimReq :
						{
							RemoveSimReq removeSimReq = new RemoveSimReq(data);

							int simId = removeSimReq.getSimid();

							simsManager.removeSimulation(simId);

							log.info("RemoveSimReq " + simId);

							sendCommandMessage(new RemoveSimAck(simId).toBytes());							
						}
							break;
						// Default / Invalid
						case NSMCP.INVALID :
						default :
						{
							log.error("Invalid NSMCP Message Recieved");

							state = ProtocolState.END;

							log.error("Error Type " + type + " len " + len);
						}	
							break;
					}

					if (state == ProtocolState.END)
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
		catch (IOException e)
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
	private boolean doTransferSocketSetup(NodeConfiguration nodeConfig, ServerSocket listenNodeTransferSocket) throws IOException
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
		while (!finished)
		{
			type = transferInput.readInt();
			len = transferInput.readInt();

			// Allocate here to avoid duplication of allocation code
			if(len > 0 )
			{
				// Destination
				backingArray = new byte[len];
				
				// Copy from the socket
				transferInput.readFully(backingArray, 0, len);
				
				// Wrap the backingArray
				data = ByteBuffer.wrap(backingArray);							
			}

			switch (type)
			{
				case NSMCP.ConfReq :

					ConfigurationRequest confReq = new ConfigurationRequest(data);
					
					// Set our max sims now
					nodeConfig.setMaxSims(simsManager.getMaxSims());
					log.info("ConfReq Recieved");

					int benchMark = confReq.getBench();
					if(benchMark == 1)
					{
						log.info("Running Weighting Benchmark");
						NodeWeightingBenchmark bench = new NodeWeightingBenchmark(3072,1000);
						bench.warmUp(100);
						long weighting = bench.weightingBenchmark(5);
						nodeConfig.setWeighting(weighting);
						log.info("Weighting\t " + weighting );
					}
					
					// Create and send the Configuration ack via TransferSocket
					sendTransferMessage(new ConfigurationAck(nodeConfig).toBytes());

					log.info("Sent Conf Ack : Max Sims " + nodeConfig.getMaxSims());

					state = ProtocolState.READY;

					createTransferRecieveThread(nodeConfig.getUid());
					
					// Now Registered
					finished = true;

					break;
				case NSMCP.INVALID :
				default :
					log.error("Recieved Invalid Frame Type " + type);
					state = ProtocolState.END;
				break;

			}

			if (state == ProtocolState.END)
			{
				log.info("Protocol State : " + state.toString());
				finished = true;
			}

		}
		
		return true;
	}
	
	private void doRegistration(NodeConfiguration nodeConfig, DataInputStream regInput) throws IOException
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
		while (!finished)
		{
			type = regInput.readInt();
			len = regInput.readInt();

			// Allocate here to avoid duplication of allocation code
			if(len > 0 )
			{
				// Destination
				backingArray = new byte[len];
				
				// Copy from the socket
				regInput.readFully(backingArray, 0, len);
				
				// Wrap the backingArray
				data = ByteBuffer.wrap(backingArray);							
			}

			switch (type)
			{
				case NSMCP.RegAck :
					
					RegistrationReqAck reqAck = new RegistrationReqAck(data);

					nodeConfig.setUid(reqAck.getUid());

					log.info("RegAck Recieved UID : " + nodeConfig.getUid());

					// Create the transferSocket
					listenNodeTransferSocket = new ServerSocket();
					log.info("Created transfer socket");
					listenNodeTransferSocket.bind(new InetSocketAddress("0.0.0.0",NSMCP.NodeTransferPort));
				
					// We Ack the Ack
					sendCommandMessage(reqAck.toBytes());
					
					finished = doTransferSocketSetup(nodeConfig,listenNodeTransferSocket);

				break;
				case NSMCP.INVALID :
				default :
					log.error("Recieved Invalid Frame Type " + type);
					state = ProtocolState.END;
					break;

			}

			if (state == ProtocolState.END)
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

					while (active)
					{
						// Detect Frame
						type = transferInput.readInt();
						len = transferInput.readInt();

						// Allocate here to avoid duplication of allocation code
						if(len > 0 )
						{
							// Destination
							backingArray = new byte[len];
							
							// Copy from the socket
							transferInput.readFully(backingArray, 0, len);
							
							// Wrap the backingArray
							data = ByteBuffer.wrap(backingArray);
							
							log.debug("Type " + type+ " len " + len);
						}

						switch (type)
						{
							case NSMCP.SimStatsReq :
							{
								SimulationStatsRequest statsReq = new SimulationStatsRequest(data);

								log.info("SimStatsReq " + statsReq.getSimId());

								// Get the stat exporter for this simId
								// StatExporter exporter = simsManager.getStatExporter(statsReq.getSimId(), "",  statsReq.getFormat());
								StatExporter exporter = statCache.remove(statsReq.getSimId());								
								
								// NSMCP.SimStats
								sendTransferMessage(exporter.toBytes());
								
								log.info("Sent SimStats " + statsReq.getSimId());
							}
							break;
							default :
								log.error("Recieved Invalid Frame");
								state = ProtocolState.END;
								
								log.error("Error Type " + type + " len " + len);
								
								break;

						}

						if (state == ProtocolState.END)
						{
							log.info("Protocol State : " + state.toString());
							active = false;
							
							// Close Connection
							if (!cmdSocket.isClosed())
							{
								cmdSocket.close();
							}
							
							// Close Connection
							if (!listenNodeTransferSocket.isClosed())
							{
								listenNodeTransferSocket.close();
							}
						}
					}
					// Exit // Do Node Shutdown

				}
				catch (IOException e1)
				{
					log.warn("Node " + uid + " Recieve Thread exited");
					// Exit // Do Node Shutdown

					active = false;

					state = ProtocolState.END;
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
		catch (IOException e1)
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
						
			if (e.getState() == SimState.FINISHED )
			{
				int simId = e.getSimId();
				
				StatExporter exporter = simsManager.getStatExporter(simId, "",  ExportFormat.ZXML);
				
				log.info("Stored Stats for Simulation " + simId);
				statCache.put(simId, exporter);
				
				simsManager.removeSimulation(simId);
				log.info("Removed Finished Simulation");
			}

			sendCommandMessage(new SimulationStateChanged(e).toBytes());
			log.info("Sent Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
			
			rxLockEvents.release();
		}
		catch (IOException e1)
		{
			log.error("Failed Sending Simulation State Changed " + e.getSimId() + " "
					+ e.getState().toString());
		}
	}
}
