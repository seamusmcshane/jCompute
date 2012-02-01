import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class Agent extends Box
{
	
	static float HEIGHT=0.2f;
	World world;
	
	BoundingVolume bounds; 
	
	Geometry myGeo;
	Material myMat;
	ColorRGBA myCol;
	
	Vector3f forward;
	Vector3f backward;
	
	/* Rotation Vector */
	Vector3f left = new Vector3f(0, -0.1f, 0);
	Vector3f right= new Vector3f(0, 0.1f, 0);
	
	Vector3f current;
	
	Quaternion rotation;

	CapsuleCollisionShape myPhy;
	CharacterControl me;
	
	public Agent(AssetManager assetManager)
	{
		super();
	}

	public Agent(float x, float y, float z)
	{
		super(x, y, z);
	}

	public Agent(AssetManager assetManager,float x, float z,BulletAppState bulletAppState, World world)
	{
		super(new Vector3f(0, 0, 0), x, HEIGHT, z);
			
		init(assetManager,ColorRGBA.White,ColorRGBA.Red,bulletAppState,world);
		
		forward = new Vector3f(0,0,0.1f);
		
		backward = new Vector3f(0,0,-0.1f);
		
		current = new Vector3f(0,0,0);	
	}

	public Agent(AssetManager assetManager,float x, float z, ColorRGBA skin,ColorRGBA glow,BulletAppState bulletAppState, World world)
	{
		super(new Vector3f(0, 0, 0), x, HEIGHT, z);
		
		init(assetManager,skin,glow,bulletAppState, world);
		
		forward = new Vector3f(0,0,0.1f);
		
		backward = new Vector3f(0,0,-0.1f);
		
		current = new Vector3f(0,0,0);	
	}
	
	public Agent(Vector3f min, Vector3f max)
	{
		super(min, max);
	}

	private void init(AssetManager assetManager,ColorRGBA skin,ColorRGBA glow, BulletAppState bulletAppState, World world)
	{

		this.world = world;
				
		/* Geometry */
		myGeo = new Geometry("Box", this);
		
		/* Color */
		myMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		myGeo.setMaterial(myMat);
		
		myMat.setColor("Color", skin);
		myMat.setColor("GlowColor", glow);
		
		myPhy = new CapsuleCollisionShape(0.5f, 0.5f, 1);
	    me = new CharacterControl(myPhy, 0.05f);
	    me.setGravity(0);
	    myGeo.addControl(me);
	    	    
	    bulletAppState.getPhysicsSpace().add(me);
	    
	    //me.setPhysicsLocation(new Vector3f(0, 10, 0));
				
	}
	public void move(Vector3f vector)
	{
		me.setViewDirection(vector);
	}

	
	/* Move in the world relative rotation based forward direction*/
	public void moveUp()
	{		
	
		// World based Rotation Quaternion of object 
		rotation = myGeo.getWorldRotation();
		
		//Rotation Matrix * forward matrix ie Add forward step to current Direction 
		current = rotation.mult(forward);		
		
		//Move to the new location
		this.move(current);

		
	}
	
	/* As moveUp only using Backward Step */
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
	
	public void setLoc(Vector3f vector)
	{
	    me.setPhysicsLocation(vector);
		
			//myGeo.move(vector);
	}
	
	/* For Adding to Scene */
	public Geometry getGeo()
	{
		return myGeo;
	}
	
	public String getlabel()
	{
		return "Current X: " + myGeo.getWorldTranslation().getX() + " Y: " + myGeo.getWorldTranslation().getY() + " Z: " + myGeo.getWorldTranslation().getZ() + "";
	}
}

