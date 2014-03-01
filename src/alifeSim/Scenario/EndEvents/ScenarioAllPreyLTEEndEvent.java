package alifeSim.Scenario.EndEvents;

public class ScenarioAllPreyLTEEndEvent implements ScenarioEndEventInf
{
	private String name = "AllPreyLTEEndEvent";
	private ScenarioAllPreyLTEEndEventInf preyManager;
	private int triggerValue;
	
	public ScenarioAllPreyLTEEndEvent(ScenarioAllPreyLTEEndEventInf preyManager,int triggerValue)
	{
		this.preyManager = preyManager;
		this.triggerValue = triggerValue;
	}

	@Override
	public boolean checkEvent()
	{
		return (preyManager.getPreyTotal() == triggerValue);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getValue()
	{
		return triggerValue;
	}
}
