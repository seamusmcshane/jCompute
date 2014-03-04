package alifeSim.Scenario.Math.LotkaVolterra;

import java.util.List;

import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatManager;

public interface LotkaVolterraSubTypeInf
{

	public void doStep();
	
	public void draw(GUISimulationView simView);
	
	public List<SingleStat> getPopulationStats();

	public void setStatManager(StatManager statManager);

	
}
