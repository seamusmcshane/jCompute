package jCompute;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Cluster.Controller.ControlNode.ControlNode;
import jCompute.Cluster.Node.Node;
import jCompute.Gui.Cluster.ClusterGUI;
import jCompute.Gui.Interactive.StandardGUI;
import jCompute.Scenario.ScenarioManager;
import jCompute.SimulationManager.SimulationsManager;
import jCompute.util.FileUtil;
import jCompute.util.JComputeInfo;
import jCompute.util.JVMInfo;
import jCompute.util.LookAndFeel;
import jCompute.util.OSInfo;
import jCompute.util.Text;

public class Launcher
{
	// SL4J Logger
	private static Logger log;

	// Defaults ( option string, default value, option description
	private static final CommandLineArg defaultsList[] =
	{
		new CommandLineArg("mcs", Integer.toString(Runtime.getRuntime().availableProcessors()), "Max Concurrent Simulations (Int)"), new CommandLineArg("mode",
		"0", "Standard/Batch GUI/Node (0/1,2)"), new CommandLineArg("iTheme", "none", "Icon Theme Name (String)"), new CommandLineArg("bText", "1",
		"Button Text (0/1)"), new CommandLineArg("addr", "127.0.0.1", "Listening Address (InetAddr)"), new CommandLineArg("loglevel", "0", "Log Level(0/1/2)"),
		new CommandLineArg("desc", null, "Node Description"), new CommandLineArg("jLook", "default", "Set JavaUI Look and Feel"), new CommandLineArg(
		"allowMulti", "false", "Allow multiple connections from same address"), new CommandLineArg("SocketTX", "65536", "SocketTX Buffer Size (int)"),
		new CommandLineArg("SocketRX", "65536", "SocketRX Buffer Size (int)"), new CommandLineArg("TcpNoDelay", "1", "Configure TcpNoDelay (0/1)"),
		new CommandLineArg("TxFreq", "10", "Frequency at which the pending tx message list is polled. (int)")
	};

	public static void main(String args[])
	{
		// Set the main threads name
		Thread.currentThread().setName("Launcher");

		// Convert the defaults list into hash map
		HashMap<String, CommandLineArg> optionsMap = new HashMap<String, CommandLineArg>();
		for(CommandLineArg cmdLineDefault : defaultsList)
		{
			// Any options on the command line will overwrite these later.
			optionsMap.put(cmdLineDefault.getName(), cmdLineDefault);
		}

		// Check if the args exist
		if(args.length > 0)
		{
			// We expect the arguments as one comma delimited string of key value pairs (eg key1=value1,key2=value2,key3=value3) with no spaces - thus only read arg[0]
			parseCommandLineOption(args[0], optionsMap);
		}
		else
		{
			System.out.println("No Command Line Args using defaults.");
		}

		attemptToLaunchUsingOptionChoices(optionsMap);

		displayValues(optionsMap);
	}

	/*
	 * ***************************************************************************************************
	 * Launch Method
	 *****************************************************************************************************/

	private static void attemptToLaunchUsingOptionChoices(HashMap<String, CommandLineArg> options)
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
			log.error("Could not resolve the address of the local host");

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
		IconManager.init(iTheme);

		ScenarioManager.init();

		int bText = Integer.valueOf(options.get("bText").getValue());
		boolean buttonText = true;

		if(bText == 0)
		{
			buttonText = false;
		}

		/* Init the Event bus in Async Mode */
		JComputeEventBus.initAsync();

		int socketTX = Integer.parseInt(options.get("SocketTX").getValue());
		int socketRX = Integer.parseInt(options.get("SocketRX").getValue());
		int txFreq = Integer.parseInt(options.get("TxFreq").getValue());

		int tcpNoDelayInt = Integer.parseInt(options.get("TcpNoDelay").getValue());
		boolean tcpNoDelay = tcpNoDelayInt == 1 ? true : false;

