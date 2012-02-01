import java.util.LinkedList;
import java.util.ListIterator;

import org.khelekore.prtree.PRTree;

public class ViewGeneratorThread extends Thread
{
	LinkedList<SimpleAgent> agentList;
	PRTree<SimpleAgent> worldView;
	ListIterator<SimpleAgent> itr;
	Iterable<SimpleAgent> tempList;
	
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

			tempList = worldView.find(temp.getFieldofView().getMinX(),temp.getFieldofView().getMinY(),temp.getFieldofView().getMaxX(),temp.getFieldofView().getMaxY());
			
			temp.updateAgentView(tempList);
			
			//temp.setHighlighted(true);

		}		
		

		
	}
	
}
