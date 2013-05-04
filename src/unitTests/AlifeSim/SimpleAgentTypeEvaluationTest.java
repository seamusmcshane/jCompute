package unitTests.AlifeSim;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import alifeSim.Alife.SimpleAgent.SimpleAgentType;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentEval;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
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

		System.out.println("====================================================");
		System.out.println("----------------------------------------------------");
	}

	@Test
	public void predatorVsPredatorIsSameType()
	{
		System.out.println("predatorVsPredatorIsSameType : " + predator1.isSameType(predator2) + " Should be : true");
		assertEquals(true, predator1.isSameType(predator2));
	}

	@Test
	public void predatorVsPreyIsSameType()
	{
		System.out.println("predatorVsPreyIsSameType : " + predator1.isSameType(prey1) + " Should be : false");
		assertEquals(false, predator1.isSameType(prey1));
	}

	@Test
	public void preyVsPredatorIsSameType()
	{
		System.out.println("preyVsPredatorIsSameType : " + prey1.isSameType(predator1) + " Should be : false");
		assertEquals(false, prey1.isSameType(predator1));
	}

	@Test
	public void preYVsPreyIsSameType()
	{
		System.out.println("preyVsPredatorIsSameType : " + prey1.isSameType(prey2) + " Should be : true");
		assertEquals(true, prey1.isSameType(prey2));
	}

	@Test
	public void predatorEatsPlants()
	{
		System.out.println("predatorEatsPlants : " + predator1.eatsPlants() + " Should be : false");
		assertEquals(false, predator1.eatsPlants());
	}

	@Test
	public void preyEatsPlants()
	{
		System.out.println("preyEatsPlants : " + prey1.eatsPlants() + " Should be : true");
		assertEquals(true, prey1.eatsPlants());
	}

	@Test
	public void PredatorvsPredatorStrongerThanTests()
	{
		/* Predator vs Predator */
		System.out.println("PredatorvsPredatorStrongerThanTests : " + predator1.strongerThan(predator2) + " Should be : " + AgentEval.SAME);
		assertEquals(AgentEval.SAME, predator1.strongerThan(predator2));
	}

	@Test
	public void PreyVsPreyStrongerThanTests()
	{
		/* Prey vs Prey */
		System.out.println("PreyVsPreyStrongerThanTests : " + prey1.strongerThan(prey2) + " Should be : " + AgentEval.SAME);
		assertEquals(AgentEval.SAME, prey1.strongerThan(prey2));
	}

	@Test
	public void PredatorvsPreyStrongerThanTests()
	{
		/* Predator vs Prey */
		System.out.println("PredatorvsPreyStrongerThanTests : " + predator1.strongerThan(prey1) + " Should be : " + AgentEval.STRONGER);
		assertEquals(AgentEval.STRONGER, predator1.strongerThan(prey1));
	}

	@Test
	public void PreyvsPredatorStrongerThanTests()
	{
		/* Prey vs Predator */
		System.out.println("PreyvsPredatorStrongerThanTests : " + prey1.strongerThan(predator1) + " Should be : " + AgentEval.WEAKER);
		assertEquals(AgentEval.WEAKER, prey1.strongerThan(predator1));
	}

	@Test
	public void PredatorType()
	{
		System.out.println("PredatorType : " + predator1.getType() + " Should be : " + AgentType.PREDATOR);
		assertEquals(AgentType.PREDATOR, predator1.getType());
	}

	@Test
	public void PreyType()
	{
		System.out.println("PreyType : " + prey1.getType() + " Should be : " + AgentType.PREY);
		assertEquals(AgentType.PREY, prey1.getType());
	}

}
