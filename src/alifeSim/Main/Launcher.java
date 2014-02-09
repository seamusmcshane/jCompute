package alifeSim.Main;

import alifeSim.Gui.GUI;
import alifeSim.Simulation.SimulationsManager;
import alifeSim.WebInterface.WebInterface;

public class Launcher
{
	private static GUI gui;

	// Simulations Manager
	private static int maxConcurrentSims = 8;
	private static SimulationsManager simsManager;
	
	// WebInterface
	private static WebInterface webInterface;
	
	public static void main(String args[])
	{
		/* Simulation Manager */
		simsManager = new SimulationsManager(maxConcurrentSims);		

		/* GUI */
		gui = new GUI(simsManager);
		
		webInterface = new WebInterface(simsManager);
		 
	}
	
}
