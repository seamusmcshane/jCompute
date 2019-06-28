package jcompute.simulationmanager.event;

/** Manager Events */
public enum SimulationsManagerEventType
{
	AddedSim	("Added Sim"),
	RemovedSim	("Removed Sim");

    private final String name;

    private SimulationsManagerEventType(String name) 
    {
        this.name = name;
    }

    @Override
	public String toString()
    {
       return name;
    }
}
