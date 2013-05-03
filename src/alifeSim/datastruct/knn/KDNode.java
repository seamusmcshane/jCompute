package alifeSim.datastruct.knn;

public class KDNode<Datatype>
{
	int dim;
	int nodeDepth;
	double pos[];
	Datatype data;

	KDNode<Datatype> branch[];
	
	public KDNode(int kd,int depth,double pos[], Datatype data)
	{
		this.pos = pos;
		this.nodeDepth = depth;
		this.data = data;
		this.branch = new KDNode[kd]; // Create kd leaf branches
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
	
	public KDNode<Datatype> getLeaf(int k)
	{
		//System.out.println("get leaf k : " + k);

		return branch[k];
	}
}