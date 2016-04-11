package jcompute.gui.interactive.listener;

import jcompute.gui.interactive.tab.GUISimulationTab;
import jcompute.simulation.SimulationState.SimState;

public interface TabStatusChangedListenerInf
{
	public void tabStatusChanged(GUISimulationTab tab,SimState state);
}
