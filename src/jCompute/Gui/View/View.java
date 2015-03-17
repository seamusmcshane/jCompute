package jCompute.Gui.View;

import jCompute.Gui.View.Graphics.A2DVector2f;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.Semaphore;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class View implements ApplicationListener, InputProcessor
{
	private JPanel basePanel;
	
	/** The Drawing Canvas's */
	private LwjglCanvas glCanvas;
	
	/** Cameara and Shape Renderer */
	private PerspectiveCamera globalViewCam;
	private ExtendViewport viewport;
	
	/** ViewTarget Reference */
	private ViewTarget target;
	private Semaphore viewLock = new Semaphore(1);
	
	private String viewTitle = "";
	
	private int defaultFrameRate = 60;
	
	// Display FullScreen Toggle
	boolean fullscreen = false;
	private int windowedWidthLatch;
	private int windowedHeightLatch;
	
	/* Mouse */
	/** Stores the mouse vector across updates */
	private A2DVector2f mousePos = new A2DVector2f(new float[]
	{
		0, 0
	});
	
	private boolean button1Pressed;
	
	private BitmapFont overlayFont;
	private SpriteBatch overlaySpriteBatch;
	
	// Renderer Keyboard input delay calc
	private long lastPressFrame = 0;
	
	private final float CAMERA_FAR_CLIP = 30000f;
	private final float CAMERA_NEAR_CLIP = 50f;
	
	private boolean cameraResetAnimate = false;
	
	public View()
	{
		System.out.println("Created View");
		
		LwjglApplicationConfiguration.disableAudio = true;
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		
		cfg.title = "PhasePlot3d";
		cfg.samples = 2;
		cfg.vSyncEnabled = true;
		cfg.useGL30 = false;
		
		basePanel = new JPanel();
		
		basePanel.setPreferredSize(new Dimension(1024, 1024));
		
		basePanel.setLayout(new BorderLayout());
		
		glCanvas = new LwjglCanvas(this, cfg);
		
		basePanel.add(glCanvas.getCanvas(), BorderLayout.CENTER);
		
		Display.setVSyncEnabled(true);
		Display.setSwapInterval(1);
		
		glCanvas.getInput().setInputProcessor(this);
		
		globalViewCam = new PerspectiveCamera();
		viewport = new ExtendViewport(1, 1, globalViewCam);
		
		globalViewCam.near = CAMERA_NEAR_CLIP;
		globalViewCam.far = CAMERA_FAR_CLIP;
		
		resetCamera();
	}
	
	public void stopDisplay()
	{
		glCanvas.getInput().setInputProcessor(null);
		glCanvas.stop();
		
		System.out.println("Stop View");
	}
	
	public void exitDisplay()
	{
		glCanvas.getInput().setInputProcessor(null);
		glCanvas.stop();
		Display.destroy();
		
		System.out.println("Exited View");
	}
	
	public JComponent getCanvas()
	{
		return basePanel;
	}
	
	public void setViewTarget(ViewTarget simIn)
	{
		viewLock.acquireUninterruptibly();
		target = simIn;
		viewLock.release();
	}
	
	@Override
	public void create()
	{
		overlayFont = new BitmapFont();
		overlayFont.setColor(Color.WHITE);
		
		overlaySpriteBatch = new SpriteBatch();
		
		overlaySpriteBatch.setProjectionMatrix(globalViewCam.combined);
		
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	@Override
	public void dispose()
	{
		/*
		 * font.dispose();
		 * shapeRenderer.dispose();
		 * spriteBatch.dispose();
		 */
	}
	
	@Override
	public void pause()
	{
		
	}
	
	private void globalInput()
	{
		fullscreen = Gdx.graphics.isFullscreen();
		
		// Full Screen Toggle - Uses LWJGL fullscreen methods
		if(Gdx.input.isKeyPressed(Input.Keys.F))
		{
			if(!fullscreen)
			{
				try
				{
					Display.setFullscreen(true);
					
					viewport.update(Gdx.graphics.getDesktopDisplayMode().width,
							Gdx.graphics.getDesktopDisplayMode().height);
				}
				catch(LWJGLException e)
				{
					e.printStackTrace();
				}
				
			}
			else
			{
				try
				{
					Display.setFullscreen(false);
					
					viewport.update(windowedWidthLatch, windowedHeightLatch);					
				}
				catch(LWJGLException e)
				{
					e.printStackTrace();
				}
				
			}
			
		}
	}
	
	@Override
	public void render()
	{
		Display.sync(defaultFrameRate);
		
		globalInput();
		
		viewLock.acquireUninterruptibly();
		
		if(target != null)
		{
			if(target.getRenderer() != null)
			{
				ViewRendererInf r = target.getRenderer();
				
				if(r.needsGLInit())
				{
					r.glInit();
				}
				
				doInput(r);
				
				target.getRenderer().updateCamera(globalViewCam);
				
				if(cameraResetAnimate)
				{
					if(target.getRenderer().getViewCam().reset())
					{
						cameraResetAnimate = false;
					}
				}
				
				r.render();
				
				viewTitle = target.getInfo();
			}
		}
		else
		{
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			viewTitle = "None";
		}
		
		drawOverlayText(10, viewport.getScreenHeight() - 10, viewTitle);
		
		viewLock.release();
	}
	
	private void doInput(ViewRendererInf r)
	{
		long currentFrame = Gdx.app.getGraphics().getFrameId();
		
		if((currentFrame - lastPressFrame) < 5)
		{
			return;
		}
		
		if(r.doInput())
		{
			lastPressFrame = currentFrame;
		}
	}
	
	@Override
	public void resize(int width, int height)
	{
		windowedWidthLatch = width;
		windowedHeightLatch = height;
		viewport.update(width, height);
		
		resetCamera();
	}
	
	@Override
	public void resume()
	{
		
	}
	
	public void drawOverlayText(float x, float y, String text)
	{
		Matrix4 viewPortTrans = globalViewCam.combined.cpy();
		viewPortTrans.setToOrtho2D(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
		overlaySpriteBatch.setProjectionMatrix(viewPortTrans);
		overlaySpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		overlaySpriteBatch.begin();
		overlayFont.draw(overlaySpriteBatch, text, x, y);
		overlaySpriteBatch.end();
	}
	
	@Override
	public boolean keyDown(int arg0)
	{
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
		return true;
	}
	
	@Override
	public boolean scrolled(int val)
	{
		if(target != null)
		{
			target.getRenderer().getViewCam().adjCamZoom(val);
		}
		
		return false;
	}
	
	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		if(button == 1)
		{
			button1Pressed = true;
			//resetCamera();
			
			cameraResetAnimate = true;
		}
		else
		{
			mousePos.set(x, y);
		}
		
		return true;
	}
	
	@Override
	public boolean touchDragged(int x, int y, int z)
	{
		
		if(!button1Pressed)
		{
			// Latch the old position
			float previousX = mousePos.getX();
			float previousY = mousePos.getY();
			
			// Update newX/Y
			mousePos.set(x, y);
			
			// How much did the mouse move.
			float diffX = previousX - mousePos.getX();
			float diffY = previousY - mousePos.getY();
			
			// -y for when converting from screen to graphics coordinates
			moveCamera(diffX, -diffY);
		}
		
		return false;
	}
	
	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		if(button1Pressed)
		{
			button1Pressed = false;
		}
		else
		{
			mousePos.set(x, y);
		}
		
		return false;
	}
	
	private void moveCamera(float x, float y)
	{
		if(target != null)
		{
			target.getRenderer().getViewCam().moveCam(x, y);
		}
	}
	
	public void resetCamera()
	{
		// viewCam.position.set(globalTranslateDefault.getX() +
		// Gdx.graphics.getWidth()/2, globalTranslateDefault.getY() +
		// Gdx.graphics.getHeight()/2, 0);
		
		if(target != null)
		{
			// This can be null if a tab is added when no sim is generated and
			// the window is resized
			// if(target.hasViewCam())
			{
				/*
				 * target.getRenderer().getViewCam().resetCamZoom();
				 * target.getRenderer().getViewCam()
				 * .resetCamPos(0,0);
				 */
				target.getRenderer().getViewCam().reset();
			}
		}
	}
	
}
