package alifeSim.Scenario.SAPP;

import java.util.concurrent.Semaphore;

import org.newdawn.slick.Graphics;

import alifeSim.Alife.GenericPlant.GenericPlantManager;
import alifeSim.Alife.SimpleAgent.SimpleAgentManager;
import alifeSim.Simulation.BarrierManager;
import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.World.World;
import alifeSim.World.WorldInf;


/**
 * This class is the top level manager for the processing sequence a step in the simulation.
 * 
 * It manages the interaction between slick2d drawing and the simulation update loop. (via semaphore)
 * 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SAPPSimulationManager implements SimulationManagerInf
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
		
	/* The Simulation World. */
	private WorldInf world;

	private SAPPScenario scenario;
	
	/**
	 * Constructor for SimulationManager.
	*/
	public SAPPSimulationManager(SAPPScenario scenario)
	{
		this.scenario = scenario;
		
		setUpBarrierManager();

		setUpWorld();
		
		setUpPlantManager();

		setUpAgentManager();
	}
	
	/**
	 * Method setUpPlantManager.
	 * @param worldSize int
	 * @param plantNumbers int
	 * @param plantRegenRate int
	 * @param plantStartingEnergy int
	 * @param plantEnergyAbsorptionRate int
	 */
	private void setUpPlantManager()
	{
		genericPlantManager = new GenericPlantManager(world,barrierManager, scenario.plantSettings);
	}

	/**
	 * Method setUpAgentManager.
	 * @param worldSize int
	 * @param agentPreyNumbers int
	 * @param agentPredatorNumbers int
	 * @param agentSettings SimpleAgentManagementSetupParam
	 */
	private void setUpAgentManager()
	{
		simpleAgentManager = new SimpleAgentManager(world,barrierManager, scenario.predatorAgentSettings,scenario.preyAgentSettings);
	}

	/**
	 * Sets up the barrier with the auto detected number of threads per processor.
	 */
	private void setUpBarrierManager()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors(); // Ask Java how many CPU threads we can run in parallel

		this.numThreads = 8;
		
		System.out.println("Threads to use for Barrier Tasks : " + numThreads);

		barrierControllerSemaphore = new Semaphore(1, true);

		barrierControllerSemaphore.acquireUninterruptibly();

		barrierManager = new BarrierManager(barrierControllerSemaphore, numThreads);

		barrierManager.start();
	}


	private void setUpWorld()
	{
		world = new World(scenario.worldSettings.getWorldSize(), scenario.worldSettings.getBarrierMode(), scenario.worldSettings.getBarrierScenario());
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
	public void drawSim(Graphics g, boolean simpleDrawing, boolean viewRangeDrawing,boolean viewsDrawing)
	{
		// Get a lock on the done list, 
		// but don't wait if we cant get a lock 
		// so we can draw the old image buffer and stay interactive with regards moving the view.
		// Side effect is that the view could look static if processing power is very low.
		try
		{
			lock.acquire();

			world.drawWorld(g);
			
			genericPlantManager.drawPlants(g, simpleDrawing);

			simpleAgentManager.drawAgent(g, simpleDrawing, viewRangeDrawing,viewsDrawing);

			// Release the lock on the done list
			lock.release();
		}
		catch (InterruptedException e)
		{
			// Debug System.out.println("Never Got Lock");
		}

	}

	@Override
	public int getWorldSize()
	{
		return world.getWorldBoundingSquareSize();
	}
	
	public void displayDebug()
	{
		barrierManager.displayBarrierTaskDebugStats();
	}

}
