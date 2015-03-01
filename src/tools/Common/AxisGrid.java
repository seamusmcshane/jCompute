package tools.Common;

import tools.MeshHelper;
import tools.PhasePlot3d.LineGrid;
import tools.PhasePlot3d.VertexModel;

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

public class AxisGrid
{
	// Floor Grid
	private LineGrid	floor;

	// Floor Ticks
	private VertexModel	floorNorthTicks;
	private VertexModel	floorSouthTicks;
	private VertexModel	floorEastTicks;
	private VertexModel	floorWestTicks;
	
	// Floor Axis Labels
	private Decal		gridXAxisName[];
	private Decal		gridYAxisName[];
	private Decal		gridZAxisName[];

	// Wall Grid
	private LineGrid	eastSide;
	private LineGrid	westSide;
	private LineGrid	northSide;
	private LineGrid	southSide;

	// Wall Axis Ticks
	private VertexModel wallNorthEastTicks;
	private VertexModel wallNorthWestTicks;
	private VertexModel wallSouthEastTicks;
	private VertexModel wallSouthWestTicks;

	private BitmapFont	decalFont	= new BitmapFont();

	public AxisGrid(float gridSize, float trans, float heightScale)
	{
		float offset1 = gridSize - (gridSize * heightScale);
		float tickSize = gridSize / 10f;

		// Grid Walls (height is scaled)
		northSide = new LineGrid(10, gridSize, new float[]
		{
				1, 0, 0, 1
		}, trans);
		northSide.scale(1f, 1f, heightScale);
		northSide.rotate(1, 0, 0, 90);
		northSide.rotate(0, 1, 0, 90);
		northSide.transform(gridSize, 0, -offset1 / 2);

		southSide = new LineGrid(10, gridSize, new float[]
		{
				0, 0, 1, 1
		}, trans);
		southSide.scale(1f, 1f, heightScale);
		southSide.rotate(1, 0, 0, 90);
		southSide.rotate(0, 1, 0, 90);
		southSide.transform(0, 0, -offset1 / 2);

		eastSide = new LineGrid(10, gridSize, new float[]
		{
				0, 1, 0, 1
		}, trans);
		eastSide.scale(1f, 1f, heightScale);
		eastSide.rotate(1, 0, 0, 90);
		eastSide.transform(0, -gridSize, -offset1 / 2);

		westSide = new LineGrid(10, gridSize, new float[]
		{
				0, 1, 1, 1
		}, trans);
		westSide.scale(1f, 1f, heightScale);
		westSide.rotate(1, 0, 0, 90);
		westSide.transform(0, 0, -offset1 / 2);

		// Floor
		floor = new LineGrid(10, gridSize, new float[]
		{
				0, 0, 0, 1
		}, trans);

		// Floor Axis Ticks
		floorNorthTicks = new VertexModel(false);
		generateTicks(floorNorthTicks, gridSize, tickSize, 10, 10);
		floorNorthTicks.getModelInstance().transform.trn(gridSize / 2, -gridSize / 2, -gridSize / 2+trans);
		
		floorSouthTicks = new VertexModel(false);
		generateTicks(floorSouthTicks, gridSize, gridSize / 10, 10, 10);
		floorSouthTicks.getModelInstance().transform.trn(-gridSize / 2-tickSize, -gridSize / 2, -gridSize / 2+trans);
		
		floorEastTicks = new VertexModel(false);
		generateTicks(floorEastTicks, gridSize, tickSize, 10, 10);
		floorEastTicks.getModelInstance().transform.trn(gridSize / 2, -gridSize / 2-tickSize, -gridSize / 2+trans);
		floorEastTicks.getModelInstance().transform.rotate(0, 0, 1, 90f);
		
		floorWestTicks = new VertexModel(false);
		generateTicks(floorWestTicks, gridSize, tickSize, 10, 10);
		floorWestTicks.getModelInstance().transform.trn(gridSize / 2, gridSize / 2, -gridSize / 2+trans);
		floorWestTicks.getModelInstance().transform.rotate(0, 0, 1, 90f);

		// Wall Ticks
		wallNorthEastTicks = new VertexModel(false);
		generateTicks(wallNorthEastTicks, gridSize, tickSize, 10, 10);
		wallNorthEastTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallNorthEastTicks.getModelInstance().transform.trn((-gridSize / 2), (-gridSize / 2), -gridSize / 2+trans);
		wallNorthEastTicks.getModelInstance().transform.rotate(0, 0, 1, 225f);
		wallNorthEastTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);

