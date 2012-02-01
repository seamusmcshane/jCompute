import gnu.trove.TIntIntProcedure;
import gnu.trove.TIntProcedure;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import org.newdawn.slick.Graphics;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.rtree.RTree;

public class AgentManager
{

	/* Field of View */
	RTree worldView;
	
	/* Actions Linked Lists */
	LinkedList<SimpleAgent> doList;
	LinkedList<SimpleAgent> doneList;

	int agentCount;
	int agentsDone;
	int agentsTodo;
			
	public AgentManager()
	{
		setUpLists();
				
		agentsTodo=0;
		agentsDone=0;
		agentCount=0;
	}
	
	/* Being born counts as an Action */
	public void addNewAgent(SimpleAgent agent)
	{
		doneList.add(agent);
	}
	
	public void drawAI(Graphics g) 
	{
		
		ListIterator<SimpleAgent> itr = doneList.listIterator(); 
		
		while(itr.hasNext()) 
		{

			itr.next().drawAgent(g);
			
		}		
		
	}
	
	public void doAi()
	{
		
		setUpLists();
		
		setUpRTree();
		
		randomizeListOrder();

		doAgents();
		
	}
	
		  
	private void setUpRTree()
	{
		worldView = new RTree();
	
		ListIterator<SimpleAgent> itr = doneList.listIterator(); 
		
		int i=0;
		
		while(itr.hasNext()) 
		{
			worldView.add(itr.next().getBodyBounds(),i);
		
			System.out.println("Slow");
			
			i++;
						
		}	
		
		
		
	}

	private void setUpLists()
	{
		doList = doneList;
		
		doneList = new LinkedList<SimpleAgent>();			
	}
	
	/* Performs AI Action and moves it to Done list */
	private void doAgents()
	{
		ListIterator<SimpleAgent> itr = doList.listIterator(); 
		
		while(itr.hasNext()) 
		{

			/* Remove this Agent from the List */
			SimpleAgent temp = itr.next();
			
			/* remove from the doList */
			itr.remove();
			
			/* Move, Eat ,Sleep etc */
			temp.think();
			
			doneList.add(temp);
			
		}
	}
	
	private boolean removeAgent(SimpleAgent agent)
	{
		return doList.remove(agent);
	}

	
	private void randomizeListOrder()
	{
		Collections.shuffle(doList);		
	}
	
}
