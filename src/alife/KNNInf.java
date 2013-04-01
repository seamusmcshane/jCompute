/**
 * 
 */
package alife;

import java.util.LinkedList;

/**
 * @author Seamie
 *
 */
public interface KNNInf <T>{
	
	void init(int dim);
	
	void add(int x, int y, SimpleAgent agent);
	void add(float x, float y, SimpleAgent agent);
	void add(double x, double y, SimpleAgent agent);	
	
	SimpleAgent nearestNeighbor(int x,int y);
	SimpleAgent nearestNeighbor(float x,float y);
	SimpleAgent nearestNeighbor(double x,double y);
	
	LinkedList<SimpleAgent>	nearestNeighbors(int x,int y);
	LinkedList<SimpleAgent>	nearestNeighbors(float x,float y);
	LinkedList<SimpleAgent>	nearestNeighbors(double x,double y);
	
}
