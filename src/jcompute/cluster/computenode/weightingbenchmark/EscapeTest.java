package jcompute.cluster.computenode.weightingbenchmark;

import java.util.Scanner;

import jcompute.math.JCMath;
import jcompute.timing.TimerObj;
import jcompute.util.JVMInfo;

public class EscapeTest
{
	public static JVMInfo jcInfo = JVMInfo.getInstance();
	
	public static Scanner scanner;
	public static final boolean debug = false;
	
	public static void main(String args[])
	{
		int target_mem_mb = 1;
		int object_size = 4 * 8;
		int base_mem_size_mb = object_size * 8192; // 32768
		int objects = (base_mem_size_mb * target_mem_mb) / object_size;
		
		int size = 32;
		printWithMem("" + size);
		
		size = JCMath.nextPow2Up(size);
		printWithMem("" + size);
		
		size = JCMath.nextPow2Up(size);
		printWithMem("" + size);
		
		size = JCMath.nextPow2Up(size);
		printWithMem("" + size);
		
		TimerObj to = new TimerObj();
		printWithMem("Starting " + objects);
		scanner = new Scanner(System.in);
		scanner.nextLine();
		to.startTimer();
		
		if(!debug)
		{
			scanner = null;
		}
		
		VectorList vectors = new VectorList(objects, false);
		
		for(int i = 0; i < 10000; i++)
		{
			vectors = doTEST2(vectors);
		}
		to.stopTimer();
		
		printWithMem("Finished : " + to.getTimeTaken());
		
		scanner = new Scanner(System.in);
		scanner.nextLine();
	}
	
	public static VectorList doTEST2(VectorList v1)
	{
		final int objects = v1.size();
		
		if(debug)
		{
			printWithMem("FloatList Allocation");
			waitEnter();
		}
		
		FloatList floatList = new FloatList(objects);
		
		if(debug)
		{
			printWithMem("V2 >  FloatList");
			waitEnter();
		}
		
		for(int i = 0; i < objects; i++)
		{
			floatList.set(i, v1.get(i).pos);
		}
		
		if(debug)
		{
			printWithMem("V1 chooseDirection");
		}
		
		for(int i = 0; i < objects; i++)
		{
			v1.set(i, v1.get(i).chooseDirection(floatList.getXY(i)[0]));
		}
		
		if(debug)
		{
			printWithMem("V1 moveInCurrentDirection (floatListX)");
			waitEnter();
		}
		
		for(int i = 0; i < objects; i++)
		{
			v1.set(i, v1.get(i).moveInCurrentDirection(1f));
			// v1.set(i, v1.get(i).ageIncrement());
		}
		
		for(int i = 0; i < objects; i++)
		{
			v1.set(i, v1.get(i).ageIncrement());
		}
		
		if(debug)
		{
			printWithMem("V2 Allocation");
			waitEnter();
		}
		
		VectorList v2 = new VectorList(objects, true);
		
		if(debug)
		{
			printWithMem("V1 > V2");
		}
		for(int i = 0; i < objects; i++)
		{
			Vector v = v1.get(i);
			v2.set(i, v);
		}
		
		if(debug)
		{
			printWithMem("V1 Print");
			for(int i = 0; i < objects; i++)
			{
				if(i % 1000000 == 0)
				{
					printWithMem(i + " " + floatList.getXY(i)[0] + " " + floatList.getXY(i)[1] + " " + v2.getToString(i));
				}
			}
		}
		
		if(debug)
		{
			printWithMem("Done");
			waitEnter();
		}
		
		return v2;
	}
	
	// public static void doTEST(int objects)
	// {
	// printWithMem("objects " + objects);
	//
	// printWithMem("V2 Allocation");
	// waitEnter();
	//
	// VectorList vectors1 = new VectorList(objects);
	//
	// printWithMem("V1 SET");
	// waitEnter();
	//
	// float direction = -90;
	//
	// for(int i = 0; i < objects; i++)
	// {
	// Vector t = new Vector(i, 0, 0, 0, direction, 1, 50, 0);
	// vectors1.set(i, t);
	// // printWithMem(vectors.get(i));
	// }
	//
	// printWithMem("V1 moveInCurrentDirection");
	// waitEnter();
	//
	// for(int i = 0; i < objects; i++)
	// {
	// vectors1.set(i, vectors1.get(i).moveInCurrentDirection(-10));
	// }
	//
	// printWithMem("V1 ageIncrement");
	//
	// for(int i = 0; i < objects; i++)
	// {
	// vectors1.set(i, vectors1.get(i).ageIncrement());
	// }
	//
	// printWithMem("V2 Allocation");
	// waitEnter();
	//
	// VectorList vectors2 = new VectorList(objects);
	//
	// printWithMem("V1 > V2");
	//
	// for(int i = 0; i < objects; i++)
	// {
	// Vector v = vectors1.get(i);
	// vectors2.set(i, v);
	// }
	//
	// printWithMem("V1 Nulling");
	// waitEnter();
	// vectors1 = null;
	//
	// printWithMem("FloatList Allocation");
	// waitEnter();
	// vectors1 = null;
	//
	// FloatList floatList = new FloatList(objects);
	//
	// printWithMem("V2 > FloatList");
	// waitEnter();
	//
	// for(int i = 0; i < objects; i++)
	// {
	// floatList.set(i, vectors2.get(i));
	// }
	//
	// printWithMem("V2 Print");
	// for(int i = 0; i < objects; i++)
	// {
	// if(i % 1000000 == 0)
	// {
	// printWithMem(i + " " + floatList.getX(i) + " " + floatList.getY(i) + " " + vectors2.getToString(i));
	// }
	// }
	//
	// printWithMem("Done");
	// waitEnter();
	// }
	
	public static void waitEnter()
	{
		if(scanner != null)
		{
			scanner.nextLine();
		}
	}
	
	public static void printWithMem(String message)
	{
		System.out.println(message + " " + jcInfo.getJVMInfoString());
	}
}
