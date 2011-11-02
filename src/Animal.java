import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;


public class Animal extends Box
{
	
	static float HEIGHT=0.2f;
	
	Geometry myGeo;
	Material myMat;
	ColorRGBA myCol;
	
	Vector3f forward;
	Vector3f backward;
	Vector3f current;
	
	Quaternion rotation;
	
	public Animal(AssetManager assetManager)
	{
		super();
	}

	public Animal(float x, float y, float z)
	{
		super(x, y, z);
	}

	public Animal(AssetManager assetManager,float x, float z)
	{
		super(new Vector3f(0, 0, 0), x, HEIGHT, z);
		init(assetManager);
		
		forward = new Vector3f(0,0,0.1f);
		
		backward = new Vector3f(0,0,-0.1f);
		
		current = new Vector3f(0,0,0);
		
	}

	public Animal(Vector3f min, Vector3f max)
	{
		super(min, max);
	}

	private void init(AssetManager assetManager)
	{
		/* Geometry */
		myGeo = new Geometry("Box", this);
		
		/* Color */
		myMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		myGeo.setMaterial(myMat);
		
		myMat.setColor("Color", ColorRGBA.White);
		myMat.setColor("GlowColor", ColorRGBA.White);
		
		/* X,Y,Z, SizeX,SizeY,SizeZ */
//		/Box box = new Box(new Vector3f(size/2,0,size/2), 0.5f, 1, 0.5f);
		//Box box = new Box(new Vector3f(0,0,0), 0.5f, 1, 0.5f);
		//Geometry boxgeom = new Geometry("Box", box);
		//boxgeom.move(new Vector3f(size/2,0,size/2));

		//Material boxmat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	    //Material boxmat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
	    //boxmat.setFloat("Shininess", 5f); // [1,128]
	    
		//boxmat.setColor("Color", ColorRGBA.White);
		//boxmat.setColor("GlowColor", ColorRGBA.White);

		//boxgeom.setMaterial(boxmat);

		//rootNode.attachChild(boxgeom);
		
		
	}
	
	
	public void moveUp()
	{		
	
		rotation = myGeo.getWorldRotation();
			
		current = rotation.mult(forward);

		myGeo.move(current);
		
	}
	public void moveDown()
	{
		rotation = myGeo.getWorldRotation();
		
		current = rotation.mult(backward);

		myGeo.move(current);
	}
	public void turnLeft()
	{
		myGeo.rotate(0, 0.1f, 0);
	}
	public void turnRight()
	{
		myGeo.rotate(0, -0.1f, 0);
	}
	
	public void move(Vector3f vector)
	{
		myGeo.move(vector);
	}
	
	/* For Adding to Scene */
	public Geometry getGeo()
	{
		return myGeo;
	}
	
	private void display()
	{
		
	}
	
	

	
}

