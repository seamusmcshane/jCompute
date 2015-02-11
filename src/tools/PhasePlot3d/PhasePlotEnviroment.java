package tools.PhasePlot3d;

import tools.OrbitalCameraInputController;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;

public class PhasePlotEnviroment implements ApplicationListener
{
	private PerspectiveCamera cam;
	private LineStrip3d ls;	
	private BoundaryCube bc;
	
	private LineStrip3d xMs;
	private LineStrip3d yMs;
	private LineStrip3d zMs;
	
	private OrbitalCameraInputController camController;

	private float width;
	private float height;
	
	public PhasePlotEnviroment(float width,float height)
	{
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void create()
	{
		int div = 32; // 16
		int lines = 360*div;
		
		ls = new LineStrip3d(1,1,1);
		
		bc = new BoundaryCube(100f,0,0,0,0.5f);
		
		xMs = new LineStrip3d(0,1,0,1);
		xMs.setPoints(new float[]{0,100,-100,0,-100,-100}, false);
		
		yMs = new LineStrip3d(1,0,0,1);
		yMs.setPoints(new float[]{100,0,-100,-100,0,-100}, false);

		zMs = new LineStrip3d(0,0,1,1);
		zMs.setPoints(new float[]{0,0,100,0,0,-100}, false);
		
		float radius = 100f;
		
		float multi = 360f/lines;
		
		float[] points = new float[lines*3];
		
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
            
            pointNum+=3;            
		}
		
		ls.setPoints(points,true);
		
		cam = new PerspectiveCamera(75f,(width/height), 1f);
		cam.position.set(0, 0, 500);
		cam.lookAt(0,0,0);
		cam.near = 0.1f;
		cam.far = 10240;
		cam.update();
		
		camController = new OrbitalCameraInputController(cam,-250,125,new float[]{0,0,0},25f);
		
        Gdx.input.setInputProcessor(camController);
	}

	@Override
	public void render()
	{
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
	    
	    Gdx.gl.glLineWidth(1f);
	    
		bc.render(cam);

		xMs.render(cam,GL20.GL_LINES);
		yMs.render(cam,GL20.GL_LINES);
		zMs.render(cam,GL20.GL_LINES);
		
	    Gdx.gl.glEnable(GL20.GL_BLEND);
	    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	    
		ls.render(cam,GL20.GL_LINE_STRIP);

		camController.update();
	}

	public void setPlotPoints(float[] points,float[] center)
	{
		camController.setTarget(new float[]{center[0],center[1],center[2]});
		ls.setPoints(points,true);
	}
	
	@Override
	public void dispose()
	{
		ls.dispose();
		bc.dispose();
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