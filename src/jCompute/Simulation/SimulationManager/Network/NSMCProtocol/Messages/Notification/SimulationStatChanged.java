package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

public class SimulationStatChanged
{
	private int simId;
	private long time;
	private int stepNo;
	private int progress;
	private int asps;
	
	public SimulationStatChanged(SimulationStatChangedEvent e)
	{
		this.simId = e.getSimId();
		this.time = e.getTime();
		this.stepNo = e.getStepNo();
		this.progress = e.getProgress();
		this.asps = e.getAsps();
	}
	
	public SimulationStatChanged(DataInputStream source) throws IOException
	{
		simId = source.readInt();
		time = source.readLong();		
		stepNo = source.readInt();
		progress = source.readInt();
		asps = source.readInt();		
	}
	
	public int getSimId()
	{
		return simId;
	}

	public long getTime()
	{
		return time;
	}

	public int getStepNo()
	{
		return stepNo;
	}

	public int getProgress()
	{
		return progress;
	}

	public int getAsps()
	{
		return asps;
	}

	public byte[] toBytes()
	{
		// Unicode 16 -2bytes chart
		ByteBuffer tbuffer = ByteBuffer.allocate(28);
		
		tbuffer.putInt(NSMCP.SimStatNoti);
		tbuffer.putInt(simId);
		tbuffer.putLong(time);
		tbuffer.putInt(stepNo);
		tbuffer.putInt(progress);
		tbuffer.putInt(asps);
		
		return tbuffer.array();
	}
	
	public String info()
	{
		return "SimulationStatChanged : " +simId + " t " + time + " s " + stepNo + " p " + progress + " a " + asps;
	}
	
}
