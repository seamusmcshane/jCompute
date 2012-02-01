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
    DistanceCalc distancecalc = new DistanceCalc();
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
			nearestAgent = worldView.nearestNeighbour(distancecalc, temp.getPos().getX(),temp.getPos().getY());
			
			if(nearestAgent.getDistance() < temp.getRange())			
			{
				temp.updateNearestAgent(nearestAgent);
			}
			else
			{
				temp.updateNearestAgent(null); // Nothing in range - allows ignoring agents that move out of range...
			}

			/* All in range */
			//tempList = worldView.find(temp.getFieldofView().getMinX(),temp.getFieldofView().getMinY(),temp.getFieldofView().getMaxX(),temp.getFieldofView().getMaxY());			
			//temp.setHighlighted(true);

		}		
		
	}
	
}
