package alifeUnitTests;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import alife.SimpleAgentEnum.AgentEval;
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
	
	SimpleAgentType predator1;
	SimpleAgentType prey1;
	SimpleAgentType predator2;
	SimpleAgentType prey2;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		
	}

	@Before
	public void setUp() throws Exception
	{
		predator1 = new SimpleAgentType(AgentType.PREDATOR);
		prey1 = new SimpleAgentType(AgentType.PREY);
		predator2 = new SimpleAgentType(AgentType.PREDATOR);
		prey2 = new SimpleAgentType(AgentType.PREY);		
	}
	
	@Test
	public void predatorVsPredatorIsSameType()
	{
		assertEquals(true,predator1.isSameType(predator2));
	}
	
	@Test
	public void predatorVsPreyIsSameType()
	{
		assertEquals(false,predator1.isSameType(prey1));
	}
	
	
	@Test
	public void preyVsPredatorIsSameType()
	{
		assertEquals(false,prey1.isSameType(predator1));
	}
	
	
	@Test
	public void preYVsPreyIsSameType()
	{
		assertEquals(true,prey1.isSameType(prey2));
	}

	@Test
	public void predatorEatsPlants()
	{
		assertEquals(false,predator1.eatsPlants());
	}	
	
	@Test
	public void preyEatsPlants()
	{
		assertEquals(true,prey1.eatsPlants());
	}
	
	@Test
	public void PredatorvsPredatorStrongerThanTests()
	{
		/* Predator vs Predator */
		assertEquals(AgentEval.SAME,predator1.strongerThan(predator2));
	}
	
	
	@Test
	public void PreyVsPreyStrongerThanTests()
	{
		/* Prey vs Prey */
		assertEquals(AgentEval.SAME,prey1.strongerThan(prey2));
	}
	
	
	@Test
	public void PredatorvsPreyStrongerThanTests()
	{		
		/* Predator vs Prey */
		assertEquals(AgentEval.STRONGER,predator1.strongerThan(prey1));
	}
	
	
	@Test
	public void PreyvsPredatorStrongerThanTests()
	{		
		/* Prey vs Predator */
		assertEquals(AgentEval.WEAKER,prey1.strongerThan(predator1));
	}
	
	@Test
	public void PredatorType()
	{
		assertEquals(AgentType.PREDATOR,predator1.getType());
	}
	
	@Test
	public void PreyType()
	{
		assertEquals(AgentType.PREY,prey1.getType());
	}
	

}
