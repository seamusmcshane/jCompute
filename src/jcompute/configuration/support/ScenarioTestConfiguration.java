package jcompute.configuration.support;

import javax.xml.bind.annotation.XmlRootElement;

import jcompute.configuration.JComputeConfiguration;
import jcompute.configuration.xml.Header;

@XmlRootElement(name = "Scenario")
public class ScenarioTestConfiguration extends JComputeConfiguration
{
	public ScenarioTestConfiguration()
	{
		
	}
	
	public ScenarioTestConfiguration(Header header)
	{
		super(header);
	}
}
