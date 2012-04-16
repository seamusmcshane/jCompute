package alifeUnitTests;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import alife.SimpleAgentManagementSetupParam;
/**
 * 
 * Simple Agent Management Setup Param Tests
 * This set of tests, ensures that the parameters can be correctly set and recovered from the 
 * SimpleAgentManagementSetupParam object.
 */
public class SimpleAgentManagementSetupParamTests
{

	SimpleAgentManagementSetupParam params;
	int max_value = 1000;
	
	
	/* Value Generator */
	Random r;
	
	@Before
	public void setUp() throws Exception
	{
		params = new SimpleAgentManagementSetupParam();
		r = new Random();
	}

	/* Setting and getting of speeds */
	@Test
	public void predatorSpeed()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredatorSpeed(value);
		
		assertEquals(true,params.getPredatorSpeed() == value);	
	}
	
	@Test
	public void preySpeed()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreySpeed(value);
		
		assertEquals(true,params.getPreySpeed() == value);
	
	}
	
	/* Setting and getting of View range */

	@Test
	public void predatorViewRange()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredatorViewRange(value);
		
		assertEquals(true,params.getPredatorViewRange() == value);
	}
	
	@Test
	public void preyViewRange()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyViewRange(value);
		
		assertEquals(true,params.getPreyViewRange() == value);
	}	
	
	/* Setting and getting of DE */
	@Test
	public void predatorDE()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredatorDE(value);
		
		assertEquals(true,params.getPredatorDE() == value);
	}
	
	@Test
	public void preyDE()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyDE(value);
		
		assertEquals(true,params.getPreyDE() == value);
	}
	
	/* Setting and getting of REDiv */
	@Test
	public void predatorREDiv()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredatorREDiv(value);
		
		assertEquals(true,params.getPredatorREDiv() == value);
	}
	
	@Test
	public void preyREDiv()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyREDiv(value);
		
		assertEquals(true,params.getPreyREDiv() == value);
	}
	
	/* Setting and getting of MoveCost */
	@Test
	public void predatorMoveCost()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredatorMoveCost(value);
		
		assertEquals(true,params.getPredatorMoveCost() == value);
	}
	
	@Test
	public void preyMoveCost()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyMoveCost(value);
		
		assertEquals(true,params.getPreyMoveCost() == value);
	}
	
	/* Setting and getting of HungerThres */
	@Test
	public void predatorHungerThres()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredatorHungerThres(value);
		
		assertEquals(true,params.getPredatorHungerThres() == value);
	}
	
	@Test
	public void preyHungerThres()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyHungerThres(value);
		
		assertEquals(true,params.getPreyHungerThres() == value);
	}
	
	/* Setting and getting of ConsumptionRate */
	@Test
	public void predatorConsumptionRate()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredatorConsumptionRate(value);
		
		assertEquals(true,params.getPredatorConsumptionRate() == value);
	}
	
	@Test
	public void preyConsumptionRate()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyConsumptionRate(value);
		
		assertEquals(true,params.getPreyConsumptionRate() == value);
	}
	
	/* Setting and getting of RepoCost */
	@Test
	public void predatorRepoCost()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredRepoCost(value);
		
		assertEquals(true,params.getPredRepoCost() == value);
	}
	
	@Test
	public void preyRepoCost()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyRepoCost(value);
		
		assertEquals(true,params.getPreyRepoCost() == value);
	}
	
	/* Setting and getting of StartingEnergy */
	@Test
	public void predatorStartingEnergy()
	{
		float value = r.nextInt(max_value)+1;
		params.setPredStartingEnergy(value);
		
		assertEquals(true,params.getPredStartingEnergy() == value);
	}
	
	@Test
	public void preyStartingEnergy()
	{
		float value = r.nextInt(max_value)+1;
		params.setPreyStartingEnergy(value);
		
		assertEquals(true,params.getPreyStartingEnergy() == value);
	}
	
}
