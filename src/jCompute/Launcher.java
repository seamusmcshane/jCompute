package jCompute;

import jCompute.Debug.DebugLogger;
import jCompute.Gui.Batch.BatchGUI;
import jCompute.Gui.Standard.StandardGUI;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager;
import jCompute.Simulation.SimulationManager.Network.Manager.NetworkSimulationsManager;
import jCompute.Simulation.SimulationManager.Network.Node.Node;

import java.util.HashMap;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher
{
	@SuppressWarnings("unused")
	private static IconManager iconManager;
	
	// Standard GUI
	@SuppressWarnings("unused")
	private static StandardGUI standardGUI;

	// Batch GUI
	@SuppressWarnings("unused")
	private static BatchGUI batchGUI;
	
	// Remote Node
	@SuppressWarnings("unused")
	private static Node node;

	// Command Line HashMap
	private static HashMap<String, CommandLineArg> opts;

	// Command Line HasMap - Defaults for faster look up
	private static HashMap<String, CommandLineArg> optDefaults;

	// Defaults ( option string, default value, option description
	private static CommandLineArg defaultsList[] =
	{
			new CommandLineArg("mcs", "8","Max Concurrent Simulations (Int)"), new CommandLineArg("mode", "0", "Standard/Batch GUI/Node (0/1,2)"),
			new CommandLineArg("debug", "0","Enable Disable Debug (0/1)"), new CommandLineArg("iTheme", "none","Icon Theme Name (String)"),
			new CommandLineArg("bText", "1","Button Text (0/1)"), new CommandLineArg("sm", "0"," Simulation Manager Local/Network(0/1)"),
			new CommandLineArg("addr", "127.0.0.1","Listening Address (InetAddr)")
	};
	
	public static void main(String args[])
	{
	    String tmpDir = System.getProperty("java.io.tmpdir");
	    DebugLogger.output("Temp dir provided by OS : " + tmpDir);
	    
		indexDefaults();

		parseCommandLine(args);

		displayValues();
		
		implementOpts();
		
	}

	private static void displayValues()
	{
		Set<String> index = opts.keySet();
		
		DebugLogger.output("Launching...");
		for(String name : index)
		{
			
			DebugLogger.output(String.format("%10s", name) + " = " + opts.get(name).getValue());
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
		
		String iTheme = opts.get("iTheme").getValue();
		IconManager.init(iTheme);
		
		int bText = Integer.valueOf(opts.get("bText").getValue());
		boolean buttonText = true;
		
		if(bText==0)
		{
			buttonText = false;
		}
		
		/* Init the Event bus in Async Mode */
		JComputeEventBus.initAsync();
		
		lookandFeel();
		
		int mode = Integer.parseInt(opts.get("mode").getValue());
		
		
		switch(mode)
		{
			case 0:
				
				/* Local Simulation Manager */			
				standardGUI = new StandardGUI(new SimulationsManager(Integer.parseInt(opts.get("mcs").getValue())));
				
			break;
			case 1:
				int simManType = Integer.parseInt(opts.get("sm").getValue());

				if(simManType==0)
				{
					// Local - Testing
					batchGUI = new BatchGUI(new SimulationsManager(Integer.parseInt(opts.get("mcs").getValue())),buttonText);
				}
				else
				{
					/* Network Simulation Manager */			
					batchGUI = new BatchGUI(new NetworkSimulationsManager(),buttonText);
				}
				
			break;			
			case 2:
				
				String address = opts.get("addr").getValue();
				
				DebugLogger.output("Creating Node : " + address);
				
				node = new Node(address);
			
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
		DebugLogger.output("Command line was : " + cmdline);

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
	
	private static void displayHelp()
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
	
	/* Set Nimbus Look and feel */
	private static void lookandFeel()
	{		
		// Default to the system provided look and feel
		String lookandfeel = UIManager.getSystemLookAndFeelClassName();
		
		UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
			
		for(int i=0;i<lookAndFeels.length;i++)
		{
			if(lookAndFeels[i].getClassName().toLowerCase().contains("nimbus"))
			{
				lookandfeel = lookAndFeels[i].getClassName();
				break;
			}
		}
		
		try
		{
			UIManager.setLookAndFeel(lookandfeel);
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (InstantiationException e1)
		{
			e1.printStackTrace();
		}
		catch (IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
	}
}
