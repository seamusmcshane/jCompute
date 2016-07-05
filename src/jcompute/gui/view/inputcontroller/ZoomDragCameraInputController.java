package jcompute.gui.view.inputcontroller;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;

import jcompute.math.FloatingPoint;
import jcompute.math.geom.JCVector2f;
import jcompute.math.geom.MathVector2f;

public class ZoomDragCameraInputController implements InputProcessor
{
	// Target Camera
	private Camera camera;
	
	// Current CamPos
	private JCVector2f camPos;
	
	// Camera Offset (from 0,0)
	private JCVector2f camOffset;
	
	// Current Zoom
	private float camZoom;
	
	// Error Margin for Animation Equality
	private final float ANIMATE_SNAP = 0.5f;
	
	// Stores the mouse vector across updates
	private JCVector2f mouseDragPos = new JCVector2f(0, 0);
	
	// Scale the X/Y passed in by this amount ( 1 / 800 )
	private final float MOVEMENT_DAMPER = 0.00125f;
	
	// Zoom Parameters
	private final float zoomIncr = 50f;
	private float zoomDefault = 800f;
	private float minZoom = zoomIncr;
	private float maxZoom = zoomDefault;
	
	// Mouse Button Status (Left/ Right / Middle)
	private boolean button0Pressed;
	private boolean button1Pressed;
	private boolean button2Pressed;
	
	// Middle Click Mode
	private final int MAX_MIDDLE_BUTTON_MODE;
	private int currentMiddleButtonMode = 0;
	
	// Animation enabled / Required (Right Click)
	private boolean cameraResetAnimate = false;
	
	public ZoomDragCameraInputController(Camera camera, float camPosX, float camPosY, float offsetX, float offsetY, int middleButtonModes)
	{
		this.camera = camera;
		
		camPos = new JCVector2f(camPosX, camPosY);
		
		camZoom = zoomDefault;
		zoomDefault = camZoom;
		
		camOffset = new JCVector2f(offsetX, offsetY);
		
		camPos.add(camOffset);
		
		this.MAX_MIDDLE_BUTTON_MODE = (middleButtonModes > 0) ? middleButtonModes : 0;
	}
	
	public void setZoomLimits(float min, float max)
	{
		minZoom = min;
		maxZoom = max;
	}
	
	public void setCamOffset(float x, float y)
	{
		// Remove old offset
		camPos.sub(this.camOffset);
		
		camOffset.x = x;
		camOffset.x = y;
		
		// Add new
		camPos.add(camOffset);
	}
	
	public void setCamPos(float x, float y)
	{
		camPos.x = x;
		camPos.x = y;
	}
	
	public void adjCamZoom(float zoomAdj)
	{
		cameraResetAnimate = false;
		
		if(zoomAdj > 0)
		{
			camZoom += zoomIncr;
		}
		else
		{
			camZoom -= zoomIncr;
		}
		
		if(camZoom < minZoom)
		{
			camZoom = minZoom;
		}
		
		if(camZoom > maxZoom)
		{
			camZoom = maxZoom;
		}
	}
	
	public boolean reset()
	{
		boolean z = resetZoom(zoomDefault);
		
		return resetCameraXY(0, 0) & z;
	}
	
	private boolean resetZoom(float z)
	{
		// Done if near z +- ANIMATE_SNAP
		if(FloatingPoint.AlmostEqualEpsilon(camZoom, z, ANIMATE_SNAP))
		{
			camZoom = zoomDefault;
			
			return true;
		}
		
		// Distance to target z
		float zoomLen = z - camZoom;
		
		// CamZoom scaled adjusted by scaled distance to z
		camZoom = camZoom + (zoomLen - (zoomLen * 0.80f));
		
		return false;
	}
	
	private boolean resetCameraXY(float x, float y)
	{
		// Remove the offset before the calculation
		camPos.sub(camOffset);
		
		// Done if near x,y +- ANIMATE_SNAP
		if(FloatingPoint.AlmostEqualEpsilon(camPos.x, x, ANIMATE_SNAP) && FloatingPoint.AlmostEqualEpsilon(camPos.y, y, ANIMATE_SNAP))
		{
			camPos.x = 0;
			camPos.y = 0;
			
			// Re-apply offset
			camPos.add(camOffset);
			
			return true;
		}
		
		// Distance Vector
		JCVector2f dv = new JCVector2f(x - camPos.x, y - camPos.y);
		
		// Vector length
		float len = dv.length();
		
		// Distance unit vector
		JCVector2f du = MathVector2f.Unit(dv);
		
		// Scaled length
		float dl = (len * 0.80f);
		
		// Unit vector scaled by new length
		du.multiply(len - dl);
		
		// CamPos adjusted by scaled unit vector
		camPos.x = camPos.x + du.x;
		camPos.y = camPos.y + du.y;
		
		// Re-apply offset
		camPos.add(camOffset);
		
		return false;
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
	public boolean scrolled(int val)
	{
		adjCamZoom(val);
		
		System.out.println("Val " + val);
		
		return false;
	}
	
	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		switch(button)
		{
			case 0:
			{
				button0Pressed = true;
				
				// Update newX/Y
				mouseDragPos.x = x;
				mouseDragPos.y = y;
				
				// Abort Animation
				cameraResetAnimate = false;
			}
			break;
			case 1:
			{
				button1Pressed = true;
				
				// Allow Animation
				cameraResetAnimate = true;
			}
			break;
			case 2:
			{
				button2Pressed = true;
				
				// Abort Animation
				cameraResetAnimate = false;
			}
			break;
		}
		
		return false;
	}
	
	@Override
	public boolean touchDragged(int x, int y, int z)
	{
		if(button0Pressed)
		{
			// Latch the old position
			float previousX = mouseDragPos.x;
			float previousY = mouseDragPos.y;
			
			// Update newX/Y
			mouseDragPos.x = x;
			mouseDragPos.y = y;
			
			// How much did the mouse move.
			float diffX = previousX - mouseDragPos.x;
			float diffY = previousY - mouseDragPos.y;
			
			// Raw X,Y,Z is at screen resolution - scaled here to smooth movement
			float ratio = (camZoom * MOVEMENT_DAMPER);
			
			// -y for when converting from screen to graphics coordinates
			camPos.add(diffX * ratio, -diffY * ratio);
		}
		
		return false;
	}
	
	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		if(button0Pressed)
		{
			// Update newX/Y
			mouseDragPos.x = x;
			mouseDragPos.y = y;
			
			button0Pressed = false;
		}
		
		if(button1Pressed)
		{
			button1Pressed = false;
		}
		
		if(button2Pressed)
		{
			currentMiddleButtonMode += 1;
			
			currentMiddleButtonMode = currentMiddleButtonMode % MAX_MIDDLE_BUTTON_MODE;
			
			button2Pressed = false;
		}
		
		return false;
	}
	
	public void update()
	{
		if(cameraResetAnimate)
		{
			if(reset())
			{
				cameraResetAnimate = false;
			}
		}
		
		camera.position.x = camPos.x;
		camera.position.y = camPos.y;
		camera.position.z = camZoom;
		
		camera.update();
	}
	
	public int middleButtonMode()
	{
		return currentMiddleButtonMode;
	}
}