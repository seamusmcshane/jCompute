package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages;

// Simulation Manager Network Protocol
 

public class NSMCP
{	
	public enum ProtocolState
	{
		NEW ("New"),
		REG ("Registering"),
		READY ("Ready"),
		END ("ENDED");
		
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
	
	// Invalid Frame 
    public static final int INVALID	= 0;
    
    // Registration 
    public static final int RegReq	= 01;	// Manager Node Registration Frame - register with a controller
    public static final int RegAck	= 02;	// Manager Node Registration ACK Frame - reply with a uid for Manager Node identification 
    public static final int RegNack	= 03;	// Manager Node Registration NACK Frame - controller denied registration
    
    // Un-Registration 
    public static final int UnReqReq	= 04;	// Manager Node UnRegistration Frame 
    public static final int UnRegAck	= 05;	// Manager Node UnRegistration ACK Frame 
    public static final int UnReqNack	= 06;	// Manager Node UnRegistration NACK Frame 
    
    // Node Configuration
    public static final int ConfReq	= 11;	// Manager Node configuration request - gets details such as max simulation slots 
    public static final int ConfAck	= 12;	// Manager Node configuration - reply with configuration 
    
    // Node Status Request
    public static final int StatusReq	= 21;	// Manager Node status request - Requests status including progress/details of all simulations 
    public static final int StatusAck	= 22;	// Manager Node status - Reply with the status details 
    
    // Simulations 
    public static final int AddSimReq	= 31;	// Manager Node Add Simulation Request - request from controller to add a sim (with config) 
    public static final int AddSimReply	= 32;	// Manager Node Add Simulation Acknowledgement	- reply with a sim slot id or -1 denied
    public static final int RemSimReq	= 33;	// Manager Node Remove Simulation Request - request from controller remove a simulation 
    public static final int RemSimAck	= 34;	// Manager Node Remove Simulation Acknowledgement	- reply confirming removal 
    public static final int StartSimCMD	= 35;	// Manager Node Start Simulation Request (Command not Request)
    
    // Simulation Statistics Retrieval 
    public static final int SimStatsReq 	= 41;	// Manager Node Statistics Req - request from controller to retrieve the finished simulation statistics 
    public static final int SimStats		= 42;	// Manager Node Statistics - reply with statistics.
    
    // Notifications 
    public static final int SimStateNoti	= 51;	// Notification of a simulation state change
    public static final int SimStatNoti		= 52;	// Notification of a simulation stat change
    
    // Heart Beat - Connection Timeout 
    public static final int KeepAlive		= 126;   // Manager Node must sent keep alives, to the controller. Else will be assumed offline by controller and will be forcibly unregistered. 
    public static final int KeepAliveAck	= 127;   // Controller will Ack a keep alive to notify the Manager Node. 
    
    // Protocol Sizes - Note 16bit Uni-code is expected in all Strings 
    public static final int DataLengthFieldSize	= 4;     // 4 bytes
    public static final int MaxFrameSize		= (256^4)-1; // 256*256-32.
    public static final int DataLengthMaxValue	= MaxFrameSize-DataLengthFieldSize;

    // Arbitrary Node Limits 
    public static final int MaxSimManNodes		= 16;
    
    // The standard ports
    public static final int StandardServerPort = 10000;
    
    public static final int ReadyStateTimeOut = 20;	// Max time to wait for a node to enter ready state 20 Seconds 
}
