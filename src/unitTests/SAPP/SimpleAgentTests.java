package unitTests.SAPP;

import static org.junit.Assert.*;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgent;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentStats;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentType;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentEnum.AgentType;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

public class SimpleAgentTests
{
	/* Debug ID */
	int agentId = 1;

	/** Coordinates */
	float x;
	float y;

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
	SimpleAgentType preyType = new SimpleAgentType(AgentType.PREY);

	/** The starting energy level */
	float startingEnergy = 50f;

	/** The ratio of energy to reproduction or survival */
	float reproductionEnergyDivision = 0.50f;

	/** Stats Object */
	SimpleAgentStats stats;
	SimpleAgentStats statsRef;

	/** Agent Object */
	SimpleAgent testAgent;

	@Before
	public void setUp() throws Exception
	{
		stats = new SimpleAgentStats(preyType,new Color(255,0,0), maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);

		statsRef = stats;

		testAgent = new SimpleAgent(null,agentId, x, y, stats);

		stats = null;
		System.out.println("====================================================");
		System.out.println("----------------------------------------------------");
	}

	@Test
	public void idIsCorrect()
	{
		System.out.println("Test - agentidIsCorrect");
		System.out.println("----------------------------------------------------");
		System.out.println("agentidIsCorrect : " + testAgent.getAgentId() + " Should be : " + agentId);
		assertEquals(true, agentId == testAgent.getAgentId());
	}

	@Test
	public void statsIsCorrectObject()
	{
		System.out.println("Test - statsIsCorrectObject");
		System.out.println("----------------------------------------------------");

		// Matches the reference
		System.out.println("statsIsCorrectObject : " + testAgent.body.getStatsDebugMethod().equals(statsRef) + " Should be : true");
		assertEquals(true, testAgent.body.getStatsDebugMethod().equals(statsRef)); // actuall ref

		// Is not the null reference	
		System.out.println("Removed Reference");
		assertEquals(false, testAgent.body.getStatsDebugMethod().equals(stats)); // Null ref
		System.out.println("statsIsCorrectObject : " + testAgent.body.getStatsDebugMethod().equals(stats) + " Should be : false");
	}

	@Test
	public void initialBodyPositionIsCorrect()
	{
		System.out.println("Test - initialBodyPositionIsCorrect");
		System.out.println("----------------------------------------------------");
		System.out.println("initialBodyPositionIsCorrect : " + (testAgent.body.getBodyPos().getX() == x && testAgent.body.getBodyPos().getY() == y) + " Should be : true");
		assertEquals(true, testAgent.body.getBodyPos().getX() == x && testAgent.body.getBodyPos().getY() == y);
	}

}