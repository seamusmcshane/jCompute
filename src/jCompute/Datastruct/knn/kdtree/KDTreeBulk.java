package jCompute.Datastruct.knn.kdtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jCompute.Datastruct.knn.KNNPosInf;

public class KDTreeBulk
{
	int dim;
	
	Random r = new Random();

	KDNode<KNNPosInf> rootnode;

	public KDTreeBulk(int dim)
	{		
		this.dim = dim;
	}


	public void load(ArrayList<KNNPosInf> listIn)
	{
		// Assume KNNPosInf supports KNNNodeInf...
		ArrayList<KNNPosInf> list = listIn;
		
		int depth = 0;
		// Random Select Pivot
		int pivot = RSelectPiviot(list);
		
		// Root Node
		rootnode = new KDNode<KNNPosInf>(0,list.get(pivot).getPos(),(KNNPosInf)list.get(pivot));
		
		// Left
		rootnode.setLeftChild(split(depth+1,rootnode, list.subList(0, pivot)));
		
		// Right
		rootnode.setRightChild(split(depth+1,rootnode, list.subList(pivot+1, list.size())));
		
	}
	
	private KDNode<KNNPosInf> split(int depth,KDNode<KNNPosInf> parent,List<KNNPosInf> list)
	{
		if(list.size() < 1)
		{
			return null;
		}
		
		int k=(depth % dim);
		
		int pivot = RSelectPiviot(list);

		KDNode<KNNPosInf> node = new KDNode<KNNPosInf>(0,list.get(pivot).getPos(),(KNNPosInf)list.get(pivot));

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
	
	private void dumpChildern(int depth,KDNode<KNNPosInf> parent)
	{
		
		KDNode<KNNPosInf> left = parent.getLeftChild();		
		
		if(left!=null)
		{
			System.out.println("Depth : " + depth + " Left x" + left.getPos()[0] + " y" + left.getPos()[1]);
		}
		
		KDNode<KNNPosInf> right = parent.getRightChild();
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
	
	private int RSelectPiviot(List<KNNPosInf> list)
	{
		return r.nextInt(list.size());		
	}
	
}

