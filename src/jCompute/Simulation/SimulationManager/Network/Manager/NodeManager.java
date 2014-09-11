package jCompute.Simulation.SimulationManager.Network.Manager;

import jCompute.JComputeEventBus;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP.ProtocolState;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationReqAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification.SimulationStatChanged;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification.SimulationStateChanged;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.SimulationStatsRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.StartSimCMD;
import jCompute.Simulation.SimulationManager.Network.Node.NodeConfiguration;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeManager
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(NodeManager.class);

	// Locks the node
	private Semaphore nodeLock = new Semaphore(1, false);

	// Node configuration
	private NodeConfiguration nodeConfig;

	private int activeSims = 0;

	// This simulations connected socket
	private final Socket socket;

	// Input Stream
	private Thread recieveThread;

	// Output Stream
	private DataOutputStream output;
	private Semaphore txLock = new Semaphore(1, false);
	private ProtocolState nodeState;

	// Counter for NSMCP state machine
	private int NSMCPReadyTimeOut;

	// Is the remote node active. (connection up)
	private boolean active = false;

	// Semaphores for methods to wait on
	private Semaphore addSimWait = new Semaphore(0, false);
	private Semaphore remSimWait = new Semaphore(0, false);
	private Semaphore simStatsWait = new Semaphore(0, false);

	// Request stats MSG box vars
	private StatExporter statExporter;

	// Add Sim MSG box Vars
	private int addSimId = -1;

	/*
	 * Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (REMOTE)
	 * simId
	 */
	private ConcurrentHashMap<Integer, RemoteSimulationMapping> remoteSimulationMap;

	public NodeManager(int uid, Socket socket) throws IOException
	{
		nodeConfig = new NodeConfiguration();

		remoteSimulationMap = new ConcurrentHashMap<Integer, RemoteSimulationMapping>(4);

		NSMCPReadyTimeOut = 0;

		log.info("New Node Manager " + uid);

		// Internal Connection ID
		nodeConfig.setUid(uid);

		// A connected socket
		this.socket = socket;

		// Output Stream
		output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

		nodeState = ProtocolState.NEW;

		createRecieveThread();

	}

	private void createRecieveThread()
	{
		// The Receive Thread
		recieveThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{

				try
				{
					DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

					int type = -1;

					active = true;

					while (active)
					{
						// Detect Frame
						type = input.readInt();

						switch (type)
						{
							case NSMCP.RegReq :

								log.info("Recieved Registration Request");

								/*
								 * A socket has been connected and we have just
								 * received a registration request
								 */
								if (nodeState == ProtocolState.NEW)
								{
									/*
									 * Later if needed Validate Request Info....
									 * Maybe protocol version etc
									 */

									// Create and Send Reg Ack
									sendMessage(new RegistrationReqAck(nodeConfig.getUid()).toBytes());

									log.info("Sent Registration Ack");

									nodeState = ProtocolState.REG;
								}
								else
								{
									// invalid sequence
									nodeState = ProtocolState.END;

									log.error("Registration Request for node " + nodeConfig.getUid()
											+ " not valid in state " + nodeState.toString());
								}

								break;
							case NSMCP.RegAck :

								/*
								 * A socket has been connected, the remove node
								 * has already sent us a reg req we have sent a
								 * reg ack and are awaiting confirmation. - We
								 * get confirmation and request the node
								 * configuration.
								 */
								if (nodeState == ProtocolState.REG)
								{

									RegistrationReqAck reqAck = new RegistrationReqAck(input);

									int ruid = reqAck.getUid();

									// Check the node is sane (UID should be
									// identical to the one we sent)
									if (nodeConfig.getUid() == ruid)
									{

										log.info("Node registration ok, now requesting node configuration and weighting");

										sendMessage(new ConfigurationRequest(1).toBytes());

									}
									else
									{
										log.error("Node registration not ok " + ruid);

										nodeState = ProtocolState.END;
									}

								}

								break;
							case NSMCP.RegNack :

								/*
								 * A socket has been connected, Remote node has
								 * decided to cancel the registration
								 */
								if (nodeState == ProtocolState.NEW || nodeState == ProtocolState.REG)
								{
									log.info("Node registration nack");
									nodeState = ProtocolState.END;
								}

								break;
							/*
							 * Remove node is about to finish registration. We
							 * are waiting on the node configuration.
							 */
							case NSMCP.ConfAck :

								if (nodeState == ProtocolState.REG)
								{
									log.info("Recieved Conf Ack");

									ConfigurationAck reqAck = new ConfigurationAck(input);

									nodeConfig.setMaxSims(reqAck.getMaxSims());
									nodeConfig.setWeighting(reqAck.getWeighting());
									
									
									log.info("Node " + nodeConfig.getUid() + " Max Sims  : " + nodeConfig.getMaxSims());
									log.info("Node " + nodeConfig.getUid() + " Weighting : " + nodeConfig.getWeighting());

									nodeState = ProtocolState.READY;
								}
								else
								{
									log.error("ConfAck for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}

								break;
							case NSMCP.AddSimReply :

								if (nodeState == ProtocolState.READY)
								{
									addSimId = input.readInt();

									log.info("AddSimReply : " + addSimId);

									addSimWait.release();
								}
								else
								{
									log.error("AddSimReply for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}

								break;
							case NSMCP.SimStateNoti :

								if (nodeState == ProtocolState.READY)
								{
									// Create the state object
									SimulationStateChanged stateChanged = new SimulationStateChanged(input);

									// find the mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.get(stateChanged.getSimId());

									// Debug as these are excessive output
									log.debug(stateChanged.info());
									log.debug("New " + mapping.info());

									// Post the event as if from a local
									// simulation
									JComputeEventBus.post(new SimulationStateChangedEvent(mapping.getLocalSimId(),
											stateChanged.getState(), stateChanged.getRunTime(), stateChanged
													.getStepCount(), stateChanged.getEndEvent()));
								}
								else
								{
									log.error("SimStateNoti for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}

								break;
							case NSMCP.SimStatNoti :

								if (nodeState == ProtocolState.READY)
								{
									// Create the state object
									SimulationStatChanged statChanged = new SimulationStatChanged(input);

									log.debug(statChanged.info());

									// find the mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.get(statChanged.getSimId());

									// We can get stat changes during add sim.
									// (ie when mapping created)
									if (mapping != null)
									{
										log.debug("New " + mapping.info());

										// Post the event as if from a local
										// simulation
										JComputeEventBus.post(new SimulationStatChangedEvent(mapping.getLocalSimId(),
												statChanged.getTime(), statChanged.getStepNo(), statChanged
														.getProgress(), statChanged.getAsps()));
									}
								}
								else
								{
									log.error("SimStatNoti for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}

								break;
							case NSMCP.SimStats :

								if (nodeState == ProtocolState.READY)
								{

									log.debug("Recieved Sim Stats");

									statExporter.populateFromStream(input);

									simStatsWait.release();
								}
								else
								{
									log.error("SimStats for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}

								break;
							case NSMCP.RemSimAck :
								if (nodeState == ProtocolState.READY)
								{
									RemoveSimAck removeSimAck = new RemoveSimAck(input);

									log.info("Recieved RemSimAck : " + removeSimAck.getSimId());

									remSimWait.release();
								}
								else
								{
									log.error("RemSimAck for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}
								break;
							// Test Frame or Garbage
							case NSMCP.INVALID :
							default :
								log.error("Recieved Invalid Frame");
								nodeState = ProtocolState.END;
								break;

						}

						if (nodeState == ProtocolState.END)
						{
							log.info("Protocol State : " + nodeState.toString());
							active = false;
						}
					}
					// Exit // Do Node Shutdown

				}
				catch (IOException e1)
				{
					log.warn("Node " + nodeConfig.getUid() + " Recieve Thread exited");
					// Exit // Do Node Shutdown

					active = false;

					nodeState = ProtocolState.END;

					// Explicit release of all semaphores
					addSimWait.release();
					remSimWait.release();
					simStatsWait.release();

				}

			}
		});

		recieveThread.setName("Node " + nodeConfig.getUid() + " Recieve");

		// Start Processing
		recieveThread.start();

	}

	private void sendMessage(byte[] bytes) throws IOException
	{
		txLock.acquireUninterruptibly();

		output.write(bytes);
		output.flush();
		txLock.release();
	}

	/**
	 * Returns if the node is in the ready state.
	 * 
	 * @return
	 */
	public boolean isReady()
	{
		if (nodeState == ProtocolState.READY)
		{
			return true;
		}

		return false;
	}

	public boolean isActive()
	{
		return active;
	}

	public void incrementTimeOut()
	{
		NSMCPReadyTimeOut+=5;
		log.info("Node " + nodeConfig.getUid() + " NSMCPReadyTimeOut@" + NSMCPReadyTimeOut);
	}

	public int getReadyStateTimeOutValue()
	{
		return NSMCPReadyTimeOut;
	}

	public ArrayList<Integer> getRecoverableSimsIds()
	{
		ArrayList<Integer> list = new ArrayList<Integer>();

		Iterator<Entry<Integer, RemoteSimulationMapping>> itr = remoteSimulationMap.entrySet().iterator();

		while (itr.hasNext())
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
		try
		{
			log.info("Removing Node Manager for Node " + nodeConfig.getUid() + " Reason : " + reason);
			socket.close();
		}
		catch (IOException e)
		{
			log.error("Socket already closed");
		}

	}

	public int getUid()
	{
		return nodeConfig.getUid();
	}

	public int getMaxSims()
	{
		return nodeConfig.getMaxSims();
	}

	public boolean hasFreeSlot()
	{

		if (activeSims < nodeConfig.getMaxSims())
		{
			return true;
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
	public int addSim(String scenarioText, int initialStepRate, RemoteSimulationMapping mapping)
	{
		nodeLock.acquireUninterruptibly();

		log.info("Node " + nodeConfig.getUid() + " AddSim");

		try
		{
			// addSimMsgBoxVarLock.acquireUninterruptibly();

			// Shared variable
			addSimId = -1;

			// Create and Send add Sim Req
			sendMessage(new AddSimReq(scenarioText, initialStepRate).toBytes());

			// addSimMsgBoxVarLock.release();

			// Wait until we are released (by timer or receive thread)
			addSimWait.acquireUninterruptibly();

			// addSimMsgBoxVarLock.acquireUninterruptibly();

			if (addSimId == -1)
			{
				// addSimMsgBoxVarLock.release();
				nodeLock.release();

				return -1;
			}
			else
			{

				mapping.setRemoteSimId(addSimId);

				remoteSimulationMap.put(addSimId, mapping);

				activeSims++;

				// addSimMsgBoxVarLock.release();
				nodeLock.release();

				return addSimId;
			}

		}
		catch (IOException e)
		{
			nodeLock.release();

			// Connection is gone add sim failed
			log.error("Node " + nodeConfig.getUid() + " Error in add Sim");

			return -1;
		}

	}

	/**
	 * Removes a simulation. Blocking.
	 * 
	 * @param remoteSimId
	 */
	public void removeSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();

		try
		{
			sendMessage(new RemoveSimReq(remoteSimId).toBytes());

			remSimWait.acquireUninterruptibly();

			// Remove the mapping as the remote simulation is gone.
			remoteSimulationMap.remove(remoteSimId);

			activeSims--;

		}
		catch (IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeConfig.getUid() + " Error in Start Sim");
		}

		nodeLock.release();
	}

	public void startSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();

		try
		{
			sendMessage(new StartSimCMD(remoteSimId).toBytes());
		}
		catch (IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeConfig.getUid() + " Error in Start Sim");

		}

		nodeLock.release();
	}

	public void exportStats(int remoteSimId, String directory, String fileNameSuffix, ExportFormat format)
	{
		nodeLock.acquireUninterruptibly();

		try
		{
			// create a new exporter as format could change.
			statExporter = new StatExporter(format, fileNameSuffix);

			// Send the request
			sendMessage(new SimulationStatsRequest(remoteSimId, format).toBytes());

			simStatsWait.acquireUninterruptibly();

			// Got reply now export the stats.
			statExporter.exportAllStatsToDir(directory);

		}
		catch (IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeConfig.getUid() + " Error in Start Sim");

		}

		nodeLock.release();
	}

	public long getWeighting()
	{
		return nodeConfig.getWeighting();
	}

}
