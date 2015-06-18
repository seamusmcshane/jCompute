package jCompute.Cluster.Controller.Mapping;

import jCompute.Cluster.Protocol.Notification.SimulationStateChanged;
import jCompute.Stats.StatExporter.ExportFormat;

public class RemoteSimulationMapping
{
	private int localSimId;
	private int remoteSimId;
	private int nodeUid;
	private ExportFormat exportFormat;
	private String fileNameSuffix;
	private SimulationStateChanged finalStateChanged;
	
	public RemoteSimulationMapping(int nodeUid)
	{
		this.nodeUid = nodeUid;
	}	
	
	public int getNodeUid()
	{
		return nodeUid;
	}
	
	public int getLocalSimId()
	{
		return localSimId;
	}
	
	public int getRemoteSimId()
	{
		return remoteSimId;
	}

	public void setLocalSimId(int localSimId)
	{
		this.localSimId = localSimId;		
	}

	public void setRemoteSimId(int remoteSimId)
	{
		this.remoteSimId = remoteSimId;		
	}

	public String info()
	{
		return "Mapping - Node : " + nodeUid + " Lsid " + localSimId + " rSid " + remoteSimId;
	}
	
	public void setExportFormat(ExportFormat exportFormat)
	{
		this.exportFormat = exportFormat;
	}
	
	public ExportFormat getExportFormat()
	{
		return exportFormat;
	}

	public String getFileNameSuffix()
	{
		return fileNameSuffix;
	}

	public void setFileNameSuffix(String fileNameSuffix)
	{
		this.fileNameSuffix = fileNameSuffix;
	}

	public void setFinalStateChanged(SimulationStateChanged finalStateChanged)
	{
		this.finalStateChanged = finalStateChanged;
	}
	
	public SimulationStateChanged getFinalStateChanged()
	{
		return finalStateChanged;
	}

}
