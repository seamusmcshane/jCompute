package tools.phaseplot3d;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import jcompute.gui.component.gdx.PhasePlot;
import jcompute.gui.view.inputcontroller.OrbitalCameraInputController;

public class PhasePlotGDXContainer implements ApplicationListener
{
	// ENV
	private Environment environment;
	
	private DirectionalLight light1 = new DirectionalLight();
	private DirectionalLight light2 = new DirectionalLight();
	private DirectionalLight light3 = new DirectionalLight();
	private DirectionalLight light4 = new DirectionalLight();
	private DirectionalLight light5 = new DirectionalLight();
	
	private float amb = 0.3f;
	private float brightSide = 0.80f - amb;
	private float darkSide = 0.45f - amb;
	private float topSide = 0.85f - amb;
	
	private float lightScale = 1f;
	
	// Camera
	private PerspectiveCamera cam;
	private ExtendViewport viewport;
	
	// Models
	private ModelBatch modelBatch;
	
	// Decals
	private DecalBatch db;
	
	private OrbitalCameraInputController camController;
	
	private float width;
	private float height;
	
	// Display FullScreen Toggle
	boolean fullscreen = false;
	private int dWidth = 0;
	private int dHeight = 0;
	
	private PhasePlot phPlot;
	
	public PhasePlotGDXContainer(float width, float height)
	{
		this.width = width;
		this.height = height;
		
		phPlot = new PhasePlot();
	}
	
	private float[] superFormulae(int lines, float radius)
	{
		int numPoints = lines + 1;
		
		float multi = (float) ((Math.PI * 2) / lines);
		
		float[] points = new float[numPoints * 3];
		
		System.out.println("superFormulae lines " + lines);
		System.out.println("superFormulae points " + numPoints);
		float a = 1f;
		float b = 1f;
		float m = 6f;
		float n1 = 0.1f;
		float n2 = 2f;
		float n3 = 1f;
		
		float max = 0;
		for(int i = 0; i < numPoints; i++)
		{
			float theta = i * multi;
			float sv = superVVV(theta, a, b, m, n1, n2, n3);
			
			max = Math.max(max, sv * radius);
			// float dv = sv * radius;
		}
		
		float diff = 0;
		if(max > radius)
		{
			diff = radius / max;
		}
		else
		{
			diff = max / radius;
		}
		
		int pointNum = 0;
		for(int i = 0; i < numPoints; i++)
		{
			float theta = i * multi;
			float sv = superVVV(theta, a, b, m, n1, n2, n3);
			
			float dv = sv * radius;
			
			dv = dv * diff;
			// float dv = sv * radius;
			
			double x = 0 + dv * Math.sin(theta);
			double y = 0 + dv * Math.cos(theta);
			double z = 0;
			
			points[pointNum] = (float) x;
			points[pointNum + 1] = (float) y;
			points[pointNum + 2] = (float) z;
			
			pointNum += 3;
		}
		
		return points;
	}
	
	private float superVVV(float theta, float a, float b, float m, float n1, float n2, float n3)
	{
		double mT = m * theta * 0.25;
		
		double cosA = Math.abs(Math.cos(mT / a));
		double sinB = Math.abs(Math.sin(mT / b));
		
		double p1 = Math.pow(cosA, n2);
		double p2 = Math.pow(sinB, n3);
		
		return (float) Math.pow(p1 + p2, -1.0 / n1);
	}
	
	public float[] createCircle(int lines, float radius)
	{
		int numPoints = lines + 1;
		
		float multi = (float) ((Math.PI * 2) / lines);
		
		float[] points = new float[numPoints * 3];
		
		System.out.println("createCircle lines " + lines);
		System.out.println("createCircle points " + numPoints);
		
		int pointNum = 0;
		for(int i = 0; i < numPoints; i++)
		{
			double x = 0 + Math.sin((i * multi)) * radius;
			double y = 0 + Math.cos((i * multi)) * radius;
			double z = 0;
			
			points[pointNum] = (float) x;
			points[pointNum + 1] = (float) y;
			points[pointNum + 2] = (float) z;
			
			pointNum += 3;
		}
		
		return points;
	}
	
	public float[] createSine(float radius)
	{
		int lines = 360 * 2;
		
		int numPoints = lines + 1;
		
		float halfRadius = radius / 2;
		
		float multi = 360f / lines;
		
		float[] points = new float[numPoints * 3];
		
		int pointNum = 0;
		for(int i = 0; i < numPoints; i++)
		{
			double x = 0 + Math.sin((i * multi) * (Math.PI / 180)) * radius;
			double y = 0 + Math.cos((i * multi) * (Math.PI / 180)) * radius;
			
			// SINE
			double z = 0 + Math.sin((i * 20f) * (Math.PI / 180)) * halfRadius / 16;
			
			points[pointNum] = (float) x;
			points[pointNum + 1] = (float) y;
			points[pointNum + 2] = (float) z;
			
			pointNum += 3;
		}
		
		return points;
	}
	
