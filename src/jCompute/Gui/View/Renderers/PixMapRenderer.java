package jCompute.Gui.View.Renderers;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import jCompute.Gui.View.Lib2D;
import jCompute.Gui.View.ViewCam;
import jCompute.Gui.View.ViewRendererInf;
import jCompute.Gui.View.Graphics.A2DVector2f;

public class PixMapRenderer implements ViewRendererInf
{
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private ShapeRenderer shapeRenderer;
	
	/** Rendering Limits */
	private final float CAMERA_NEAR_CLIP = 1f;
	private final float CAMERA_FAR_CLIP = 1024f;
	
	private PerspectiveCamera camera;
	private ViewCam simViewCam;
	private ExtendViewport viewport;
	
	private boolean needsGLInit;
	
	/** Synchronisers */
	private Semaphore compute;
	private Semaphore computed;
	
	private int textureSize;
	private float scaledSize;
	
	private int[] computeBuffer;
	
	private long drawTimeoutLimit = 15;
	
	private int[] drawBuffer;
	
	private InputProcessor inputProcessor;
	
	public PixMapRenderer(InputProcessor inputProcessor, Semaphore compute, Semaphore computed, int textureSize,
			int[] computeBuffer)
	{
		this.inputProcessor = inputProcessor;
		
		// To avoid concurrent access to dynamic simulation structures
		this.compute = compute;
		this.computed = computed;
		
		this.textureSize = textureSize;
		
		drawBuffer = new int[textureSize * textureSize];
		
		this.computeBuffer = computeBuffer;
		
		needsGLInit = true;
		
		simViewCam = new ViewCam(new A2DVector2f(new float[]
		{
			0, 0
		}), new A2DVector2f(new float[]
		{
			0, 0
		}));
	}
	
	@Override
	public boolean needsGLInit()
	{
		return needsGLInit;
	}
	
	@Override
	public void glInit()
	{
		camera = new PerspectiveCamera();
		
		viewport = new ExtendViewport(1, 1, camera);
		
		camera.near = CAMERA_NEAR_CLIP;
		camera.far = CAMERA_FAR_CLIP;
		
		// Renderer
		shapeRenderer = new ShapeRenderer();
		
		spriteBatch = new SpriteBatch();
		
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		
		updateCamera();
		
		needsGLInit = false;
	}
	
	@Override
	public SpriteBatch getSpriteBatch()
	{
		return spriteBatch;
	}
	
	@Override
	public ShapeRenderer getShapeRenderer()
	{
		return shapeRenderer;
	}
	
	@Override
	public BitmapFont getFont()
	{
		return font;
	}
	
	@Override
	public ViewCam getViewCam()
	{
		return simViewCam;
	}
	
	@Override
	public Camera getCamera()
	{
		return camera;
	}
	
	private void updateCamera()
	{
		spriteBatch.setProjectionMatrix(camera.combined);
		shapeRenderer.setProjectionMatrix(camera.combined);
		
		camera.position.x = simViewCam.getCamPosX();
		camera.position.y = simViewCam.getCamPosY();
		camera.position.z = simViewCam.getCamZoom();
		
		camera.update();
	}
	
	@Override
	public boolean doInput()
	{
		return false;
	}
	
	@Override
	public void render()
	{
		updateCamera();
		
		try
		{
			// Wait on the kernel to signal we can swap buffers, but if they
			// don't reply in time draw the OLD buffer
			if(computed.tryAcquire(drawTimeoutLimit, TimeUnit.MILLISECONDS))
			{
				System.arraycopy(computeBuffer, 0, drawBuffer, 0, computeBuffer.length);
			}
			
			Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			scaledSize = Lib2D.drawPixelMap(this, textureSize, drawBuffer, 0, 0);
			
		}
		catch(InterruptedException e)
		{
		}
		
		// Signal kernel it can compute now
		if(!(compute.availablePermits() > 0))
		{
			compute.release();
		}
	}
	
	@Override
	public void updateViewPort(int width, int height)
	{
		viewport.update(width, height);
	}
	
	@Override
	public void setMultiplexer(InputMultiplexer inputMultiplexer)
	{
		// No requirement to have input
		if(inputProcessor != null)
		{
			inputMultiplexer.addProcessor(inputProcessor);
		}
		
	}
	
	public float getTextureScale()
	{
		return scaledSize;
	}
	
}
