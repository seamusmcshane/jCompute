package alifeSim.Gui;

import java.awt.Canvas;
import java.util.concurrent.Semaphore;

import org.lwjgl.opengl.Display;

import alifeSim.Simulation.Simulation;
import alifeSimGeom.A2DCircle;
import alifeSimGeom.A2DLine;
import alifeSimGeom.A2DRectangle;
import alifeSimGeom.A2RGBA;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class NewSimView implements ApplicationListener
{
	/** The Drawing Canvas */
	private LwjglAWTCanvas canvas;	
	
	/** Cameara and Shape Renderer */
	private ShapeRenderer shapeRenderer;
	private OrthographicCamera viewCam;
	
	/** Simulation Reference */
	private static Simulation sim;
	private static Semaphore viewLock = new Semaphore(1);
	
	
	public NewSimView()
	{
		canvas = new LwjglAWTCanvas(this, false);
		Display.setVSyncEnabled(true);
		Display.setSwapInterval(2);
		
		viewCam = new OrthographicCamera(640, 480);
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

	}

	@Override
	public void dispose()
	{
		
	}

	@Override
	public void pause()
	{

		
	}

	@Override
	public void render()
	{        
		GL10 gl = Gdx.graphics.getGL10();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		viewCam.update();
		viewCam.apply(gl);
				
		shapeRenderer.setProjectionMatrix(viewCam.combined);
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.line(0, 0, 100, 200);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(100,  100, 50, 50);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle( 100,  100, 25, 16);
        shapeRenderer.end();
        
		viewLock.acquireUninterruptibly();
		
		if(sim!=null)
		{
			sim.drawSim(this);
		}
		
		viewLock.release();

	}

	@Override
	public void resize(int width, int height)
	{		
		viewCam.setToOrtho(true, width, height);
		
		viewCam.position.set(width/2, height/2, 0);
	}
	
	@Override
	public void resume()
	{
		
	} 

	/*
	 * Drawing Methods
	 * 
	 */
		
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
	
	// Outlined Rectangle
	public void drawRectangle(A2DRectangle rectangle,A2RGBA color)
	{
		drawRectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(),color);
	}

	public void drawRectangle(float x,float y,float width,float height,A2RGBA color)
	{
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
}
