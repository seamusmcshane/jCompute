package jcompute.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import jcompute.configuration.xml.Header;

@XmlTransient
@XmlRootElement(name = "Invalid")
public abstract class JComputeConfiguration
{
	private Header header;
	
	public JComputeConfiguration()
	{
		
	}
	
	public JComputeConfiguration(Header header)
	{
		this.header = header;
	}
	
	@XmlElement(name = "Header")
	public void setHeader(Header header)
	{
		this.header = header;
	}
	
	public Header getHeader()
	{
		return header;
	}
}
