package jCompute;

import jCompute.Thread.SimpleNamedThreadFactory;

import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.AsyncEventBus;

public class JComputeEventBus
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(JComputeEventBus.class);
	
	private static boolean init = false;
	private static AsyncEventBus eventBus;
	
	protected JComputeEventBus()
	{
	}

	public static boolean initAsync()
	{		
		if(!init)
		{
			/*
			 * Important -
			 * AsyncEventBus allows events to be dispatched and not block the poster.
			 * A fixed thread pool of 1 ensures the event orders are preserved in order of posting to the bus.
			 * This makes the event bus - a non-blocking in-order event dispatcher.
			 */
			 
			eventBus = new AsyncEventBus(Executors.newFixedThreadPool(1, new SimpleNamedThreadFactory("JComputeEventBus")));
			init = true;
			
			log.info("JComputeEventBus Started");
		}
		
		return init;
	}
	
	public static void register(Object subscriber)
	{
		log.info(subscriber.getClass().getSimpleName() + " Registered on EventBus");
		eventBus.register(subscriber);
	}

	public static void unregister(Object subscriber)
	{
		log.info(subscriber.getClass().getSimpleName() + " Unregistered from EventBus");
		eventBus.unregister(subscriber);
	}

	public static void post(Object e)
	{
		eventBus.post(e);
	}
}
