package jcompute;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.cluster.batchmanager.BatchManager;
import jcompute.cluster.computenode.ComputeNode2;
import jcompute.cluster.controlnode.ControlNodeServer;
import jcompute.gui.cluster.ClusterGUI;
import jcompute.gui.interactive.StandardGUI;
import jcompute.logging.Logging;
import jcompute.scenario.ScenarioPluginManager;
import jcompute.simulationmanager.SimulationsManager;
import jcompute.util.FileUtil;
import jcompute.util.JComputeInfo;
import jcompute.util.JVMInfo;
import jcompute.util.LookAndFeel;
import jcompute.util.OSInfo;

/***
 * jCompute common entry method.
 * Use command line help command line switch [--help/-help,help,\\help,/help] for a listing of launch parameters;
 * 
 * @author Seamus McShane
 */
public class Launcher
{
	// Log4j2 Logger
	private static Logger log;
	
	// Defaults ( option string, default value, option description
	private static final CommandLineArg defaultsList[] =
	{
		new CommandLineArg("mcs", Integer.toString(Runtime.getRuntime().availableProcessors()), "Max Concurrent Simulations (Int)"), new CommandLineArg("mode",
		"0", "Standard/ControlNodeServer/ComputeNode (0/1,2)"), new CommandLineArg("iTheme", "none", "Icon Theme Name (String)"), new CommandLineArg("bText", "1",
		"Button Text (0/1)"), new CommandLineArg("addr", "127.0.0.1", "Listening Address (InetAddr)"), new CommandLineArg("loglevel", "0",
		"Logging Level (int) Info/Error/Debug (0/1/2)"), new CommandLineArg("desc", null, "ComputeNode Description"), new CommandLineArg("jLook", "default",
		"Set JavaUI Look and Feel"), new CommandLineArg("allowMulti", "false", "Allow multiple connections from same address"), new CommandLineArg("SocketTX",
		"65536", "SocketTX Buffer Size (int)"), new CommandLineArg("SocketRX", "65536", "SocketRX Buffer Size (int)"), new CommandLineArg("TcpNoDelay", "1",
		"Configure TcpNoDelay (0/1)"), new CommandLineArg("TxFreq", "10", "Frequency at which the pending tx message list is polled. (int)")
	};
	
	/**
	 * @param arguments
	 * A comma delimited list of key/value pairs.
	 * @throws IOException
	 */
	public static void main(String arguments[]) throws IOException
	{
		// Change the main threads name
		Thread.currentThread().setName("Launcher");
		
		// Convert the defaults list into hash map
		HashMap<String, CommandLineArg> optionsMap = new HashMap<String, CommandLineArg>();
		for(CommandLineArg cmdLineDefault : defaultsList)
		{
			// Any options on the command line will overwrite these later.
			optionsMap.put(cmdLineDefault.getName(), cmdLineDefault);
		}
		
		// Check if the arguments exist
		if(arguments.length > 0)
		{
			// We expect the arguments as one comma delimited string of key value pairs (i.e key1=value1,key2=value2,key3=value3) with no spaces.
			// Thus only need to read arguments[0]
			parseCommandLineOption(arguments[0], optionsMap);
		}
		else
		{
			System.out.println("No command line arguments using defaults.");
		}
		
		attemptToLaunchUsingOptionChoices(optionsMap);
		
		displayValues(optionsMap);
	}
	
	/*
	 * ***************************************************************************************************
	 * Launch Method
	 *****************************************************************************************************/
	
