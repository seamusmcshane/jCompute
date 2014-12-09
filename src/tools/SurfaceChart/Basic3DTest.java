package tools.SurfaceChart;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class Basic3DTest implements ApplicationListener
{
	public Environment environment;
	public PerspectiveCamera cam;

	private ShapeRenderer shapeRenderer;
	private SpriteBatch shapeBatch;
		
	private DirectionalLight light1 = new DirectionalLight();
	private DirectionalLight light2 = new DirectionalLight();
	private DirectionalLight light3 = new DirectionalLight();
	private DirectionalLight light4 = new DirectionalLight();
		
	private float amb = .3f;
	
	private float lightScale = 100f;
	
	private BarSurface barSurface;
	
	@Override
	public void create()
	{
		environment = new Environment();

		environment.add(light1);		
		environment.add(light2);		
		environment.add(light3);		
		environment.add(light4);	
		
		light1.set(1f,1f,1f,0,lightScale, -lightScale/4);
		light2.set(1f,1f,1f, 0,-lightScale,-lightScale/4);
		light3.set(.5f,.5f,.5f, lightScale,0,-lightScale/4);
		light4.set(.5f,.5f,.5f, -lightScale,0,-lightScale/4);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, amb,amb,amb, 1.0f));
        		
        // Pallete
		HueColorPallete pallete = new HueColorPallete(100);

		// Surface
		barSurface = new BarSurface(100,pallete);

		cam = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0, -barSurface.getSize(), barSurface.getSize());
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 10240f;
		//cam.rotateAround(new Vector3(0,0,0), new Vector3(0,0,1f), 45f);
		cam.update();

		CameraInputController camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		shapeBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		
		Gdx.graphics.setVSync(true);
	}

	@Override
	public void render()
	{
		cam.rotateAround(new Vector3(0,0,0), new Vector3(0,0,1f), .2f);

		cam.update();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Gdx.graphics.getGL10().glClearColor( 1, 1, 1, 1 );
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		barSurface.render(cam, environment);
		

	}

	@Override
	public void dispose()
	{
		shapeRenderer.dispose();
		shapeBatch.dispose();
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