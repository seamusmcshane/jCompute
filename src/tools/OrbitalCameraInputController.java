package tools;

import org.lwjgl.input.Mouse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class OrbitalCameraInputController implements InputProcessor
{	
	private PerspectiveCamera cam;
	private float[] target = new float[3];
	private float altitude=1000f;
	private float speed;
	
	// Movement
	private int startPos[] = new int[2];
	private int prevPos[] = new int[2];
	
	// FOV
	private float prevFov;
	
	private boolean button0 = false;
	private boolean button1 = false;
	
	private static int defaultPos = -750;
	
	public OrbitalCameraInputController(PerspectiveCamera cam, float[] target,float speed)
	{
		this.cam = cam;
		
		target[0] = target[0];
		target[1] = target[1];
		target[2] = target[2];
		
		this.speed = speed;
		
		// Movement
		startPos[0] = 0; 
		startPos[1] = 0;
		
		reset();
		
		prevFov = cam.fieldOfView;
	}
	
	public void reset()
	{
		cam.up.x = 0;
		cam.up.y = 0;
		cam.up.z = 1;

		cam.position.set(defaultPos, defaultPos, altitude);
		cam.lookAt(target[0],target[1],target[2]);
	}
	
	public OrbitalCameraInputController(PerspectiveCamera cam,float pos, float altitude,float[] target,float speed)
	{
		this.cam = cam;
		
		target[0] = target[0];
		target[1] = target[1];
		target[2] = target[2];
		
		this.speed = speed;
		
		// Movement
		startPos[0] = 0; 
		startPos[1] = 0;
		
		this.altitude = altitude;
		
		cam.position.set(pos, pos, altitude);
		cam.rotate(new Vector3(1,1,0), 90f);
		cam.lookAt(target[0],target[1],target[2]);
		
		prevFov = cam.fieldOfView;
	}
	
	@Override
	public boolean keyDown(int arg0)
	{
		return false;
	}

	@Override
	public boolean keyTyped(char arg0)
	{
		return false;
	}

	@Override
	public boolean keyUp(int arg0)
	{
		return false;
	}

	@Override
	public boolean mouseMoved(int arg0, int arg1)
	{
		return false;
	}

	@Override
	public boolean scrolled(int moved)
	{		
		boolean moveOk = true;
		float minA = 25f;
		float maxA = 1500f;
		
		float camX = cam.position.x;
		float camY = cam.position.y;
		float camZ = cam.position.z;
		
		// If Not rightbutton clicked
		if(!button1)
		{
			if(moved<0)
			{
				altitude=altitude+(speed*4);
			}
			else
			{
				altitude=altitude-(speed*4);
			}
			
			if(altitude>maxA)
			{
				altitude=maxA;
			}

			if(altitude<minA)
			{
				altitude = minA;
			}
			
			if(altitude <= minA || altitude>=maxA)
			{
				moveOk=false;
			}
			else
			{
				camZ = altitude;
			}
		}
		else
		{
			if(moved<0)
			{
				cam.fieldOfView = cam.fieldOfView+1f;
				
				if(cam.fieldOfView > 150f)
				{
					cam.fieldOfView = 150f;
				}
				
			}
			else
			{
				cam.fieldOfView = cam.fieldOfView-1f;
				
				if(cam.fieldOfView < 1)
				{
					cam.fieldOfView = 1;
				}
			}
			
			prevFov = cam.fieldOfView;			
		}

				
		/*if(moveOk)
		{
			float tdis = 0;
			float angle = (float) Math.atan2(camY, camX);
			
			if(moved<0)
			{
				// Rotate Y Polar Axis up
				tdis=(float) Math.sqrt((camX*camX)+(camY*camY));
				tdis=tdis+(stepSize);

				camX = (float) (tdis*Math.cos(angle));
				camY = (float) (tdis*Math.sin(angle));
				
				if(tdis>=1000f)
				{
					tdis = 1000f;
				}
			}
			else
			{
				// Rotate Y Polar Axis Down
				tdis=(float) Math.sqrt((camX*camX)+(camY*camY));
				tdis=tdis-(stepSize);
				
				if(tdis<=100f)
				{
					tdis = 100f;
				}
				
				camX = (float) (tdis*Math.cos(angle));
				camY = (float) (tdis*Math.sin(angle));
			}
		}*/
		
		cam.position.set(camX, camY, camZ);
		
		return true;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		Mouse.setGrabbed(true);
		Gdx.input.setCursorCatched(true);
		
		if(button == 0)
		{
			prevPos[0] = x;
			prevPos[1] = y;

			
			button0 = true;
		}
		
		if(button==1)
		{
			prevPos[1] = y;
			
			//prevFov = cam.fieldOfView;
			
			button1 = true;

		}
		
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		
		float camX = cam.position.x;
		float camY = cam.position.y;
		float camZ = cam.position.z;
		
		// From target
		/*float tdis = 0;
		float angle = (float) Math.atan2(camY, camX);*/
				
		if(button0)
		{			
			if(x>prevPos[0])
			{
				int dis = (x-prevPos[0]);

				cam.rotateAround(new Vector3(target[0],target[1],target[2]), new Vector3(0,0,1f), (dis*speed/100));
			}
			else
			{
				int dis = (prevPos[0]-x);
				cam.rotateAround(new Vector3(target[0],target[1],target[2]), new Vector3(0,0,1f), -(dis*speed/100));
			}
			
			prevPos[0] = x;
			prevPos[1] = y;
		}
		
		return true;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{

		Mouse.setGrabbed(false);
		Gdx.input.setCursorCatched(false);

		if(button==0)
		{
			button0=false;
		}
		
		if(button == 1)
		{
			button1=false;
		}
		
		return true;
	}
	
	public void update()
	{
		//cam.rotateAround(new Vector3(0,0,0), new Vector3(0,0,1f), 0.4f);
		
		cam.lookAt(target[0],target[1],target[2]);
		cam.update();
		
		//System.out.println("X " + cam.position.x + " Y " + cam.position.y + " Z " + cam.position.z);
	}
	
	public void setTarget(float[] target)
	{
		this.target = target;
	}

	public void setAltitude(float altitude)
	{
		this.altitude = altitude;		
	}

}
