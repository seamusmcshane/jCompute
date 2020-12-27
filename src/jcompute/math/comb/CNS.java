package jcompute.math.comb;

public class CNS
{
	/**
	 * Generates the multiplication values for a specifc combo index using combination splits.
	 * These numbers can then be used to multiply the numeric value needed.
	 * eg
	 */
	public static int[] OLD(int index, int[] splits, boolean[] base1)
	{
		int numberOfValues = splits.length;
		
		int numberOfCombos = 1;
		
		for(int s = 0; s < splits.length; s++)
		{
			numberOfCombos *= splits[s];
		}
		
		// System.out.println("NumberOfCombos : " + numberOfCombos);
		// System.out.println("NumberOfValues : " + numberOfValues);
		
		int[] indexValues = new int[numberOfValues];
		
		for(int s = 0; s < numberOfValues; s++)
		{
			indexValues[s] = (index % splits[s]);
		}
		
		for(int bs = 0; bs < base1.length; bs++)
		{
			if(base1[bs])
			{
				indexValues[bs] += 1;
			}
		}
		
		return indexValues;
	}
	
	public static int[] GenerateComboMultipliersForComboIndex(int index, int[] splits, boolean[] base1)
	{
		int numberOfValues = splits.length;
		
		int[] indexValues = new int[numberOfValues];
		
		// System.out.println("index " + index);
		for(int s = 0; s < numberOfValues; s++)
		{
			int tmod = 1;
			for(int sm = 0; sm < s; sm++)
			{
				tmod *= splits[sm];
			}
			
			int div = 0;
			
			if(index > 0)
			{
				div = index / tmod;
				
				indexValues[s] = div % splits[s];
			}
			
			if(base1[s])
			{
				indexValues[s] += 1;
			}
			
			// System.out.print("mod " + mod + " div " + div + "/");
		}
		
		// System.out.print("\n");
		
		return indexValues;
	}
	
	public static void main(String args[])
	{
		int[] splits =
		{
			10, 10, 2
		};
		
		boolean[] base1 =
		{
			false, false, false
		};
		
		System.out.println("TEST");
		
		// System.out.println("10 10 ");
		
		int numberOfCombos = 1;
		for(int s = 0; s < splits.length; s++)
		{
			numberOfCombos *= splits[s];
		}
		
		int[] baseVals =
		{
			10, 10, 0
		};
		
		int[] increments =
		{
			10, 10, 1
		};
		
		for(int i = 0; i < numberOfCombos; i++)
		{
			// System.out.println("i : " + i);
			// printValue(i, splits, base1);
			
			int[] comboMuls = GenerateComboMultipliersForComboIndex(i, splits, base1);
			
			int[] values = GenerateValues(comboMuls, baseVals, increments);
			
			// printArrayValues(comboMuls);
			printArrayValues(values);
			
		}
		
	}
	
	public static int[] GenerateValues(int[] comboMuls, int[] baseVals, int[] increments)
	{
		int numVals = comboMuls.length;
		
		int[] values = new int[numVals];
		
		for(int i = 0; i < numVals; i++)
		{
			values[i] = (comboMuls[i] * increments[i]) + baseVals[i];
		}
		
		return values;
	}
	
	public static void printArrayValues(int[] vals)
	{
		for(int v = 0; v < vals.length; v++)
		{
			System.out.print(vals[v] + " ");
		}
		System.out.print("\n");
	}
	
	public static void printValue(int index, int[] splits, boolean[] base1)
	{
		int[] vals = GenerateComboMultipliersForComboIndex(index, splits, base1);
		
		for(int v = 0; v < vals.length; v++)
		{
			System.out.print(vals[v] + " ");
		}
		System.out.print("\n");
	}
}
