package jCompute.Datastruct.knn;

public class KNNResult
{
	private KNNPosInf pos;
	private float dis;

	public KNNResult(KNNPosInf pos, float dis)
	{
		this.pos = pos;
		this.dis = dis;
	}

	public KNNPosInf getPos()
	{
		return pos;
	}

	public float getDis()
	{
		return dis;
	}

	public void setPos(KNNPosInf pos)
	{
		this.pos = pos;
	}

	public void setDis(float dis)
	{
		this.dis = dis;
	}
	
}
