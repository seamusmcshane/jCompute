package jCompute.Scenario.Math.Mandelbrot;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.SimViewCam;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.EndEvents.ScenarioEndEventInf;
import jCompute.Scenario.EndEvents.ScenarioStepCountEndEvent;
import jCompute.Scenario.Math.Mandelbrot.Lib.AparapiUtil;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotAnimate;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotAparapiKernel;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotJavaKernel;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotKernelInterface;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotPallete;
import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Simulation.SimulationStats;
import jCompute.Stats.StatManager;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MandelbrotSimulationManager implements SimulationScenarioManagerInf
{	
	private Semaphore compute = new Semaphore(0, false);
	private Semaphore computed = new Semaphore(0, false);
	private long drawTimeoutLimit = 15;
	private long computeTimeoutLimit = 1000;

	private MandelbrotScenario scenario;

	private StatManager statManager;

	private MandelbrotSettings settings;

	private SimViewCam simViewCam;

	private ArrayList<ScenarioEndEventInf> endEvents;
	private String endEvent = "None";

	private MandelbrotKernelInterface kernel;
	private MandelbrotCoordinate coordinates;

	private double coords[];
	private double zooms[];
	private int zoomPos=2;
	private int x = 0;
	private int y = 1;
	private int x2 = 2;
	private int y2 = 3;
	
	private int textureSize;
	private int iterations;
	
	private int[] computeBuffer;
	private int[] drawBuffer;
	
	public MandelbrotSimulationManager(MandelbrotScenario scenario)
	{
		simViewCam = new SimViewCam();
		
		simViewCam.setCamOffset(new A2DVector2f(50,50));
		
		this.scenario = scenario;
		
		settings = scenario.getSettings();

		textureSize = settings.getTextureSize();

		computeBuffer = new int[textureSize*textureSize];
		drawBuffer = new int[textureSize*textureSize];
		
		iterations = settings.getIterations();
		
		if (settings.getComputeMethod().equals("Aparapi"))
		{
			kernel = new MandelbrotAparapiKernel(AparapiUtil.chooseOpenCLDevice(), textureSize, textureSize);
		}
		else
		{
			kernel = new MandelbrotJavaKernel(textureSize, textureSize);
		}

		kernel.setDest(computeBuffer,MandelbrotPallete.HUEPalete(true));
		
		coords = settings.getCoordiantes();
		zooms = settings.getZooms();
		
		coordinates = new MandelbrotCoordinate(true);
		
		coordinates.setCoordinateX(coords[x]);
		coordinates.setCoordinateY(coords[y]);
		
		coordinates.setCurrentZoom(zooms[zoomPos]);
		coordinates.setZoomStart(zooms[zoomPos]);
		coordinates.setZoomEnd(zooms[zoomPos+1]);
		
		setUpStatManager();

		setUpEndEvents();

	}

	@Override
	public void cleanUp()
	{
		kernel.destroy();
	}

	@Override
	public void doSimulationUpdate()
	{

		try
		{
			// Wait on the display to signal we can compute, but if they don't reply in time compute anyway.
			if(compute.tryAcquire(computeTimeoutLimit, TimeUnit.MILLISECONDS))
			{
			}
		}
		catch (InterruptedException e)
		{
		}
		
		boolean finished = MandelbrotAnimate.animateSingleStep(kernel, coords[x], coords[y], coordinates, coords[x2], coords[y2], iterations);

		kernel.computeMandle(coordinates.getCoordinateX(), coordinates.getCoordinateY(), coordinates.getCurrentZoom(), iterations);
		
		kernel.updateBuffers();
		
		if (finished)
		{
			x = (x + 2) % coords.length;
			y = (y + 2) % coords.length;
			x2 = (x2 + 2) % coords.length;
			y2 = (y2 + 2) % coords.length;

			zoomPos = (zoomPos+2)%zooms.length;			
			
			coordinates.setCoordinateX(coords[x]);
			coordinates.setCoordinateY(coords[y]);
			
			coordinates.setCurrentZoom(zooms[zoomPos]);
			coordinates.setZoomStart(zooms[zoomPos]);
			coordinates.setZoomEnd(zooms[zoomPos+1]);
			
			coordinates.setMotionIn(true);
			
			finished = false;
			
		}
		
		// Signal display to swap the buffer
		if(!(computed.availablePermits()>0))
		{
			computed.release();
		}
		
	}

	@Override
	public StatManager getStatmanger()
	{
		return statManager;
	}

	@Override
	public void drawSim(GUISimulationView simView, boolean ignored, boolean ignored2)
	{
		try
		{
			// Wait on the kernel to signal we can swap buffers, but if they don't reply in time draw the OLD buffer
			if(computed.tryAcquire(drawTimeoutLimit, TimeUnit.MILLISECONDS))
			{				
				System.arraycopy(computeBuffer, 0, drawBuffer, 0, computeBuffer.length);
			}
		}
		catch (InterruptedException e)
		{
		}
		
		simView.drawPixelMap(textureSize, drawBuffer, 0, 0);
		
		// Signal kernel it can compute now
		if(!(compute.availablePermits()>0))
		{
			compute.release();
		}		
		
	}
	
	@Override
	public int getWorldSize()
	{
		return 0;
	}

	private void setUpStatManager()
	{
		statManager = new StatManager("Mandelbrot");
	}
	
	@Override
	public float getCamZoom()
	{
		return simViewCam.getCamZoom();
	}

	@Override
	public void resetCamPos(float x, float y)
	{
		simViewCam.resetCamPos(x, y);
	}

	@Override
	public void adjCamZoom(float z)
	{
		simViewCam.adjCamZoom(z);
	}

	@Override
	public void resetCamZoom()
	{
		simViewCam.resetCamZoom();
	}

	@Override
	public A2DVector2f getCamPos()
	{
		return new A2DVector2f(simViewCam.getCamPosX(), simViewCam.getCamPosY());
	}

	@Override
	public void moveCamPos(float x, float y)
	{
		simViewCam.moveCam(x, y);
	}

	private void setUpEndEvents()
	{
		endEvents = new ArrayList<ScenarioEndEventInf>();

	}

	@Override
	public boolean hasEndEventOccurred()
	{
		boolean eventOccurred = false;

		for (ScenarioEndEventInf event : endEvents)
		{
			if (event.checkEvent())
			{
				endEvent = event.getName();

				eventOccurred = true;

				// Output the final update
				statManager.endEventNotifiyStatListeners();

				System.out.println("Event Event Occurred : " + event.getName() + " - " + event.getValue());

				break;	// No need to check other events
			}
		}

		return eventOccurred;
	}

	@Override
	public void setScenarioStepCountEndEvent(SimulationStats simStats)
	{
		if (scenario.endEventIsSet("StepCount"))
		{
			int endStep = scenario.getEventValue("StepCount");

			endEvents.add(new ScenarioStepCountEndEvent(simStats, endStep));
		}
	}

	@Override
	public ScenarioInf getScenario()
	{
		return scenario;
	}

	@Override
	public String getEndEvent()
	{
		return endEvent;
	}
	
}
