package jcompute.datastruct.knn;

import ags.utils.dataStructures.KNN1.KNN1SearchRule;

public class KNN1AgentSearchRule extends KNN1SearchRule
{
	public KNN1AgentSearchRule(float[] searchPoint, Object searchObj)
	{
		super(searchPoint, searchObj);
		
		obj = null;
		dis = Integer.MAX_VALUE;
	}
	
	@Override
	public void replaceIfMatchKNNRule(Object obj, float dis)
	{
		// Not Less than current dis or we have found our self in the tree.
		if(dis > this.dis)
		{
			return;
		}
		
		if(obj == SEARCH_OBJ)
		{
			return;
		}
		
		this.obj = obj;
		this.dis = dis;
	}
}
