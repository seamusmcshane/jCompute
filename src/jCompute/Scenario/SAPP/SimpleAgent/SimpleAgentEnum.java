package jCompute.Scenario.SAPP.SimpleAgent;
/**
 * A Small Enumerate container class for Agents.
 * Used to avoid magic numbers for types and states.
 * Enums are treated as special case classes thus also have an author.
 *  
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentEnum
{
	/** 
	 * Agent Type Enum 
	 * @author Seamus McShane
	 * @version $Revision: 1.0 $
	 */
	public enum AgentType
	{
		INVALID,PREY, PREDATOR
	};

	/** 
	 * Agent State Enum 
	 * @author Seamus McShane
	 * @version $Revision: 1.0 $
	 */
	public enum AgentState
	{
		ROAM, HUNT, EVADE, GRAZE
	};

	/** 
	 * Agent Types Relative Strengths 
	 * @author Seamus McShane
	 * @version $Revision: 1.0 $
	 */
	public enum AgentEval
	{
		SAME, STRONGER, WEAKER
	};
}
