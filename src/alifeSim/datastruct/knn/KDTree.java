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
	
	@Override
	public void init(int dim)
	{
		this.root = null;
		nodeCount=0;
		this.dim = dim;	
		System.out.println("Init : Node Count : " + nodeCount);
	}

	@Override
	public void add(double[] pos, Datatype data)
	{		
		if(root == null)
		{
			root = new KDNode<Datatype>(dim,0,pos, data);
			//System.out.println("add ROOT NULL");
		}
		else
		{
			insert(0,root,pos,data);	// start from the root node/ level 0	
		}

	}
	
	// Dynamic Insert
	private void insert(int depth, KDNode<Datatype> node ,double[] pos, Datatype data)
	{
		if(node == null)
		{
			node = new KDNode<Datatype>(dim,depth,pos, data);
			nodeCount++;
			//System.out.println("Node null Insert : Node Count : " + nodeCount);
			return;
		}
		
		for(int k=0;k<dim;k++)
		{
			if(node.isValueGreater(k, pos) == true)
			{
				
				insert(depth+1,node.getLeaf(k),pos,data);
				
				return;
			} // else loop to the next kdim...
						
		}

	}

	@Override
	public Datatype nearestNeighbour(double[] pos)
	{
		if(root == null)
		{
			System.out.println("ROOT NULL");
		}
		
		return findNearest(0,root,pos); // start search from the root
	}

	private Datatype findNearest(int depth,KDNode<Datatype> node,double pos[])
	{
		
		KDNode<Datatype> branchCheck = null;
		for(int i=0;i<dim;i++)
		{
			branchCheck = node.getLeaf(i);
			
			if(branchCheck!=null) // we have a branch
			{
				break;
			}
			
		}
		
		if(branchCheck == null) // no more branch .... end of tree -- search end
		{
			return node.getData();
		}
		
		double current_value = node.getPos()[depth%dim]; // Get the nodes value of the dim for this level
		double compare_value = pos[depth%dim]; 			 // The value of the dim for this level
				
		int sk = depth%dim; // start at the correct K
		
		if(compare_value < current_value)
		{
			return findNearest(depth+1,node.getLeaf(sk),pos); // current dim
		}
		else if(compare_value == current_value)
		{
			return node.getData();
		}			
		else
		{
			return findNearest(depth+1,node.getLeaf( (sk+1) % dim ),pos); // next dim
		}	
		
	}
	
	public int size()
	{
		return nodeCount;
	}
	
	/** tuple function search_kd_tree(int depth, tuple tree, tuple point):
	    if tree is a single point:
	        return the point

	    meanValue = the value of the root node of tree

	    if depth is even:
	        comparisonValue = the x-value of the point
	    else:
	        comparisonValue = the y-value of the point

	    if comparisonValue < meanValue :
	        subtree = left-hand subtree of tree
	    else:
	        subtree = right-hand subtree of tree

	    return search_kd_tree(depth + 1, subtree, point)
	*/
	@Override
	public LinkedList<Datatype> nearestNeighbours(double[] pos)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datatype nearestNNeighbour(double[] pos, int n)
	{
		// TODO Auto-generated method stub
		return null;
	}
	

}
