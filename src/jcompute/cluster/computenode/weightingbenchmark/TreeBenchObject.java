package jcompute.cluster.computenode.weightingbenchmark;

import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.math.JCMath;

public class TreeBenchObject implements KNNFloatPosInf
{
	private int id;
	private float pos[];
	private float latchedPos[];
	
	private TreeBenchObject nearestObject;
	private int nearestObjectID;
	private float nearestObjectDistance;
	
	public TreeBenchObject(int id,float[] pos) 
	{
		this.id = id;
		
		this.pos = pos;
		latchedPos = new float[pos.length];
		
		updateAndGetPos();
	}
	
	public void setNearestObject(TreeBenchObject nearestObject)
	{
		this.nearestObject = nearestObject;
		
		this.nearestObjectDistance = distanceTo(nearestObject);
	}
	
	public TreeBenchObject getNearestObject()
	{
		return nearestObject;
	}
	
	public double getNearestObjectDistance()
	{
		return nearestObjectDistance;
	}
	
	public int getId()
	{
		return id;
	}

	public float getX()
	{
		return pos[0];
	}

	public float getY()
	{
		return pos[1];
	}

	public float distanceTo(TreeBenchObject nearestObject)
	{
		float dis;
		
		/* ignore self */
		if(this.getX() == nearestObject.getX() && this.getY() == nearestObject.getY())
		{
			dis = Float.MAX_VALUE;
		}
		else
		{
			dis = JCMath.distanceSquared(pos, nearestObject.getLatchedPos());
		}

		/* Distance */
		return dis;
	}

	// Aparapi
	public int getNearestObjectID()
	{
		return nearestObjectID;
	}

	public void setNearestObjectID(int nearestObjectID)
	{
		this.nearestObjectID = nearestObjectID;
	}

	public TreeBenchObject getObject()
	{
		return this;
	}

	@Override
	public float[] updateAndGetPos()
	{
		latchedPos[0] = pos[0];
		latchedPos[1] = pos[1];
		
		return latchedPos;
	}

	@Override
	public float[] getLatchedPos()
	{
		return latchedPos;
	}
	
}
