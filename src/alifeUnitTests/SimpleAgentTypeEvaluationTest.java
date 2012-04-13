package alifeUnitTests;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import alife.SimpleAgentEnum.AgentType;
import alife.SimpleAgentType;
import alife.World;
/**
 * 
 * Simple Agent Type Evaluation Tests
 *
 */

public class SimpleAgentTypeEvaluationTest
{
	SimpleAgentType agent1;
	SimpleAgentType agent2;
	SimpleAgentType agent3;
	SimpleAgentType agent4;

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		
	}

	@Before
	public void setUp() throws Exception
	{
		agent1 = new SimpleAgentType(AgentType.PREDATOR);
		agent2 = new SimpleAgentType(AgentType.PREY);
		agent3 = new SimpleAgentType(AgentType.PREDATOR);
		agent4 = new SimpleAgentType(AgentType.PREY);		
	}
	
	@Test
	public void predatorVsPredatorIsSameType()
	{
		assertEquals(true,agent1.isSameType(agent3));
	}
	
	@Test
	public void predatorVsPreyIsSameType()
	{
		assertEquals(false,agent1.isSameType(agent2));
	}
	
	
	@Test
	public void preyVsPredatorIsSameType()
	{
		assertEquals(false,agent2.isSameType(agent1));
	}
	
	
	@Test
	public void preYVsPreyIsSameType()
	{
		assertEquals(true,agent2.isSameType(agent4));
	}

	@Test
	public void predatorEatsPlants()
	{
		assertEquals(false,agent1.eatsPlants());
	}	
	
	@Test
	public void preyEatsPlants()
	{
		assertEquals(true,agent2.eatsPlants());
	}
	
	@Test
	public void PredatorvsPredatorStrongerThanTests()
	{
		/* Predator vs Predator */
		assertEquals(0,agent1.strongerThan(agent3));
	}
	
	
	@Test
	public void PreyVsPreyStrongerThanTests()
	{
		/* Prey vs Prey */
		assertEquals(0,agent2.strongerThan(agent4));
	}
	
	
	@Test
	public void PredatorvsPreyStrongerThanTests()
	{		
		/* Predator vs Prey */
		assertEquals(1,agent1.strongerThan(agent2));
	}
	
	
	@Test
	public void PreyvsPredatorStrongerThanTests()
	{		
		/* Prey vs Predator */
		assertEquals(-1,agent2.strongerThan(agent1));
	}
	
	@Test
	public void PredatorType()
	{
		assertEquals(AgentType.PREDATOR,agent1.getType());
	}
	
	@Test
	public void PreyType()
	{
		assertEquals(AgentType.PREY,agent2.getType());
	}
	

}
