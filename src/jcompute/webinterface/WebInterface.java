package jcompute.webinterface;

import org.eclipse.jetty.server.Server;

import jcompute.cluster.batchmanager.BatchManager;

public class WebInterface
{
	private Server server;

	public WebInterface(BatchManager bm)
	{
		server = new Server(8080);
		server.setHandler(new TestHandler(bm));
		
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
