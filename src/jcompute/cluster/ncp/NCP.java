
package jcompute.cluster.ncp;

// jCompute Node Communication Protocol (NCP)

public class NCP
{
	public enum ProtocolMode
	{
		CONTROLLER("Controller"), NODE("Node");
		
		final String name;
		
		private ProtocolMode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	public enum ProtocolState
	{
		CON("Connect"), REG("Registering"), REG_ACK("Registration Acknowledged"), CONF("Configuration"), RDY("Ready"), DIS("Disconnect");
		
		private final String name;
		
		private ProtocolState(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	public enum DisconnectReason
	{
		NCPNack("NCP Registration negative acknowledgement"), NullNCPRecv("Null NCP message received"), InvRegSeq("Invalid registration sequence"),
		ReadyStateTimeout("Ready state timeout"), UnknownNCPType("Received invalid or unknown NCP message"), NCPNotReady("NCP not ready"),
		TCPDisconnected("TCP disconnected"), RegNotCompleted("Registration did not complete"), NCPVersionMismatch("Protocol version mismatch"),
		RegAckUidMismatch("Registration acknowledgement uid mismatch");
		
		final String reason;
		
		private DisconnectReason(String reason)
		{
			this.reason = reason + " switching to protocol state " + ProtocolState.DIS.toString();
		}
		
		public String appendfromStateInfo(ProtocolState ncpState)
		{
			return reason + " from state " + ncpState.toString();
		}
		
		public String appendfromStateInfoAndLastNCPType(ProtocolState ncpState, int lastNCPType)
		{
			return reason + " from state " + ncpState.toString() + " - last NCP Message  received was type " + lastNCPType;
		}
		
		@Override
		public String toString()
		{
			return reason;
		}
	}
	
	// Protocol version - prevent mismatched nodes
	public static final int NCP_PROTOCOL_VERSION = 8;
	
	// NCP Message Type Definitions
	
	public static final int HEADER_SIZE = 8;	 // Type + Len Fields (int)
	
	// Invalid Frame
	public static final int INVALID = 0;
	
	// Registration
	public static final int RegReq = 01;				// Registration Request
	public static final int RegAck = 02;				// Registration acknowledgement - reply with a uid for identification
	public static final int RegNack = 03;			// ComputeNode Registration NACK Frame - ComputeNodeManager denied registration
	
	// Configuration
	public static final int ConfReq = 11;			// ComputeNode configuration request - requests configuration information such as max simulation slots
	public static final int ConfAck = 12;			// ComputeNode configuration - reply with configuration
	
	// Status Request
	// public static final int StatusReq = 21; // ComputeNode status request - Requests status including progress/details of all simulations
	// public static final int StatusAck = 22; // ComputeNode status - Reply with the status details
	
	// Simulations
	public static final int AddSimReq = 31;			// ComputeNode Add Simulation Request - request from controller to add a sim (with config)
	public static final int AddSimReply = 32;		// ComputeNode Add Simulation Acknowledgement - reply with a sim slot id or -1 denied
	
	// Simulation Statistics Retrieval
	public static final int SimStatsReq = 41;		// ComputeNode Statistics request from controller to retrieve the finished simulation statistics
	public static final int SimStatsReply = 42;		// ComputeNode Statistics - reply with statistics.
	
	// Notifications
	public static final int SimStateNoti = 51;		// Notification of a simulation state change
	public static final int SimStatNoti = 52;		// Notification of a simulation stat change
	
	// Status Request
	public static final int NodeStatsRequest = 61;					// Request for Statistic Information related to the node
	public static final int NodeStatsReply = 62;					// Reply with ComputeNode Statistics
	
	// Shutdown request
	public static final int NodeOrderlyShutdownRequest = 71;		// Request that the ComputeNode does an orderly shutdown and not attempt to reconnect.
	public static final int NodeOrderlyShutdownReply = 72;		// A Courteous reply that the ComputeNode is now shutting down as requested.
	
	// NCP connection activity test.
	public static final int ActivityTestRequest = 80737871;		// Connection Test
	public static final int ActivityTestReply = 80797871;			// Connection Test Reply
	
	// The standard NCP port
	public static final int StandardServerPort = 10000;			// ControlNode Listening Port
	
	private static final int ReadyStateTimeOut = 120000;			// Max time to wait for a node to enter ready state in seconds.
	
	public static final int InactivityTimeOut = 15000;				// Max time milliseconds to wait for a node to reply before closing connection
	public static final int ActivityTestFreq = 1000;				// Activity test frequency in milliseconds
	
	// Registration Nack Reasons
	public static final int ProtocolVersionMismatch = 01;			// Protocol Versions do not match - value contains local node version.
	
	// Timeouts in millis
	public enum Timeout
	{
		ReadyState(ReadyStateTimeOut, "NCP Ready State Timeout"), Inactivity(InactivityTimeOut, "NCP Inactivity Timeout");
		
		private final String name;
		public final int value;
		
		private Timeout(int value, String name)
		{
			this.value = value;
			this.name = name;
		}
		
		public boolean isTimedout(int value)
		{
			return(value > this.value);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
