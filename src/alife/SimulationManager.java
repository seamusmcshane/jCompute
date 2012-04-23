package alife;

import java.util.concurrent.Semaphore;

import org.newdawn.slick.Graphics;

/**
 * This class is the top level manager for the processing sequence a step in the simulation.
 * 
 * It manages the interaction between slick2d drawing and the simulation update loop. (via semaphore)
 * 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimulationManager
{
	/** Used to prevent dual Access to the lists - which would cause an exception.
	 * This semaphore is unfair.
	 * The more the drawAgentsAndPlants updates grabs it the lower step rate in simulation.
	 * The inverse doesn't happen, if the doSimulationUpdate grabs its,  the drawAgentsAndPlants will skip the attempt and draw the old buffer.
	 * */
	private Semaphore lock = new Semaphore(1,false); 
	
	/** Simulation Agent Manager */
	private SimpleAgentManager simpleAgentManager;
	
	/** Simulation Plant Manager */
	private GenericPlantManager genericPlantManager;
		
	/** Threads used for processing */
	private int num_threads=0;
	
	/** The threaded/barrier manager */
	private BarrierManager barrierManager;
	
	/** Controls when the barrier is released **/
	private Semaphore barrierControllerSemaphore;
	
	/**
	 * Constructor for SimulationManager.
	 * @param world_size int
	 * @param agent_prey_numbers int
	 * @param agent_predator_numbers int
	 * @param plant_numbers int
	 * @param plant_regen_rate int
	 * @param plantstartingenergy int
	 * @param plant_energy_absorption_rate int
	 * @param agentSettings SimpleAgentManagementSetupParam
	 */
	public SimulationManager(int world_size,int agent_prey_numbers,int agent_predator_numbers, int plant_numbers, int plant_regen_rate , int plantstartingenergy, int plant_energy_absorption_rate, SimpleAgentManagementSetupParam agentSettings)
	{	
		setUpBarrierManager();
		
		setUpPlantManager(world_size,plant_numbers,plant_regen_rate, plantstartingenergy, plant_energy_absorption_rate);

		setUpAgentManager(world_size,agent_prey_numbers,agent_predator_numbers,agentSettings);
	}
	
	/**
	 * Method setUpPlantManager.
	 * @param world_size int
	 * @param plant_numbers int
	 * @param plant_regen_rate int
	 * @param plantstartingenergy int
	 * @param plant_energy_absorption_rate int
	 */
	private void setUpPlantManager(int world_size,int plant_numbers,int plant_regen_rate,int plantstartingenergy, int plant_energy_absorption_rate)
	{
		genericPlantManager = new GenericPlantManager(barrierManager,world_size,plant_numbers,plant_regen_rate, plantstartingenergy, plant_energy_absorption_rate);		
	}
	
	/**
	 * Method setUpAgentManager.
	 * @param world_size int
	 * @param agent_prey_numbers int
	 * @param agent_predator_numbers int
	 * @param agentSettings SimpleAgentManagementSetupParam
	 */
	private void setUpAgentManager(int world_size,int agent_prey_numbers,int agent_predator_numbers,SimpleAgentManagementSetupParam agentSettings)
	{
		simpleAgentManager = new SimpleAgentManager(barrierManager,world_size,agent_prey_numbers,agent_predator_numbers, agentSettings);		
	}
	
	/**
	 * Sets up the barrier with the auto detected number of threads per processor.
	 */
	private void setUpBarrierManager()
	{
		this.num_threads = Runtime.getRuntime().availableProcessors(); // Ask Java how many CPU we can use
		
		System.out.println("Threads used for View Generation : " + num_threads);
		
		barrierControllerSemaphore = new Semaphore(1,true);
		
		barrierControllerSemaphore.acquireUninterruptibly();
		
		barrierManager = new BarrierManager(barrierControllerSemaphore,num_threads);
		
		barrierManager.start();	
	}
	
	// stage1 and stage3 could be run in parallel - stage 2 is already threaded 
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
		genericPlantManager.stage1();
		
		simpleAgentManager.stage1();
	}
	
	// View barrier operates in this method timeslot
	private void stage2()
	{
		// Prepare views
		genericPlantManager.stage2();
		simpleAgentManager.stage2();
		
		barrierControllerSemaphore.release();
		
		barrierControllerSemaphore.acquireUninterruptibly();	
		
	}
	
	private void stage3()
	{
		genericPlantManager.stage3();
		simpleAgentManager.stage3();
	}
	
	/**
	 * Method drawAgentsAndPlants.
	 * @param g Graphics
	 * @param true_drawing boolean
	 * @param view_range_drawing boolean
	 */
	public void drawAgentsAndPlants(Graphics g, boolean true_drawing,boolean view_range_drawing)
	{		
		// Get a lock on the done list, 
		// but don't wait if we cant get a lock 
		// so we can draw the old image buffer and stay interactive with regards moving the view.
		// Side effect is that the view could look static if processing power is very low.
		try
		{
			lock.acquire();
			
			genericPlantManager.drawPlants(g,true_drawing);
			
			simpleAgentManager.drawAgent(g,true_drawing,view_range_drawing);	
			
			// Release the lock on the done list
			lock.release();
		}
		catch (InterruptedException e)
		{
			// Debug System.out.println("Never Got Lock");
		}			

	}	
}
