import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.material.Material;

public class MainApp extends SimpleApplication
{

	World world;
	BulletAppState bulletAppState; /* Physics */
	
	static int size;
	static MainApp mainapp;
	Agent a1,a2,a3;

	Boolean follow=false;
	Boolean track=false;
	
	
	BitmapText text;
	BitmapFont font;
	
	public static void main(String[] args)
	{
		AppSettings settings= new AppSettings(true);
		
		settings.setTitle("AlifeSim");
		settings.setVSync(false);
		settings.setResolution(1280, 720);
		settings.setFrameRate(60);

		mainapp = new MainApp();
		
		mainapp.setShowSettings(false); 
		mainapp.setSettings(settings);
		
		
		mainapp.start();
		
		size = 64;
		
	}

	@Override
	public void simpleInitApp()
	{
		bulletAppState = new BulletAppState();
		
	    stateManager.attach(bulletAppState);
		
	    bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0f,0f,0f));
	    
	    bulletAppState.getPhysicsSpace().enableDebug(assetManager);
	    
		setupCam();

		setUpWorld(size);

		setupLight();
		
		addAnimals(1);
		
		setUpKeys();
		
		font = assetManager.loadFont("Interface/Fonts/Default.fnt");
		text = new BitmapText(font,false);
		text.setSize(40f);
		text.setText("Init");
		text.setLocalTranslation(50f, 100f, 0f);
		
		guiNode.attachChild(text);

		//text.rotate(-180f, 0f, 0f);
		
	}

	public void setUpWorld(int size)
	{
		
		world = new World(size,1, rootNode, assetManager, bulletAppState);
		
	}
	
	@Override
	public void simpleUpdate(float tpf)
	{
		/* Interact with game events in the main loop */
		// boxgeom.rotate(0, 0.1f, 0);
				
		if(follow==true)
		{
			cam.setLocation(a1.getGeo().getLocalTranslation());
			cam.setRotation(a1.getGeo().getLocalRotation());
		}
		else
		{
			cam.lookAt(new Vector3f(size / 2, 1, size / 2), Vector3f.UNIT_Y);			
		}
		
		text.setText("World " + world.getlabel() + "\n" + "Agent :" + a1.getlabel());
		//cam.setLocation(a1.getGeo().getLocalTranslation());

	}

	@Override
	public void simpleRender(RenderManager rm)
	{
		/*
		 * (optional) Make advanced modifications to frameBuffer and scene
		 * graph.
		 */
	}

	private void addAnimals(int num)
	{

		a1 = new Agent(assetManager, 0.5f, 0.5f,bulletAppState, world);
		a1.setLoc(new Vector3f(size / 2, 1, size / 2));
		rootNode.attachChild(a1.getGeo());
	}

	/* The view */
	private void setupCam()
	{
		cam.setFrustumFar(size * 3);

		flyCam.setEnabled(false);
		flyCam.setMoveSpeed(10);
		
		
		// cam.setLocation(new Vector3f(size/2, size,0));
		cam.setLocation(new Vector3f(size/2, size*1.5f, size/2));
		
		cam.lookAt(new Vector3f(size / 2, 0, size / 2), Vector3f.UNIT_Y);

		/* Bloom Filter */
		/*FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.Objects);
		bf.setBlurScale(1.5f);
		bf.setBloomIntensity(2f);
		bf.setExposureCutOff(2f);
		fpp.addFilter(bf);
		viewPort.addProcessor(fpp);*/

	}

	/* Lighting */
	private void setupLight()
	{
		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);
		
		
	}

	private void setUpKeys()
	{
		// You can map one or several inputs to one named action
		
		/* Manual Movement */
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));

		/* Follow On/Off */
		inputManager.addMapping("F", new KeyTrigger(KeyInput.KEY_F));
		inputManager.addMapping("T", new KeyTrigger(KeyInput.KEY_T));
		
		/* Camera Selection */
		inputManager.addMapping("KP_0", new KeyTrigger(KeyInput.KEY_0));
		inputManager.addMapping("KP_1", new KeyTrigger(KeyInput.KEY_1));
		inputManager.addMapping("KP_2", new KeyTrigger(KeyInput.KEY_2));
		inputManager.addMapping("KP_3", new KeyTrigger(KeyInput.KEY_3));
		
		// Add the names to the action listener.
		/*inputManager.addListener(actionListener, new String[]
		{"Pause"});*/
		
		inputManager.addListener(analogListener, new String[]
		{"Up", "Down", "Left", "Right" , "KP_0" , "KP_1" , "KP_2" , "KP_3" , "F" ,"T"});

	}

	private void camMode(String mode)
	{
				
		if(mode.equals("Follow"))
		{
			follow=true;
			track=false;
		}
		
		if(mode.equals("Track"))
		{
			cam.setLocation(new Vector3f(size/2, 25, -40));

			follow=false;
			track=true;
		}
		
	}
	private AnalogListener analogListener = new AnalogListener()
	{
		public void onAnalog(String name, float value, float tpf)
		{

			if(name.equals("Up"))
			{
				a1.moveUp();
			}
			if(name.equals("Down"))
			{
				a1.moveDown();
			}
			if(name.equals("Left"))
			{
				a1.turnLeft();
			}
			if(name.equals("Right"))
			{
				a1.turnRight();
			}
			
			if(name.equals("F"))
			{
			
				camMode("Follow");
				
			}			

			if(name.equals("T"))
			{
				camMode("Track");
			}	
			
			if(name.equals("KP_0"))
			{
				cam.setLocation(new Vector3f(size/2, size*1.5f, size/2));
			}
			
			if(name.equals("KP_1"))
			{
				cam.setLocation(new Vector3f(size/2, 25, -40));
			}
			
			if(name.equals("KP_2"))
			{
				cam.setLocation(new Vector3f(size/2, 50, -40));
			}
			
			if(name.equals("KP_3"))
			{
				cam.setLocation(new Vector3f(size/2, 75, -40));
			}
			
		}
	};

}
