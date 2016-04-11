package jCompute.cluster.ncp.notification;

import java.io.IOException;
import java.nio.ByteBuffer;

import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.cluster.ncp.NCP;

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
	
	public SimulationStatChanged(ByteBuffer source) throws IOException
	{
		simId = source.getInt();
		time = source.getLong();		
		stepNo = source.getInt();
		progress = source.getInt();
		asps = source.getInt();		
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
		int dataLen = 24;

		ByteBuffer tbuffer = ByteBuffer.allocate(dataLen+NCP.HEADER_SIZE);

		// Header
		tbuffer.putInt(NCP.SimStatNoti);
		tbuffer.putInt(dataLen);

		// Data
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
