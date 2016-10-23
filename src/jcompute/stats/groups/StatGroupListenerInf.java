package jcompute.stats.groups;

import java.util.ArrayList;

import jcompute.stats.trace.Trace;

public interface StatGroupListenerInf
{
	void groupStatsUpdated(ArrayList<Trace> sampleList);
}
