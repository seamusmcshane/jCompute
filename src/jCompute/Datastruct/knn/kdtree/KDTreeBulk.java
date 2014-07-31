package jCompute.Datastruct.knn.kdtree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class KDTreeBulk <Datatype> 
{
	int dim;
	
	Random r = new Random();

	KDNode<Datatype> rootnode;

	public KDTreeBulk(int dim)
	{		
		this.dim = dim;
	}


	public void load(ArrayList<Datatype> listIn)
	{
		// Assume Datatype supports KNNNodeInf...
		ArrayList<KNNNodeInf> list = (ArrayList<KNNNodeInf>) listIn;
		
		int depth = 0;
		// Random Select Pivot
		int pivot = RSelectPiviot(list);
		
		// Root Node
		rootnode = new KDNode<Datatype>(0,list.get(pivot).getPos(),(Datatype)list.get(pivot).getObject());
		
		// Left
		rootnode.setLeftChild(split(depth+1,rootnode, list.subList(0, pivot)));
		
		// Right
		rootnode.setRightChild(split(depth+1,rootnode, list.subList(pivot+1, list.size())));
		
	}
	
	private KDNode<Datatype> split(int depth,KDNode<Datatype> parent,List<KNNNodeInf> list)
	{
		if(list.size() < 1)
		{
			return null;
		}
		
		int k=(depth % dim);
		
		int pivot = RSelectPiviot(list);

		KDNode<Datatype> node = new KDNode<Datatype>(0,list.get(pivot).getPos(),(Datatype)list.get(pivot).getObject());

		if(parent.isValueGreater(k, node.getPos())) // Larger
		{
			
			// Left
			node.setLeftChild(split(depth+1,node, list.subList(0, pivot)));
			
			
			// Right
			node.setRightChild(split(depth+1,node, list.subList(pivot+1, list.size())));
			
		}
		else // Smaller
		{
			
			// Right
			node.setRightChild(split(depth+1,node, list.subList(0, pivot)));
			
			
			// Left
			node.setLeftChild(split(depth+1,node, list.subList(pivot+1, list.size())));
			
		}

		return node;		
	}
	
	public void dump()
	{
		int depth = 0;
		System.out.println("Depth : " + depth + " x" + rootnode.getPos()[0] + " y" + rootnode.getPos()[1]);
		
		dumpChildern(depth+1,rootnode);
		
	}
	
	private void dumpChildern(int depth,KDNode<Datatype> parent)
	{
		
		KDNode<Datatype> left = parent.getLeftChild();		
		
		if(left!=null)
		{
			System.out.println("Depth : " + depth + " Left x" + left.getPos()[0] + " y" + left.getPos()[1]);
		}
		
		KDNode<Datatype> right = parent.getRightChild();
		if(right!=null)
		{
			System.out.println("Depth : " + depth + " Right x" + right.getPos()[0] + " y" + right.getPos()[1]);
		}
		
		if(left!=null)
		{
			dumpChildern(depth+1,left);
		}
		
		if(right!=null)
		{
			dumpChildern(depth+1,right);		
		}
		
	}
	
	private int RSelectPiviot(List<KNNNodeInf> list)
	{
		return r.nextInt(list.size());		
	}
	
	/* Axis Sort Comparators */
    static final Comparator<KDNode> X_AXIS_ORDER = new Comparator<KDNode>() 
	{
	    public int compare(KDNode n1, KDNode n2) 
	    {
	        return Double.compare(n1.getPos()[0],n2.getPos()[0]);
	    }
	};
	
    static final Comparator<KDNode> Y_AXIS_ORDER = new Comparator<KDNode>() 
	{
	    public int compare(KDNode n1, KDNode n2) 
	    {
	        return Double.compare(n1.getPos()[1],n2.getPos()[1]);
	    }
	};
	
}