		wallNorthWestTicks = new VertexModel(false);
		generateTicks(wallNorthWestTicks, gridSize, tickSize, 10, 10);
		wallNorthWestTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallNorthWestTicks.getModelInstance().transform.trn((gridSize / 2), (gridSize / 2), -gridSize / 2+trans);
		wallNorthWestTicks.getModelInstance().transform.rotate(0, 0, 1, 45f);
		wallNorthWestTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);
		
		wallSouthEastTicks = new VertexModel(false);
		generateTicks(wallSouthEastTicks, gridSize, tickSize, 10, 10);
		wallSouthEastTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallSouthEastTicks.getModelInstance().transform.trn((-gridSize / 2), (gridSize / 2), -gridSize / 2+trans);
		wallSouthEastTicks.getModelInstance().transform.rotate(0, 0, 1, 135f);
		wallSouthEastTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);
		
		wallSouthWestTicks = new VertexModel(false);
		generateTicks(wallSouthWestTicks, gridSize, tickSize, 10, 10);
		wallSouthWestTicks.getModelInstance().transform.scale(1f, 1f, heightScale);
		wallSouthWestTicks.getModelInstance().transform.trn((gridSize / 2), -(gridSize / 2), -gridSize / 2+trans);
		wallSouthWestTicks.getModelInstance().transform.rotate(0, 0, 1, 315f);
		wallSouthWestTicks.getModelInstance().transform.rotate(1, 0, 0, 90f);		
		
		// Floor Tick Labels
		gridXAxisName = new Decal[2];
		gridYAxisName = new Decal[2];

		// North + South / East + West
		gridZAxisName = new Decal[4];

		// Axis Labels
		setAxisLabels(gridSize, tickSize, trans, "X Axis", "Y Axis", "Z Axis");
	}

	private void setAxisLabels(float gridSize, float tickSize, float trans, String xAxis, String yAxis, String zAxis)
	{
		// X Axis Labels
		gridXAxisName[0] = generateDecal(xAxis);
		gridXAxisName[0].getPosition().set(gridSize / 2 + (tickSize * 2), gridSize / 2 - trans, 0);
		gridXAxisName[0].setRotationX(90);

		gridXAxisName[1] = generateDecal(xAxis);
		gridXAxisName[1].getPosition().set(-gridSize / 2 - (tickSize * 2), gridSize / 2 - trans, 0);
		gridXAxisName[1].setRotationX(90);

		// Y Axis Labels
		gridYAxisName[0] = generateDecal(yAxis);
		gridYAxisName[0].getPosition().set(gridSize / 2 - trans, gridSize / 2 + (tickSize * 2), 0);
		gridYAxisName[0].setRotationX(90);

		gridYAxisName[1] = generateDecal(yAxis);
		gridYAxisName[1].getPosition().set(gridSize / 2 - trans, -gridSize / 2 - (tickSize * 2), 0);
		gridYAxisName[1].setRotationX(90);

		// Z Axis Column Labels (NorthEast,SouthEast,SouthWest,NorthWest)
		gridZAxisName[0] = generateDecal(zAxis + "NorthEast");
		gridZAxisName[0].getPosition().set(gridSize / 2 + (tickSize * 2), -gridSize / 2 - (tickSize * 2), gridSize / 2);
		gridZAxisName[0].setRotationX(90);
		
		gridZAxisName[1] = generateDecal(zAxis + "SouthEast");
		gridZAxisName[1].getPosition()
				.set(-gridSize / 2 - (tickSize * 2), -gridSize / 2 - (tickSize * 2), gridSize / 2);
		gridZAxisName[1].setRotationX(90);
				
		gridZAxisName[2] = generateDecal(zAxis + "SouthWest");
		gridZAxisName[2].getPosition().set(-gridSize / 2 - (tickSize * 2), gridSize / 2 + (tickSize * 2), gridSize / 2);
		gridZAxisName[2].setRotationX(90);

		gridZAxisName[3] = generateDecal(zAxis + "NorthWest");
		gridZAxisName[3].getPosition()
				.set(+gridSize / 2 + (tickSize * 2), +gridSize / 2 + (tickSize * 2), gridSize / 2);
		gridZAxisName[3].setRotationX(90);
	}

	private Decal generateDecal(String decalText)
	{
		String svalue = decalText;
		int tWidth = (int) (decalFont.getBounds(svalue).width+2);
		int tHeight = (int) (decalFont.getBounds(svalue).height + 2);
		
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, tWidth, tHeight, false);

		Matrix4 pm = new Matrix4();
		pm.setToOrtho2D(0, 0, tWidth, tHeight);

		decalFont.setColor(Color.BLACK);
		//decalFont.scale(0.1f);
		SpriteBatch sb = new SpriteBatch();
		sb.setProjectionMatrix(pm);
		fbo.begin();
		sb.begin();
		decalFont.draw(sb, svalue, 0, tHeight);
		sb.end();
		fbo.end();

		TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, tWidth, tHeight);
		t1.flip(false, true);

		return Decal.newDecal(tWidth * 2, tHeight * 2, t1, true);
	}

	public void generateTicks(VertexModel vModel, float gridSize, float tickSize, int div, int numTicks)
	{
		float start = 0;
		float end = tickSize;

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
	}

	public void render(Camera cam, ModelBatch modelBatch, DecalBatch db, Environment environment)
	{
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
		
		if(northVis)
		{	modelBatch.render(northSide.getModelInstance(), environment);
			
			// South Ticks
			modelBatch.render(floorSouthTicks.getModelInstance());
			gridXAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridXAxisName[1]);
		}

		if(southVis)
		{
			modelBatch.render(southSide.getModelInstance(), environment);
			
			// North Ticks
			modelBatch.render(floorNorthTicks.getModelInstance());
			gridXAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridXAxisName[0]);
		}

		if(eastVis)
		{
			modelBatch.render(eastSide.getModelInstance(), environment);
			
			// West Tick
			modelBatch.render(floorWestTicks.getModelInstance());
			gridYAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridYAxisName[0]);
		}

		if(westVis)
		{
			modelBatch.render(westSide.getModelInstance(), environment);
			
			// East Ticks
			modelBatch.render(floorEastTicks.getModelInstance());
			gridYAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridYAxisName[1]);
		}
		
		// NorthWest ZColumn
		if(southVis ^ eastVis)
		{
			gridZAxisName[3].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridZAxisName[3]);
			modelBatch.render(wallNorthWestTicks.getModelInstance(), environment);	
		}

		// NorthEast ZColumn
		if(southVis ^ westVis)
		{
			gridZAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridZAxisName[0]);
			modelBatch.render(wallSouthWestTicks.getModelInstance(), environment);
		}

		// SouthEast ZColumn	
		if(northVis ^ westVis)
		{
			gridZAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridZAxisName[1]);
			modelBatch.render(wallNorthEastTicks.getModelInstance(), environment);
		}
		
		// SouthWest ZColumn
		if(northVis ^ eastVis)
		{
			gridZAxisName[2].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridZAxisName[2]);
			modelBatch.render(wallSouthEastTicks.getModelInstance(), environment);
		}
		
		// Floor always rendered
		modelBatch.render(floor.getModelInstance(), environment);
	}

}