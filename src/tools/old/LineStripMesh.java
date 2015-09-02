package tools.old;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class LineStripMesh
{
	private static final int POSITION_COMPONENTS = 3;
	private static final int COLOR_COMPONENTS = 4;
	private static final int NUM_COMPONENTS = POSITION_COMPONENTS + COLOR_COMPONENTS;
	
	private float color[];		
	private Mesh mesh;
		
	public LineStripMesh(float[] color)
	{
		this.color = color;
	}
	
	public void setPoints(float[] points)
	{		
		float[] glLines = getGLLines(points); 
		
		int vertexCount = glLines.length/NUM_COMPONENTS;

		short[] indices = new short[vertexCount/POSITION_COMPONENTS];
		
		for(int i=0;i<indices.length;i++)
		{
			indices[i] = (short)i;
		}
		
		mesh = new Mesh(false, vertexCount, indices.length, new VertexAttribute(Usage.Position, POSITION_COMPONENTS, "a_position"), new VertexAttribute(Usage.ColorPacked, COLOR_COMPONENTS, "a_color"));

		mesh.setVertices(glLines);	
		mesh.setIndices(indices);
	}
	
	private float[] getGLLines(float[] points)
	{
		int lines = points.length/3;
		
		int glLineSize = lines*NUM_COMPONENTS;
		
		int linesListSize = points.length;
		
		float[] glLines = new float[glLineSize];
		
		System.out.println("lines " + lines);
		System.out.println("points " + points.length);
		
		int line = 0;
		for(int i=0;i<linesListSize;i+=3)
		{
			System.out.println("Line " + line);
			System.out.println("I " + i);
			glLines[NUM_COMPONENTS*line+0] = points[i];
			glLines[NUM_COMPONENTS*line+1] = points[i+1];
			glLines[NUM_COMPONENTS*line+2] = points[i+2];
			
			glLines[NUM_COMPONENTS*line+3] = color[0];
			glLines[NUM_COMPONENTS*line+4] = color[1];
			glLines[NUM_COMPONENTS*line+5] = color[2];
			glLines[NUM_COMPONENTS*line+6] = color[3];
			
			line++;
		}
		
		return glLines;
	}
	
	public Mesh getMesh()
	{
		return mesh;
	}
	
}
