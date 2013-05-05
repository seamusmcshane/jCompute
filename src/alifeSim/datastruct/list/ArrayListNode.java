package alifeSim.datastruct.list;

public class ArrayListNode<Datatype> implements  NodeInf<ArrayListNode,Datatype>
{

	Datatype data;
	double val;
	public ArrayListNode()
	{
		data = null;		
	}
	
	public void setNode(Datatype data,double val)
	{
		this.val = val;
		this.data = data;
	}
	
	@Override
	public Datatype getData()
	{		
		return data;
	}

	@Override
	public double getVal()
	{
		// TODO Auto-generated method stub
		return val;
	}

}
