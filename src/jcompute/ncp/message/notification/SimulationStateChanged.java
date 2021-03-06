package jcompute.ncp.message.notification;

import java.io.IOException;
import java.nio.ByteBuffer;

import jcompute.ncp.NCPDefinitions;
import jcompute.ncp.message.NCPMessage;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulation.event.SimulationStateChangedEvent;

public class SimulationStateChanged extends NCPMessage
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
	
	public SimulationStateChanged(ByteBuffer source) throws IOException
	{
		state = SimState.fromInt(source.getInt());
		simId = source.getInt();
		runTime = source.getLong();
		
		int elen = source.getInt();
		
		if(elen > 0)
		{
			byte[] sBytes = new byte[elen];
			
			source.get(sBytes, 0, elen);
			
			endEvent = new String(sBytes);
		}
		else
		{
			endEvent = "NONE";
		}
		
		stepCount = source.getLong();
		
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
	
	@Override
	public int getType()
	{
		return NCPDefinitions.SimStateNoti;
	}
	
	@Override
	public byte[] toBytes()
	{
		// End Event
		int elen = 0;
		
		if(endEvent != null)
		{
			elen = endEvent.getBytes().length;
		}
		
		int dataLen = elen + 28;
		
		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen + NCPDefinitions.HEADER_SIZE);
		
		// Header
		tbuffer.putInt(NCPDefinitions.SimStateNoti);
		tbuffer.putInt(dataLen);
		
		// Data
		tbuffer.putInt(state.ordinal());
		tbuffer.putInt(simId);
		tbuffer.putLong(runTime);
		
		// EndEvent follows (elen is chars)
		tbuffer.putInt(elen);
		
		if(elen > 0)
		{
			tbuffer.put(endEvent.getBytes());
		}
		
		tbuffer.putLong(stepCount);
		
		return tbuffer.array();
	}
	
	public String info()
	{
		return "SimulationStateChanged : " + simId + " s " + state.toString() + " RT " + runTime + " ee " + endEvent + " sc " + stepCount;
	}
	
}
