package jcompute.math.trig;

import java.util.ArrayList;
import java.util.Collections;

import jcompute.timing.TimerObj;
import jcompute.util.JCText;

public class TrigTest
{
	public static void main(String args[])
	{
		int vals = 3600;
		double stepSize = ((2 * Math.PI) / vals);
		double invStepSize = 2.0 / vals;
		
		System.out.println("stepSize " + stepSize);
		System.out.println("invStepSize " + invStepSize);
		
		int size = 20;
		
		printDouble(Math.sin(0));
		printDouble(Math.sin(Math.PI / 2));
		printDouble(Math.sin(3 * (Math.PI / 2)));
		printDouble(Math.sin(4 * (Math.PI / 2)));
		
		printDouble(JCTrig.sinLutInt(0));
		printDouble(JCTrig.sinLutInt((Math.PI / 2)));
		printDouble(JCTrig.sinLutInt((3 * (Math.PI / 2))));
		printDouble(JCTrig.sinLutInt((4 * (Math.PI / 2))));
		
		double warmup = 0;
		for(int i = 0; i < 200000 + 1; i++)
		{
			warmup += Math.sin(i);
			warmup += JCTrig.sinLutInt(i);
			warmup += Math.cos(i);
			warmup += JCTrig.cosLutInt(i);
			warmup += Math.tan(i);
			warmup += JCTrig.tanLutInt(i);
		}
		
		System.out.println("Warmup " + warmup);
		//
		// System.out.println("Sin x " + JCText.SpacePaddedString(0, size) + " : " + JCText.SpacePaddedString("Math.sin(x)", size) + " " + JCText
		// .SpacePaddedString("JCTrig.sinLut(x)", size) + " " + JCText.SpacePaddedString("JCTrig.sinLutInt(x)", size));
		//
		// for(int i = 0; i <= vals; i++)
		// {
		// double x = (stepSize * i);
		//
		// System.out.println("Sin x " + JCText.SpacePaddedString(x, size) + " : " + JCText.SpacePaddedString(Math.sin(x), size) + " " + JCText
		// .SpacePaddedString(JCTrig.sinLutInt(x), size) + " DIFF " + (Math.sin(x) - JCTrig.sinLutInt(x)));
		// }
		//
		// for(int i = 0; i <= vals; i++)
		// {
		// double x = (stepSize * i);
		//
		// System.out.println("Cos x " + JCText.SpacePaddedString(x, size) + " : " + JCText.SpacePaddedString(Math.cos(x), size) + " " + JCText
		// .SpacePaddedString(JCTrig.cosLutInt(x), size) + " DIFF " + (Math.cos(x) - JCTrig.cosLutInt(x)));
		// }
		//
		// for(int i = 0; i <= vals; i++)
		// {
		// double x = (stepSize * i);
		//
		// System.out.println("Tan x " + JCText.SpacePaddedString(x, size) + " : " + JCText.SpacePaddedString(Math.tan(x), size) + " " + JCText
		// .SpacePaddedString(JCTrig.tanLutInt(x), size) + " DIFF " + (Math.tan(x) - JCTrig.tanLutInt(x)));
		// }
		
		for(int i = 0; i <= vals; i++)
		{
			double x = -1.0 + (invStepSize * i);
			
			System.out.println("ASin x " + JCText.SpacePaddedString(x, size) + " : " + JCText.SpacePaddedString(Math.asin(x), size) + " " + JCText
			.SpacePaddedString(JCTrig.asinLutInt(x), size) + " DIFF " + (Math.asin(x) - JCTrig.asinLutInt(x)));
		}
		
		for(int i = 0; i < vals + 1; i++)
		{
			double x = -1.0 + (invStepSize * i);
			
			System.out.println("ACos x " + JCText.SpacePaddedString(x, size) + " : " + JCText.SpacePaddedString(Math.acos(x), size) + " " + JCText
			.SpacePaddedString(JCTrig.acosLutInt(x), size) + " DIFF " + (Math.acos(x) - JCTrig.acosLutInt(x)));
		}
		
		int iter = 100000000;
		
		System.out.println("Iterations " + iter);
		testSin(iter);
		testCos(iter);
		testTan(iter);
		testASin(iter);
		testACos(iter);
	}
	
