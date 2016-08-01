package jcompute.cluster.computenode.weightingbenchmark;

import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.math.MathVector2f;
import jcompute.math.geom.JCVector2f;

public class TreeBenchObject implements KNNFloatPosInf
{
	private int id;
	public final JCVector2f position;
	
	private TreeBenchObject nearestObject;
	private int nearestObjectID;
	private float nearestObjectDistance;
	
	public TreeBenchObject(int id, float x, float y)
	{
		this.id = id;
		
		position = new JCVector2f(x, y);
	}
	
	public TreeBenchObject(int id, JCVector2f position)
	{
		this.id = id;
		
		this.position = new JCVector2f(position);
	}
	
	public void setNearestObject(TreeBenchObject nearestObject)
	{
		if(nearestObject == null)
		{
			return;
		}
		
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
	
	public float distanceTo(TreeBenchObject nearestObject)
	{
		float dis;
		
		/* Ignore self */
		if(position.x == nearestObject.position.x && position.y == nearestObject.position.y)
		{
			dis = Float.MAX_VALUE;
		}
		else
		{
			dis = MathVector2f.DistanceSquared(position, nearestObject.position);
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
	public JCVector2f getXY()
	{
		return position;
	}
	
}
