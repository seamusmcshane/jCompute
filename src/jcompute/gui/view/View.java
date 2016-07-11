package jcompute.gui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Semaphore;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.gui.view.renderer.util.Text;
import jcompute.gui.view.renderer.util.VectorGraphic2d;

public class View implements ApplicationListener, ViewRendererInf
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(View.class);
	
	/** Swing base panel */
	private JPanel basePanel;
	
	/** The Drawing Canvas's */
	private LwjglCanvas glCanvas;
	
	// Overlay camera
	private OrthographicCamera overlayCamera;
	private ScreenViewport overlayViewport;
	
	// OverLay Text
	private GlyphLayout layout;
	private SpriteBatch overlaySpriteBatch;
	private ShapeRenderer overlayShapeRenderer;
	private String overlayTitle = "";
	
	// Fonts
	private BitmapFont headingFont;
	private BitmapFont subHeadingFont;
	private BitmapFont textFont;
	
	// ViewTarget Reference
	private ViewTarget target;
	private Semaphore viewLock = new Semaphore(1);
	
	// Frame Rate
	private int defaultFrameRate = 60;
	
	// Help overlay text
	private boolean displayHelp = false;
	
	// Input
	private InputMultiplexer inputMultiplexer;
	
	// Renderer Keyboard input repeat delay calc
	private long lastPressFrame = 0;
	
	private boolean viewportNeedsupdated = true;
	
	// View port width/height latched
	private int viewWidth;
	private int viewHeight;
	
	public View()
	{
		LwjglApplicationConfiguration.disableAudio = true;
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		
		cfg.title = "View";
		cfg.samples = 0;
		cfg.vSyncEnabled = true;
		cfg.useGL30 = false;
		
		basePanel = new JPanel();
		
		basePanel.setPreferredSize(new Dimension(1024, 1024));
		
		basePanel.setLayout(new BorderLayout());
		
		glCanvas = new LwjglCanvas(this, cfg);
		
		basePanel.add(glCanvas.getCanvas(), BorderLayout.CENTER);
		
		log.info("Created View");
	}
	
	public void stopDisplay()
	{
		glCanvas.getInput().setInputProcessor(null);
		glCanvas.stop();
		
		log.info("Stop View");
	}
	
	public void exitDisplay()
	{
		glCanvas.getInput().setInputProcessor(null);
		glCanvas.stop();
		Display.destroy();
		
		log.info("Exited View");
	}
	
	public JComponent getCanvas()
	{
		return basePanel;
	}
	
	public void setViewTarget(ViewTarget simIn)
	{
		viewLock.acquireUninterruptibly();
		
		inputMultiplexer.clear();
		
		target = simIn;
		
		if(target != null && target.getRenderer() != null)
		{
			target.getRenderer().setMultiplexer(inputMultiplexer);
		}
		
		viewportNeedsupdated = true;
		
		viewLock.release();
	}
	
	@Override
	public void create()
	{
		Display.setVSyncEnabled(true);
		Display.setSwapInterval(1);
		
		inputMultiplexer = new InputMultiplexer();
		
		glCanvas.getInput().setInputProcessor(inputMultiplexer);
		
		overlayCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		overlayViewport = new ScreenViewport(overlayCamera);
		
		layout = new GlyphLayout();
		
		// TODO - font name hardcoded for now.
		String fontPathString = "/fonts/" + "Open_Sans/" + "OpenSans-Regular.ttf";
		URL fontUrl = View.class.getResource(fontPathString);
		String fontFile = null;
		try
		{
			fontFile = new File(fontUrl.toURI()).getAbsolutePath();
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
			
			System.exit(-1);
		}
		
		// Load the source font
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontFile));
		
		// Set the parameters
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.color = Color.WHITE;
		parameter.borderColor = Color.BLACK;
		parameter.borderStraight = false;
		// parameter.genMipMaps = true;
		// parameter.minFilter = TextureFilter.MipMapLinearLinear;
		// parameter.magFilter = TextureFilter.MipMapLinearLinear;
		
		parameter.borderWidth = 1;
		parameter.size = 20;
		headingFont = generator.generateFont(parameter);
		
		parameter.borderWidth = 1;
		parameter.size = 12;
		subHeadingFont = generator.generateFont(parameter);
		
		parameter.borderWidth = 0;
		parameter.size = 12;
		textFont = generator.generateFont(parameter);
		
		// Free font generator
		generator.dispose();
		
		overlaySpriteBatch = new SpriteBatch();
		overlayShapeRenderer = new ShapeRenderer();
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
		// Display FullScreen Toggle
		boolean fullscreen = Gdx.graphics.isFullscreen();
		
		// Full Screen Toggle - Uses LWJGL fullscreen methods
		if(Gdx.input.isKeyPressed(Input.Keys.F))
		{
			if(!fullscreen)
			{
				try
				{
					Display.setFullscreen(true);
					
					viewportNeedsupdated = true;
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
					
					viewportNeedsupdated = true;
				}
				catch(LWJGLException e)
				{
					e.printStackTrace();
				}
				
			}
			
		}
		
		if(Gdx.input.isKeyPressed(Input.Keys.H))
		{
			if(displayHelp)
			{
				displayHelp = false;
			}
			else
			{
				displayHelp = true;
			}
		}
		
	}
	
	@Override
	public void render()
	{
		Display.sync(defaultFrameRate);
		
		globalInput();
		
		overlayCamera.update();
		
		overlaySpriteBatch.setProjectionMatrix(overlayCamera.combined);
		overlayShapeRenderer.setProjectionMatrix(overlayCamera.combined);
		
		viewLock.acquireUninterruptibly();
		
		if(target != null)
		{
			if(target.getRenderer() != null)
			{
				ViewRendererInf r = target.getRenderer();
				
				if(r.needsGLInit())
				{
					r.glInit(this);
					
					// Input reset
					inputMultiplexer.clear();
					r.setMultiplexer(inputMultiplexer);
					
					viewportNeedsupdated = true;
				}
				
				if(viewportNeedsupdated)
				{
					// Display FullScreen Toggle
					boolean fullscreen = Gdx.graphics.isFullscreen();
					
					if(fullscreen)
					{
						viewWidth = Gdx.graphics.getDisplayMode().width;
						viewHeight = Gdx.graphics.getDisplayMode().height;
						
					}
					else
					{
						viewWidth = Gdx.graphics.getWidth();
						viewHeight = Gdx.graphics.getHeight();
					}
					
					r.updateViewPort(viewWidth, viewHeight);
					
					overlayViewport.update(viewWidth, viewHeight);
					
					viewportNeedsupdated = false;
				}
				
				doInput(r);
				
				r.render();
				
				overlayTitle = target.getInfo();
				
				if(displayHelp)
				{
					// Draw Help Text
				}
			}
		}
		else
		{
			Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			
			if(viewportNeedsupdated)
			{
				// Display FullScreen Toggle
				boolean fullscreen = Gdx.graphics.isFullscreen();
				
				if(fullscreen)
				{
					// resize(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height);
					
					viewWidth = Gdx.graphics.getDisplayMode().width;
					viewHeight = Gdx.graphics.getDisplayMode().height;
					
				}
				else
				{
					// resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
					
					viewWidth = Gdx.graphics.getWidth();
					viewHeight = Gdx.graphics.getHeight();
				}
				
				overlayViewport.update(viewWidth, viewHeight);
				
				viewportNeedsupdated = false;
			}
			
			overlayTitle = "None";
		}
		
		overlayViewport.apply();
		
		// View Title String (TopLeft)
		float x = -(overlayCamera.viewportWidth * 0.5f);
		float y = (overlayCamera.viewportHeight * 0.5f);
		
		// Blending on
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		// View Overlay
		VectorGraphic2d.RectangleFilled(this, x, y - 35, overlayCamera.viewportWidth, 35, 0f, 0f, 0f, 0.5f);
		
		drawOverlayText(x + 10, y - 10, false, overlayTitle, headingFont);
		
		Text.String(this, headingFont, x + 50, y - 150, "HEADING");
		Text.String(this, subHeadingFont, x + 50, y - 200, "SUBHEADING");
		Text.String(this, textFont, x + 50, y - 250, "TEXT");
		
		// End Blending
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
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
		// overlayCam.viewportWidth = width;
		// overlayCam.viewportHeight = height;
		
		viewWidth = width;
		viewHeight = height;
		
		viewportNeedsupdated = true;
		
		// resetCamera();
	}
	
	@Override
	public void resume()
	{
		
	}
	
	public void drawOverlayText(float x, float y, boolean centered, String text, BitmapFont font)
	{
		overlaySpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		overlaySpriteBatch.begin();
		
		if(centered)
		{
			layout.setText(font, text);
			font.draw(overlaySpriteBatch, text, -layout.width * 0.5f + x, -(layout.height * 0.5f) - y);
		}
		else
		{
			font.draw(overlaySpriteBatch, text, x, y);
		}
		
		overlaySpriteBatch.end();
	}
	
	public void resetCamera()
	{
		if(target != null)
		{
			target.getRenderer().resetViewCam();
		}
	}
	
	@Override
	public boolean needsGLInit()
	{
		// NA
		return false;
	}
	
	@Override
	public void glInit(View view)
	{
		// NA
		return;
	}
	
	@Override
	public SpriteBatch getSpriteBatch()
	{
		return overlaySpriteBatch;
	}
	
	@Override
	public ShapeRenderer getShapeRenderer()
	{
		return overlayShapeRenderer;
	}
	
	@Override
	public void resetViewCam()
	{
		// NA
		return;
	}
	
	@Override
	public Camera getCamera()
	{
		return overlayCamera;
	}
	
	@Override
	public void updateViewPort(int width, int height)
	{
		// NA
	}
	
	@Override
	public boolean doInput()
	{
		return false;
	}
	
	@Override
	public void setMultiplexer(InputMultiplexer inputMultiplexer)
	{
		// NA
	}
	
	@Override
	public Texture getTexture(int id)
	{
		return null;
	}
	
	@Override
	public Pixmap getPixmap(int num)
	{
		return null;
	}
	
	@Override
	public int getTextureSize(int id)
	{
		return 0;
	}
	
	@Override
	public void cleanup()
	{
		// NA
	}
	
	@Override
	public int getHeight()
	{
		return 0;
	}
	
	@Override
	public int getWidth()
	{
		return 0;
	}
	
	public enum ViewFont
	{
		TITLE, HEADING, SUBHEADING, TEXT;
	}
	
	/**
	 * Allows retrieving and using the fonts provided by the view.
	 * 
	 * @param font
	 * @return
	 */
	public BitmapFont getFont(ViewFont font)
	{
		switch(font)
		{
			case HEADING:
			{
				return headingFont;
			}
			case SUBHEADING:
			{
				return subHeadingFont;
			}
			case TEXT:
				// Fallthough
			default:
			{
				return textFont;
			}
		}
	}
	
	public Camera getOverlayCamera()
	{
		return overlayCamera;
	}
}
