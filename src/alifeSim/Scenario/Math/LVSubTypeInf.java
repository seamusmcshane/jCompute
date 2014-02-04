package alifeSim.Scenario.Math;

import java.util.List;

import alifeSim.Gui.NewSimView;
import alifeSim.Stats.StatInf;

public interface LVSubTypeInf
{

	public void doStep();
	
	public void draw(NewSimView simView);
	
	public List<StatInf> getPopulationStats();

	
}
