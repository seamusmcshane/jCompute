package alife;

import alife.SimulationEnums.AgentType;

public class SimpleAgentType
{

	private AgentType type;
	
	public SimpleAgentType(AgentType type)
	{
		this.type = type;
	}
	
	public AgentType getType()
	{
		return type;
	}
	
	public boolean compareType(SimpleAgentType type)
	{
		
		if(this.type == type.getType())
		{
			return true;
		}
		
		return false;
		
	}
	
	
	// Enum Strength Evaluations (Tri-Logic)
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
