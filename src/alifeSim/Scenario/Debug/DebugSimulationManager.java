package alifeSim.Scenario.Debug;

import org.newdawn.slick.Graphics;

import alifeSim.Alife.DebugAgent.DebugAgent;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.World.World;
import alifeSim.datastruct.knn.KDTree;
import alifeSim.datastruct.knn.KNNInf;

public class DebugSimulationManager implements SimulationManagerInf
{

	private DebugScenario scenario;
	
	/* The Simulation World. */
	public World world;
	
	KNNInf<DebugAgent> agentTree;
	
	int noAgents;
	
	DebugAgent[] testAgents;
	
	DebugAgent debugAgent;
	
	public DebugSimulationManager(DebugScenario debugScenario)
	{
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
			testAgents[i] = new DebugAgent(1,i, World.getWorldSize());
		}
		
		debugAgent = new DebugAgent(0,-1,world.getWorldSize());
		
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
	public void drawSim(Graphics g, boolean trueDrawing, boolean viewRangeDrawing, boolean viewsDrawing)
	{
		world.drawWorld(g);
		
		
		for(int i=0;i<noAgents;i++)
		{
			testAgents[i].drawAgent(g);
		}
		
		debugAgent.drawAgent(g);
		
	}

	@Override
	public int getWorldSize()
	{
		return World.getWorldSize();
	}

	private void setUpWorld()
	{
		world = new World(scenario.worldSettings.getWorldSize(), scenario.worldSettings.getBarrierMode(), scenario.worldSettings.getBarrierScenario());
	}

	@Override
	public void displayDebug()
	{
		// TODO Auto-generated method stub
		
	}
}
