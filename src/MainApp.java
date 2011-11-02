import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.material.Material;

public class MainApp extends SimpleApplication
{

	static int	size;
	static MainApp mainapp;
	
	public static void main(String[] args)
	{
		mainapp = new MainApp();
		mainapp.start();
		size = 100;
	}

	@Override
	public void simpleInitApp()
	{
		setupCam();
		gridSetup(size,1);
		worldbondary(size);
		setupLight();
		addAnimals(1);
	}

	@Override
	public void simpleUpdate(float tpf)
	{
		/* Interact with game events in the main loop */
		//boxgeom.rotate(0, 0.1f, 0);
		
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
		
		Animal a1 = new Animal(assetManager,new Vector3f(0,0,0), 0.5f, 1, 0.5f);
		a1.move(new Vector3f(size/2,1,size/2));
		rootNode.attachChild(a1.getGeo());
		
	}

	/* The view */
	private void setupCam()
	{
		cam.setFrustumFar(size*2);
		flyCam.setEnabled(true);
		flyCam.setMoveSpeed(100);
		//cam.setLocation(new Vector3f(size/2, size,0));
		cam.setLocation(new Vector3f(0, 100,0));
		cam.lookAt(new Vector3f(size/2, 0, size/2), Vector3f.UNIT_Y);

		/* Bloom Filter */
		FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
		BloomFilter bf=new BloomFilter(BloomFilter.GlowMode.Objects);
		bf.setBlurScale(1.5f);
		bf.setBloomIntensity(2f);
		bf.setExposureCutOff(2f);
		fpp.addFilter(bf);
		viewPort.addProcessor(fpp);	
		
	}

	/* Lighting */
	private void setupLight()
	{
	    // We add light so we see the scene
	    AmbientLight al = new AmbientLight();
	    al.setColor(ColorRGBA.White.mult(1.3f));
	    rootNode.addLight(al);
	}
	
	/* The Sandbox Edges */
	private void worldbondary(int size)
	{
		ColorRGBA boundaryColor = ColorRGBA.White;
		
		/* X,Y,Z, SizeX,SizeY,SizeZ */
		
		/*
		 *  Corners
		 */
		Box brightCorner = new Box(new Vector3f(0, 0, 0), 0.5f, 1, 0.5f);	
		Geometry brightCornerGeom = new Geometry("brightCorner", brightCorner);
		Material brightCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		brightCornermat.setColor("Color", ColorRGBA.Red);
		brightCornerGeom.setMaterial(brightCornermat);
		rootNode.attachChild(brightCornerGeom);		

		Box bleftCorner = new Box(new Vector3f(size, 0, 0), 0.5f, 1, 0.5f);	
		Geometry bleftCornerGeom = new Geometry("bleftCorner",  bleftCorner);
		Material bleftCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		bleftCornermat.setColor("Color", ColorRGBA.Orange);
		bleftCornerGeom.setMaterial(bleftCornermat);
		rootNode.attachChild(bleftCornerGeom);	
		
		
		Box tleftCorner = new Box(new Vector3f(size, 0, size), 0.5f, 1, 0.5f);	
		Geometry tleftCornerGeom = new Geometry("tleftCorner",  tleftCorner);
		Material tleftCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		tleftCornermat.setColor("Color", ColorRGBA.Green);
		tleftCornerGeom.setMaterial(tleftCornermat);
		rootNode.attachChild(tleftCornerGeom);		
		
		Box trightCorner = new Box(new Vector3f(0, 0, size), 0.5f, 1, 0.5f);	
		Geometry trightCornerGeom = new Geometry("trightCorner", trightCorner);
		Material trightCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		trightCornermat.setColor("Color", ColorRGBA.Yellow);
		trightCornerGeom.setMaterial(trightCornermat);
		rootNode.attachChild(trightCornerGeom);		
		
		/*
		 * Side
		 */
		
		Box bottom = new Box(new Vector3f(size/2, 0, 0), (size/2)-0.5f, 1, 0.5f);	
		Geometry bottomGeom = new Geometry("bottom", bottom);
		Material bottomMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		bottomMat.setColor("Color", boundaryColor);
		bottomMat.setColor("GlowColor", boundaryColor);
		bottomGeom.setMaterial(bottomMat);
		rootNode.attachChild(bottomGeom);	
	
				
		Box top = new Box(new Vector3f(size/2, 0, size), (size/2)-0.5f, 1, 0.5f);	
		Geometry topGeom = new Geometry("top", top);
		Material topMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		topMat.setColor("Color",boundaryColor);
		topMat.setColor("GlowColor", boundaryColor);
		topGeom.setMaterial(topMat);
		rootNode.attachChild(topGeom);	

		
		Box left = new Box(new Vector3f(size, 0, size/2),  0.5f, 1, (size/2)-0.5f);	
		Geometry leftGeom = new Geometry("left", left);
		Material leftMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		leftMat.setColor("Color", boundaryColor);
		leftMat.setColor("GlowColor", boundaryColor);
		leftGeom.setMaterial(leftMat);
		rootNode.attachChild(leftGeom);		

		

		Box right = new Box(new Vector3f(0, 0, size/2),  0.5f, 1, (size/2)-0.5f);	
		Geometry rightGeom = new Geometry("right", right);
		Material rightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		rightMat.setColor("Color", boundaryColor);
		rightMat.setColor("GlowColor", boundaryColor);
		rightGeom.setMaterial(rightMat);
		rootNode.attachChild(rightGeom);		

	}


	/* Setup Grid */
	private void gridSetup(int size,float interval)
	{
		/* X,Y,Z, SizeX,SizeY,SizeZ */
		Grid grid = new Grid(size+2, size+2, interval);
		Geometry gridgeom = new Geometry("grid", grid);
		Material gridmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		gridmat.setColor("Color", ColorRGBA.Blue);
		gridmat.setColor("GlowColor", ColorRGBA.Blue);
		gridgeom.setMaterial(gridmat);
		rootNode.attachChild(gridgeom);
		/* 0,0,0 */
		gridgeom.move(new Vector3f(-0.5f,-0.8f,-0.5f));

	}

}
