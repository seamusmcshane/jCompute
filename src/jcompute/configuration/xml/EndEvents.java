package jcompute.configuration.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EndEvents")
public class EndEvents
{
	private List<Event> endEvents;
	
	@XmlElement(name = "Event")
	public void setEvents(List<Event> endEvents)
	{
		this.endEvents = endEvents;
	}
	
	public List<Event> getEvents()
	{
		return endEvents;
	}
}