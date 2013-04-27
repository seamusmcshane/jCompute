package alifeSim.Scenario;

public class ScenariosTypes
{
	/* Enum and String must be kept in sync (TODO enum property file/ Similar )*/
	private String supportedScenarios[] = { "INVALID", "SAPP" };	
	public enum Scenario
	{
		INVALID,SAPP,
	};
	
	public String scenarioEnumToString(Scenario type)
	{		
		return supportedScenarios[type.ordinal()];
	}
	
	/*
	 * Sequential Search
	 */
	public Scenario scenarioStringToEnum(String name)
	{		
		Scenario list[] = Scenario.values();
		
		for(int i=0;i<list.length;i++)
		{		
			if(supportedScenarios[i].equals(name))
			{
				return list[i];
			}
			
		}
		
		return Scenario.INVALID;
		
	}
	
}
