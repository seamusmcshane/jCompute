package jCompute.Simulation.SimulationManager.Network.Node;

import jCompute.JComputeEventBus;
import jCompute.Datastruct.knn.benchmark.NodeWeightingBenchmark;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
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
	private Semaphore txLock = new Semaphore(1, true);

	// To ensure receive frames and events are processed atomically
	private Semaphore rxLockEvents = new Semaphore(1, true);

	// Output Stream
	private DataOutputStream output;

	// ProtocolState
	private ProtocolState state = ProtocolState.NEW;
	
	public Node(String address, SimulationsManagerInf simsManager)
	{
		log.info("Starting Node");

		/* Our Configuration */
		NodeConfiguration nodeConfig = new NodeConfiguration();

		int port = NSMCP.StandardServerPort;

		this.simsManager = simsManager;

		JComputeEventBus.register(this);
		log.info("Registered on event bus");

		// Disconnect Recovery Loop
		while (true)
		{
			// Connecting to Server
			Socket clientSocket = null;

			try
			{
				// Only attempt to connect every 5 seconds
				Thread.sleep(5000);

				log.info("Connecting to : " + address + "@" + port);

				// clientSocket = new Socket(address, port);
				clientSocket = new Socket();

				clientSocket.setSendBufferSize(1048576);
				clientSocket.setReceiveBufferSize(32768);

				clientSocket.connect(new InetSocketAddress(address, port), 1000);

				if (!clientSocket.isClosed())
				{
					log.info("Connected to : " + clientSocket.getRemoteSocketAddress());
					log.info("We are : " + clientSocket.getLocalSocketAddress());

					// Main
					process(nodeConfig, clientSocket);
				}

				// Close Connection
				if (!clientSocket.isClosed())
				{
					clientSocket.close();
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

	private void process(NodeConfiguration nodeConfig, Socket clientSocket)
	{

		try
		{
			// Link up output
			output = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

			// Input Stream
			final DataInputStream input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

			doRegistration(nodeConfig, input);

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
					type = input.readInt();
					len = input.readInt();

					// Allocate here to avoid duplication of allocation code
					if(len > 0 )
					{
						// Destination
						backingArray = new byte[len];
						
						// Copy from the socket
						input.readFully(backingArray, 0, len);
						
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

							sendMessage(new AddSimReply(simId).toBytes());

							rxLockEvents.release();
						}
							break;
						case NSMCP.StartSimCMD :

							StartSimCMD cmd = new StartSimCMD(data);

							log.info("StartSimCMD " + cmd.getSimid());
							
							simsManager.startSim(cmd.getSimid());

							break;
						case NSMCP.SimStatsReq :

							SimulationStatsRequest statsReq = new SimulationStatsRequest(data);

							log.info("SimStatsReq " + statsReq.getSimId());

							// NSMCP.SimStats
							sendMessage(simsManager.getStatsAsBytes(statsReq.getSimId(), statsReq.getFormat()));
							log.info("Sent SimStats " + statsReq.getSimId());

							break;
						case NSMCP.RemSimReq :
						{
							RemoveSimReq removeSimReq = new RemoveSimReq(data);

							int simId = removeSimReq.getSimid();

							simsManager.removeSimulation(simId);

							log.info("RemoveSimReq " + simId);

							sendMessage(new RemoveSimAck(simId).toBytes());
						}
							break;
						// Default / Invalid
						case NSMCP.INVALID :
						default :
							log.error("Invalid NSMCP Message Recieved");

							state = ProtocolState.END;

							log.error("Error Type " + type + " len " + len);
							
							break;
					}

					if (state == ProtocolState.END)
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
		catch (IOException e)
		{

			// Our connection to the remote manager is gone.
			simsManager.removeAll();

			log.info("Removed all Simulations as connection lost");
		}

	}

	private void doRegistration(NodeConfiguration nodeConfig, DataInputStream input) throws IOException
	{
		boolean finished = false;

		log.info("Attempting Registration");

		state = ProtocolState.REG;

		// Create a registration request and send it
		sendMessage(new RegistrationRequest().toBytes());

		int type = -1;
		int len = -1;
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		// Read the replies
		while (!finished)
		{
			type = input.readInt();
			len = input.readInt();

			// Allocate here to avoid duplication of allocation code
			if(len > 0 )
			{
				// Destination
				backingArray = new byte[len];
				
				// Copy from the socket
				input.readFully(backingArray, 0, len);
				
				// Wrap the backingArray
				data = ByteBuffer.wrap(backingArray);							
			}

			switch (type)
			{
				case NSMCP.RegAck :
					
					RegistrationReqAck reqAck = new RegistrationReqAck(data);

					nodeConfig.setUid(reqAck.getUid());

					log.info("RegAck Recieved UID : " + nodeConfig.getUid());

					// We Ack the Ack
					sendMessage(reqAck.toBytes());

					break;
				case NSMCP.ConfReq :

					ConfigurationRequest confReq = new ConfigurationRequest(data);
					
					// Set our max sims now
					nodeConfig.setMaxSims(simsManager.getMaxSims());
					log.info("ConfReq Recieved");

					int benchMark = confReq.getBench();
					if(benchMark == 1)
					{
						log.info("Running Weighting Benchmark");
						NodeWeightingBenchmark bench = new NodeWeightingBenchmark(8192,1000);
						bench.warmUp(1000);
						long weighting = bench.weightingBenchmark(5);
						nodeConfig.setWeighting(weighting);
						log.info("Weighting\t " + weighting );
					}
					
					// Create and send the Configuration ack
					sendMessage(new ConfigurationAck(nodeConfig).toBytes());

					log.info("Sent Conf Ack : Max Sims " + nodeConfig.getMaxSims());

					state = ProtocolState.READY;

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

	}

	private void sendMessage(byte[] bytes) throws IOException
	{
		txLock.acquireUninterruptibly();

		output.write(bytes);
		output.flush();

		txLock.release();
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

			sendMessage(new SimulationStateChanged(e).toBytes());
			log.debug("Sent Simulation State Changed " + e.getSimId() + " " + e.getState().toString());

			rxLockEvents.release();
		}
		catch (IOException e1)
		{
			log.error("Failed Sending Simulation State Changed " + e.getSimId() + " "
					+ e.getState().toString());
		}
	}
}
