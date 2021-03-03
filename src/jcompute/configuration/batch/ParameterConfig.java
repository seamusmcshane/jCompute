package jcompute.configuration.batch;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Parameter")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
public class ParameterConfig
{
	private String type;	
	private String path;
	private String groupName;
	private String parameterName;
	
	private int initialVal;
	private int valIncrement;
	private int valCombinations;
	
	public ParameterConfig()
	{

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

	@XmlElement(name = "Path")
	public void setPath(String path)
	{
		this.path = path;
	}
	
	public String getPath()
	{
		return path;
	}
	
	@XmlElement(name = "GroupName")
	public void setGroupName(String groupName)
	{
		this.groupName = groupName;
	}
	
	public String getGroupName()
	{
		return groupName;
	}

	@XmlElement(name = "ParameterName")
	public void setParameterName(String parameterName)
	{
		this.parameterName = parameterName;
	}

	public String getParameterName()
	{
		return parameterName;
	}

	@XmlElement(name = "InitialVal")
	public void setInitialVal(int initialVal)
	{
		this.initialVal = initialVal;
	}
	
	public int getInitialVal()
	{
		return initialVal;
	}

	@XmlElement(name = "ValIncrement")
	public void setValIncrement(int valIncrement)
	{
		this.valIncrement = valIncrement;
	}
	
	public int getValIncrement()
	{
		return valIncrement;
	}

	@XmlElement(name = "ValCombinations")
	public void setValCombinations(int valCombinations)
	{
		this.valCombinations = valCombinations;
	}
	
	public int getValCombinations()
	{
		return valCombinations;
	}
}
