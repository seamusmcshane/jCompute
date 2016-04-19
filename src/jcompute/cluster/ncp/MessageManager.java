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
import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
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
import jcompute.cluster.ncp.message.control.NodeOrderlyShutdownReply;
import jcompute.cluster.ncp.message.control.NodeOrderlyShutdownRequest;
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
import jcompute.stats.StatExporter.ExportFormat;

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
	
	private final int RX_QUEUE_SIZE = 32;
	private final int MAX_RX_CYCLES = 16;
	
	private final int TX_QUEUE_SIZE = 32;
	
	// RX Message List
	private ArrayDeque<NCPMessage> rxMessages;
	private Semaphore rxLock = new Semaphore(1, false);
	
	private AtomicBoolean waitingRX = new AtomicBoolean(false);
	private Semaphore rxWait = new Semaphore(0, false);
	
	// TX Pending Message List
	private ArrayDeque<byte[]> txPendingList;
	private Semaphore txLock = new Semaphore(1, false);
	
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
			
			// RX Message List
			rxMessages = new ArrayDeque<NCPMessage>(RX_QUEUE_SIZE);
			
			// TX Pending Message List to allowing TCP message concatenation
			txPendingList = new ArrayDeque<byte[]>(TX_QUEUE_SIZE);
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
		
		Thread ncpMessageManager = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				log.info("Started");
				
				final int TX_FREQUENCY = txFreq;
				
				try
				{
					long currentTXTime = System.currentTimeMillis();
					long lastTXTime = System.currentTimeMillis();
					
					while(connected)
					{
						boolean outStandingRx = rxDataEnqueue();
						
						// The connection has no pending data, no we had no test replies and we are over the NCP.TimeOut window length.
						if(isNCPTimeout())
						{
							shutdown(timeout.toString() + " : " + (System.currentTimeMillis() - lastTestMessageTime));
						}
						
						// Do a test if there is no data - initiate an activity test sequence if we have not performed a test recently
						if((recvConnectionTestSeqNum == sentConnectionTestSeqNum) && needsActivityTest())
						{
							sendActivityTestMessage();
						}
						
						currentTXTime = System.currentTimeMillis();
						boolean txNeeded = ((currentTXTime - lastTXTime) >= TX_FREQUENCY);
						
						if(txNeeded)
						{
							txPendingData();
							
							lastTXTime = System.currentTimeMillis();
						}
						
						// Only sleep if RX is not busy.
						if(!outStandingRx)
						{
							Thread.sleep(1);
						}
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
		ncpMessageManager.setName("NCP Queue Processor");
		ncpMessageManager.start();
		
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
	
	public void sendRegistrationReqNack(int reason, int value)
	{
		txDataEnqueue(new RegistrationReqNack(reason, value).toBytes());
	}
	
	public void sendConfigurationRequest(int benchmark, int objects, int iterations, int warmupIterations, int runs)
	{
		txDataEnqueue(new ConfigurationRequest(benchmark, objects, iterations, warmupIterations, runs).toBytes());
	}
	
	public void sendConfigurationAck(NodeInfo nodeInfo)
	{
		txDataEnqueue(new ConfigurationAck(nodeInfo).toBytes());
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP ASync Replies
	 *****************************************************************************************************/
	
	// Reply to an AddSimReq
	public void sendAddSimReply(AddSimReq req, int simId)
	{
		if(req == null)
		{
			return;
			
		}
		
		txDataEnqueue(new AddSimReply(req.getRequestId(), simId).toBytes());
	}
	
	// Reply to a NodeStatsRequest
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
	
	// Reply to a SimulationStatsRequest
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
	
	// Send a sendSimulationStateChanged
	public void sendSimulationStateChanged(SimulationStateChangedEvent e)
	{
		if(e == null)
		{
			return;
		}
		
		txDataEnqueue(new SimulationStateChanged(e).toBytes());
	}
	
	// Send a sendSimulationStatChanged
	public void sendSimulationStatChanged(SimulationStatChangedEvent e)
	{
		if(e == null)
		{
			return;
		}
		
		txDataEnqueue(new SimulationStatChanged(e).toBytes());
	}
	
	/**
	 * Reply to a NodeOrderlyShutdownRequest
	 * Note sending this message assumes the socket is about to close and will cause all pending data to be sent ensure the message is sent without delay.
	 * This message exists so the receiver can also close their side with out waiting on a timeout.
	 * 
	 * @throws IOException
	 */
	public void sendNodeOrderlyShutdownReply() throws IOException
	{
		txDataEnqueue(new NodeOrderlyShutdownReply().toBytes());
		
		txPendingData();
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP ASync Requests (Processing)
	 *****************************************************************************************************/
	
	public void sendAddSimulationRequest(long requestId, String scenarioText)
	{
		if(scenarioText == null)
		{
			return;
		}
		
		txDataEnqueue(new AddSimReq(requestId, scenarioText).toBytes());
	}
	
	public void sendSimulationStatisticsRequest(int simId, ExportFormat format)
	{
		if(format == null)
		{
			return;
		}
		
		txDataEnqueue(new SimulationStatsRequest(simId, format).toBytes());
	}
	
	public void sendNodeStatisticsRequest(int sequenceNum)
	{
		txDataEnqueue(new NodeStatsRequest(sequenceNum).toBytes());
	}
	
	/**
	 * Node sending this message is a request that the node finishes processing and does not reconnect.
	 * There may or may not be a NodeOrderlyShutdownReply to confirm a pending shutdown.
	 */
	public void sendNodeOrderlyShutdownRequest()
	{
		txDataEnqueue(new NodeOrderlyShutdownRequest().toBytes());
	}
	
	/*
	 * ***************************************************************************************************
	 * NCP Getter
	 *****************************************************************************************************/
	
	public NCPMessage getMessage(boolean wait)
	{
		rxLock.acquireUninterruptibly();
		
		NCPMessage message = rxMessages.pollFirst();
		
		rxLock.release();
		
		// Are we waiting.
		if(wait && message == null)
		{
			// We are waiting
			waitingRX.set(true);
			
			// Wait for a message.
			rxWait.acquireUninterruptibly();
			
			// We are not waiting
			waitingRX.set(false);
			
			// Got a message.
			rxLock.acquireUninterruptibly();
			message = rxMessages.pollFirst();
			rxLock.release();
		}
		
		return message;
	}
	
	/*
	 * ***************************************************************************************************
	 * RX Transfer
	 *****************************************************************************************************/
	
	private boolean rxDataEnqueue() throws IOException
	{
		// Connection lost
		if(input == null)
		{
			return false;
		}
		
		// Any pending data in the TCP socket?
		if(input.available() == 0)
		{
			return false;
		}
		
		try
		{
			// We transparently receive ActivityTest messages here and reply immediately.
			
			// Begin the cycle count.
			int cycle = 0;
			
			while(true)
			{
				// TVL
				int type = -1;
				int len = -1;
				ByteBuffer data = null;
				
				// Switch the socket the normal timeout.
				socket.setSoTimeout(timeout.normalTimeout);
				
				try
				{
					// Wait for data or timeout
					type = input.readInt();
				}
				catch(SocketTimeoutException e)
				{
					
					// Timeout waiting for type field.
					// This is OK return as there is no data.
					
					return false;
				}
				
				// Switch the socket the error timeout.
				socket.setSoTimeout(timeout.errorTimeout);
				
				// To get here we must have read a type field - there is a message in the buffer.
				// A timeout here is will cause cause the connection to close.
				len = input.readInt();					// TODO validate lengths vs the type
				
				data = readBytesToByteBuffer(len);		// Get the data
				
				// The abstract message
				NCPMessage message = null;
				
				// Marker to intercept test messages
				boolean testMessage = false;
				boolean ourMessage = false;
				
				// Determine how to parse the message.
				switch(type)
				{
					// Registration
					case NCP.RegReq:
					{
						message = new RegistrationRequest(data);
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
					case NCP.NodeOrderlyShutdownRequest:
					{
						message = new NodeOrderlyShutdownRequest();
					}
					break;
					case NCP.NodeOrderlyShutdownReply:
					{
						message = new NodeOrderlyShutdownReply();
					}
					break;
					case NCP.ActivityTestRequest:
					{
						ActivityTestRequest req = new ActivityTestRequest(data);
						txDataEnqueue(new ActivityTestReply(req).toBytes());
						
						testMessage = true;
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
							// An error has occurred.
							
							shutdown("ConnectionTest Sequence not in sync " + sentConnectionTestSeqNum + " " + recvConnectionTestSeqNum + " " + reqSeqNum);
							
							return false;
						}
						
						ourMessage = true;
						testMessage = true;
					}
					break;
				}
				
				// Is the test message marker set.
				if(!testMessage)
				{
					rxLock.acquireUninterruptibly();
					
					// The is a normal NCP message - enqueue.
					rxMessages.add(message);
					
					rxLock.release();
					
					if(waitingRX.get())
					{
						// Release waiting for a message.
						rxWait.release();
					}
				}
				else
				{
					// This was a test message.
					// Was it ours
					if(ourMessage)
					{
						// Reset NCP our Timeout as we got a reply.
						resetNCPTimeout();
						
						// Reset flag
						ourMessage = false;
					}
					
					// Reset flag
					testMessage = false;
				}
				
				// Increase our cycle count and continue the loop
				cycle++;
				
				// There are no more messages
				if(input.available() == 0)
				{
					return false;
				}
				
				// We may have more message but we have to allow other processing
				if(cycle == MAX_RX_CYCLES)
				{
					return true;
				}
			}
		}
		catch(SocketTimeoutException e)
		{
			// To get here we have timed out waiting on len or data
			// This is not recoverable as the stream is out of sync.
			shutdown(e.getMessage());
		}
		catch(IOException e)
		{
			shutdown(e.getMessage());
		}
		
		// An error has occurred - no messages
		return false;
	}
	
	/*
	 * ***************************************************************************************************
	 * TX Transfer
	 *****************************************************************************************************/
	
	// Enqueue Messages to be sent
	private void txDataEnqueue(byte[] bytes)
	{
		txLock.acquireUninterruptibly();
		
		txPendingList.add(bytes);
		
		// Byte count pending
		pendingByteCount += bytes.length;
		
		txLock.release();
	}
	
	// Send Pending Messages
	private void txPendingData() throws IOException
	{
		txLock.acquireUninterruptibly();
		
		if(pendingByteCount == 0)
		{
			txLock.release();
			
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
		
		txLock.release();
	}
	
	/*
	 * ***************************************************************************************************
	 * RX Transfer
	 *****************************************************************************************************/
	
	// TODO check lengths
	// Reads the specified length of bytes from the socket and returns them in a byte buffer.
	private ByteBuffer readBytesToByteBuffer(int length) throws SocketTimeoutException, IOException
	{
		byte[] backingArray = null;
		ByteBuffer data = null;
		
		// Allocate here to avoid duplication of allocation code
		if(length > 0)
		{
			// Destination
			backingArray = new byte[length];
			
			// Block until whole message is complete then copy data from the socket
			input.readFully(backingArray, 0, length);
			
			// Wrap the backing array
			data = ByteBuffer.wrap(backingArray);
			
			// Record RXBytes + 8 for type and length fields already read
			bytesRX.add(backingArray.length + 8);
			
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
		
		// Check no one is waiting in the getMessageMethod.
		if(waitingRX.get())
		{
			// Release waiting for a message.
			rxWait.release();
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
		return timeout.hasTimeoutError((int) (System.currentTimeMillis() - lastTestMessageTime));
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
		
		// Keep socket timeout in sync - default to error timeout.
		socket.setSoTimeout(timeout.errorTimeout);
	}
}
