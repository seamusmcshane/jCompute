package alife;

import org.newdawn.slick.Graphics;

public class SimulationManager
{

	/** Simulation Agent Manager */
	public SimpleAgentManager simpleAgentManager;
	
	/** Simulation Plant Manager */
	public GenericPlantManager genericPlantManager;
	
	/* Draw slow but accurate circular bodies or faster rectangular ones */
	Boolean true_body_drawing = false;

	/** Toggle for Drawing agent field of views */
	Boolean draw_field_of_views = false;
	
	public SimulationManager(int world_size,int plant_numbers,int agent_numbers)
	{
		genericPlantManager = new GenericPlantManager(world_size,plant_numbers);
		
		simpleAgentManager = new SimpleAgentManager(world_size,agent_numbers);

		simpleAgentManager.setTrueDrawing(true_body_drawing);

		simpleAgentManager.setFieldOfViewDrawing(draw_field_of_views);
	}
		
	public void doUpdate()
	{
		// Do a Simulation Step
		simpleAgentManager.doAi();
		
		// Do Plants
		genericPlantManager.updatePlants();
	}

	public void drawAgentsAndPlants(Graphics g)
	{
		genericPlantManager.drawPlants(g);
		
		simpleAgentManager.drawAI(g);	
	}
	
}
