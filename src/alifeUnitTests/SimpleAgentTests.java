package alifeUnitTests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.newdawn.slick.geom.Vector2f;

import alife.SimpleAgent;
import alife.SimpleAgentBody;
import alife.SimpleAgentStats;
import alife.SimpleAgentType;
import alife.SimpleAgentView;
import alife.SimpleAgentEnum.AgentType;

public class SimpleAgentTests
{
	/* Debug ID */
	int agentId = 1;
	
	/** Coordinates */
	float x;
	float y;	
	
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
	SimpleAgentStats stats_ref;	


	/** Agent Object */
	SimpleAgent testAgent;

	
	@Before
	public void setUp() throws Exception
	{
		stats = new SimpleAgentStats(preyType,max_speed,size,energy,max_energy,hungryThreshold,view_range,base_move_cost,base_reproduction_cost,energy_consumption_rate,digestive_efficency,reproduction_energy_division);		
		
		stats_ref=stats;
		
		testAgent = new SimpleAgent(0, x, y, stats);
		
		stats = null;
	}

	@Test
	public void idIsCorrect()
	{
		assertEquals(agentId,agentId,testAgent.getAgentId());
	}

	@Test
	public void statsIsCorrectObject()
	{
		// Matches the reference
		assertEquals(true,testAgent.body.getStatsDebugMethod().equals(stats_ref));
		
		// Is not the null reference
		assertEquals(false,testAgent.body.getStatsDebugMethod().equals(stats));

	}
	
	@Test
	public void initialBodyPositionIsCorrect()
	{		
		assertEquals(true,testAgent.body.getBodyPos().getX() == x &&  testAgent.body.getBodyPos().getY() == y);
	}	
	
}
