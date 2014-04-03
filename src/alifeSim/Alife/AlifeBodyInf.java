package alifeSim.Alife;

import alifeSim.Gui.View.GUISimulationView;
import alifeSimGeom.A2DCircle;
import alifeSimGeom.A2DRectangle;
import alifeSimGeom.A2DVector2f;
import alifeSimGeom.A2RGBA;

public interface AlifeBodyInf
{
	public A2DVector2f getBodyPos();
	public double[] getBodyPosKD();
	public float getSize();

	public void draw(GUISimulationView simView);
	
	public A2DRectangle getBoundingRectangle();

}
