package jcompute.cluster.computenode.weightingbenchmark;

public class Vector
{
	public final float id;
	
	// public final float x_pos;
	// public final float y_pos;
	
	public final float[] pos;
	
	// Agent age in Simulation Steps
	public final float age;
	
	// Current Energy of the agent
	public final float energy;
	
	// Reproduction Energy
	public final float reproductionBank;
	
	// Current Direction
	public final float direction;
	
	public Vector(float id, float[] pos, float direction, float age, float energy, float reproductionBank)
	{
		this.id = id;
		// this.x_pos = x_pos;
		// this.y_pos = y_pos;
		this.pos = pos;
		this.direction = direction;
		this.age = age;
		this.energy = energy;
		this.reproductionBank = reproductionBank;
	}
	
	public Vector chooseDirection(float newDirection)
	{
		return new Vector(id, pos, newDirection + 1, age, energy, reproductionBank);
	}
	
	// Direction / (Speed/Distance/Magnitude)
	public Vector moveInCurrentDirection(float speed)
	{
		float x = pos[0] + (float) (speed * (Math.cos(-1 * Math.toRadians(direction))));
		float y = pos[1] + (float) (speed * (Math.sin(-1 * Math.toRadians(direction))));
		
		pos[0] = x;
		pos[1] = y;
		
		return new Vector(id, pos, direction, age, energy, reproductionBank);
	}
	
	public Vector ageIncrement()
	{
		return new Vector(id, pos, direction, age + 1, energy, reproductionBank);
	}
}
