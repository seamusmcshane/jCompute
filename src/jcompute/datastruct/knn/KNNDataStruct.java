package jcompute.datastruct.knn;

import java.util.ArrayList;

import jcompute.math.geom.JCVector2f;

public interface KNNDataStruct
{
	public void addPoint(KNNFloatPosInf point);
	
	public ArrayList<KNNFloatPosInf> findNearestNeighbours(JCVector2f point, float maxDistance);
	
	public void setNearestNeighbour(KNNResult result, JCVector2f point);
	
	public float[][] getPartitionLines();
}
