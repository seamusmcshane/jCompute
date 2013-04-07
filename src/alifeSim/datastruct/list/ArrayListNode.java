package alifeSim.datastruct.list;

public class ArrayListNode<Datatype> implements  NodeInf<ArrayListNode,Datatype>
{

	Datatype data;
	
	public ArrayListNode()
	{
		data = null;		
	}
	
	public ArrayListNode(Datatype data)
	{
		this.data = data;
	}

	@Override
	public void setData(Datatype data)
	{
		this.data = data;		
	}

	@Override
	public Datatype getData()
	{		
		return data;
	}

	@Override
	public ArrayListNode getNext()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayListNode getPrev()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNext(ArrayListNode node)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPrev(ArrayListNode node)
	{
		// TODO Auto-generated method stub
		
	}
}
