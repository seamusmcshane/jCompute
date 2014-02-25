package alifeSim.Scenario.Debug;

import alifeSim.Alife.DebugAgent.DebugAgent;
import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Gui.View.SimViewCam;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationStats;
import alifeSim.Stats.StatManager;
import alifeSim.World.World;
import alifeSim.World.WorldInf;
import alifeSim.datastruct.knn.KDTree;
import alifeSim.datastruct.knn.KNNInf;
import alifeSimGeom.A2DVector2f;

public class DebugSimulationManager implements SimulationScenarioManagerInf
{

	private DebugScenario scenario;
	
	/* The Simulation World. */
	private WorldInf world;
	
	private KNNInf<DebugAgent> agentTree;
	
	private int noAgents;
	
	private DebugAgent[] testAgents;
	
	private DebugAgent debugAgent;

	private SimViewCam simViewCam;
	
	public DebugSimulationManager(DebugScenario debugScenario)
	{
		simViewCam = new SimViewCam();
		
		simViewCam.setCamOffset(new A2DVector2f(50f,50f));
		
		this.scenario = debugScenario;
		
		setUpWorld();
		
		setUpAgents();
	}

	private void setUpAgents()
	{
		noAgents = scenario.getTestAgentNum();
		
		testAgents = new DebugAgent[noAgents];
		
		for(int i=0;i<noAgents;i++)
		{
			testAgents[i] = new DebugAgent(world,1,i, world.getWorldBoundingSquareSize());
		}
		
		debugAgent = new DebugAgent(world,0,-1,world.getWorldBoundingSquareSize());
		
	}

	
	
	@Override
	public void cleanUp()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSimulationUpdate()
	{
		agentTree = new KDTree<DebugAgent>(2);
		
		for(int i=0;i<noAgents;i++)
		{
			agentTree.add(testAgents[i].getPos(), testAgents[i]);
		}
		
		agentTree.add(debugAgent.getPos(), debugAgent);
		
		for(int i=0;i<noAgents;i++)
		{
			testAgents[i].doMove();			
		}
		
		debugAgent.doMove();
		
	}

	
		
	@Override
	public void drawSim(GUISimulationView simView, boolean ignored1,boolean ignored2)
	{
		world.drawWorld(simView);		
		
		for(int i=0;i<noAgents;i++)
		{
			testAgents[i].draw(simView);
		}
		
		debugAgent.draw(simView);
		
	}

	@Override
	public int getWorldSize()
	{
		return world.getWorldBoundingSquareSize();
	}

	private void setUpWorld()
	{
		world = new World(scenario.worldSettings.getWorldSize(), scenario.worldSettings.getBarrierNum(), scenario.worldSettings.getBarrierScenario());
	}

	@Override
	public void displayDebug()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public StatManager getStatmanger()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getCamZoom()
	{
		return simViewCam.getCamZoom();
	}
	
	@Override
	public void resetCamPos(float x,float y)
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
		return new A2DVector2f(simViewCam.getCamPosX(),simViewCam.getCamPosY());
	}

	@Override
	public void moveCamPos(float x, float y)
	{
		simViewCam.moveCam(x,y);		
	}

	@Override
	public boolean hasEndEventOccurred()
	{
		return false;
	}

	@Override
	public void setScenarioStepCountEndEvent(SimulationStats simStats)
	{
		
	}
	
	@Override
	public ScenarioInf getScenario()
	{
		return scenario;
	}	
}
