package jCompute.SimulationManager;

import jCompute.Gui.View.View;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.Groups.StatGroupListenerInf;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface SimulationsManagerInf
{	
	public void removeSimulation(int simId);

	public void startSim(int simId);
		
	public void pauseSim(int simId);
		
	public void setReqSimStepRate(int simId, int stepRate);

	public SimState togglePause(int simId);
	
	public String getScenarioText(int simId);
	
	public void unPauseSim(int simId);

	public int addSimulation(String scenarioText,int intialStepRate);
		
	public void setActiveSim(int simId);

	public void setSimView(View simView);
	
	public void clearActiveSim();

	/* Will Reset the Camera of the active */
	public void resetActiveSimCamera();
	
	public List<Simulation> getSimList();	
	
	public List<Integer> getSimIdList();
		
	public int getMaxSims();

	public SimState getState(int simId);
	
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
	public StatExporter getStatExporter(int simId,String fileNameSuffix, ExportFormat format);
	
	void removeAll();

	boolean hasFreeSlot();

}
