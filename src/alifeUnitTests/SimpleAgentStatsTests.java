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
	
	/** The movement cost of the agent */
	float base_move_cost=0.025f;
		
	/** Agent movement speed */
	float max_speed=1f;
	
	/** Agent Size */
	float size=5f;
	
	/** Max Energy of Agent */
	float max_energy=100f;
	
	/** Starting Energy Energy of the agent */
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
		agentPredatorStats = new SimpleAgentStats(predatorType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);		
		agentPreyStats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);
	}

	
	@Test
	public void notDead()
	{
		assertEquals(false,agentPredatorStats.isDead());
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
						
		/* Eat some food - not Hungry */
		agentPreyStats.addEnergy(10);		
		agentPreyStats.decrementMoveEnergy();
		assertEquals(false,agentPreyStats.isHungry());
		
	}
	
	@Test
	public void agentReproductionTests()
	{								
		/* Cannot Reproduce */
		assertEquals(false,agentPreyStats.canReproduce());		
		agentPreyStats.addEnergy(200);
			
		/* Still cannot */
		assertEquals(false,agentPreyStats.canReproduce());
		
		/* Reproduction threshold */
		agentPreyStats.addEnergy(1);

		/* Can Reproduce */
		assertEquals(true,agentPreyStats.canReproduce());	
		
		/* Reproduce */
		agentPreyStats.decrementReproductionCost();
		
		/* Cannot Reproduce */
		assertEquals(false,agentPreyStats.canReproduce());			

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
	
	@Test
	public void agentKilledTest()
	{		
		assertEquals(false,agentPreyStats.isDead());
		agentPreyStats.killAgent();		
		assertEquals(true,agentPreyStats.isDead());
	}	
	
	@Test
	public void speedSetCorrectly()
	{
		assertEquals(max_speed,max_speed,agentPreyStats.getBaseMoveCost());
	}
	
	@Test
	public void sizeSetCorrectly()
	{
		assertEquals(size,size,agentPreyStats.getSize());
	}	
	
	@Test
	public void startingEnergySetCorrectly()
	{
		assertEquals(energy,energy,agentPreyStats.getStartingEnergy());
	}	
	
	@Test
	public void maxEnergySetCorrectly()
	{
		assertEquals(max_energy,max_energy,agentPreyStats.getEnergy());
	}	
		
	@Test
	public void hungryThresholdSetCorrectly()
	{
		assertEquals(hungryThreshold,hungryThreshold,agentPreyStats.getHungryThreshold());
	}	
	
	@Test
	public void baseViewRangeSetCorrectly()
	{
		assertEquals(view_range,view_range,agentPreyStats.getBaseViewRange());
	}	
	
	@Test
	public void viewRangeCalcCorrectly()
	{
		assertEquals(view_range-size,view_range-size,agentPreyStats.getViewRange());
		
		assertEquals(view_range*view_range,view_range*view_range,agentPreyStats.getViewRangeSquared());
	}
	
	@Test
	public void baseReproductionCostSetCorrectly()
	{
		assertEquals(base_reproduction_cost,base_reproduction_cost,agentPreyStats.getHungryThreshold());
	}	

	@Test
	public void energyConsumptionRateCostSetCorrectly()
	{
		assertEquals(energy_consumption_rate,energy_consumption_rate,agentPreyStats.getHungryThreshold());
	}	
	
	@Test
	public void digestiveEfficencySetCorrectly()
	{
		assertEquals(digestive_efficency,digestive_efficency,agentPreyStats.getHungryThreshold());
	}	
	
	@Test
	public void reproductionEnergyDivisionCostSetCorrectly()
	{
		assertEquals(reproduction_energy_division,reproduction_energy_division,agentPreyStats.getHungryThreshold());
	}	
}