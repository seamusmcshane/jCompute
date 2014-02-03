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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class NewSimView implements ApplicationListener, InputProcessor
{
	/** The Drawing Canvas */
	private LwjglAWTCanvas canvas;	
	
	/** Cameara and Shape Renderer */
	private ShapeRenderer shapeRenderer;
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
	private SpriteBatch spriteBatch;

	private float defaultLineWidth = 0.25f;
	
	public NewSimView()
	{
		canvas = new LwjglAWTCanvas(this, false);
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
        shapeRenderer = new ShapeRenderer();
        
        spriteBatch = new SpriteBatch();
        
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        
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

	@Override
	public void render()
	{        
		Display.sync(defaultFrameRate);

		GL10 gl = Gdx.graphics.getGL10();
		
		Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
		Gdx.gl.glEnable(GL10.GL_POINT_SMOOTH);
		Gdx.gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
		Gdx.gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		viewCam.update();
		
		viewCam.apply(gl);
				        
		shapeRenderer.setProjectionMatrix(viewCam.combined);
		
		viewLock.acquireUninterruptibly();
		
		if(sim!=null)
		{
			if (drawSim)
			{
				sim.drawSim(this,viewRangeDrawing,viewsDrawing);
			}
		}
		
		viewLock.release();
		
		spriteBatch.setProjectionMatrix(viewCam.combined);
		spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		spriteBatch.begin();			
        	font.draw(spriteBatch, simulationTitle, 0, Gdx.graphics.getHeight()-25);
        spriteBatch.end();
        
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
	public void drawPixMap(Pixmap pixmap, float x, float y)
	{
		Texture texture = new Texture(pixmap);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		Sprite sprite = new Sprite(texture);
		spriteBatch.begin();
	        sprite.setPosition(x, y);
	        sprite.draw(spriteBatch);
        spriteBatch.end();
        
        texture.dispose();
	}
	
	public void drawCircle(A2DCircle circle,A2RGBA color)
	{
		Gdx.gl10.glLineWidth(defaultLineWidth);
		
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.circle(circle.getX(),circle.getY(),circle.getRadius());
        shapeRenderer.end();
	}
	
	public void drawFilledCircle(A2DCircle circle,A2RGBA color)
	{
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.circle(circle.getX(),circle.getY(),circle.getRadius());
        shapeRenderer.end();
	}
	
	// Line
	public void drawLine(A2DLine line,A2RGBA color,float width)
	{
		Gdx.gl10.glLineWidth(width);
		
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.line(line.getX1(), line.getY1(), line.getX2(), line.getY2());
        shapeRenderer.end();
        
	}
	
	// Line
	public void drawLine(float x1,float y1, float x2, float y2,A2RGBA color,float width)
	{
		Gdx.gl10.glLineWidth(width);
		
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.line(x1, y1, x2, y2);
        shapeRenderer.end();
        
	}
	
	// Line
	public void drawLine(float x1,float y1, float x2, float y2,A2RGBA color)
	{
		Gdx.gl10.glLineWidth(defaultLineWidth);
		
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.line(x1, y1, x2, y2);
        shapeRenderer.end();
        
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
		Gdx.gl10.glLineWidth(lineWidth);

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.rect(x,  y, width, height);
        shapeRenderer.end();
	}
	
	public void drawRectangle(float x,float y,float width,float height,A2RGBA color)
	{
		Gdx.gl10.glLineWidth(defaultLineWidth);

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.rect(x,  y, width, height);
        shapeRenderer.end();
	}
	
	// Filled Rectangle
	public void drawFilledRectangle(A2DRectangle rectangle,A2RGBA color)
	{
		drawFilledRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(),color);
	}
	
	public void drawFilledRectangle(float x,float y,float width,float height,A2RGBA color)
	{
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        shapeRenderer.rect(x,  y, width, height);
        shapeRenderer.end();
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
