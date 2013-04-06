package alifeSim.datastruct;

public class ArrayList<Datatype> implements ListInf<Datatype>
{
	private ArrayListNode<Datatype>[] arrayList;
	private ArrayListNode<Datatype>[] tArrayList;
	private int position;
		
	private int nodeCount;
	
	private int size;

	private int sizeAdj;
	
	/* Default Constructor */
	public ArrayList()
	{
		this(10000);		// default size
	}
	
	/* Size based constructor */
	public ArrayList(int size)
	{
		this.size = size;
		
		sizeAdj = 2; // x2 double list size on reach capacity
		
		arrayList = new ArrayListNode[size];
		
		// Create all the nodes
		for(position=0;position<size;position++)
		{
			arrayList[position] = new ArrayListNode<Datatype>();			
		}
		
		position = 0;
		
		nodeCount = 0;
		
		//System.out.println("Size " + size);
	}

	@Override
	public void add(Datatype data)
	{
		//System.out.println("Size " + size + " Pos " + position);
		// Arrays....
		if( (position) == size-1 )
		{
			increaseListSize();
		}
		
		arrayList[position].setData(data);	
		
		nodeCount++;
		position++;
	}

	private void increaseListSize()
	{
		System.out.println("List Size too small....");
		int newSize = size*sizeAdj;
		tArrayList = new ArrayListNode[newSize];
		
		int tPos = 0;
		
		while(arrayList[tPos].getData()!=null)
		{
			tArrayList[tPos] = new ArrayListNode<Datatype>();	
			tArrayList[tPos].setData(arrayList[tPos].getData());
			tPos++;
		}
		
		while(tPos<newSize)
		{
			tArrayList[tPos] = new ArrayListNode<Datatype>();
			tPos++;
		}
		
		arrayList = tArrayList;
		size = newSize;
		
	}
	
	@Override
	public Datatype remove()
	{	
		// Not implemented
		return null;
	}

	@Override
	public void delete()
	{
		// Not implemented
	}

	@Override
	public Datatype get()
	{		
		return arrayList[position].getData();
	}

	@Override
	public Datatype getNext()
	{				
		Datatype temp = arrayList[position].getData();
		
		position++;
		
		return temp;
	}
	
	/* Get a particular position */
	public Datatype getPos(int pos)
	{		
		Datatype temp;

		if(pos>size)
		{
			temp = null;
		}		
		else
		{
			temp = arrayList[position].getData();	
		}
		
		return temp;	
	}

	@Override
	public boolean hasNext()
	{
		boolean status = true;
		
		if(arrayList[position+1].getData()== null)
		{
			status = false;
		}
		
		return status;
	}

	@Override
	public void resetHead()
	{
		position=0;
	}

	public int getNodeCount()
	{
		return nodeCount;
	}

}
