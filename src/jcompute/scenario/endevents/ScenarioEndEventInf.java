package jcompute.scenario.endevents;

public interface ScenarioEndEventInf
{
	/**
	 * Name of the end event.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Logic that checks if the event has triggered.
	 * 
	 * @return true if the event has occurred.
	 */
	public boolean checkEvent();
	
	/**
	 * Value of the event trigger.
	 * 
	 * @return The trigger value.
	 */
	public int getValue();
}
