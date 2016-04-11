package jcompute.stats.groups;

import java.util.ArrayList;

import jcompute.stats.trace.SingleStat;

public interface StatGroupListenerInf
{
	void groupStatsUpdated(ArrayList<SingleStat> sampleList);
}
