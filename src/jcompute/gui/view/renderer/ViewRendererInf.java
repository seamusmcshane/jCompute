package jcompute.gui.view.renderer;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import jcompute.gui.view.View;

public interface ViewRendererInf
{
	public boolean needsGLInit();
	public void glInit(View view);
	
	public SpriteBatch getSpriteBatch();
	public ShapeRenderer getShapeRenderer();
	
	public Texture getTexture(int id);
	public int getTextureSize(int id);
	public Pixmap getPixmap(int num);

	public void resetViewCam();
	
	public Camera getCamera();
	
	public void updateViewPort(int width,int height);
	
	public void render();
	
	public boolean doInput();
	
	public void setMultiplexer(InputMultiplexer inputMultiplexer);
	
	public void cleanup();
	
	public int getHeight();
	public int getWidth();
}