package tools.SurfaceChart;

import java.util.Random;

import tools.SurfaceChart.Surface.BarGrid;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class BarSurface
{
	private ModelBatch modelBatch;

	float trans;
	private float barSize;
	
	private ModelInstance gridXAxisInstances1[];
	private ModelInstance gridYAxisInstances1[];
	
	private ModelInstance gridXAxisInstances2[];
	private ModelInstance gridYAxisInstances2[];
	
	private ModelInstance gridXAxisInstances3[];
	private ModelInstance gridYAxisInstances3[];
	
	private ModelInstance gridXAxisInstances4[];
	private ModelInstance gridYAxisInstances4[];
			
	private ModelInstance gridXAxisTickInstances1[];
	private Decal gridXAxisTickValuesInstances1[];
	private ModelInstance gridXAxisTickInstances2[];
	private Decal gridXAxisTickValuesInstances2[];
	private Decal gridXAxisName[];
	
	private ModelInstance gridYAxisTickInstances1[];
	private Decal gridYAxisTickValuesInstances1[];
	private ModelInstance gridYAxisTickInstances2[];
	private Decal gridYAxisTickValuesInstances2[];
	private Decal gridYAxisName[];
	
	private int gridSteps;
	private float gridSize = 1000;

	private HueColorPallete pallete;

	private int columns;
	
	private DecalBatch db;
	private BitmapFont axisFont;
	
	private BarGrid grid;
	
	private Random r = new Random();
	
	// Data Limits (min/max)
	float xAxisMin;
	float xAxisMax;
	float yAxisMin;
	float yAxisMax;
	float zAxisMin;
	float zAxisMax;
		
	public BarSurface(Camera cam, int samples, HueColorPallete pallete)
	{		
		modelBatch = new ModelBatch();
		
		// Labels		
		db = new DecalBatch(new CameraGroupStrategy(cam));
		axisFont = new BitmapFont();
		
		this.columns = (int) Math.sqrt(samples);
		this.barSize = gridSize/columns;

		// 0 + 1 for top/bottom lines
		this.gridSteps = 11;
		
		this.pallete = pallete;
		
		
		gridXAxisInstances1 = new ModelInstance[gridSteps];
		gridYAxisInstances1 = new ModelInstance[gridSteps];
		
		gridXAxisInstances2 = new ModelInstance[gridSteps];
		gridYAxisInstances2 = new ModelInstance[gridSteps];
		
		gridXAxisInstances3 = new ModelInstance[gridSteps];
		gridYAxisInstances3 = new ModelInstance[gridSteps];
		
		gridXAxisInstances4 = new ModelInstance[gridSteps];
		gridYAxisInstances4 = new ModelInstance[gridSteps];
		
		gridXAxisTickInstances1 = new ModelInstance[gridSteps];
		gridXAxisTickValuesInstances1 = new Decal[gridSteps];
		gridXAxisTickInstances2 = new ModelInstance[gridSteps];
		
		gridXAxisTickValuesInstances2 = new Decal[gridSteps];
		
		gridXAxisName = new Decal[2];
		
		gridYAxisTickInstances1 = new ModelInstance[gridSteps];
		gridYAxisTickValuesInstances1 = new Decal[gridSteps];
		
		gridYAxisTickInstances2 = new ModelInstance[gridSteps];
		gridYAxisTickValuesInstances2 = new Decal[gridSteps];
		
		gridYAxisName = new Decal[2];
		
		grid = new BarGrid(gridSize,samples,pallete);
				
		setData(cam,50,100,50,200,null);
	}
	
	public void setData(Camera cam, float xAxisMin,float xAxisMax,float yAxisMin,float yAxisMax,float data[][])
	{
		// Data X/Y Axis Limits
		this.xAxisMin = xAxisMin;
		this.xAxisMax = xAxisMax;
		this.yAxisMin = yAxisMin;
		this.yAxisMax = yAxisMax;
		
		// Pillars
		ModelBuilder modelBuilder = new ModelBuilder();

		float max = columns*columns;
		//float max = barSize*columns/2;
		trans = columns*barSize/2;

		float height = 0;
		
		int c=0;
		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				height = r.nextFloat()*(100f);
				//height = 0.1f;
				//height= (((x+1)*(y+1))/max)*100f;
				if(height==0)
				{
					height=0.001f;
				}

				grid.setBarHeight(x, y, height);
				//pillars[y][x].dispose();
			}
		}
		
		grid.setBarHeight(0, 0, 0f);
		
		float axisHeight = barSize*columns/2;
		Model[] gridLines = new Model[gridSteps];
		float xInterval = axisHeight/(gridSteps-1);
		float yInterval = gridSize/(gridSteps-1);
		
		float lineSize = 3f;
		float axisPad = 1f;
		
		// X1
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);
			}
			
			gridXAxisInstances1[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances1[g].transform.rotate(1, 0, 0, 90);
			gridXAxisInstances1[g].transform.trn(0,-trans-axisPad,(g*xInterval));
			
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);

			}

			gridYAxisInstances1[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances1[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances1[g].transform.trn((g*yInterval)-trans,-trans-axisPad,axisHeight/2);	
		}
				
		// X2
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisInstances2[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances2[g].transform.rotate(1, 0, 0, 90);
			gridXAxisInstances2[g].transform.trn(0,trans+axisPad,(g*xInterval));
			
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);

			}
			
			gridYAxisInstances2[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances2[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances2[g].transform.trn((g*yInterval)-trans,trans+axisPad,axisHeight/2);	
		}
		
		// X3
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisInstances3[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances3[g].transform.rotate(0, 0, 1, 90);
			gridXAxisInstances3[g].transform.trn(trans+axisPad,0,(g*xInterval));
			
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			
			gridYAxisInstances3[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances3[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances3[g].transform.trn(trans+axisPad,(g*yInterval)-trans,axisHeight/2);
		}
		
		// X4
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisInstances4[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances4[g].transform.rotate(0, 0, 1, 90);
			gridXAxisInstances4[g].transform.trn(-trans+axisPad,0,(g*xInterval));
			
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,axisHeight, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			
			gridYAxisInstances4[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances4[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances4[g].transform.trn(-trans+axisPad,(g*yInterval)-trans,axisHeight/2);
		}
		
		// Axis Labels and Ticks
		
		float zInterval = gridSize/(gridSteps-1);
		float tickSize = gridSize*0.05f;
		
		// ZX Tick 1
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisTickInstances1[g] = new ModelInstance(gridLines[g]);	
			//gridZXAxisInstances1[g].transform.rotate(0, 0, 1, 90);
			//gridZXAxisInstances1[g].transform.trn(-trans+axisPad,0,(g*zInterval));
			gridXAxisTickInstances1[g].transform.trn(gridSize/2+tickSize/2,(g*zInterval)-trans,0);
		}
		
		// ZX Tick 2
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisTickInstances2[g] = new ModelInstance(gridLines[g]);	
			//gridZXAxisInstances1[g].transform.rotate(0, 0, 1, 90);
			//gridZXAxisInstances1[g].transform.trn(-trans+axisPad,0,(g*zInterval));
			gridXAxisTickInstances2[g].transform.trn(-gridSize/2-tickSize/2,(g*zInterval)-trans,0);
		}
		
		// ZY Tick 1
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}				
			gridYAxisTickInstances1[g] = new ModelInstance(gridLines[g]);	
			gridYAxisTickInstances1[g].transform.rotate(0, 0, 1, 90);
			//gridZXAxisInstances1[g].transform.trn(-trans+axisPad,0,(g*zInterval));
			gridYAxisTickInstances1[g].transform.trn((g*zInterval)-trans,gridSize/2+tickSize/2,0);
		}
		
		// ZY Tick 2
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(tickSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}				
			gridYAxisTickInstances2[g] = new ModelInstance(gridLines[g]);	
			gridYAxisTickInstances2[g].transform.rotate(0, 0, 1, 90);
			//gridZXAxisInstances1[g].transform.trn(-trans+axisPad,0,(g*zInterval));
			gridYAxisTickInstances2[g].transform.trn((g*zInterval)-trans,-gridSize/2-tickSize/2,0);
		}
		
		// TickValues
		float xMin=xAxisMin;
		float xMax=xAxisMax;
		float xTickValueIncr = (xMax-xMin)/(gridSteps-1);
		
		// tick value - reused for YAxis
		float value = xMin;

		for(int xtl=0;xtl<gridSteps;xtl++)
		{
			String svalue = String.valueOf(value);
			int tWidth = (int) axisFont.getBounds(svalue).width;
			int tHeight = (int) axisFont.getBounds(svalue).height+2;
			
			FrameBuffer fbo = new FrameBuffer(Format.RGBA8888,tWidth,tHeight, false);

			Matrix4 pm = new Matrix4();
			pm.setToOrtho2D(0, 0, tWidth, tHeight);
			
			axisFont.setColor(Color.BLACK);
			//axisFont.scale(10f);
			SpriteBatch sb = new SpriteBatch();
			sb.setProjectionMatrix(pm);
				fbo.begin();
					sb.begin();
						axisFont.draw(sb,svalue,0,tHeight);		
					sb.end();
				fbo.end();		
			
			TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(),0,0,tWidth,tHeight);
			t1.flip(false, true);
			
			gridXAxisTickValuesInstances1[xtl] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridXAxisTickValuesInstances1[xtl].getPosition().set(gridSize/2+tickSize*2, (xtl*zInterval)-trans, 0);
			gridXAxisTickValuesInstances1[xtl].setRotationX(90);
			
			gridXAxisTickValuesInstances2[xtl] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridXAxisTickValuesInstances2[xtl].getPosition().set(-gridSize/2-tickSize*2, (xtl*zInterval)-trans, 0);
			gridXAxisTickValuesInstances2[xtl].setRotationX(90);
			
			value+=xTickValueIncr;			
		}
		
		// TickValues
		float yMin=yAxisMin;
		float yMax=yAxisMax;
		float yTickValueIncr = (yMax-yMin)/(gridSteps-1);		
		
		value = yMin;
		
		for(int ytl=0;ytl<gridSteps;ytl++)
		{
			//float value = yTickValueIncr*ytl;
			String svalue = String.valueOf(value);
			int tWidth = (int) axisFont.getBounds(svalue).width;
			int tHeight = (int) axisFont.getBounds(svalue).height+2;
			
			FrameBuffer fbo = new FrameBuffer(Format.RGBA8888,tWidth,tHeight, false);

			Matrix4 pm = new Matrix4();
			pm.setToOrtho2D(0, 0, tWidth, tHeight);
			
			axisFont.setColor(Color.BLACK);
			//axisFont.scale(10f);
			SpriteBatch sb = new SpriteBatch();
			sb.setProjectionMatrix(pm);
				fbo.begin();
					sb.begin();
						axisFont.draw(sb,svalue,0,tHeight);		
					sb.end();
				fbo.end();		
			
			TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(),0,0,tWidth,tHeight);
			t1.flip(false, true);
			
			gridYAxisTickValuesInstances1[ytl] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridYAxisTickValuesInstances1[ytl].getPosition().set((ytl*zInterval)-trans,gridSize/2+tickSize*2, 0);
			gridYAxisTickValuesInstances1[ytl].setRotationX(90);
			
			gridYAxisTickValuesInstances2[ytl] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridYAxisTickValuesInstances2[ytl].getPosition().set((ytl*zInterval)-trans,-gridSize/2-tickSize*2, 0);
			gridYAxisTickValuesInstances2[ytl].setRotationX(90);
			
			value+=yTickValueIncr;			
		}
				
		// X Axis Name
		{
			String svalue = "X AxisName";
			int tWidth = (int) axisFont.getBounds(svalue).width;
			int tHeight = (int) axisFont.getBounds(svalue).height+2;
			int tMax = Math.max(tWidth, tHeight);
			
			FrameBuffer fbo = new FrameBuffer(Format.RGBA8888,tWidth,tHeight, false);
			
			Matrix4 pm = new Matrix4();
			pm.setToOrtho2D(0, 0, tWidth, tHeight);
			
			axisFont.setColor(Color.BLACK);
			//axisFont.scale(10f);
			SpriteBatch sb = new SpriteBatch();
			sb.setProjectionMatrix(pm);
				fbo.begin();
					sb.begin();
						axisFont.draw(sb,svalue,0,tHeight);		
					sb.end();
				fbo.end();		
			
			TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(),0,0,tWidth,tHeight);
			t1.flip(false, true);
			
			gridXAxisName[0] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridXAxisName[0].getPosition().set(gridSize/2+tMax*3, gridSize/2-trans, 0);
			gridXAxisName[0].setRotationX(90);
			
			gridXAxisName[1] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridXAxisName[1].getPosition().set(-gridSize/2-tMax*3, gridSize/2-trans, 0);
			gridXAxisName[1].setRotationX(90);
		}
		
		// Y Axis Name
		{
			String svalue = "Y AxisName";
			int tWidth = (int) axisFont.getBounds(svalue).width;
			int tHeight = (int) axisFont.getBounds(svalue).height+2;
			int tMax = Math.max(tWidth, tHeight);
			
			FrameBuffer fbo = new FrameBuffer(Format.RGBA8888,tWidth,tHeight, false);
			
			Matrix4 pm = new Matrix4();
			pm.setToOrtho2D(0, 0, tWidth, tHeight);
			
			axisFont.setColor(Color.BLACK);
			//axisFont.scale(10f);
			SpriteBatch sb = new SpriteBatch();
			sb.setProjectionMatrix(pm);
				fbo.begin();
					sb.begin();
						axisFont.draw(sb,svalue,0,tHeight);		
					sb.end();
				fbo.end();
			
			TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(),0,0,tWidth,tHeight);
			t1.flip(false, true);
			
			gridYAxisName[0] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridYAxisName[0].getPosition().set(gridSize/2-trans,gridSize/2+tMax*3, 0);
			gridYAxisName[0].setRotationX(90);
			
			gridYAxisName[1] = Decal.newDecal(tWidth*2, tHeight*2, t1, true);
			gridYAxisName[1].getPosition().set(gridSize/2-trans,-gridSize/2-tMax*3, 0);
			gridYAxisName[1].setRotationX(90);			
		}
		
	}
	
	public float getSize()
	{
		return columns * barSize;
	}
	
	public void render(Camera cam,Environment environment)
	{
		/*float max = barSize*columns/2;		

		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				if(r.nextInt(100) % 100 == 0)
				{
					//float height = grid.getBarHeight(x, y);
					
					//height = (height+1f)%100f;
					
					grid.setBarHeight(x, y, (100f/max)*r.nextFloat()*max);
					
					//grid.setBarHeight(x, y, height);
				}
			}
		}*/
		
		modelBatch.begin(cam);
		
		grid.render(modelBatch, cam, environment);
				
		Vector3 pos1 = new Vector3();
		gridXAxisInstances1[gridSteps/2].transform.getTranslation(pos1);
		
		Vector3 pos2 = new Vector3();
		gridXAxisInstances2[gridSteps/2].transform.getTranslation(pos2);
		
		Vector3 pos3 = new Vector3();
		gridXAxisInstances3[gridSteps/2].transform.getTranslation(pos3);
		
		Vector3 pos4 = new Vector3();
		gridXAxisInstances4[gridSteps/2].transform.getTranslation(pos4);
				
		float east = cam.position.dst2(pos1);
		float west = cam.position.dst2(pos2);
		float north = cam.position.dst2(pos3);
		float south = cam.position.dst2(pos4);
		
		if(north>south)
		{
			drawGridInstances(gridXAxisInstances3,cam,environment);
			drawGridInstances(gridYAxisInstances3,cam,environment);
			drawGridInstances(gridXAxisTickInstances2,cam,environment);
			for(Decal d:gridXAxisTickValuesInstances2)
			{
				d.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(d);
			}
			gridXAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridXAxisName[1]);
		}
		else
		{
			drawGridInstances(gridXAxisInstances4,cam,environment);
			drawGridInstances(gridYAxisInstances4,cam,environment);
			drawGridInstances(gridXAxisTickInstances1,cam,environment);
			for(Decal d:gridXAxisTickValuesInstances1)
			{
				d.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(d);
			}
			gridXAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridXAxisName[0]);
		}
		
		if(east>west)
		{
			drawGridInstances(gridXAxisInstances1,cam,environment);
			drawGridInstances(gridYAxisInstances1,cam,environment);
			drawGridInstances(gridYAxisTickInstances1,cam,environment);
			for(Decal d:gridYAxisTickValuesInstances1)
			{
				d.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(d);
			}
			gridYAxisName[0].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridYAxisName[0]);
		}
		else
		{
			drawGridInstances(gridXAxisInstances2,cam,environment);
			drawGridInstances(gridYAxisInstances2,cam,environment);
			drawGridInstances(gridYAxisTickInstances2,cam,environment);
			for(Decal d:gridYAxisTickValuesInstances2)
			{
				d.lookAt(cam.position.cpy(), cam.up.cpy().nor());
				db.add(d);
			}
			gridYAxisName[1].lookAt(cam.position.cpy(), cam.up.cpy().nor());
			db.add(gridYAxisName[1]);
		}
				
		modelBatch.end();
				
		db.flush();
	}
	
	public void drawGridInstances(ModelInstance instances[], Camera cam,Environment environment)
	{
		for(int i=0;i<instances.length;i++)
		{
			modelBatch.render(instances[i], environment);		
		}
	}
	
	public void dispose()
	{

	}
	
	public float getXmin()
	{
		return xAxisMin;
	}
	
	public float getXmax()
	{
		return xAxisMax;
	}
	
	public float getYmin()
	{
		return yAxisMin;
	}
	
	public float getYmax()
	{
		return yAxisMax;
	}
	
	public float getZmin()
	{
		return zAxisMin;
	}
	
	public float getZmax()
	{
		return zAxisMax;
	}
	
}
