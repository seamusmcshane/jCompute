package alifeSim.Network.SMNMP;

/*
 * Simulation Manager Network Protocol
 */

public class Protocol
{
	/* Invalid Frame */
    public static final byte SMNP_INVALID		= 0;	/* Included for Error Checking - A zero type should be ignore by client and server */
    
    /* Registration */
    public static final byte SMNP_MNRegReq		= 01;	/* Manager Node Registration Frame - register with a controller */
    public static final byte SMNP_MNReqAck		= 02;	/* Manager Node Registration ACK Frame - reply with a uid for Manager Node identification */
    public static final byte SMNP_MNRegNack		= 03;	/* Manager Node Registration NACK Frame - controller denied registration*/
    
    /* Un-Registration */
    public static final byte SMNP_MNUnReqReq	= 04;	/* Manager Node UnRegistration Frame */
    public static final byte SMNP_MNUnRegAck	= 05;	/* Manager Node UnRegistration ACK Frame */
    public static final byte SMNP_MNUnReqNack	= 06;	/* Manager Node UnRegistration NACK Frame */
    
    /* Node Configuration/Status */
    public static final byte SMNP_MNCReq	= 11;	/* Manager Node configuration request - gets details such as max simulation slots */
    public static final byte SMNP_MNConf	= 12;	/* Manager Node configuration - reply with configuration */
    public static final byte SMNP_MNSReq	= 21;	/* Manager Node status request - Requests status including progress/details of all simulations */
    public static final byte SMNP_MNStatus	= 22;	/* Manager Node status - Reply with the status details */
    
    /* Simulations */
    public static final byte SMNP_MNASReq	= 31;	/* Manager Node Add Simulation Request - request from controller to add a sim (with config) */
    public static final byte SMNP_MNASAck	= 32;	/* Manager Node Add Simulation Acknowledgement	- reply with a sim slot id */
    public static final byte SMNP_MNASNack	= 33;	/* Manager Node Add Simulation Non-Acknowledgement	- request denied */
    public static final byte SMNP_MNRSReq	= 34;	/* Manager Node Remove Simulation Request - request from controller remove a simulation */
    public static final byte SMNP_MNRSAck	= 35;	/* Manager Node Remove Simulation Acknowledgement	- reply confirming removal */
    
    /* Simulation Statistics Retrieval */
    public static final byte SMNP_MNStatsReq 		= 41;	/* Manager Node Statistics Req - request from controller to retrieve the finished simulation statistics */
    public static final byte SMNP_MNStats			= 42;	/* Manager Node Statistics - reply with statistics (can be multi-frame) */
    public static final byte SMNP_MNStatsRecvAck	= 42;	/* Control Node Statistics received acknowledgement (multi-frame ack) */
    
    /* Notifications */
    public static final byte SMNP_MNSimFinNot	= 51;	/* Notification containing the details of the finished simulation */
    
    /* Heart Beat - Connection Timeout */
    public static final byte SMNP_KeepAlive		= 126;   /* Manager Node must sent keep alives, to the controller. Else will be assumed offline by controller and will be forcibly unregistered. */
    public static final byte SMNP_KeepAliveAck	= 127;   /* Controller will Ack a keep alive to notify the Manager Node. */
    
    /* Heart Beat timer values - used by server to timeout clients and for clients to determine if they have lost connection with the server */
    public static final int KeepAliveTime		= 5000;		/* Send a keep alive every n milliseconds seconds */
    public static final int KeepAliveTimeout	= 20000;    /* Time out after n milliseconds seconds (Node/Server time out if no SMNP_KeepAlive/SMNP_KeepAliveAck received with in this timeout period) */
              
    /* Protocol Sizes - Note 16bit Uni-code is expected in all Strings */
    public static final int DataLengthFieldSize	= 2;     // 2 bytes
    public static final int MaxFrameSize		= 65504; // 256*256-32.
    public static final int DataLengthMaxValue	= MaxFrameSize-DataLengthFieldSize;

    /* Limits */
    public static final byte MaxSimManNodes		= 16;
    
    // The standard ports
    public static final int StandardNodePort 	= 1001;
    public static final int StandardControlPort = 1000;
}
