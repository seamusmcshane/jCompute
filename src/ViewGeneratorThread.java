import java.util.LinkedList;
import java.util.ListIterator;

import org.khelekore.prtree.DistanceCalculator;
import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.PRTree;

public class ViewGeneratorThread extends Thread
{
	LinkedList<SimpleAgent> agentList;
	PRTree<SimpleAgent> worldView;
	ListIterator<SimpleAgent> itr;
	Iterable<SimpleAgent> tempList;
	
    /*EuclideanDistanceCalc distancecalc1 = new EuclideanDistanceCalc();
    SquaredEuclideanDistanceCalc distancecalc2 = new SquaredEuclideanDistanceCalc();
    Vector2fEuclideanDistanceCalc distancecalc3 = new Vector2fEuclideanDistanceCalc();*/
    Vector2fSquaredEuclideanDistanceCalc distancecalc4 = new Vector2fSquaredEuclideanDistanceCalc();
    
    DistanceResult<SimpleAgent> nearestAgent;
	
	public ViewGeneratorThread(LinkedList<SimpleAgent> linkedList,	PRTree<SimpleAgent> prTree)
	{
		this.agentList = linkedList;	
		itr = agentList.listIterator();
		this.worldView = prTree;
	}
	
	public void run()
	{
		
		/* Split the lists */
		while(itr.hasNext()) 
		{
			SimpleAgent temp = itr.next();	
						
			/* Closest ignoring range */
			nearestAgent = worldView.nearestNeighbour(distancecalc4, temp.getPos().getX(),temp.getPos().getY());
			
			//distanceCalcCompare(temp);

			squaredDistanceCalcCompare(temp);

			
			/* All in range */
			//tempList = worldView.find(temp.getFieldofView().getMinX(),temp.getFieldofView().getMinY(),temp.getFieldofView().getMaxX(),temp.getFieldofView().getMaxY());			
			//temp.setHighlighted(true);

		}		
		
	}
	
	
	private void distanceCalcCompare(SimpleAgent temp)
	{
		 /* Euclidean  Distance used */
		if(nearestAgent.getDistance() < (temp.getRange()))			
		{
			temp.updateNearestAgent(nearestAgent);
		}
		else
		{
			temp.updateNearestAgent(null); // Nothing in range - allows ignoring agents that move out of range...
		}
	}
	
	private void squaredDistanceCalcCompare(SimpleAgent temp)
	{
		 /* Note : HALF Squared Euclidean  Distance used so - square the range and half it for correct comparison */
		if(nearestAgent.getDistance() < (( temp.getRange()* temp.getRange())/2) )			
		{
			temp.updateNearestAgent(nearestAgent);
		}
		else
		{
			temp.updateNearestAgent(null); // Nothing in range - allows ignoring agents that move out of range...
		}
	}
	
}
