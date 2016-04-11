package jcompute.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class JUnitTestLogger extends TestWatcher
{
	@Override
	protected void starting(Description description)
	{
		if(description.getMethodName() == null)
		{
			// Suit Top Level
			System.out.println("Starting " + description.getDisplayName() + " - Tests : " + description.testCount());
		}
		else
		{
			System.out.println("Running Test " + description.getMethodName() + " ");
		}
	}
	
	@Override
	protected void succeeded(Description description)
	{
		if(description.getMethodName() == null)
		{
			System.out.println("Passed " + description.getDisplayName() + " - Tests : " + description.testCount());
		}
		else
		{
			System.out.println(" Passed " + description.getMethodName() + " ");
		}
	}
	
	@Override
	protected void failed(Throwable e, Description description)
	{
		System.out.print(" Failed " + e.getMessage());
	}
	
	@Override
	protected void finished(Description description)
	{
		System.out.println();
	}
}
