package alifeSim.Scenario.Debug;

import alifeSim.Alife.DebugAgent.DebugAgent;
import alifeSim.Gui.NewSimView;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Stats.StatManager;
import alifeSim.World.World;
import alifeSim.World.WorldInf;
import alifeSim.datastruct.knn.KDTree;
import alifeSim.datastruct.knn.KNNInf;

public class DebugSimulationManager implements SimulationScenarioManagerInf
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
	public void drawSim(NewSimView simView, boolean ignored1,boolean ignored2)
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
}
