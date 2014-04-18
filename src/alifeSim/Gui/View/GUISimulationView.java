package alifeSim.Gui.View;

import java.awt.Canvas;
import java.util.concurrent.Semaphore;

import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;

import alifeSim.Simulation.Simulation;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSimGeom.A2DCircle;
import alifeSimGeom.A2DLine;
import alifeSimGeom.A2DRectangle;
import alifeSimGeom.A2DVector2f;
import alifeSimGeom.A2RGBA;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

public class GUISimulationView implements ApplicationListener, InputProcessor
{
	/** The Drawing Canvas */
	private LwjglAWTCanvas canvas;
	private int width;
	private int height;
	
	/** Cameara and Shape Renderer */
	private OrthographicCamera viewCam;
	
	/** Simulation Reference */
	private Simulation sim;
	private Semaphore viewLock = new Semaphore(1);

	private String simulationTitle = "";
	
	/** Records status of mouse button */
	private boolean mouseButtonPressed = false;
	
	private int defaultFrameRate = 15;

	/** Draw the View range of the agents */
	private boolean viewRangeDrawing = false;

	/** Draw Views */
	private boolean viewsDrawing = false;
		
	/* Mouse */
	/** Stores the mouse vector across updates */
	private A2DVector2f mousePos = new A2DVector2f(0, 0);
	
	private boolean button0Pressed;
	
	private BitmapFont font;

	private float defaultLineWidth = 0.10f;
		
	/* FBO / BBO */
	private SpriteBatch bboSpriteBatch;
	private ShapeRenderer bboShapeRenderer;
	
	private FrameBuffer fbo;
	private SpriteBatch fboSpriteBatch;
	private ShapeRenderer fboShapeRenderer;

	private SpriteBatch currentSpriteBatch;
	private ShapeRenderer currentShapeRenderer;
	
	public GUISimulationView()
	{
		System.out.println("Created Simulation View");
		
		LwjglApplicationConfiguration.disableAudio = true;
		
		canvas = new LwjglAWTCanvas(this, true);

		Display.setVSyncEnabled(true);
		Display.setSwapInterval(1);
		
		canvas.getInput().setInputProcessor(this);
		
		viewCam = new OrthographicCamera(640, 480);
		
		resetCamera();
		//viewCam.setToOrtho(true, 640, 480);
	}
	
	public void exitDisplay()
	{
		canvas.getInput().setInputProcessor(null);
		canvas.stop();		
		Display.destroy();
		
		System.out.println("Exited Simulation View");
	}
	
	public Canvas getAwtCanvas()
	{
		return canvas.getCanvas();
	}

	public void setSim(Simulation simIn)
	{
		//System.out.println("Simulation Set");
		viewLock.acquireUninterruptibly();
		sim = simIn;		
		viewLock.release();
	}
	
