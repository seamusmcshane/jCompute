package alifeSim.Scenario.Math;

import java.util.List;

import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatManager;

public interface LVSubTypeInf
{

	public void doStep();
	
	public void draw(GUISimulationView simView);
	
	public List<SingleStat> getPopulationStats();

	public void setStatManager(StatManager statManager);

	
}
