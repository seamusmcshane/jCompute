package tools.PhasePlot3d;

import tools.MeshHelper;
import tools.OrbitalCameraInputController;
import tools.Common.AxisGrid;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PhasePlotEnviroment implements ApplicationListener
{
	private float scale = 1000f;
	private float scaleHalf = scale / 2;

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
	private FitViewport viewport;
	
	// Models
	private ModelBatch modelBatch;

	// Decals
	private DecalBatch db;

	private AxisGrid axisGrid;

	private VertexModel ws;
	private BoundaryCube2 bc;

	private VertexModel xMaxVM;
	private VertexModel xMinVM;

	private VertexModel yMaxVM;
	private VertexModel yMinVM;

	private VertexModel zMaxVM;
	private VertexModel zMinVM;

	private OrbitalCameraInputController camController;

	private float width;
	private float height;

	private float plotLineWidth = 2f;
	private float gridLineWidth = 1f;
	private float minMaxLineWidth = 3f;
	
	public PhasePlotEnviroment(float width, float height)
	{
		this.width = width;
		this.height = height;
	}

	public float getScale()
	{
		return scaleHalf;
	}

	public float[] createCircle(int lines)
	{
		int numPoints = lines + 1;

		float radius = scaleHalf;

		float multi = 360f / (float) lines;

		float[] points = new float[numPoints * 3];

		System.out.println("createCircle lines " + lines);
		System.out.println("createCircle points " + numPoints);

		int pointNum = 0;
		for(int i = 0; i < numPoints; i++)
		{
			double x = 0 + Math.sin((i * multi) * (Math.PI / 180)) * radius;
			double y = 0 + Math.cos((i * multi) * (Math.PI / 180)) * radius;
			double z = 0;

			points[pointNum] = (float) x;
			points[pointNum + 1] = (float) y;
			points[pointNum + 2] = (float) z;

			pointNum += 3;
		}

		return points;
	}

	public float[] createSine()
	{
		int lines = 360 * 2;

		int numPoints = lines + 1;

		float radius = scaleHalf;

		float multi = 360f / (float) lines;

		float[] points = new float[numPoints * 3];

		int pointNum = 0;
		for(int i = 0; i < numPoints; i++)
		{
			double x = 0 + Math.sin((i * multi) * (Math.PI / 180)) * radius;
			double y = 0 + Math.cos((i * multi) * (Math.PI / 180)) * radius;

			// SINE
			double z = 0 + Math.sin((i * 20f) * (Math.PI / 180)) * scaleHalf / 16;

			points[pointNum] = (float) x;
			points[pointNum + 1] = (float) y;
			points[pointNum + 2] = (float) z;

			pointNum += 3;
		}

		return points;
	}

	public float[] createTorus(int div, float zmultimulti)
	{
		// Torus
		// int div = 32; // 16
		int lines = 360 * div;

		float radius = scaleHalf;

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
			double rx = Math.sin((i * zMuli * planes) * (Math.PI / 360)) * scaleHalf;

			radius = (float) rx;

			double x = 0 + Math.sin((i * multi) * (Math.PI / 180)) * radius;
			double y = 0 + Math.cos((i * multi) * (Math.PI / 180)) * radius;
			double z = 0 + Math.sin((i * zMuli) * (Math.PI / 180)) * scaleHalf / 2;

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

		axisGrid = new AxisGrid(scale, scaleHalf, 1f);

		/*
		 * xMs = new LineStrip3d(0,1,0,1);
		 * xMs.setPoints(new float[]{0,100,-100,0,-100,-100}, false);
		 * yMs = new LineStrip3d(1,0,0,1);
		 * yMs.setPoints(new float[]{100,0,-100,-100,0,-100}, false);
		 * zMs = new LineStrip3d(0,0,1,1);
		 * zMs.setPoints(new float[]{0,0,100,0,0,-100}, false);
		 */

		ws = new VertexModel(true);

		bc = new BoundaryCube2(0, 0, 0, 10, 10, 10, new float[]
		{
				1, 0, 0, 1
		}, scaleHalf);

		// Cam
		//cam = new PerspectiveCamera(75f, (width / height), 1f);
		cam = new PerspectiveCamera();
		viewport = new FitViewport(width, height, cam);
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

		// Default Model
		setPlotPoints(createTorus(16, 360), new float[]
		{
				0, 0, 0
		},"X","Y","Z");
	}

	@Override
	public void render()
	{
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		// ls.render(cam,GL20.GL_LINE_STRIP);

		modelBatch.begin(cam);
		Gdx.gl.glLineWidth(gridLineWidth);
		modelBatch.render(bc.getModelInstance(), environment);
		axisGrid.render(cam, modelBatch, db, environment);
		modelBatch.end();

		modelBatch.begin(cam);
		Gdx.gl.glLineWidth(plotLineWidth);
		modelBatch.render(ws.getModelInstance(), environment);
		modelBatch.end();

		modelBatch.begin(cam);
		Gdx.gl.glLineWidth(minMaxLineWidth);
		modelBatch.render(xMaxVM.getModelInstance(), environment);
		modelBatch.render(xMinVM.getModelInstance(), environment);
		modelBatch.render(yMaxVM.getModelInstance(), environment);
		modelBatch.render(yMinVM.getModelInstance(), environment);
		modelBatch.render(zMaxVM.getModelInstance(), environment);
		modelBatch.render(zMinVM.getModelInstance(), environment);
		modelBatch.end();

		// Flush Decals
		db.flush();

		camController.update();
	}

	private void generateMinMaxLines(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax)
	{
		xMaxVM = new VertexModel(false);
		xMinVM = new VertexModel(false);

		xMaxVM.setVertices(new float[]
		{
				xMax, -scaleHalf, 0, 1, 0, 0, 1, xMax, scaleHalf, 0, 1, 0, 0, 1
		}, GL20.GL_LINE_STRIP);
		xMinVM.setVertices(new float[]
		{
				xMin, -scaleHalf, 0, 1, 0, 0, 1, xMin, scaleHalf, 0, 1, 0, 0, 1
		}, GL20.GL_LINE_STRIP);

		yMaxVM = new VertexModel(false);
		yMinVM = new VertexModel(false);

		yMaxVM.setVertices(new float[]
		{
				-scaleHalf, yMax, 0, 0, 1, 0, 1, scaleHalf, yMax, 0, 0, 1, 0, 1
		}, GL20.GL_LINE_STRIP);
		yMinVM.setVertices(new float[]
		{
				-scaleHalf, yMin, 0, 0, 1, 0, 1, scaleHalf, yMin, 0, 0, 1, 0, 1
		}, GL20.GL_LINE_STRIP);

		zMaxVM = new VertexModel(false);
		zMinVM = new VertexModel(false);

		zMaxVM.setVertices(new float[]
		{
				-scaleHalf, -scaleHalf, zMax, 0, 0, 1, 1, scaleHalf, -scaleHalf, zMax, 0, 0, 1, 1
		}, GL20.GL_LINE_STRIP);
		zMinVM.setVertices(new float[]
		{
				-scaleHalf, -scaleHalf, zMin, 0, 0, 1, 1, scaleHalf, -scaleHalf, zMin, 0, 0, 1, 1
		}, GL20.GL_LINE_STRIP);
	}
	
	public void setPlotPoints(float[] points, float[] center, String xAxisLabel, String yAxisLabel, String zAxisLabel)
	{
		camController.setTarget(new float[]
		{
				center[0], center[1], center[2] + scaleHalf
		});

		float xMax = Float.NEGATIVE_INFINITY;
		float yMax = Float.NEGATIVE_INFINITY;
		float zMax = Float.NEGATIVE_INFINITY;

		float xMin = Float.POSITIVE_INFINITY;
		float yMin = Float.POSITIVE_INFINITY;
		float zMin = Float.POSITIVE_INFINITY;

		// Min / Max
		for(int ii = 0; ii < points.length; ii += 3)
		{
			float x = points[ii];
			float y = points[ii + 1];
			float z = points[ii + 2] + scaleHalf;

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

		generateMinMaxLines(xMin,xMax,yMin,yMax,zMin,zMax);
		
		ws.setVertices(MeshHelper.colorAllVerticesRGBA(points, 0.4f, 0.95f, xMin, xMax, yMin, yMax, zMin, zMax, scaleHalf), GL20.GL_LINE_STRIP);

		axisGrid.setAxisLabels(xAxisLabel, yAxisLabel, zAxisLabel);
		axisGrid.setTickInterval(20);
		
		camController.reset();
	}

	public void setPlotLineWidth(float lineWidth)
	{
		this.plotLineWidth = lineWidth;
	}
	
	public void setMinMaxLineWidth(float lineWidth)
	{
		this.minMaxLineWidth = lineWidth;
	}
	
	public void setGridLineWidth(float lineWidth)
	{
		this.gridLineWidth = lineWidth;
	}
	
	@Override
	public void dispose()
	{
		ws.dispose();
	}

	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height);
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