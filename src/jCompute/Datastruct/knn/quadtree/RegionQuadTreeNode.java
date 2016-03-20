package jCompute.Datastruct.knn.quadtree;

import java.util.ArrayList;
import java.util.Iterator;

import jCompute.Datastruct.knn.KNNPosInf;
import jCompute.Datastruct.knn.KNNResult;
import jCompute.util.JCMath;

/**
 * Quad Tree Node
 * @author Seamus McShane
 */
public class RegionQuadTreeNode
{
	private final boolean debug = false;

	public int nodeIndex;

	// Center of the partition
	public float center[];

	// Size of the partition
	public float size;

	// Level of the Node
	public int level;

	// Sub Nodes
	public RegionQuadTreeNode nodes[];

	// Object in partition
	private ArrayList<KNNPosInf> objects;

	private int maxObjectsPerNode;

	public RegionQuadTreeNode(int nodeIndex, float[] center, float size, int level, int maxObjectsPerNode)
	{
		super();
		this.nodeIndex = nodeIndex;
		this.center = center;
		this.size = size;
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
	 * @param object
	 */
	public void addPoint(KNNPosInf object)
	{
		if(objects == null)
		{
			objects = new ArrayList<KNNPosInf>(maxObjectsPerNode);
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
	 * @param result
	 * @param point
	 * @param maxDistance
	 */
	public void setNearestObject(KNNResult result, float[] point, float maxDistance)
	{
		if(objects == null)
		{
			return;
		}

		for(KNNPosInf object : objects)
		{
			float dis = JCMath.distanceSquared(point, object.getPos());

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
	 * @param result
	 * @param point
	 * @param maxDistance
	 */
	public void getNearestObjects(ArrayList<KNNPosInf> result, float[] point, float maxDistance)
	{
		if(objects == null)
		{
			return;
		}

		for(KNNPosInf object : objects)
		{
			float dis = JCMath.distanceSquared(point, object.getPos());

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
	 * @param objects
	 */
	public void setPoints(ArrayList<KNNPosInf> objects)
	{
		this.objects = objects;
	}

	/**
	 * Explicitly removes the objects in this node.
	 * @return
	 */
	public ArrayList<KNNPosInf> removeObjects()
	{
		ArrayList<KNNPosInf> currentObjects = objects;

		// Clear internal reference
		objects = null;

		return currentObjects;
	}

	/**
	 * Explicitly sets the sub nodes of this node.
	 * @param nodes
	 */
	public void setSubNodes(RegionQuadTreeNode nodes[])
	{
		this.nodes = nodes;
	}

	/**
	 * Returns a sub node.
	 * @param num
	 * @return
	 */
	public RegionQuadTreeNode getSubNodeNode(int num)
	{
		return nodes[num];
	}

	/**
	 * If this node has no sub nodes then isLeft() returns true.
	 * @return
	 */
	public boolean isLeaf()
	{
		return(nodes == null);
	}

	/**
	 * If there are no objects in this node then the node is empty.
	 * @return
	 */
	public boolean isEmpty()
	{
		return(objects == null);
	}

	/**
	 * If this node is a leaf and the objects size is under the max per node
	 * then this method returns true.
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
	 * @param searchPoint
	 */
	public void removePoint(KNNPosInf searchPoint)
	{
		float[] searchPos = searchPoint.getPos();

		if(objects != null)
		{
			Iterator<KNNPosInf> itr = objects.iterator();

			if(debug)
			{
				System.out.println("ITR " + objects.size());
			}

			while(itr.hasNext())
			{
				KNNPosInf object = itr.next();
				float[] objectPos = object.getPos();

				if(objectPos[0] == searchPos[0] && objectPos[1] == searchPos[1])
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