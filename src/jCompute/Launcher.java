package jCompute;

import jCompute.Debug.DebugLogger;
import jCompute.Gui.Batch.BatchGUI;
import jCompute.Gui.Standard.StandardGUI;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager;

import java.util.HashMap;
import java.util.Set;

public class Launcher
{
	private static IconManager iconManager;
	
	// Standard GUI
	private static StandardGUI standardGUI;

	// Batch GUI
	private static BatchGUI batchGUI;

	// Command Line HasMap
	private static HashMap<String, CommandLineArg> opts;

	// Command Line HasMap - Defaults for faster look up
	private static HashMap<String, CommandLineArg> optDefaults;

	// Defaults
	private static CommandLineArg defaultsList[] =
	{
			new CommandLineArg("mcs", "8","Max Concurrent Simulations"), new CommandLineArg("guiInt", "1", "Enable Disable Standard GUI (0/1)"), 
			new CommandLineArg("batchInt", "0","Enable Disable Batch Interface (0/1)"), new CommandLineArg("debug", "0","Enable Disable Debug (0/1)"),
			new CommandLineArg("iTheme", "none","Icon Theme Name")
	};

	public static void main(String args[])
	{
	    String tmpDir = System.getProperty("java.io.tmpdir");
	    System.out.println("Temp dir provided by OS : " + tmpDir);
	    
		indexDefaults();

		parseCommandLine(args);

		displayValues();
		
		implementOpts();
		
	}

	private static void displayValues()
	{
		Set<String> index = opts.keySet();
		
		System.out.println("Launching...");
		for(String name : index)
		{
			
			System.out.println(String.format("%10s", name) + " = " + opts.get(name).getValue());
		}
	}

	private static void indexDefaults()
	{
		optDefaults = new HashMap<String,CommandLineArg>();
		
		for(CommandLineArg cmdLineDefault : defaultsList)
		{
			optDefaults.put(cmdLineDefault.getName(), cmdLineDefault);
		}
		
	}
	private static void implementOpts()
	{	
		
		if(Integer.parseInt(opts.get("debug").getValue())== 1)
		{
			DebugLogger.setDebug(true);
		}


		IconManager.init(opts.get("iTheme").getValue());

		
		if(Integer.parseInt(opts.get("guiInt").getValue()) == 1)
		{
			/* Local Simulation Manager */			
			standardGUI = new StandardGUI(new SimulationsManager(Integer.parseInt(opts.get("mcs").getValue())));
		}
		
		if(Integer.parseInt(opts.get("batchInt").getValue()) == 1)
		{
			/* Network Simulation Manager */			
			//batchGUI = new BatchGUI(new NetworkSimulationsManager());
			
			// Local - Testing
			batchGUI = new BatchGUI(new SimulationsManager(Integer.parseInt(opts.get("mcs").getValue())));

		}

	}

	private static void parseCommandLine(String args[])
	{
		// Clone the defaults, we will used these values if the args arnt passed
		opts = (HashMap<String, CommandLineArg>) optDefaults.clone();
		
		if (args.length > 0 )
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

		for (String opt : options)
		{
			//System.out.println(opt);

			String kv[] = opt.split("=");

			// KV correct length and KV[0] is a valid arg
			if (kv.length == 2 && optDefaults.containsKey(kv[0]))
			{
				// Get the description or we will wipe it.
				String description = opts.get(kv[0]).getDescription();
				
				// Name/value with name as index in map
				opts.put(kv[0], new CommandLineArg(kv[0], kv[1],description));
			}
			else
			{
				if(opt.equalsIgnoreCase("--help") || opt.equals("-help") || opt.equals("help") || opt.equals("\\help") || opt.equals("/help"))
				{
					System.out.println("Usage : java [javaopts] -jar app_name [option1=n,option2=n]\n");

					System.out.println("Help");
					
					for(int i=0;i<78;i++)
					{
						System.out.print("-");
					}
					System.out.print("\n");

					System.out.println(String.format("%10s","option")+String.format("%10s","\t(default)")+"\tDescription");
					
					for(CommandLineArg defaultItem : defaultsList)
					{
						System.out.println(String.format("%10s",defaultItem.getName()) + "\t" + String.format("%1$s %2$s %3$s", "     ", defaultItem.getValue(), "     ") + "\t" + String.format("%10s",defaultItem.getDescription()));
					}
					
					System.exit(0);
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
}
