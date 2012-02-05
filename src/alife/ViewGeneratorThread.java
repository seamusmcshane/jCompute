package alife;
import java.util.LinkedList;
import java.util.ListIterator;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;

public class ViewGeneratorThread extends Thread
{
	LinkedList<SimpleAgent> agentList;
	KdTree<SimpleAgent> worldView;
	ListIterator<SimpleAgent> itr;
	Iterable<SimpleAgent> tempList;
    
    SquareEuclideanDistanceFunction distanceKD = new SquareEuclideanDistanceFunction();

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
		
		Thread thisThread = Thread.currentThread();

		/* Top Priority to the view threads */
		thisThread.setPriority(Thread.MAX_PRIORITY);
		
		// Split the lists
		while(itr.hasNext()) 
		{
			SimpleAgent temp = itr.next();	
			
			pos = new double[2];			
			pos[0]=temp.getPos().getX();
			pos[1]=temp.getPos().getY();	
			
			/* Get two - due to closest agent being its self */
			temp1 = worldView.findNearestNeighbors(pos, 2, distanceKD);
			
			/* Max is the next closest - Self is 0 */
			nearestAgent = temp1.getMax();

			distanceCalcCompareKDSQ(temp);
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
		
}
