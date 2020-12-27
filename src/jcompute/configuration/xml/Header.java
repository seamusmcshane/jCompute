package jcompute.configuration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Header")
@XmlType(propOrder =
{
	"type", "version"
})
public class Header
{
	private String type;
	
	private double version;
	
	public Header()
	{
		
	}
	
	public Header(String type, double version)
	{
		this.type = type;
		this.version = version;
	}
	
	@XmlElement(name = "Type")
	public void setType(String type)
	{
		this.type = type;
	}
	
	public String getType()
	{
		return type;
	}
	
	@XmlElement(name = "Version")
	public void setVersion(double version)
	{
		this.version = version;
	}
	
	public double getVersion()
	{
		return version;
	}
	
}
