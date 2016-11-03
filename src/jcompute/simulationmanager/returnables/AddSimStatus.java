package jcompute.simulationmanager.returnables;

public class AddSimStatus
{
	public final int simId;
	public final boolean needData;
	public final String[] fileNames;
	
	public AddSimStatus(int simId, boolean needData, String[] fileNames)
	{
		this.simId = simId;
		this.needData = needData;
		this.fileNames = fileNames;
	}
}
