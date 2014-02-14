package alifeSim.Gui;

import alifeSim.Simulation.SimulationState.SimStatus;

public interface TabStatusChangedListenerInf
{
	public void tabStatusChanged(SimulationTabPanel tab,SimStatus status);
}
