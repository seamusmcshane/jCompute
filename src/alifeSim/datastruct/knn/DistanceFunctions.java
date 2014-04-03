package alifeSim.datastruct.knn;

import org.khelekore.prtree.DistanceCalculator;
import org.khelekore.prtree.PointND;
import alifeSim.Alife.AlifeBodyInf;

public class DistanceFunctions implements DistanceCalculator<AlifeBodyInf>
{

	/** Return the Squared Euclidien Distance in two dimensions */
	public static double SquaredEuclidienDistance2D(double from[],double to[])
	{
		return (((from[0]-to[0])*(from[0]-to[0])) + ((from[1]-to[1])*(from[1]-to[1])));		
	}
	
	/** Return the Squared Euclidien Distance in two dimensions */
	public static double SquaredEuclidienDistance2D(double x1,double y1,double x2,double y2)
	{
		return (((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)));		
	}
	
	/** Return the Squared Euclidien Distance in one dimensions */
	public static double SquaredEuclidienDistance1D(double from,double to)
	{
		return (from*to) - (from*to);
	}

	@Override
	/** Used In Pr-Tree */
	public double distanceTo(AlifeBodyInf body, PointND point)
	{
		return SquaredEuclidienDistance2D(body.getBodyPos().getX(),body.getBodyPos().getY(),point.getOrd(0),point.getOrd(1));
	}
	
}
