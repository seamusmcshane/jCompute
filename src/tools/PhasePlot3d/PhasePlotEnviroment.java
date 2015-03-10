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

	private OrbitalCameraInputController camController;

	private float width;
	private float height;

	private float plotLineWidth = 2f;
	private boolean drawBoundaryCube = false;

	// GlobalVars
	private float data[][];
	private String axisNames[];

	// Axis Order
	private int xAxis = 0;
	private int yAxis = 1;
	private int zAxis = 2;
	
	// Scaling
	private boolean sameScale = false;

	public PhasePlotEnviroment(float width, float height)
	{
		this.width = width;
		this.height = height;
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

		ws = new VertexModel(true);

		bc = new BoundaryCube2(0, 0, 0, 10, 10, 10, new float[]
		{
				1, 0, 0, 1
		}, scaleHalf);

		// Cam
		// cam = new PerspectiveCamera(75f, (width / height), 1f);
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

		// Default Model
		setPlotPoints(createTorus(16, 360), new float[]
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
		// Background
		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Depth Buffer Mode
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);

		// Alpha Blending
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		// Grid and Boundary Cube
		modelBatch.begin(cam);
		if(drawBoundaryCube)
		{
			modelBatch.render(bc.getModelInstance(), environment);
		}
		modelBatch.end();

		axisGrid.render(cam, modelBatch, db, environment);

		// The Plot lines
		modelBatch.begin(cam);
		Gdx.gl.glLineWidth(plotLineWidth);
		modelBatch.render(ws.getModelInstance(), environment);
		modelBatch.end();

		// Flush Decals
		db.flush();

		camController.update();
	}

	public boolean setData(float[][] inData, String[] inNames)
	{
		// Need 3 arrays for X,Y,Z + axis names same length as data
		if(inData.length < 3 | (inData.length != inNames.length))
		{
			return false;
		}

		this.data = inData;
		this.axisNames = inNames;

		return true;
	}

	public void populateChart()
	{
		if(data == null | axisNames == null)
		{
			return;
		}

		// Samples - All Sample Length
		int numSamples = data[0].length;

		// Get the correct drawing offset for the points
		float envScale = scaleHalf;

		// X,Y,Z
		float[] points = new float[numSamples * 3];

		float xMax = Float.NEGATIVE_INFINITY;
		float yMax = Float.NEGATIVE_INFINITY;
		float zMax = Float.NEGATIVE_INFINITY;

		float xMin = Float.POSITIVE_INFINITY;
		float yMin = Float.POSITIVE_INFINITY;
		float zMin = Float.POSITIVE_INFINITY;

		// Axis Names
		String[] names = new String[3];
		names[0] = new String(axisNames[xAxis]);
		names[1] = new String(axisNames[yAxis]);
		names[2] = new String(axisNames[zAxis]);

		int point = 0;

		// Assumes Population chart
		for(int i = 0; i < numSamples; i++)
		{
			// Plants, Predator, Prey
			float x = data[xAxis][i];
			float y = data[yAxis][i];
			float z = data[zAxis][i];

			points[point] = x;

			if(x > xMax)
			{
				xMax = (float) x;
			}

			if(x < xMin)
			{
				xMin = (float) x;
			}

			points[point + 1] = y;

			if(y > yMax)
			{
				yMax = (float) y;
			}

			if(y < yMin)
			{
				yMin = (float) y;
			}

			points[point + 2] = z;

			if(z > zMax)
			{
				zMax = (float) z;
			}

			if(z < zMin)
			{
				zMin = (float) z;
			}

			point += 3;
		}

		System.out.println("Scaling Points");

		// Used for generating Tick Values
		float[][] minMax = new float[3][2];

		if(sameScale)
		{
			// Set Scales to min/max for each axis
			float scaleMin = Math.min(Math.min(xMin, yMin), zMin);
			float scaleMax = Math.max(Math.max(xMax, yMax), zMax);
			minMax[xAxis][0] = scaleMin;
			minMax[xAxis][1] = scaleMax;
			minMax[yAxis][0] = scaleMin;
			minMax[yAxis][1] = scaleMax;
			minMax[zAxis][0] = scaleMin;
			minMax[zAxis][1] = scaleMax;
		}
		else
		{
			// Scale each axis independently
			minMax[xAxis][0] = xMin;
			minMax[xAxis][1] = xMax;
			minMax[yAxis][0] = yMin;
			minMax[yAxis][1] = yMax;
			minMax[zAxis][0] = zMin;
			minMax[zAxis][1] = zMax;
		}

		// value scaling
		float xScale = ((envScale * 2) / xMax);
		float yScale = ((envScale * 2) / yMax);
		float zScale = ((envScale * 2) / zMax);

		// Mid of the phase plot values
		float[] mids = new float[3];

		mids[xAxis] = 0;
		mids[yAxis] = 0;
		mids[zAxis] = 0;

		// First+Last * (Actual Values + Scaled values) (For label text and
		// display)
		float[][] firstLast = new float[6][2];

		// First (Actual/Scaled)
		firstLast[0][0] = points[0];
		firstLast[1][0] = points[1];
		firstLast[2][0] = points[2];
		firstLast[3][0] = 0;
		firstLast[4][0] = 0;
		firstLast[5][0] = 0;

		// Last (Actual/Scaled)
		firstLast[0][1] = points[(numSamples * 3) - 3];
		firstLast[1][1] = points[(numSamples * 3) - 2];
		firstLast[2][1] = points[(numSamples * 3) - 1];
		firstLast[3][1] = 0;
		firstLast[4][1] = 0;
		firstLast[5][1] = 0;

		if(sameScale)
		{
			float scale = Math.min(xScale, yScale);
			scale = Math.min(scale, zScale);

			for(int p = 0; p < (numSamples * 3); p += 3)
			{
				points[p] = (points[p] * scale) - envScale;
				points[p + 1] = (points[p + 1] * scale) - envScale;
				points[p + 2] = (points[p + 2] * scale) - envScale;
			}

			mids[xAxis] = (((xMax / 2) + (xMin / 2)) * scale) - envScale;
			mids[yAxis] = (((yMax / 2) + (yMin / 2)) * scale) - envScale;
			mids[zAxis] = (((zMax / 2) + (zMin / 2)) * scale) - envScale;
		}
		else
		{
			for(int p = 0; p < (numSamples * 3); p += 3)
			{
				points[p] = (points[p] * xScale) - envScale;
				points[p + 1] = (points[p + 1] * yScale) - envScale;
				points[p + 2] = (points[p + 2] * zScale) - envScale;
			}
			mids[xAxis] = (((xMax / 2) + (xMin / 2)) * xScale) - envScale;
			mids[yAxis] = (((yMax / 2) + (yMin / 2)) * yScale) - envScale;
			mids[zAxis] = (((zMax / 2) + (zMin / 2)) * zScale) - envScale;
		}

		// Scaled First
		firstLast[3][0] = points[0];
		firstLast[4][0] = points[1];
		firstLast[5][0] = points[2];
		// Scaled Last
		firstLast[3][1] = points[(numSamples * 3) - 3];
		firstLast[4][1] = points[(numSamples * 3) - 2];
		firstLast[5][1] = points[(numSamples * 3) - 1];
		System.out.println("Setting Points");

		// Set the values
		setPlotPoints(points, mids, names, minMax, firstLast);
	}

	private void setPlotPoints(float[] points, float[] center, String[] axisLabels, float[][] minMax,
			float[][] firstLast)
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

		ws.setVertices(
				MeshHelper.colorAllVerticesRGBA(points, 0.4f, 0.95f, xMin, xMax, yMin, yMax, zMin, zMax, scaleHalf,yAxis,xAxis,zAxis),
				GL20.GL_LINE_STRIP);

		axisGrid.setTickIntervals(4);
		axisGrid.setAxisRangeMinMax(minMax);
		axisGrid.setValueMinMax(xMin, xMax, yMin, yMax, zMin, zMax);
		axisGrid.setLabelSize(2f);
		axisGrid.setAxisLabels(axisLabels);
		axisGrid.setFirstLast(firstLast);
		axisGrid.update();

		camController.reset();
	}

	public void setPlotLineWidth(float lineWidth)
	{
		this.plotLineWidth = lineWidth;
	}

	public void setMinMaxLineWidth(float lineWidth)
	{
		axisGrid.setMinMaxLineWidth(lineWidth);
	}

	public void setGridLineWidth(float lineWidth)
	{
		axisGrid.setGridLineWidth(lineWidth);
	}

	public void enableBoundaryCube(boolean enabled)
	{
		this.drawBoundaryCube = enabled;
	}

	public void enableMinMax(boolean enabled)
	{
		axisGrid.setMinMaxDisplayed(enabled);
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

	public void setScalingMode(boolean sameScale)
	{
		this.sameScale = sameScale;
	}

	public void replot()
	{
		populateChart();
	}
}