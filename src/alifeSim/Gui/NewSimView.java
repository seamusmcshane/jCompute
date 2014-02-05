package alifeSim.Gui;

import java.awt.Canvas;
import java.util.concurrent.Semaphore;

import org.lwjgl.opengl.Display;

import alifeSim.Simulation.Simulation;
import alifeSimGeom.A2DCircle;
import alifeSimGeom.A2DLine;
import alifeSimGeom.A2DRectangle;
import alifeSimGeom.A2DVector2f;
import alifeSimGeom.A2RGBA;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
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
import com.badlogic.gdx.math.Matrix4;

public class NewSimView implements ApplicationListener, InputProcessor
{
	/** The Drawing Canvas */
	private LwjglAWTCanvas canvas;	
	private int width;
	private int height;
	
	/** Cameara and Shape Renderer */
	private OrthographicCamera viewCam;
	
	/** Simulation Reference */
	private static Simulation sim;
	private static Semaphore viewLock = new Semaphore(1);

	private static String simulationTitle = "";
		
	/** Records status of mouse button */
	private boolean mouseButtonPressed = false;
	
	private int defaultFrameRate = 60;

	/** Draw the View range of the agents */
	private static boolean viewRangeDrawing = false;

	/** Draw Views */
	private static boolean viewsDrawing = false;
	
	/** Is Simulation view drawing enabled */
	private static boolean drawSim = false;
	
	/* Mouse */
	/** Stores the mouse vector across updates */
	private A2DVector2f mousePos = new A2DVector2f(0, 0);
	private A2DVector2f globalTranslateDefault = new A2DVector2f(-50, -50);

	private boolean button0Pressed;
	
	private BitmapFont font;


	private float defaultLineWidth = 0.25f;
	
	/* FBO / BBO */
	private SpriteBatch bboSpriteBatch;
	private ShapeRenderer bboShapeRenderer;
	
	private FrameBuffer fbo;
	private SpriteBatch fboSpriteBatch;
	private ShapeRenderer fboShapeRenderer;

	private SpriteBatch currentSpriteBatch;
	private ShapeRenderer currentShapeRenderer;

	
	public NewSimView()
	{
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
		// Needed or a variety of deadlocks and errors can occur on App exit.
		canvas.stop();
	}
	
	public Canvas getAwtCanvas()
	{
		return canvas.getCanvas();
	}

	public static void setSim(Simulation simIn)
	{
		System.out.println("Simulation Set");
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

		//GL20 gl = Gdx.graphics.getGL20();
		
		/*Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
		Gdx.gl.glEnable(GL10.GL_POINT_SMOOTH);
		Gdx.gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
		Gdx.gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);*/
		
		Gdx.gl.glViewport(0, 0, width, height);
				
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		viewCam.update();
		
		// viewCam.apply(gl); // GL10
				        
		bboShapeRenderer.setProjectionMatrix(viewCam.combined);
		bboSpriteBatch.setProjectionMatrix(viewCam.combined);
		
		viewLock.acquireUninterruptibly();
		
		if(sim!=null)
		{
			if (drawSim)
			{
				sim.drawSim(this,viewRangeDrawing,viewsDrawing);
			}
		}
		
		viewLock.release();
				

        
	}

	@Override
	public void resize(int width, int height)
	{		
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
		
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.circle(circle.getX(),circle.getY(),circle.getRadius());
		currentShapeRenderer.end();
	}
	
	public void drawFilledCircle(A2DCircle circle,A2RGBA color)
	{
		currentShapeRenderer.begin(ShapeType.Filled);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.circle(circle.getX(),circle.getY(),circle.getRadius());
		currentShapeRenderer.end();
	}
	
	// Line
	public void drawLine(A2DLine line,A2RGBA color,float width)
	{
		Gdx.gl20.glLineWidth(width);
		
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.line(line.getX1(), line.getY1(), line.getX2(), line.getY2());
		currentShapeRenderer.end();
        
	}
	
	// Line
	public void drawLine(float x1,float y1, float x2, float y2,A2RGBA color,float width)
	{
		Gdx.gl20.glLineWidth(width);
		
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        currentShapeRenderer.line(x1, y1, x2, y2);
        currentShapeRenderer.end();
        
	}
	
	// Line
	public void drawLine(float x1,float y1, float x2, float y2,A2RGBA color)
	{
		Gdx.gl20.glLineWidth(defaultLineWidth);
		
		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.line(x1, y1, x2, y2);
		currentShapeRenderer.end();
        
	}
	
	// Outlined Rectangle
	public void drawRectangle(A2DRectangle rectangle,A2RGBA color,float lineWidth)
	{
		drawRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(),color,lineWidth);
	}
	
	// Outlined Rectangle
	public void drawRectangle(A2DRectangle rectangle,A2RGBA color)
	{
		drawRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(),color);
	}

	public void drawRectangle(float x,float y,float width,float height,A2RGBA color,float lineWidth)
	{
		Gdx.gl20.glLineWidth(lineWidth);

		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		currentShapeRenderer.rect(x,  y, width, height);
		currentShapeRenderer.end();
	}
	
	public void drawRectangle(float x,float y,float width,float height,A2RGBA color)
	{
		Gdx.gl20.glLineWidth(defaultLineWidth);

		currentShapeRenderer.begin(ShapeType.Line);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
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
		viewCam.zoom +=(val*0.125f);
		
		if(viewCam.zoom < 0.2f)
		{
			viewCam.zoom = 0.2f;
		}
		
		if(viewCam.zoom > 10f)
		{
			viewCam.zoom = 10f;
		}
		
		System.out.println("Zoom " + viewCam.zoom);
		
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
		viewCam.position.set(x, y,0);
	}
	
	private void resetCamera()
	{
		viewCam.zoom = 1f;
		
		viewCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		viewCam.position.set(globalTranslateDefault.getX() + Gdx.graphics.getWidth()/2, globalTranslateDefault.getY() + Gdx.graphics.getHeight()/2, 0);
	}
		
	public static void setSimulationTitle(String text)
	{
		simulationTitle = text;
	}
	
	/**
	 * Method setViewRangeDrawing.
	 * @param inViewRangeDrawing boolean
	 */
	public static void setViewRangeDrawing(boolean inViewRangeDrawing)
	{
		viewRangeDrawing = inViewRangeDrawing;
	}

	/**
	 * Method setViewRangeDrawing.
	 * @param inViewRangeDrawing boolean
	 */
	public static void setViewsDrawing(boolean inViewsDrawing)
	{
		viewsDrawing = inViewsDrawing;
	}
	
	/**
	 * Method setVisible.
	 * @param visible boolean
	 */
	public static void setVisible(boolean visible)
	{
		drawSim = visible;
	}
	
}