	private static void attemptToLaunchUsingOptionChoices(HashMap<String, CommandLineArg> options) throws IOException
	{
		int mode = Integer.parseInt(options.get("mode").getValue());
		CommandLineArg desc = options.get("desc");
		
		String hostAddress = "";
		final String nodeDescription;
		
		// This would probably fail if there was no TCP/IP available
		try
		{
			hostAddress = InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e)
		{
			System.out.println("Could not resolve the address of the local host");
			
			e.printStackTrace();
			
			System.exit(-1);
		}
		
		// If there is no description - set the description to the hostname
		if(desc.getValue() == null)
		{
			nodeDescription = hostAddress;
			
			// Update map with the new description
			options.put(desc.getName(), new CommandLineArg(desc.getName(), nodeDescription, desc.getDescription()));
		}
		else
		{
			nodeDescription = desc.getValue();
		}
		
		int loglevel = Integer.parseInt(options.get("loglevel").getValue());
		
		setUpLogging(loglevel, mode, hostAddress);
		
		logRunningEnvironment();
		
		String iTheme = options.get("iTheme").getValue();
		IconManager.initialiseWithTheme(iTheme);
		
		// Load plug-ins
		ScenarioPluginManager.loadPlugins();
		
		int bText = Integer.valueOf(options.get("bText").getValue());
		boolean buttonText = true;
		
		if(bText == 0)
		{
			buttonText = false;
		}
		
		/* Initialise the event bus in asynchronous mode */
		JComputeEventBus.initAsync();
		
		int socketTX = Integer.parseInt(options.get("SocketTX").getValue());
		int socketRX = Integer.parseInt(options.get("SocketRX").getValue());
		int txFreq = Integer.parseInt(options.get("TxFreq").getValue());
		
		int tcpNoDelayInt = Integer.parseInt(options.get("TcpNoDelay").getValue());
		boolean tcpNoDelay = tcpNoDelayInt == 1 ? true : false;
		
		switch(mode)
		{
			case 0:
			{
				LookAndFeel.setLookandFeel(options.get("jLook").getValue());
				log.info("Requested Standard GUI");
				
				// Standard GUI
				createStandardGUI(new SimulationsManager(Integer.parseInt(options.get("mcs").getValue())));
			}
			break;
			case 1:
			{
				LookAndFeel.setLookandFeel(options.get("jLook").getValue());
				
				String allowMultiValue = options.get("allowMulti").getValue();
				boolean allowMulti = false;
				
				if(allowMultiValue.equalsIgnoreCase("true"))
				{
					allowMulti = true;
				}
				
				// Cluster GUI + Batch Manager with ControlNodeServer
				createClusterGUI(buttonText, new BatchManager(new ControlNodeServer(allowMulti, socketTX, socketRX, tcpNoDelay, txFreq)));
			}
			break;
			case 2:
			{
				final String address = options.get("addr").getValue();
				
				log.info("Creating ComputeNode : " + address + " (" + nodeDescription + ")");
				
				// ComputeNode2
				ComputeNode2 node = new ComputeNode2(nodeDescription, new SimulationsManager(Integer.parseInt(options.get("mcs").getValue())));
				node.start(5, address, socketTX, socketRX, tcpNoDelay, txFreq);
			}
			break;
			default:
				
				displayHelp();
				
			break;
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Tiny methods for creating the GUI's to avoid limit the scope of warning suppression
	 *****************************************************************************************************/
	
	@SuppressWarnings("unused")
	private static void createStandardGUI(SimulationsManager simulationsManager)
	{
		/* Standard GUI */
		new StandardGUI(simulationsManager);
	}
	
	@SuppressWarnings("unused")
	private static void createClusterGUI(boolean buttonText, BatchManager batchManager)
	{
		new ClusterGUI(buttonText, batchManager);
	}
	
	/*
	 * ***************************************************************************************************
	 * Command Line Parser
	 *****************************************************************************************************/
	
	// Get all the command line arguments and puts then in the map
	private static void parseCommandLineOption(String cmdline, HashMap<String, CommandLineArg> destMap)
	{
		System.out.println("Command line was : " + cmdline);
		
		// All the Command line with values
		String argument[] = cmdline.split(",");
		
		// Parse each argument in turn
		for(String arg : argument)
		{
			// Split the key=values up
			String argKV[] = arg.split("=");
			
			// Check the KV is the correct length and KV[0] (argument name) is a valid argument
			// by looking it up in the hashmap (which has the arguments with values set to defaults)
			CommandLineArg cmdlineArg = destMap.get(argKV[0]);
			
			if((argKV.length == 2) && (cmdlineArg != null))
			{
				// Get the description or we will wipe it.
				String description = cmdlineArg.getDescription();
				
				// Overwrite the current values with the command line values (and correct description)
				destMap.put(argKV[0], new CommandLineArg(argKV[0], argKV[1], description));
			}
			else
			{
				// Check if the user needs help :)
				switch(argKV[0])
				{
					case "--help":
					case "-help":
					case "help":
					case "\\help":
					case "/help":
						displayHelp();
					break;
					default:
						System.out.println("Usage : java [javaopts] -jar app_name [option1=n,option2=n]\n");
						
						System.out.println("Invalid Option : " + argKV[0]);
						
						System.exit(0);
					break;
				}
			}
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Console Output and Logging
	 *****************************************************************************************************/
	
	// Setup the logger
	private static void setUpLogging(int logLevel, int mode, String hostAddress)
	{
		String logPath = "log" + File.separator;
		
		StringBuilder logPrefix = new StringBuilder();
		
		// Detect the mode and add the log file prefix - Mode_hostname
		switch(mode)
		{
			case 0:
				logPrefix.append("Interactive_");
			break;
			case 1:
				logPrefix.append("Cluster_");
				logPrefix.append(hostAddress + "_");
			break;
			case 2:
				logPrefix.append("Node_");
				logPrefix.append(hostAddress + "_");
			break;
		}
		
		// Set the log level filenames using the logPrefix and append with the correct level suffix
		String errorLog = logPrefix.toString() + "error.log";
		String standardLog = logPrefix.toString() + "standard.log";
		
		Level level;
		
		switch(logLevel)
		{
			case 0:
			default:
				level = Level.INFO;
			break;
			case 1:
				level = Level.ERROR;
			break;
			case 2:
				level = Level.DEBUG;
			break;
		}
		
		// Initialise logging via programmatic configuration.
		Logging.InitLoggingConfig(logPath, standardLog, errorLog, level);
		
		// Configure the launcher logger - as it is the first class it needs to be after l4j2 conf.
		log = LogManager.getLogger(Launcher.class);
		
		// Classes with internal static loggers cannot be referenced until logging is setup - FileUtil is one of those classes.
		// Create the log dir now
		FileUtil.createDirIfNotExist(logPath);
		
		// Display the log file name - record in info log
		log.info("LogPath      : " + logPath);
		log.info("Standard Log : " + standardLog);
		log.info("Error Log    : " + errorLog);
	}
	
	// Output the running environment
	private static void logRunningEnvironment()
	{
		// JVM Info
		JVMInfo jvmInfo = JVMInfo.getInstance();
		log.info(jvmInfo.getJVMInfoString());
		
		// JComputeInfo - reads a properties file.
		try
		{
			JComputeInfo jcInfo = JComputeInfo.getInstance();
			log.info(jcInfo.getLaunched());
			log.info("Build            : " + jcInfo.getBuildDate());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		// OS Info
		OSInfo osInfo = OSInfo.getInstance();
		
		log.info("Operating System : " + osInfo.getOSName());
		log.info("Architecture     : " + osInfo.getSystemArch());
		log.info("Hardware Threads : " + Integer.toString(osInfo.getHWThreads()));
		log.info("Physical Memory  : " + Integer.toString(osInfo.getSystemPhysicalMemorySize()));
		
		String tmpDir = System.getProperty("java.io.tmpdir");
		log.info("Temp dir provided by OS : " + tmpDir);
	}
	
	// Output all the values the program launched with - only called if command line successfully parsed
	private static void displayValues(HashMap<String, CommandLineArg> options)
	{
		Set<String> index = options.keySet();
		
		log.info("Launch Values");
		for(String name : index)
		{
			CommandLineArg option = options.get(name);
			
			log.info(String.format("%10s", name) + " = " + String.format("%-10s", option.getValue()) + " (" + option.getDescription() + ")");
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Help Interface
	 *****************************************************************************************************/
	
	// Logger not used
	private static void displayHelp()
	{
		System.out.println("Usage : java [javaopts] -cp \"classpath\" jcompute.Launcher [option1=n,option2=n]\n");
		
		System.out.println("Help");
		
		for(int i = 0; i < 78; i++)
		{
			System.out.print("-");
		}
		System.out.print("\n");
		
		System.out.println(String.format("%10s", "option") + String.format("%10s", "\t(default)") + "\tDescription");
		
		for(CommandLineArg defaultItem : defaultsList)
		{
			System.out.println(String.format("%10s", defaultItem.getName()) + "\t" + String.format("%1$s %2$s %3$s", "     ", defaultItem.getValue(), "     ")
			+ "\t" + String.format("%10s", defaultItem.getDescription()));
		}
		
		System.exit(0);
	}
}
