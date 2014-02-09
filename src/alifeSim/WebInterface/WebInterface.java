package alifeSim.WebInterface;

import org.eclipse.jetty.server.Server;

import alifeSim.Simulation.SimulationsManager;

public class WebInterface
{
	private Server server;

	private SimulationsManager simsManager;
	
	public WebInterface(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
		
		server = new Server(8080);
		server.setHandler(new TestHandler(simsManager));
		
		try
		{
			server.start();
			server.join();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
