package jCompute.Simulation.SimulationManager.Network.Manager;

import jCompute.JComputeEventBus;
import jCompute.Simulation.SimulationState.SimState;
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
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReply;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.SimulationStatsReply;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
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
	private Semaphore activeSimsLock = new Semaphore(1, false);

	
	// This node cmd socket
	private final Socket cmdSocket;
	private Socket transferSocket;	

	// Output Stream
	private DataOutputStream cmdOutput;
	private DataInputStream cmdInput;
	private Semaphore cmdTxLock = new Semaphore(1, false);
	private Thread cmdRecieveThread;

	// Transfer Streams
	private DataOutputStream transferOutput;
	private DataInputStream transferInput;
	private Semaphore transTxLock = new Semaphore(1, true);
	private Thread transferRecieveThread;

	private ProtocolState nodeState;

	// Counter for NSMCP state machine
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
	private ConcurrentHashMap<Integer,NodeManagerStatRequestMapping> statRequests;

	public NodeManager(int uid, Socket cmdSocket) throws IOException
	{
		nodeConfig = new NodeConfiguration();

		remoteSimulationMap = new ConcurrentHashMap<Integer, RemoteSimulationMapping>(8);

		statRequests = new ConcurrentHashMap<Integer,NodeManagerStatRequestMapping>(8);
		
		NSMCPReadyTimeOut = 0;

		log.info("New Node Manager " + uid);

		// Internal Connection ID
		nodeConfig.setUid(uid);

		// A connected socket
		this.cmdSocket = cmdSocket;

		// Cmd Output Stream
		cmdOutput = new DataOutputStream(new BufferedOutputStream(cmdSocket.getOutputStream()));
		
		// Cmd Input Stream
		cmdInput = new DataInputStream(new BufferedInputStream(cmdSocket.getInputStream()));

		nodeState = ProtocolState.NEW;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					handleRegistration();
					
					createCMDRecieveThread();
					
					createTransferRecieveThread();
				}
				catch(IOException e)
				{
					active = false;
				}
			}
		}).start();
	}

	private void handleRegistration() throws IOException
	{
		log.info("Awaiting Registration");

		
		boolean finished = false;
		
		int type = -1;
		int len = -1;
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		while (!finished)
		{
			// Detect Frame
			type = cmdInput.readInt();
			len = cmdInput.readInt();

			// Allocate here to avoid duplication of allocation code
			if(len > 0 )
			{
				// Destination
				backingArray = new byte[len];
				
				// Copy from the socket
				cmdInput.readFully(backingArray, 0, len);
				
				// Wrap the backingArray
				data = ByteBuffer.wrap(backingArray);
				
				log.debug("Type " + type+ " len " + len);
			}
			
			switch (type)
			{
				case NSMCP.RegReq:
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
						sendCMDMessage(new RegistrationReqAck(nodeConfig.getUid()).toBytes());
	
						log.info("Sent Registration Ack");
	
						nodeState = ProtocolState.REG;
					}
					else
					{
						log.error("Registration Request for node " + nodeConfig.getUid()
								+ " not valid in state " + nodeState.toString());
						
						// invalid sequence
						nodeState = ProtocolState.END;
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
						RegistrationReqAck reqAck = new RegistrationReqAck(data);

						int ruid = reqAck.getUid();

						// Check the node is sane (UID should be
						// identical to the one we sent)
						if (nodeConfig.getUid() == ruid)
						{
							log.info("Node registration ok");
							finished = doTransferSocketSetup();

						}
						else
						{
							log.error("Node registration not ok " + ruid);

							nodeState = ProtocolState.END;
						}

					}
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
				// Test Frame or Garbage
				case NSMCP.INVALID :
				default :
					log.error("Recieved Invalid Frame");
					nodeState = ProtocolState.END;
					finished = true;
					log.error("Error Type " + type + " len " + len);
					
				break;
			}
		}
		

		
	}
	
	private boolean doTransferSocketSetup()  throws IOException
	{
		log.info("Setting up transfer socket");
		boolean finished = false;

		// Create and connect the transfer socket
		transferSocket = new Socket();
		transferSocket.connect(new InetSocketAddress(cmdSocket.getInetAddress(), NSMCP.NodeTransferPort), 1000);
		
		transferSocket.setSendBufferSize(32768);
		transferSocket.setReceiveBufferSize(1048576);

		transferInput = new DataInputStream(new BufferedInputStream(transferSocket.getInputStream()));
		transferOutput = new DataOutputStream(new BufferedOutputStream(transferSocket.getOutputStream()));
		
		log.info("Socket Setup");
		
		log.info("Now requesting node configuration and weighting");
		sendTransferMessage(new ConfigurationRequest(1).toBytes());
		
		int type = -1;
		int len = -1;
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		while (!finished)
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
				/*
				 * Remove node is about to finish registration. We
				 * are waiting on the node configuration.
				 */
				case NSMCP.ConfAck :

					if (nodeState == ProtocolState.REG)
					{
						log.info("Recieved Conf Ack");

						ConfigurationAck reqAck = new ConfigurationAck(data);

						nodeConfig.setMaxSims(reqAck.getMaxSims());
						nodeConfig.setWeighting(reqAck.getWeighting());
						
						
						log.info("Node " + nodeConfig.getUid() + " Max Sims  : " + nodeConfig.getMaxSims());
						log.info("Node " + nodeConfig.getUid() + " Weighting : " + nodeConfig.getWeighting());
						
						nodeState = ProtocolState.READY;
						finished = true;

					}
					else
					{
						log.error("ConfAck for node " + nodeConfig.getUid() + " not valid in state "
								+ nodeState.toString());
					}

				break;
				// Test Frame or Garbage
				case NSMCP.INVALID :
				default :
					log.error("Recieved Invalid Frame");
					nodeState = ProtocolState.END;
					
					log.error("Error Type " + type + " len " + len);
					finished = true;
				break;
			}
		}
		
		return finished;
	}
	
	private void createTransferRecieveThread()
	{
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
							case NSMCP.SimStats :

								if (nodeState == ProtocolState.READY)
								{
									log.info("Recieved Sim Stats");

									// Read this here as we need it for the stat request mapping lookup
									int simId = data.getInt();
									
									// Remove Request Mapping
									NodeManagerStatRequestMapping request = statRequests.remove(simId);
																		
									SimulationStatsReply statsReply = new SimulationStatsReply(simId,data,request.getFormat(),request.getFileNameSuffix());
									
									request.setStatExporter(statsReply.getStatExporter());
									
									// Signal the waiting thread.
									request.signalReply();
								}
								else
								{
									log.error("SimStats for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}

								break;
							// Test Frame or Garbage
							case NSMCP.INVALID :
							default :
								log.error("Recieved Invalid Frame");
								nodeState = ProtocolState.END;
								
								log.error("Error Type " + type + " len " + len);
								
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
					log.warn("Node " + nodeConfig.getUid() + " Transfer Recieve Thread exited");
					// Exit // Do Node Shutdown

					active = false;

					nodeState = ProtocolState.END;

					// Explicit release of all semaphores
					addSimWait.release();
					remSimWait.release();
				}
				
			}
			
		});
		
		transferRecieveThread.setName("Node Manager " + nodeConfig.getUid() + " Transfer Recieve");

		// Start Processing
		transferRecieveThread.start();		
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

					while (active)
					{
						// Detect Frame
						type = cmdInput.readInt();
						len = cmdInput.readInt();

						// Allocate here to avoid duplication of allocation code
						if(len > 0 )
						{
							// Destination
							backingArray = new byte[len];
							
							// Copy from the socket
							cmdInput.readFully(backingArray, 0, len);
							
							// Wrap the backingArray
							data = ByteBuffer.wrap(backingArray);
							
							log.debug("Type " + type+ " len " + len);
						}

						switch (type)
						{
							case NSMCP.AddSimReply :

								if (nodeState == ProtocolState.READY)
								{
									AddSimReply addSimReply = new AddSimReply(data);
									addSimId = addSimReply.getSimId();

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
										
										// Remote Sim is auto-removed when finished
										activeSims--;
										
										activeSimsLock.release();

										JComputeEventBus.post(new SimulationsManagerEvent(mapping.getLocalSimId(),SimulationsManagerEventType.RemovedSim));

									}
									
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
									SimulationStatChanged statChanged = new SimulationStatChanged(data);

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
									else
									{
										log.warn("No mapping found for " + statChanged.info());

									}
								}
								else
								{
									log.error("SimStatNoti for node " + nodeConfig.getUid() + " not valid in state "
											+ nodeState.toString());
								}

								break;
							case NSMCP.RemSimAck :
								if (nodeState == ProtocolState.READY)
								{
									RemoveSimAck removeSimAck = new RemoveSimAck(data);

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
								
								log.error("Error Type " + type + " len " + len);
								
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
				}
				
			}
		});

		cmdRecieveThread.setName("Node " + nodeConfig.getUid() + " Command Recieve");

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

	private void sendTransferMessage(byte[] bytes) throws IOException
	{
		transTxLock.acquireUninterruptibly();

		transferOutput.write(bytes);
		transferOutput.flush();

		transTxLock.release();
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
			transferSocket.close();
			cmdSocket.close();
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
		int tActive=0;
		
		activeSimsLock.acquireUninterruptibly();
		
			tActive = activeSims;
			
		activeSimsLock.release();
		
		if (tActive < nodeConfig.getMaxSims())
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
			sendCMDMessage(new AddSimReq(scenarioText, initialStepRate).toBytes());

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

				activeSimsLock.acquireUninterruptibly();
				
				activeSims++;

				activeSimsLock.release();
				
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
		// NA - Finished Simulation are auto-removed, but the remote node will still have  the stats in the cache
		// - we assume calling this method means you do not want stats or the simulation.
		
		getStatExporter(remoteSimId,"",ExportFormat.XML);
		
	}

	public void startSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();

		try
		{
			sendCMDMessage(new StartSimCMD(remoteSimId).toBytes());
		}
		catch (IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeConfig.getUid() + " Error in Start Sim");

		}

		nodeLock.release();
	}

	/**
	 * Method returns a stat exporter for the request simulation id - Method Blocks.
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
		
		//nodeLock.release();
		
		try
		{
			// Send the request
			sendTransferMessage(new SimulationStatsRequest(remoteSimId, format).toBytes());
			
			request.waitOnReply();
			
			returnedExporter = request.getExporter();
			
			// Mapping no longer needed
			remoteSimulationMap.remove(remoteSimId);
			
		}
		catch (IOException e)
		{
			// Connection is gone...
			log.error("Node " + nodeConfig.getUid() + " Error in get Stats Exporter");
		}

		
		
		return returnedExporter;
	}

	public long getWeighting()
	{
		return nodeConfig.getWeighting();
	}

	public int getActiveSims()
	{
		int tActive=0;
		
		activeSimsLock.acquireUninterruptibly();
		
			tActive = activeSims;
			
		activeSimsLock.release();
		
		return tActive;
	}

}
