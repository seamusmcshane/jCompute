package tools.SurfaceChart;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
	public Camera cam;
		
	private DirectionalLight light1 = new DirectionalLight();
	private DirectionalLight light2 = new DirectionalLight();
	private DirectionalLight light3 = new DirectionalLight();
	private DirectionalLight light4 = new DirectionalLight();
		
	private float amb = .3f;
	
	private float lightScale = 1000f;
	
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

		cam = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		/*
		cam = new OrthographicCamera(640,480);
		cam.translate(0, 0, 10000f);
		cam.far = 102420f;
*/
		// Surface
		barSurface = new BarSurface(cam,20*20,pallete);
		
		cam.position.set(0, -barSurface.getSize(), 1000);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 10240f;
		cam.update();
		

		CameraInputController camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);
	}

	@Override
	public void render()
	{
		cam.rotateAround(new Vector3(0,0,0), new Vector3(0,0,1f), 0.4f);
		cam.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.graphics.getGL20().glClearColor( 1, 1, 1, 1 );
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

		barSurface.render(cam,environment);
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