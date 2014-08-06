package jCompute;

import jCompute.Debug.DebugLogger;

import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;

public class JComputeEventBus
{
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
			eventBus = new AsyncEventBus(Executors.newFixedThreadPool(1));
			init = true;
			
			launchMessage("JComputeEventBus Started");
		}
		
		return init;
	}
	
	private static void launchMessage(String message)
	{
		DebugLogger.output("Started " + message);
	}
	
	public static void register(Object subscriber)
	{
		eventBus.register(subscriber);
	}

	public static void unregister(Object subscriber)
	{
		eventBus.unregister(subscriber);
	}

	public static void post(Object e)
	{
		eventBus.post(e);
	}
}
