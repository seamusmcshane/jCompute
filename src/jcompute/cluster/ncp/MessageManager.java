package jcompute.cluster.ncp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.cluster.computenode.nodedetails.NodeInfo;
import jcompute.cluster.computenode.nodedetails.NodeStatsSample;
import jcompute.cluster.ncp.NCP.Timeout;
import jcompute.cluster.ncp.message.NCPMessage;
import jcompute.cluster.ncp.message.command.AddSimReply;
import jcompute.cluster.ncp.message.command.AddSimReq;
import jcompute.cluster.ncp.message.command.SimulationStatsReply;
import jcompute.cluster.ncp.message.command.SimulationStatsRequest;
import jcompute.cluster.ncp.message.control.NodeOrderlyShutdown;
import jcompute.cluster.ncp.message.monitoring.ActivityTestReply;
import jcompute.cluster.ncp.message.monitoring.ActivityTestRequest;
import jcompute.cluster.ncp.message.monitoring.NodeStatsReply;
import jcompute.cluster.ncp.message.monitoring.NodeStatsRequest;
import jcompute.cluster.ncp.message.notification.SimulationStatChanged;
import jcompute.cluster.ncp.message.notification.SimulationStateChanged;
import jcompute.cluster.ncp.message.registration.ConfigurationAck;
import jcompute.cluster.ncp.message.registration.ConfigurationRequest;
import jcompute.cluster.ncp.message.registration.RegistrationReqAck;
import jcompute.cluster.ncp.message.registration.RegistrationReqNack;
import jcompute.cluster.ncp.message.registration.RegistrationRequest;
import jcompute.simulation.event.SimulationStatChangedEvent;
import jcompute.simulation.event.SimulationStateChangedEvent;
import jcompute.stats.StatExporter;

