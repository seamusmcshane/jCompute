package alifeSim.Stats;

import java.awt.Color;
import java.util.List;

public interface StatInf
{

	public String getStatName();
	
	public String getType();
	
	public Color getColor();

	public int getHistoryLength();
	
	public List<Integer> getHistory();
	
}
