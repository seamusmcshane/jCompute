package jcompute.datastruct.knn.kdtree;

import java.util.Arrays;

import jcompute.math.geom.JCVector2f;

public class KDNode<Datatype>
{
	// All types
	protected int bucketCapacity;
	protected int size;
	
	// Leaf only
	protected JCVector2f[] points;
	protected Datatype[] data;
	
	// Stem only
	protected KDNode<Datatype> left;
	protected KDNode<Datatype> right;
	protected int splitDimension;
	protected float splitValue;
	
	// Bounds
	protected JCVector2f minBound;
	protected JCVector2f maxBound;
	
	protected boolean singlePoint;
	
	public KDNode(int bucketCapacity)
	{
		// Init base
		this.bucketCapacity = bucketCapacity;
		this.size = 0;
		this.singlePoint = true;
		
		// Init leaf elements
		this.points = new JCVector2f[bucketCapacity + 1];
		
		// Avoids needing to cast every index
		this.data = (Datatype[]) new Object[bucketCapacity + 1];
	}
	
	public int size()
	{
		return size;
	}
	
	public void add(JCVector2f point, Datatype value)
	{
		KDNode<Datatype> cursor = this;
		while(!cursor.isLeaf())
		{
			cursor.checkBounds(point);
			cursor.size++;
			
			// float pointVal = cursor.splitDimension == 0 ? point.x : point.y;
			
			float pointVal = point.getDimVal(cursor.splitDimension);
			
			if(pointVal > cursor.splitValue)
			{
				cursor = cursor.right;
			}
			else
			{
				cursor = cursor.left;
			}
		}
		cursor.addLeafPoint(point, value);
	}
	
	public void addLeafPoint(JCVector2f point, Datatype value)
	{
		// Add the data point
		points[size] = point;
		data[size] = value;
		
		checkBounds(point);
		
		size++;
		
		if(size == points.length - 1)
		{
			// If the node is getting too large
			if(calculateSplit())
			{
				// If the node successfully had it's split value calculated,
				// split node
				splitLeafNode();
			}
			else
			{
				// If the node could not be split, enlarge node
				increaseLeafCapacity();
			}
		}
	}
	
	public boolean isLeaf()
	{
		return points != null;
	}
	
	private void checkBounds(JCVector2f point)
	{
		if(minBound == null)
		{
			// Single Point
			minBound = new JCVector2f(point);
			maxBound = new JCVector2f(point);
			
			return;
		}
		
		for(int d = 0; d < JCVector2f.DIMENSIONS; d++)
		{
			if(Float.isNaN(point.getDimVal(d)))
			{
				// A previous point may have set bounds - this prevents invalidating it.
				if(!Float.isNaN(minBound.getDimVal(d)) || !Float.isNaN(maxBound.getDimVal(d)))
				{
					singlePoint = false;
				}
				
				// Invalid first bound
				minBound.setDimVal(d, Float.NaN);
				maxBound.setDimVal(d, Float.NaN);
			}
			else if(minBound.getDimVal(d) > point.getDimVal(d))
			{
				minBound.setDimVal(d, point.getDimVal(d));
				singlePoint = false;
			}
			else if(maxBound.getDimVal(d) < point.getDimVal(d))
			{
				maxBound.setDimVal(d, point.getDimVal(d));
				singlePoint = false;
			}
		}
	}
	
	private void increaseLeafCapacity()
	{
		points = Arrays.copyOf(points, points.length * 2);
		data = Arrays.copyOf(data, data.length * 2);
	}
	
	private boolean calculateSplit()
	{
		if(singlePoint)
		{
			return false;
		}
		
		float width = 0;
		
		for(int d = 0; d < JCVector2f.DIMENSIONS; d++)
		{
			// Dimension Width
			float dWidth = maxBound.getDimVal(d) - minBound.getDimVal(d);
			
			// First bound
			if(Float.isNaN(dWidth))
			{
				dWidth = 0;
			}
			
			// Which dimension is largest
			if(dWidth > width)
			{
				splitDimension = d;
				
				width = dWidth;
			}
		}
		
		// Can we split
		if(width == 0)
		{
			return false;
		}
		
		// Start the split in the middle of the variance
		splitValue = (minBound.getDimVal(splitDimension) + maxBound.getDimVal(splitDimension)) * 0.5f;
		
		// Never split on infinity or NaN
		if(splitValue == Float.POSITIVE_INFINITY)
		{
			splitValue = Float.MAX_VALUE;
		}
		else if(splitValue == Float.NEGATIVE_INFINITY)
		{
			splitValue = -Float.MAX_VALUE;
		}
		
		// Don't let the split value be the same as the upper value as
		// can happen due to rounding errors!
		if(splitValue == maxBound.getDimVal(splitDimension))
		{
			splitValue = minBound.getDimVal(splitDimension);
		}
		
		// Success
		return true;
	}
	
	private void splitLeafNode()
	{
		right = new KDNode<Datatype>(bucketCapacity);
		left = new KDNode<Datatype>(bucketCapacity);
		
		// Move locations into children
		for(int i = 0; i < size; i++)
		{
			// Location
			JCVector2f oldLocation = points[i];
			
			// Data
			Datatype oldData = data[i];
			
			if(oldLocation.getDimVal(splitDimension) > splitValue)
			{
				right.addLeafPoint(oldLocation, oldData);
			}
			else
			{
				left.addLeafPoint(oldLocation, oldData);
			}
		}
		
		points = null;
		data = null;
	}
}
