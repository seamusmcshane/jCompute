package jcompute.datastruct.knn;

import java.util.ArrayList;

public interface KNNDataStruct
{
	public void addPoint(KNNFloatPosInf point);
	
	public ArrayList<KNNFloatPosInf> findNearestNeighbours(float[] point, float maxDistance);
	
	public void setNearestNeighbour(KNNResult result, float[] point, float maxDistance);
	
	public float[][] getPartitionLines();
}
