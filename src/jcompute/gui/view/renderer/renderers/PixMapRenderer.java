package jcompute.gui.view.renderer.renderers;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import jcompute.gui.view.ViewCam;
import jcompute.gui.view.graphics.A2DVector2f;
import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.gui.view.renderer.util.Lib2D;

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

	private long drawTimeoutLimit = 67;

	private ByteBuffer drawBuffer;

	private InputProcessor inputProcessor;

	private Texture texture;
	private Pixmap pTemp;

	public PixMapRenderer(InputProcessor inputProcessor, Semaphore compute, Semaphore computed, int textureSize, int[] computeBuffer)
	{
		this.inputProcessor = inputProcessor;

		// To avoid concurrent access to dynamic simulation structures
		this.compute = compute;
		this.computed = computed;

		this.textureSize = textureSize;

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

		// Target Texture
		pTemp = new Pixmap(textureSize, textureSize, Format.RGBA8888);
		texture = new Texture(textureSize, textureSize, Format.RGBA8888);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		drawBuffer = pTemp.getPixels();

		updateCamera();

		needsGLInit = false;
	}

	@Override
	public int getTextureSize(int id)
	{
		return textureSize;
	}

	@Override
	public Texture getTexture(int id)
	{
		// only 1 texture
		return texture;
	}

	@Override
	public Pixmap getPixmap(int id)
	{
		// only 1 Pixmap
		return pTemp;
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
	public void resetViewCam()
	{
		simViewCam.reset();
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
				drawBuffer.asIntBuffer().put(computeBuffer);
			}
		}
		catch(InterruptedException e)
		{
		}

		// Signal kernel it can compute now
		if(!(compute.availablePermits() > 0))
		{
			compute.release();
		}

		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		scaledSize = Lib2D.drawPixelMap(this, 0, 0, 0, true);
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

	@Override
	public int getWidth()
	{
		return viewport.getScreenWidth();
	}

	@Override
	public int getHeight()
	{
		return viewport.getScreenHeight();
	}

	public float getTextureScale()
	{
		return scaledSize;
	}

	@Override
	public void cleanup()
	{
		if(!needsGLInit)
		{
			spriteBatch.dispose();
			font.dispose();
			shapeRenderer.dispose();

			texture.dispose();
			pTemp.dispose();
		}
	}

}
