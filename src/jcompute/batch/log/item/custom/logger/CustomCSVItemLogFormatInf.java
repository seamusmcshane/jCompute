package jcompute.batch.log.item.custom.logger;

public interface CustomCSVItemLogFormatInf
{
	public String getLogFileName();
	
	public int numberOfFields();
	
	public String getFieldValue(int field);
	
	public String getFieldHeading(int field);
	
	public void bytesToRow(byte[] bytes);
}
