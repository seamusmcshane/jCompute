package alifeSim.WebInterface;

import java.util.Properties;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

import alifeSim.Simulation.SimulationsManager;

public class WebInterface
{
	private Server server;

	private SimulationsManager simsManager;
	
	public WebInterface(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
				
		server = new Server(8080);
		HandlerList handlers = new HandlerList();
        		
		ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase("./WebInterface");
        handlers.addHandler(resource_handler);
		
		// SIM LIST
        ContextHandler context = new ContextHandler();
        context.setContextPath("/xml");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        handlers.addHandler(context);
        
		server.setHandler(handlers);
		context.setHandler(new XMLHandler(simsManager));
		
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
