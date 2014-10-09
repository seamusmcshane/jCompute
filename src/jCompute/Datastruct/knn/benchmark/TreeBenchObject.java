package jCompute.Datastruct.knn.benchmark;

import jCompute.Datastruct.knn.kdtree.KNNNodeInf;

public class TreeBenchObject implements KNNNodeInf
{
	
	private int id;
	private double pos[];
	
	private TreeBenchObject nearestObject;
	private int nearestObjectID;
	private double nearestObjectDistance;
	
	public TreeBenchObject(int id,float x, float y) 
	{
		this.id = id;
		pos = new double[2];
		pos[0] = x;
		pos[1] = y;

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

	public double getX()
	{
		return pos[0];
	}

	public double getY()
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
	public double[] getPos()
	{
		return pos;
	}

	@Override
	public TreeBenchObject getObject()
	{
		return this;
	}
	
}
