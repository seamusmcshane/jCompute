package jCompute.Scenario.SAPP.SimpleAgent;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.Graphics.A2DCircle;
import jCompute.Gui.View.Graphics.A2DLine;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Gui.View.Graphics.A2RGBA;
import jCompute.Scenario.SAPP.Plant.GenericPlant;
import jCompute.Scenario.SAPP.Plant.GenericPlantViewStats;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentEnum.AgentEval;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentEnum.AgentType;

/**
 * This Class holds the representation of a view for the agent in the current simulation step.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentView
{
	/** Reference to the body */
	private SimpleAgentBody body;

	/** Agent View Range */
	private A2DCircle fov;
	private float fovRadius;

	/** Agent in View Range mode */
	private AgentEval inViewMode = null;

	/** States of View */
	private boolean agentInView = false;
	private boolean plantInView = false;

	/** Statistics of plants and agents in view */
	private SimpleAgentViewStats inViewAgentStats;
	private GenericPlantViewStats inViewPlantStats;
	
	private A2RGBA stronger = new A2RGBA(1,0,0,1);
	private A2RGBA weaker = new A2RGBA(0,0,1,1);
	private A2RGBA neutral = new A2RGBA(1,1,1,1);
	/**
	 * Creates an agent view.
	 * @param body SimpleAgentBody
	 */
	public SimpleAgentView(SimpleAgentBody body)
	{

		this.body = body;

		inViewAgentStats = new SimpleAgentViewStats();

		inViewPlantStats = new GenericPlantViewStats();

		setUpView();
	}

	/** 
	 * Generates the agents View representation 
	 */
	private void setUpView()
	{
		fovRadius = body.stats.getViewRange();
		fov = new A2DCircle(body.getBodyPos().getX(), body.getBodyPos().getY(), fovRadius);
	}

	/**
	 * Sets the agent in view.
	 * @param agent
	 */
	public void setAgentView(SimpleAgent agent)
	{
		if (agent != null)  // Agent is in view
		{
			agentInView = true;

			// Copy stats
			inViewAgentStats.updateStats(agent);

		}
		else
		// No agent in View
		{
			agentInView = false;

			// clear stats
			inViewAgentStats.clearStats();
		}
	}

	/**
	 * Sets the plant in view.
	 * @param plant
	 */
	public void setPlantView(GenericPlant plant)
	{

		if (plant != null)
		{
			plantInView = true;

			// Copy stats
			inViewPlantStats.updateStats(plant);
		}
		else
		{
			plantInView = false;

			// clear stats
			inViewPlantStats.clearStats();
		}

	}

	/** 
	* Nearest Agent Position 
	* @return Vector2f */
	public A2DVector2f getNearestAgentPos()
	{
		if (!agentInView)
		{
			return null; // To stop external code calling this explicitly with out checking if its been updated
		}
		return inViewAgentStats.getAgentPos();
	}

	/** Nearest Agent Position 
	* @return Vector2f	*/
	public A2DVector2f getNearestPlantPos()
	{
		if (!plantInView)
		{
			return null;
		}
		return inViewPlantStats.getPlantPos();
	}

	/** 
	* Status of View 
	* @return boolean */
	public boolean hasAgentInView()
	{
		return agentInView;
	}

	/** 
	* Status of View
	* @return boolean	*/
	public boolean hasPlantInView()
	{
		return plantInView;
	}

	/**
	 * Method agentType.
	 * @return SimpleAgentType */
	public SimpleAgentType agentType()
	{
		return inViewAgentStats.getAgentType();
	}

	/*
	 * Following functions may fail if not checked by calling the above function
	 * helpers
	 */

	/** Returns the direction to move in to go away from the nearest agent
	 * Y is inverted
	* @param myBody SimpleAgentBody
	* @return float */
	public float awayfromAgentDirection(SimpleAgentBody myBody)
	{
		double direction = Math.toDegrees(Math.atan2(getNearestAgentPos().getX() - myBody.getBodyPos().getX(), myBody.getBodyPos().getY() - getNearestAgentPos().getY()));

		direction = direction - 180;

		if (direction < 0)
		{
			direction += 360;
		}

		return (float)direction % 360;
	}

	/** Returns the direction to move in to go towards the nearest agent
	 * Y is inverted
	 * @param myBody SimpleAgentBody
	 * @return float */
	public float towardsAgentDirection(SimpleAgentBody myBody)
	{
		double direction = Math.toDegrees(Math.atan2(getNearestAgentPos().getX() - myBody.getBodyPos().getX(), myBody.getBodyPos().getY() - getNearestAgentPos().getY()));

		if (direction < 0)
		{
			direction += 360;
		}

		return (float)direction;
	}

	/** Returns the direction to move in to go towards the nearest plant
	 * Y is inverted
	 * @param myBody SimpleAgentBody
	 * @return float */
	public float towardsPlantDirection(SimpleAgentBody myBody)
	{
		float direction = (float) Math.toDegrees(Math.atan2(getNearestPlantPos().getX() - myBody.getBodyPos().getX(), myBody.getBodyPos().getY() - getNearestPlantPos().getY()));

		if (direction < 0)
		{
			direction += 360;
		}

		return direction;
	}

	/** Returns the squared distances between two vectors
	 * @param from A2DVector2f
	 * @param posTo A2DVector2f
	 * @return float */
	public float distanceTo(A2DVector2f from, A2DVector2f posTo)
	{
		return from.distanceSquared(posTo);
	}

	/** Not to be called by agents directly
	 * @return GenericPlant */
	public GenericPlant getOriginalPlantRef()
	{
		return inViewPlantStats.getOriginalPlantRef();
	}

	/**
	 * Method getOriginalAgentRef.
	 * @return SimpleAgent */
	public SimpleAgent getOriginalAgentRef()
	{
		return inViewAgentStats.getOriginalAgentRef();
	}

	/** 
	 * Updates the location of the representation of View position 
	 */
	private void upDateViewLocation(float radius)
	{
		fov.setLocation(body.getBodyPos().getX(), body.getBodyPos().getY());
		fov.setRadius(radius);
	}
	
	/**
	 * Method getFovRadiusSquared.
	 * @return float
	 */
	public float getFovRadius()
	{
		return fovRadius;
	}
	
	/**
	 * Method setViewDrawMode.
	 * @param mode AgentEval
	 */
	public void setViewDrawMode(AgentEval mode)
	{
		this.inViewMode = mode;
	}

	/**
	 * Draws the agents field of view.
	 */
	public void drawViewRange(GUISimulationView simView, boolean distanceRings, boolean edgeStyled)
	{
		A2RGBA color;
		float lineWidth;
		
		// Edge
		if(edgeStyled)
		{
			lineWidth = 0.75f;
			
			if (body.stats.getType().getType() == AgentType.PREDATOR )
			{
				color = stronger;	// Same is treated as inactive as Agents of same type ignore each other		
			}
			else
			{
				color = weaker;	// The current Agent is Stronger i.e Predator 		
			}
		}
		else
		{
			lineWidth = 0.25f;
			
			color = neutral;
		}
		
		upDateViewLocation(fovRadius);
		
		//simView.drawCircle(fov, color,lineWidth);
		
		simView.drawTransparentFilledCircle(fov, color,0.25f);
		
		if(distanceRings)
		{
			drawDistanceRings(simView,lineWidth);
		}
		
	}

	public void drawDistanceRings(GUISimulationView simView, float lineWidth)
	{
		float fovd;
		
		float rings = (fovRadius/20);
		
		float spacing = fovRadius/rings;
				
		A2RGBA color = neutral;
		
		for(int i=1; i<=rings; i++)
		{
			fovd = spacing*i;
						
			upDateViewLocation(fovd);

			// Only use colors for views that are active
			if (inViewMode != null)
			{
				switch (inViewMode)
				{
					case SAME :						
						color = new A2RGBA(1f/i,1f/i,1f/i,1f/i);	// Same is treated as inactive as Agents of same type ignore each other		
						break;
					case STRONGER :
						color = new A2RGBA(1f/i,0,0,1);				// The current Agent is Stronger i.e Predator 		
						break;
					case WEAKER :
						color = new A2RGBA(0,0,1f/i,1f);				// The current Agent is Weaker i.e Prey 	
						break;
				}
			}
			else
			{
				color = new A2RGBA(1f/i,1f/i,1f/i,1f/i);			
			}
			
			simView.drawCircle(fov, color);
			
		}		
	}
	
	public void drawViews(GUISimulationView simView)
	{
		float lineWidth = 0.25f;
		A2RGBA color = neutral;
		
		// Highlight the View Type
		setViewDrawMode(body.stats.getType().strongerThan(inViewAgentStats.getAgentType()));

		if (inViewMode != null)
		{
			switch (inViewMode)
			{
				case STRONGER :
					color = stronger;				// The current Agent is Stronger i.e Predator 		
					break;
				case WEAKER :
					color = weaker;			// The current Agent is Weaker i.e Prey 	
					break;
			}
		}		
		
		if(getNearestAgentPos() != null)
		{			
			upDateViewLocation((float) Math.sqrt(distanceTo(body.getBodyPos(),getNearestAgentPos())));
			simView.drawCircle(fov, color);
			
			simView.drawLine(body.getBodyPos(),getNearestAgentPos(), color, lineWidth,true);
			
		}
		
		if(getNearestPlantPos() != null)
		{
			color = new A2RGBA(0,1f,0,1f);
			
			upDateViewLocation((float) Math.sqrt(distanceTo(body.getBodyPos(),getNearestPlantPos())));
			simView.drawCircle(fov, color);
			
			/*System.out.println("Bx " + body.getBodyPos().getX() + " By " + body.getBodyPos().getY());
			System.out.println("Px " + getNearestPlantPos().getX() + " Py " + getNearestPlantPos().getY());
			System.out.println("PLinesQ " + pDis*pDis);*/
			
			simView.drawLine(body.getBodyPos(),getNearestPlantPos(), color, lineWidth,true);
		}

	}
	
}
