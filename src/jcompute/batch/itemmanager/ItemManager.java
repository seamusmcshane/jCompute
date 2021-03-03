package jcompute.batch.itemmanager;

import java.util.ArrayList;
import java.util.LinkedList;

import jcompute.batch.batchitem.BatchItem;

public class ItemManager
{
	// Our Queue of Items yet to be processed
	private LinkedList<BatchItem> queuedItems;
	
	private int totalItems = 0;
	
	// Items Management
	private int itemsRequested = 0;
	private int itemsReturned = 0;
	
	// The active Items currently being processed.
	private ArrayList<BatchItem> activeItems;
	
	private int active = 0;
	
	/**
	 * This object is not thread safe.
	 * It is expected to be used in the batch object, protected by the batch lock.
	 */
	public ItemManager()
	{
		queuedItems = new LinkedList<BatchItem>();
		
		activeItems = new ArrayList<BatchItem>();
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
		BatchItem temp = queuedItems.remove();
		
		activeItems.add(temp);
		
		active = activeItems.size();
		
		itemsRequested++;
		
		return temp;
	}
	
	public int getItemsRequested()
	{
		return itemsRequested;
	}
	
	public void returnItem(BatchItem item)
	{
		activeItems.remove(item);
		
		active = activeItems.size();
		
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
		
		activeItems = null;
	}
	
	public void setNotActive(BatchItem item)
	{
		activeItems.remove(item);
		
		active = activeItems.size();
	}
	
	public int getTotalActiveItems()
	{
		return active;
	}
}
