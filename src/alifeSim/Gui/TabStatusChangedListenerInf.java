package alifeSim.Gui;

import alifeSim.Simulation.SimulationState.SimState;

public interface TabStatusChangedListenerInf
{
	public void tabStatusChanged(GUISimulationTab tab,SimState state);
}
