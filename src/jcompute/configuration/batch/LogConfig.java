package jcompute.configuration.batch;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Logging")
public class LogConfig
{
	private boolean itemLog;
	
	private boolean infoLog;
	
	public LogConfig()
	{
		
	}
	
	public LogConfig(boolean itemLog, boolean infoLog)
	{
		this.itemLog = itemLog;
		this.infoLog = infoLog;
	}
	
	@XmlElement(name = "ItemLog")
	public void setItemLog(boolean itemLog)
	{
		this.itemLog = itemLog;
	}
	
	public boolean getItemLog()
	{
		return itemLog;
	}
	
	@XmlElement(name = "InfoLog")
	public void setInfoLog(boolean infoLog)
	{
		this.infoLog = infoLog;
	}
	
	public boolean getInfoLog()
	{
		return infoLog;
	}
}
