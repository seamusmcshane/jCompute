/**
 * 
 */
package alifeSim.datastruct.knn;

import java.util.LinkedList;

import alifeSim.Alife.SimpleAgent.SimpleAgent;

/**
 * @author Seamie
 *
 */
public interface KNNInf <Datatype>
{
	
	/* Init the Tree */
	void init(int dim);
	
	/* Add Item to Tree */
	void add(int kd[], Datatype data);
	void add(float kd[],Datatype data);
	void add(double kd[],Datatype data);
	
	/* Find Nearest Item */
	Datatype nearestNeighbor(int kd[]);
	Datatype nearestNeighbor(float kd[]);
	Datatype nearestNeighbor(double kd[]);
	
	/* Find Nearest Items */
	LinkedList<Datatype>	nearestNeighbors(int kd[]);
	LinkedList<Datatype>	nearestNeighbors(float kd[]);
	LinkedList<Datatype>	nearestNeighbors(double kd[]);
	
}
