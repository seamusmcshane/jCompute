package tools.SurfaceChart;

import jCompute.Gui.View.Input.OrbitalCameraInputController;
import jCompute.Gui.View.Misc.Palette;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class SurfacePlotEnv implements ApplicationListener
{
	public Environment environment;
	public PerspectiveCamera cam;
	
	private DirectionalLight light1 = new DirectionalLight();
	private DirectionalLight light2 = new DirectionalLight();
	private DirectionalLight light3 = new DirectionalLight();
	private DirectionalLight light4 = new DirectionalLight();
	private DirectionalLight light5 = new DirectionalLight();
	
	private float amb = 0.3f;
	private float brightSide = 0.80f - amb;
	private float darkSide = 0.45f - amb;
	private float topSide = 0.85f - amb;
	
	private float lightScale = 1f;
	
	private BarSurfaceOri barSurface;
	private ColorMap colorMap;
	
	private OrbitalCameraInputController camController;
	
	private float width;
	private float height;
	
	// test
	private SpriteBatch batch;
	private TextureRegion tr;
	
	public SurfacePlotEnv(float width, float height)
	{
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void create()
	{
		
		environment = new Environment();
		
		environment.add(light1);
		environment.add(light2);
		environment.add(light3);
		environment.add(light4);
		environment.add(light5);
		
		light1.set(brightSide, brightSide, brightSide, 0, lightScale, 0);
		light2.set(brightSide, brightSide, brightSide, 0, -lightScale, 0);
		light3.set(darkSide, darkSide, darkSide, lightScale, 0, 0);
		light4.set(darkSide, darkSide, darkSide, -lightScale, 0, 0);
		light5.set(topSide, topSide, topSide, 0, 0, -lightScale);
		
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, amb, amb, amb, 1f));
		
		cam = new PerspectiveCamera(75f, (width / height), 1f);
		
		/*
		 * cam = new OrthographicCamera(640,480);
		 * cam.translate(0, 0, 10000f);
		 * cam.far = 102420f;
		 */
		
		int[] pallete = Palette.HUEPalete(true, 2048);
		
		// Surface
		barSurface = new BarSurfaceOri(cam, 20 * 20, pallete);
		
		batch = new SpriteBatch();
		colorMap = new ColorMap(barSurface.getZmin(), barSurface.getZmax(), pallete);
		
		tr = colorMap.getTextureRegion();
		
		cam.near = 0.1f;
		cam.far = 10240f;
		cam.update();
		
		camController = new OrbitalCameraInputController(cam, new float[]
		{
			0, 0, 0
		}, 25f);
		Gdx.input.setInputProcessor(camController);
	}
	
	@Override
	public void render()
	{
		camController.update();
		// cam.rotateAround(new Vector3(0,0,0), new Vector3(0,0,1f), 0.4f);
		// cam.update();
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.graphics.getGL20().glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		barSurface.render(cam, environment);
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		batch.begin();
		
		batch.draw(tr, 0.9f, 0.1f, 0.05f, 0.75f);
		
		batch.end();
		
	}
	
	@Override
	public void dispose()
	{
		barSurface.dispose();
	}
	
	@Override
	public void resize(int width, int height)
	{
		
	}
	
	@Override
	public void pause()
	{
		
	}
	
	@Override
	public void resume()
	{
		
	}
}