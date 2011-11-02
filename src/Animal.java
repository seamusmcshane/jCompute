import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;


public class Animal extends Box
{
	Geometry myGeo;
	Material myMat;
	ColorRGBA myCol;
	
	public Animal(AssetManager assetManager)
	{
		super();
	}

	public Animal(float x, float y, float z)
	{
		super(x, y, z);
	}

	public Animal(AssetManager assetManager,Vector3f center, float x, float y, float z)
	{
		super(center, x, y, z);
		init(assetManager);
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

