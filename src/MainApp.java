import java.awt.Color;

import com.threed.jpct.*;
import com.threed.jpct.util.Light;
import com.threed.jpct.util.ShadowHelper;
public class MainApp
{
	private static float PI = (float) Math.PI;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		World world = new World();
		World sky = new World();
		world.setAmbientLight(30, 30, 30);
		sky.setAmbientLight(255, 255, 255);
		world.getLights().setRGBScale(Lights.RGB_SCALE_2X);
		sky.getLights().setRGBScale(Lights.RGB_SCALE_2X);
	
		/*	FrameBuffer */
		FrameBuffer buffer = new FrameBuffer(800, 600,FrameBuffer.SAMPLINGMODE_NORMAL);
		buffer.disableRenderer(IRenderer.RENDERER_SOFTWARE);
		buffer.enableRenderer(IRenderer.RENDERER_OPENGL);		
		
		/* Box */
		Object3D box = Primitives.getBox(13f, 2f);
		box.setAdditionalColor(new Color(100,100,0));
		box.setEnvmapped(Object3D.ENVMAP_ENABLED);   
		box.translate(0f, 0f, 25f);
		world.addObject(box);		

		
		/* Plane */
		Object3D plane = Primitives.getPlane(20, 30);
		plane.rotateX(PI / 2f);
		plane.setAdditionalColor(new Color(50,100,0));
		plane.setSpecularLighting(true);
		plane.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);	
		plane.translate(0f, 0f, 0f);
		world.addObject(plane);

		/* Build World */
		world.buildAllObjects();		
		
		
		world.getCamera().setPosition(50, -50, -25);
		world.getCamera().lookAt(box.getTransformedCenter());

		/* Projector */
		Projector projector = new Projector();
		projector.setFOV(1.5f);
		projector.setYFOV(1.5f);
		
		/* Shadows */
		ShadowHelper sh = new ShadowHelper(world, buffer, projector, 2048);
		sh.setCullingMode(false);
		sh.setAmbientLight(new Color(30, 30, 30));
		sh.setLightMode(true);
		sh.setBorder(1);

		sh.addCaster(box);
		sh.addReceiver(plane);		

		Light sun = new Light(world);
		sun.setIntensity(250, 250, 250);
		sun.setAttenuation(100);

		while (!org.lwjgl.opengl.Display.isCloseRequested())
		{
			box.rotateY(0.01f);

			projector.lookAt(plane.getTransformedCenter());
			
			sun.setPosition(projector.getPosition());
			sh.updateShadowMap();
			buffer.clear();
			
			buffer.setPaintListenerState(false);
			sky.renderScene(buffer);
			sky.draw(buffer);
			buffer.setPaintListenerState(true);
			sh.drawScene();
			buffer.update();
			buffer.displayGLOnly();
			
			
			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
				
			}
			
		}
		buffer.disableRenderer(IRenderer.RENDERER_OPENGL);
		buffer.dispose();
		System.exit(0);

	}

}
