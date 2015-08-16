package jCompute;

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
		new CommandLineArg("mcs", "8", "Max Concurrent Simulations (Int)"),
		new CommandLineArg("mode", "0", "Standard/Batch GUI/Node (0/1,2)"),
		new CommandLineArg("iTheme", "none", "Icon Theme Name (String)"), new CommandLineArg("bText", "1", "Button Text (0/1)"),
		new CommandLineArg("addr", "127.0.0.1", "Listening Address (InetAddr)"), new CommandLineArg("loglevel", "0", "Log Level(0/1/2)"),
		new CommandLineArg("desc", "not set", "Node Description"), new CommandLineArg("jLook", "default", "Set JavaUI Look and Feel")
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
		
		switch(loglevel)
		{
			case 2:
				// Debug
				System.setProperty("log4j.configurationFile", "log/config/log4j2-debug.xml");
				System.out.println("Enabled Debug Log Level");
			break;
			case 1:
				// Standard
				System.setProperty("log4j.configurationFile", "log/config/log4j2.xml");
				System.out.println("Enabled Standard Log Level");
			break;
			case 0:
			default:
				// Standard
				System.setProperty("log4j.configurationFile", "log/config/log4j2-consoleonly.xml");
				System.out.println("Logging to Console Only at Standard Log Level");
			break;
		}
		
		// Configure the launcher logger - as it is the first class it needs to
		// be after l4j2 conf.
		log = LoggerFactory.getLogger(Launcher.class);
		
		log.info(JVMInfo.getJVMInfoString());
		
		try
		{
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
			log.info("Host Address : " + hostAddress);
		}
		catch(UnknownHostException e)
		{
			log.error("Hostname lookup failed");
			e.printStackTrace();
		}
		
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
		
		int mode = Integer.parseInt(opts.get("mode").getValue());
		
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
				
				clusterGUI = new ClusterGUI(buttonText);
				
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
						node = new Node(address, desc, new SimulationsManager(Integer.parseInt(opts.get("mcs").getValue())));
						
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
				if(opt.equalsIgnoreCase("--help") || opt.equals("-help") || opt.equals("help") || opt.equals("\\help")
						|| opt.equals("/help"))
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
			System.out.println(String.format("%10s", defaultItem.getName()) + "\t"
					+ String.format("%1$s %2$s %3$s", "     ", defaultItem.getValue(), "     ") + "\t"
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
