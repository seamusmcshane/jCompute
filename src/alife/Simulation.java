package alife;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import org.newdawn.slick.Graphics;

/**
 * Simulation class
 * This class handles the Simulation activity of the program.
 * It contains the main loop in the form of the asynchronous Simulation Update Thread.
 * Apart from performing a step, it is primarily concerned with timing the step and ensuring statistics are updated..
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class Simulation
{
	/** Simulation Performance Indicators */
	int step_num = 0;
	private long stepStartTime = 0;
	private long stepEndTime = 0;
	private long stepTotalTime = 0; // Total Simulation run-time is the time taken per step for each step

	// Step Per Second Calculations
	private long startTime;
	private long previousTime;
	private long currentTime;
	private long diffTime;

	// Average Steps per second
	private int numSamples = 150;
	private double stepSamples[];
	private double tasps; 			// To avoid a cumulative rounding error when calculating the average, a double is use
	private double sps;	 			// Average Steps Per Second as an int for display purposes

	// Inter-step delay Calculations
	private long stepTimeNow;
	private long stepTimeDiff;
	private long stepTimeTotal;
	private long stepTimePrev;

	/* The Simulation manager */
	SimulationManager simManager;

	/* The default simulation update rate */
	public int reqSps = 15;

	/* Sim Start/Pause Control */
	private Semaphore pause;

	/* Simulation state */
	private boolean simPaused = true;
	private boolean simStarted = false;

	/* Simulation Update Thread */
	private Thread asyncUpdateThread;

	/* The Simulation World. */
	public World world;

	public Simulation()
	{
		setupThreads();

		newSim(null, 256, 0, 0, 0, 0, 0, 0, null); // Never used - needed for successful startup
	}

	/**
	 * Method newSim.
	 * @param stats StatsPanel
	 * @param worldSize int
	 * @param agentPreyNumbers int
	 * @param agentPredatorNumbers int
	 * @param plantNumbers int
	 * @param plantRegenRate int
	 * @param plantStartingEnergy int
	 * @param plantEnergyAbsorptionRate int
	 * @param agentSettings SimpleAgentManagementSetupParam
	 */
	public void newSim(StatsPanel stats, int worldSize, int agentPreyNumbers, int agentPredatorNumbers, int plantNumbers, int plantRegenRate, int plantStartingEnergy, int plantEnergyAbsorptionRate, SimpleAgentManagementSetupParam agentSettings)
	{

		stepSamples = new double[numSamples];

		// Assume we are at 15 steps per second
		Arrays.fill(stepSamples, 15);

		this.step_num = 0;

		StatsPanel.setStepNo(step_num);

		this.stepTotalTime = 0;

		StatsPanel.setTime(stepTotalTime);

		tasps = 0;
		sps = 0;
		StatsPanel.setASPS(averageStepsPerSecond());

		world = new World(worldSize);

		simManager = new SimulationManager(worldSize, agentPreyNumbers, agentPredatorNumbers, plantNumbers, plantRegenRate, plantStartingEnergy, plantEnergyAbsorptionRate, agentSettings);

		if (stats != null)
		{
			StatsPanel.clearStats();

			StatsPanel.updateGraphs();
		}

	}

	/* Simulation Main Thread - The step update loop */
	private void setupThreads()
	{
		pause = new Semaphore(0, true); // Starts Paused

		asyncUpdateThread = new Thread(new Runnable()
		{
			public void run()
			{
				Thread thisThread = Thread.currentThread();

				/* Top Priority to the simulation thread */
				thisThread.setPriority(Thread.MAX_PRIORITY);

				setUpStepsPerSecond();

				while (true)
				{
					Thread.yield(); // Allow other threads to run				

					// The pause semaphore (We do not pause half way through a step)
					pause.acquireUninterruptibly();

					stepStartTime = System.currentTimeMillis(); // For the average

					timeTotal();								  // record step start time for inter-step delay

					// This single method hides the rest of the sim
					simManager.doSimulationUpdate();

					// Calculate the Steps per Second
					calcStepsPerSecond();

					StatsPanel.setASPS(averageStepsPerSecond());

					// Increment the Step counter
					step_num++;

					StatsPanel.setStepNo(step_num);

					// Calculate how much we need to wait (in nanoseconds, based on the time taken so far) before proceeding to the next step 
					while (timeTotal() < (1000000000 / reqSps)) // Approximation of what the inter-step delay should be
					{
						// Inter-Step Busy wait delay (66ms~ for 15 steps per second)
						// This will only wait if the step performance level is being exceeded
						// Waiting between steps ensures smooth animiation on the view
					}

					// resets the value calculated in timeTotal()
					resetTotalTime();

					stepEndTime = System.currentTimeMillis();

					stepTotalTime += stepEndTime - stepStartTime;

					StatsPanel.setTime(stepTotalTime);

					StatsPanel.updateGraphs();

					// Allow the simulation to be paused again
					pause.release();
				}
			}
		}, "Simulation Update Thread"

		);

		asyncUpdateThread.start();

	}

	/**
	 * Initializes the average steps per second counters
	 */
	private void setUpStepsPerSecond()
	{
		startTime = System.nanoTime();
		previousTime = startTime;				// At Start up this is true
		currentTime = System.nanoTime();
		diffTime = currentTime - previousTime;	// Diff time is initialized
	}

	/**
	 * Calculates the Average Steps Per Second
	 */
	private void calcStepsPerSecond()
	{
		currentTime = System.nanoTime();	// Current TIme

		diffTime = currentTime - previousTime;		// Time between this and the last call				

		sps = 1000f / (diffTime / 1000000f);			//  converts diff time to milliseconds then gives a instantaneous performance indicator of steps per second		

		previousTime = currentTime;		 			// Stores the current diff for the diff in the next iteration

		for (int i = 0; i < (numSamples - 1); i++)			// Moves the previous samples back by 1, leaves space for the new sps sample 
		{
			stepSamples[i] = stepSamples[(i + 1)];
		}

		stepSamples[numSamples - 1] = sps;			// Store the new sps sample

		tasps = 0;									// clear the old total average (or it will increment for ever)
		for (int i = 0; i < numSamples; i++)
		{
			tasps += stepSamples[i];					// Total all the steps
		}

	}

	/**
	 * Average the steps thus giving an average steps per second count
	 * @return int */
	public int averageStepsPerSecond()
	{
		return (int) (tasps / numSamples);
	}

	/**
	 * Calculates the total taken between repeated call to this method - used for inter-step time wait
	 * @return long */
	private long timeTotal()
	{
		stepTimeNow = System.nanoTime();		 // Current Time

		stepTimeDiff = stepTimeNow - stepTimePrev; // Time Between this call and the last call

		stepTimeTotal += stepTimeDiff;			 // Total the time between calls

		stepTimePrev = stepTimeNow;				 // Set the current time as the previous to the next call

		return stepTimeTotal;					 // Return the current total
	}

	private void resetTotalTime()
	{
		stepTimeTotal = 0;
	}

	/**
	 * Called by the start button
	 */
	public void startSim()
	{
		simStarted = true;
		unPauseSim();
	}

	/**
	 * UnPauses the Sim.
	 */
	public void unPauseSim()
	{
		simPaused = false;					// Sets the logic boolean to indicate to the other parts of the code that the sim is now unpaused.
		pause.release();					// Release the pause semaphore
	}

	/**
	 * Method simPaused.
	 * @return boolean */
	public boolean simPaused()
	{
		return simPaused;
	}

	/**
	 * Pauses the Sim and sets the display frame rate to a more-interactive and more intensive update rate for better mouse interaction 
	 */
	public void pauseSim()
	{
		pause.acquireUninterruptibly();		// Pause the sim

		simPaused = true;					// Sets the logic boolean to indicate to the other parts of the code that the sim is now paused.					
	}

	/**
	 * Method reqSimUpdateRate.
	 * @param steps int
	 */
	public void reqSimUpdateRate(int steps)
	{
		if (steps > 0)
		{
			reqSps = steps;
		}
	}

	/**
	 * Method drawSim.
	 * @param g Graphics
	 * @param true_drawing boolean
	 * @param view_range_drawing boolean
	 */
	public void drawSim(Graphics g, boolean true_drawing, boolean view_range_drawing)
	{
		if (simStarted)
		{
			if (world != null)
			{
				world.drawWorld(g);
			}
			simManager.drawAgentsAndPlants(g, true_drawing, view_range_drawing);
		}
	}
}