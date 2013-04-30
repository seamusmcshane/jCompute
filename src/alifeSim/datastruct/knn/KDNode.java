package alifeSim.datastruct.knn;

public class KDNode<Datatype>
{

	double pos[];
	Datatype data;

	KDNode<Datatype> left;
	KDNode<Datatype> right;
	
	public KDNode(double kd[], Datatype data)
	{
		pos = kd;
		this.data = data;
	}

	public boolean isValueGreater(int k,double kd[])
	{
		if( pos[k] > kd[k])
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
	
	public KDNode<Datatype> getLeft()
	{
		return left;
	}

	public void setLeft(KDNode<Datatype> left)
	{
		this.left = left;
	}

	public KDNode<Datatype>getRight()
	{
		return right;
	}

	public void setRight(KDNode<Datatype> right)
	{
		this.right = right;
	}
	
}