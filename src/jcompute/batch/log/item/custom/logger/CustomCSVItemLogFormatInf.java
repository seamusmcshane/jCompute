package jcompute.batch.log.item.custom.logger;

public interface CustomCSVItemLogFormatInf
{
	public String getLogFileName();
	
	public int numberOfFields();
	
	public String getFieldHeading(int field);
	
	public Object getFieldValue(int field);
	
	public void setFieldValue(int field, Object value);
}
