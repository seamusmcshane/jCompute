package tools.SurfaceChart;

import java.util.Random;

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
	
	private ModelInstance surfaceBarsInstances[][];
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
	
	private int gridSteps;
	private float gridSize = 1000;

	private HueColorPallete pallete;

	private int columns;
	
	private DecalBatch db;
	private BitmapFont axisFont;
	
	public BarSurface(Camera cam, int samples, HueColorPallete pallete)
	{		
		modelBatch = new ModelBatch();
		
		// Labels		
		db = new DecalBatch(new CameraGroupStrategy(cam));
		axisFont = new BitmapFont();
		
		this.columns = (int) Math.sqrt(samples);
		
		this.gridSteps = 11;
		
		this.barSize = gridSize/columns;

		this.pallete = pallete;
		
		surfaceBarsInstances = new ModelInstance[columns][columns];
		
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

		setData(cam,null);
	}
	
	public void setData(Camera cam, float data[][])
	{
		// Pillars
		ModelBuilder modelBuilder = new ModelBuilder();
		
		Random r = new Random();

		float max = barSize*columns/2;
		//float max = barSize*columns/2;
		float crange = pallete.getColorCount()/max;
		trans = columns*barSize/2;

		Model[][] pillars = new Model[columns][columns];		

		float height = 0;
		
		int c=0;
		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				height = r.nextFloat()*max;
				//height= (columns*columns/max)*x*y;
				if(height==0)
				{
					height=0.001f;
				}
//				height = (height+5)%max;
				c = (int) (crange * height);
				//c=(c+1)%colorsVal;
				pillars[y][x] = modelBuilder.createBox(barSize,height, barSize,new Material(ColorAttribute.createDiffuse(pallete.getColor(c))),	Usage.Position | Usage.Normal);
				surfaceBarsInstances[y][x] = new ModelInstance(pillars[y][x]);
				surfaceBarsInstances[y][x].transform.rotate(1, 0, 0, 90);
				surfaceBarsInstances[y][x].transform.trn((y*barSize)-trans+(barSize/2), (x*barSize)-trans+(barSize/2), ((height/2)));
			}
		}
		
		Model[] gridLines = new Model[gridSteps];
		float xInterval = max/(gridSteps-1);
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
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);
			}
			
			gridXAxisInstances1[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances1[g].transform.rotate(1, 0, 0, 90);
			gridXAxisInstances1[g].transform.trn(0,-trans-axisPad,(g*xInterval));			
		}
		
		// Y1
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLACK)),	Usage.Position | Usage.Normal);

			}

			gridYAxisInstances1[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances1[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances1[g].transform.trn((g*yInterval)-trans,-trans-axisPad,max/2);			
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
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLUE)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisInstances2[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances2[g].transform.rotate(1, 0, 0, 90);
			gridXAxisInstances2[g].transform.trn(0,trans+axisPad,(g*xInterval));
		}
		
		// Y2
		for(int g=0;g<gridSteps;g++)
		{

			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.BLUE)),	Usage.Position | Usage.Normal);

			}
			
			gridYAxisInstances2[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances2[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances2[g].transform.trn((g*yInterval)-trans,trans+axisPad,max/2);			
		}
		
		// X3
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.ORANGE)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisInstances3[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances3[g].transform.rotate(0, 0, 1, 90);
			gridXAxisInstances3[g].transform.trn(trans+axisPad,0,(g*xInterval));
		}
		
		// Y3
		for(int g=0;g<gridSteps;g++)
		{

			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.ORANGE)),	Usage.Position | Usage.Normal);

			}
			
			gridYAxisInstances3[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances3[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances3[g].transform.trn(trans+axisPad,(g*yInterval)-trans,max/2);
		}
		
		// X4
		for(int g=0;g<gridSteps;g++)
		{
			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.RED)),	Usage.Position | Usage.Normal);
			}
			else
			{
				gridLines[g] = modelBuilder.createBox(gridSize,lineSize, axisPad,new Material(ColorAttribute.createDiffuse(Color.CYAN)),	Usage.Position | Usage.Normal);
			}				
			gridXAxisInstances4[g] = new ModelInstance(gridLines[g]);	
			gridXAxisInstances4[g].transform.rotate(0, 0, 1, 90);
			gridXAxisInstances4[g].transform.trn(-trans+axisPad,0,(g*xInterval));
		}
		
		// Y4
		for(int g=0;g<gridSteps;g++)
		{

			if(g==0)
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.GREEN)),	Usage.Position | Usage.Normal);

			}
			else
			{
				gridLines[g] = modelBuilder.createBox(lineSize,max, axisPad,new Material(ColorAttribute.createDiffuse(Color.CYAN)),	Usage.Position | Usage.Normal);

			}
			
			gridYAxisInstances4[g] = new ModelInstance(gridLines[g]);	
			gridYAxisInstances4[g].transform.rotate(1, 0, 0, 90);
			gridYAxisInstances4[g].transform.trn(-trans+axisPad,(g*yInterval)-trans,max/2);
		}
		
		float zInterval = gridSize/(gridSteps-1);
		float tickSize = gridSize*0.05f;
		
		// Z Tick 1
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
		
		// Z Tick 1
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
				
		// TickValues
		float xMin=0;
		float xMax=100;
		float xTickValueIncr = xMax-xMin/(gridSteps-1);		
		
		for(int xtl=0;xtl<gridSteps;xtl++)
		{
			float value = xTickValueIncr*xtl;
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
		
		
	}	
	
	public float getSize()
	{
		return columns * barSize;
	}
	
	public void render(Camera cam,Environment environment)
	{		
		modelBatch.begin(cam);
		
		drawSurfaceBars(cam,environment);
		
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
		
		if(east>west)
		{
			drawGridInstances(gridXAxisInstances1,cam,environment);
			drawGridInstances(gridYAxisInstances1,cam,environment);
		}
		else
		{
			drawGridInstances(gridXAxisInstances2,cam,environment);
			drawGridInstances(gridYAxisInstances2,cam,environment);
		}
		
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
		
		
		modelBatch.end();

		
/*
		// Pos
		Vector3 position = new Vector3(0,0,0);
		
		// Rotation
		Matrix4 mx4Font = new Matrix4();
		//mx4Font.setTranslation(new Vector3(120,120,0));
		//mx4Font.setToRotation(new Vector3(1,0,0),90);
		mx4Font.setToRotation(new Vector3(0,0,1), -180);
		
		Vector3 viewingDirection = new Vector3(cam.position.cpy().sub(position).nor());

		mx4Font.setToLookAt(viewingDirection, cam.up.cpy());

		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.setTransformMatrix(mx4Font);
		spriteBatch.begin();
		spriteBatch.enableBlending();
		
		axisFont.setColor(Color.BLACK);
		axisFont.setScale(10f);
		axisFont.draw(spriteBatch, "TEST 123456789",position.x,position.y);		
		spriteBatch.end();		*/
		
		

		

		
		db.flush();
	}

	
	public void drawSurfaceBars(Camera cam,Environment environment)
	{

		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				modelBatch.render(surfaceBarsInstances[y][x], environment);
			}
		}
		
	}
	
	public void drawGridInstances(ModelInstance instances[], Camera cam,Environment environment)
	{
		for(int i=0;i<instances.length;i++)
		{
			modelBatch.render(instances[i], environment);		
		}
	}
	
	/*public void drawGrid(Camera cam,float x,float y, float z,float axisX, float axisY, float axisZ, float angle)
	{
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.rotate(axisX, axisY, axisZ, angle);
		shapeRenderer.translate(x, y, z);
		shapeRenderer.setColor(0, 0, 0, 1);
		shapeRenderer.begin(ShapeType.Line);
		for(int yl=0;yl<=gridSize;yl++)
		{
			if(yl%10==0)
			{
				shapeRenderer.line(0-(gridSize/2)-barSize/2, yl-(gridSize/2)-barSize/2, (gridSize/2)-barSize/2, yl-(gridSize/2)-barSize/2);
			}

			for(int xl=0;xl<=gridSize;xl++)
			{
				if(xl%10==0)
				{
					shapeRenderer.line(xl-(gridSize/2)-barSize/2, 0-(gridSize/2)-barSize/2, xl-(gridSize/2)-barSize/2, (gridSize/2)-barSize/2);
				}
			}	
		}
		shapeRenderer.end();
		
	}*/
	
	public void dispose()
	{
		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				surfaceBarsInstances[y][x].model.dispose();
			}
		}
	}
	
}
