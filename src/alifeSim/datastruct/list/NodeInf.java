package alifeSim.datastruct.list;

public abstract interface NodeInf<Nodetype,Datatype>
{

	public void setNode(Datatype data,double val);
	
	/* Gets Data */
	public Datatype getData();

	public double getVal();
	
}
