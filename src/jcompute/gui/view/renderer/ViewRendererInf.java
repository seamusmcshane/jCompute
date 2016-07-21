package jcompute.gui.view.renderer;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

import jcompute.gui.view.View;
import jcompute.math.geom.JCVector2f;
import jcompute.math.geom.JCVector3f;

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
	
	public void screenToWorld(JCVector2f toProject, float targetZ, JCVector3f projected);
	void worldToScreen(JCVector3f projected, JCVector2f destination);
}
