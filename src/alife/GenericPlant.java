package alife;

import org.newdawn.slick.geom.Vector2f;

public class GenericPlant
{

	public GenericPlantBody body;
	
	public GenericPlant(float x,float y,GenericPlantStats stats)
	{
		body = new GenericPlantBody(new Vector2f(x,y),stats);
	}	
		
}
