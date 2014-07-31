package jCompute.Scenario.SAPP;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.Graphics.A2DRectangle;
import jCompute.Gui.View.Graphics.A2DVector2f;

public interface BodyInf
{
	public A2DVector2f getBodyPos();
	public double[] getBodyPosKD();
	public float getSize();

	public void draw(GUISimulationView simView);
	
	public A2DRectangle getBoundingRectangle();

}
