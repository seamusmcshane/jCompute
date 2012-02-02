import java.util.LinkedList;
import java.util.ListIterator;

import org.khelekore.prtree.DistanceCalculator;
import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.PRTree;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;

public class ViewGeneratorThread extends Thread
{
	LinkedList<SimpleAgent> agentList;
	KdTree<SimpleAgent> worldView;
	ListIterator<SimpleAgent> itr;
	Iterable<SimpleAgent> tempList;
	
    /*EuclideanDistanceCalc distancecalc1 = new EuclideanDistanceCalc();
    SquaredEuclideanDistanceCalc distancecalc2 = new SquaredEuclideanDistanceCalc();
    Vector2fEuclideanDistanceCalc distancecalc3 = new Vector2fEuclideanDistanceCalc();*/
    //Vector2fSquaredEuclideanDistanceCalc distancecalc4 = new Vector2fSquaredEuclideanDistanceCalc();
    
    SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();
    
    //DistanceResult<SimpleAgent> nearestAgent;
    
    
    
	private MaxHeap<SimpleAgent> temp1;
	private SimpleAgent nearestAgent;
	private double[] pos;
	
	public ViewGeneratorThread(LinkedList<SimpleAgent> linkedList,	KdTree<SimpleAgent> prTree)
	{
		this.agentList = linkedList;	
		itr = agentList.listIterator();
		this.worldView = prTree;
	}
	
	public void run()
	{
		
		// Split the lists
		while(itr.hasNext()) 
		{
			SimpleAgent temp = itr.next();	
						
			// Closest ignoring range 
			//nearestAgent = worldView.nearestNeighbour(distancecalc4, temp.getPos().getX(),temp.getPos().getY());
			
			pos = new double[2];
			
			pos[0]=temp.getPos().getX();
			pos[1]=temp.getPos().getY();	
			
			temp1 = worldView.findNearestNeighbors(pos, 2, distanceKD);
			
			nearestAgent = temp1.getMax();
			
			/*System.out.println(" ");		
			System.out.println("t1 " + temp.getPos().getX() + " " + temp.getPos().getY() );		
			System.out.println("n1 " + nearestAgent.getPos().getX() + " " + nearestAgent.getPos().getY() );*/

			distanceCalcCompareKD(temp);
			
			//System.out.println(" ");		

			
			//distanceCalcCompare(temp);

			//squaredDistanceCalcCompare(temp);

			
			// All in range 
			//tempList = worldView.find(temp.getFieldofView().getMinX(),temp.getFieldofView().getMinY(),temp.getFieldofView().getMaxX(),temp.getFieldofView().getMaxY());			
			//temp.setHighlighted(true);

		}		
		
	}
	
	
	private void distanceCalcCompareKD(SimpleAgent temp)
	{
		if(temp.getPos().distance(nearestAgent.getPos()) <  (temp.getRange()/2)  )
		{
			temp.updateNearestAgentKD(nearestAgent);
			//System.out.println("inrange");		

		}
		else
		{
			temp.updateNearestAgentKD(null);

		}
	}
	
	private void distanceCalcCompareKDSQ(SimpleAgent temp)
	{
		if(temp.getPos().distanceSquared(nearestAgent.getPos()) <  ( (temp.getRange()* temp.getRange() )/2)  )
		{
			temp.updateNearestAgentKD(nearestAgent);
			//System.out.println("inrange");		

		}
		else
		{
			temp.updateNearestAgentKD(null);

		}
	}	
	
	/*private void distanceCalcCompare(SimpleAgent temp)
	{
		 // Euclidean  Distance used 
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
		 // Note : HALF Squared Euclidean  Distance used so - square the range and half it for correct comparison 
		if(nearestAgent.getDistance() < (( temp.getRange()* temp.getRange())/2) )			
		{
			temp.updateNearestAgent(nearestAgent);
		}
		else
		{
			temp.updateNearestAgent(null); // Nothing in range - allows ignoring agents that move out of range...
		}
	}*/
	
}
