import org.khelekore.prtree.DistanceCalculator;


public class DistanceCalc implements DistanceCalculator<SimpleAgent>
{

	@Override
	public double distanceTo(SimpleAgent agent, double x, double y)
	{

		double dis = Math.sqrt(Math.abs((agent.getPos().getX()-x)*(agent.getPos().getX()-x)) + Math.abs((agent.getPos().getY()-y)*(agent.getPos().getY()-y)));
		
		if(agent.getPos().getX() == x && agent.getPos().getY() == y)
		{
			dis = Double.MAX_VALUE;
		}
		
		/*float x1=agent.getPos().getX();
		float y1=agent.getPos().getY();
		
		float x2=(float) x;
		float y2=(float) y;
		
		float dis = (float) Math.sqrt(((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2)));
		
		 // sqr ( ((x1-x2)^2)+((y1-y2)^2) )
		
		
		// (agent.getBodyBounds().getX()-x)* (agent.getBodyBounds().getX()-x)
		// (agent.getBodyBounds().getY()-y)* (agent.getBodyBounds().getY()-y)
		// (agent.getBodyBounds().getX()-x)* (agent.getBodyBounds().getX()-x)+(agent.getBodyBounds().getY()-y)* (agent.getBodyBounds().getY()-y)
		
		System.out.println("Distance "+ Math.sqrt(Math.abs((agent.getPos().getX()-x)*(agent.getPos().getX()-x)) + Math.abs((agent.getPos().getY()-y)*(agent.getPos().getY()-y))));
		System.out.println("Dis " + dis);*/

		/* Euclidian Distance */
		return dis;
	}

}
