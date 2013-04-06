package alifeSim.Simulation;

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
	private Semaphore lock = new Semaphore(1, false);

	/** Simulation Agent Manager */
	private SimpleAgentManager simpleAgentManager;
	
	/** Simulation Plant Manager */
	private GenericPlantManager genericPlantManager;

	/** Threads used for processing */
	private int numThreads = 0;

	/** The threaded/barrier manager */
	private BarrierManager barrierManager;

	/** Controls when the barrier is released **/
	private Semaphore barrierControllerSemaphore;

	/**
	 * Constructor for SimulationManager.
	 * @param worldSize int
	 * @param agentPreyNumbers int
	 * @param agentPredatorNumbers int
	 * @param plantNumbers int
	 * @param plantRegenRate int
	 * @param plantStartingEnergy int
	 * @param plantEnergyAbsorptionRate int
	 * @param agentSettings SimpleAgentManagementSetupParam
	 */
	public SimulationManager(int worldSize, int agentPreyNumbers, int agentPredatorNumbers, int plantNumbers, int plantRegenRate, int plantStartingEnergy, int plantEnergyAbsorptionRate, SimpleAgentManagementSetupParam agentSettings)
	{
		setUpBarrierManager();

		setUpPlantManager(worldSize, plantNumbers, plantRegenRate, plantStartingEnergy, plantEnergyAbsorptionRate);

		setUpAgentManager(worldSize, agentPreyNumbers, agentPredatorNumbers, agentSettings);
	}

	/**
	 * Method setUpPlantManager.
	 * @param worldSize int
	 * @param plantNumbers int
	 * @param plantRegenRate int
	 * @param plantStartingEnergy int
	 * @param plantEnergyAbsorptionRate int
	 */
	private void setUpPlantManager(int worldSize, int plantNumbers, int plantRegenRate, int plantStartingEnergy, int plantEnergyAbsorptionRate)
	{
		genericPlantManager = new GenericPlantManager(barrierManager, worldSize, plantNumbers, plantRegenRate, plantStartingEnergy, plantEnergyAbsorptionRate);
	}

	/**
	 * Method setUpAgentManager.
	 * @param worldSize int
	 * @param agentPreyNumbers int
	 * @param agentPredatorNumbers int
	 * @param agentSettings SimpleAgentManagementSetupParam
	 */
	private void setUpAgentManager(int worldSize, int agentPreyNumbers, int agentPredatorNumbers, SimpleAgentManagementSetupParam agentSettings)
	{
		simpleAgentManager = new SimpleAgentManager(barrierManager, worldSize, agentPreyNumbers, agentPredatorNumbers, agentSettings);
	}

	/**
	 * Sets up the barrier with the auto detected number of threads per processor.
	 */
	private void setUpBarrierManager()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors(); // Ask Java how many CPU threads we can run in parallel

		System.out.println("Threads to use for Barrier Tasks : " + numThreads);

		barrierControllerSemaphore = new Semaphore(1, true);

		barrierControllerSemaphore.acquireUninterruptibly();

		barrierManager = new BarrierManager(barrierControllerSemaphore, numThreads);

		barrierManager.start();
	}

	/**
	 * Initiates the barrier thread shutdown.
	 */
	public void cleanUp()
	{
		lock.acquireUninterruptibly();
		
		/* Clean up */
		barrierManager.cleanUp();
		
		/* Set to null so garbage collector can get to work */
		barrierManager=null;
		
		lock.release();
		
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
	 * @param trueDrawing boolean
	 * @param viewRangeDrawing boolean
	 */
	public void drawAgentsAndPlants(Graphics g, boolean trueDrawing, boolean viewRangeDrawing)
	{
		// Get a lock on the done list, 
		// but don't wait if we cant get a lock 
		// so we can draw the old image buffer and stay interactive with regards moving the view.
		// Side effect is that the view could look static if processing power is very low.
		try
		{
			lock.acquire();

			genericPlantManager.drawPlants(g, trueDrawing);

			simpleAgentManager.drawAgent(g, trueDrawing, viewRangeDrawing);

			// Release the lock on the done list
			lock.release();
		}
		catch (InterruptedException e)
		{
			// Debug System.out.println("Never Got Lock");
		}

	}
}
