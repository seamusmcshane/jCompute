package jcompute.configuration.shared;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Event")
@XmlType(propOrder =
{
	"name", "value"
})
public class Event
{
	private String name;
	
	private double value;
	
	public Event()
	{
		
	}
	
	public Event(String name, double value)
	{
		this.name = name;
		this.value = value;
	}
	
	@XmlElement(name = "Name")
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	@XmlElement(name = "Value")
	public void setValue(double value)
	{
		this.value = value;
	}
	
	public double getValue()
	{
		return value;
	}
}