	@Override
	public void create()
	{		
		bboShapeRenderer = new ShapeRenderer();
		fboShapeRenderer = new ShapeRenderer();
        
		bboSpriteBatch = new SpriteBatch();
		fboSpriteBatch = new SpriteBatch();
		
		currentShapeRenderer = bboShapeRenderer;
		
		currentSpriteBatch = bboSpriteBatch;
        
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888,Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),false);
        
		viewCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

	}

	@Override
	public void dispose()
	{
       /* font.dispose();
		shapeRenderer.dispose();
		spriteBatch.dispose();*/
	}

	@Override
	public void pause()
	{

		
	}

	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	@Override
	public void render()
	{        
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		
		Display.sync(defaultFrameRate);
		
		Gdx.gl.glViewport(0, 0, width, height);
				
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
						        
		bboShapeRenderer.setProjectionMatrix(viewCam.combined);
		bboSpriteBatch.setProjectionMatrix(viewCam.combined);
		
		viewLock.acquireUninterruptibly();
		

		
		if(sim!=null)
		{
			if(sim.getSimManager()!=null)
			{
				viewCam.position.set(sim.getSimManager().getCamPos().getX(), sim.getSimManager().getCamPos().getY(),0);

				viewCam.zoom = sim.getSimManager().getCamZoom();
			}

			viewCam.update();
			
			sim.drawSim(this,viewRangeDrawing,viewsDrawing);
		}
		
		viewLock.release();

	}

	@Override
	public void resize(int width, int height)
	{		
		viewCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		resetCamera();
	}
	
	@Override
	public void resume()
	{
		
	} 

	/*
	 * Drawing Methods
	 * 
	 */			
	
	public void blankBBO()
	{
		
	}
	
	public void blankFBO()
	{
		fbo.begin();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		fbo.end();
	}
	
	public void targetFBOStart()
	{
		currentShapeRenderer = fboShapeRenderer;
		
		currentSpriteBatch = fboSpriteBatch;
		
		fbo.begin();
		
	}
	
	public void drawText(float x,float y,String text)
	{
		currentSpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		currentSpriteBatch.begin();
			font.setColor(Color.WHITE);
			font.draw(currentSpriteBatch, text, x, y);
		currentSpriteBatch.end();
	}
	
	public void drawText(float x,float y,String text,A2RGBA color)
	{
		currentSpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		currentSpriteBatch.begin();
			font.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
			font.draw(currentSpriteBatch, text, x, y);
		currentSpriteBatch.end();
	}
	
	public void targetFBOStop()
	{				
		currentShapeRenderer = bboShapeRenderer;
		
		currentSpriteBatch = bboSpriteBatch;
		
		fbo.end();
		
		//Sprite sprite = new Sprite(fbo.getColorBufferTexture());
				
		//Matrix4 projectionMatrix = new Matrix4();
		//projectionMatrix.setToOrtho2D(0, 0, sprite.getRegionWidth(), sprite.getRegionHeight());
		//spriteBatch.setProjectionMatrix(projectionMatrix);
		
		//currentShapeRenderer.setProjectionMatrix(projectionMatrix);
		//currentSpriteBatch.setProjectionMatrix(projectionMatrix);

		currentSpriteBatch.begin();

		
		currentSpriteBatch.draw(fbo.getColorBufferTexture(),0, 0);
		
       // sprite.setPosition(0, 0);
       // sprite.flip(false, true);
		
		//sprite.flip(false, false);
        //sprite.draw(currentSpriteBatch);
		
        currentSpriteBatch.end();
        
	}
	
	public void drawPixMap(Pixmap pixmap, float x, float y)
	{
		Texture texture = new Texture(pixmap);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		Sprite sprite = new Sprite(texture);
		currentSpriteBatch.begin();
	        sprite.setPosition(x, y);
	        sprite.flip(false, true);
	        sprite.draw(currentSpriteBatch);
        currentSpriteBatch.end();
        
        texture.dispose();
	}
	
	public void drawCircle(A2DCircle circle,A2RGBA color)
	{	
		Gdx.gl20.glLineWidth(defaultLineWidth);
		
		drawCircle(circle,color, defaultLineWidth);
	}
	
	public void drawCircle(A2DCircle circle,A2RGBA color, float lineWidth)
	{	
		if(!viewCam.frustum.pointInFrustum(new Vector3(circle.getX(),circle.getY(),0)))
		{
			return;
		}
	
		Gdx.gl20.glLineWidth(lineWidth);
		
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.circle(circle.getX(),circle.getY(),circle.getRadius());
		currentShapeRenderer.end();
	}
	
	public void drawFilledCircle(A2DCircle circle,A2RGBA color)
	{
		if(!viewCam.frustum.pointInFrustum(new Vector3(circle.getX(),circle.getY(),0)))
		{
			return;
		}
		
		currentShapeRenderer.begin(ShapeType.Filled);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.circle(circle.getX(),circle.getY(),circle.getRadius());
		currentShapeRenderer.end();
	}
	

	public void drawLine(A2DVector2f pos1, A2DVector2f pos2, A2RGBA color, float lineWidth, boolean clipCheck)
	{
		drawLine(pos1.getX(),pos1.getY(), pos2.getX(), pos2.getY(),color,lineWidth,clipCheck);		
	}
	
	// Line
	public void drawLine(A2DLine line,A2RGBA color,float width,boolean clipCheck)
	{
		if(clipCheck)
		{

			boolean pos00View = true;
			boolean pos11View = true;
			
			if(!viewCam.frustum.pointInFrustum(new Vector3(line.getX1(),line.getY1(),0)))
			{
				pos00View = false;
			}
			
			if(!viewCam.frustum.pointInFrustum(new Vector3(line.getX2(),line.getY2(),0)))
			{
				pos11View = false;
			}
			
			if(!pos00View && !pos11View)
			{
				return;
			}
		
		}
		Gdx.gl20.glLineWidth(width);
				
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.line(line.getX1(), line.getY1(), line.getX2(), line.getY2());
		currentShapeRenderer.end();
        
	}
	
	// Line
	public void drawLine(float x1,float y1, float x2, float y2,A2RGBA color,float width,boolean clipCheck)
	{
		
		if(clipCheck)
		{
			boolean pos00View = true;
			boolean pos11View = true;
			
			if(!viewCam.frustum.pointInFrustum(new Vector3(x1,y1,0)))
			{
				pos00View = false;
			}
			
			if(!viewCam.frustum.pointInFrustum(new Vector3(x2,y2,0)))
			{
				pos11View = false;
			}
			
			if(!pos00View && !pos11View)
			{
				return;
			}		
		}

		
		Gdx.gl20.glLineWidth(width);
				
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        currentShapeRenderer.line(x1, y1, x2, y2);
        currentShapeRenderer.end();
        
	}
	
	// Line
	public void drawLine(float x1,float y1, float x2, float y2,A2RGBA color,boolean clipCheck)
	{
		if(clipCheck)
		{
			boolean pos00View = true;
			boolean pos11View = true;
			
			if(!viewCam.frustum.pointInFrustum(new Vector3(x1,y1,0)))
			{
				pos00View = false;
			}
			
			if(!viewCam.frustum.pointInFrustum(new Vector3(x2,y2,0)))
			{
				pos11View = false;
			}
			
			if(!pos00View && !pos11View)
			{
				return;
			}			
		}
		
		Gdx.gl20.glLineWidth(defaultLineWidth);
		
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.line(x1, y1, x2, y2);
		currentShapeRenderer.end();
        
	}
	
	// Outlined Rectangle
	public void drawRectangle(A2DRectangle rectangle)
	{
		drawRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(),rectangle.getColor().getRed(),rectangle.getColor().getGreen(),rectangle.getColor().getBlue(),rectangle.getColor().getAlpha(),defaultLineWidth);
	}
	
	public void drawRectangle(A2DRectangle rectangle, float lineWidth)
	{
		drawRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(),rectangle.getColor().getRed(),rectangle.getColor().getGreen(),rectangle.getColor().getBlue(),rectangle.getColor().getAlpha(),lineWidth);
	}

	public void drawRectangle(float x,float y,float width,float height,A2RGBA color)
	{
		drawRectangle(x,y,width,height,color.getRed(),color.getGreen(), color.getBlue(), color.getAlpha(),defaultLineWidth);
	}
	
	public void drawRectangle(float x,float y,float width,float height,A2RGBA color,float lineWidth)
	{
		drawRectangle(x,y,width,height,color.getRed(),color.getGreen(), color.getBlue(), color.getAlpha(),lineWidth);
	}
		
	
	public void drawRectangle(float x,float y,float width,float height,float r,float g, float b, float a)
	{
		drawRectangle(x,y,width,height,r,g, b, a, defaultLineWidth);
	}
	
	public void drawRectangle(float x,float y,float width,float height,float r,float g, float b, float a, float lineWidth)
	{
		Gdx.gl20.glLineWidth(lineWidth);

		boolean pos00View = true;
		boolean pos01View = true;
		boolean pos11View = true;
		boolean pos10View = true;

		if(!viewCam.frustum.pointInFrustum(new Vector3(x,y,0)))
		{
			pos00View = false;
		}
		
		if(!viewCam.frustum.pointInFrustum(new Vector3(x+width,y,0)))
		{
			pos10View = false;
		}
		
		if(!viewCam.frustum.pointInFrustum(new Vector3(x,y+height,0)))
		{
			pos10View = false;
		}
		
		if(!viewCam.frustum.pointInFrustum(new Vector3(x+width,y+height,0)))
		{
			pos11View = false;
		}
		
		if(!pos00View && !pos10View && !pos01View && !pos11View)
		{
			return;
		}
		
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(r, g, b, a);
		currentShapeRenderer.rect(x,  y, width, height);
		currentShapeRenderer.end();
	}
	
	// Filled Rectangle
	public void drawFilledRectangle(A2DRectangle rectangle,A2RGBA color)
	{
		drawFilledRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(),color);
	}
	
	public void drawFilledRectangle(float x,float y,float width,float height,A2RGBA color)
	{
		boolean pos00View = true;
		boolean pos01View = true;
		boolean pos11View = true;
		boolean pos10View = true;

		if(!viewCam.frustum.pointInFrustum(new Vector3(x,y,0)))
		{
			pos00View = false;
		}
		
		if(!viewCam.frustum.pointInFrustum(new Vector3(x+width,y,0)))
		{
			pos10View = false;
		}
		
		if(!viewCam.frustum.pointInFrustum(new Vector3(x,y+height,0)))
		{
			pos10View = false;
		}
		
		if(!viewCam.frustum.pointInFrustum(new Vector3(x+width,y+height,0)))
		{
			pos11View = false;
		}
		
		if(!pos00View && !pos10View && !pos01View && !pos11View)
		{
			return;
		}
		
		currentShapeRenderer.begin(ShapeType.Filled);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.rect(x,  y, width, height);
		currentShapeRenderer.end();
	}

	@Override
	public boolean keyDown(int arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char arg0)
	{
		return false;
	}

	@Override
	public boolean keyUp(int arg0)
	{
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y)
	{
		return false;
	}

	@Override
	public boolean scrolled(int val)
	{
		if(sim!=null)
		{
			sim.getSimManager().adjCamZoom(val);
		}
		
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		if(button == 0)
		{
			button0Pressed = true;
			mousePos.set(-x - viewCam.position.x, y - viewCam.position.y);
			
		}
		else
		{
			resetCamera();
		}
		
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int z)
	{
		if(button0Pressed)
		{
			float camX = (-x) - mousePos.getX();
			float camY = (y) - mousePos.getY();
	
			moveCamera(camX, camY);
		}
		else
		{
			
		}
			
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		button0Pressed = false;
		
		mousePos.set(-x - viewCam.position.x, y - viewCam.position.y);
				
		return false;
	}
	
	private void moveCamera(float x, float y)
	{		
		if(sim!=null)
		{
			sim.getSimManager().moveCamPos(x,y);
		}
	}
	
	public void resetCamera()
	{		
		//viewCam.position.set(globalTranslateDefault.getX() + Gdx.graphics.getWidth()/2, globalTranslateDefault.getY() + Gdx.graphics.getHeight()/2, 0);
		
		if(sim!=null)
		{
			SimulationScenarioManagerInf simManager = sim.getSimManager();
			
			// This can be null if a tab is added when no sim is generated and the window is resized
			if(simManager!=null)
			{
				sim.getSimManager().resetCamZoom();	
				sim.getSimManager().resetCamPos(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);	
			}
		}
	}	
	
	public void setSimulationTitle(String text)
	{
		simulationTitle = text;
	}
	
	/**
	 * Method setViewRangeDrawing.
	 * @param inViewRangeDrawing boolean
	 */
	public void setViewRangeDrawing(boolean inViewRangeDrawing)
	{
		viewRangeDrawing = inViewRangeDrawing;
	}

	/**
	 * Method setViewRangeDrawing.
	 * @param inViewRangeDrawing boolean
	 */
	public void setViewsDrawing(boolean inViewsDrawing)
	{
		viewsDrawing = inViewsDrawing;
	}
		
}
