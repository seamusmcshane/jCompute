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
public interface KNNInf <T>
{
	
	/* Init the Tree */
	void init(int dim);
	
	/* Add Item to Tree */
	void add(int kd[], SimpleAgent agent);
	void add(float kd[], SimpleAgent agent);
	void add(double kd[], SimpleAgent agent);	
	
	/* Find Nearest Item */
	SimpleAgent nearestNeighbor(int kd[]);
	SimpleAgent nearestNeighbor(float kd[]);
	SimpleAgent nearestNeighbor(double kd[]);
	
	/* Find Nearest Items */
	LinkedList<SimpleAgent>	nearestNeighbors(int kd[]);
	LinkedList<SimpleAgent>	nearestNeighbors(float kd[]);
	LinkedList<SimpleAgent>	nearestNeighbors(double kd[]);
	
}
