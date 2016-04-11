package jCompute.gui.view.lib3d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class LineGrid
{
	private ModelInstance modelInstance;
	private final Vector3 center;

	public LineGrid(int div, float size, float[] color, float trans)
	{
		float linewidth = 0.1f;

		ModelBuilder modelBuilder = new ModelBuilder();

		modelBuilder.begin();

		addGrid(modelBuilder, div, size, color, 0, 0, -size / 2);

		// modelBuilder.createBox(size/2, size/2, 1f, new Material(new
		// BlendingAttribute()), Usage.Position | Usage.Normal);

		MeshPartBuilder meshBuilder;

		meshBuilder = modelBuilder.part("background", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(
				new BlendingAttribute(), ColorAttribute.createDiffuse(0.5f, 0.5f, 0.5f, 0.05f)));

		meshBuilder.box(size - linewidth, size - linewidth, linewidth);

		modelInstance = new ModelInstance(modelBuilder.end());

		center = new Vector3(-size / 2, -size / 2, 0.5f);

		modelInstance.transform.translate(0, 0, trans);

		center.add(0, 0, -size / 2);
	}

	public Vector3 getLocation()
	{
		return center;
	}

	private void addGrid(ModelBuilder modelBuilder, int div, float size, float[] rgba, float x, float y, float z)
	{
		float start = -(size / 2);
		float end = (size / 2);

		int gridSteps = div + 1;
		float xInterval = size / (gridSteps - 1);

		float[] vertices = new float[(gridSteps * 3) * 4]; // *4 due to two sets
															// of lines (2
															// vertices per
															// line).

		int vLen = vertices.length;

		int line = 0;
		// X Step Interval Lines
		for(int v = 0; v < (vLen / 2); v += 6)
		{
			vertices[v] = start + (line * xInterval);		// X1
			vertices[v + 1] = start;						// Y1
			vertices[v + 2] = 0;							// Z1
			vertices[v + 3] = start + (line * xInterval);	// X2
			vertices[v + 4] = end;							// Y2
			vertices[v + 5] = 0;							// Z2

			line++;
		}
		// Fix the offset for the next axis
		line -= 1;

		// Y Step Interval Lines
		for(int v = (vLen / 2); v < vLen; v += 6)
		{
			vertices[v] = start;								// X1
			vertices[v + 1] = start + (line * xInterval) - size;// Y1
			vertices[v + 2] = 0;								// Z1
			vertices[v + 3] = end;								// X2
			vertices[v + 4] = start + (line * xInterval) - size;// Y2
			vertices[v + 5] = 0;								// Z2

			line++;
		}

		float[] cVerts = MeshHelper.colorAllVertices(vertices, rgba);

		// Mesh mesh = MeshHelper.verticesToMesh(cVerts);

		Node node = modelBuilder.node();

		// GL_LINES (Unconnected)
		modelBuilder.part("Grid", MeshHelper.verticesToMesh(cVerts), GL20.GL_LINES, new Material(
				new BlendingAttribute()));

		node.translation.set(x, y, z);
	}

	public void rotate(float axisX, float axisY, float axisZ, float degrees)
	{
		modelInstance.transform.rotate(axisX, axisY, axisZ, degrees);
		center.rotate(degrees, axisX, axisY, axisZ);
	}

	public void scale(float x, float y, float z)
	{
		modelInstance.transform.scale(x, y, z);
	}

	public void transform(float x, float y, float z)
	{
		modelInstance.transform.trn(x, y, z);
		center.add(x, y, z);
	}

	public ModelInstance getModelInstance()
	{
		return modelInstance;
	}

}
