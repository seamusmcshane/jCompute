package jCompute.Cluster.Controller;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Event.NodeStatsUpdate;
import jCompute.Cluster.Controller.Mapping.NodeManagerStatRequestMapping;
import jCompute.Cluster.Controller.Mapping.RemoteSimulationMapping;
import jCompute.Cluster.Node.NodeInfo;
import jCompute.Cluster.Protocol.NCP;
import jCompute.Cluster.Protocol.Command.AddSimReply;
import jCompute.Cluster.Protocol.Command.AddSimReq;
import jCompute.Cluster.Protocol.Command.RemoveSimAck;
import jCompute.Cluster.Protocol.Command.SimulationStatsReply;
import jCompute.Cluster.Protocol.Command.SimulationStatsRequest;
import jCompute.Cluster.Protocol.Command.StartSimCMD;
import jCompute.Cluster.Protocol.NCP.ProtocolState;
import jCompute.Cluster.Protocol.Monitoring.NodeStatsReply;
import jCompute.Cluster.Protocol.Monitoring.NodeStatsRequest;
import jCompute.Cluster.Protocol.Notification.SimulationStatChanged;
import jCompute.Cluster.Protocol.Notification.SimulationStateChanged;
import jCompute.Cluster.Protocol.Registration.ConfigurationAck;
import jCompute.Cluster.Protocol.Registration.ConfigurationRequest;
import jCompute.Cluster.Protocol.Registration.RegistrationReqAck;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;

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
import java.util.concurrent.ConcurrentHashMap.KeySetView;
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
	private NodeInfo nodeInfo;

	private int activeSims = 0;
	private Semaphore activeSimsLock = new Semaphore(1, false);

	// This node cmd socket
	private final Socket cmdSocket;

	// Output Stream
	private DataOutputStream cmdOutput;
	private DataInputStream cmdInput;
	private Semaphore cmdTxLock = new Semaphore(1, false);
	private Thread cmdRecieveThread;

	private ProtocolState nodeState;

	// Counter for NCP state machine
	private int NSMCPReadyTimeOut;

	// Is the remote node active. (connection up)
	private boolean active = false;

	// Semaphores for methods to wait on
	private Semaphore addSimWait = new Semaphore(0, false);
	private Semaphore remSimWait = new Semaphore(0, false);

	// Add Sim MSG box Vars
	private int addSimId = -1;

	/*
	 * Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (REMOTE)
	 * simId
	 */
	private ConcurrentHashMap<Integer, RemoteSimulationMapping> remoteSimulationMap;

	// Stats Request map
	private ConcurrentHashMap<Integer, NodeManagerStatRequestMapping> statRequests;

	public NodeManager(int uid, Socket cmdSocket) throws IOException
	{
		nodeInfo = new NodeInfo();

		remoteSimulationMap = new ConcurrentHashMap<Integer, RemoteSimulationMapping>(8);

		statRequests = new ConcurrentHashMap<Integer, NodeManagerStatRequestMapping>(8);

		NSMCPReadyTimeOut = 0;

		log.info("New Node Manager " + uid);

		// Internal Connection ID
		nodeInfo.setUid(uid);

		// Node Address
		nodeInfo.setAddress(cmdSocket.getInetAddress().getHostAddress());

		// A connected socket
		this.cmdSocket = cmdSocket;

		// Cmd Output Stream
		cmdOutput = new DataOutputStream(new BufferedOutputStream(cmdSocket.getOutputStream()));

		// Cmd Input Stream
		cmdInput = new DataInputStream(new BufferedInputStream(cmdSocket.getInputStream()));

		nodeState = ProtocolState.CON;

		Thread nodeManager = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if(handleRegistration())
					{
						log.info("Registration Succeeded");
						createCMDRecieveThread();
					}
					else
					{
						log.info("Registration Failed");
						active = false;
					}

				}
				catch(IOException e)
				{
					active = false;
				}
			}
		});

		nodeManager.setName("NodeManager");
		nodeManager.start();
	}

	private boolean handleRegistration() throws IOException
	{
		log.info("Awaiting Registration");

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

				log.info("Type " + type + " len " + len);
			}

			switch(type)
			{
				case NCP.RegReq:
					log.info("Recieved Registration Request");

					/*
					 * A socket has been connected and we have just received a
					 * registration request
					 */
					if(nodeState == ProtocolState.CON)
					{
						/*
						 * Later if needed Validate Request Info.... Maybe
						 * protocol version etc
						 */

						// Create and Send Reg Ack
						sendCMDMessage(new RegistrationReqAck(nodeInfo.getUid()).toBytes());

						log.info("Sent Registration Ack");

						nodeState = ProtocolState.REG;
					}
					else
					{
						log.error("Registration Request for node " + nodeInfo.getUid() + " not valid in state "
								+ nodeState.toString());

						// invalid sequence
						nodeState = ProtocolState.DIS;
					}
				break;
				case NCP.RegAck:

					/*
					 * A socket has been connected, the remove node has already
					 * sent us a reg req we have sent a reg ack and are awaiting
					 * confirmation. - We get confirmation and request the node
					 * configuration.
					 */
					if(nodeState == ProtocolState.REG)
					{
						RegistrationReqAck reqAck = new RegistrationReqAck(data);

						int ruid = reqAck.getUid();

						// Check the node is sane (UID should be
						// identical to the one we sent)
						if(nodeInfo.getUid() == ruid)
						{
							log.info("Node registration ok");

							log.info("Now requesting node configuration and weighting");
							sendCMDMessage(new ConfigurationRequest(1, 1024, 5000, 1000, 5).toBytes());
						}
						else
						{
							log.error("Node registration not ok " + ruid);

							nodeState = ProtocolState.DIS;
						}

					}
				break;
				case NCP.RegNack:

					/*
					 * A socket has been connected, Remote node has decided to
					 * cancel the registration
					 */
					if(nodeState == ProtocolState.CON || nodeState == ProtocolState.REG)
					{
						log.info("Node registration nack");
						nodeState = ProtocolState.DIS;
					}

				break;
				case NCP.ConfAck:

					if(nodeState == ProtocolState.REG)
					{
						log.info("Recieved Conf Ack");

						ConfigurationAck reqAck = new ConfigurationAck(data);

						nodeInfo.setMaxSims(reqAck.getMaxSims());
						nodeInfo.setWeighting(reqAck.getWeighting());

						nodeInfo.setHWThreads(reqAck.getHwThreads());
						nodeInfo.setOperatingSystem(reqAck.getOs());
						nodeInfo.setSystemArch(reqAck.getArch());
						nodeInfo.setTotalMemory(reqAck.getTotalMemory());

						log.debug("Node " + nodeInfo.getUid() + " Max Sims  : " + nodeInfo.getMaxSims());
						log.debug("Node " + nodeInfo.getUid() + " HW Threads: " + nodeInfo.getHWThreads());
						log.debug("Node " + nodeInfo.getUid() + " Weighting : " + nodeInfo.getWeighting());
						log.debug("Node " + nodeInfo.getUid() + " OS        : " + nodeInfo.getOperatingSystem());
						log.debug("Node " + nodeInfo.getUid() + " Arch      : " + nodeInfo.getSystemArch());
						log.debug("Node " + nodeInfo.getUid() + " TotalMem  : " + nodeInfo.getTotalMemory());

						nodeState = ProtocolState.RDY;

					}
					else
					{
						log.error("ConfAck for node " + nodeInfo.getUid() + " not valid in state "
								+ nodeState.toString());
					}

				break;
				// Test Frame or Garbage
				case NCP.INVALID:
				default:
					log.error("Recieved Invalid Frame");
					nodeState = ProtocolState.DIS;
					log.error("Error Type " + type + " len " + len);
				break;
			}

			// Check Node State
			switch(nodeState)
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
				break;
			}
		}

		return registered;
	}

	private void createCMDRecieveThread()
	{
		// The Command Receive Thread
		cmdRecieveThread = new Thread(new Runnable()
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

								if(nodeState == ProtocolState.RDY)
								{
									AddSimReply addSimReply = new AddSimReply(data);
									addSimId = addSimReply.getSimId();

									log.info("AddSimReply : " + addSimId);

									addSimWait.release();
								}
								else
								{
									log.error("AddSimReply for node " + nodeInfo.getUid() + " not valid in state "
											+ nodeState.toString());
								}

							break;
							case NCP.SimStateNoti:

								if(nodeState == ProtocolState.RDY)
								{
									// Create the state object
									SimulationStateChanged stateChanged = new SimulationStateChanged(data);

									// find the mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.get(stateChanged.getSimId());

									// Debug as these are excessive output
									log.info(stateChanged.info());
									log.debug("New " + mapping.info());

									// Post the event as if from a local
									// simulation
									JComputeEventBus.post(new SimulationStateChangedEvent(mapping.getLocalSimId(),
											stateChanged.getState(), stateChanged.getRunTime(), stateChanged
													.getStepCount(), stateChanged.getEndEvent()));

									if(stateChanged.getState() == SimState.FINISHED)
									{

										activeSimsLock.acquireUninterruptibly();

										// Remote Sim is auto-removed when
										// finished
										activeSims--;

										activeSimsLock.release();

										JComputeEventBus.post(new SimulationsManagerEvent(mapping.getLocalSimId(),
												SimulationsManagerEventType.RemovedSim));

									}

								}
								else
								{
									log.error("SimStateNoti for node " + nodeInfo.getUid() + " not valid in state "
											+ nodeState.toString());
								}

							break;
							case NCP.SimStatNoti:

								if(nodeState == ProtocolState.RDY)
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

										// Post the event as if from a local
										// simulation
										JComputeEventBus.post(new SimulationStatChangedEvent(mapping.getLocalSimId(),
												statChanged.getTime(), statChanged.getStepNo(), statChanged
														.getProgress(), statChanged.getAsps()));

									}
									else
									{
										log.warn("No mapping found for " + statChanged.info());

									}
								}
								else
								{
									log.error("SimStatNoti for node " + nodeInfo.getUid() + " not valid in state "
											+ nodeState.toString());
								}

							break;
							case NCP.RemSimAck:
								if(nodeState == ProtocolState.RDY)
								{
									RemoveSimAck removeSimAck = new RemoveSimAck(data);

									log.info("Recieved RemSimAck : " + removeSimAck.getSimId());

									remSimWait.release();
								}
								else
								{
									log.error("RemSimAck for node " + nodeInfo.getUid() + " not valid in state "
											+ nodeState.toString());
								}
							break;
							case NCP.NodeStatsReply:
								if(nodeState == ProtocolState.RDY)
								{
									NodeStatsReply nodeStatsReply = new NodeStatsReply(data);

									log.debug("Recieved NodeStatsReply");

									JComputeEventBus.post(new NodeStatsUpdate(nodeInfo.getUid(), nodeStatsReply
											.getSequenceNum(), nodeStatsReply.getNodeStats()));

								}
								else
								{
									log.error("NodeStatsReply for node " + nodeInfo.getUid() + " not valid in state "
											+ nodeState.toString());
								}
							break;
							case NCP.SimStats:

								if(nodeState == ProtocolState.RDY)
								{
									log.info("Recieved Sim Stats");

									// Read this here as we need it for the stat
									// request mapping lookup
									int simId = data.getInt();

									// Remove Request Mapping
									NodeManagerStatRequestMapping request = statRequests.remove(simId);

									SimulationStatsReply statsReply = new SimulationStatsReply(simId, data,
											request.getFormat(), request.getFileNameSuffix());

									request.setStatExporter(statsReply.getStatExporter());

									// Signal the waiting thread.
									request.signalReply();
								}
								else
								{
									log.error("SimStats for node " + nodeInfo.getUid() + " not valid in state "
											+ nodeState.toString());
								}

							break;
							// Test Frame or Garbage
							case NCP.INVALID:
							default:
								log.error("Recieved Invalid Frame");
								nodeState = ProtocolState.DIS;

								log.error("Error Type " + type + " len " + len);

							break;

						}

						if(nodeState == ProtocolState.DIS)
						{
							log.info("Protocol State : " + nodeState.toString());
							active = false;
						}
					}
					// Exit // Do Node Shutdown

				}
				catch(IOException e1)
				{
					log.warn("Node " + nodeInfo.getUid() + " Recieve Thread exited");
					// Exit // Do Node Shutdown

					active = false;

					nodeState = ProtocolState.DIS;

					// Explicit release of all semaphores
					addSimWait.release();
					remSimWait.release();
					
					// Release any threads waiting on a stat reply - they will get a null stat exporter object.
					KeySetView<Integer, NodeManagerStatRequestMapping> keys = statRequests.keySet();
					
					for(Integer key : keys)
					{
						NodeManagerStatRequestMapping request = statRequests.remove(key);
						
						log.info("Aborting Stat fetch for Request " + key);
												
						request.setStatExporter(null);
						
						request.signalReply();
					}
					
				}

			}
		});

		cmdRecieveThread.setName("Node " + nodeInfo.getUid() + " Command Recieve");

		// Start Processing
		cmdRecieveThread.start();

	}

	private void sendCMDMessage(byte[] bytes) throws IOException
	{
		cmdTxLock.acquireUninterruptibly();

		cmdOutput.write(bytes);
		cmdOutput.flush();

		cmdTxLock.release();
	}

	/**
	 * Returns if the node is in the ready state.
	 * 
	 * @return
	 */
	public boolean isReady()
	{
		if(nodeState == ProtocolState.RDY)
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
		NSMCPReadyTimeOut += 5;
		log.info("Node " + nodeInfo.getUid() + " TimeOut@" + NSMCPReadyTimeOut);
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
		log.info("Removing Node Manager for Node " + nodeInfo.getUid() + " Reason : " + reason);

		try
		{
			if(cmdSocket != null)
			{
				cmdSocket.close();
				log.info("Node " + nodeInfo.getUid() + " Command socket closed");
			}

		}
		catch(IOException e)
		{
			log.error("Node " + nodeInfo.getUid() + " Command socket already closed");
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

	public boolean hasFreeSlot()
	{
		int tActive = 0;

		if(isActive())
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
	public int addSim(String scenarioText, int initialStepRate, RemoteSimulationMapping mapping)
	{
		nodeLock.acquireUninterruptibly();

		log.info("Node " + nodeInfo.getUid() + " AddSim");

		try
		{
			// addSimMsgBoxVarLock.acquireUninterruptibly();

			// Shared variable
			addSimId = -1;

			// Create and Send add Sim Req
			sendCMDMessage(new AddSimReq(scenarioText, initialStepRate).toBytes());

			// addSimMsgBoxVarLock.release();

			// Wait until we are released (by timer or receive thread)
			addSimWait.acquireUninterruptibly();

			// addSimMsgBoxVarLock.acquireUninterruptibly();

			if(addSimId == -1)
			{
				// addSimMsgBoxVarLock.release();
				nodeLock.release();

				return -1;
			}
			else
			{

				mapping.setRemoteSimId(addSimId);

				remoteSimulationMap.put(addSimId, mapping);

				activeSimsLock.acquireUninterruptibly();

				activeSims++;

				activeSimsLock.release();

				// addSimMsgBoxVarLock.release();
				nodeLock.release();

				return addSimId;
			}

		}
		catch(IOException e)
		{
			nodeLock.release();

			// Connection is gone add sim failed
			log.error("Node " + nodeInfo.getUid() + " Error in add Sim");

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
		// NA - Finished Simulation are auto-removed, but the remote node will
		// still have the stats in the cache
		// - we assume calling this method means you do not want stats or the
		// simulation.

		getStatExporter(remoteSimId, "", ExportFormat.XML);

	}

	public void startSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();

		try
		{
			sendCMDMessage(new StartSimCMD(remoteSimId).toBytes());
		}
		catch(IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeInfo.getUid() + " Error in Start Sim");

		}

		nodeLock.release();
	}

	/**
	 * Method returns a stat exporter for the request simulation id - Method
	 * Blocks.
	 * 
	 * @param remoteSimId
	 * @param fileNameSuffix
	 * @param format
	 * @return
	 */
	public StatExporter getStatExporter(int remoteSimId, String fileNameSuffix, ExportFormat format)
	{
		StatExporter returnedExporter = null;

		log.info("Requesting SimStats for remote sim : " + remoteSimId);

		// create a new request
		NodeManagerStatRequestMapping request = new NodeManagerStatRequestMapping(format, fileNameSuffix);

		// Add the request to the map
		statRequests.put(remoteSimId, request);

		try
		{
			// Send the request
			sendCMDMessage(new SimulationStatsRequest(remoteSimId, format).toBytes());

			request.waitOnReply();

			returnedExporter = request.getExporter();

			// Mapping no longer needed
			remoteSimulationMap.remove(remoteSimId);

		}
		catch(IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeInfo.getUid() + " Error in get Stats Exporter - SimId " + remoteSimId);
		}

		return returnedExporter;
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
		try
		{
			sendCMDMessage(new NodeStatsRequest(id).toBytes());
		}
		catch(IOException e)
		{
			log.error("Fail to send Node stats request for Node " + nodeInfo.getUid());
		}
	}

}
