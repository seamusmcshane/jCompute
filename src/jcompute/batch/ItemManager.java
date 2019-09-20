package jcompute.batch;

import java.util.ArrayList;
import java.util.LinkedList;

public class ItemManager
{
	// Our Queue of Items yet to be processed
	private LinkedList<BatchItem> queuedItems;
	
	private int totalItems = 0;
	
	// Items Management
	private int itemsRequested = 0;
	private int itemsReturned = 0;
	
	/**
	 * This object is not thread safe.
	 * It is expected to be used in the batch object, protected by the batch lock.
	 */
	public ItemManager()
	{
		queuedItems = new LinkedList<BatchItem>();
	}
	
	public void addItem(BatchItem item)
	{
		queuedItems.add(item);
		
		totalItems++;
	}
	
	public void addItems(ArrayList<BatchItem> itemList)
	{
		queuedItems.addAll(itemList);
		
		// Adding the itemList size as total items is the total that ever existed in queud items, which may have had items dequeued.
		totalItems += itemList.size();
	}
	
	public int getTotalItems()
	{
		return totalItems;
	}
	
	public int getCurrentItems()
	{
		return queuedItems.size();
	}
	
	public BatchItem getNext()
	{
		itemsRequested++;
		
		return queuedItems.remove();
	}
	
	public int getItemsRequested()
	{
		return itemsRequested;
	}
	
	public void returnItem(BatchItem item)
	{
		queuedItems.add(item);
		
		itemsReturned++;
	}
	
	public int getItemsReturned()
	{
		return itemsReturned;
	}
	
	public void compact()
	{
		queuedItems = null;
	}
}
