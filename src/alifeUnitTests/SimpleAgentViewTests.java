package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.newdawn.slick.geom.Vector2f;

import alife.SimpleAgent;
import alife.SimpleAgentBody;
import alife.SimpleAgentEnum.AgentType;
import alife.SimpleAgentStats;
import alife.SimpleAgentType;
import alife.SimpleAgentView;

public class SimpleAgentViewTests
{
	/* Debug ID */
	int agentId = 1;
	
	/** Coordinates */
	float x=0;
	float y=0;	
	
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
	SimpleAgentType preyType = new SimpleAgentType(AgentType.PREY);
	
	/** The starting energy level */
	float starting_energy=50f;
	
	/** The ratio of energy to reproduction or survival */
	float reproduction_energy_division=0.50f;	
	
	/** Stats Object */
	SimpleAgentStats stats;	
	
	SimpleAgentView view;
	
	/** Agent Object */
	SimpleAgent nearestAgent;
	
	/** Agent Body */
	SimpleAgentBody currentAgentBody;
	Vector2f bodyPos;
	
	@Before
	public void setUp() throws Exception
	{		
		/* The Nearest Agent */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);		
				
		nearestAgent = new SimpleAgent(0, x, y, stats);
				
		/* The View of the current agent */
		view = new SimpleAgentView();
		
	}
	
	/*
	 * Test the setting and clearing of the Agent in view logic
	 */
	@Test
	public void nearestAgentSetAndClearedCorrectly()
	{		
		/* The Current Agents Body */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(0,-1);
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);
		
		/* No agent in view */
		assertEquals(false,view.hasAgentInView());

		/* Set agent inview */
		view.setAgentView(nearestAgent);

		/* Agent in view */
		assertEquals(true,view.hasAgentInView());
		
		/* Agent in view is correct object */
		assertEquals(true,nearestAgent.equals(view.getOriginalAgentRef()));
		
		/* Set agent inview */
		view.setAgentView(null);
		
		/* No agent in view */
		assertEquals(false,view.hasAgentInView());
		
		/* Agent in view is null */
		assertEquals(null,null,view.getOriginalAgentRef());	
		
	}
		
	/*
	 * Tests the retrieval of the direction towards the nearest agent
	 * y inverted on screen, so left and right are inverted here.  
	 * Nearest Agent is at 0,0
	 */
	@Test
	public void nearestAgentDirectionTowards()
	{
		float value = 0;
		
		/*
		 *  Towards Above
		 *  
		 *  */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(0,1); 
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);
		view.setAgentView(nearestAgent);		
		value = 0; // Up	
		System.out.println(" Direction TU" + view.towardsAgentDirection(currentAgentBody));				
		assertEquals(true,view.towardsAgentDirection(currentAgentBody) == value);
		
		/*
		 *  Towards Left
		 *  
		 * */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(-1,0); // Left
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);				
		value = 90; // Left
		view.setAgentView(nearestAgent);
		System.out.println(" Direction TL" + view.towardsAgentDirection(currentAgentBody));		
		assertEquals(true,view.towardsAgentDirection(currentAgentBody) == value);

		/*
		 *  Towards Right
		 *  
		 *  */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(1,0); // Right
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);				
		value = 270; // Right
		view.setAgentView(nearestAgent);
		System.out.println(" Direction TR" + view.towardsAgentDirection(currentAgentBody));				
		assertEquals(true,view.towardsAgentDirection(currentAgentBody) == value);	
		
		/*
		 *  Towards below
		 *  
		 *  */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(0,-1); // Below
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);
		value = 180; // Below
		view.setAgentView(nearestAgent);
		System.out.println(" Direction TD" + view.towardsAgentDirection(currentAgentBody));				
		assertEquals(true,view.towardsAgentDirection(currentAgentBody) == value);			
		

	}

	/*
	 * Tests the retrieval of the direction away from the nearest agent
	 * y inverted on screen, so left and right are inverted here.  
	 * Nearest Agent is at 0,0
	 */
	@Test
	public void nearestAgentDirectionAway()
	{		
		float value = 0;

		/*
		 *  Away from Above
		 *  
		 *  */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(0,-1); // Above (y inverted on screen)
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);
		view.setAgentView(nearestAgent);		
		value = 0; // Up	
		System.out.println(" Direction AU" + view.awayfromAgentDirection(currentAgentBody));				
		assertEquals(true,view.awayfromAgentDirection(currentAgentBody) == value);
		
		/*
		 *  Away from Left
		 *  
		 * */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(-1,0); // Left
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);				
		value = 270; // Right
		view.setAgentView(nearestAgent);
		System.out.println(" Direction AL" + view.awayfromAgentDirection(currentAgentBody));		
		assertEquals(true,view.awayfromAgentDirection(currentAgentBody) == value);

		/*
		 *  Away from Right
		 *  
		 *  */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(1,0); // Right
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);				
		value = 90; // Left
		view.setAgentView(nearestAgent);
		System.out.println(" Direction AR" + view.awayfromAgentDirection(currentAgentBody));				
		assertEquals(true,view.awayfromAgentDirection(currentAgentBody) == value);	
		
		/*
		 *  Away from below
		 *  
		 *  */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(0,-1); // Below
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);
		value = 0; // Up
		view.setAgentView(nearestAgent);
		System.out.println(" Direction AD" + view.awayfromAgentDirection(currentAgentBody));				
		assertEquals(true,view.awayfromAgentDirection(currentAgentBody) == value);	
	}
	
	/*
	 * Tests the retrieval of the distance to an agent
	 * Nearest Agent is at 0,0
	 * We are at 0,-1 
	 * Should return 1;
	 */
	@Test
	public void distanceToAgent()
	{		
		/* The Current Agents Body - below 0,0 */
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);				
		bodyPos = new Vector2f(0,-1); // Below
		currentAgentBody = new SimpleAgentBody(bodyPos,stats);
		
		view.setAgentView(nearestAgent);
				
		/* -90 Degrees is down */
		assertEquals(1,1,view.distanceTo(currentAgentBody.getBodyPos(), view.getNearestAgentPos()));
	}	
	
}
