package jCompute.Gui.View;

import jCompute.Gui.View.Graphics.A2DCircle;
import jCompute.Gui.View.Graphics.A2DLine;
import jCompute.Gui.View.Graphics.A2DRectangle;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Gui.View.Graphics.A2RGBA;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class View implements ApplicationListener, InputProcessor
{
	private JPanel basePanel;
	
	/** The Drawing Canvas's */
	private LwjglAWTCanvas glCanvas;
	
	private int width;
	private int height;
	
	/** Cameara and Shape Renderer */
	private PerspectiveCamera viewCam;
	
	/** ViewTarget Reference */
	private ViewTarget target;
	private Semaphore viewLock = new Semaphore(1);

	private String viewTitle = "";
	
	private int defaultFrameRate = 60;

	/** Draw the View range of the agents */
	private boolean viewRangeDrawing = false;

	/** Draw Views */
	private boolean viewsDrawing = false;
		
	/* Mouse */
	/** Stores the mouse vector across updates */
	private A2DVector2f mousePos = new A2DVector2f(0, 0);
	
	private boolean button1Pressed;
	
	private BitmapFont overlayFont;
	private SpriteBatch overlaySpriteBatch;

	
	private float defaultLineWidth = 0.10f;
		
	/* FBO / BBO */
	private SpriteBatch bboSpriteBatch;
	private ShapeRenderer bboShapeRenderer;
	
	private FrameBuffer fbo;
	private SpriteBatch fboSpriteBatch;
	private ShapeRenderer fboShapeRenderer;
	
	private SpriteBatch currentSpriteBatch;
	private BitmapFont font;
	private ShapeRenderer currentShapeRenderer;
	
	private Pixmap pTemp;
	private Texture tTemp;
	
	private ShaderProgram shaderProgram;
	private Mesh mesh;
	
	public View()
	{
		System.out.println("Created View");
		
		LwjglApplicationConfiguration.disableAudio = true;
		
		basePanel = new JPanel();
		
		basePanel.setPreferredSize(new Dimension(1024, 1024));

		basePanel.setLayout(new BorderLayout());
		
		glCanvas = new LwjglAWTCanvas(this, true);	
		
		basePanel.add(glCanvas.getCanvas(),BorderLayout.CENTER);
		
		Display.setVSyncEnabled(true);
		Display.setSwapInterval(1);
		
		glCanvas.getInput().setInputProcessor(this);
		
		viewCam = new PerspectiveCamera(90,640, 480);
		viewCam.near = 0.1f;
		viewCam.far = 30000f;
		
		resetCamera();
		//viewCam.setToOrtho(true, 640, 480);
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
		bboShapeRenderer = new ShapeRenderer();
		fboShapeRenderer = new ShapeRenderer();
        
		bboSpriteBatch = new SpriteBatch();
		fboSpriteBatch = new SpriteBatch();
		
		currentShapeRenderer = bboShapeRenderer;
		
		currentSpriteBatch = bboSpriteBatch;
		font = new BitmapFont();
		font.setColor(Color.WHITE);
        
		overlayFont = new BitmapFont();
		overlayFont.setColor(Color.WHITE);
		
		overlaySpriteBatch = new SpriteBatch();
		
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888,2048, 2048,false);
        
		//viewCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		// mshade
		String vertexShader = 
				"attribute vec4 vPosition; 		\n" + 
				"void main()					\n" +
				"{								\n" +
				"	gl_Position = vPosition;	\n" +
				"}								\n";
		
		String fragmentShader = 
			"#ifdef GL_ES 								\n"+
			"precision mediump float;					\n"+
			"#endif 									\n"+
			"void main()								\n"+
			"{											\n"+
			"	gl_FragColor = vec4(1.0,0.0,0.0,1.0);	\n"+
			"}";
        

       
		shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
		
		 if (!shaderProgram.isCompiled())
			 throw new GdxRuntimeException(shaderProgram.getLog());
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
		
		if(target!=null)
		{
			if(target.hasViewCam())
			{
				viewCam.position.x = target.getViewCam().getCamPosX();
				viewCam.position.y = target.getViewCam().getCamPosY();

				viewCam.position.z = target.getViewCam().getCamZoom();				
			}

			viewCam.update();
			
			target.draw(this,viewRangeDrawing,viewsDrawing);
			
			viewTitle = target.getInfo();
			
		}
		else
		{
			viewTitle = "None";
		}
		
		drawOverlayText(10,Gdx.graphics.getHeight()-10,viewTitle);

		viewLock.release();
	}

	@Override
	public void resize(int width, int height)
	{		
		//viewCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		resetCamera();
	}
	
	@Override
	public void resume()
	{
		
	} 
	
	private void blankFBO()
	{
		fbo.begin();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		fbo.end();
	}
	
	private void targetFBOStart()
	{
		currentShapeRenderer = fboShapeRenderer;
		
		currentSpriteBatch = fboSpriteBatch;
		
		fbo.begin();
		
	}
	
	public void drawOverlayText(float x,float y,String text)
	{
		overlaySpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		overlaySpriteBatch.begin();
			overlayFont.draw(overlaySpriteBatch, text, x, y);
		overlaySpriteBatch.end();
	}
	
	public void drawText(float x,float y,String text)
	{
		currentSpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		currentSpriteBatch.begin();
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
	
	private void targetFBOStop()
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
		
	public void drawPixelMap(int textureSize, int[] buffer, float x, float y)
	{	
		if(pTemp == null)
		{
			pTemp = new Pixmap(textureSize, textureSize,Format.RGBA8888);
		}
		
		if(tTemp == null)
		{
			tTemp = new Texture(pTemp);
			tTemp.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		
		if(textureSize!=tTemp.getHeight() || textureSize!=tTemp.getWidth())
		{
			pTemp.dispose();
			tTemp.dispose();
			
			pTemp = new Pixmap(textureSize, textureSize,Format.RGBA8888);
			tTemp = new Texture(pTemp);
			tTemp.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		
    	ByteBuffer pixels = pTemp.getPixels();
		
    	pixels.asIntBuffer().put(buffer);
    	
    	tTemp.draw(pTemp, 0,0);


    	float min = Math.min(Gdx.graphics.getWidth(),  Gdx.graphics.getHeight()-100);
    	
    	float scale = min/textureSize;
    	
    	currentSpriteBatch.begin();
    	currentSpriteBatch.draw(tTemp,x,y,textureSize*scale,textureSize*scale);
    	currentSpriteBatch.end();    	
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
	
	public void drawFilledCircleBatch(A2DCircle[] circles, A2RGBA[] colors)
	{
		currentShapeRenderer.begin(ShapeType.Filled);
		int size = circles.length;
		for(int c=0;c<size;c++)
		{
			if(viewCam.frustum.pointInFrustum(new Vector3(circles[c].getX(),circles[c].getY(),0)))
			{
				currentShapeRenderer.setColor(colors[c].getRed(), colors[c].getGreen(), colors[c].getBlue(), colors[c].getAlpha());
				currentShapeRenderer.circle(circles[c].getX(),circles[c].getY(),circles[c].getRadius());
			}
		}

		currentShapeRenderer.end();
	}
	
	public void drawTransparentFilledCircle(A2DCircle circle,A2RGBA color, float transparency)
	{
		if(!viewCam.frustum.pointInFrustum(new Vector3(circle.getX(),circle.getY(),0)))
		{
			return;
		}
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		currentShapeRenderer.begin(ShapeType.Filled);
		currentShapeRenderer.setColor(color.getRed(), color.getGreen(), color.getBlue(), transparency);
		currentShapeRenderer.circle(circle.getX(),circle.getY(),circle.getRadius());
		currentShapeRenderer.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);

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
	
	public void drawMesh()
	{
        if (mesh == null) 
        {
        	float[] verts = new float[]{ -0.5f, -0.5f, 0,
        			0.5f, -0.5f, 0, 
        			0, 0.5f, 0};
        	
        	mesh = new Mesh(true, 3, 3, new VertexAttribute(VertexAttributes.Usage.Position, 3, "vPosition"));
        	
            mesh.setVertices(verts);
            
            mesh.setIndices(new short[] { 0, 1, 2 });       

            // mesh.scale(100f, 100f, 100f);
            
            CameraInputController camController = new CameraInputController(viewCam);
            Gdx.input.setInputProcessor(camController);
            
            viewCam.lookAt(0,0,0);
        }
        
			shaderProgram.begin();
			//shaderProgram.setUniformMatrix(u_projTrans, viewCam.combined);
			mesh.render(shaderProgram, GL20.GL_TRIANGLES);
			shaderProgram.end();
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
		if(target!=null)
		{			
			target.getViewCam().adjCamZoom(val);
		}
		
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		if(button == 1)
		{
			button1Pressed = true;
			resetCamera();
		}
		else
		{
			mousePos.set(x,y);
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
			mousePos.set(x,y);

			// How much did the mouse move.
			float diffX = previousX - mousePos.getX();
			float diffY = previousY - mousePos.getY();
			
			// -y for when converting from screen to graphics coordinates
			moveCamera(diffX,-diffY);
		}
		
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{	
		if(button1Pressed)
		{
			button1Pressed=false;
		}
		else
		{
			mousePos.set(x,y);
		}
		
		return false;
	}
	
	private void moveCamera(float x, float y)
	{		
		if(target!=null)
		{			
			target.getViewCam().moveCam(x,y);
		}
	}
	
	public void resetCamera()
	{		
		//viewCam.position.set(globalTranslateDefault.getX() + Gdx.graphics.getWidth()/2, globalTranslateDefault.getY() + Gdx.graphics.getHeight()/2, 0);
		
		if(target!=null)
		{			
			// This can be null if a tab is added when no sim is generated and the window is resized
			if(target.hasViewCam())
			{
				target.getViewCam().resetCamZoom();	
				target.getViewCam().resetCamPos(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);	
			}
		}
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
