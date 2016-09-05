package jcompute.gui.view.renderer.vectorgraphic;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.math.geom.JCVector2f;

public abstract class VectorGraphic
{
	// Describs how to draw the graphic
	public abstract void draw(ViewRendererInf renderer, JCVector2f position, float scaleXY, float degrees);

	// Clean up any hardware resources
	public abstract void dispose();
}
