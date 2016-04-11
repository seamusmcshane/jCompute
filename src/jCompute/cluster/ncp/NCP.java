
package jCompute.cluster.ncp;

// JCompute NodeCommunicationProtocol (NCP)

public class NCP
{	
	public enum ProtocolState
	{
		CON ("Connect"),
		REG ("Registering"),
		RDY ("Ready"),
		DIS ("Disconnect");
		
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
	
	// Protocol version - prevent mismatched nodes
	public static final int NCP_PROTOCOL_VERSION = 5;
	
	public static final int HEADER_SIZE = 8; // Type + Len Fields (int)
	
	// Invalid Frame 
    public static final int INVALID	= 0;
    
    // Registration 
    public static final int RegReq	= 01;	// ComputeNode Registration Frame - register with a ControlNode
    public static final int RegAck	= 02;	// ComputeNode Registration ACK Frame - reply with a uid from ComputeNodeManager for ComputeNode identification 
    public static final int RegNack	= 03;	// ComputeNode Registration NACK Frame - ComputeNodeManager denied registration
    public static final int UnReq	= 04;	// ComputeNode UnRegistration Frame - the ComputeNode wants to disconnect. (Not implemented)
    
    // ComputeNode Configuration
    public static final int ConfReq	= 11;	// ComputeNode configuration request - requests configuration information such as max simulation slots 
    public static final int ConfAck	= 12;	// ComputeNode configuration - reply with configuration
    
    // ComputeNode Status Request
    public static final int StatusReq	= 21;	// ComputeNode status request - Requests status including progress/details of all simulations 
    public static final int StatusAck	= 22;	// ComputeNode status - Reply with the status details 
    
    // Simulations 
    public static final int AddSimReq	= 31;	// ComputeNode Add Simulation Request - request from controller to add a sim (with config) 
    public static final int AddSimReply	= 32;	// ComputeNode Add Simulation Acknowledgement	- reply with a sim slot id or -1 denied
    public static final int RemSimReq	= 33;	// ComputeNode Remove Simulation Request - request from controller remove a simulation (DEFUNCT)
    public static final int RemSimAck	= 34;	// ComputeNode Remove Simulation Acknowledgement	- reply confirming removal (DEFUNCT)
    public static final int StartSimCMD	= 35;	// ComputeNode Start Simulation Request (Command not Request) (DEFUNCT)
    
    // Simulation Statistics Retrieval 
    public static final int SimStatsReq 	= 41;	// ComputeNode Statistics Req - request from controller to retrieve the finished simulation statistics 
    public static final int SimStats		= 42;	// ComputeNode Statistics - reply with statistics.
    
    // Notifications 
    public static final int SimStateNoti	= 51;	// Notification of a simulation state change
    public static final int SimStatNoti		= 52;	// Notification of a simulation stat change
    
    // ComputeNode Status Request
    public static final int NodeStatsRequest	= 61;	// Request for Statistic Information related to the node
    public static final int NodeStatsReply		= 62;	// Reply with ComputeNode Statistics
    
    public static final int NodeOrderlyShutdown	= 70;	// Request that the ComputeNode does an orderly shutdown and not attempt to reconnect.
    
    // The standard NCP port
    public static final int StandardServerPort	= 10000;	// ControlNode Listening Port
    
    public static final int ReadyStateTimeOut = 120;		// Max time to wait for a node to enter ready state in seconds.
    
    // Registration Nack Reasons
    public static final int ProtocolVersionMismatch = 01;	// Protocol Versions do not match - value contains local node version.
}
