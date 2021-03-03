package jcompute.configuration.batch;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Parameters")
public class ParametersConfig
{
	private List<ParameterConfig> parameterList;
	
	@XmlElement(name = "Parameter")
	public void setParameterList(List<ParameterConfig> parameterList)
	{
		this.parameterList = parameterList;
	}
	
	public List<ParameterConfig> getParameterList()
	{
		return parameterList;
	}
}
