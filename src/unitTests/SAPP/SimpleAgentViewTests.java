package unitTests.SAPP;

import static org.junit.Assert.*;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgent;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentBody;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentStats;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentType;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentView;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentEnum.AgentType;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

public class SimpleAgentViewTests
{
	/* Debug ID */
	int agentId = 1;

	/** Coordinates */
	float x = 0;
	float y = 0;

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

	SimpleAgentView view;

	/** Agent Object */
	SimpleAgent nearestAgent;

	/** Agent Body */
	SimpleAgentBody currentAgentBody;
	A2DVector2f bodyPos;

	@Before
	public void setUp() throws Exception
	{
		/* The Nearest Agent */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);

		nearestAgent = new SimpleAgent(null,0, x, y, stats);

		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);

		System.out.println("====================================================");
		System.out.println("----------------------------------------------------");

	}

	/*
	 * Test the setting and clearing of the Agent in view logic
	 */
	@Test
	public void nearestAgentSetAndClearedCorrectly()
	{
		System.out.println("----------------------------------------------------");
		System.out.println("Test - nearestAgentSetAndClearedCorrectly");
		System.out.println("----------------------------------------------------");

		/* The Current Agents Body */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(0, -1);
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		
		/* The View of the current agent */
		view = new SimpleAgentView(currentAgentBody);
		
		/* No agent in view */
		System.out.println("hasAgentInView :" + view.hasAgentInView() + " should be : false");
		assertEquals(false, view.hasAgentInView());

		/* Set agent inview */
		System.out.println("setAgentView : nearestAgent");
		view.setAgentView(nearestAgent);

		/* Agent in view */
		System.out.println("hasAgentInView :" + view.hasAgentInView() + " should be : true");
		assertEquals(true, view.hasAgentInView());

		/* Agent in view is correct object */
		System.out.println("Agent in view is correct object : " + nearestAgent.equals(view.getOriginalAgentRef()) + " should be : true");
		assertEquals(true, nearestAgent.equals(view.getOriginalAgentRef()));

		/* Set agent inview */
		System.out.println("setAgentView : null");
		view.setAgentView(null);

		/* No agent in view */
		System.out.println("hasAgentInView :" + view.hasAgentInView() + " should be : false");
		assertEquals(false, view.hasAgentInView());

		/* Agent in view is null */
		System.out.println("Agent in view is null : " + view.getOriginalAgentRef() + " should be : null");
		assertEquals(null, null, view.getOriginalAgentRef());

	}

	/*
	 * Tests the retrieval of the Towards Direction owards the nearest agent y
	 * inverted on screen. Nearest Agent is at 0,0
	 */
	@Test
	public void nearestAgentDirectionTowards()
	{
		float value = 0;
		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("Test - nearestAgentDirectionTowards");
		System.out.println("====================================================");

		/*
		 * Towards Above
		 */

		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(0, 1);
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		
		/* The View of the current agent */
		view = new SimpleAgentView(currentAgentBody);
		
		view.setAgentView(nearestAgent);
		value = 0; // Up	
		drawGrid(0, 0, 0, 1);
		System.out.println("Towards Direction " + view.towardsAgentDirection(currentAgentBody));
		assertEquals(true, view.towardsAgentDirection(currentAgentBody) == value);
		System.out.println("-----------------------------------------------------");

		/*
		 * Towards Left
		 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0), maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(-1, 0); // Left
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		value = 90; // Left
		view.setAgentView(nearestAgent);
		drawGrid(0, 0, -1, 0);
		System.out.println("Towards Direction " + view.towardsAgentDirection(currentAgentBody));
		assertEquals(true, view.towardsAgentDirection(currentAgentBody) == value);
		System.out.println("-----------------------------------------------------");

		/*
		 * Towards Right
		 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(1, 0); // Right
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		value = 270; // Right
		view.setAgentView(nearestAgent);
		drawGrid(0, 0, 1, 0);
		System.out.println("Towards Direction " + view.towardsAgentDirection(currentAgentBody));
		assertEquals(true, view.towardsAgentDirection(currentAgentBody) == value);

		/*
		 * Towards below
		 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(0, -1); // we are above
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		value = 180; // Below
		view.setAgentView(nearestAgent);
		drawGrid(0, 0, 0, -1);
		System.out.println("Towards Direction " + view.towardsAgentDirection(currentAgentBody));
		assertEquals(true, view.towardsAgentDirection(currentAgentBody) == value);
		System.out.println("-----------------------------------------------------");

	}

	/*
	 * Tests the retrieval of the Away Direction way from the nearest agent y
	 * inverted on screen. Nearest Agent is at 0,0
	 */
	@Test
	public void nearestAgentDirectionAway()
	{
		float value = 0;
		System.out.println("====================================================");
		System.out.println("====================================================");
		System.out.println("Test - nearestAgentDirectionAway");
		System.out.println("====================================================");

		/*
		 * Away from Above
		 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(0, 1); // Below
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		
		/* The View of the current agent */
		view = new SimpleAgentView(currentAgentBody);
		
		view.setAgentView(nearestAgent);
		value = 180; // away from Up is Down	
		drawGrid(0, 0, 0, 1);
		System.out.println("Away Direction " + view.awayfromAgentDirection(currentAgentBody));
		assertEquals(true, view.awayfromAgentDirection(currentAgentBody) == value);
		System.out.println("-----------------------------------------------------");

		/*
		 * Away from Right
		 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(-1, 0); // we are Left
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		value = 270; // away from Right is left
		view.setAgentView(nearestAgent);
		drawGrid(0, 0, -1, 0);
		System.out.println("Away Direction " + view.awayfromAgentDirection(currentAgentBody));
		assertEquals(true, view.awayfromAgentDirection(currentAgentBody) == value);
		System.out.println("-----------------------------------------------------");

		/*
		 * Away from Right
		 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(1, 0); // we are Right
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		value = 90; // away from left is right
		view.setAgentView(nearestAgent);
		drawGrid(0, 0, 1, 0);
		System.out.println("Away Direction " + view.awayfromAgentDirection(currentAgentBody));
		assertEquals(true, view.awayfromAgentDirection(currentAgentBody) == value);
		System.out.println("-----------------------------------------------------");

		/*
		 * Away from below
		 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(0, 1); // we are Below
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);
		value = 180; // away from up is down	
		view.setAgentView(nearestAgent);
		drawGrid(0, 0, 0, 1);
		System.out.println("Away Direction " + view.awayfromAgentDirection(currentAgentBody));
		assertEquals(true, view.awayfromAgentDirection(currentAgentBody) == value);
		System.out.println("-----------------------------------------------------");

	}

	/*
	 * Tests the retrieval of the distance to an agent Nearest Agent is at 0,0
	 * We are at 0,-1 Should return 1;
	 */
	@Test
	public void distanceToAgent()
	{
		/* The Current Agents Body - below 0,0 */
		stats = new SimpleAgentStats(preyType, new Color(255,0,0),maxSpeed, size, energy, maxEnergy, hungryThreshold, viewRange, baseMoveCost, baseReproductionCost, energyConsumptionRate, digestiveEfficency, reproductionEnergyDivision);
		bodyPos = new A2DVector2f(0, -1); // Below
		currentAgentBody = new SimpleAgentBody(null,bodyPos, stats);

		/* The View of the current agent */
		view = new SimpleAgentView(currentAgentBody);
		
		view.setAgentView(nearestAgent);

		/* -90 Degrees is down */
		assertEquals(1, 1, view.distanceTo(currentAgentBody.getBodyPos(), view.getNearestAgentPos()));
	}

	// Target X,Y then Your X,Y
	public void drawGrid(int x1, int y1, int x2, int y2)
	{
		int size = 3;
		System.out.println("----------");
		System.out.println("Position Scenario");
		System.out.println("----------");
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				if (j == x1 + 1 && i == y1 + 1)
				{
					System.out.print("[T]");
				}
				else
					if (j == x2 + 1 && i == y2 + 1)
					{
						System.out.print("[A]");
					}
					else
					{
						System.out.print("[ ]");
					}
			}
			System.out.print("\n");
		}
		System.out.println("----------");
		System.out.println("T = Target Agent  " + x1 + " " + y1);
		System.out.println("A = Agent Positon " + x2 + " " + y2);
		System.out.println("----------");

	}

}
