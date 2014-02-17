package alifeSim.Scenario.Math;

import java.util.List;

import alifeSim.Gui.NewSimView;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatManager;

public interface LVSubTypeInf
{

	public void doStep();
	
	public void draw(NewSimView simView);
	
	public List<SingleStat> getPopulationStats();

	public void setStatManager(StatManager statManager);

	
}
