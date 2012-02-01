import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;


public class World
{
	/* Grid */
	Grid grid;
	Geometry gridGeom;
	Material gridMat;
	
	/* Border */
	Box top,bottom,right,left;
	Geometry topGeom,bottomGeom,rightGeom,leftGeom;
	/* Corners */
	Box topLeft,topRight,bottomLeft,bottomRight;
	

	public World(int size, int gridStep, Node rootNode, AssetManager assetManager,BulletAppState bulletAppState)
	{

		gridSetup(size, gridStep, rootNode, assetManager,bulletAppState);
		
		generateBoundaries(size, gridStep, rootNode, assetManager,bulletAppState);
	}
	
	private void generateBoundaries(int size, float gridStep, Node rootNode, AssetManager assetManager,BulletAppState bulletAppState)
	{
			ColorRGBA boundaryColor = ColorRGBA.White;

			float height = 1f;
			float floor= height;
			float width=1f;
			/* X,Y,Z, SizeX,SizeY,SizeZ */

			/*
			 * Corners
			 */
			Box brightCorner = new Box(new Vector3f(0, floor, 0), width, height, width);
			Geometry brightCornerGeom = new Geometry("brightCorner", brightCorner);
			Material brightCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			brightCornermat.setColor("Color", ColorRGBA.Red);
			brightCornerGeom.setMaterial(brightCornermat);
			rootNode.attachChild(brightCornerGeom);
						
			Box bleftCorner = new Box(new Vector3f(size, floor, 0), width, height, width);
			Geometry bleftCornerGeom = new Geometry("bleftCorner", bleftCorner);
			Material bleftCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			bleftCornermat.setColor("Color", ColorRGBA.Orange);
			bleftCornerGeom.setMaterial(bleftCornermat);
			rootNode.attachChild(bleftCornerGeom);

			Box tleftCorner = new Box(new Vector3f(size, floor, size), width, height, width);
			Geometry tleftCornerGeom = new Geometry("tleftCorner", tleftCorner);
			Material tleftCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			tleftCornermat.setColor("Color", ColorRGBA.Green);
			tleftCornerGeom.setMaterial(tleftCornermat);
			rootNode.attachChild(tleftCornerGeom);

			Box trightCorner = new Box(new Vector3f(0, floor, size), width, height, width);
			Geometry trightCornerGeom = new Geometry("trightCorner", trightCorner);
			Material trightCornermat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			trightCornermat.setColor("Color", ColorRGBA.Yellow);
			trightCornerGeom.setMaterial(trightCornermat);
			rootNode.attachChild(trightCornerGeom);

			/*
			 * Side
			 */

			bottom = new Box(new Vector3f(size/2,floor,0), (size / 2) - width, height, width);
			bottomGeom = new Geometry("bottom", bottom);
			Material bottomMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			bottomMat.setColor("Color", boundaryColor);
			bottomMat.setColor("GlowColor", boundaryColor);
			bottomGeom.setMaterial(bottomMat);
			rootNode.attachChild(bottomGeom);		
			
			top = new Box(new Vector3f(size / 2, floor, size), (size / 2) - width, height, width);
			topGeom = new Geometry("top", top);
			Material topMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			topMat.setColor("Color", boundaryColor);
			topMat.setColor("GlowColor", boundaryColor);
			topGeom.setMaterial(topMat);
			rootNode.attachChild(topGeom);

			left = new Box(new Vector3f(size, floor, size / 2), width, height, (size / 2) - width);
			leftGeom = new Geometry("left", left);
			Material leftMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			leftMat.setColor("Color", boundaryColor);
			leftMat.setColor("GlowColor", boundaryColor);
			leftGeom.setMaterial(leftMat);
			rootNode.attachChild(leftGeom);

			right = new Box(new Vector3f(0, floor, size / 2), width, height, (size / 2) - width);
			rightGeom = new Geometry("right", right);
			Material rightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			rightMat.setColor("Color", boundaryColor);
			rightMat.setColor("GlowColor", boundaryColor);
			rightGeom.setMaterial(rightMat);
			rootNode.attachChild(rightGeom);
	
			
	}
		
	/* Setup Grid */
	private void gridSetup(int size, float gridStep, Node rootNode, AssetManager assetManager, BulletAppState bulletAppState)
	{
		/* X,Y,Z, SizeX,SizeY,SizeZ */
		grid = new Grid(size, size, gridStep);
		
		gridGeom = new Geometry("grid", grid);
		
		gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		
		gridMat.setColor("Color", ColorRGBA.Blue);
		
		gridMat.setColor("GlowColor", ColorRGBA.Blue);
		
		gridGeom.setMaterial(gridMat);
		
		rootNode.attachChild(gridGeom);

		/* 0,0,0 */
		gridGeom.move(new Vector3f(0.5f, 0f, 0.5f));
	}
	
	public boolean collidesWith(Vector3f vector)
	{
		return (topGeom.getWorldBound().contains(vector));		
	}
	
	public String getlabel()
	{
		return "Current X: " + topGeom.getWorldBound().getCenter().getX() + " Y: " + topGeom.getWorldBound().getCenter().getY() + " Z: " + topGeom.getWorldBound().getCenter().getZ() + "";
	}

}
