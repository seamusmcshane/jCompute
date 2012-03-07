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
	
	// TODO make a GUI setting
	int num_threads=6;
	ViewGeneratorManager viewGenerator;
	Semaphore viewManagerSemaphore;
	
	public SimulationManager(int world_size,int plant_numbers,int agent_numbers)
	{
		
		setUpViewManager();
		
		setUpPlantManager(world_size,plant_numbers);

		setUpAgentManager(world_size,agent_numbers);

	}
	
	private void setUpPlantManager(int world_size,int plant_numbers)
	{
		genericPlantManager = new GenericPlantManager(viewGenerator,world_size,plant_numbers);		
	}
	
	private void setUpAgentManager(int world_size,int agent_numbers)
	{
		simpleAgentManager = new SimpleAgentManager(viewGenerator,world_size,agent_numbers);
		
		// TODO MAKE GUI SETTING
		simpleAgentManager.setTrueDrawing(true_body_drawing);

		simpleAgentManager.setFieldOfViewDrawing(draw_field_of_views);		
	}
	
	private void setUpViewManager()
	{
		this.num_threads = Runtime.getRuntime().availableProcessors(); // Ask Java how many CPU we can use
		
		System.out.println("Threads used for View Generation : " + num_threads);
		
		viewManagerSemaphore = new Semaphore(1,true);
		
		viewManagerSemaphore.acquireUninterruptibly();
		
		viewGenerator = new ViewGeneratorManager(viewManagerSemaphore,num_threads);
		
		viewGenerator.start();	
	}
	
	// Every stage1 and stage3 could be run in parallel - stage 2 is already threaded 
	public void doSimulationUpdate()
	{
		
		// Get a lock managers to prevent dual access by draw methods
		lock.acquireUninterruptibly();
		
		// Prepare
		stage1();
		
		// Distribute
		stage2();
		
		// Collect
		stage3();
		
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
		
		viewManagerSemaphore.release();
		
		viewManagerSemaphore.acquireUninterruptibly();	
		
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
