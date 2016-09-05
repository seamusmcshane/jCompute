package jcompute.gui.view.renderer.vectorgraphic;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.math.geom.JCVector2f;
import jcompute.math.trig.JCTrig;

public class SuperVector extends VectorGraphic
{
	private static final int POINTS = 30;
	private static final int VERTICES = POINTS * 2;
	
	private PolygonSprite ps;
	private Texture tex;
	
	public SuperVector(ViewRendererInf renderer, float a, float b, float m1, float n1, float n2, float n3, float red, float green, float blue, float alpha,
	float orientation)
	{
		Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(red, green, blue, alpha);
		pix.fill();
		
		// Generate the vertices
		float[] vertices = createSuperVertices(1f, a, b, m1, n1, n2, n3, orientation);
		
		// Generate the correct indices
		EarClippingTriangulator et = new EarClippingTriangulator();
		short[] indices = et.computeTriangles(vertices).toArray();
		
		// Record texture ref for clean up
		tex = new Texture(pix);
		
		TextureRegion texR = new TextureRegion(tex);
		
		// Define a textured polygon
		PolygonRegion polyReg = new PolygonRegion(texR, vertices, indices);
		
		// Create the sprite
		ps = new PolygonSprite(polyReg);
		ps.setOrigin(0, 0);
	}
	
	private float[] createSuperVertices(float radius, float a, float b, float m, float n1, float n2, float n3, float thetaOffset)
	{
		// X+Y
		float[] vertices = new float[VERTICES];
		
		// Convert thetaOffset to radians
		float offset = JCTrig.DEG_TO_RAD * thetaOffset;
		
		// Multiplier for each point
		float multi = (float) ((Math.PI * 2) / POINTS);
		
		// Rescaler max
		float maxRadius = 0;
		
		// Detect max radius
		for(int i = 0; i < POINTS; i++)
		{
			float theta = i * multi;
			float sv = superVVV(theta, a, b, m, n1, n2, n3);
			
			maxRadius = Math.max(maxRadius, sv * radius);
		}
		
		// Get the rescale value
		float rescale = 0;
		if(maxRadius > radius)
		{
			rescale = radius / maxRadius;
		}
		else
		{
			rescale = maxRadius / radius;
		}
		
		// Generate vertices using super formulae and scale the radius
		int v = 0;
		for(int i = 0; i < POINTS; i++)
		{
			float theta = i * multi;
			
			float sv = superVVV(theta, a, b, m, n1, n2, n3);
			
			float dv = sv * radius * rescale;
			
			// Convert to polar coordinates with theta offset.
			vertices[v] = (float) (Math.sin(theta + offset) * dv);
			vertices[v + 1] = (float) (Math.cos(theta + offset) * dv);
			
			v += 2;
		}
		
		return vertices;
	}
	
	/**
	 * Note this formulae is patented 2005 GIELIS JOHAN
	 * "METHOD AND APPARATUS FOR SYNTHESIZING PATTERNS" - EP1177529
	 * 
	 * @param theta
	 * @param a
	 * @param b
	 * @param m
	 * @param n1
	 * @param n2
	 * @param n3
	 * @return
	 */
	private float superVVV(float theta, float a, float b, float m, float n1, float n2, float n3)
	{
		double mT = m * theta * 0.25;
		
		double cosA = Math.abs(Math.cos(mT / a));
		double sinB = Math.abs(Math.sin(mT / b));
		
		double p1 = Math.pow(cosA, n2);
		double p2 = Math.pow(sinB, n3);
		
		return (float) Math.pow(p1 + p2, -1.0 / n1);
	}
	
	@Override
	public void draw(ViewRendererInf renderer, JCVector2f position, float scaleXY, float degrees)
	{
		ps.setPosition(position.x, position.y);
		ps.setScale(scaleXY);
		ps.setRotation(degrees);
		ps.draw(renderer.getPolygonSpriteBatch());
	}
	
	@Override
	public void dispose()
	{
		tex.dispose();
	}
}
