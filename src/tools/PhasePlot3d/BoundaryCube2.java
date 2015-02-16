package tools.PhasePlot3d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class BoundaryCube2
{
	private ModelInstance modelInstance;
	
	public BoundaryCube2(float x, float y, float z, float width, float height, float depth, float[] color)
	{
		ModelBuilder modelBuilder = new ModelBuilder();
		
		modelBuilder.begin();
		MeshPartBuilder meshBuilder;
		
        meshBuilder = modelBuilder.part("part1", GL20.GL_LINES, Usage.Position | Usage.Normal, new Material());
        //meshBuilder.cone(5, 5, 5, 10);
        
        meshBuilder.rect(-1, -1, 1,
        		1, -1, 1,
        		1, 1, 1,
        		-1, 1, 1,
        		0, 0, 0);
        
        meshBuilder.rect(-1, -1, -1,
        		1, -1, -1,
        		1, 1, -1,
        		-1, 1, -1,
        		0, 0, 0);
        
        meshBuilder.line(-1, -1, -1, -1, -1, 1);

        meshBuilder.line(1, -1, -1, 1, -1, 1);

        meshBuilder.line(-1, 1, -1, -1, 1, 1);

        meshBuilder.line(1, 1, -1, 1, 1, 1);
        
        Model model = modelBuilder.end();
        modelInstance = new ModelInstance(model);
        modelInstance.transform.scale(100f, 100f, 100f);		
	}

	public ModelInstance getModelInstace()
	{
		return modelInstance;
	}
}