public class MessageManager
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(MessageManager.class);
	
	// Running / Stopped
	private boolean connected;
	private boolean started;
	
	// TCP socket
	private Socket socket;
	
	// I/O Streams
	private DataOutputStream output;
	private DataInputStream input;
	
	// TX Pending Message List
	private ArrayList<byte[]> txPendingList;
	private int pendingByteCount;
	
	private LongAdder bytesTX;
	private LongAdder txS;
	private LongAdder bytesRX;
	private LongAdder rxS;
	
	// Connection Test
	private LongAdder totalTestSinceReset;
	private LongAdder totalResponseTime;
	
	private int recvConnectionTestSeqNum;
	private int sentConnectionTestSeqNum;
	
	// Last received test message time
	private long lastTestMessageTime;
	
	private Timeout timeout = Timeout.ReadyState;
	
	/**
	 * NCP class used to send and retrieve NCP messages.
	 *
	 * @param socket
	 * A connected TCP socket.
	 */
	public MessageManager(Socket socket)
	{
		// Underlying TCP socket
		this.socket = socket;
		
		try
		{
			// Link up Output Stream
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			// Link up Input Stream
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			
			// Manager is connected - (if no exceptions).
			connected = true;
		}
		catch(IOException e)
		{
			// Streams are gone
			connected = false;
			
			// Log why.
			log.error(e.getMessage());
		}
		
		// If connected setup internal statistics
		if(connected)
		{
			// Atomic variables as independent threads can be calling RX/TX
			bytesTX = new LongAdder();
			bytesRX = new LongAdder();
			txS = new LongAdder();
			rxS = new LongAdder();
			totalTestSinceReset = new LongAdder();
			totalResponseTime = new LongAdder();
			
			// TX Pending Message List to allowing TCP message concatenation
			txPendingList = new ArrayList<byte[]>();
			pendingByteCount = 0;
			recvConnectionTestSeqNum = 0;
			sentConnectionTestSeqNum = 0;
		}
		
		// We require to be started before processing can begin.
		started = false;
	}
	
	/**
	 * Start the manager.
	 *
	 * @param txFreq
	 * @return
	 */
	public boolean start(int txFreq)
	{
		// If not connected or already started
		if(!connected || started)
		{
			return false;
		}
		
		try
		{
			setTimeOut(NCP.Timeout.ReadyState);
		}
		catch(SocketException e)
		{
			shutdown(e.getMessage());
			
			return false;
		}
		
		started = true;
		
		Thread ncpMessageManagerTX = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				log.info("Started");
				
				try
				{
					while(connected)
					{
						txPendingData();
						Thread.sleep(txFreq);
					}
				}
				catch(InterruptedException | IOException e)
				{
					shutdown(e.getMessage());
				}
				
				log.info("Stopped");
			}
		});
		
		// Manager is the NCP sockets tx
		ncpMessageManagerTX.setName("NCP TX");
		ncpMessageManagerTX.start();
		
		return true;
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP ASync Senders (Registration)
	 *****************************************************************************************************/
	
	public void sendRegistrationRequest()
	{
		txDataEnqueue(new RegistrationRequest().toBytes());
	}
	
	public void sendRegistrationReqAck(int uid)
	{
		txDataEnqueue(new RegistrationReqAck(uid).toBytes());
	}
	
	public void sendConfigurationAck(NodeInfo nodeInfo)
	{
		txDataEnqueue(new ConfigurationAck(nodeInfo).toBytes());
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP ASync Replies
	 *****************************************************************************************************/
	
	// Reply to and AddSimReq
	public void sendAddSimReply(AddSimReq req, int simId)
	{
		if(req == null)
		{
			return;
			
		}
		
		txDataEnqueue(new AddSimReply(req.getRequestId(), simId).toBytes());
	}
	
	// Reply to and AddSimReq
	public void sendNodeStatsReply(NodeStatsRequest req, NodeStatsSample nodeStatsSample)
	{
		if((req == null) || (nodeStatsSample == null))
		{
			return;
		}
		
		// Store total values since last NodeStatsRequest then reset.
		nodeStatsSample.setBytesTX(bytesTX.sumThenReset());
		nodeStatsSample.setBytesRX(bytesRX.sumThenReset());
		nodeStatsSample.setTXS(txS.sumThenReset());
		nodeStatsSample.setRXS(rxS.sumThenReset());
		
		long time = totalResponseTime.sumThenReset();
		long test = totalTestSinceReset.sumThenReset();
		
		// Check for a div0
		long avgResponseTime = (test > 0) ? (time / test) : -1;
		
		nodeStatsSample.setAvgRTT(avgResponseTime);
		
		txDataEnqueue(new NodeStatsReply(req.getSequenceNum(), nodeStatsSample).toBytes());
	}
	
	public void sendSimulationStatsReply(SimulationStatsRequest req, StatExporter exporter)
	{
		if((req == null) || (exporter == null))
		{
			return;
		}
		
		txDataEnqueue(new SimulationStatsReply(req.getSimId(), exporter).toBytes());
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP ASync Notifications
	 *****************************************************************************************************/
	
	public void sendSimulationStateChanged(SimulationStateChangedEvent e)
	{
		if(e == null)
		{
			return;
		}
		
		txDataEnqueue(new SimulationStateChanged(e).toBytes());
	}
	
	public void sendSimulationStatChanged(SimulationStatChangedEvent e)
	{
		if(e == null)
		{
			return;
		}
		
		txDataEnqueue(new SimulationStatChanged(e).toBytes());
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP Getter
	 *****************************************************************************************************/
	
	public NCPMessage getMessage(boolean wait)
	{
		// Connection lost
		if(input == null)
		{
			return null;
		}
		
		int type = -1;
		int len = -1;
		ByteBuffer data = null;
		
		try
		{
			// We transparently receive ActivityTest messages here and reply.
			// But they do not go higher up, so if received we intercept and redo the requested read while keeping any wait behaviour.
			// This also limits processing of ActivityTest to the speed at which this method is called.
			while(true)
			{
				// Is there any data
				if(input.available() == 0)
				{
					// Do a test if there is no data - initiate an activity test sequence if we have not performed a test recently
					if((recvConnectionTestSeqNum == sentConnectionTestSeqNum) && needsActivityTest())
					{
						sendActivityTestMessage();
					}
					
					// The connection has no pending data, no we had no test replies and we are over the NCP.TimeOut window length.
					if(isNCPTimeout())
					{
						shutdown(timeout.toString() + " : " + (System.currentTimeMillis() - lastTestMessageTime));
					}
					
					// No data but requested not to wait.
					if(!wait)
					{
						return null;
					}
				}
				
				NCPMessage message = null;
				boolean testFrame = false;
				
				// Block here until data or timeout
				type = input.readInt();
				
				// TODO validate lengths vs the type
				len = input.readInt();
				
				// Get the data
				data = readBytesToByteBuffer(len);
				
				// Determine how to parse the message.
				switch(type)
				{
					// Registration
					case NCP.RegReq:
					{
						message = new RegistrationRequest();
					}
					break;
					case NCP.RegAck:
					{
						message = new RegistrationReqAck(data);
					}
					break;
					case NCP.RegNack:
					{
						message = new RegistrationReqNack(data);
					}
					break;
					case NCP.ConfReq:
					{
						message = new ConfigurationRequest(data);
					}
					break;
					case NCP.ConfAck:
					{
						message = new ConfigurationAck(data);
					}
					break;
					// Command
					case NCP.AddSimReq:
					{
						message = new AddSimReq(data);
					}
					break;
					case NCP.AddSimReply:
					{
						message = new AddSimReply(data);
					}
					break;
					case NCP.SimStatsReq:
					{
						message = new SimulationStatsRequest(data);
					}
					break;
					case NCP.SimStatsReply:
					{
						message = new SimulationStatsReply(data);
					}
					break;
					case NCP.SimStateNoti:
					{
						message = new SimulationStateChanged(data);
					}
					break;
					case NCP.SimStatNoti:
					{
						message = new SimulationStatChanged(data);
					}
					break;
					case NCP.NodeStatsRequest:
					{
						message = new NodeStatsRequest(data);
					}
					break;
					case NCP.NodeStatsReply:
					{
						message = new NodeStatsReply(data);
					}
					break;
					case NCP.NodeOrderlyShutdown:
					{
						message = new NodeOrderlyShutdown();
					}
					break;
					case NCP.ActivityTestRequest:
					{
						log.error("NOT !!!");
						
						ActivityTestRequest req = new ActivityTestRequest(data);
						txDataEnqueue(new ActivityTestReply(req).toBytes());
						
						testFrame = true;
					}
					break;
					case NCP.ActivityTestReply:
					{
						ActivityTestReply req = new ActivityTestReply(data);
						
						int reqSeqNum = req.getSequenceNum();
						
						if(sentConnectionTestSeqNum == reqSeqNum)
						{
							recvConnectionTestSeqNum = sentConnectionTestSeqNum;
							
							totalTestSinceReset.increment();
							
							totalResponseTime.add(System.nanoTime() - req.getSentTime());
						}
						else
						{
							shutdown("ConnectionTest Sequence not in sync " + sentConnectionTestSeqNum + " " + recvConnectionTestSeqNum + " " + reqSeqNum);
						}
						
						testFrame = true;
					}
					break;
					default:
					{
						return null;
					}
				}
				
				// The socket may have intercepted an activity test message.
				if(!testFrame)
				{
					// Return the message
					return message;
				}
				
				// Reset NCP Timeout
				resetNCPTimeout();
				
				// Reset flag
				testFrame = false;
			}
		}
		catch(SocketTimeoutException e)
		{
			shutdown(e.getMessage());
		}
		catch(IOException e)
		{
			shutdown(e.getMessage());
		}
		
		// No message after exceptions
		return null;
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
		
		// Send the concatenated data in bulk
		output.write(concatenated);
		
		// Flush the data
		output.flush();
		
		// Record TXBytes
		bytesTX.add(pendingByteCount);
		
		// Record TX call
		txS.increment();
		
		// Queue Cleared
		txPendingList.clear();
		
		// Count reset
		pendingByteCount = 0;
	}
	
	/*
	 * ***************************************************************************************************
	 * RX Transfer
	 *****************************************************************************************************/
	
	// TODO check lengths
	// Reads n bytes from the socket and returns them in a byte buffer.
	private ByteBuffer readBytesToByteBuffer(int len) throws SocketTimeoutException, IOException
	{
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		// Allocate here to avoid duplication of allocation code
		if(len > 0)
		{
			// Destination
			backingArray = new byte[len];
			
			// Block until whole message is complete then copy data from the socket
			input.readFully(backingArray, 0, len);
			
			// Wrap the backing array
			data = ByteBuffer.wrap(backingArray);
			
			// Record RXBytes
			bytesRX.add(backingArray.length);
			
			// Record RX Call
			rxS.increment();
		}
		
		return data;
	}
	
	public boolean isConnected()
	{
		return connected;
	}
	
	public void shutdown(String reason)
	{
		// Allow TX thread exit if not already
		connected = false;
		
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
		
		log.info("Shutting down : " + reason);
	}
	
	/*
	 * ***************************************************************************************************
	 * Timeout
	 *****************************************************************************************************/
	
	private void sendActivityTestMessage()
	{
		sentConnectionTestSeqNum++;
		
		txDataEnqueue(new ActivityTestRequest(sentConnectionTestSeqNum).toBytes());
	}
	
	private boolean needsActivityTest()
	{
		// If timer is not an ActivityTest timer then we do not need a test other wise check the age of the last message against the test freq min.
		return (timeout == NCP.Timeout.Inactivity) ? ((System.currentTimeMillis() - lastTestMessageTime) >= NCP.ActivityTestFreq) : false;
	}
	
	private boolean isNCPTimeout()
	{
		return timeout.isTimedout((int) (System.currentTimeMillis() - lastTestMessageTime));
	}
	
	// Reset NCP Timeout
	private void resetNCPTimeout()
	{
		lastTestMessageTime = System.currentTimeMillis();
	}
	
	public void setTimeOut(Timeout timeout) throws SocketException
	{
		this.timeout = timeout;
		
		log.info("Activated " + timeout.toString());
		
		resetNCPTimeout();
		
		// Keep socket timeout in sync
		socket.setSoTimeout(timeout.value);
	}
}
