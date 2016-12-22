package jcompute.batch.itemstore;

import java.util.Comparator;

public class ItemDiskCacheComparator implements Comparator<ItemDiskCacheEntry>
{
	@Override
	public int compare(ItemDiskCacheEntry item1, ItemDiskCacheEntry item2)
	{
		long item1AddedTime = item1.getTimeAdded();
		long item2AddedTime = item2.getTimeAdded();
		
		long item1AccessTime = item1.getLastAccessTime();
		long item2AccessTime = item2.getLastAccessTime();
		
		// Assumes time value increases (larger msec is more recent)
		boolean item1Older = (item1AddedTime < item2AddedTime);
		boolean itemOneMoreRecent = (item1AccessTime > item2AccessTime);
		
		// If more recently accessed
		if(itemOneMoreRecent)
		{
			if(item1Older)
			{
				// and older then move up
				return -1;
			}
			else
			{
				return 1;
			}
			
		}
		else
		{
			if(item1Older)
			{
				// and older then move up
				return -1;
			}
			else
			{
				// more recent and younger move up
				return 1;
			}
		}
	}
}
