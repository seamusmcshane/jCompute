package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationState.SimState;

public class SimulationStateChanged
{
	private int simId;
	private SimState state;
	private long runTime;
	private String endEvent;
	private long stepCount;
	
	public SimulationStateChanged(SimulationStateChangedEvent e)
	{
		this.state = e.getState();
		this.simId = e.getSimId();
		this.runTime = e.getRunTime();
		this.endEvent = e.getEndEvent();
		this.stepCount = e.getStepCount();		
	}
	
	public SimulationStateChanged(DataInputStream source) throws IOException
	{
		state = SimState.fromInt(source.readInt());
		simId = source.readInt();		
		runTime = source.readLong();
		
		int elen = source.readInt();		

		if(elen > 0)
		{
			byte[] sBytes = new byte[elen];
			
			source.read(sBytes, 0, elen);
			
			endEvent = new String(sBytes);
		}
		else
		{
			endEvent = "NONE";
		}
		
		stepCount = source.readLong();
		
	}

	public SimState getState()
	{
		return state;
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public long getRunTime()
	{
		return runTime;
	}

	public String getEndEvent()
	{
		return endEvent;
	}

	public long getStepCount()
	{
		return stepCount;
	}

	public byte[] toBytes()
	{
		int elen = 0;
		
		if(endEvent!=null)
		{
			elen = endEvent.getBytes().length;
		}
		
		// Unicode 16 -2bytes chart
		ByteBuffer tbuffer = ByteBuffer.allocate(elen+32);
		
		tbuffer.putInt(NSMCP.SimStateNoti);
		tbuffer.putInt(state.ordinal());
		tbuffer.putInt(simId);
		tbuffer.putLong(runTime);
		
		// EndEvent follows (elen is chars)
		tbuffer.putInt(elen);
		
		if(elen>0)
		{
			tbuffer.put(endEvent.getBytes());
		}
		
		tbuffer.putLong(stepCount);
		
		return tbuffer.array();
	}
	
	public String info()
	{
		return "SimulationStateChanged : " +simId + " s " + state.toString() + " RT " + runTime + " ee " + endEvent + " sc " + stepCount;
	}
	
}
