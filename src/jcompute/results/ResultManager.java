package jcompute.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import jcompute.results.binary.BinaryDataFileCollection;
import jcompute.results.custom.CustomItemResultInf;
import jcompute.results.trace.group.TraceGroupSetting;
import jcompute.results.trace.group.TraceGroup;

/**
 * Statistics manager - Not Thread Safe.
 */
public class ResultManager
{
	private String managerName;
	
	private HashMap<String, TraceGroup> traceGroupMap;
	private ArrayList<TraceGroup> traceGroupList;
	
	private ArrayList<BinaryDataFileCollection> binaryDataList;
	
	private ArrayList<CustomItemResultInf> customItemResults;
	
	private Semaphore lock = new Semaphore(1);
	
	public ResultManager(String managerName)
	{
		this.managerName = managerName;
		
		traceGroupMap = new HashMap<String, TraceGroup>();
		
		binaryDataList = new ArrayList<BinaryDataFileCollection>();
		
		customItemResults = new ArrayList<CustomItemResultInf>();
	}
	
	/**
	 * Add a new trace group to the manager
	 * 
	 * @param group
	 */
	public void registerTraceGroup(TraceGroup group)
	{
		lock.acquireUninterruptibly();
		traceGroupMap.put(group.getName(), group);
		lock.release();
	}
	
	/**
	 * Add a new BinaryDataFileCollection
	 * 
	 * @param group
	 */
	public void registerBDFCollection(BinaryDataFileCollection collection)
	{
		lock.acquireUninterruptibly();
		
		System.out.println("Registered BDFCollection " + collection.getName());
		
		binaryDataList.add(collection);
		lock.release();
	}
	
	/**
	 * Add a list of trace groups to the manager
	 * 
	 * @param groupList
	 */
	public void registerTraceGroups(List<TraceGroup> groupList)
	{
		for(TraceGroup group : groupList)
		{
			registerTraceGroup(group);
		}
	}
	
	public void setTraceGroupSettings(String groupName, TraceGroupSetting setting)
	{
		lock.acquireUninterruptibly();
		TraceGroup group = traceGroupMap.get(groupName);
		group.setGroupSettings(setting);
		lock.release();
	}
	
	/**
	 * @param groupName
	 * @return A trace group matching the group name requested or null if none exists.
	 */
	public TraceGroup getTraceGroup(String name)
	{
		lock.acquireUninterruptibly();
		TraceGroup group = traceGroupMap.get(name);
		lock.release();
		
		return group;
	}
	
	public ArrayList<BinaryDataFileCollection> getBDFList()
	{
		return binaryDataList;
	}
	
	/**
	 * An unsorted list of the group names in the manager, or if there are nested groups then the top level group names.
	 * 
	 * @return
	 */
	public Set<String> getTraceGroupNames()
	{
		return traceGroupMap.keySet();
	}
	
	public String getName()
	{
		return managerName;
	}
	
	public boolean containsTraceGroup(String name)
	{
		lock.acquireUninterruptibly();
		boolean status = traceGroupMap.containsKey(name);
		lock.release();
		
		return status;
	}
	
	/**
	 * @return A list of the groups in this manager.
	 */
	private List<TraceGroup> getTraceGroupList()
	{
		if(traceGroupList == null)
		{
			traceGroupList = new ArrayList<TraceGroup>();
		}
		
		if(traceGroupList.size() != traceGroupMap.size())
		{
			traceGroupList = new ArrayList<TraceGroup>();
			
			Set<String> groupList = getTraceGroupNames();
			
			for(String group : groupList)
			{
				traceGroupList.add(traceGroupMap.get(group));
			}
		}
		
		return traceGroupList;
	}
	
	/**
	 * Send a notification to trace group listeners of this manager.
	 */
	public void notifiyTraceListeners()
	{
		for(TraceGroup group : getTraceGroupList())
		{
			group.notifyTraceGroupListeners();
		}
	}
	
	/**
	 * Send a notification to end event listeners of this manager.
	 */
	public void endEventNotifiyTraceListeners()
	{
		for(TraceGroup group : getTraceGroupList())
		{
			group.endEventNotifyTraceGroupListeners();
		}
	}
	
	public void registerCustomItemResultFormat(CustomItemResultInf customItemResult)
	{
		customItemResults.add(customItemResult);
	}
	
	public ArrayList<CustomItemResultInf> getCustomItemResultList()
	{
		return customItemResults;
	}
}
