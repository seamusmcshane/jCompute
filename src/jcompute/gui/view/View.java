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
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.gui.view.renderer.util.VectorGraphic2d;
import jcompute.math.geom.JCVector2f;
import jcompute.math.geom.JCVector3f;
import jcompute.util.JCText;

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
	private final int defaultFrameRate = 60;
	
	// Help overlay text
	private boolean displayHelp = false;
	private boolean debugView = false;
	
	// Input
	private InputMultiplexer inputMultiplexer;
	
	private boolean viewportNeedsupdated = true;
	
	// View port width/height latched
	private int viewWidth;
	private int viewHeight;
	
	// Info Help Text
	private Stage infoStage;
	private Table infoTable;
	private TextureRegionDrawable rowBK;
	
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
			
			infoTable.clear();
			
			String scenarioTitle = target.getHelpTitleText();
			String[] keyList = target.getHelpKeyList();
			
			AddHelpTitle(scenarioTitle);
			
			// Default Keys
			AddDefaultHelpText();
			
			addHelpSubTitle("Scenario Keys");
			for(int k = 0; k < keyList.length; k += 2)
			{
				addHelpText(keyList[k], keyList[k + 1]);
			}
		}
		else
		{
			infoTable.clear();
			
			// Default Help
			AddDefaultHelpTitle();
			AddDefaultHelpText();
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
		parameter.color.a = 0.99f;
		parameter.borderColor = Color.BLACK;
		parameter.color.a = 0.99f;
		parameter.borderStraight = false;
		// parameter.genMipMaps = true;
		// parameter.minFilter = TextureFilter.MipMapLinearLinear;
		// parameter.magFilter = TextureFilter.MipMapLinearLinear;
		
		parameter.borderWidth = 1;
		parameter.size = 16;
		headingFont = generator.generateFont(parameter);
		
		parameter.borderWidth = 1;
		parameter.size = 12;
		subHeadingFont = generator.generateFont(parameter);
		
		parameter.borderWidth = 0;
		parameter.size = 10;
		textFont = generator.generateFont(parameter);
		
		// Free font generator
		generator.dispose();
		
		overlaySpriteBatch = new SpriteBatch();
		overlayShapeRenderer = new ShapeRenderer();
		
		Pixmap bkpm = new Pixmap(1, 1, Format.RGBA8888);
		bkpm.setColor(new Color(0, 0, 0, 0.85f));
		bkpm.fill();
		
		rowBK = new TextureRegionDrawable(new TextureRegion(new Texture(bkpm)));
		
		infoStage = new Stage(new ScreenViewport());
		infoTable = new Table();
		infoTable.setFillParent(true);
		infoTable.setBackground(rowBK);
		infoStage.addActor(infoTable);
		
		// Table layout debug
		infoTable.setDebug(debugView);
		
		// Default Help
		AddDefaultHelpTitle();
		AddDefaultHelpText();
	}
	
	/*
	 * WIP
	 */
	private ShaderProgram createShaderProgram(String vertex, String fragment)
	{
		String uri = "/shaders/";
		URL url = View.class.getResource(uri);
		
		if(url == null)
		{
			log.error("Not Found");
			
			return null;
		}
		
		String path = null;
		try
		{
			path = new File(url.toURI()).getAbsolutePath();
		}
		catch(URISyntaxException e1)
		{
			e1.printStackTrace();
		}
		
		String vfile = path + File.separator + "test.glvs";
		String ffile = path + File.separator + "default.glfs";
		
		final String VERTEX = JCText.textFileToString(vfile);
		final String FRAG = JCText.textFileToString(ffile);
		
		ShaderProgram.pedantic = false;
		
		ShaderProgram shader = new ShaderProgram(VERTEX, FRAG);
		
		if(!shader.isCompiled())
			throw new GdxRuntimeException("Couldn't compile shader: " + shader.getLog());
		
		return shader;
	}
	
	private void AddHelpTitle(String text)
	{
		infoTable.add(getTitleLabel(text)).colspan(2).center();
		infoTable.row().pad(10f);
	}
	
	private void AddDefaultHelpTitle()
	{
		AddHelpTitle("No Scenario Active");
	}
	
	private void addHelpSubTitle(String text)
	{
		infoTable.add(getSubTitleLabel(text)).colspan(2).center();
		infoTable.row().pad(5f);
	}
	
	private void addHelpText(String key, String text)
	{
		infoTable.add(getHighlightTextLabel(key)).right();
		infoTable.add(getTextLabel(text)).left();
		infoTable.row().pad(5f);
	}
	
	private void AddDefaultHelpText()
	{
		addHelpSubTitle("Standard Keys");
		
		addHelpText("H", "Display Help");
		addHelpText("F", "Toggle view Fullscreen/Windowed");
	}
	
	private Label getTitleLabel(String text)
	{
		return new Label(text, new Label.LabelStyle(headingFont, Color.YELLOW));
	}
	
	private Label getSubTitleLabel(String text)
	{
		return new Label(text, new Label.LabelStyle(subHeadingFont, Color.WHITE));
	}
	
	private Label getHighlightTextLabel(String text)
	{
		return new Label(text, new Label.LabelStyle(textFont, Color.WHITE));
	}
	
	private Label getTextLabel(String text)
	{
		return new Label(text, new Label.LabelStyle(textFont, Color.LIGHT_GRAY));
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
	
	private void doInput(ViewRendererInf r, long currentFrame)
	{
		// if(inputDelay(currentFrame))
		// {
		// return;
		// }
		
		r.doInput();
	}
	
	// private boolean inputDelay(long currentFrame)
	// {
	// if((currentFrame - lastPressFrame) < 5)
	// {
	// return true;
	// }
	//
	// lastPressFrame = currentFrame;
	//
	// return false;
	// }
	
	private void globalInput(long currentFrame)
	{
		// Full Screen Toggle - Uses LWJGL fullscreen methods
		if(Gdx.input.isKeyJustPressed(Input.Keys.F))
		{
			// Display FullScreen Toggle
			boolean fullscreen = Gdx.graphics.isFullscreen();
			
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
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.H))
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
		
		if(Gdx.input.isKeyJustPressed(Input.Keys.D))
		{
			if(debugView)
			{
				debugView = false;
			}
			else
			{
				debugView = true;
			}
			
			infoTable.setDebug(debugView);
		}
	}
	
	@Override
	public void render()
	{
		Display.sync(defaultFrameRate);
		
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		globalInput(Gdx.graphics.getFrameId());
		
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
				
				checkForResize(r);
				
				doInput(r, Gdx.graphics.getFrameId());
				
				r.render();
				
				overlayTitle = target.getInfo();
			}
		}
		else
		{
			checkForResize(null);
			
			overlayTitle = "None";
		}
		
		// Blending on
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		// View Title String (TopLeft)
		float x = -(overlayCamera.viewportWidth * 0.5f);
		float y = (overlayCamera.viewportHeight * 0.5f);
		
		overlayViewport.apply();
		
		// View Overlay
		VectorGraphic2d.RectangleFilled(this, x, y - 35, overlayCamera.viewportWidth, 35, 0f, 0f, 0f, 0.5f);
		
		drawOverlayText(x + 10, y - 10, false, overlayTitle, headingFont);
		
		// Text.String(this, headingFont, x + 50, y - 150, "HEADING");
		// Text.String(this, subHeadingFont, x + 50, y - 200, "SUBHEADING");
		// Text.String(this, textFont, x + 50, y - 250, "TEXT");
		
		if(displayHelp)
		{
			// Draw Help Text
			// infoStage.getCamera().position.set(viewWidth * 0.5f, viewHeight * 0.5f, 0);
			infoStage.getViewport().apply();
			infoStage.act(Gdx.graphics.getDeltaTime());
			infoStage.draw();
		}
		
		// End Blending
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		viewLock.release();
	}
	
	private void checkForResize(ViewRendererInf r)
	{
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
			
			if(r != null)
			{
				r.updateViewPort(viewWidth, viewHeight);
			}
			
			overlayViewport.update(viewWidth, viewHeight);
			
			infoStage.getViewport().update(viewWidth, viewHeight, true);
			
			viewportNeedsupdated = false;
		}
	}
	
	@Override
	public void resize(int width, int height)
	{
		viewWidth = width;
		viewHeight = height;
		
		viewportNeedsupdated = true;
	}
	
	@Override
	public void resume()
	{
		
	}
	
	public void drawOverlayText(float x, float y, boolean centered, String text, BitmapFont font)
	{
		overlaySpriteBatch.begin();
		overlaySpriteBatch.enableBlending();
		overlaySpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		if(centered)
		{
			layout.setText(font, text);
			font.draw(overlaySpriteBatch, text, -layout.width * 0.5f + x, -(layout.height * 0.5f) - y);
		}
		else
		{
			font.draw(overlaySpriteBatch, text, x, y);
		}
		
		overlaySpriteBatch.disableBlending();
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
	public PolygonSpriteBatch getPolygonSpriteBatch()
	{
		// NA
		return null;
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
	
	@Override
	public void screenToWorld(JCVector2f toProject, float targetZ, JCVector3f projected)
	{
		// TODO
	}
	
	@Override
	public void worldToScreen(JCVector3f projected, JCVector2f destination)
	{
		// TODO
	}
}
