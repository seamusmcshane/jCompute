package jcompute.datastruct.knn.quadtree;

import java.util.ArrayList;
import java.util.Iterator;

import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.datastruct.knn.KNNResult;
import jcompute.math.MathVector2f;
import jcompute.math.geom.JCVector2f;

/**
 * Quad Tree Node
 * 
 * @author Seamus McShane
 */
public class RegionQuadTreeNode
{
	private final boolean debug = false;
	
	public final int nodeIndex;
	
	// Center of the partition
	public final JCVector2f center;
	
	// Size of the partition
	public final float halfExtend;
	
	// Level of the Node
	public final int level;
	
	// Sub Nodes
	public RegionQuadTreeNode nodes[];
	
	// Object in partition
	private ArrayList<KNNFloatPosInf> objects;
	
	private int maxObjectsPerNode;
	
	public RegionQuadTreeNode(int nodeIndex, JCVector2f center, float halfExtend, int level, int maxObjectsPerNode)
	{
		super();
		this.nodeIndex = nodeIndex;
		this.center = center;
		this.halfExtend = halfExtend;
		this.level = level;
		this.maxObjectsPerNode = maxObjectsPerNode;
	}
	
	/**
	 * Sets this node as empty with no subnodes.
	 */
	public void collapseNode()
	{
		nodes = null;
		objects = null;
	}
	
	/**
	 * Adds a point to this node.
	 * This method does not check for if the objects will go over
	 * MAX_OBJECTS_PER_NODE, as the tree may have reached the max level.
	 * 
	 * @param object
	 */
	public void addPoint(KNNFloatPosInf object)
	{
		if(objects == null)
		{
			objects = new ArrayList<KNNFloatPosInf>(maxObjectsPerNode);
		}
		
		objects.add(object);
		
		if(debug)
		{
			System.out.println("Node Added point ");
		}
	}
	
	/**
	 * Finds the nearest neighbour to point with in maxDistance and sets the
	 * nearest neighbour in a result object.
	 * 
	 * @param result
	 * @param point
	 * @param maxDistance
	 */
	public void setNearestObject(KNNResult result, JCVector2f point)
	{
		if(objects == null)
		{
			return;
		}
		
		for(KNNFloatPosInf object : objects)
		{
			float dis = MathVector2f.DistanceSquared(point, object.getXY());
			
			if(dis < result.getDis())
			{
				if(debug)
				{
					System.out.println("Dis " + dis);
					System.out.println("minDis " + result.getDis());
				}
				
				result.setDis(dis);
				result.setPos(object);
			}
		}
	}
	
	/**
	 * Find the nearest neighbours to point with in maxDistance and adds
	 * them to a results array list.
	 * 
	 * @param result
	 * @param point
	 * @param maxDistance
	 */
	public void getNearestObjects(ArrayList<KNNFloatPosInf> result, JCVector2f point, float maxDistance)
	{
		if(objects == null)
		{
			return;
		}
		
		for(KNNFloatPosInf object : objects)
		{
			float dis = MathVector2f.DistanceSquared(object.getXY(), point);
			
			if(dis < maxDistance)
			{
				if(debug)
				{
					System.out.println("Dis " + dis);
					System.out.println("maxDistance " + maxDistance);
				}
				
				result.add(object);
			}
		}
	}
	
	/**
	 * Explicitly sets the objects in this node.
	 * 
	 * @param objects
	 */
	public void setPoints(ArrayList<KNNFloatPosInf> objects)
	{
		this.objects = objects;
	}
	
	/**
	 * Explicitly removes the objects in this node.
	 * 
	 * @return
	 */
	public ArrayList<KNNFloatPosInf> removeObjects()
	{
		ArrayList<KNNFloatPosInf> currentObjects = objects;
		
		// Clear internal reference
		objects = null;
		
		return currentObjects;
	}
	
	/**
	 * Explicitly sets the sub nodes of this node.
	 * 
	 * @param nodes
	 */
	public void setSubNodes(RegionQuadTreeNode nodes[])
	{
		this.nodes = nodes;
	}
	
	/**
	 * Returns a sub node.
	 * 
	 * @param num
	 * @return
	 */
	public RegionQuadTreeNode getSubNodeNode(int num)
	{
		return nodes[num];
	}
	
	/**
	 * If this node has no sub nodes then isLeft() returns true.
	 * 
	 * @return
	 */
	public boolean isLeaf()
	{
		return(nodes == null);
	}
	
	/**
	 * If there are no objects in this node then the node is empty.
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return(objects == null);
	}
	
	/**
	 * If this node is a leaf and the objects size is under the max per node
	 * then this method returns true.
	 * 
	 * @return
	 */
	public boolean canStorePoint()
	{
		boolean isLeaf = isLeaf();
		boolean objectsStatus = true;
		
		if(objects != null)
		{
			objectsStatus = (objects.size() < maxObjectsPerNode);
			
			if(debug)
			{
				System.out.println(objects.size() + " objectsStatus" + objectsStatus);
			}
		}
		
		return isLeaf && objectsStatus;
	}
	
	/**
	 * removes a point from this node.
	 * 
	 * @param searchPoint
	 */
	public void removePoint(KNNFloatPosInf searchPoint)
	{
		JCVector2f searchPos = searchPoint.getXY();
		
		if(objects != null)
		{
			Iterator<KNNFloatPosInf> itr = objects.iterator();
			
			if(debug)
			{
				System.out.println("ITR " + objects.size());
			}
			
			while(itr.hasNext())
			{
				KNNFloatPosInf object = itr.next();
				JCVector2f objectPos = object.getXY();
				
				if(objectPos.x == searchPos.x && objectPos.y == searchPos.y)
				{
					itr.remove();
					
					if(debug)
					{
						System.out.println("Removed");
					}
				}
			}
			
			if(objects.size() == 0)
			{
				if(debug)
				{
					System.out.println("SIZE " + objects.size());
				}
				
				objects = null;
			}
			
		}
	}
}