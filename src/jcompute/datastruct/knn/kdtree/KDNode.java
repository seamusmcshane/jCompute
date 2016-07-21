package jcompute.datastruct.knn.kdtree;

import jcompute.math.geom.JCVector2f;

public class KDNode<Datatype>
{
	int dim;
	int nodeDepth;
	JCVector2f pos;
	Datatype data;
	
	KDNode<Datatype> parentNode;
	KDNode<Datatype> leftChild;
	KDNode<Datatype> rightChild;
	
	public KDNode(int depth, JCVector2f pos, Datatype object)
	{
		this.pos = pos;
		this.nodeDepth = depth;
		this.data = object;
		
		parentNode = null;
		leftChild = null;
		rightChild = null;
	}
	
	public boolean isValueGreater(int d, JCVector2f pos)
	{
		if(d == 0)
		{
			if(this.pos.x > pos.x)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			if(this.pos.y > pos.y)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	public Datatype getData()
	{
		return data;
	}
	
	public JCVector2f getPos()
	{
		return pos;
	}
	
	public KDNode<Datatype> getParentNode()
	{
		return parentNode;
	}
	
	public void setParentNode(KDNode<Datatype> parentNode)
	{
		this.parentNode = parentNode;
	}
	
	public KDNode<Datatype> getLeftChild()
	{
		return leftChild;
	}
	
	public void setLeftChild(KDNode<Datatype> leftChild)
	{
		this.leftChild = leftChild;
	}
	
	public KDNode<Datatype> getRightChild()
	{
		return rightChild;
	}
	
	public void setRightChild(KDNode<Datatype> rightChild)
	{
		this.rightChild = rightChild;
	}
	
}