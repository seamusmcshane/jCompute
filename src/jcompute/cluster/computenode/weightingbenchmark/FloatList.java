package jcompute.cluster.computenode.weightingbenchmark;

public class FloatList
{
	// private static final int X = 0;
	// private static final int Y = 1;
	
	// private static final int PACKED_SIZE = 2;
	
	private float[][] data;
	private final int LIST_SIZE;
	
	public FloatList(int size)
	{
		this.LIST_SIZE = size;
		
		this.data = new float[LIST_SIZE][];
	}
	
	public void set(int index, float[] xy)
	{
		data[index] = xy;
		// data[index * PACKED_SIZE + Y] = v.y_pos;
	}
	
	public float[] getXY(int index)
	{
		//return data[index * PACKED_SIZE + X];
		return data[index];
	}
	
//	public float getY(int index)
//	{
//		return data[index * PACKED_SIZE + Y];
//	}
}
