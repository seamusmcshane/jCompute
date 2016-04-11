package jcompute.batch.logfileprocessor.tests;

import java.io.IOException;

import jcompute.batch.logfileprocessor.InfoLogProcessor;

public class InfoLogTest
{
	public static void main(String args[])
	{
		InfoLogProcessor infoLog;
		try
		{
			infoLog = new InfoLogProcessor("S:\\PHD\\WorkSpace\\jCompute\\stats\\CPRG\\MovementCost0.0500\\2016-02-01@0926[1] ContinuousPredationPrg1\\InfoLog.log");
			infoLog.dump();
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}