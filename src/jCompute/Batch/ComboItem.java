package jCompute.Batch;

import jCompute.util.JCMath;

public class ComboItem
{
	private int cid;
	
	private int dims;
	private int pos[];
	private float vals[];
	
	public ComboItem(int cid, int dims)
	{
		this.cid = cid;
		this.dims = dims;
		pos = new int[dims];
		vals = new float[dims];
	}
	
	public void setDimPos(int dim, int val)
	{
		pos[dim] = val;
	}
	
	public int[] getPos()
	{
		return pos;
	}
	
	public String posToString()
	{
		StringBuilder builder = new StringBuilder();
		
		for(int d=0;d<dims;d++)
		{
			builder.append(pos[d]);
			
			if(d<(dims-1))
			{
				builder.append("x");
			}
		}
		
		return builder.toString();
	}
	
	public String valToString()
	{
		StringBuilder builder = new StringBuilder();
		
		for(int d=0;d<dims;d++)
		{
			builder.append(vals[d]);
			
			if(d<(dims-1))
			{
				builder.append("x");
			}
		}
		
		return builder.toString();
	}

	public void setDimVals(int d, double val)
	{
		vals[d] = (float)JCMath.round(val, 7);		
	}
			
}