		switch(mode)
		{
			case 0:
				LookAndFeel.setLookandFeel(options.get("jLook").getValue());
				log.info("Requested Standard GUI");

				// Standard GUI
				createStandardGUI(Integer.parseInt(options.get("mcs").getValue()));

			break;
			case 1:
				LookAndFeel.setLookandFeel(options.get("jLook").getValue());

				String allowMultiValue = options.get("allowMulti").getValue();
				boolean allowMulti = false;

				if(allowMultiValue.equalsIgnoreCase("true"))
				{
					allowMulti = true;
				}

				// Cluster GUI + Batch Manager with Control Node
				createClusterGUI(buttonText, new BatchManager(new ControlNode(allowMulti, socketTX, socketRX, tcpNoDelay, txFreq)));

			break;
			case 2:

				final String address = options.get("addr").getValue();

				log.info("Creating Node : " + address + " (" + nodeDescription + ")");

				// Launcher thread so we don't block Launcher and allow it to exit the same way as the GUI modes.
				Thread nodeLauncher = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						// Remote Node
						Node node = new Node(address, nodeDescription, new SimulationsManager(Integer.parseInt(options.get("mcs").getValue())), socketTX,
						socketRX, tcpNoDelay, txFreq);

						node.start();

						log.info("Node Exited");

						System.exit(0);
					}
				});
				nodeLauncher.setName("Node");
				nodeLauncher.start();

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
	private static void createStandardGUI(int mcs)
	{
		/* Standard GUI with Local Simulation Manager */
		new StandardGUI(new SimulationsManager(mcs));
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

	// Get all the command line args and puts then in the map
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

		FileUtil.createDirIfNotExist(logPath);

		StringBuilder logPrefix = new StringBuilder();

		// Detect the mode and add the log file prefix - Mode_hostname
		switch(mode)
		{
			case 0:
				logPrefix.append("Standard");
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

		// Append a date stamp to the log name
		logPrefix.append(Text.longTimeToDateSafeString(System.currentTimeMillis()));

		// detect the log level and uses the correct configuration
		switch(logLevel)
		{
			case 2:
				// Debug
				System.setProperty("log4j.configurationFile", "log-config/config/log4j2-debug.xml");
				System.out.println("Debug Logging Level");
			break;
			case 1:
				// Standard
				System.setProperty("log4j.configurationFile", "log-config/log4j2.xml");
				System.out.println("Info level Logging to file and errors logged to file.");
			break;
			default:
				// Standard

				// Async Logging
				System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
				System.setProperty("log4j.configurationFile", "log-config/log4j2-consoleonly.xml");
				System.out.println("AsyncLogging + Info level console Logging, with errors logged to file.");
			break;
		}

		// Set the log level filenames using the logPrefix and append with the correct level suffix
		String errorLog = logPrefix.toString() + "_error";
		String standardLog = logPrefix.toString() + "_standard";
		String debugLog = logPrefix.toString() + "_debug";
		System.setProperty("ERROR_LOG_FILENAME", errorLog);
		System.setProperty("STANDARD_LOG_FILENAME", standardLog);
		System.setProperty("DEBUG_LOG_FILENAME", debugLog);

		try
		{
			Class.forName("org.apache.logging.log4j.core.LoggerContext");

			// Update the loggers with the correct log filenames
			org.apache.logging.log4j.core.LoggerContext.getContext().reconfigure();
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("Skipping log4j reconfigure - classes not loaded.");
		}

		// Configure the launcher logger - as it is the first class it needs to be after l4j2 conf.
		log = LoggerFactory.getLogger(Launcher.class);

		// Display the log file name - record in info log
		log.info("LogPath      : " + logPath);
		log.info("Standard Log : " + standardLog);
		log.info("Error Log    : " + errorLog);
		log.info("Debug Log    : " + debugLog);
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
		System.out.println("Usage : java [javaopts] -jar app_name [option1=n,option2=n]\n");

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
