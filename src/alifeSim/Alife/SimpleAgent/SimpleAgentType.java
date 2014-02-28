package alifeSim.Alife.SimpleAgent;

import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentEval;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
/**
 * This Class does the Agent Type evaluations for the agents brains.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentType
{
	/** The type enum */
	private AgentType type;

	/**
	 * Creates a type.
	 * @param type
	 */
	public SimpleAgentType(AgentType type)
	{
		this.type = type;
	}

	/** Returns the type 
	 * @return AgentType */
	public AgentType getType()
	{
		return type;
	}

	/** Compares the type 
	 * @param type SimpleAgentType
	 * @return boolean */
	public boolean isSameType(SimpleAgentType type)
	{

		if (this.type == type.getType())
		{
			return true;
		}

		return false;

	}

	/**
	 * Enum Strength Evaluation.
	 * @param type
	 * @return AgentEval */
	public AgentEval strongerThan(SimpleAgentType type)
	{
		AgentEval eval = AgentEval.SAME;

		if(type == null)
		{
			return null;
		}
		
		if ((this.type == AgentType.PREDATOR) && (type.getType() == AgentType.PREDATOR))
		{
			eval = AgentEval.SAME;
		}

		if ((this.type == AgentType.PREDATOR) && (type.getType() == AgentType.PREY))
		{
			eval = AgentEval.STRONGER;
		}

		if ((this.type == AgentType.PREY) && (type.getType() == AgentType.PREDATOR))
		{
			eval = AgentEval.WEAKER;
		}

		if ((this.type == AgentType.PREY) && (type.getType() == AgentType.PREY))
		{
			eval = AgentEval.SAME;
		}

		return eval;

	}

	/** The Evaluation of if this type eats plants 
	 * @return boolean */
	public boolean eatsPlants()
	{
		boolean eval = false;

		if (this.type == AgentType.PREDATOR) // Predators do not eat plants in this simulation
		{
			eval = false;
		}
		else
		// Logical prey do
		{
			eval = true;
		}

		return eval;

	}
	
	
	public AgentType typeFromString(String type)
	{
		if(type.equalsIgnoreCase("Predator"))
		{
			return AgentType.PREDATOR;
		}
		else if (type.equalsIgnoreCase("Prey"))
		{
			return AgentType.PREY;
		}
		else
		{
			return AgentType.INVALID;
		}
	}
}
