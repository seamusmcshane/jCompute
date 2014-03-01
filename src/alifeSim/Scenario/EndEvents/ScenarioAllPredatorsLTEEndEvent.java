package alifeSim.Scenario.EndEvents;

public class ScenarioAllPredatorsLTEEndEvent implements ScenarioEndEventInf
{
	private String name = "AllPredatorsLTEEndEvent";
	private ScenarioAllPredatorsLTEEndEventInf predatorManager;
	private int triggerValue;
	
	public ScenarioAllPredatorsLTEEndEvent(ScenarioAllPredatorsLTEEndEventInf predatorManager,int triggerValue)
	{
		this.predatorManager = predatorManager;
		this.triggerValue = triggerValue;
	}

	@Override
	public boolean checkEvent()
	{
		return (predatorManager.getPredatorTotal() == triggerValue);
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
