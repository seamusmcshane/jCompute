package jCompute.Stats.Groups;

import jCompute.Stats.Trace.SingleStat;

import java.util.ArrayList;

public interface StatGroupListenerInf
{
	void groupStatsUpdated(ArrayList<SingleStat> sampleList);
}
