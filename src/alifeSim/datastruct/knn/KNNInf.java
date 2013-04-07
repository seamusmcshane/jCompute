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
	void add(int x, int y, SimpleAgent agent);
	void add(float x, float y, SimpleAgent agent);
	void add(double x, double y, SimpleAgent agent);	
	
	/* Find Nearest Item */
	SimpleAgent nearestNeighbor(int x,int y);
	SimpleAgent nearestNeighbor(float x,float y);
	SimpleAgent nearestNeighbor(double x,double y);
	
	/* Find Nearest Items */
	LinkedList<SimpleAgent>	nearestNeighbors(int x,int y);
	LinkedList<SimpleAgent>	nearestNeighbors(float x,float y);
	LinkedList<SimpleAgent>	nearestNeighbors(double x,double y);
	
}
