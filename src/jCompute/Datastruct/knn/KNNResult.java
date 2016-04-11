package jCompute.datastruct.knn;

public class KNNResult
{
	private KNNFloatPosInf pos;
	private float dis;

	public KNNResult()
	{
	}

	public KNNResult(KNNFloatPosInf pos, float dis)
	{
		this.pos = pos;
		this.dis = dis;
	}

	public KNNFloatPosInf getPos()
	{
		return pos;
	}

	public float getDis()
	{
		return dis;
	}

	public void setPos(KNNFloatPosInf pos)
	{
		this.pos = pos;
	}

	public void setDis(float dis)
	{
		this.dis = dis;
	}

}
