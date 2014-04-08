package unitTests.AlifeSim;
import alifeSim.datastruct.knn.KNNInf;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class KDTreeTest
{
	KNNInf<Integer> KDTree;
	
	double pos[];

	double pos1[];
	double pos2[];
	double pos3[];
	double pos4[];
	
	@Before
	public void setUp() throws Exception
	{
		//KDTree = new KDTreeSeq<Integer>(2);
		
		pos = new double[2];
		
		pos[0] = 0;
		pos[1] = 0;
	}
	
	@Test
	public void KDTreeTopRightIsClosest()
	{
		
		
		pos1 = new double[2];
		pos2 = new double[2];
		pos3 = new double[2];
		pos4 = new double[2];
		
		
		pos1[0] = 1;
		pos1[1] = 1;
		KDTree.add(pos1, 1);
		
		pos2[0] = 2;
		pos2[1] = 2;
		KDTree.add(pos2, 2);
		
		pos3[0] = 2;
		pos3[1] = 2;
		KDTree.add(pos3, 3);
		
		pos4[0] = 2;
		pos4[1] = 2;
		KDTree.add(pos4, 4);
		
		int closest = KDTree.nearestNeighbour(pos);
		
		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println("Closest should be 1 and is " + closest);
		assertEquals(true, closest == 1);
		
	}
	
	@Test
	public void KDTreeTopLeftIsClosest()
	{
		pos1 = new double[2];
		pos2 = new double[2];
		pos3 = new double[2];
		pos4 = new double[2];
		
		
		pos1[0] = 2;
		pos1[1] = 2;
		KDTree.add(pos1, 1);
		
		pos2[0] = -1;
		pos2[1] = -1;
		KDTree.add(pos2, 2);
		
		pos3[0] = 2;
		pos3[1] = 2;
		KDTree.add(pos3, 3);
		
		pos4[0] = 2;
		pos4[1] = 2;
		KDTree.add(pos4, 4);
		
		int closest = KDTree.nearestNeighbour(pos);
		
		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println("Closest should be 2 and is " + closest);
		assertEquals(true, closest == 2);		
	}
	
	@Test
	public void KDTreeBottomLeftIsClosest()
	{
		pos1 = new double[2];
		pos2 = new double[2];
		pos3 = new double[2];
		pos4 = new double[2];
		
		
		pos1[0] = 2;
		pos1[1] = 2;
		KDTree.add(pos1, 1);
		
		pos2[0] = 2;
		pos2[1] = 2;
		KDTree.add(pos2, 2);
		
		pos3[0] = -1;
		pos3[1] = -1;
		KDTree.add(pos3, 3);
		
		pos4[0] = 2;
		pos4[1] = 2;
		KDTree.add(pos4, 4);
		
		int closest = KDTree.nearestNeighbour(pos);
		
		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println("Closest should be 3 and is " + closest);
		assertEquals(true, closest == 3);		
	}
	
	@Test
	public void KDTreeBottomRightIsClosest()
	{
		pos1 = new double[2];
		pos2 = new double[2];
		pos3 = new double[2];
		pos4 = new double[2];
		
		
		pos1[0] = 2;
		pos1[1] = 2;
		KDTree.add(pos1, 1);
		
		pos2[0] = 2;
		pos2[1] = 2;
		KDTree.add(pos2, 2);
		
		pos3[0] = 2;
		pos3[1] = 2;
		KDTree.add(pos3, 3);
		
		pos4[0] = 1;
		pos4[1] = -1;
		KDTree.add(pos4, 4);
		
		int closest = KDTree.nearestNeighbour(pos);
		
		System.out.println("----------------------------------------------------");
		System.out.println("Test " + (new Exception()).getStackTrace()[0].getMethodName());
		System.out.println("----------------------------------------------------");
		System.out.println("Closest should be 4 and is " + closest);
		assertEquals(true, closest == 4);		
	}	
	
}
