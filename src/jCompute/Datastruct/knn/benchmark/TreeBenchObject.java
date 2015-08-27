package jCompute.Datastruct.knn.benchmark;

public class TreeBenchObject
{
	private int id;
	private double pos[];
	
	private TreeBenchObject nearestObject;
	private int nearestObjectID;
	private double nearestObjectDistance;
	
	public TreeBenchObject(int id,double[] pos) 
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
			dis = Double.MAX_VALUE;
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

	public double[] getPos()
	{
		return pos;
	}

	public TreeBenchObject getObject()
	{
		return this;
	}
	
}
