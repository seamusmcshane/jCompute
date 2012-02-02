import org.khelekore.prtree.DistanceCalculator;
import org.newdawn.slick.geom.Vector2f;

/*
 * 
 * Uses Vector2f distance function
 * 
 * 
 */
public class Vector2fSquaredEuclideanDistanceCalc implements DistanceCalculator<SimpleAgent>
{
	double dis =0;

	Vector2f other = new Vector2f();
	
	@Override
	public double distanceTo(SimpleAgent agent, double x, double y)
	{
		other.set((float)x, (float)y);
		dis = agent.getPos().distanceSquared(other);
		
		/* ignore self */
		
		// System.out.println("X " + agent.getPos().getX() + "Y " + agent.getPos().getY());
		// System.out.println("X " + x + " Y " + y);
		// System.out.println(" ");

		if(agent.getPos().getX() == x && agent.getPos().getY() == y)
		{
			dis = Double.MAX_VALUE;
			//System.out.println("Equal");

		}

		/* Euclidian Distance */
		return dis;
	}

}
