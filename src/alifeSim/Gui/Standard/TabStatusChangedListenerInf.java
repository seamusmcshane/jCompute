package alifeSim.Gui.Standard;

import alifeSim.Simulation.SimulationState.SimState;

public interface TabStatusChangedListenerInf
{
	public void tabStatusChanged(GUISimulationTab tab,SimState state);
}
