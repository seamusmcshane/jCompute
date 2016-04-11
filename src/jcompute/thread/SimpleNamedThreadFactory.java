package jcompute.thread;

import java.util.concurrent.ThreadFactory;

// Simple Thread factory so threads are named
public class SimpleNamedThreadFactory implements ThreadFactory 
{
	private String name;
	private int numThreads;
	
	public SimpleNamedThreadFactory(String name)
	{
		this.name = name;
		numThreads = 1;
	}
	
	@Override
	public Thread newThread(Runnable r) 
	{
		Thread thread = new Thread(r);
		thread.setName(name + " Thread " + numThreads);
		numThreads++;
		return thread;
	}
}
