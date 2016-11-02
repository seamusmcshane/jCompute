package jcompute.results.export.trace;

import jcompute.results.export.ExportFormat;

public interface TraceResultInf
{
	public String[] getTraceFileNames();
	
	public byte[][] getTraceData();
	
	public byte[] toBytes();
	
	public void export(String directory, ExportFormat format);
}
