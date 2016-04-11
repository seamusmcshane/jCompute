package jCompute.gui.interactive.listener;

import jCompute.Simulation.SimulationState.SimState;
import jCompute.gui.interactive.tab.GUISimulationTab;

public interface TabStatusChangedListenerInf
{
	public void tabStatusChanged(GUISimulationTab tab,SimState state);
}
