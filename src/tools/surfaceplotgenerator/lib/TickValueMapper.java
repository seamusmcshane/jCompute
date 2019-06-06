package tools.surfaceplotgenerator.lib;

import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public class TickValueMapper implements ITickRenderer
{
	double multi = 0;
	
	public TickValueMapper(int coordMax, double valueMax)
	{
		super();
		
		multi = valueMax / coordMax;
	}
	
	public TickValueMapper(int stepSize)
	{
		super();
		
		multi = stepSize;
	}
	
	@Override
	public String format(double pos)
	{
		double val = (multi * pos);
		
		if(val % 1.0 == 0)
		{
			return String.valueOf((int) (val));
		}
		else
		{
			return String.format("%.3g%n", val);
		}
	}
}
