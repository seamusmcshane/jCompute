package jcompute.cluster.computenode.weightingbenchmark;

public class VectorList
{
	private static final int ID = 0;
	
	// KNN (Update)
	//private static final int KD_XPOS = 0;
	//private static final int KD_YPOS = 1;
	
	// Agent Variables
	private static final int XPOS = 0;
	private static final int YPOS = 1;
	private static final int DIRECTION = 2;
	private static final int AGE = 3;
	private static final int ENERGY = 4;
	private static final int REPRODUCTION_BANK = 5;
	
	// Total Fields
	private static final int posPACKED_SIZE = 2;
	private static final int statPACKED_SIZE = 6;
	
	private float[][] posData;
	private float[] statData;
	private final int LIST_SIZE;
	
	public VectorList(int size, boolean nullPos)
	{
		this.LIST_SIZE = size;
		
		if(nullPos)
		{
			posData = new float[LIST_SIZE][];
		}
		else
		{
			posData = new float[LIST_SIZE][posPACKED_SIZE];
		}
		
		statData = new float[LIST_SIZE * statPACKED_SIZE];
	}
	
	public Vector get(int index)
	{
		return new Vector(statData[index * statPACKED_SIZE + ID], posData[index], statData[index * statPACKED_SIZE + DIRECTION], statData[index
		* statPACKED_SIZE + AGE], statData[index * statPACKED_SIZE + ENERGY], statData[index * statPACKED_SIZE + REPRODUCTION_BANK]);
	}
	
	public void set(int index, Vector v)
	{
		statData[index * statPACKED_SIZE + ID] = v.id;
		
		posData[index] = v.pos;
		
		statData[index * statPACKED_SIZE + DIRECTION] = v.direction;
		statData[index * statPACKED_SIZE + AGE] = v.age;
		statData[index * statPACKED_SIZE + ENERGY] = v.energy;
		statData[index * statPACKED_SIZE + REPRODUCTION_BANK] = v.reproductionBank;
	}
	
	public String getToString(int index)
	{
		float id = statData[index * statPACKED_SIZE + ID];
		float x_pos = posData[index][XPOS];
		float y_pos = posData[index][YPOS];
		float direction = statData[index * statPACKED_SIZE + DIRECTION];
		float age = statData[index * statPACKED_SIZE + AGE];
		float energy = statData[index * statPACKED_SIZE + ENERGY];
		float reproductionBank = statData[index * statPACKED_SIZE + REPRODUCTION_BANK];
		
		return "Id : " + id + " x" + x_pos + " y" + y_pos + " d" + direction + " a" + age + " e" + energy + " r" + reproductionBank;
	}
	
	public int size()
	{
		return LIST_SIZE;
	}
}
