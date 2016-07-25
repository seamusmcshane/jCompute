package jcompute.datastruct.heap;

import java.util.Arrays;

public class BinaryHeap<Datatype>
{
	// Min or Max
	private final int order;
	
	// Sorting Keys
	private float[] keys;
	
	// Data indexed
	private Datatype[] values;
	
	// Heap capacity before resize
	private int capacity;
	
	// Current
	private int occupancy;
	
	@SuppressWarnings("unchecked")
	/**
	 * Array backed binary heap supporting min or max operation.
	 * 
	 * @param type
	 * @param initialCapacity
	 */
	public BinaryHeap(HeapType type, int initialCapacity)
	{
		// Heap sorting order
		order = type.order;
		
		// > 0
		this.capacity = initialCapacity;
		
		// The sorting values.
		keys = new float[capacity + 1];
		
		// Allocate an array of the generic type - warning suppressed
		values = (Datatype[]) new Object[capacity + 1];
		
		// Empty
		occupancy = 0;
		
		// Place a debug marker at index 0.
		keys[0] = Float.NaN;
	}
	
	public void push(float key, Datatype value)
	{
		// Apply limits
		checkSize();
		
		// Update
		occupancy++;
		
		// Append to end of heap
		keys[occupancy] = key;
		values[occupancy] = value;
		
		// Sift up / Percolate up
		int currentIndex = occupancy;
		
		// Avoids repeated / 2
		int cid2 = 0;
		
		while(currentIndex > 0)
		{
			cid2 = currentIndex / 2;
			
			// Current Less than Parent - Do Swap
			if((order * keys[currentIndex]) < (order * keys[cid2]))
			{
				// Temp
				float ktmp = keys[cid2];
				Datatype vtmp = values[cid2];
				
				// Child to Parent
				keys[cid2] = keys[currentIndex];
				values[cid2] = values[currentIndex];
				
				// Parent to Child
				keys[currentIndex] = ktmp;
				values[currentIndex] = vtmp;
			}
			
			// Switch to parent
			currentIndex = currentIndex / 2;
		}
	}
	
	// Top of heap
	public Datatype peek()
	{
		return values[1];
	}
	
	// Remove top of heap
	public Datatype poll()
	{
		// Latch top
		Datatype topValue = values[1];
		
		// Store bottom
		keys[1] = keys[occupancy];
		values[1] = values[occupancy];
		
		// Clear Bottom
		keys[occupancy] = 0;
		values[occupancy] = null;
		
		// Reduce size
		occupancy--;
		
		// Sift down / Percolate down
		int currentIndex = 1;
		
		while((currentIndex * 2) <= occupancy)
		{
			int minChild;
			
			int cim2 = currentIndex * 2;
			
			if(cim2 + 1 > occupancy)
			{
				minChild = currentIndex * 2;
			}
			else
			{
				if((order * keys[cim2]) < (order * keys[cim2 + 1]))
				{
					minChild = cim2;
				}
				else
				{
					minChild = cim2 + 1;
				}
			}
			
			// Child is smaller - Do Swap
			if((order * keys[minChild]) < (order * keys[currentIndex]))
			{
				// Temp
				float ktmp = keys[currentIndex];
				Datatype vtmp = values[currentIndex];
				
				// Min to Current
				keys[currentIndex] = keys[minChild];
				values[currentIndex] = values[minChild];
				
				// Current to Min
				keys[minChild] = ktmp;
				values[minChild] = vtmp;
			}
			
			// Switch to child
			currentIndex = minChild;
		}
		
		return topValue;
	}
	
	public int contents()
	{
		return occupancy;
	}
	
	public boolean isEmpty()
	{
		return occupancy == 0;
	}
	
	// Check Size
	private void checkSize()
	{
		if(occupancy == capacity - 1)
		{
			// Double
			capacity *= 2;
			
			// Create sized arrays with old data
			keys = Arrays.copyOf(keys, capacity);
			values = Arrays.copyOf(values, capacity);
		}
	}
	
	public void dumpHeapArray()
	{
		for(int i = 0; i < occupancy + 1; i++)
		{
			System.out.println("i " + i + " val " + keys[i]);
		}
	}
	
	/**
	 * Heap Type Enum
	 * 
	 * @author Seamus McShane
	 */
	public enum HeapType
	{
		MIN(1), MAX(-1);
		
		public final int order;
		
		private HeapType(int val)
		{
			order = val;
		}
	}
}
