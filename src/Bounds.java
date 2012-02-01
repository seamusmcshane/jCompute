import org.khelekore.prtree.MBRConverter;


public class Bounds implements MBRConverter<SimpleAgent>
{

	public Bounds()
	{
		
	}

	@Override
	public double getMaxX(SimpleAgent t)
	{	
		return t.getBodyBounds().getMaxX();
	}

	@Override
	public double getMaxY(SimpleAgent t)
	{
		return t.getBodyBounds().getMaxY();
	}

	@Override
	public double getMinX(SimpleAgent t)
	{
		return t.getBodyBounds().getMinX();
	}

	@Override
	public double getMinY(SimpleAgent t)
	{
		return t.getBodyBounds().getMinY();
	}

}
