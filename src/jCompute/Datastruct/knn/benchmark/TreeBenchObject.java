package jCompute.Datastruct.knn.benchmark;

import jCompute.Datastruct.knn.KNNPosInf;
import jCompute.util.JCMath;

public class TreeBenchObject implements KNNPosInf
{
	private int id;
	private float pos[];
	
	private TreeBenchObject nearestObject;
	private int nearestObjectID;
	private float nearestObjectDistance;
	
	public TreeBenchObject(int id,float[] pos) 
	{
		this.id = id;
		this.pos = pos;
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
			dis = JCMath.distanceSquared(pos, nearestObject.getPos());
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

	public float[] getPos()
	{
		return pos;
	}

	public TreeBenchObject getObject()
	{
		return this;
	}
	
}
