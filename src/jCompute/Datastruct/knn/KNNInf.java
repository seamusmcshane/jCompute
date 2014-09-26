/**
 * 
 */
package jCompute.Datastruct.knn;

import java.util.List;

/**
 * @author Seamie
 *
 */
public interface KNNInf <Datatype>
{

	
	/* Add Item to Tree */
	void add(float pos[],Datatype data);
	
	/* Find Nearest Item */
	Datatype nearestNeighbour(float pos[]);
		
	/* Find the nth nearest */
	Datatype nearestNNeighbour(float pos[], int n);
	
	/* Find Nearest Items */
	List nearestNeighbours(float pos[]);

	public int size();
	
}
