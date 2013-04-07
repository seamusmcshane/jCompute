package alifeSim.datastruct.list;

public abstract interface NodeInf<Nodetype,Datatype>
{
	/* Sets data */	
	public void setData(Datatype data);
	
	/* Gets Data */
	public Datatype getData();

	/* Gets next node pointer */
	public Nodetype getNext();
	
	/* Gets prev node pointer */
	public Nodetype getPrev();
	
	/* Sets next node pointer */
	public void setNext(Nodetype node);
	
	/* Sets prev node pointer */
	public void setPrev(Nodetype node);	
	
}
