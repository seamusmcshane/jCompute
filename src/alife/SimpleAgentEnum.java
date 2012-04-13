package alife;
/**
 * A Small enumerate type class.
 * Used to avoid magic numbers for types and states.
 */
public class SimpleAgentEnum
{
	public enum AgentType{PREY,PREDATOR};
	
	public enum AgentState{ROAM,HUNT,EVADE,GRAZE};
	
	public enum AgentEval{SAME,STRONGER,WEAKER};
}
