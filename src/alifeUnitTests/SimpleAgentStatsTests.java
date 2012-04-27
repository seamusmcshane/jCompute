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
	float baseMoveCost = 0.025f;

	/** Agent movement speed */
	float maxSpeed = 1f;

	/** Agent Size */
	float size = 5f;

	/** Max Energy of Agent */
	float maxEnergy = 100f;

	/** Starting Energy Energy of the agent */
	float energy = 50f;

	/** The ability of this agent to consume energy */
	float digestiveEfficency = 0.50f;

	/** The threshold before this agent is hungry */
	float hungryThreshold = 50f;

	/** View Range */
	float viewRange = 10f;

	/** Base cost of reproduction */
	float baseReproductionCost = 0.50f;

	/** The consumption rate of energy from what every the agent is eating (Current only plants) */
	float energyConsumptionRate = 10f;

	/** This agents type Predator/Prey */
	SimpleAgentType predatorType = new SimpleAgentType(AgentType.PREDATOR);
	SimpleAgentType preyType = new SimpleAgentType(AgentType.PREY);

	/** The starting energy level */
	float startingEnergy = 50f;

	/** The ratio of energy to reproduction or survival */
	float reproductionEnergyDivision = 0.50f;

	@Before
	public void setUp() throws Exception
	{
		agentPredatorStats = new SimpleAgentStats(predatorType, maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		agentPreyStats = new SimpleAgentStats(preyType, maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		System.out.println("====================================================");
		System.out.println("----------------------------------------------------");
	}

	@Test
	public void agentNotDead()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - agentNotDead");
		System.out.println("----------------------------------------------------");
		System.out.println("agentNotDead : " + agentPredatorStats.isDead() + " Should be : false");
		assertEquals(false, agentPredatorStats.isDead());
	}

	/*
	 * Types
	 */
	@Test
	public void predatorIsPredator()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - predatorIsPredator");
		System.out.println("----------------------------------------------------");
		System.out.println("predatorIsPredator : " + agentPredatorStats.getType().getType() + " Should be : " + AgentType.PREDATOR);
		assertEquals(true, agentPredatorStats.getType().getType() == AgentType.PREDATOR);
	}

	@Test
	public void preyIsPrey()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - preyIsPrey");
		System.out.println("----------------------------------------------------");
		System.out.println("preyIsPrey : " + agentPreyStats.getType().getType() + " Should be : " + AgentType.PREY);
		assertEquals(true, agentPreyStats.getType().getType() == AgentType.PREY);
	}

	@Test
	public void agentHungerTest()
	{

		System.out.println("----------------------------------------------------");
		System.out.println("Test - agentHungerTest");
		System.out.println("----------------------------------------------------");
		System.out.println("Hungry Threshold : " + hungryThreshold);

		/* Hunger is updated when the agent moves */
		agentPreyStats.decrementMoveEnergy();
		System.out.println("Hungry : " + agentPreyStats.isHungry() + " Should be : true");
		assertEquals(true, agentPreyStats.isHungry());

		System.out.println("Energy : " + agentPreyStats.getEnergy());
		/* Eat some food - not Hungry */
		agentPreyStats.addEnergy(10);
		System.out.println("Add Energy : " + agentPreyStats.getEnergy());

		agentPreyStats.decrementMoveEnergy();
		System.out.println("Hungry : " + agentPreyStats.isHungry() + " Should be : false");
		assertEquals(false, agentPreyStats.isHungry());

	}

	@Test
	public void agentReproductionTests()
	{

		System.out.println("----------------------------------------------------");
		System.out.println("Test - agentReproductionTests");
		System.out.println("----------------------------------------------------");
		System.out.println("Energy : " + agentPreyStats.getEnergy());
		System.out.println("REDiv  : " + agentPreyStats.getReproductionEnergyDivision());

		/* Cannot Reproduce */
		assertEquals(false, agentPreyStats.canReproduce());
		System.out.println("Can Reproduce : " + agentPreyStats.canReproduce() + " Should be : false");

		agentPreyStats.addEnergy(200);
		System.out.println("Energy : " + agentPreyStats.getEnergy());

		/* Still cannot */
		System.out.println("Can Reproduce : " + agentPreyStats.canReproduce() + " Should be : false");
		assertEquals(false, agentPreyStats.canReproduce());

		/* Reproduction threshold */
		agentPreyStats.addEnergy(1);
		System.out.println("Reached Threshold");

		/* Can Reproduce */
		System.out.println("Can Reproduce : " + agentPreyStats.canReproduce() + " Should be : true");
		assertEquals(true, agentPreyStats.canReproduce());

		/* Reproduce */
		System.out.println("Reproduced");
		agentPreyStats.decrementReproductionCost();

		/* Cannot Reproduce */
		System.out.println("Can Reproduce : " + agentPreyStats.canReproduce() + " Should be : false");
		assertEquals(false, agentPreyStats.canReproduce());

	}

	@Test
	public void agentStarvationTest()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - agentStarvationTest");
		System.out.println("----------------------------------------------------");
		System.out.println("Energy : " + agentPreyStats.getEnergy());

		System.out.println("Moving");

		/* 50 energy - (399*(5*0.025)) or (0.125*399) ~ 49.875 */
		for (int i = 0; i < 399; i++)
		{
			// Make it run
			agentPreyStats.decrementMoveEnergy();
			System.out.println("Energy : " + agentPreyStats.getEnergy());
		}
		System.out.println("agentNotDead : " + agentPreyStats.isDead() + " Should be : false");
		assertEquals(false, agentPreyStats.isDead());

		System.out.println("Last Move");
		agentPreyStats.decrementMoveEnergy();
		System.out.println("Energy : " + agentPreyStats.getEnergy());

		System.out.println("agentNotDead : " + agentPreyStats.isDead() + " Should be : true");
		assertEquals(true, agentPreyStats.isDead());

	}

	@Test
	public void agentKilledTest()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - agentKilledTest");
		System.out.println("----------------------------------------------------");

		System.out.println("agentNotDead : " + agentPreyStats.isDead() + " Should be : false");
		assertEquals(false, agentPreyStats.isDead());

		System.out.println("killAgent");
		agentPreyStats.killAgent();

		System.out.println("agentNotDead : " + agentPreyStats.isDead() + " Should be : true");
		assertEquals(true, agentPreyStats.isDead());
	}

	@Test
	public void baseMoveCostSetCorrectly()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - baseMoveCostSetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("baseMoveCostSetCorrectly : " + agentPreyStats.getBaseMoveCost() + " Should be : " + baseMoveCost);

		assertEquals(maxSpeed, maxSpeed, agentPreyStats.getBaseMoveCost());
	}

	@Test
	public void maxSpeedSetCorrectly()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - maxSpeedSetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("maxSpeed : " + agentPreyStats.getMaxSpeed() + " Should be : " + maxSpeed);

		assertEquals(true, maxSpeed == agentPreyStats.getMaxSpeed());
	}

	@Test
	public void sizeSetCorrectly()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - sizeSetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("size : " + agentPreyStats.getSize() + " Should be : " + size);
		assertEquals(true, size == agentPreyStats.getSize());
	}

	@Test
	public void startingEnergySetCorrectly()
	{
		System.out.println("Test - startingEnergySetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("energy : " + agentPreyStats.getStartingEnergy() + " Should be : " + energy);

		assertEquals(true, energy == agentPreyStats.getStartingEnergy());
	}

	@Test
	public void maxEnergySetCorrectly()
	{
		System.out.println("Test - maxEnergySetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("max_energy : " + agentPreyStats.getMaxEnergy() + " Should be : " + maxEnergy);
		assertEquals(true, maxEnergy == agentPreyStats.getMaxEnergy());
	}

	@Test
	public void hungryThresholdSetCorrectly()
	{
		System.out.println("Test - hungryThresholdSetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("hungryThreshold : " + agentPreyStats.getHungryThreshold() + " Should be : " + hungryThreshold);
		assertEquals(true, hungryThreshold == agentPreyStats.getHungryThreshold());
	}

	@Test
	public void baseViewRangeSetCorrectly()
	{
		System.out.println("Test - baseViewRangeSetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("view_range : " + agentPreyStats.getBaseViewRange() + " Should be : " + viewRange);
		assertEquals(true, viewRange == agentPreyStats.getBaseViewRange());
	}

	@Test
	public void viewRangeCalcCorrectly()
	{
		float value;
		System.out.println("Test - viewRangeCalcCorrectly");
		System.out.println("----------------------------------------------------");
		value = viewRange + size;
		System.out.println("view_range : " + agentPreyStats.getViewRange() + " Should be : " + (viewRange + size));
		assertEquals(true, agentPreyStats.getViewRange() == value);

		value = viewRange;
		System.out.println("BaseViewRange : " + agentPreyStats.getBaseViewRange() + " Should be : " + value);
		assertEquals(true, agentPreyStats.getBaseViewRange() == value);
	}

	@Test
	public void baseReproductionCostSetCorrectly()
	{
		System.out.println("Test - baseReproductionCostSetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("base_reproduction_cost : " + agentPreyStats.getBaseReproductionCost() + " Should be : " + baseReproductionCost);
		assertEquals(true, baseReproductionCost == agentPreyStats.getBaseReproductionCost());
	}

	@Test
	public void energyConsumptionRateCostSetCorrectly()
	{
		System.out.println("Test - energyConsumptionRateCostSetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("energy_consumption_rate : " + agentPreyStats.getStartingEnergy() + " Should be : " + energyConsumptionRate);
		assertEquals(true, energyConsumptionRate == agentPreyStats.getEnergyConsumptionRate());
	}

	@Test
	public void digestiveEfficencySetCorrectly()
	{
		System.out.println("Test - digestiveEfficencySetCorrectly");
		System.out.println("----------------------------------------------------");
		System.out.println("digestive_efficency : " + agentPreyStats.getDigestiveEfficency() + " Should be : " + digestiveEfficency);
		assertEquals(true, digestiveEfficency == agentPreyStats.getDigestiveEfficency());
	}

	@Test
	public void reproductionEnergyDivisionSetCorrectly()
	{
		System.out.println("Test - reproductionEnergyDivisionSetCorrectly");
		System.out.println("----------------------------------------------------");

		System.out.println("reproduction_energy_division : " + agentPreyStats.getReproductionEnergyDivision() + " Should be : " + reproductionEnergyDivision);
		assertEquals(true, reproductionEnergyDivision == agentPreyStats.getReproductionEnergyDivision());

	}
}