	public static void testSin(int iter)
	{
		int range = 360;
		
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		for(int i = 0; i < range; i++)
		{
			orders.add(i);
		}
		
		Collections.shuffle(orders);
		
		TimerObj to = new TimerObj();
		to.startTimer();
		
		double dval = 0;
		for(int i = 0; i < iter + 1; i++)
		{
			dval += Math.sin(orders.get(i % range));
		}
		to.stopTimer();
		
		System.out.println("Math.sin " + to.getTimeTaken() + " " + dval);
		
		dval = 0;
		
		to.startTimer();
		
		for(int i = 0; i < iter + 1; i++)
		{
			dval += JCTrig.sinLutInt(orders.get(i % range));
		}
		to.stopTimer();
		
		System.out.println("JCTrig.sinLutInt " + to.getTimeTaken() + " " + dval);
	}
	
	public static void testCos(int iter)
	{
		int range = 360;
		
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		for(int i = 0; i < range; i++)
		{
			orders.add(i);
		}
		
		Collections.shuffle(orders);
		
		TimerObj to = new TimerObj();
		to.startTimer();
		
		double dval = 0;
		for(int i = 0; i < iter + 1; i++)
		{
			dval += Math.cos(orders.get(i % range));
		}
		to.stopTimer();
		
		System.out.println("Math.cos " + to.getTimeTaken() + " " + dval);
		
		dval = 0;
		
		to.startTimer();
		
		for(int i = 0; i < iter + 1; i++)
		{
			dval += JCTrig.cosLutInt(orders.get(i % range));
		}
		to.stopTimer();
		
		System.out.println("JCTrig.cosLutInt " + to.getTimeTaken() + " " + dval);
	}
	
	public static void testTan(int iter)
	{
		int range = 360;
		
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		for(int i = 0; i < range; i++)
		{
			orders.add(i);
		}
		
		Collections.shuffle(orders);
		
		TimerObj to = new TimerObj();
		to.startTimer();
		
		double dval = 0;
		for(int i = 0; i < iter + 1; i++)
		{
			dval += Math.tan(orders.get(i % range));
		}
		to.stopTimer();
		
		System.out.println("Math.tan " + to.getTimeTaken() + " " + dval);
		
		dval = 0;
		
		to.startTimer();
		
		for(int i = 0; i < iter + 1; i++)
		{
			dval += JCTrig.tanLutInt(orders.get(i % range));
		}
		to.stopTimer();
		
		System.out.println("JCTrig.tanLutInt " + to.getTimeTaken() + " " + dval);
	}
	
	public static void testASin(int iter)
	{
		int range = 1000;
		double scale = 1000000.0;
		
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		for(int i = 0; i < range; i++)
		{
			orders.add(i);
		}
		
		Collections.shuffle(orders);
		
		TimerObj to = new TimerObj();
		to.startTimer();
		
		double dval = 0;
		for(int i = 0; i < iter + 1; i++)
		{
			dval += Math.asin(orders.get(i % range) / scale);
		}
		to.stopTimer();
		
		System.out.println("Math.asin " + to.getTimeTaken() + " " + dval);
		
		dval = 0;
		
		to.startTimer();
		
		for(int i = 0; i < iter + 1; i++)
		{
			dval += JCTrig.asinLutInt(orders.get(i % range) / scale);
		}
		to.stopTimer();
		
		System.out.println("JCTrig.asinLutInt " + to.getTimeTaken() + " " + dval);
	}
	
	public static void testACos(int iter)
	{
		int range = 1000;
		double scale = 1000000.0;
		
		ArrayList<Integer> orders = new ArrayList<Integer>();
		
		for(int i = 0; i < range; i++)
		{
			orders.add(i);
		}
		
		Collections.shuffle(orders);
		
		TimerObj to = new TimerObj();
		to.startTimer();
		
		double dval = 0;
		for(int i = 0; i < iter + 1; i++)
		{
			dval += Math.acos(orders.get(i % range) / scale);
		}
		to.stopTimer();
		
		System.out.println("Math.acos " + to.getTimeTaken() + " " + dval);
		
		dval = 0;
		
		to.startTimer();
		
		for(int i = 0; i < iter + 1; i++)
		{
			dval += JCTrig.acosLutInt(orders.get(i % range) / scale);
		}
		to.stopTimer();
		
		System.out.println("JCTrig.acosLutInt " + to.getTimeTaken() + " " + dval);
	}
	
	public static void printDouble(double d)
	{
		System.out.printf("%f\n", d);
	}
}
