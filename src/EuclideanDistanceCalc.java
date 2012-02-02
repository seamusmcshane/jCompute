import org.khelekore.prtree.DistanceCalculator;
import org.newdawn.slick.geom.Vector2f;


public class EuclideanDistanceCalc implements DistanceCalculator<SimpleAgent>
{
	double dis =0;
	
	@Override
	public double distanceTo(SimpleAgent agent, double x, double y)
	{
		
		dis = Math.sqrt(Math.abs((agent.getPos().getX()-x)*(agent.getPos().getX()-x)) + Math.abs((agent.getPos().getY()-y)*(agent.getPos().getY()-y)));
		
		/* ignore self */
		if(agent.getPos().getX() == x && agent.getPos().getY() == y)
		{
			dis = Double.MAX_VALUE;
		}
		
		/* Euclidian Distance */
		return dis;
	}

}
