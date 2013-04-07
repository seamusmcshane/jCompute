package alifeSim.datastruct.list;

public interface ListInf<Datatype>
{
	/* Add Item */
	public void add(Datatype data);
	
	/* Returns data and Removes Node */
	public Datatype remove();
	
	/* Removes Node and Data */
	public void delete();
	
	/* Gets current Data - does not increment Node */
	public Datatype get();
	
	/* Gets current Data and then increments Node */
	public Datatype getNext();
	
	/* Returns if there is a next Node */
	public boolean hasNext();	
	
	/* Resets to first Node of data structure */
	public void resetHead();
}
