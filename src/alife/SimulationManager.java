package alife;

import java.util.concurrent.Semaphore;

import org.newdawn.slick.Graphics;

public class SimulationManager
{

	// Used to prevent dual Access to the lists - which would cause an exception
	Semaphore lock = new Semaphore(1,true);
	
	/** Simulation Agent Manager */
	public SimpleAgentManager simpleAgentManager;
	
	/** Simulation Plant Manager */
	public GenericPlantManager genericPlantManager;
	
	/* Draw slow but accurate circular bodies or faster rectangular ones */
	Boolean true_body_drawing = false;

	/** Toggle for Drawing agent field of views */
	Boolean draw_field_of_views = false;
	
	public SimulationManager(int world_size,int plant_numbers,int agent_numbers)
	{
		genericPlantManager = new GenericPlantManager(world_size,plant_numbers);
		
		simpleAgentManager = new SimpleAgentManager(world_size,agent_numbers);

		
		// TODO FIX
		simpleAgentManager.setTrueDrawing(true_body_drawing);

		simpleAgentManager.setFieldOfViewDrawing(draw_field_of_views);
	}
			
	public void doUpdate()
	{
		
		// Get a lock managers to prevent dual access by draw methods
		lock.acquireUninterruptibly();
		
		// Prepare
		stage1();
		
		// Distribute
		stage2();
		
		// Collect
		stage3();
		
		// Do Plants
		//genericPlantManager.updatePlants();
		
		// Do a Agent Step
		//simpleAgentManager.doAi();
		
		// Release the lock on the managers
		lock.release();
		
	}

	private void stage1()
	{
		//setup lists
		genericPlantManager.stage1();
		simpleAgentManager.stage1();

		//randomise
	}
	
	// View barrier operates in this method timeslot
	private void stage2()
	{
		// Prepare views
		genericPlantManager.stage2();
		simpleAgentManager.stage2();
		
	}
	
	private void stage3()
	{
		genericPlantManager.stage3();
		simpleAgentManager.stage3();

		// update lists
		// add new entities
	}
	
	public void drawAgentsAndPlants(Graphics g)
	{
		
		// Get a lock on the done list
		lock.acquireUninterruptibly();
		
		genericPlantManager.drawPlants(g);
		
		simpleAgentManager.drawAgent(g);	
		
		// Release the lock on the done list
		lock.release();
	}
	
}
