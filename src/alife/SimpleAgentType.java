package alife;

import alife.SimpleAgentEnum.AgentType;
/**
 * This Class does the Agent Type evaluations for the agents brains.
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
	
	/** Returns the type */
	public AgentType getType()
	{
		return type;
	}
	
	/** Compares the type */
	public boolean isSameType(SimpleAgentType type)
	{
		
		if(this.type == type.getType())
		{
			return true;
		}
		
		return false;
		
	}
	
	
	/**
	 * Enum Strength Evaluation.
	 * @param type
	 * @return
	 */
	public int strongerThan(SimpleAgentType type)
	{
		int eval = 0;
		if( (this.type == AgentType.PREDATOR ) && ( type.getType() == AgentType.PREDATOR ) )
		{
			eval = 0;
		}

		if( (this.type == AgentType.PREDATOR ) && ( type.getType() == AgentType.PREY ) )
		{
			eval = 1;
		}
			
		if( (this.type == AgentType.PREY ) && ( type.getType() == AgentType.PREDATOR ) )
		{
			eval = -1;
		}
		
		if( (this.type == AgentType.PREY ) && ( type.getType() == AgentType.PREY ) )
		{
			eval = 0;
		}
		
		return eval;
		
	}
	
	/** The Evaluation of if this type eats plants */
	public boolean eatsPlants()
	{
		boolean eval = false;
		
		if(this.type == AgentType.PREDATOR) // Predators do not eat plants in this simulation
		{
			eval = false;
		}
		else // Logical prey do
		{
			eval = true;
		}
		
		return eval;
		
	}
}
