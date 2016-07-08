package jcompute.gui.view.inputcontroller;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;

import jcompute.math.FloatingPoint;
import jcompute.math.geom.JCVector2f;
import jcompute.math.geom.MathVector2f;

public class ZoomDragCameraInputController implements InputProcessor
{
	// Target Camera
	private final Camera camera;
	
	// Current CamPos
	private final JCVector2f position;
	
	// Camera Offset (from 0,0)
	private final JCVector2f offset;
	
	// Current Zoom
	private float zoom;
	
	// Error Margin for Animation Equality
	private final float EPSILON = 0.5f;
	
	// Stores the mouse vector across updates
	private final JCVector2f mousePosition = new JCVector2f(0, 0);
	
	// Scale the X/Y passed in by this amount ( 1 / 800 )
	private final float MOVEMENT_DAMPER = 0.00125f;
	
	// Zoom Parameters
	private final float zoomIncr = 200f;
	private float zoomDefault = 800f;
	private float minZoom = zoomIncr;
	private float maxZoom = zoomDefault;
	
	// Mouse Button Status (Left/ Right / Middle)
	private boolean button0Pressed;
	private boolean button1Pressed;
	private boolean button2Pressed;
	
	private final long DOUBLE_CLICK_TIMEOUT = 500;
	private long lastButton1Time;
	
	// Middle Click Mode
	private final int MAX_MIDDLE_BUTTON_MODE;
	private int currentMiddleButtonMode = 0;
	
	// Animation enabled / Required (Right Click)
	private boolean cameraResetAnimate = false;
	
	private boolean cameraZoomAnimate = false;
	private float zoomTo;
	
	public ZoomDragCameraInputController(Camera camera, float camPosX, float camPosY, float offsetX, float offsetY, int middleButtonModes)
	{
		this.camera = camera;
		
		position = new JCVector2f(camPosX, camPosY);
		
		zoom = zoomDefault;
		zoomDefault = zoom;
		
		offset = new JCVector2f(offsetX, offsetY);
		
		position.add(offset);
		
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
		position.sub(this.offset);
		
		offset.x = x;
		offset.x = y;
		
		// Add new
		position.add(offset);
	}
	
	public void setCamPos(float x, float y)
	{
		position.x = x;
		position.x = y;
	}
	
	public void adjCamZoom(float zoomAdj)
	{
		cameraResetAnimate = false;
		
		float tZoom = zoom;
		
		if(zoomAdj > 0)
		{
			tZoom += zoomIncr;
		}
		else
		{
			tZoom -= zoomIncr;
		}
		
		if(tZoom < minZoom)
		{
			tZoom = minZoom;
		}
		
		if(tZoom > maxZoom)
		{
			tZoom = maxZoom;
		}
		
		zoomTo = tZoom;
		
		cameraZoomAnimate = true;
	}
	
	public boolean reset()
	{
		cameraZoomAnimate = false;
		
		boolean z = zoomTo(zoomDefault, 0.60f);
		
		return resetCameraXY(0, 0) & z;
	}
	
	private boolean zoomTo(float z, float percent)
	{
		// Done if near z +- ANIMATE_SNAP
		if(FloatingPoint.AlmostEqualEpsilon(zoom, z, EPSILON))
		{
			zoom = z;
			
			return true;
		}
		
		// Distance to target z
		float zoomLen = z - zoom;
		
		// CamZoom scaled adjusted by scaled distance to z
		zoom = zoom + (zoomLen - (zoomLen * percent));
		
		return false;
	}
	
	private boolean resetCameraXY(float x, float y)
	{
		// Remove the offset before the calculation
		position.sub(offset);
		
		// Done if near x,y +- ANIMATE_SNAP
		if(FloatingPoint.AlmostEqualEpsilon(position.x, x, EPSILON) && FloatingPoint.AlmostEqualEpsilon(position.y, y, EPSILON))
		{
			position.x = 0;
			position.y = 0;
			
			// Re-apply offset
			position.add(offset);
			
			return true;
		}
		
		// Distance Vector
		JCVector2f dv = new JCVector2f(x - position.x, y - position.y);
		
		// Vector length
		float len = dv.length();
		
		// Distance unit vector
		JCVector2f du = MathVector2f.Unit(dv);
		
		// Scaled length
		float dl = (len * 0.80f);
		
		// Unit vector scaled by new length
		du.multiply(len - dl);
		
		// CamPos adjusted by scaled unit vector
		position.add(du);
		
		// Re-apply offset
		position.add(offset);
		
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
				mousePosition.x = x;
				mousePosition.y = y;
				
				// Abort Animation
				cameraResetAnimate = false;
			}
			break;
			case 1:
			{
				button1Pressed = true;
				
				// Reset Zoom
				cameraZoomAnimate = true;
				zoomTo = zoomDefault;
				
				long timeNow = System.currentTimeMillis();
				
				// Has a double click occurred
				if((timeNow - lastButton1Time) < DOUBLE_CLICK_TIMEOUT)
				{
					cameraZoomAnimate = false;
					
					// Allow Animation
					cameraResetAnimate = true;
				}
				
				lastButton1Time = System.currentTimeMillis();
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
			float previousX = mousePosition.x;
			float previousY = mousePosition.y;
			
			// Update newX/Y
			mousePosition.x = x;
			mousePosition.y = y;
			
			// How much did the mouse move.
			float diffX = previousX - mousePosition.x;
			float diffY = previousY - mousePosition.y;
			
			// Raw X,Y,Z is at screen resolution - scaled here to smooth movement
			float ratio = (zoom * MOVEMENT_DAMPER);
			
			// -y for when converting from screen to graphics coordinates
			position.add(diffX * ratio, -diffY * ratio);
		}
		
		return false;
	}
	
	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		if(button0Pressed)
		{
			// Update newX/Y
			mousePosition.x = x;
			mousePosition.y = y;
			
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
		
		if(cameraZoomAnimate)
		{
			if(zoomTo(zoomTo, 0.74f))
			{
				cameraZoomAnimate = false;
			}
		}
		
		camera.position.x = position.x;
		camera.position.y = position.y;
		camera.position.z = zoom;
		
		camera.update();
	}
	
	public int middleButtonMode()
	{
		return currentMiddleButtonMode;
	}
}