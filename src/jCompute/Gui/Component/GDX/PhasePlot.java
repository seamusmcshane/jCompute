/**
 *
 */
package jCompute.gui.component.gdx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

import jCompute.gui.view.lib3d.AxisGrid;
import jCompute.gui.view.lib3d.BoundaryCube2;
import jCompute.gui.view.lib3d.MeshHelper;
import jCompute.gui.view.lib3d.VertexModel;

/**
 * @author Seamus McShane
 */
public class PhasePlot
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(PhasePlot.class);

	private float scale = 1000f;
	private float scaleHalf = scale / 2;

	// Scaling
	private boolean sameScale = false;

	// Axis Order
	private int xAxis = 0;
	private int yAxis = 1;
	private int zAxis = 2;

	// Line Widths
	private float plotLineWidth = 2f;
	private boolean drawBoundaryCube = false;

	// GlobalVars
	private float data[][];
	private String axisNames[];

	private AxisGrid axisGrid;

	private VertexModel ws;
	private boolean populated = false;

	private BoundaryCube2 bc;

	private float[] center;

	public PhasePlot()
	{
		center = new float[3];
	}

	public void glInit()
	{
		axisGrid = new AxisGrid(scale, scaleHalf, 1f);

		ws = new VertexModel(true);

		bc = new BoundaryCube2(0, 0, 0, 10, 10, 10, new float[]
		{
			1, 0, 0, 1
		}, scaleHalf);
	}

	public boolean setData(float[][] inData, String[] inNames, int xAxis, int yAxis, int zAxis)
	{
		// Need 3 arrays for X,Y,Z + axis names same length as data
		if((inData.length < 3) | (inData.length != inNames.length))
		{
			return false;
		}

		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;

		data = inData;
		axisNames = inNames;

		return true;
	}

	public void setPlotPoints(float[] points, float[] center, String[] axisLabels, float[][] minMax, float[][] firstLast)
	{
		
		float camZOffset = scaleHalf / 2;
		float pointsZOffset = scaleHalf;

		this.center[0] = center[0];
		this.center[1] = center[1];
		this.center[2] = center[2] + camZOffset;

		float xMax = Float.NEGATIVE_INFINITY;
		float yMax = Float.NEGATIVE_INFINITY;
		float zMax = Float.NEGATIVE_INFINITY;

		float xMin = Float.POSITIVE_INFINITY;
		float yMin = Float.POSITIVE_INFINITY;
		float zMin = Float.POSITIVE_INFINITY;

		if(points != null)
		{
			// Min / Max
			for(int ii = 0; ii < points.length; ii += 3)
			{
				float x = points[ii];
				float y = points[ii + 1];
				float z = points[ii + 2] + pointsZOffset;

				if(x > xMax)
				{
					xMax = x;
				}

				if(x < xMin)
				{
					xMin = x;
				}

				if(y > yMax)
				{
					yMax = y;
				}

				if(y < yMin)
				{
					yMin = y;
				}

				if(z > zMax)
				{
					zMax = z;
				}

				if(z < zMin)
				{
					zMin = z;
				}

			}

			ws.setVertices(MeshHelper.colorAllVerticesRGBA(points, 0.4f, 0.95f, xMin, xMax, yMin, yMax, zMin, zMax, pointsZOffset, yAxis, xAxis, zAxis),
			GL20.GL_LINE_STRIP);

			populated = true;
		}

		axisGrid.setTickIntervals(4, 4, 4);
		axisGrid.setAxisRangeMinMax(minMax);
		axisGrid.setValueMinMax(xMin, xMax, yMin, yMax, zMin, zMax);
		axisGrid.setLabelSize(2f);
		axisGrid.setAxisLabels(axisLabels);
		axisGrid.setFirstLast(firstLast);
		axisGrid.update();

		// camController.reset();
	}

	public void populateChart()
	{
		if((data == null) | (axisNames == null))
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
				xMax = x;
			}

			if(x < xMin)
			{
				xMin = x;
			}

			points[point + 1] = y;

			if(y > yMax)
			{
				yMax = y;
			}

			if(y < yMin)
			{
				yMin = y;
			}

			points[point + 2] = z;

			if(z > zMax)
			{
				zMax = z;
			}

			if(z < zMin)
			{
				zMin = z;
			}

			point += 3;
		}

		log.debug("Scaling Points");

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
		log.debug("Setting Points");

		// Set the values
		setPlotPoints(points, mids, names, minMax, firstLast);
	}

	public void render(ModelBatch modelBatch, Environment environment, Camera cam, DecalBatch db)
	{
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
		if(populated)
		{
			modelBatch.render(ws.getModelInstance(), environment);
		}
		modelBatch.end();
	}

	public void setScalingMode(boolean sameScale)
	{
		this.sameScale = sameScale;
	}

	public void setPlotLineWidth(float lineWidth)
	{
		plotLineWidth = lineWidth;
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
		drawBoundaryCube = enabled;
	}

	public void enableMinMax(boolean enabled)
	{
		axisGrid.setMinMaxDisplayed(enabled);
	}

	public float[] getCenter()
	{
		return center;
	}

	public void replot()
	{
		populateChart();
	}

	public void dispose()
	{
		ws.dispose();
		bc.getModelInstance().model.dispose();
		axisGrid.dispose();
	}

}
