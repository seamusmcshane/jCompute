package jCompute.Datastruct.knn.kdtree;

public class KDNode<Datatype>
{
	int dim;
	int nodeDepth;
	double pos[];
	Datatype data;

	KDNode<Datatype> parentNode;
	KDNode<Datatype> leftChild;
	KDNode<Datatype> rightChild;
	
	public KDNode(int depth,double pos[], Datatype object)
	{
		this.pos = pos;
		this.nodeDepth = depth;
		this.data = object;
		
		parentNode = null;
		leftChild = null;
		rightChild = null;
	}

	public boolean isValueGreater(int k,double pos[])
	{
		if( this.pos[k] > pos[k])
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public Datatype getData()
	{
		return data;
	}
		
	public double[] getPos()
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