package jcompute.batch.log.item.custom.logger;

import jcompute.results.custom.CustomResultFieldType;

public interface CustomCSVItemLogFormatInf
{
	public String getLogFileName();
	
	public int numberOfFields();
	
	public String getFieldHeading(int field);
	
	public Object getFieldValue(int field);
	
	public CustomResultFieldType getFieldType(int field);
	
	public void setFieldValue(int field, Object value);
	
	public CustomCSVItemLogFormatInf getInstance() throws InstantiationException, IllegalAccessException;
}
