package jcompute.cluster.controlnode.mapping;

import jcompute.batch.batchitem.BatchItem;
import jcompute.ncp.message.notification.SimulationStateChanged;
import jcompute.results.export.ExportFormat;

public class RemoteSimulationMapping
{
	private BatchItem batchItem;
	private int localSimId;
	private int remoteSimId;
	private int nodeUid;
	private ExportFormat exportFormat;
	private String fileNameSuffix;
	private SimulationStateChanged finalStateChanged;
	
	public RemoteSimulationMapping(BatchItem batchItem, int localSimId)
	{
		this.batchItem = batchItem;
		this.localSimId = localSimId;
	}
	
	public BatchItem getBatchItem()
	{
		return batchItem;
	}
	
	public int getLocalSimId()
	{
		return localSimId;
	}
	
	public void setNodeUid(int nodeUid)
	{
		this.nodeUid = nodeUid;
	}
	
	public int getNodeUid()
	{
		return nodeUid;
	}
	
	public void setRemoteSimId(int remoteSimId)
	{
		this.remoteSimId = remoteSimId;
	}
	
	public int getRemoteSimId()
	{
		return remoteSimId;
	}
	
	public String info()
	{
		return "Mapping - ComputeNode : " + nodeUid + " Lsid " + localSimId + " rSid " + remoteSimId;
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
