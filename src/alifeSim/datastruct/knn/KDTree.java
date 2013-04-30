package alifeSim.datastruct.knn;

import java.util.LinkedList;

public class KDTree<Datatype,Positon> implements KNNInf<Datatype>
{

	int dim;
	KDNode<Datatype> root;	
	
	@Override
	public void init(int dim)
	{
		this.root = null;
		this.dim = dim;		
	}

	@Override
	public void add(double kd[], Datatype data)
	{		
		int l=0;
		if(root == null)
		{			
			this.root = new KDNode<Datatype>(kd, data);			
			return;			
		}
		
		KDNode node = root;
		boolean done = false;
		l=0;
		
		while(!done)
		{			
			if(node.isValueGreater( (l%dim), kd)) // Left
			{							
				if(node.getLeft() == null)
				{
					node.setLeft(new KDNode(kd,data));
					done = true;
				}
				else
				{   
					// Move left
					node = node.getLeft();		
				}				
				
			}
			else // We are greater value
			{
				
				if(node.getRight() == null)
				{
					node.setRight(new KDNode(kd,data));					
				}
				else
				{   
					// Move Right
					node = node.getRight();
				}				
			}
			
			l++;
		}

	}

	@Override
	public void add(int[] kd, Datatype data)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void add(float[] kd, Datatype data)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Datatype nearestNeighbor(int[] kd)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datatype nearestNeighbor(float[] kd)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datatype nearestNeighbor(double[] kd)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Datatype> nearestNeighbors(int[] kd)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Datatype> nearestNeighbors(float[] kd)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Datatype> nearestNeighbors(double[] kd)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
