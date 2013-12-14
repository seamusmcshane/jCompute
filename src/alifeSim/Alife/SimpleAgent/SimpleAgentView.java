package alifeSim.Alife.SimpleAgent;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import fastMath.fastMath;
import alifeSim.Alife.GenericPlant.GenericPlant;
import alifeSim.Alife.GenericPlant.GenericPlantViewStats;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentEval;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;

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
	private Circle fov;
	private float aDis = 0;
	private float pDis = 0;
	private float fovRadius;
	private float fovDiameter;

	/** Agent in View Range mode */
	private AgentEval inViewMode = null;

	/** States of View */
	private boolean agentInView = false;
	private boolean plantInView = false;

	/** Statistics of plants and agents in view */
	private SimpleAgentViewStats inViewAgentStats;
	private GenericPlantViewStats inViewPlantStats;

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

		Rectangle viewBox = new Rectangle(0, 0, body.stats.getViewRange(), body.stats.getBaseViewRange());

		fovRadius = viewBox.getBoundingCircleRadius();
		fovDiameter = fovRadius * 2;

		fov = new Circle(body.getBodyPos().getX(), body.getBodyPos().getY(), fovDiameter);
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
	public Vector2f getNearestAgentPos()
	{
		if (!agentInView)
		{
			return null; // To stop external code calling this explicitly with out checking if its been updated
		}
		return inViewAgentStats.getAgentPos();
	}

	/** Nearest Agent Position 
	* @return Vector2f	*/
	public Vector2f getNearestPlantPos()
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
		double direction = Math.toDegrees(fastMath.atan2(getNearestAgentPos().getX() - myBody.getBodyPos().getX(), myBody.getBodyPos().getY() - getNearestAgentPos().getY()));

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
		double direction = Math.toDegrees(fastMath.atan2(getNearestAgentPos().getX() - myBody.getBodyPos().getX(), myBody.getBodyPos().getY() - getNearestAgentPos().getY()));

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
		float direction = (float) Math.toDegrees(fastMath.atan2(getNearestPlantPos().getX() - myBody.getBodyPos().getX(), myBody.getBodyPos().getY() - getNearestPlantPos().getY()));

		if (direction < 0)
		{
			direction += 360;
		}

		return direction;
	}

	/** Returns the squared distances between two vectors
	 * @param from Vector2f
	 * @param posTo Vector2f
	 * @return float */
	public float distanceTo(Vector2f from, Vector2f posTo)
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
	private void upDateViewLocation(float fovDiam)
	{
		fov.setLocation(body.getBodyPos().getX() - (fovDiam), body.getBodyPos().getY() - (fovDiam));
		fov.setRadius(fovDiam);
	}

	/**
	 * Method getFovRadiusSquared.
	 * @return float
	 */
	public float getFovRadiusSquared()
	{
		return fovRadius * fovRadius;
	}

	/**
	 * Method getFovDiameterSquared.
	 * @return float
	 */
	public float getFovDiameterSquared()
	{
		return fovDiameter * fovDiameter;
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
	public void drawViewRange(Graphics g, boolean distanceRings, boolean edgeStyled)
	{
		/* Edge */
		if(edgeStyled)
		{
			g.setLineWidth(0.75f);
			
			if (body.stats.getType().getType() == AgentType.PREDATOR )
			{
				g.setColor(Color.red);	// Same is treated as inactive as Agents of same type ignore each other		
			}
			else
			{
				g.setColor(Color.blue);		// The current Agent is Stronger i.e Predator 		
			}
		}
		else
		{
			g.setLineWidth(0.25f);
			
			g.setColor(new Color(255,255,255));	
		}
		
		upDateViewLocation(fovDiameter);
		g.draw(fov);
		
		if(distanceRings)
		{
			drawDistanceRings(g);
		}
		
	}

	public void drawDistanceRings(Graphics g)
	{
		float fovd;
		
		float rings = (fovDiameter/20);
		
		float spacing = fovDiameter/rings;
				
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
						g.setColor(new Color(255/i,255/i,255/i));	// Same is treated as inactive as Agents of same type ignore each other		
						break;
					case STRONGER :
						g.setColor(new Color(255/i,0,0));		// The current Agent is Stronger i.e Predator 		
						break;
					case WEAKER :
						g.setColor(new Color(0,0,255/i));		// The current Agent is Weaker i.e Prey 	
						break;
				}
			}
			else
			{
				g.setColor(new Color(255/i,255/i,255/i));			
			}
			
			g.draw(fov);
			
		}		
	}
	
	public void drawViews(Graphics g)
	{
		g.setLineWidth(0.25f);
		if(getNearestAgentPos() != null)
		{
			Line line = new Line(body.getBodyPos(), getNearestAgentPos());
			aDis = line.length();
			
			upDateViewLocation(aDis);
			g.draw(fov);
			
			g.draw(line);
		}
		else
		{
			aDis=0;
		}
		
		if(getNearestPlantPos() != null)
		{
			g.setColor(Color.green);
			Line line = new Line(body.getBodyPos(), getNearestPlantPos());
			pDis = line.length();
			
			upDateViewLocation(pDis);
			g.draw(fov);
			
			g.draw(line);
		}
		else
		{
			pDis=0;
		}
	}
	
}
