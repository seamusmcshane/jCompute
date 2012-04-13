/**
 * Tests the SimpleAgentStats class
 */
package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import alife.SimpleAgentEnum.AgentType;
import alife.SimpleAgentStats;
import alife.SimpleAgentType;

public class SimpleAgentStatsTests
{
	SimpleAgentStats agentPreyStats;
	SimpleAgentStats agentPredatorStats;
	
	/** The movement cost of the agent before modification */
	float base_move_cost=0.025f;
		
	/** Agent movement speed */
	float max_speed=1f;
	
	/** Agent Size */
	float size=5f;
	
	/** Max Energy of Agent */
	float max_energy=100f;
	
	/** Current Energy of the agent */
	float energy=50f;
	
	/** The ability of this agent to consume energy */
	float digestive_efficency=0.50f;
	
	/** The threshold before this agent is hungry */
	float hungryThreshold=50f;
	
	/** View Range */
	float view_range=10f;
		
	/** Base cost of reproduction */
	float base_reproduction_cost=0.50f;
	
	/** The consumption rate of energy from what every the agent is eating (Current only plants) */
	float energy_consumption_rate=10f;
	
	/** This agents type Predator/Prey */
	SimpleAgentType predatorType = new SimpleAgentType(AgentType.PREDATOR);
	SimpleAgentType preyType = new SimpleAgentType(AgentType.PREY);
	
	/** The starting energy level */
	float starting_energy=50f;
	
	/** The ratio of energy to reproduction or survival */
	float reproduction_energy_division=0.50f;

	@Before
	public void setUp() throws Exception
	{
		// SimpleAgentStats(SimpleAgentType type,float ms, float sz, float se,float me, float ht, float vr, float bmc,float brc, float ecr, float de, float red)
		agentPredatorStats = new SimpleAgentStats(predatorType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);		
		agentPreyStats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);
	}

	/*
	 * Types
	 */
	@Test
	public void predatorIsPredator()
	{
		assertEquals(true,agentPredatorStats.getType().getType() == AgentType.PREDATOR);
	}
	
	@Test
	public void preyIsPrey()
	{
		assertEquals(true,agentPreyStats.getType().getType() == AgentType.PREY);
	}

	@Test
	public void agentHungerTest()
	{	
	
		/* Hunger is updated when the agent moves */
		agentPreyStats.decrementMoveEnergy();
		assertEquals(true,agentPreyStats.isHungry());
						
		agentPreyStats.addEnergy(10);		
		agentPreyStats.decrementMoveEnergy();
		assertEquals(false,agentPreyStats.isHungry());
		
	}
	
	@Test
	public void agentReproductionTests()
	{								
		assertEquals(false,agentPreyStats.canReproduce());
		
		agentPreyStats.addEnergy(200);
			
		assertEquals(false,agentPreyStats.canReproduce());
		
		agentPreyStats.addEnergy(201);

		assertEquals(true,agentPreyStats.canReproduce());		


	}	

	@Test
	public void agentStarvationTest()
	{			
		/* 50 energy - (399*(5*0.025)) or (0.125*399) ~ 49.875*/
		for(int i=0;i<399;i++)
		{
			// Make it run
			agentPreyStats.decrementMoveEnergy();
		}
		assertEquals(false,agentPreyStats.isDead());	
		
		agentPreyStats.decrementMoveEnergy();
		
		assertEquals(true,agentPreyStats.isDead());
		
	}
	
}
