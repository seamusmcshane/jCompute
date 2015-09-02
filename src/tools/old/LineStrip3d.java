package tools.old;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class LineStrip3d
{
	private static final int POSITION_COMPONENTS = 3;
	private static final int COLOR_COMPONENTS = 4;
	private static final int NUM_COMPONENTS = POSITION_COMPONENTS + COLOR_COMPONENTS;
	
	private float linesList[];
	private float color[];
	
	private int numLines;
	
	private Mesh mesh;
	private ShaderProgram shader;
	
	private boolean needsSet = true;
	private int vertexCount = 0;
	
	private boolean rgbXYZ = false;
	private float xScale;
	private float yScale;
	private float zScale;
	
	public static final String VERT_SHADER = "attribute vec4 a_position;\n" + "attribute vec4 a_color;\n"
			+ "uniform mat4 u_projTrans;\n" + "varying vec4 vColor;\n" + "void main() {\n" + "	vColor = a_color;\n"
			+ "	gl_Position =  u_projTrans * a_position;\n" + "}";

	public static final String FRAG_SHADER = "#ifdef GL_ES\n" + "precision highp float;\n" + "#endif\n"
			+ "varying vec4 vColor;\n" + "void main() {\n" + "	gl_FragColor = vColor;\n" + "}";
		
	public LineStrip3d(float r,float g,float b, float a)
	{		
		color = new float[]{r,g,b,a};
		
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(VERT_SHADER, FRAG_SHADER);
		String log = shader.getLog();
		if(!shader.isCompiled()) throw new GdxRuntimeException(log);
		if(log != null && log.length() != 0) System.out.println("Shader Log: " + log);
	}
	
	public LineStrip3d(float xScale,float yScale,float zScale)
	{		
		rgbXYZ = true;
		
		color = new float[]{1,1,1,1};
		
		this.xScale = xScale;
		this.yScale = yScale;
		this.zScale = zScale;
		
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(VERT_SHADER, FRAG_SHADER);
		String log = shader.getLog();
		if(!shader.isCompiled()) throw new GdxRuntimeException(log);
		if(log != null && log.length() != 0) System.out.println("Shader Log: " + log);
	}
	
	public void setPoints(float[] points, boolean rgbXYZ)
	{
		this.linesList = points;
	
		this.rgbXYZ = rgbXYZ;
		
		this.xScale = 1f;
		this.yScale = 1f;
		this.zScale = 1f;
		
		update();
	}

	private void update()
	{
		needsSet = true;
	}
		
	private float[] getGLLines()
	{
		if(linesList==null)
		{
			return null;
		}
		int lines = linesList.length/3;
		
		System.out.println("getGLLines lines " + lines);
		
		int glLineSize = lines*NUM_COMPONENTS;
		
		int linesListSize = linesList.length;
		
		float[] glLines = new float[glLineSize];
		
		float xMax = Float.NEGATIVE_INFINITY;
		float yMax = Float.NEGATIVE_INFINITY;
		float zMax = Float.NEGATIVE_INFINITY;
		
		float xMin = Float.POSITIVE_INFINITY;
		float yMin = Float.POSITIVE_INFINITY;
		float zMin = Float.POSITIVE_INFINITY;		
		
		if(rgbXYZ)
		{		// Find Max / Min
			for(int ii=0;ii<linesListSize;ii+=3)
			{

				float x = linesList[ii];
				float y = linesList[ii+1];
				float z = linesList[ii+2];
				
				
	            if(x > xMax)
	            {
	            	xMax = (float) x;
	            }
	            
	            if(x < xMin)
	            {
	            	xMin = (float) x;
	            }
				
				
	            if(y > yMax)
	            {
	            	yMax = (float) y;
	            }
	            
	            if(y < yMin)
	            {
	            	yMin = (float) y;
	            }
	            				
	            if(z > zMax)
	            {
	            	zMax = (float) z;
	            }
	            
	            if(z < zMin)
	            {
	            	zMin = (float) z;
	            }
				
			}
		}
		

		
		float cBoost = 0.4f;
		
		float xColorScale = (1f-cBoost)/(xMax-xMin);
		float yColorScale = (1f-cBoost)/(yMax-yMin);
		float zColorScale = (1f-cBoost)/(zMax-zMin);
		
		float scale = Math.min(xColorScale, yColorScale);
		scale = Math.min(scale, zColorScale);
		
		int line = 0;
		for(int i=0;i<linesListSize;i+=3)
		{
			
			if(!rgbXYZ)
			{
				glLines[NUM_COMPONENTS*line+0] = linesList[i];
				glLines[NUM_COMPONENTS*line+1] = linesList[i+1];
				glLines[NUM_COMPONENTS*line+2] = -10;//linesList[i+2];
				
				glLines[NUM_COMPONENTS*line+3] = color[0];
				glLines[NUM_COMPONENTS*line+4] = color[1];
				glLines[NUM_COMPONENTS*line+5] = color[2];
				glLines[NUM_COMPONENTS*line+6] = color[3];
			}
			else
			{
				glLines[NUM_COMPONENTS*line+0] = linesList[i];
				glLines[NUM_COMPONENTS*line+1] = linesList[i+1];
				glLines[NUM_COMPONENTS*line+2] = linesList[i+2];
				
				glLines[NUM_COMPONENTS*line+3] = (color[0]*(scale*linesList[i]))+cBoost;
				glLines[NUM_COMPONENTS*line+4] = (color[1]*(scale*linesList[i+1]))+cBoost;
				glLines[NUM_COMPONENTS*line+5] = (color[2]*(scale*linesList[i+2]))+cBoost;
				glLines[NUM_COMPONENTS*line+6] = color[3];
			}
			
			line++;
		}
		
		return glLines;
	}
	
	public void render(Camera cam, int mode)
	{
		if(needsSet)
		{
			float[] glLines = getGLLines(); 
			
			if(glLines!=null)
			{
				System.out.println("Updating");

				vertexCount = glLines.length/NUM_COMPONENTS;
				
				if(mesh!=null)
				{
					mesh.dispose();
				}
				
				short[] indices = new short[ vertexCount/POSITION_COMPONENTS];
				
				for(int i=0;i<indices.length;i++)
				{
					indices[i] = (short)i;
				}
				
				mesh = new Mesh(true, vertexCount, indices.length, new VertexAttribute(Usage.Position, POSITION_COMPONENTS, "a_position"),
						new VertexAttribute(Usage.ColorPacked, COLOR_COMPONENTS, "a_color"));
				
				mesh.setVertices(glLines);
				
				needsSet = false;
				
				linesList = null;
			}
		}

		shader.begin();

		// update the projection matrix so our triangles are rendered in 2D
		shader.setUniformMatrix("u_projTrans", cam.combined);
		
		// render the mesh
		mesh.render(shader, mode, 0, vertexCount);

		shader.end();
		
	}
	
	public void dispose()
	{
		mesh.dispose();
		shader.dispose();		
	}
	
}
