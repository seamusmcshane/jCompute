package alifeSim.datastruct.knn;

import com.amd.aparapi.Kernel;

public class AparapiSquaresTest extends Kernel
{
	int squares[];
	int values[];
	
	public AparapiSquaresTest(int[] squares, int[] values)
	{
		this.squares = squares;
		this.values = values;
	}

	@Override
	public void run()
	{
		int gid = getGlobalId();
		
		squares[gid] = values[gid] * values[gid];
		
	}
	
}
