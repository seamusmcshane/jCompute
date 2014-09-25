package jCompute.Stats;

import jCompute.Stats.Groups.ImageStatGroup;
import jCompute.Stats.Groups.StatGroup;
import jCompute.Stats.Trace.ImageStat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

/**
 * Statistics manager - Not Thread Safe.
 *
 */
public class StatManager
{
	private String managerName;
	
	private HashMap<String, StatGroup> groupMap;
	private ArrayList<StatGroup> statGroupList;
	
	private HashMap<String, ImageStatGroup> imageStatMap;
	private ArrayList<ImageStatGroup> statImageGroupList;
	
	private Semaphore statsManagerLock = new Semaphore(1);
	
	public StatManager(String managerName)
	{
		this.managerName = managerName;
		groupMap = new HashMap<String, StatGroup>();
		imageStatMap = new HashMap<String, ImageStatGroup>();
	}
	
	public void registerImageStat(ImageStatGroup stat)
	{
		statsManagerLock.acquireUninterruptibly();
			imageStatMap.put(stat.getName(), stat);
		statsManagerLock.release();
	}
	
	// Add a new stat to the stat manager
	public void registerGroup(StatGroup group)
	{		
		statsManagerLock.acquireUninterruptibly();
			groupMap.put(group.getName(), group);
		statsManagerLock.release();
	}
	
	// Add a list of group to the stat manager
	public void registerGroups(List<StatGroup> groupList)
	{
		for (StatGroup group : groupList) 
		{
			registerGroup(group);
		}			
	}
	
	public void setGroupSettings(String groupName,StatGroupSetting setting)
	{
		statsManagerLock.acquireUninterruptibly();
			StatGroup group = groupMap.get(groupName);
			group.setGroupSettings(setting);
		statsManagerLock.release();
	}
	
	// returns a group based on the group name requested
	public StatGroup getStatGroup(String groupName)
	{
		statsManagerLock.acquireUninterruptibly();
			StatGroup group = groupMap.get(groupName);
		statsManagerLock.release();
		
		return group;
	}
	
	// returns an Image Stat based on the group name requested
	public ImageStatGroup getImageStat(String statName)
	{
		statsManagerLock.acquireUninterruptibly();
			ImageStatGroup imageStat = imageStatMap.get(statName);
		statsManagerLock.release();
		
		return imageStat;
	}
	
	// An unsorted list of the Stat names in the manager
	public Set<String> getGroupList()
	{
		return groupMap.keySet();
	}
	
	// An unsorted list of the Stat names in the manager
	public Set<String> getImageStatList()
	{
		return imageStatMap.keySet();
	}
	
	public String getname()
	{
		return managerName;
	}
		
	public boolean containsGroup(String name)
	{
		statsManagerLock.acquireUninterruptibly();
			boolean status = groupMap.containsKey(name);
		statsManagerLock.release();
		
		return status;
	}
	
	private List<ImageStatGroup> getImageStatGroupList()
	{
		if(statImageGroupList == null)
		{
			statImageGroupList = new ArrayList<ImageStatGroup>();
		}		
		
		if(statImageGroupList.size() != imageStatMap.size())
		{
			statImageGroupList = new ArrayList<ImageStatGroup>();
			
			Set<String> imageGroupList = getImageStatList();
			
			for(String group : imageGroupList)
			{
				statImageGroupList.add(imageStatMap.get(group));
			}			
		}
		
		return statImageGroupList;
	}
	
	private List<StatGroup> getStatGroupList()
	{
		if(statGroupList == null)
		{
			statGroupList = new ArrayList<StatGroup>();
		}		
		
		if(statGroupList.size() != groupMap.size())
		{
			statGroupList = new ArrayList<StatGroup>();
			
			Set<String> groupList = getGroupList();
			
			for(String group : groupList)
			{
				statGroupList.add(groupMap.get(group));
			}			
		}
		
		return statGroupList;
	}
	
	public void notifiyStatListeners()
	{
		for (StatGroup group : getStatGroupList()) 
		{
			group.notifyStatGroupListeners();
		}
	}
	
	public void endEventNotifiyStatListeners()
	{
		for (StatGroup group : getStatGroupList()) 
		{
			group.endEventNotifyStatGroupListeners();
		}
	}

	public void exportImages(String path)
	{

		for (ImageStatGroup group : getImageStatGroupList()) 
		{
			for(String stat : group.getStatList())
			{
				ImageStat imageStat = group.getStat(stat);
				
			    BufferedImage bi = imageStat.getImage();
			    File outputfile = new File(path + File.pathSeparatorChar + imageStat.getName()+".png");
			   
			    try
				{
					ImageIO.write(bi, "png", outputfile);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				
			}
			

		}
		

		
	}

}
