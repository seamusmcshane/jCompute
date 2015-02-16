package tools.PhasePlot3d;

import tools.OrbitalCameraInputController;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PhasePlotEnviroment implements ApplicationListener
{
	// ENV
	private Environment environment;
	
	private DirectionalLight light1 = new DirectionalLight();
	private DirectionalLight light2 = new DirectionalLight();
	private DirectionalLight light3 = new DirectionalLight();
	private DirectionalLight light4 = new DirectionalLight();
	private DirectionalLight light5 = new DirectionalLight();
	
	private float amb = 0.3f;
	private float brightSide = 0.80f-amb;
	private float darkSide = 0.45f-amb;
	private float topSide = 0.85f-amb;
	
	private float lightScale = 1f;
	
	// Camera
	private PerspectiveCamera cam;

	// Models
	private ModelBatch modelBatch;
	
	private LineStrip3d ls;
	private VertexModel ws;
	/*private BoundaryCube2 bc2;
	
	private BoundaryCube bc;
	
	private LineStrip3d xMs;
	private LineStrip3d yMs;
	private LineStrip3d zMs;
	
	private LineStripMesh meshtest;*/
	
	private OrbitalCameraInputController camController;

	private float width;
	private float height;
	
	public static final String VERT_SHADER = "attribute vec4 a_position;\n" + "attribute vec4 a_color;\n"
			+ "uniform mat4 u_projTrans;\n" + "varying vec4 vColor;\n" + "void main() {\n" + "	vColor = a_color;\n"
			+ "	gl_Position =  u_projTrans * a_position;\n" + "}";

	public static final String FRAG_SHADER = "#ifdef GL_ES\n" + "precision highp float;\n" + "#endif\n"
			+ "varying vec4 vColor;\n" + "void main() {\n" + "	gl_FragColor = vColor;\n" + "}";
	
	private ShaderProgram shader;

	public PhasePlotEnviroment(float width,float height)
	{
		this.width = width;
		this.height = height;
	}
	
	public float[] createCircle()
	{
        // SINE
		int lines = 360;
		
		int numPoints = lines+1;
		
		float radius = 100f;
		
		float multi = 360f/(float)lines;
		
		float[] points = new float[numPoints*3];				
		
		System.out.println("createCircle lines " + lines);
		System.out.println("createCircle points " + numPoints);
		
		int pointNum = 0;
		for(int i=0;i<numPoints;i++)
		{
			//double rx = Math.sin( (i*zMuli*planes) * (Math.PI / 360))*100f;
			
			//radius = (float) rx;
			
			double x = 0+ Math.sin( (i*multi) * (Math.PI / 180)) * radius;
            double y = 0+ Math.cos( (i*multi) * (Math.PI / 180) )* radius;
            double z = 0;//+ Math.sin( (i*zMuli) * (Math.PI / 180)) * 50f;
                        
            points[pointNum] = (float) x;
            points[pointNum+1] = (float) y;
            points[pointNum+2] = (float) z;
            
            pointNum+=3;
		}		
		
		return points;
	}
	
	public float[] createLine()
	{
        // LINE
		int lines = 10;
		
		double inc = 100.0/(double)lines;
	
		float[] points = new float[lines*3];
		
		int pointNum = 0;
		for(int i=0;i<lines;i++)
		{			
			double x = (double)i*inc;
            double y = (double)i*inc;
            double z = (double)i*inc;
                        
            points[pointNum] = (float) x;
            points[pointNum+1] = (float) y;           
            points[pointNum+2] = (float) z;
            
            pointNum+=3;
		}
		
		return points;
	}
	
	public float[] createTorus()
	{
        // Torus
		int div = 32; // 16
		int lines = 360*div;
		
		float radius = 100f;
		
		float multi = 360f/lines;
		
		int POS = 3;
		//int COLOR = 4;
		
		float[] points = new float[lines*POS];
		short[] indicies = new short[lines];

		float zMuli = multi*360f; // 90 
		
		int planes = 63;
		
		int pointNum = 0;
		for(int i=0;i<lines;i++)
		{
			double rx = Math.sin( (i*zMuli*planes) * (Math.PI / 360))*100f;
			
			radius = (float) rx;
			
			double x = 0+ Math.sin( (i*multi) * (Math.PI / 180)) * radius;
            double y = 0+ Math.cos( (i*multi) * (Math.PI / 180) )* radius;
            double z = 0+ Math.sin( (i*zMuli) * (Math.PI / 180)) * 50f;
                        
            points[pointNum] = (float) x;
            points[pointNum+1] = (float) y;           
            points[pointNum+2] = (float) z;
            
            /*points[pointNum+3] = 1f;
            points[pointNum+4] = 0f;
            points[pointNum+5] = 0f;
            points[pointNum+6] = 0.5f;*/
            
            pointNum+=3;
            
            indicies[i]=(short)i;
		}
		
		return points;
	}
	
	@Override
	public void create()
	{
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(VERT_SHADER, FRAG_SHADER);
		String log = shader.getLog();
		if(!shader.isCompiled()) throw new GdxRuntimeException(log);
		if(log != null && log.length() != 0) System.out.println("Shader Log: " + log);
		
		// ENV
		environment = new Environment();

		environment.add(light1);		
		environment.add(light2);		
		environment.add(light3);		
		environment.add(light4);	
		environment.add(light5);	
		
		light1.set(brightSide,brightSide,brightSide,0,lightScale, 0);
		light2.set(brightSide,brightSide,brightSide,0,-lightScale,0);
		light3.set(darkSide,darkSide,darkSide, lightScale,0,0);
		light4.set(darkSide,darkSide,darkSide, -lightScale,0,0);
		light5.set(topSide,topSide,topSide, 0,0,-lightScale);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, amb,amb,amb, 0.1f));
        
        // Models
        modelBatch = new ModelBatch();

		
		/*bc = new BoundaryCube(100f,0,0,0,0.5f);
		
		xMs = new LineStrip3d(0,1,0,1);
		xMs.setPoints(new float[]{0,100,-100,0,-100,-100}, false);
		
		yMs = new LineStrip3d(1,0,0,1);
		yMs.setPoints(new float[]{100,0,-100,-100,0,-100}, false);

		zMs = new LineStrip3d(0,0,1,1);
		zMs.setPoints(new float[]{0,0,100,0,0,-100}, false);*/
		
		ws = new VertexModel(true);
		
		//ws.setVertices(points, indicies, GL20.GL_LINE_STRIP);
		
		/*bc2 = new BoundaryCube2(0,0,0,10,10,10,new float[]{1,0,0,1});
		
		//ls.setPoints(points,true);
		
		meshtest = new LineStripMesh(new float[]{1,0,0,1});
		
		meshtest.setPoints(new float[]{0,0,0,100,0,0});*/
		
		// Cam
		cam = new PerspectiveCamera(75f,(width/height), 1f);
		cam.position.set(0, 0, 500);
		cam.lookAt(0,0,0);
		cam.near = 0.1f;
		cam.far = 10240;
		cam.update();
		
		camController = new OrbitalCameraInputController(cam,-250,125,new float[]{0,0,0},25f);
		
        Gdx.input.setInputProcessor(camController);
        
		//ls = new LineStrip3d(1,0,1,1);
        
		setPlotPoints(createCircle(), new float[]{0,0,0});
	}

	@Override
	public void render()
	{
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
	    Gdx.gl.glDepthFunc(GL20.GL_LESS);
		
	    Gdx.gl.glLineWidth(2f);
	    
		//bc.render(cam);
		
	    Gdx.gl.glEnable(GL20.GL_BLEND);
	    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	    
		//ls.render(cam,GL20.GL_LINE_STRIP);
	    
        modelBatch.begin(cam);

        modelBatch.render(ws.getModelInstace(),environment);
       
        //modelBatch.render(bc2.getModelInstace(),environment);
        
        modelBatch.end();

		/*xMs.render(cam,GL20.GL_LINES);
		yMs.render(cam,GL20.GL_LINES);
		zMs.render(cam,GL20.GL_LINES);		*/
	    
		shader.begin();

		// update the projection matrix so our triangles are rendered in 2D
		shader.setUniformMatrix("u_projTrans", cam.combined);

		// render the mesh
		//meshtest.getMesh().render(shader, GL20.GL_LINE_STRIP, 0, meshtest.getMesh().getNumVertices());

		shader.end();
			    
		camController.update();
	}

	public void setPlotPoints(float[] points,float[] center)
	{
		camController.setTarget(new float[]{center[0],center[1],center[2]});
		//ls.setPoints(points,false);
		
		int COLOR_WIDTH = 4;
		int clen = (points.length/3)*COLOR_WIDTH;
		int pLen = points.length;
				
		float[] glPoints = new float[pLen+clen];
		
		int glLen = glPoints.length;
		
		System.out.println("setPlotPoints pLen " + pLen/3);
		System.out.println("setPlotPoints glPoints " + glLen);
		
		int point = 0;
		for(int i=0;i<pLen;i+=3)
		{
			glPoints[point] = points[i];
			glPoints[point+1] = points[i+1];
			glPoints[point+2] = points[i+2];
            
			glPoints[point+3] = 1f;
			glPoints[point+4] = 0f;
			glPoints[point+5] = 0f;
			glPoints[point+6] = 0.5f;
			
			point+=7;
		}
		
		System.out.println("GLPOINTS " + point);
		
		ws.setVertices(glPoints, GL20.GL_LINE_STRIP);
	}
	
	@Override
	public void dispose()
	{
		//ls.dispose();
		//bc.dispose();
	}

	@Override
	public void resize(int width, int height)
	{
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
		
	}
}