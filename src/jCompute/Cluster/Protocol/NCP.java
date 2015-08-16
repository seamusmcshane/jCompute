package jCompute.Cluster.Protocol;

// Node Communication Protocol

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

	    public String toString()
	    {
	       return name;
	    }
	};
	
	// Protocol version - prevent mismatched nodes
	public static final int NCP_PROTOCOL_VERSION = 2;
	
	public static final int HEADER_SIZE = 8; // Type + Len Fields (int)
	
	// Invalid Frame 
    public static final int INVALID	= 0;
    
    // Registration 
    public static final int RegReq	= 01;	// Node Registration Frame - register with a controller
    public static final int RegAck	= 02;	// Node Registration ACK Frame - reply with a uid for Manager Node identification 
    public static final int RegNack	= 03;	// Node Registration NACK Frame - controller denied registration
    public static final int UnReq	= 04;	// Node UnRegistration Frame - No Reply.
    
    // Node Configuration
    public static final int ConfReq	= 11;	// Node configuration request - gets details such as max simulation slots 
    public static final int ConfAck	= 12;	// Node configuration - reply with configuration
    
    // Node Status Request
    public static final int StatusReq	= 21;	// Node status request - Requests status including progress/details of all simulations 
    public static final int StatusAck	= 22;	// Node status - Reply with the status details 
    
    // Simulations 
    public static final int AddSimReq	= 31;	// Node Add Simulation Request - request from controller to add a sim (with config) 
    public static final int AddSimReply	= 32;	// Node Add Simulation Acknowledgement	- reply with a sim slot id or -1 denied
    public static final int RemSimReq	= 33;	// Node Remove Simulation Request - request from controller remove a simulation (DEFUNCT)
    public static final int RemSimAck	= 34;	// Node Remove Simulation Acknowledgement	- reply confirming removal (DEFUNCT)
    public static final int StartSimCMD	= 35;	// Node Start Simulation Request (Command not Request)
    
    // Simulation Statistics Retrieval 
    public static final int SimStatsReq 	= 41;	// Node Statistics Req - request from controller to retrieve the finished simulation statistics 
    public static final int SimStats		= 42;	// Node Statistics - reply with statistics.
    
    // Notifications 
    public static final int SimStateNoti	= 51;	// Notification of a simulation state change
    public static final int SimStatNoti		= 52;	// Notification of a simulation stat change
    
    // Node Status Request
    public static final int NodeStatsRequest	= 61;	// Request for Statistic Information related to the node
    public static final int NodeStatsReply		= 62;	// Reply with Node Statistics
    
    public static final int NodeOrderlyShutdown	= 70;		// Request that the node does an orderly shutdown and not reconnect.
    
    // The standard port
    public static final int StandardServerPort	= 10000;	// Control Node Listening Port
    public static final int NodeTransferPort 	= 10001;	// Nodes Transfer Listening Port
    
    public static final int ReadyStateTimeOut = 120;		// Max time to wait for a node to enter ready state (120 seconds)
    
    // Registration Nack Reasons
    public static final int ProtocolVersionMismatch = 01;	// Protocol Versions do not match - value contains local node version.
    
}
