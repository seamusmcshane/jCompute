package alifeSim.Alife;

import org.khelekore.prtree.MBRConverter;

public class AlifeBodyMBRConverter implements MBRConverter<AlifeBodyInf>
{
	private int dim;
	
	public AlifeBodyMBRConverter(int dim)
	{
		this.dim = dim;
	}

	@Override
	public int getDimensions()
	{
		return dim;
	}
	
	@Override
	public double getMin(int axis, AlifeBodyInf body)
	{
		return body.getBoundingRectangle().getAxisMin(axis);
	}
	
	@Override
	public double getMax(int axis, AlifeBodyInf body)
	{
		return body.getBoundingRectangle().getAxisMax(axis);
	}


	
}

