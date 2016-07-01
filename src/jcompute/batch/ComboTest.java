package jcompute.batch;

import jcompute.math.JCMath;

public class ComboTest
{
	public static void main(String args[])
	{
		double[] bases = new double[]
		{
			0, 0
		};
		double[] incr = new double[]
		{
			0.02857142857142857142857142857143, 0.02857142857142857142857142857143
		};
		int[] steps = new int[]
		{
			35, 35
		};
		
		doCombos(bases, incr, steps);
	}
	
	public static void doCombos(double bases[], double[] incr, int[] steps)
	{
		// Calculate combos
		int combinations = 1;
		for(int s = 0; s < steps.length; s++)
		{
			combinations *= steps[s];
		}
		
		// Dims
		int dimensions = bases.length;
		
		// Floating point equality ranges
		// Get the number of decimal places
		// Get 10^places
		// divide 1 by 10^places to get
		// n places .1 above the the significant value to test for
		double[] errormargin = new double[dimensions];
		for(int d = 0; d < dimensions; d++)
		{
			int places = JCMath.getNumberOfDecimalPlaces(incr[d]);
			
			if(places > 0)
			{
				// We cannot represent error margins for values with more than
				// 14 decimals
				if(places > 14)
				{
					places = 14;
					
					double prev = incr[d];
					
					incr[d] = JCMath.round(incr[d], places);
					
					System.out.println("incr " + d + " rounded " + prev + " to " + incr[d]);
				}
				
				// + 1 places to set the range for the unit after the number of
				// decimals places
				errormargin[d] = 1.0 / (Math.pow(10, (places + 1)));
			}
			else
			{
				errormargin[d] = 0;
			}
			System.out.println("Error     : " + d + " " + errormargin[d]);
			System.out.println("places    : " + d + " " + places);
			System.out.println("Increment : " + d + " " + incr[d]);
		}
		
		System.out.println("Combinations : " + combinations);
		System.out.println("Dimensions : " + dimensions);
		
		// Combo Arrays
		ComboItem[] comboItems = new ComboItem[combinations];
		for(int c = 0; c < combinations; c++)
		{
			comboItems[c] = new ComboItem(c, dimensions);
		}
		
		// When to increment values in the combos
		int incrementMods[] = new int[dimensions];
		int div = combinations;
		for(int d = 0; d < dimensions; d++)
		{
			div = div / steps[d];
			// Increment depending on the step div
			incrementMods[d] = div;
			System.out.println("d " + d + " increments every " + incrementMods[d]);
		}
		
		double[] max = new double[dimensions];
		// Calculate value roll overs
		for(int d = 0; d < dimensions; d++)
		{
			max[d] = bases[d] + (incr[d] * (steps[d] - 1));
		}
		
		// Init combo initial starting bases
		double val[] = new double[dimensions];
		for(int d = 0; d < dimensions; d++)
		{
			val[d] = bases[d];
		}
		
		// Combo x,y,z... pos
		int pos[] = new int[dimensions];
		
		for(int c = 0; c < combinations; c++)
		{
			for(int d = 0; d < dimensions; d++)
			{
				if((c) % incrementMods[d] == 0 && c > 0)
				{
					pos[d] = (pos[d] + 1) % steps[d];
					
					val[d] = (val[d] + incr[d]);
					
					if(val[d] > (max[d] + errormargin[d]))
					{
						val[d] = bases[d];
					}
				}
				
				comboItems[c].setDimPos(d, pos[d]);
				comboItems[c].setDimVals(d, val[d]);
			}
		}
		
		// Output
		System.out.println("Pos : ");
		System.out.println("----------------------");
		for(int c = 0; c < combinations; c++)
		{
			System.out.println("c" + c + " " + comboItems[c].posToString());
		}
		
		System.out.println("Combo");
		System.out.println("----------------------");
		for(int c = 0; c < combinations; c++)
		{
			System.out.println("c" + c + " " + comboItems[c].valToString());
		}
	}
	
}
