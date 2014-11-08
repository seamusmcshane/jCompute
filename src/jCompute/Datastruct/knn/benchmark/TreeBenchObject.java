package jCompute.Datastruct.knn.benchmark;

import jCompute.Datastruct.knn.kdtree.KNNNodeInf;

public class TreeBenchObject implements KNNNodeInf
{
	
	private int id;
	private float pos[];
	
	private TreeBenchObject nearestObject;
	private int nearestObjectID;
	private double nearestObjectDistance;
	
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

	public double distanceTo(TreeBenchObject nearestObject)
	{
		double dis;
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
	public float[] getPos()
	{
		return pos;
	}

	@Override
	public TreeBenchObject getObject()
	{
		return this;
	}
	
}
