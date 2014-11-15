package jCompute.Gui.Interactive.Listener;

import jCompute.Gui.Interactive.Tab.GUISimulationTab;
import jCompute.Simulation.SimulationState.SimState;

public interface TabStatusChangedListenerInf
{
	public void tabStatusChanged(GUISimulationTab tab,SimState state);
}
