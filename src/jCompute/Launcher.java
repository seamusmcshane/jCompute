package jCompute;

import jCompute.Batch.BatchManager.BatchManager;
import jCompute.Cluster.Controller.ControlNode.ControlNode;
import jCompute.Cluster.Node.Node;
import jCompute.Gui.Cluster.ClusterGUI;
import jCompute.Gui.Interactive.StandardGUI;
import jCompute.Scenario.ScenarioManager;
import jCompute.SimulationManager.SimulationsManager;
import jCompute.util.JVMInfo;
import jCompute.util.LookAndFeel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher
{
	// SL4J Logger
	private static Logger log;
	
	@SuppressWarnings("unused")
	private static IconManager iconManager;
	
	// Standard GUI
	@SuppressWarnings("unused")
	private static StandardGUI standardGUI;
	
	// Cluster GUI
	@SuppressWarnings("unused")
	private static ClusterGUI clusterGUI;
	
	// Remote Node
	private static Node node;
	
	// Command Line HashMap
	private static HashMap<String, CommandLineArg> opts;
	
	// Command Line HasMap - Defaults for faster look up
	private static HashMap<String, CommandLineArg> optDefaults;
	
	// Defaults ( option string, default value, option description
	private static CommandLineArg defaultsList[] =
	{
		new CommandLineArg("mcs", "8", "Max Concurrent Simulations (Int)"), new CommandLineArg("mode", "0", "Standard/Batch GUI/Node (0/1,2)"),
		new CommandLineArg("iTheme", "none", "Icon Theme Name (String)"), new CommandLineArg("bText", "1", "Button Text (0/1)"),
		new CommandLineArg("addr", "127.0.0.1", "Listening Address (InetAddr)"), new CommandLineArg("loglevel", "0", "Log Level(0/1/2)"), new CommandLineArg("desc", "not set", "Node Description"),
		new CommandLineArg("jLook", "default", "Set JavaUI Look and Feel"), new CommandLineArg("allowMulti", "false", "Allow multiple connections from same address"),
		new CommandLineArg("SocketTX", "65536", "SocketTX Buffer Size (int)"), new CommandLineArg("SocketRX", "65536", "SocketRX Buffer Size (int)"),
		new CommandLineArg("TcpNoDelay", "1", "Configure TcpNoDelay (0/1)")
	};
	
	public static void main(String args[])
	{
		indexDefaults();
		
		parseCommandLine(args);
		
		implementOpts();
		
		displayValues();
	}
	
	private static void implementOpts()
	{
		int loglevel = Integer.parseInt(opts.get("loglevel").getValue());
		
		int mode = Integer.parseInt(opts.get("mode").getValue());
		
		String hostAddress = "";
		
		try
		{
			hostAddress = InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		StringBuilder logPrefix = new StringBuilder();
		
		switch(mode)
		{
			case 0:
				logPrefix.append("StandardGUI_");
			break;
			case 1:
				logPrefix.append("ClusterGUI_");
				logPrefix.append(hostAddress);
			break;
			case 2:
				logPrefix.append("Node_");
				logPrefix.append(hostAddress);
			break;
		}
		
		switch(loglevel)
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
		
		// Configure the launcher logger - as it is the first class it needs to
		// be after l4j2 conf.
		log = LoggerFactory.getLogger(Launcher.class);
		
		log.info(JVMInfo.getJVMInfoString());
		
		log.info("Standard Log " + standardLog);
		log.info("Error Log    " + errorLog);
		log.info("Debug Log    " + debugLog);
		
		String tmpDir = System.getProperty("java.io.tmpdir");
		log.info("Temp dir provided by OS : " + tmpDir);
		
		String iTheme = opts.get("iTheme").getValue();
		IconManager.init(iTheme);
		
		ScenarioManager.init();
		
		int bText = Integer.valueOf(opts.get("bText").getValue());
		boolean buttonText = true;
		
		if(bText == 0)
		{
			buttonText = false;
		}
		
		/* Init the Event bus in Async Mode */
		JComputeEventBus.initAsync();
		
		int socketTX = Integer.parseInt(opts.get("SocketTX").getValue());
		int socketRX = Integer.parseInt(opts.get("SocketRX").getValue());
		
		int tcpNoDelayInt = Integer.parseInt(opts.get("TcpNoDelay").getValue());
		boolean tcpNoDelay = tcpNoDelayInt == 1 ? true : false;
		
		switch(mode)
		{
			case 0:
				LookAndFeel.setLookandFeel(opts.get("jLook").getValue());
				log.info("Requested Standard GUI");
				/* Local Simulation Manager */
				standardGUI = new StandardGUI(new SimulationsManager(Integer.parseInt(opts.get("mcs").getValue())));
				
			break;
			case 1:
				LookAndFeel.setLookandFeel(opts.get("jLook").getValue());
				
				String allowMultiValue = opts.get("allowMulti").getValue();
				boolean allowMulti = false;
				
				if(allowMultiValue.equalsIgnoreCase("true"))
				{
					allowMulti = true;
				}
				
				BatchManager batchManager = new BatchManager(new ControlNode(allowMulti, socketTX, socketRX, tcpNoDelay));
				
				clusterGUI = new ClusterGUI(buttonText, batchManager);
				
			break;
			case 2:
				
				final String address = opts.get("addr").getValue();
				final String desc = opts.get("desc").getValue();
				
				log.info("Creating Node : " + address + " (" + desc + ")");
				
				Thread nodeLauncher = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						node = new Node(address, desc, new SimulationsManager(Integer.parseInt(opts.get("mcs").getValue())), socketTX, socketRX, tcpNoDelay);
						
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
	
	@SuppressWarnings("unchecked")
	private static void parseCommandLine(String args[])
	{
		// Clone the defaults, we will used these values if the args arnt passed
		opts = (HashMap<String, CommandLineArg>) optDefaults.clone();
		
		if(args.length > 0)
		{
			getOptions(args[0]);
		}
		else
		{
			System.out.println("No Command Line Args using defaults.");
		}
	}
	
	// Get all the command line args and puts then in the map
	private static void getOptions(String cmdline)
	{
		System.out.println("Command line was : " + cmdline);
		
		String options[] = cmdline.split(",");
		
		for(String opt : options)
		{
			// System.out.println(opt);
			
			String kv[] = opt.split("=");
			
			// KV correct length and KV[0] is a valid arg
			if(kv.length == 2 && optDefaults.containsKey(kv[0]))
			{
				// Get the description or we will wipe it.
				String description = opts.get(kv[0]).getDescription();
				
				// Name/value with name as index in map
				opts.put(kv[0], new CommandLineArg(kv[0], kv[1], description));
			}
			else
			{
				if(opt.equalsIgnoreCase("--help") || opt.equals("-help") || opt.equals("help") || opt.equals("\\help") || opt.equals("/help"))
				{
					displayHelp();
				}
				else
				{
					System.out.println("Usage : java [javaopts] -jar app_name [option1=n,option2=n]\n");
					
					System.out.println("Invalid Option : " + opt);
					
					System.exit(0);
				}
			}
		}
	}
	
	/**
	 * Help Interface
	 */
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
			System.out.println(String.format("%10s", defaultItem.getName()) + "\t" + String.format("%1$s %2$s %3$s", "     ", defaultItem.getValue(), "     ") + "\t"
					+ String.format("%10s", defaultItem.getDescription()));
		}
		
		System.exit(0);
	}
	
	private static void displayValues()
	{
		Set<String> index = opts.keySet();
		
		log.info("Launch Values");
		for(String name : index)
		{
			
			log.info(String.format("%10s", name) + " = " + opts.get(name).getValue());
		}
	}
	
	private static void indexDefaults()
	{
		optDefaults = new HashMap<String, CommandLineArg>();
		
		for(CommandLineArg cmdLineDefault : defaultsList)
		{
			optDefaults.put(cmdLineDefault.getName(), cmdLineDefault);
		}
	}
}
