package jcompute.scenario.endevents;

/**
 * Interface for defining events which the simulation can react to during processing and stop.
 * This apparatus is for logical execution events not error handling.
 * 
 * @author Seamus McShane
 */
public interface ScenarioEndEventInf
{
	/**
	 * Getter for the event name.
	 * 
	 * @return Name of the end event.
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
