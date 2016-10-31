package jcompute.results.trace.group;

import java.util.ArrayList;

import jcompute.results.trace.Trace;

public interface TraceGroupListenerInf
{
	void groupStatsUpdated(ArrayList<Trace> sampleList);
}
