package alife;
/**
 * A Small Enumerate container class for Agents.
 * Used to avoid magic numbers for types and states.
 */
public class SimpleAgentEnum
{
	/** Agent Type Enum */
	public enum AgentType{PREY,PREDATOR};
	
	/** Agent State Enum */
	public enum AgentState{ROAM,HUNT,EVADE,GRAZE};
	
	/** Agent Types Relative Strengths */
	public enum AgentEval{SAME,STRONGER,WEAKER};
}
