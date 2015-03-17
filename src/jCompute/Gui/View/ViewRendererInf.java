package jCompute.Gui.View;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface ViewRendererInf
{
	public SpriteBatch getSpriteBatch();
	public ShapeRenderer getShapeRenderer();
	public BitmapFont getFont();
	public ViewCam getViewCam();
	public void updateCamera(Camera cam);
	public Camera getCamera();
	public void glInit();
	public boolean needsGLInit();
	public void render();	
	public boolean doInput();
}
