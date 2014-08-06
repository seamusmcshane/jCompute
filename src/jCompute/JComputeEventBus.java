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
			eventBus = new AsyncEventBus(Executors.newCachedThreadPool());
			init = true;
			
			launchMessage("AsyncEventBus with Cached Thread Pool");
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
