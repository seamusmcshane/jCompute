import org.khelekore.prtree.DistanceCalculator;
import org.newdawn.slick.geom.Vector2f;

/*
 * 
 *  Note Square Variable used for Comparison
 * 
 */
public class SquaredEuclideanDistanceCalc implements DistanceCalculator<SimpleAgent>
{
	double dis =0;
	

	
	@Override
	public double distanceTo(SimpleAgent agent, double x, double y)
	{
		
		/* Much much faster */
		dis = (((agent.getPos().getX()-x)*(agent.getPos().getX()-x)) + ((agent.getPos().getY()-y)*(agent.getPos().getY()-y)));
		
		/* ignore self */
		if(agent.getPos().getX() == x && agent.getPos().getY() == y)
		{
			dis = Double.MAX_VALUE;
		}

		/* Distance */
		return dis;
	}

}
