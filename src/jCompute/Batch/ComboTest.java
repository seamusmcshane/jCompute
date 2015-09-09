package jCompute.Batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.util.JCMath;

public class ComboTest
{
	private static Logger log;
	
	public static void main(String args[])
	{
		System.setProperty("log4j.configurationFile", "log/config/log4j2-consoleonly.xml");
		
		log = LoggerFactory.getLogger(ComboTest.class);
		
		double[] bases = new double[]
		{
			0, 0
		};
		double[] incr = new double[]
		{
			0.05, 0.05
		};
		int[] steps = new int[]
		{
			20, 20
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
		for(int d=0;d<dimensions;d++)
		{
			int places = JCMath.getNumberOfDecimalPlaces(incr[d]);
			
			if(places > 0)
			{
				// + 1 places to set the range for the unit after the number of decimals places
				errormargin[d] = 1.0 / (Math.pow(10, (places+1)));
			}
			else
			{
				errormargin[d] = 0;
			}
			log.info("e " + d + " " + errormargin[d]);
		}
		
		log.info("Combinations : " + combinations);
		log.info("Dimensions : " + dimensions);
		
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
			log.info("d " + d + " increments every " + incrementMods[d]);
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
		log.info("Pos : ");
		log.info("----------------------");
		for(int c = 0; c < combinations; c++)
		{
			log.info("c" + c + " " + comboItems[c].posToString());
		}
		
		log.info("Combo");
		log.info("----------------------");
		for(int c = 0; c < combinations; c++)
		{
			log.info("c" + c + " " + comboItems[c].valToString());
		}
	}
	
}
