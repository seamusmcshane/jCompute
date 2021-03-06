package jcompute.gui.view.lib3d;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class VertexModel
{
	private ModelBuilder modelBuilder;
	private ModelInstance modelInstance;
	private boolean dynamic;

	public VertexModel(boolean dynamic)
	{
		this.dynamic = dynamic;

		modelBuilder = new ModelBuilder();
	}

	public void setVertices(float[] vertices, int primitiveType)
	{
		if(modelInstance != null)
		{
			modelInstance.model.dispose();

			modelInstance = null;
		}

		// Indices are shorts and our mesh might exceed Short.MAX_VALUE we may
		// need to split the mesh into sub meshes
		int neededIndicies = (vertices.length / 7);

		int splitIndicie = 4000;

		// Max indicie is the smaller of the following (if vertices.len <
		// maxIndex then we do not need to split the list)
		int maxIndicie = Math.min(splitIndicie, neededIndicies);

		int meshes = neededIndicies / maxIndicie;
		int maxVerticie = maxIndicie * 7;

		int base = 1;
		int splitMod = (vertices.length / base) % 7;
		int baseSplit = vertices.length;

		if(meshes > 1)
		{
			// Start by spliting in half
			base = 2;
			splitMod = (vertices.length / base) % 7;
			baseSplit = vertices.length / base;

			while((splitMod != 0) || (baseSplit > maxVerticie))
			{
				base++;

				baseSplit = vertices.length / base;
				splitMod = (vertices.length / base) % 7;
			}

		}

		// Begin new Model
		modelBuilder.begin();

		int count = 0;
		for(int i = 0; i < base; i++)
		{
			float[] subVertices = new float[baseSplit];
			System.arraycopy(vertices, i * baseSplit, subVertices, 0, baseSplit);

			modelBuilder.part("mesh_" + count, MeshHelper.verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));
			count++;
		}

		Model model = modelBuilder.end();

		modelInstance = new ModelInstance(model);

		if(!dynamic)
		{
			modelBuilder = null;
		}
	}

	public ModelInstance getModelInstance()
	{
		return modelInstance;
	}

	public void dispose()
	{
		if(modelInstance != null)
		{
			if(modelInstance.model != null)
			{
				modelInstance.model.dispose();
			}
		}
	}

}
