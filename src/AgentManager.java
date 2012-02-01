import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;


public class AgentManager
{

	/* Field of View */
	PRTree<SimpleAgent> worldView;
    Bounds converter = new Bounds();
    DistanceCalc distancecalc = new DistanceCalc();
	
	/* Actions Linked Lists */
	LinkedList<SimpleAgent> doList;
	LinkedList<SimpleAgent> doneList;
	
	/* Temp */
	Iterable<SimpleAgent> tempList;
	/*Iterator tempItr;*/

	
	/* DrawAI */
	ListIterator<SimpleAgent> itrDrawAI;
	SimpleAgent tAgentDrawAI;
	
	/* DoAI */
	
	
	int agentCount;
	int agentsDone;
	int agentsTodo;
			
	SimpleAgent testAgent;
	
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
		if(agentCount==0)
		{
			testAgent=agent;
			testAgent.setVisible(true);
			testAgent.setHighlighted(true);
		}
		
		agent.setHighlighted(false);
		agent.setVisible(true);
		
		doneList.add(agent);
		agentCount++;
	}
	
	public void drawAI(Graphics g) 
	{
		
		itrDrawAI = doneList.listIterator(); 
		
		while(itrDrawAI.hasNext()) 
		{

			tAgentDrawAI = itrDrawAI.next();
			tAgentDrawAI.drawAgent(g);
			tAgentDrawAI.drawViewRange(g);
			
		}		
		
	}
	
	public void doAi()
	{
		
		setUpLists();
		
		setUpRTree();
		
		randomizeListOrder();
		
		/* TODO Threaded */
		//setUpAgentViews();
		
		/* Non Threaded */
		doAgents();
		
	}
	
		  
	private void setUpRTree()
	{
		worldView = new PRTree<SimpleAgent>(converter, 100);
		worldView.load(doList);		
	}

	private void setUpLists()
	{
		doList = doneList;		
		doneList = new LinkedList<SimpleAgent>();			
	}
	
	private void setUpAgentViews()
	{
		ListIterator<SimpleAgent> itr = doList.listIterator(); 
		while(itr.hasNext()) 
		{
			
			
		}
		
	}
	
	/* Performs AI Action and moves it to Done list */
	private void doAgents()
	{
		ListIterator<SimpleAgent> itr = doList.listIterator(); 
		
		while(itr.hasNext()) 
		{

			/* Remove this Agent from the List */
			SimpleAgent temp = itr.next();
			
			/*temp.setHighlighted(false);
			
			temp.setVisible(false);*/
			
			/* remove from the doList */
			itr.remove();
			
			/* Views */
			//temp.upDateView();
			//tempList = worldView.nearestNeighbour(distancecalc, mainApp.mouse_pos.getX(),mainApp.mouse_pos.getY());
			
			// = worldView.find(mainApp.m.getMinX(),mainApp.m.getMinY(),mainApp.m.getMaxX(),mainApp.m.getMaxY());	
			
			tempList = worldView.find(temp.getFieldofView().getMinX(),temp.getFieldofView().getMinY(),temp.getFieldofView().getMaxX(),temp.getFieldofView().getMaxY());
					
			temp.updateAgentView(tempList);
			
			//testAgent.setHighlighted(true);
			
//			tempList = worldView.find(mainApp.mouse_pos.getX()-(temp.getRange()/4),mainApp.mouse_pos.getY()-(temp.getRange()/4),mainApp.mouse_pos.getX()+(temp.getRange()/4),mainApp.mouse_pos.getY()+(temp.getRange()/4));

			//body.getBodyPos().getX()-(range_limit/2),body.getBodyPos().getY()-(range_limit/2)

			
			/*if(tempList!=null)
			{

				tempItr =  tempList.iterator();

				while(tempItr.hasNext())
				{
					((SimpleAgent) tempItr.next()).setHighlighted(true);
				}*/
			
				/*if(temp.equals(tempList.get()))
				{
					if(tempList.getDistance()<temp.getRange())
					{
						temp.setHighlighted(true);
					}
					else
					{
						temp.setHighlighted(false);
					}

				}
				else
				{
					temp.setHighlighted(false);

				}*/
			/*}*/
			
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
