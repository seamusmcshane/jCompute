package alife;

public class SimpleAgentTraits
{
	/* Food preference */
	int food_preference; // 0 = Herbivore || 50 Omnivore || 100 Carnivore

	/* Likely hood of agent to attack with out cause/reason/thinking */
	int aggressiveness; // 0 - Sociable || 100 Skittish
	
	/* Likely Hood of Cannibalism */
	int cannibalistic; // 0 - never || 50 only under stress || 100 - inherent  
	
	public SimpleAgentTraits(int fp, int aggr, int canni)
	{
		this.food_preference = fp;
		
		this.aggressiveness = aggr;
		
		this.cannibalistic = canni;
	}
	
	
}
