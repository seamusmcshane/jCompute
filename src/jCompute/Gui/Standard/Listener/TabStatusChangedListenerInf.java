package jCompute.Gui.Standard.Listener;

import jCompute.Gui.Standard.Tab.GUISimulationTab;
import jCompute.Simulation.SimulationState.SimState;

public interface TabStatusChangedListenerInf
{
	public void tabStatusChanged(GUISimulationTab tab,SimState state);
}
