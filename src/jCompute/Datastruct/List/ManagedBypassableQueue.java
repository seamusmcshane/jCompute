package jCompute.Datastruct.List;

import jCompute.Datastruct.List.Interface.StoredQueuePosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ManagedBypassableQueue implements Iterable
{
	private ArrayList<StoredQueuePosition> baseList;

	/**
	 * Creates a managed bypassable queue
	 */
	public ManagedBypassableQueue()
	{
		baseList = new ArrayList<StoredQueuePosition>();
	}

	/**
	 * Creates a managed bypassable queue and initialises the underlying array
	 * list to an initial size;
	 */
	public ManagedBypassableQueue(int intialSize)
	{
		baseList = new ArrayList<StoredQueuePosition>(intialSize);
	}

	/**
	 * Adds the object the the queue
	 * 
	 * @param object
	 */
	public synchronized void add(StoredQueuePosition object)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to add null object to ManagedBypassableQueue");
		}
		
		baseList.add(object);
		
		// 1 indexed
		object.setPosition(baseList.size());
	}

	/**
	 * Insert object at the insert position, if valid, else append it to the
	 * end.
	 * 
	 * @param object
	 * @param insertPosition
	 */
	public synchronized void insert(StoredQueuePosition object, int insertPosition)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to add insert object to ManagedBypassableQueue at position " + insertPosition);
		}
		
		ArrayList<StoredQueuePosition> baseListTemp = new ArrayList<StoredQueuePosition>(baseList.size() * 2);
		Iterator<StoredQueuePosition> itr = baseList.iterator();
		StoredQueuePosition temp = null;
		int index = 0;

		boolean inserted = false;

		while (itr.hasNext())
		{

			if (index == insertPosition)
			{
				baseListTemp.add(object);
				
				// Set queue position
				object.setPosition(baseListTemp.size());
				inserted = true;
			}

			temp = itr.next();

			baseListTemp.add(temp);

			index++;
		}

		// Add at the end
		if (!inserted)
		{
			baseListTemp.add(object);
			
			// Update queue position
			object.setPosition(baseListTemp.size());
		}

		baseList = baseListTemp;

	}

	/**
	 * Retrieves but does not remove the first item in the queue
	 * 
	 * @return
	 */
	public synchronized StoredQueuePosition peek()
	{
		StoredQueuePosition first = null;

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
	public synchronized StoredQueuePosition poll()
	{
		StoredQueuePosition first = null;

		if (baseList.size() > 0)
		{
			first = baseList.remove(0);
		}

		// Refresh queue positions as an object has been removed
		updateQueuePositions();

		return first;
	}

	public synchronized StoredQueuePosition get(int pos)
	{
		StoredQueuePosition temp = null;

		if (pos >= 0 && pos < baseList.size())
		{
			temp = baseList.get(pos);
		}

		return temp;
	}

	private void updateQueuePositions()
	{
		Iterator<StoredQueuePosition> itr = baseList.iterator();
		StoredQueuePosition temp = null;

		// Refresh queue positions as an object has been removed
		int index=1;
		while (itr.hasNext())
		{

			temp = itr.next();
			
			temp.setPosition(index);

			index++;
		}
	}
	
	/**
	 * Removes the object from the queue
	 * 
	 * @param object
	 * @return
	 */
	public synchronized boolean remove(StoredQueuePosition object)
	{
		if(object == null)
		{
			 throw new NullPointerException("Attempted to remove null  from ManagedBypassableQueue");
		}
		
		boolean removed =  baseList.remove(object);
		
		
		if(removed)
		{
			updateQueuePositions();
		}

		
		return removed;
	}
	
	/**
	 * Removes the object at postion from the queue
	 * 
	 * @param object
	 * @return
	 */
	public synchronized StoredQueuePosition remove(int position)
	{		
		StoredQueuePosition object = baseList.remove(position);
		
		updateQueuePositions();
		
		return object;
	}

	/**
	 * Moves the object forward in the queue if it is possible.
	 * 
	 * @param object
	 */
	public synchronized void moveForward(StoredQueuePosition object)
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

		updateQueuePositions();
	}

	/**
	 * Moves the object to the front of the queue if it is possible.
	 * 
	 * @param object
	 */
	public synchronized void moveToFront(StoredQueuePosition object)
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

		// Invalid object or already at front
		if (currentPosition == -1 || currentPosition == 0)
		{
			return;
		}


		// Remove the object from its current position
		baseList.remove(object);
		
		// Insert the object at the front
		baseList.add(0, object);
		
		updateQueuePositions();		
	}
	
	/**
	 * Moves the object backward in the queue if it is possible.
	 * 
	 * @param object
	 */
	public synchronized void moveBackward(StoredQueuePosition object)
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
		
		updateQueuePositions();
	}

	/**
	 * Finds the position of any object in the list
	 * 
	 * @param object
	 */
	private int findPosition(StoredQueuePosition object)
	{
		int position = -1;
		int index = 0;
		Iterator<StoredQueuePosition> itr = baseList.iterator();
		StoredQueuePosition temp = null;

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
	 * Searches the queue for the object and returns it if found.
	 * 
	 * @param object
	 * @return
	 */
	public synchronized StoredQueuePosition searchObject(StoredQueuePosition object)
	{
		StoredQueuePosition temp = null;

		Iterator<StoredQueuePosition> itr = baseList.iterator();

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
	public Iterator<StoredQueuePosition> iterator()
	{
		Iterator<StoredQueuePosition> itr = new Iterator<StoredQueuePosition>()
		{
			private int index = 0;

			@Override
			public boolean hasNext()
			{				
				return ( baseList.size() != 0  &&  index < baseList.size() );
			}

			@Override
			public StoredQueuePosition next()
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
