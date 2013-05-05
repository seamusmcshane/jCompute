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
		//System.out.println("<<<<<<<<<<<Search Start>>>>>>>>>>>>>>>>>");
		KDNode<Datatype> node = findNearest(0,root,pos); // start search from the root
		
		double dis = DistanceFunctions.SquaredEuclidienDistance2D(node.getPos(), pos);
		
		//System.out.println("<<<<<<<<<<<Search End NNDis + " + dis + " >>>>>>>>>>>>>>>>>");
		/*try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return node.getData();
	}

	private KDNode<Datatype> findNearest(int depth,KDNode<Datatype> node,double pos[])
	{
		KDNode<Datatype> nearestNode = node; // Current Node unless we set a better one
		KDNode<Datatype> recNode = null; 	// Node Found in rec search

		int k=(depth % dim);
				
		double currentNodeDis = DistanceFunctions.SquaredEuclidienDistance2D(node.getPos(), pos);
		double nearestNodeDis = currentNodeDis;
		double recDis = Double.POSITIVE_INFINITY;
		
		//System.out.println("vvvv Depth : " + depth);
		
		/* Right is greater */
		if( (pos[k]) > (node.getPos()[k]) )
		{
			//System.out.println(">>>> Depth : Going Right" + depth);

			if(node.getRightChild()!=null)
			{
				recNode = findNearest(depth+1,node.getRightChild(),pos);
				
				// Nears dis is the nearest node out of the recursion
				recDis = DistanceFunctions.SquaredEuclidienDistance2D(recNode.getPos(), pos);									
				
				/* is current Node closer than the nearest node out of the recursion */
				if(recDis < nearestNodeDis)
				{
					//System.out.println(">>>> Depth : " + depth + " New NN " + recDis + " Prev " + nearestNodeDis);

					nearestNode = recNode;
					nearestNodeDis = recDis;
				}

			}

			int altk=( (depth+1) % dim);
			/* Is it possible a node on the other side tree is closer */					
			double kdis = DistanceFunctions.SquaredEuclidienDistance1D(nearestNode.getPos()[altk], pos[altk]);
			
			/* If yes check the left tree*/
			//if(kdis < nearestNodeDis)
			{
				KDNode<Datatype> nearestNodeAlt = null;
				double nearestNodeDisAlt;

				/* Before transversing check its not null */
				if(node.getLeftChild()!=null)
				{
					nearestNodeAlt = findNearest(depth+1,node.getLeftChild(),pos);						
					nearestNodeDisAlt = DistanceFunctions.SquaredEuclidienDistance2D(nearestNodeAlt.getPos(), pos);
					
					if(nearestNodeDisAlt < nearestNodeDis)
					{
						//System.out.println("<<<< Depth : " + depth + " New NN " + recDis + " Prev " + nearestNodeDis);					
						nearestNode = nearestNodeAlt;
						nearestNodeDis = nearestNodeDisAlt;
					}
					
				}

			}				
			
		}
		else /* Left is less */
		{
			//System.out.println("<<<< Depth : Going left" + depth);
				
			if(node.getLeftChild()!=null)
			{
				recNode = findNearest(depth+1,node.getLeftChild(),pos);
				
				// Nears dis is the nearest node out of the recursion
				recDis = DistanceFunctions.SquaredEuclidienDistance2D(recNode.getPos(), pos);									
				
				/* is current Node closer than the nearest node out of the recursion */
				if(recDis < nearestNodeDis)
				{
					//System.out.println("<<<< Depth : " + depth + " New NN " + recDis + " Prev " + nearestNodeDis);					

					nearestNode = recNode;
					nearestNodeDis = recDis;

				}

			}
			
			int altk=( (depth+1) % dim);
			/* Is it possible a node on the other side tree is closer */					
			double kdis = DistanceFunctions.SquaredEuclidienDistance1D(nearestNode.getPos()[altk], pos[altk]);
			
			/* If yes check the left tree*/
			//if(kdis < nearestNodeDis)
			{
				KDNode<Datatype> nearestNodeAlt = null;
				double nearestNodeDisAlt;

				/* Before transversing check its not null */
				if(node.getRightChild()!=null)
				{
					nearestNodeAlt = findNearest(depth+1,node.getRightChild(),pos);						
					nearestNodeDisAlt = DistanceFunctions.SquaredEuclidienDistance2D(nearestNodeAlt.getPos(), pos);
					
					if(nearestNodeDisAlt < nearestNodeDis)
					{
						//System.out.println(">>>> Depth : " + depth + " New NN " + recDis + " Prev " + nearestNodeDis);					
						
						nearestNode = nearestNodeAlt;
						nearestNodeDis = nearestNodeDisAlt;

					}
					
				}

			}
		}

		//System.out.println("^^^^ Depth : Returning" + depth);

		return nearestNode;
	}
	
/*private Datatype wfindNearest(int depth,KDNode<Datatype> node,double pos[])
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
		
		//System.out.println("Compare dis to C Node " + currentNode);
		//System.out.println("Compare dis to L Node " + leftNode);
		//System.out.println("Compare dis to R Node " + rightNode);		

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
	}*/
	
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
