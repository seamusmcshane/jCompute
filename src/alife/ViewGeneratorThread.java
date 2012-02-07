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

	private MaxHeap<SimpleAgent> neighbourlist;
	
	private SimpleAgent currentAgent;
	
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
			currentAgent = itr.next();	
			
			pos = new double[2];			
			pos[0]=currentAgent.getPos().getX();
			pos[1]=currentAgent.getPos().getY();	
			
			/* Get two - due to closest agent being its self */
			neighbourlist = worldView.findNearestNeighbors(pos, 2, distanceKD);
			
			/* Max is the next closest - Self is 0 */
			nearestAgent = neighbourlist.getMax();

			distanceCalcCompareKDSQ();
		}		
		
	}	
	
	/* Part 1 : Get Squared Distance between two agents positions
	 * This distance is from the center of the agents,  
	 * 
	 * Part 2: Square the size of the bodies -
	 * We also need to subtract the square of the body sizes, so distance is on the body edges of each agent
	 *
	 * Part 3: Square the view distance (range) of the current agent
	 * 
	 * Pseudo Code : 
	 * If the distance between the closest agents surface is less than the view range of the current agent then it is in view 
	 * True - add it to the Agent to the current agents view.
	 * False- clear the view.
	 */
	private void distanceCalcCompareKDSQ()
	{
		
//		if( (currentAgent.getPos().distanceSquared(nearestAgent.getPos())) <  ((currentAgent.getRange()*currentAgent.getRange())) )  																	// Part 3

		if( (currentAgent.getPos().distanceSquared(nearestAgent.getPos()) - 																// Part 1
				( (currentAgent.body.getSize()+currentAgent.body.getSize()) * (nearestAgent.body.getSize()+nearestAgent.body.getSize()) ) ) // Part 2
				<  ((currentAgent.getRange()*currentAgent.getRange())) )																	// Part 3			
		{
			currentAgent.updateNearestAgentKD(nearestAgent);
		}
		else // Clear the view 
		{
			currentAgent.updateNearestAgentKD(null);
		}
	}	
		
}
