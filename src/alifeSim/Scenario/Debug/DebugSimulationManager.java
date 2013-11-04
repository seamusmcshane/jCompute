package alifeSim.Scenario.Debug;

import org.newdawn.slick.Graphics;

import alifeSim.Alife.DebugAgent.DebugAgent;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.World.World;
import alifeSim.World.WorldInf;
import alifeSim.datastruct.knn.KDTree;
import alifeSim.datastruct.knn.KNNInf;

public class DebugSimulationManager implements SimulationManagerInf
{

	private DebugScenario scenario;
	
	/* The Simulation World. */
	private WorldInf world;
	
	private KNNInf<DebugAgent> agentTree;
	
	private int noAgents;
	
	private DebugAgent[] testAgents;
	
	private DebugAgent debugAgent;
	
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
		return world.getWorldBoundingSquareSize();
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
