package alifeSim.Alife.SimpleAgent;

import alifeSim.Alife.AlifeBody;
import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;
import alifeSim.World.WorldInf;
import alifeSimGeom.A2DVector2f;
import alifeSimGeom.A2RGBA;

/**
 * Agent Body Class
 * This Class performs the world movement checks and contains the draw code of this visual representation of the body.
 * 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimpleAgentBody extends AlifeBody
{

	public final SimpleAgentStats stats;
	
	/** Calculated body pos */
	private A2DVector2f newBodyPos = new A2DVector2f(0, 0);

	/** Forward Vector */
	private A2DVector2f forwardVector;

	/** Calculated forward vector */
	private A2DVector2f newForwardVector = new A2DVector2f(0, 0); /* Latched */

	/** Direction of movement of the Agent */
	private float direction;

	private WorldInf world;
	
	/**
	 * Creates a new agents body centered on the pos with the set stats.
	 * @param pos
	 * @param stats
	 */
	public SimpleAgentBody(WorldInf world, A2DVector2f pos, SimpleAgentStats stats)
	{
		this.world = world;	
		
		this.stats = stats;

		forwardVector = new A2DVector2f(0, -stats.getMaxSpeed()); /* Forward 1 up */

		initAgentBody(stats.getSize());

		setIntialPos(pos);
	}

	/** 
	 * Initialises the two body representations 
	 */
	private void initAgentBody(float size)
	{
		setSize(size);
		
		setAgentColor();
	}

	/** 
	 * Polar Movement - Entry Move Statement - World Physics Will be Checked and Enforced, Physics can still deny the movement.
	 * 
	 * @param requestedDirection float
	 * 	
	 * @return boolean   */
	public boolean move(float requestedDirection)
	{
		A2DVector2f newPos = newPosition(requestedDirection);

		/* If physics says yes then move the agent */
		if (world.isValidPosition(newPos.getX(), newPos.getY()))
		{
			//System.out.println("Safe - newPos : X | " + newPos.getX() + " Y |" + newPos.getY());

			updateBodyPosition(newPos);

			return true;
		}
		else
		{
			//System.out.println("Wall - newPos : X | " + newPos.getX() + " Y |" + newPos.getY());
		}
		
		/* Agent is trying to move into a wall - move denied */
		return false;
	}

	/** 
	 * Like above but does't move - can be called by the agent brain to check if the move is valid.
	 * 
	 * @param reqDirection float
	 * 
	 * @return boolean  */
	public boolean movePossible(float reqDirection)
	{
		A2DVector2f newPos = newPosition(reqDirection);

		if (world.isValidPosition(newPos.getX(), newPos.getY()))
		{
			return true;
		}

		return false;
	}

	/**
	 * Used to turn a direction of movement into a new XY coordinate.
	 * 
	 * @param reqDirection	
	 * 
	 * @return newBodyPos  */
	private A2DVector2f newPosition(float reqDirection)
	{
		//System.out.println("reqDirection : " + reqDirection);

		/* Get our current forward direction */
		newForwardVector.set(forwardVector);

		/* Change it by the new direction */
		newForwardVector.add(reqDirection);

		/* Get our current Cartesian X and Y */
		newBodyPos.set(bodyPos);

		/* Add our new forward vector */
		newBodyPos.add(newForwardVector);

		//req_pos.set(bodyPos.getX()+req_pos.getX(),bodyPos.getY()+req_pos.getY());

		//System.out.println("vector  : X | " + newBodyPos.getX() + " Y |" + newBodyPos.getY());

		return newBodyPos;
	}


	/** 
	 * Internal Movement - decrements move energy
	 * @param pos Vector2f
	 */
	private void updateBodyPosition(A2DVector2f pos)
	{
		stats.decrementMoveEnergy();
		bodyPos.set(pos);
	}


	/**
	 * The eat agent Action, attempts to eat the agent in view.
	 * @param view 
	 * @return boolean */
	public boolean eatAgent(SimpleAgentView view)
	{
		if (view.getOriginalAgentRef() != null)
		{

			if (!view.getOriginalAgentRef().body.stats.isDead())
			{
				if (isAgentCloseEnoughToEat(view))
				{
					// Kill Agent
					stats.addEnergy(view.getOriginalAgentRef().body.stats.killAgent()); // 100% energy

					return true;
				}
			}

		}
		return false;
	}

	/**
	 * Checks if the agent in view is close enough to eat.
	 * @param view
	 * @return boolean */
	private boolean isAgentCloseEnoughToEat(SimpleAgentView view)
	{
		// If the distance between the position of the agent and other agent is less than the "true size" of the two bodies... ie are the agent and other agent touching
		if ((view.distanceTo(getBodyPos(), view.getOriginalAgentRef().body.getBodyPos())) < (this.getTrueSizeSQRDiameter() + view.getOriginalAgentRef().body.getTrueSizeSQRDiameter()))
		{
			return true;
		}

		return false;
	}

	/**
	 * The eat plant Action, attempts to eat the plant in view.
	 * @param view	
	 * @return boolean */
	public boolean eatPlant(SimpleAgentView view)
	{
		if (view.getOriginalPlantRef() != null)
		{

			if (!view.getOriginalPlantRef().body.stats.isDead()) // Plant is alive?
			{
				if (isPlantCloseEnoughToEat(view))
				{
					// Remove plant energy (decrements the energy_consumption_rate amount from the plant each time)
					stats.addEnergy(view.getOriginalPlantRef().body.stats.decrementEnergy(stats.getEnergyConsumptionRate()));

					return true; // Have ate a "bit" of the plant - could be the entire plant - we wont know until the next step updates the view
				}
			}

		}

		return false; // Did not eat plant - it died or someone ate the last bit		
	}

	/**
	 * Checks if the plant in view is close enough to eat.
	 * @param view	
	 * @return boolean */
	private boolean isPlantCloseEnoughToEat(SimpleAgentView view)
	{
		// If the distance between the position of the agent and plant is less than the size of the two bodies... ie are the agent and plant touching
		if ((view.distanceTo(getBodyPos(), view.getOriginalPlantRef().body.getBodyPos())) < (this.getTrueSizeSQRDiameter() + view.getOriginalPlantRef().body.getTrueSizeSQRDiameter()))
		{
			return true;
		}

		return false;
	}

	/** 
	 * Returns the agents direction of movement. 
	 * @return float */
	public float getDirection()
	{
		return this.direction;
	}

	/** 
	 * Sets the Body Color
	 */
	private void setAgentColor()
	{
		if (stats.getType().getType() == AgentType.PREY)
		{
			setColor(new A2RGBA(0,0,1f,0));
		}
		else // Predator
		{
			setColor(new A2RGBA(1f,0,0f,0));
		}
	}


	/**
	 * Method getStatsDebugMethod.
	 * @return SimpleAgentStats  */
	public SimpleAgentStats getStatsDebugMethod()
	{
		return stats;
	}
}
