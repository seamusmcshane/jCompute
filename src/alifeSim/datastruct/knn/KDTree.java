package alifeSim.datastruct.knn;

import java.util.LinkedList;

/**
 * Basic KD-Tree that supports K+ Dimensions
 */
public class KDTree<Datatype> implements KNNInf<Datatype>
{

	int dim;
	int nodeCount;
	KDNode<Datatype> root;	
	
	public KDTree(int dim)
	{
		this.root = null;
		nodeCount=0;
		this.dim = dim;	
		//System.out.println("Init : Node Count : " + nodeCount);
	}

	@Override
	public void add(double[] pos, Datatype data)
	{		
		if(root == null)
		{
			//System.out.println("ROOT " + root);
			root = new KDNode<Datatype>(dim,0,pos, data);
			nodeCount++;
			//System.out.println("ROOT " + root);
		}
		else
		{
			insert(0,root,pos,data);	// start from the root node/ level 0	
		}

	}
	
	// Dynamic Insert
	private void insert(int depth, KDNode<Datatype> node ,double[] pos, Datatype data)
	{
		int k=(depth % dim);
		
		if(node.isValueGreater(k, pos)) // Parent is higher on the Dim
		{
		
			if(node.getLeftChild() != null)
			{
				insert(depth+1,node.getLeftChild(),pos,data);
			}
			else
			{
				node.setLeftChild(new KDNode<Datatype>(dim,depth+1,pos, data));
				//System.out.println("Node " + node +" set left node " + node.getLeftChild());
				nodeCount++;
			}
			
		}
		else // Child is higher on the dim
		{
			if(node.getRightChild() != null)
			{
				insert(depth+1,node.getRightChild(),pos,data);
			}
			else
			{
				node.setRightChild(new KDNode<Datatype>(dim,depth+1,pos, data));
				//System.out.println("Node " + node +" set right node " + node.getRightChild());
				nodeCount++;
			}
		}

	}

	@Override
	public Datatype nearestNeighbour(double[] pos)
	{
		//System.out.println("Search Start Node Count : " + nodeCount);
		
		if(root == null)
		{
			System.out.println("<<<<<<<<<<<Search found ROOT NULL>>>>>>>>>>>>>>>>>");
		}
		return findNearest(0,root,pos); // start search from the root
	}

	private Datatype findNearest(int depth,KDNode<Datatype> node,double pos[])
	{
		
		//int k=(depth % dim);
		Datatype nearestNodeData = null;

		double currentNode = DistanceFunctions.SquaredEuclidienDistance2D(pos,node.getPos());	
		
		double leftNode = Double.MAX_VALUE;
		double rightNode = Double.MAX_VALUE;
		
		if( (node.getLeftChild() == null) &&  (node.getRightChild() == null) )
		{
			nearestNodeData =  node.getData();
		}
				
		if(node.getLeftChild() != null)
		{
			leftNode =  DistanceFunctions.SquaredEuclidienDistance2D(node.getLeftChild().getPos(), pos);
		}

		if(node.getRightChild() != null)
		{
			rightNode =  DistanceFunctions.SquaredEuclidienDistance2D(node.getRightChild().getPos(), pos);
		}
		
		/*System.out.println("Compare dis to C Node " + currentNode);
		System.out.println("Compare dis to L Node " + leftNode);
		System.out.println("Compare dis to R Node " + rightNode);		*/	

		if(leftNode < currentNode)
		{

			if(leftNode<rightNode)
			{
				//System.out.println("left is closer (nodeCount " + nodeCount + ")");
				nearestNodeData = findNearest(depth+1,node.getLeftChild(),pos);	
			}
			else
			{
				//System.out.println("right is closer (nodeCount " + nodeCount + ")");
				nearestNodeData = findNearest(depth+1,node.getRightChild(),pos);
			}
			
		}
		else if(rightNode < currentNode)
		{
			if(leftNode<rightNode)
			{
				//System.out.println("left is closer (nodeCount " + nodeCount + ")");
				nearestNodeData = findNearest(depth+1,node.getLeftChild(),pos);	
			}
			else
			{
				//System.out.println("right is closer (nodeCount " + nodeCount + ")");
				nearestNodeData = findNearest(depth+1,node.getRightChild(),pos);
			}
		}
		else
		{
			//System.out.println("currentNode is closer (nodeCount " + nodeCount + ")");
			nearestNodeData = node.getData();
		}
		
		return nearestNodeData;
	}
	
	public int size()
	{
		return nodeCount;
	}
	
	@Override
	public LinkedList<Datatype> nearestNeighbours(double[] pos)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datatype nearestNNeighbour(double[] pos, int n)
	{
		System.out.println("Search Start");

		return null;
	}
	

}
