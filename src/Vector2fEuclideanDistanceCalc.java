import org.khelekore.prtree.DistanceCalculator;
import org.newdawn.slick.geom.Vector2f;

/*
 * 
 * Uses Vector2f distance function
 * 
 * 
 */
public class Vector2fEuclideanDistanceCalc implements DistanceCalculator<SimpleAgent>
{
	double dis =0;

	Vector2f other = new Vector2f();
	
	@Override
	public double distanceTo(SimpleAgent agent, double x, double y)
	{
		other.set((float)x, (float)y);
		dis = agent.getPos().distance(other);
		
		/* ignore self */
		if(agent.getPos().getX() == x && agent.getPos().getY() == y)
		{
			dis = Double.MAX_VALUE;
		}

		/* Euclidian Distance */
		return dis;
	}

}
