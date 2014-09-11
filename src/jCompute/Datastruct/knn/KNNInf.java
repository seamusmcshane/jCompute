/**
 * 
 */
package jCompute.Datastruct.knn;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Seamie
 *
 */
public interface KNNInf <Datatype>
{

	
	/* Add Item to Tree */
	void add(double pos[],Datatype data);
	
	/* Find Nearest Item */
	Datatype nearestNeighbour(double pos[]);
		
	/* Find the nth nearest */
	Datatype nearestNNeighbour(double pos[], int n);
	
	/* Find Nearest Items */
	List nearestNeighbours(double pos[]);

	public int size();
	
}
