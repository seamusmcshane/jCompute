package alifeSim.Simulation.SimulationManager;

import java.util.List;
import java.util.Set;

import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Simulation.Simulation;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationStatListenerInf;
import alifeSim.Simulation.SimulationManager.Local.SimulationsManagerEventListenerInf;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Simulation.SimulationStateListenerInf;
import alifeSim.Stats.StatGroupListenerInf;
import alifeSim.Stats.StatManager;

public interface SimulationsManagerInf
{	
	public void removeSimulation(int simId);

	public void startSim(int simId);
		
	public void pauseSim(int simId);
	
	public long getSimRunTime(int simId);
	
	public long getSimStepCount(int simId);
	
	public void setReqSimStepRate(int simId, int stepRate);

	public SimState togglePause(int simId);
	
	public String getScenarioText(int simId);
	
	public void unPauseSim(int simId);

	public int addSimulation(String scenarioText,int intialStepRate);
		
	public void setActiveSim(int simId);

	public void setSimView(GUISimulationView simView);
	
	public void clearActiveSim();

	/* Will Reset the Camera of the active */
	public void resetActiveSimCamera();
	
	public List<Simulation> getSimList();	
	
	public List<Integer> getSimIdList();
		
	public int getMaxSims();

	public int getActiveSims();
	
	public void addSimulationManagerListener(SimulationsManagerEventListenerInf listener);
	
	public void removeSimulationManagerListener(SimulationsManagerEventListenerInf listener);
			
	public void addSimulationStateListener(int simId,SimulationStateListenerInf listener);
	
	public void removeSimulationStateListener(int simId,SimulationStateListenerInf listener);
	
	public void addSimulationStatListener(int simId,SimulationStatListenerInf listener);
	
	public void removeSimulationStatListener(int simId,SimulationStatListenerInf listener);
	
	public SimState getState(int simId);
	
	public String getEndEvent(int simId);
	
	public int getReqSps(int simId);
		
	/** Manager Events */
	public enum SimulationManagerEvent
	{
		AddedSim	("Added Sim"),
		RemovedSim	("Removed Sim");

	    private final String name;

	    private SimulationManagerEvent(String name) 
	    {
	        this.name = name;
	    }

	    public String toString()
	    {
	       return name;
	    }
	}

	// Stat Group getters
	public boolean isStatGroupGraphingEnabled(int simId, String group);
	public int getStatGroupGraphSampleWindowSize(int simId, String group);
	public boolean hasStatGroupTotalStat(int simId, String group);
	public Set<String> getStatGroupNames(int simId);
	public void addStatGroupListener(int simId, String group, StatGroupListenerInf listener);
	public void removeStatGroupListener(int simId, String group, StatGroupListenerInf listener);
	public void exportAllStatsToDir(int simId, String directory, String fileNameSuffix, String format);
	
}
