package alife;

import java.util.concurrent.Semaphore;

import org.newdawn.slick.Graphics;

public class SimulationManager
{

	// Used to prevent dual Access to the lists - which would cause an exception
	private Semaphore lock = new Semaphore(1,true);
	
	/** Simulation Agent Manager */
	private SimpleAgentManager simpleAgentManager;
	
	/** Simulation Plant Manager */
	private GenericPlantManager genericPlantManager;
		
	/** Threads used for processing */
	private int num_threads=0;
	
	/** The threaded/barrier manager */
	private ViewGeneratorManager viewGenerator;
	
	/** Controls when the barrier is released **/
	private Semaphore viewManagerSemaphore;
	
	public SimulationManager(int world_size,int agent_prey_numbers,int agent_predator_numbers, int plant_numbers, int plant_regen_rate , int plantstartingenergy, int plant_energy_absorption_rate, SimpleAgentManagementSetupParam agentSettings)
	{	
		setUpViewManager();
		
		setUpPlantManager(world_size,plant_numbers,plant_regen_rate, plantstartingenergy, plant_energy_absorption_rate);

		setUpAgentManager(world_size,agent_prey_numbers,agent_predator_numbers,agentSettings);
	}
	
	private void setUpPlantManager(int world_size,int plant_numbers,int plant_regen_rate,int plantstartingenergy, int plant_energy_absorption_rate)
	{
		genericPlantManager = new GenericPlantManager(viewGenerator,world_size,plant_numbers,plant_regen_rate, plantstartingenergy, plant_energy_absorption_rate);		
	}
	
	private void setUpAgentManager(int world_size,int agent_prey_numbers,int agent_predator_numbers,SimpleAgentManagementSetupParam agentSettings)
	{
		simpleAgentManager = new SimpleAgentManager(viewGenerator,world_size,agent_prey_numbers,agent_predator_numbers, agentSettings);		
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
	
	public void drawAgentsAndPlants(Graphics g, boolean true_drawing,boolean view_range_drawing)
	{
		
		// Get a lock on the done list
		lock.acquireUninterruptibly();
		
		genericPlantManager.drawPlants(g,true_drawing);
		
		simpleAgentManager.drawAgent(g,true_drawing,view_range_drawing);	
		
		// Release the lock on the done list
		lock.release();
	}
	
}
