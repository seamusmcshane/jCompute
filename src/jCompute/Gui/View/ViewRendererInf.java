package jCompute.Gui.View;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface ViewRendererInf
{
	public boolean needsGLInit();
	public void glInit();
	public SpriteBatch getSpriteBatch();
	public ShapeRenderer getShapeRenderer();
	public BitmapFont getFont();
	public ViewCam getViewCam();
	public Camera getCamera();
	public void updateViewPort(int width,int height);
	public void render();	
	public boolean doInput();
	public void setMultiplexer(InputMultiplexer inputMultiplexer);
}
