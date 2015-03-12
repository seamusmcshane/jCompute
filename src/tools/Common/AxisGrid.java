package tools.Common;

import tools.MeshHelper;
import tools.PhasePlot3d.LineGrid;
import tools.PhasePlot3d.VertexModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class AxisGrid
{
	// Floor Grid
	private LineGrid floor;

	// Floor Ticks
	private VertexModel floorNorthTicks;
	private VertexModel floorSouthTicks;
	private VertexModel floorEastTicks;
	private VertexModel floorWestTicks;

	// Floor TickLabels
	private Decal[] floorNorthTickLabels;
	private Decal[] floorSouthTickLabels;
	private Decal[] floorEastTickLabels;
	private Decal[] floorWestTickLabels;

	// Floor Axis Labels
	private Decal gridXAxisName[];
	private Decal gridYAxisName[];
	private Decal gridZAxisName[];

	// Wall Grid
	private LineGrid eastSide;
	private LineGrid westSide;
	private LineGrid northSide;
	private LineGrid southSide;

	// Wall Axis Ticks
	private VertexModel wallNorthEastTicks;
	private VertexModel wallNorthWestTicks;
	private VertexModel wallSouthEastTicks;
	private VertexModel wallSouthWestTicks;

	// Wall Axis Tick Labels
	private Decal[] wallNorthEastTickLabels;
	private Decal[] wallNorthWestTickLabels;
	private Decal[] wallSouthEastTickLabels;
	private Decal[] wallSouthWestTickLabels;

	// MinMax Rects
	private VertexModel floorMinMaxRect;
	private VertexModel northMinMaxRect;
	private VertexModel southMinMaxRect;
	private VertexModel eastMinMaxRect;
	private VertexModel westMinMaxRect;

	// Scaling + Dimensions
	private float gridSize;
	private float trans;
	private float heightScale;
	private int intervals;
	private float labelSize = 1f;

	private float gridLineWidth = 0.5f;
	private float minMaxLineWidth = 3f;

	// Hardcoded Sizes
	private float standardTickLength = 50f;
	private float tickLabelPad = 25f;
	private float axisLabelPad = standardTickLength * 4 + tickLabelPad;

	// Decal Font
	private BitmapFont decalFont = new BitmapFont();

	// Axis Ranges (x,y,z|min,max)
	private float axisRanges[][] = new float[3][2];

	// Axis Order
	private final int XAXIS = 1;
	private final int YAXIS = 0;
	private final int ZAXIS = 2;

	// Min Max Order
	private final int MIN = 0;
	private final int MAX = 1;

	// Display ZAxis Ticks/Labels
	boolean zAxisEnabled = true;

	// Display MinMax
	boolean drawMinMax = true;

	// Display Floor grid
	boolean floorGrid = true;

	// MinMax Lines
	float xMax = Float.NEGATIVE_INFINITY;
	float yMax = Float.NEGATIVE_INFINITY;
	float zMax = Float.NEGATIVE_INFINITY;

	float xMin = Float.POSITIVE_INFINITY;
	float yMin = Float.POSITIVE_INFINITY;
	float zMin = Float.POSITIVE_INFINITY;

	// Has the axis grid been populated with values
	boolean generated = false;

	private float[] gridColorRed = {1,0,0,1};
	private float[] gridColorBlack = {0,0,0,1};
	
	private float[] gridNorthColor;
	
	public AxisGrid(float gridSize, float trans, float heightScale)
	{
		this.gridSize = gridSize;
		this.trans = trans;
		this.heightScale = heightScale;

		gridNorthColor = gridColorBlack;
		
		setTickIntervals(4);

		// Floor Tick Labels
		gridXAxisName = new Decal[2];
		gridYAxisName = new Decal[2];

		// North + South / East + West
		gridZAxisName = new Decal[4];

		// Axis Labels
		setAxisLabels(new String[]
		{
			"X Axis", "Y Axis", "Z Axis"
		});
	}

	public void setFloorGridDisplayed(boolean enabled)
	{
		this.floorGrid = enabled;
	}

	public void setZAxisDisplayed(boolean zAxisEnabled)
	{
		this.zAxisEnabled = zAxisEnabled;
	}

	public void setMinMaxDisplayed(boolean displayed)
	{
		this.drawMinMax = displayed;
	}

	private void generateGrids(int intervals)
	{
		// Account for a scaled Height in wall position
		float wallHeightOffset = gridSize - (gridSize * heightScale);

		// Grid Walls (height is scaled)
		northSide = new LineGrid(intervals, gridSize, gridNorthColor, trans);
		northSide.scale(1f, 1f, heightScale);
		northSide.rotate(1, 0, 0, 90);
		northSide.rotate(0, 1, 0, 90);
		northSide.transform(gridSize, 0, -wallHeightOffset / 2);

		southSide = new LineGrid(intervals, gridSize, gridColorBlack, trans);
		southSide.scale(1f, 1f, heightScale);
		southSide.rotate(1, 0, 0, 90);
		southSide.rotate(0, 1, 0, 90);
		southSide.transform(0, 0, -wallHeightOffset / 2);

		eastSide = new LineGrid(intervals, gridSize, gridColorBlack, trans);
		eastSide.scale(1f, 1f, heightScale);
		eastSide.rotate(1, 0, 0, 90);
		eastSide.transform(0, -gridSize, -wallHeightOffset / 2);

		westSide = new LineGrid(intervals, gridSize, gridColorBlack, trans);
		westSide.scale(1f, 1f, heightScale);
		westSide.rotate(1, 0, 0, 90);
		westSide.transform(0, 0, -wallHeightOffset / 2);

		// Floor
		floor = new LineGrid(intervals, gridSize, gridColorBlack, trans);
	}

	private void generateGridsAndTicks(int intervals)
	{
		generateGrids(intervals);
		generateMinMaxLines();

		// North Floor Axis Ticks + Labels
		Vector3 floorNorthTrans = new Vector3(gridSize / 2, -gridSize / 2, -gridSize / 2 + trans);
		floorNorthTicks = generateTicks(gridSize, standardTickLength, intervals);
		floorNorthTicks.getModelInstance().transform.trn(floorNorthTrans);
		floorNorthTickLabels = generateTickLabels(axisRanges[XAXIS][MIN], axisRanges[XAXIS][MAX], gridSize,
				standardTickLength, tickLabelPad, intervals, floorNorthTrans);

		// South Floor Axis Ticks + Labels
		Vector3 floorSouthTrans = new Vector3(-gridSize / 2 - standardTickLength, -gridSize / 2, -gridSize / 2 + trans);
		floorSouthTicks = generateTicks(gridSize, standardTickLength, intervals);
		floorSouthTicks.getModelInstance().transform.trn(floorSouthTrans);
		floorSouthTickLabels = generateTickLabels(axisRanges[XAXIS][MIN], axisRanges[XAXIS][MAX], gridSize,
				-standardTickLength, tickLabelPad, intervals, floorSouthTrans);

		// East Floor Axis Ticks + Labels
		Vector3 floorEastTrans = new Vector3(gridSize / 2, -gridSize / 2 - standardTickLength, -gridSize / 2 + trans);
		floorEastTicks = generateTicks(gridSize, standardTickLength, intervals);
		floorEastTicks.getModelInstance().transform.trn(floorEastTrans);
		floorEastTicks.getModelInstance().transform.rotate(0, 0, 1, 90f);
		floorEastTickLabels = generateTickLabels(axisRanges[YAXIS][MIN], axisRanges[YAXIS][MAX], gridSize,
				-standardTickLength, tickLabelPad, intervals, floorEastTrans);

		// Flip Coords (rotation 90 around z) - Corrects for standardTickLength
		// offset
		for(Decal decal : floorEastTickLabels)
		{
			float x = decal.getPosition().x + (standardTickLength * 2);
			float y = decal.getPosition().y + (standardTickLength);
			float z = decal.getPosition().z;

			decal.setPosition(y, -x, z);
		}

		// West Floor Axis Ticks + Labels - Corrects for standardTickLength
		// offset
		Vector3 floorWestTrans = new Vector3(gridSize / 2, gridSize / 2, -gridSize / 2 + trans);
		floorWestTicks = generateTicks(gridSize, standardTickLength, intervals);
		floorWestTicks.getModelInstance().transform.trn(floorWestTrans);
		floorWestTicks.getModelInstance().transform.rotate(0, 0, 1, 90f);
		floorWestTickLabels = generateTickLabels(axisRanges[YAXIS][MIN], axisRanges[YAXIS][MAX], gridSize,
				-standardTickLength, tickLabelPad, intervals, floorEastTrans);

		// Flip Coords (rotation 270 around z)
		for(Decal decal : floorWestTickLabels)
		{
			float x = decal.getPosition().x + (standardTickLength * 2);
			float y = decal.getPosition().y + (standardTickLength);
			float z = decal.getPosition().z;

			decal.setPosition(y, x, z);
		}

		// Wall Ticks North East
		Vector3 wallNorthEastTrans = new Vector3((-gridSize / 2), (-gridSize / 2), -gridSize / 2 + trans);
		wallNorthEastTicks = generateTicks(gridSize, standardTickLength, intervals);
		wallNorthEastTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallNorthEastTicks.getModelInstance().transform.trn(wallNorthEastTrans);
		wallNorthEastTicks.getModelInstance().transform.rotate(0, 0, 1, 225f);
		wallNorthEastTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);
		wallNorthEastTickLabels = generateTickLabels(axisRanges[ZAXIS][MIN], axisRanges[ZAXIS][MAX], gridSize,
				standardTickLength, tickLabelPad, intervals, wallNorthEastTrans);

		// Flip Coords (Rotate around Y+Translated on z)
		for(Decal decal : wallNorthEastTickLabels)
		{
			float x = decal.getPosition().x + (standardTickLength - tickLabelPad) + gridSize;
			float y = decal.getPosition().y + (standardTickLength - (tickLabelPad * 2)) + gridSize / 2;
			float z = decal.getPosition().z - (standardTickLength + tickLabelPad * 2) - gridSize / 2;

			decal.setPosition(x, z, y);
		}

		// Wall Ticks North West
		Vector3 wallNorthWestTrans = new Vector3((gridSize / 2), (gridSize / 2), -gridSize / 2 + trans);
		wallNorthWestTicks = generateTicks(gridSize, standardTickLength, intervals);
		wallNorthWestTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallNorthWestTicks.getModelInstance().transform.trn(wallNorthWestTrans);
		wallNorthWestTicks.getModelInstance().transform.rotate(0, 0, 1, 45f);
		wallNorthWestTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);
		wallNorthWestTickLabels = generateTickLabels(axisRanges[ZAXIS][MIN], axisRanges[ZAXIS][MAX], gridSize,
				standardTickLength, tickLabelPad, intervals, wallNorthWestTrans);

		// Flip Coords (Rotate around Y+Translated on z)
		for(Decal decal : wallNorthWestTickLabels)
		{
			float x = decal.getPosition().x + (standardTickLength - tickLabelPad);
			float y = decal.getPosition().y + (standardTickLength - (tickLabelPad * 2)) - gridSize / 2;
			float z = decal.getPosition().z + (standardTickLength + tickLabelPad * 2) + gridSize / 2;

			decal.setPosition(x, z, y);
		}

		// Wall Ticks South East
		Vector3 wallSouthEastTrans = new Vector3((-gridSize / 2), (gridSize / 2), -gridSize / 2 + trans);
		wallSouthEastTicks = generateTicks(gridSize, standardTickLength, intervals);
		wallSouthEastTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallSouthEastTicks.getModelInstance().transform.trn(wallSouthEastTrans);
		wallSouthEastTicks.getModelInstance().transform.rotate(0, 0, 1, 135f);
		wallSouthEastTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);

		wallSouthEastTickLabels = generateTickLabels(axisRanges[ZAXIS][MIN], axisRanges[ZAXIS][MAX], gridSize,
				-standardTickLength, tickLabelPad, intervals, wallSouthEastTrans);

		// Flip Coords (Rotate around Y+Translated on z)
		for(Decal decal : wallSouthEastTickLabels)
		{
			float x = decal.getPosition().x - (standardTickLength + tickLabelPad);
			float y = decal.getPosition().y + (standardTickLength - (tickLabelPad * 2)) - gridSize / 2;
			float z = decal.getPosition().z - (standardTickLength + tickLabelPad * 2) - gridSize / 2;

			decal.setPosition(x, z, y);
		}

		// Wall Ticks South West
		Vector3 wallSouthWestTrans = new Vector3((gridSize / 2), -(gridSize / 2), -gridSize / 2 + trans);
		wallSouthWestTicks = generateTicks(gridSize, standardTickLength, intervals);
		wallSouthWestTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallSouthWestTicks.getModelInstance().transform.trn(wallSouthWestTrans);
		wallSouthWestTicks.getModelInstance().transform.rotate(0, 0, 1, 315f);
		wallSouthWestTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);

		wallSouthWestTickLabels = generateTickLabels(axisRanges[ZAXIS][MIN], axisRanges[ZAXIS][MAX], gridSize,
				-standardTickLength, tickLabelPad, intervals, wallSouthWestTrans);

		// Flip Coords (Rotate around Y+Translated on z)
		for(Decal decal : wallSouthWestTickLabels)
		{
			float x = decal.getPosition().x - (standardTickLength + tickLabelPad) - gridSize;
			float y = decal.getPosition().y + (standardTickLength - (tickLabelPad * 2)) + gridSize / 2;
			float z = decal.getPosition().z + (standardTickLength + tickLabelPad * 2) + gridSize / 2;

			decal.setPosition(x, z, y);
		}
	}

	public Decal[] generateTickLabels(float valMin, float valMax, float gridSize, float tickLength, float pad,
			int intervals, Vector3 translate)
	{
		int numDecals = intervals + 1;

		Decal[] decals = new Decal[numDecals];

		// float valStep = (int) ((valMax - valMin) / intervals);
		float valStep = (int) ((valMax) / intervals);

		float posInteval = gridSize / intervals;

		for(int i = 0; i < numDecals; i++)
		{
			decals[i] = generateDecal(String.valueOf((int) (i * valStep)));
			decals[i].getPosition().set(tickLength + pad, (i * posInteval), 0);
			decals[i].setRotationX(90);

			// Apply World Trans
			decals[i].translate(translate);
		}

		return decals;
	}

	private VertexModel generateTicks(float gridSize, float tickLength, int div)
	{
		VertexModel vModel = new VertexModel(false);

		float start = 0;
		float end = tickLength;

		int gridSteps = div + 1;
		float xInterval = gridSize / (gridSteps - 1);

		float[] vertices = new float[(gridSteps * 3) * 2];

		int vLen = vertices.length;

		int line = 0;
		// X Step Interval Lines
		for(int v = 0; v < vLen; v += 6)
		{
			vertices[v] = start;					// X1
			vertices[v + 1] = (line * xInterval);	// Y1
			vertices[v + 2] = 0;					// Z1
			vertices[v + 3] = end;					// X2
			vertices[v + 4] = (line * xInterval);	// Y2
			vertices[v + 5] = 0;					// Z2

			line++;
		}

		float[] cVerts = MeshHelper.colorAllVertices(vertices, new float[]
		{
			0, 0, 0, 1
		});

		vModel.setVertices(cVerts, GL20.GL_LINES);

		return vModel;
	}

	public void setTickIntervals(int intervals)
	{
		this.intervals = intervals;
	}

	public void setAxisLabels(String[] axisLabels)
	{
		// X Axis Labels
		gridXAxisName[0] = generateDecal(axisLabels[XAXIS]);
		gridXAxisName[0].getPosition().set(gridSize / 2 + (axisLabelPad), gridSize / 2 - trans, 0);
		gridXAxisName[0].setRotationX(90);

		gridXAxisName[1] = generateDecal(axisLabels[XAXIS]);
		gridXAxisName[1].getPosition().set(-gridSize / 2 - (axisLabelPad), gridSize / 2 - trans, 0);
		gridXAxisName[1].setRotationX(90);

		// Y Axis Labels
		gridYAxisName[0] = generateDecal(axisLabels[YAXIS]);
		gridYAxisName[0].getPosition().set(gridSize / 2 - trans, gridSize / 2 + (axisLabelPad), 0);
		gridYAxisName[0].setRotationX(90);

		gridYAxisName[1] = generateDecal(axisLabels[YAXIS]);
		gridYAxisName[1].getPosition().set(gridSize / 2 - trans, -gridSize / 2 - (axisLabelPad), 0);
		gridYAxisName[1].setRotationX(90);

		// Z Axis Column Labels (NorthEast,SouthEast,SouthWest,NorthWest)
		gridZAxisName[0] = generateDecal(axisLabels[ZAXIS]/* + "NorthEast" */);
		gridZAxisName[0].getPosition().set(gridSize / 2 + (axisLabelPad), -gridSize / 2 - (axisLabelPad), gridSize / 2);
		gridZAxisName[0].setRotationX(90);

		gridZAxisName[1] = generateDecal(axisLabels[ZAXIS]/* + "SouthEast" */);
		gridZAxisName[1].getPosition()
				.set(-gridSize / 2 - (axisLabelPad), -gridSize / 2 - (axisLabelPad), gridSize / 2);
		gridZAxisName[1].setRotationX(90);

		gridZAxisName[2] = generateDecal(axisLabels[ZAXIS]/* + "SouthWest" */);
		gridZAxisName[2].getPosition().set(-gridSize / 2 - (axisLabelPad), gridSize / 2 + (axisLabelPad), gridSize / 2);
		gridZAxisName[2].setRotationX(90);

		gridZAxisName[3] = generateDecal(axisLabels[ZAXIS]/* + "NorthWest" */);
		gridZAxisName[3].getPosition()
				.set(+gridSize / 2 + (axisLabelPad), +gridSize / 2 + (axisLabelPad), gridSize / 2);
		gridZAxisName[3].setRotationX(90);
	}

	private Decal generateDecal(String decalText)
	{
		String svalue = decalText;
		int tWidth = (int) (decalFont.getBounds(svalue).width + 2);
		int tHeight = (int) (decalFont.getBounds(svalue).height + 2);

		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, tWidth, tHeight, false);

		Matrix4 pm = new Matrix4();
		pm.setToOrtho2D(0, 0, tWidth, tHeight);

		decalFont.setColor(Color.BLACK);
		// decalFont.scale(0.1f);
		SpriteBatch sb = new SpriteBatch();
		sb.setProjectionMatrix(pm);
		fbo.begin();
		sb.begin();
		decalFont.draw(sb, svalue, 0, tHeight);
		sb.end();
		fbo.end();

		TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, tWidth, tHeight);
		t1.flip(false, true);

		return Decal.newDecal(tWidth * labelSize, tHeight * labelSize, t1, true);
	}

	private void generateMinMaxLines()
	{
		float overlay = 1f;

		float scaleHalf = gridSize / 2;

		float[][] colors = new float[3][3];

		// AXIS Order Dependent
		colors[XAXIS] = new float[]
		{
			0, 1, 0, 1
		};
		colors[YAXIS] = new float[]
		{
			1, 0, 0, 1
		};
		colors[ZAXIS] = new float[]
		{
			0, 0, 1, 1
		};

		floorMinMaxRect = new VertexModel(false);

		floorMinMaxRect.setVertices(new float[]
		{
			// MinX
			xMax - overlay, yMin, overlay, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2], colors[XAXIS][3],
			xMax - overlay, yMax, overlay, colors[XAXIS][0], colors[XAXIS][1],
			colors[XAXIS][2],
			colors[XAXIS][3],
			// MaxX
			xMin + overlay, yMin, overlay, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2], colors[XAXIS][3],
			xMin + overlay, yMax, overlay, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2],
			colors[XAXIS][3],
			// MinY
			xMin, yMin, overlay, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3], xMax, yMin,
			overlay, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3],
			// MaxY
			xMin, yMax, overlay, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3], xMax, yMax,
			overlay, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3]
		}, GL20.GL_LINES);

		northMinMaxRect = new VertexModel(false);

		northMinMaxRect.setVertices(new float[]
		{
			// MinY
			scaleHalf - overlay, yMin, zMin, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3],
			scaleHalf - overlay, yMin, zMax, colors[YAXIS][0], colors[YAXIS][1],
			colors[YAXIS][2],
			colors[YAXIS][3],
			// MaxY
			scaleHalf - overlay, yMax, zMin, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3],
			scaleHalf - overlay, yMax, zMax, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2],
			colors[YAXIS][3],
			// MinZ
			scaleHalf - overlay, yMin, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			scaleHalf - overlay, yMax, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			// MaxY
			scaleHalf - overlay, yMin, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			scaleHalf - overlay, yMax, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],

		}, GL20.GL_LINES);

		southMinMaxRect = new VertexModel(false);

		southMinMaxRect.setVertices(new float[]
		{
			// MinY
			-scaleHalf + overlay, yMin, zMin, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3],
			-scaleHalf + overlay, yMin, zMax, colors[YAXIS][0], colors[YAXIS][1],
			colors[YAXIS][2],
			colors[YAXIS][3],
			// MaxY
			-scaleHalf + overlay, yMax, zMin, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2], colors[YAXIS][3],
			-scaleHalf + overlay, yMax, zMax, colors[YAXIS][0], colors[YAXIS][1], colors[YAXIS][2],
			colors[YAXIS][3],
			// MinZ
			-scaleHalf + overlay, yMin, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			-scaleHalf + overlay, yMax, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			// MaxY
			-scaleHalf + overlay, yMin, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			-scaleHalf + overlay, yMax, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],

		}, GL20.GL_LINES);

		eastMinMaxRect = new VertexModel(false);

		eastMinMaxRect.setVertices(new float[]
		{
			// MinX
			xMin, -scaleHalf + overlay, zMin, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2], colors[XAXIS][3],
			xMin, -scaleHalf + overlay, zMax, colors[XAXIS][0], colors[XAXIS][1],
			colors[XAXIS][2],
			colors[XAXIS][3],
			// MaxX
			xMax, -scaleHalf + overlay, zMin, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2], colors[XAXIS][3],
			xMax, -scaleHalf + overlay, zMax, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2],
			colors[XAXIS][3],
			// MinZ
			xMin, -scaleHalf + overlay, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			xMax, -scaleHalf + overlay, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			// MaxX
			xMin, -scaleHalf + overlay, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			xMax, -scaleHalf + overlay, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3]

		}, GL20.GL_LINES);

		westMinMaxRect = new VertexModel(false);

		westMinMaxRect.setVertices(new float[]
		{
			// MinX
			xMin, scaleHalf - overlay, zMin, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2], colors[XAXIS][3],
			xMin, scaleHalf - overlay, zMax, colors[XAXIS][0], colors[XAXIS][1],
			colors[XAXIS][2],
			colors[XAXIS][3],
			// MaxX
			xMax, scaleHalf - overlay, zMin, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2], colors[XAXIS][3],
			xMax, scaleHalf - overlay, zMax, colors[XAXIS][0], colors[XAXIS][1], colors[XAXIS][2],
			colors[XAXIS][3],
			// MinZ
			xMin, scaleHalf - overlay, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			xMax, scaleHalf - overlay, zMin, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			// MaxX
			xMin, scaleHalf - overlay, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3],
			xMax, scaleHalf - overlay, zMax, colors[ZAXIS][0], colors[ZAXIS][1], colors[ZAXIS][2], colors[ZAXIS][3]

		}, GL20.GL_LINES);
	}

	public void update()
	{
		this.generated = false;
	}

	public void render(Camera cam, ModelBatch modelBatch, DecalBatch db, Environment environment)
	{
		if(!generated)
		{
			generateGridsAndTicks(intervals);
			generated = true;
		}

		float northDis = cam.position.dst2(northSide.getLocation());
		float southDis = cam.position.dst2(southSide.getLocation());
		float eastDis = cam.position.dst2(eastSide.getLocation());
		float westDis = cam.position.dst2(westSide.getLocation());

		boolean northVis = false;
		boolean southVis = false;
		boolean eastVis = false;
		boolean westVis = false;

		if(northDis > southDis)
		{
			northVis = true;
		}
		else
		{
			southVis = true;
		}

		if(eastDis > westDis)
		{
			eastVis = true;
		}
		else
		{
			westVis = true;
		}

		modelBatch.begin(cam);
		Gdx.gl.glLineWidth(gridLineWidth);

		if(northVis)
		{
			modelBatch.render(northSide.getModelInstance(), environment);

			// South Ticks
			modelBatch.render(floorSouthTicks.getModelInstance());
			gridXAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridXAxisName[1]);

			for(Decal decal : floorSouthTickLabels)
			{
				decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(decal);
			}
		}

		if(southVis)
		{
			modelBatch.render(southSide.getModelInstance(), environment);

			// North Ticks
			modelBatch.render(floorNorthTicks.getModelInstance());
			gridXAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridXAxisName[0]);

			for(Decal decal : floorNorthTickLabels)
			{
				decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(decal);
			}
		}

		if(eastVis)
		{
			modelBatch.render(eastSide.getModelInstance(), environment);

			// West Tick
			modelBatch.render(floorWestTicks.getModelInstance());
			gridYAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridYAxisName[0]);

			for(Decal decal : floorWestTickLabels)
			{
				decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(decal);
			}
		}

		if(westVis)
		{
			modelBatch.render(westSide.getModelInstance(), environment);

			// East Ticks
			modelBatch.render(floorEastTicks.getModelInstance());
			gridYAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridYAxisName[1]);

			for(Decal decal : floorEastTickLabels)
			{
				decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(decal);
			}
		}

		if(zAxisEnabled)
		{
			// NorthWest ZColumn
			if(southVis ^ eastVis)
			{
				gridZAxisName[3].lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(gridZAxisName[3]);
				modelBatch.render(wallNorthWestTicks.getModelInstance(), environment);

				for(Decal decal : wallSouthEastTickLabels)
				{
					decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
					db.add(decal);
				}
			}

			// NorthEast ZColumn
			if(southVis ^ westVis)
			{
				gridZAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(gridZAxisName[0]);
				modelBatch.render(wallSouthWestTicks.getModelInstance(), environment);

				for(Decal decal : wallSouthWestTickLabels)
				{
					decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
					db.add(decal);
				}
			}

			// SouthEast ZColumn
			if(northVis ^ westVis)
			{
				gridZAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(gridZAxisName[1]);
				modelBatch.render(wallNorthEastTicks.getModelInstance(), environment);

				for(Decal decal : wallNorthWestTickLabels)
				{
					decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
					db.add(decal);
				}
			}

			// SouthWest ZColumn
			if(northVis ^ eastVis)
			{
				gridZAxisName[2].lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(gridZAxisName[2]);
				modelBatch.render(wallSouthEastTicks.getModelInstance(), environment);

				for(Decal decal : wallNorthEastTickLabels)
				{
					decal.lookAt(cam.position.cpy(), cam.up.cpy().nor());
					db.add(decal);
				}
			}

		}

		// Floor Grid
		if(floorGrid)
		{
			modelBatch.render(floor.getModelInstance(), environment);
		}

		modelBatch.end();

		// Min Max Lines
		if(drawMinMax)
		{
			modelBatch.begin(cam);
			Gdx.gl.glLineWidth(minMaxLineWidth);
			/*
			 * modelBatch.render(zMaxVM.getModelInstance(), environment);
			 * modelBatch.render(zMinVM.getModelInstance(), environment);
			 */
			modelBatch.render(floorMinMaxRect.getModelInstance(), environment);

			if(northVis)
			{
				modelBatch.render(northMinMaxRect.getModelInstance(), environment);
			}

			if(southVis)
			{
				modelBatch.render(southMinMaxRect.getModelInstance(), environment);
			}

			if(eastVis)
			{
				modelBatch.render(eastMinMaxRect.getModelInstance(), environment);
			}

			if(westVis)
			{
				modelBatch.render(westMinMaxRect.getModelInstance(), environment);
			}

			modelBatch.end();
		}
	}

	public void setAxisRangeMinMax(float[][] minMax)
	{
		// Invert Min/Max
		axisRanges[XAXIS][MIN] = minMax[XAXIS][0];
		axisRanges[XAXIS][MAX] = minMax[XAXIS][1];
		axisRanges[YAXIS][MIN] = minMax[YAXIS][0];
		axisRanges[YAXIS][MAX] = minMax[YAXIS][1];
		axisRanges[ZAXIS][MIN] = minMax[ZAXIS][0];
		axisRanges[ZAXIS][MAX] = minMax[ZAXIS][1];
	}

	public void setFirstLast(float[][] firstLast)
	{
		// TODO
	}

	public void setLabelSize(float size)
	{
		this.labelSize = size;
	}

	public void setValueMinMax(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax)
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
	}

	public void setMinMaxLineWidth(float lineWidth)
	{
		this.minMaxLineWidth = lineWidth;
	}

	public void setGridLineWidth(float lineWidth)
	{
		this.gridLineWidth = lineWidth;
	}

}