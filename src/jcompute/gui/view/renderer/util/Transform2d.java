package jcompute.gui.view.renderer.util;

import jcompute.math.geom.JCVector2f;

public class Transform2d
{
	// Absolute Cartesian position ( Final to ensure value assignment and not reference bugs)
	public final JCVector2f position = new JCVector2f();
	
	// Linked XY scale
	public float scale;
	
	// 2d orientation
	public float orientation;
}
