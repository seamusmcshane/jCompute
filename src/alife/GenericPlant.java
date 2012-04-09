package alife;

import org.newdawn.slick.geom.Vector2f;
/**
 * This Class is an instantiation of a plant. 
 */
public class GenericPlant
{
	public GenericPlantBody body;
	
	public GenericPlant(float x,float y,float starting_energy, float max_energy, float absorption_rate, float base_plant_reproduction_cost)
	{
		body = new GenericPlantBody(new Vector2f(x,y),starting_energy, max_energy, absorption_rate, base_plant_reproduction_cost);
	}	
		
}
