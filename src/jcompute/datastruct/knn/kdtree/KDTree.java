package jcompute.datastruct.knn.kdtree;

import ags.utils.dataStructures.singlePrecision.kdTree.thirdGen.DistanceFunctionFloat;
import jcompute.datastruct.heap.BinaryHeap;
import jcompute.datastruct.heap.BinaryHeap.HeapType;
import jcompute.math.MathVector2f;
import jcompute.math.geom.JCVector2f;

//@formatter:off
/**
 * Copyright 2009 Rednaxela
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 
 *    2. This notice may not be removed or altered from any source
 *    distribution.
 */

/**
 * Copyright 2016 Seamus McShane
 * 
 * Original source code has been modified, such modifications may include but may not be limited to code and feature adaptation, addition and removal.
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 
 *    2. This notice may not be removed or altered from any source
 *    distribution.
 */
//@formatter:on

public class KDTree<Datatype> extends KDNode<Datatype>
{
	public KDTree()
	{
		this(24);
	}
	
	public KDTree(int bucketCapacity)
	{
		super(bucketCapacity);
	}
	
	/**
	 * Performs an optimised single nearest neighbour search.
	 * 
	 * @param searchPoint
	 * @param notObject
	 * @return
	 */
	public Datatype kn1NearestNeighbour(JCVector2f searchPoint, Datatype notObject)
	{
		// Tree nodes/branches to follow later.
		BinaryHeap<KDNode<Datatype>> pendingPaths = new BinaryHeap<KDNode<Datatype>>(HeapType.MIN, 64);
		
		// Start at the root
		pendingPaths.push(0, this);
		
		// No min dis yet
		float minDis = Float.MAX_VALUE;
		
		// No min object
		Datatype currentMinObj = null;
		
		// While still paths to evaluate and ( there are still points to evaluate or there the are pending nodes that may be closer )
		while(pendingPaths.contents() > 0 && pendingPaths.peekKey() < minDis)
		{
			// Get the top node
			KDNode<Datatype> cursor = pendingPaths.poll();
			
			// Descend the tree, recording paths not taken
			while(!cursor.isLeaf())
			{
				KDNode<Datatype> pathNotTaken;
				
				if(searchPoint.getDimVal(cursor.splitDimension) > cursor.splitValue)
				{
					pathNotTaken = cursor.left;
					cursor = cursor.right;
				}
				else
				{
					pathNotTaken = cursor.right;
					cursor = cursor.left;
				}
				
				float otherDistance = MathVector2f.DistanceToRectangleVectorRange(searchPoint, pathNotTaken.minBound, pathNotTaken.maxBound);
				
				// Only add a path if we either need more points or it's closer than
				// Farthest point on list so far
				if(currentMinObj == null || otherDistance < minDis)
				{
					pendingPaths.push(otherDistance, pathNotTaken);
				}
			}
			
			// Optimised for a single point leaf
			if(cursor.singlePoint)
			{
				// Is the single point the object we wish to ignore?
				if(cursor.data[0] != notObject)
				{
					// Calculate the distance
					float singlePoint = MathVector2f.DistanceSquared(cursor.points[0], searchPoint);
					
					// Is it the closest?
					if(singlePoint < minDis)
					{
						currentMinObj = cursor.data[0];
						
						minDis = singlePoint;
					}
				}
			}
			else
			{
				// Check all the points in the bucket
				for(int i = 0; i < cursor.size(); i++)
				{
					// Is the data the object we wish to ignore?
					if(cursor.data[i] != notObject)
					{
						JCVector2f point = cursor.points[i];
						
						// Calculate the distance
						float distance = MathVector2f.DistanceSquared(point, searchPoint);
						
						// Is it the closest?
						if(currentMinObj == null || distance < minDis)
						{
							currentMinObj = cursor.data[i];
							minDis = distance;
						}
					}
				}
			}
		}
		
		return currentMinObj;
	}
	
	public BinaryHeap<Datatype> findNearestNeighbors(JCVector2f searchPoint, int maxNeighbours, DistanceFunctionFloat distanceFunctionFloat)
	{
		// Min Heap
		BinaryHeap<KDNode<Datatype>> pendingPaths = new BinaryHeap<KDNode<Datatype>>(HeapType.MIN, 64);
		
		// Max Heap
		BinaryHeap<Datatype> nearestNeighbors = new BinaryHeap<Datatype>(HeapType.MAX, 64);
		
		// Total result point
		int pointsRemaining = Math.min(maxNeighbours, size());
		
		// Start at root node
		pendingPaths.push(0, this);
		
		// While still paths to evaluate and ( there are still points to evaluate or there the are unexplored nodes at a higher depth in the tree to explore)
		while(pendingPaths.contents() > 0 && (nearestNeighbors.contents() < pointsRemaining || (pendingPaths.peekKey() < nearestNeighbors.peekKey())))
		{
			nearestNeighborSearchStep(pendingPaths, nearestNeighbors, pointsRemaining, distanceFunctionFloat, searchPoint);
		}
		
		return nearestNeighbors;
	}
	
	protected void nearestNeighborSearchStep(BinaryHeap<KDNode<Datatype>> pendingPaths, BinaryHeap<Datatype> nearestNeighbors, int maxNeighbours,
	DistanceFunctionFloat distanceFunctionFloat, JCVector2f searchPoint)
	{
		// If there are pending paths possibly closer than the nearest evaluated
		// point, check it out
		KDNode<Datatype> cursor = pendingPaths.poll();
		
		// Descend the tree, recording paths not taken
		while(!cursor.isLeaf())
		{
			KDNode<Datatype> pathNotTaken;
			if(searchPoint.getDimVal(cursor.splitDimension) > cursor.splitValue)
			{
				pathNotTaken = cursor.left;
				cursor = cursor.right;
			}
			else
			{
				pathNotTaken = cursor.right;
				cursor = cursor.left;
			}
			
			float otherDistance = MathVector2f.DistanceToRectangleVectorRange(searchPoint, pathNotTaken.minBound, pathNotTaken.maxBound);
			
			// Only add a path if we either need more points or it's closer than
			// Farthest point on list so far
			if(nearestNeighbors.contents() < maxNeighbours || otherDistance <= nearestNeighbors.peekKey())
			{
				pendingPaths.push(otherDistance, pathNotTaken);
			}
		}
		
		if(cursor.singlePoint)
		{
			float nodeDistance = MathVector2f.DistanceSquared(cursor.points[0], searchPoint);
			
			// Only add a point if either need more points or it's closer than
			// furthest on list so far
			if(nearestNeighbors.contents() < maxNeighbours || nodeDistance <= nearestNeighbors.peekKey())
			{
				for(int i = 0; i < cursor.size(); i++)
				{
					Datatype value = cursor.data[i];
					
					// If we don't need any more, replace max
					if(nearestNeighbors.contents() == maxNeighbours)
					{
						nearestNeighbors.replace(nodeDistance, value);
					}
					else
					{
						nearestNeighbors.push(nodeDistance, value);
					}
				}
			}
		}
		else
		{
			// Add the points at the cursor
			for(int i = 0; i < cursor.size(); i++)
			{
				Datatype value = cursor.data[i];
				JCVector2f point = cursor.points[i];
				
				float distance = MathVector2f.DistanceSquared(point, searchPoint);
				
				// Only add a point if either need more points or it's closer than farthest on list so far
				if(nearestNeighbors.contents() < maxNeighbours)
				{
					nearestNeighbors.push(distance, value);
				}
				else if(distance < nearestNeighbors.peekKey())
				{
					nearestNeighbors.replace(distance, value);
				}
			}
		}
	}
}