	public float[] createTorus(int div, float zmultimulti, float radius)
	{
		// Torus
		// int div = 32; // 16
		int lines = 360 * div;
		
		float halfRadius = radius / 2;
		
		float multi = 360f / lines;
		
		int POS = 3;
		// int COLOR = 4;
		
		float[] points = new float[lines * POS];
		short[] indicies = new short[lines];
		
		float zMuli = multi * zmultimulti; // 360/ 90
		
		int planes = 63;
		
		int pointNum = 0;
		for(int i = 0; i < lines; i++)
		{
			double rx = Math.sin((i * zMuli * planes) * (Math.PI / 360)) * halfRadius;
			
			radius = (float) rx;
			
			double x = 0 + Math.sin((i * multi) * (Math.PI / 180)) * radius;
			double y = 0 + Math.cos((i * multi) * (Math.PI / 180)) * radius;
			double z = 0 + Math.sin((i * zMuli) * (Math.PI / 180)) * halfRadius / 2;
			
			points[pointNum] = (float) x;
			points[pointNum + 1] = (float) y;
			points[pointNum + 2] = (float) z;
			
			pointNum += 3;
			
			indicies[i] = (short) i;
		}
		
		return points;
	}
	
	@Override
	public void create()
	{
		// ENV
		environment = new Environment();
		
		environment.add(light1);
		environment.add(light2);
		environment.add(light3);
		environment.add(light4);
		environment.add(light5);
		
		light1.set(brightSide, brightSide, brightSide, 0, lightScale, 0);
		light2.set(brightSide, brightSide, brightSide, 0, -lightScale, 0);
		light3.set(darkSide, darkSide, darkSide, lightScale, 0, 0);
		light4.set(darkSide, darkSide, darkSide, -lightScale, 0, 0);
		light5.set(topSide, topSide, topSide, 0, 0, -lightScale);
		
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, amb, amb, amb, 0.1f));
		
		// Cam
		// cam = new PerspectiveCamera(75f, (width / height), 1f);
		cam = new PerspectiveCamera();
		viewport = new ExtendViewport(width, height, cam);
		cam.position.set(0, 0, 500);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 10240;
		cam.update();
		
		camController = new OrbitalCameraInputController(cam, new float[]
		{
			0, 0, 0
		}, 25f);
		
		Gdx.input.setInputProcessor(camController);
		
		// Models
		modelBatch = new ModelBatch();
		
		// Decals
		db = new DecalBatch(new CameraGroupStrategy(cam));
		
		// Initial Model
		float[][] minMax = new float[3][2];
		minMax[0][0] = 0;
		minMax[0][1] = 100;
		minMax[1][0] = 0;
		minMax[1][1] = 100;
		minMax[2][0] = 0;
		minMax[2][1] = 100;
		
		float[][] firstLast = new float[6][2];
		// First (Actual/Scaled)
		firstLast[0][0] = 0;
		firstLast[1][0] = 0;
		firstLast[2][0] = 0;
		firstLast[3][0] = 0;
		firstLast[4][0] = 0;
		firstLast[5][0] = 0;
		
		// Last (Actual/Scaled)
		firstLast[0][1] = 0;
		firstLast[1][1] = 0;
		firstLast[2][1] = 0;
		firstLast[3][1] = 0;
		firstLast[4][1] = 0;
		firstLast[5][1] = 0;
		
		phPlot.glInit();
		
		// Default Model
		phPlot.setPlotPoints(superFormulae(30, 200), new float[]
		{
			0, 0, 0
		}, new String[]
		{
			"X", "Y", "Z"
		}, minMax, firstLast);
		
	}
	
	@Override
	public void render()
	{
		fullscreen = Gdx.graphics.isFullscreen();
		
		// Full Screen Toggle - Uses LWJGL fullscreen methods
		if(Gdx.input.isKeyPressed(Input.Keys.F))
		{
			if(!fullscreen)
			{
				try
				{
					Display.setFullscreen(true);
					viewport.update(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height);
				}
				catch(LWJGLException e)
				{
					e.printStackTrace();
				}
				
			}
			else
			{
				try
				{
					Display.setFullscreen(false);
					viewport.update(dWidth, dHeight);
				}
				catch(LWJGLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		// Background
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		camController.setTarget(phPlot.getCenter());
		
		phPlot.render(modelBatch, environment, cam, db);
		
		// Flush Decals
		db.flush();
		
		camController.update();
	}
	
	public void resetView()
	{
		camController.reset();
	}
	
	@Override
	public void dispose()
	{
		// phPlot.dispose();
	}
	
	@Override
	public void resize(int width, int height)
	{
		this.dWidth = width;
		this.dHeight = height;
		
		viewport.update(dWidth, dHeight);
	}
	
	@Override
	public void pause()
	{
	}
	
	@Override
	public void resume()
	{
		
	}
	
	public void setScalingMode(boolean sameScale)
	{
		phPlot.setScalingMode(sameScale);
	}
	
	public void replot()
	{
		phPlot.populateChart();
	}
	
	/**
	 * @param data
	 * @param names
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public boolean setData(float[][] inData, String[] inNames, int xAxis, int yAxis, int zAxis)
	{
		boolean status = phPlot.setData(inData, inNames, xAxis, yAxis, zAxis);
		
		// Populate now
		phPlot.populateChart();
		
		return status;
	}
	
	/**
	 * @param lineWidth
	 */
	public void setPlotLineWidth(float lineWidth)
	{
		phPlot.setPlotLineWidth(lineWidth);
	}
	
	/**
	 * @param lineWidth
	 */
	public void setGridLineWidth(float lineWidth)
	{
		phPlot.setGridLineWidth(lineWidth);
	}
	
	/**
	 * @param enabled
	 */
	public void enableMinMax(boolean enabled)
	{
		phPlot.enableMinMax(enabled);
	}
	
	/**
	 * @param val
	 */
	public void setMinMaxLineWidth(float lineWidth)
	{
		phPlot.setMinMaxLineWidth(lineWidth);
	}
}