package jCompute.Datastruct.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ManagedBypassableQueue<Datatype> implements Iterable<Datatype>
{
	private ArrayList<Datatype> baseList;

	/**
	 * Creates a managed bypassable queue
	 */
	public ManagedBypassableQueue()
	{
		baseList = new ArrayList<Datatype>();
	}

	/**
	 * Creates a managed bypassable queue and initialises the underlying array
	 * list to an initial size;
	 */
	public ManagedBypassableQueue(int intialSize)
	{
		baseList = new ArrayList<Datatype>(intialSize);
	}

	/**
	 * Adds the object the the queue
	 * 
	 * @param object
	 */
	public synchronized void add(Datatype object)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to add null object to ManagedBypassableQueue");
		}
		
		baseList.add(object);
	}

	/**
	 * Insert object at the insert position, if valid, else append it to the
	 * end.
	 * 
	 * @param object
	 * @param insertPosition
	 */
	public synchronized void insert(Datatype object, int insertPosition)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to add insert object to ManagedBypassableQueue at position " + insertPosition);
		}
		
		ArrayList<Datatype> baseListTemp = new ArrayList<Datatype>(baseList.size() * 2);
		Iterator<Datatype> itr = baseList.iterator();
		Datatype temp = null;
		int index = 0;

		boolean inserted = false;

		while (itr.hasNext())
		{

			if (index == insertPosition)
			{
				baseListTemp.add(object);

				inserted = true;
			}

			temp = itr.next();

			baseListTemp.add(temp);

			index++;
		}

		if (!inserted)
		{
			baseListTemp.add(object);
		}

		baseList = baseListTemp;

	}

	/**
	 * Retrieves but does not remove the first item in the queue
	 * 
	 * @return
	 */
	public synchronized Datatype peek()
	{
		Datatype first = null;

		if (baseList.size() > 0)
		{
			first = baseList.get(0);
		}

		return first;
	}

	/**
	 * Removes the first item in the queue
	 * 
	 * @return
	 */
	public synchronized Datatype poll()
	{
		Datatype first = null;

		if (baseList.size() > 0)
		{
			first = baseList.remove(0);
		}

		return first;
	}

	public synchronized Datatype get(int pos)
	{
		Datatype temp = null;

		if (pos >= 0 && pos < baseList.size())
		{
			temp = baseList.get(pos);
		}

		return temp;
	}

	/**
	 * Removes the object from the queue
	 * 
	 * @param object
	 * @return
	 */
	public synchronized boolean remove(Datatype object)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to remove null  from ManagedBypassableQueue");
		}
		
		return baseList.remove(object);
	}
	
	/**
	 * Removes the object at postion from the queue
	 * 
	 * @param object
	 * @return
	 */
	public synchronized Datatype remove(int position)
	{		
		return baseList.remove(position);
	}

	/**
	 * Moves the object forward in the queue if it is possible.
	 * 
	 * @param object
	 */
	public synchronized void moveForward(Datatype object)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to move null  object forward in ManagedBypassableQueue");
		}
		
		// Cannot move objects forward in queue with 0 or 1 objects
		if (baseList.size() < 2)
		{
			return;
		}

		int currentPosition = findPosition(object);

		// 0 is front
		int newPosition = currentPosition - 1;

		// Invalid object or already at front
		if (currentPosition == -1 || currentPosition == 0)
		{
			return;
		}

		// Swap this object with the one in front of it
		Collections.swap(baseList, currentPosition, newPosition);

	}

	/**
	 * Moves the object backward in the queue if it is possible.
	 * 
	 * @param object
	 */
	public synchronized void moveBackward(Datatype object)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to move null  object backward in ManagedBypassableQueue");
		}
		
		// Cannot move backward forward in queue with 0 or 1 objects
		if (baseList.size() < 2)
		{
			return;
		}

		int currentPosition = findPosition(object);

		// 0 is front
		int newPosition = currentPosition + 1;

		// Invalid object or already at the end of the queue
		if (currentPosition == -1 || currentPosition == (baseList.size() - 1))
		{
			return;
		}

		// Swap this object with the one in front of it
		Collections.swap(baseList, currentPosition, newPosition);

	}

	/**
	 * Finds the position of any object in the list
	 * 
	 * @param object
	 */
	private int findPosition(Datatype object)
	{
		int position = -1;
		int index = 0;
		Iterator<Datatype> itr = baseList.iterator();
		Datatype temp = null;

		while (itr.hasNext())
		{
			temp = itr.next();

			if (temp == object)
			{
				position = index;
				break;
			}

			index++;
		}

		return position;
	}

	/**
	 * Returns the position of the object in the queue
	 * 
	 * @param object
	 * @return
	 */
	public synchronized int getQueuePosition(Datatype object)
	{
		return findPosition(object);
	}

	/**
	 * Searches the queue for the object and returns it if found.
	 * 
	 * @param object
	 * @return
	 */
	public synchronized Datatype searchObject(Datatype object)
	{
		Datatype temp = null;

		Iterator<Datatype> itr = baseList.iterator();

		while (itr.hasNext())
		{
			temp = itr.next();

			if (temp == object)
			{
				break;
			}
		}

		return temp;
	}

	/**
	 * Returns the size of the queue
	 * 
	 * @return
	 */
	public synchronized int size()
	{
		return baseList.size();
	}

	@Override
	public Iterator<Datatype> iterator()
	{
		Iterator<Datatype> itr = new Iterator<Datatype>()
		{
			private int index = 0;

			@Override
			public boolean hasNext()
			{				
				return ( baseList.size() != 0  &&  index < baseList.size() );
			}

			@Override
			public Datatype next()
			{
				return baseList.get(index++);
			}

			@Override
			public void remove()
			{
				if(index<0)
				{
					throw new IllegalStateException("");
				}
				
				ManagedBypassableQueue.this.remove(index);
				index--;
			}
		};
		return itr;
	}

}
