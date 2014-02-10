package alifeSim.WebInterface;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
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
		
		ResourceHandler resource_handler = new ResourceHandler();
		
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase("./WebInterface");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(resource_handler);
        //handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        //server.setHandler(handlers);
		
		// SIM LIST
        ContextHandler context = new ContextHandler();
        context.setContextPath("/SimulationsList");
        context.setResourceBase(".");
        //context.setClassLoader(Thread.currentThread().getContextClassLoader());
        handlers.addHandler(context);
        
		server.setHandler(handlers);
		context.setHandler(new TestHandler(simsManager));
		
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
