package jCompute.Datastruct.knn.benchmark;

import jCompute.Datastruct.knn.kdtree.KNNNodeInf;

public class TreeBenchObject implements KNNNodeInf
{
	
	private int id;
	private float x;
	private float y;
	
	private TreeBenchObject nearestObject;
	private int nearestObjectID;
	private float nearestObjectDistance;
	
	public TreeBenchObject(int id,float x, float y) 
	{
		this.id = id;
		this.x = x;
		this.y = y;
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
	
	public float getNearestObjectDistance()
	{
		return nearestObjectDistance;
	}
	
	public int getId()
	{
		return id;
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public float distanceTo(TreeBenchObject nearestObject)
	{
		float dis;
		/* Much much faster */
		dis = (((this.getX()-nearestObject.getX())*(this.getX()-nearestObject.getX())) + ((this.getY()-nearestObject.getY())*(this.getY()-nearestObject.getY())));
		
		/* ignore self */
		if(this.getX() == nearestObject.getX() && this.getY() == nearestObject.getY())
		{
			dis = Float.MAX_VALUE;
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

	@Override
	public double[] getPos()
	{
		return new double[]{x,y};
	}

	@Override
	public TreeBenchObject getObject()
	{
		return this;
	}
	
}
