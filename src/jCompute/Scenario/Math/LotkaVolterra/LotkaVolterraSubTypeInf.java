package jCompute.Scenario.Math.LotkaVolterra;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Stats.SingleStat;
import jCompute.Stats.StatManager;

import java.util.List;

public interface LotkaVolterraSubTypeInf
{

	public void doStep();
	
	public void draw(GUISimulationView simView);
	
	public List<SingleStat> getPopulationStats();

	public void setStatManager(StatManager statManager);

	
}
