package alifeSim.datastruct.list;

import java.util.Collections;

public class ArrayList<Datatype> implements ListInf<Datatype>
{
	private ArrayListNode<Datatype>[] arrayList;
	private ArrayListNode<Datatype>[] tArrayList;
	private int position;
		
	private int nodeCount;
	
	private int size;

	private int sizeAdj;
		
	private double min = Double.POSITIVE_INFINITY;
	private double max = 0;
	
	private double medianVal = min + (max/2);
	private double bestMedian = 0;
	private int medianPos = 0;
	
	/* Default Constructor */
	public ArrayList()
	{
		this(100);		// default size
	}
	
	/* Size based constructor */
	public ArrayList(int size)
	{
		if(size<10)
		{
			size = 10;
		}
		
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
	public void add(Datatype data, double val)
	{
		//System.out.println("Size " + size + " Pos " + position);
		// Arrays....
		if( (position) == size-1 )
		{
			increaseListSize();
		}
				
		arrayList[position].setNode(data, val);
		
		nodeCount++;
		position++;
	}
	
	private void updateMedian()
	{
		
		min = Double.POSITIVE_INFINITY;
		max = 0;
		
		medianVal = min + (max/2);
		bestMedian = 0;
		medianPos = 0;
		
		for(int i = 0;i<arrayList.length;i++)
		{
			if(arrayList[i].getVal()<min)
			{
				min = arrayList[i].getVal();
			}
			
			if(arrayList[i].getVal()>max)
			{
				max = arrayList[i].getVal();
			}
			
			medianVal = (min/2) + (max/2);
			
			if( Math.abs(arrayList[i].getVal() - medianVal) < Math.abs(bestMedian - medianVal))
			{
				bestMedian = arrayList[i].getVal();
				medianPos = i;
			}
		}
		
	}
	
	private void increaseListSize()
	{
		//System.out.println(size + " List Size too small....");
		int newSize = size*sizeAdj;
		tArrayList = new ArrayListNode[newSize];
		
		int tPos = 0;
		
		while(arrayList[tPos].getData()!=null)
		{
			//tArrayList[tPos] = new ArrayListNode<Datatype>();	
			tArrayList[tPos] = arrayList[tPos];
			tPos++;
		}
		
		while(tPos<newSize)
		{
			tArrayList[tPos] = new ArrayListNode<Datatype>();
			tPos++;
		}
		
		arrayList = tArrayList;
		size = newSize;

		//System.out.println(size + " new Size");

		
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

		if(pos >=nodeCount)
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
		boolean status = false;
		
		if(position < nodeCount)
		{
			if(arrayList[position].getData() != null)
			{
				status = true;
			}
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

	@Override
	public Datatype getMedianNode()
	{
		updateMedian();
		
		// TODO Auto-generated method stub
		return arrayList[medianPos].getData();
	}

}
