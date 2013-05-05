package alifeSim.datastruct.knn;

public class DistanceFunctions
{

	/** Return the Squared Euclidien Distance in two dimensions */
	public static double SquaredEuclidienDistance2D(double from[],double to[])
	{
		return (((from[0]-to[0])*(from[0]-to[0])) + ((from[1]-to[1])*(from[1]-to[1])));		
	}
	
	/** Return the Squared Euclidien Distance in two dimensions */
	public static double SquaredEuclidienDistance1D(double from,double to)
	{
		return (from*from) - (to*to);
	}


}
