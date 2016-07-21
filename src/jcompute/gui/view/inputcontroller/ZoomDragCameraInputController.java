package jcompute.gui.view.inputcontroller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.math.FloatingPoint;
import jcompute.math.MathCollision2f;
import jcompute.math.MathVector3f;
import jcompute.math.geom.JCVector2f;
import jcompute.math.geom.JCVector3f;

public class ZoomDragCameraInputController implements InputProcessor
{
	// ViewRendererInf with Controlled Camera
	private final ViewRendererInf renderer;
	private final Camera camera;
	
	// Calculated camera position
	public final JCVector3f position;
	
	// Camera Offset (from 0,0,0)
	private final JCVector3f offset;
	
	// Error Margin for Animation Equality
	private final float EPSILON = 0.5f;
	
	// Percentage to scale distances
	private final float SCALED_PERCENTAGE = 0.8f;
	
	// Raw mouse
	public final JCVector2f mouseRawXY;
	
	// Mouse dragging
	public final JCVector2f mouseXYDragging;
	private float mouseXYDraggingSmoothing = 1f;
	
	// Mouse Cursor Grabbed
	public final JCVector3f mouseXYCursorProjected;
	
	// Zoom Parameters
	private float zoomDefault = 800f;
	private float minZoom = 200f;
	private float maxZoom = zoomDefault;
	
	// Mouse Button Status (Left/ Right / Middle)
	private boolean button0Pressed;
	private boolean button1Pressed;
	private boolean button2Pressed;
	
	// Double click detection
	private final long DOUBLE_CLICK_TIMEOUT = 250;
	private long lastButton0Time;
	private long lastButton1Time;
	
	// Middle Click Mode
	private final int MAX_MIDDLE_BUTTON_MODE;
	private int currentMiddleButtonMode = 0;
	
	// Animation enabled / Required (Right Click)
	private boolean cameraAnimate;
	private boolean zooming;
	private boolean zoomingIn;
	
	// Position to animate to
	private final JCVector3f animateToPosition = new JCVector3f(0, 0, 0);
	
	public ZoomDragCameraInputController(ViewRendererInf renderer, float camPosX, float camPosY, float offsetX, float offsetY, int middleButtonModes)
	{
		this.renderer = renderer;
		this.camera = renderer.getCamera();
		
		position = new JCVector3f(camPosX, camPosY, zoomDefault);
		
		offset = new JCVector3f(offsetX, offsetY, 0);
		
		position.add(offset);
		
		mouseRawXY = new JCVector2f(0, 0);
		
		mouseXYDragging = new JCVector2f(0, 0);
		
		mouseXYCursorProjected = new JCVector3f(0, 0, 0);
		
		this.MAX_MIDDLE_BUTTON_MODE = (middleButtonModes > 0) ? middleButtonModes : 0;
	}
	
	public void reset()
	{
		animateToPosition.x = 0;
		animateToPosition.y = 0;
		animateToPosition.z = zoomDefault;
		
		position.add(offset);
		
		cameraAnimate = true;
	}
	
	public void setZoomLimits(float min, float max)
	{
		minZoom = min;
		maxZoom = max;
	}
	
	public void setCamOffset(float x, float y, float z)
	{
		// Remove old offset
		position.sub(this.offset);
		
		offset.x = x;
		offset.x = y;
		offset.z = z;
		
		// Add new
		position.add(offset);
	}
	
	public void setCamPos(float x, float y, float z)
	{
		position.x = x;
		position.x = y;
		position.z = z;
	}
	
	private boolean animateToPosition(JCVector3f newPosition)
	{
		// Assume we have been provided newPosition with no offset applied
		// Remove the current offset before the calculation
		position.sub(offset);
		
		// Done if near x,y +- ANIMATE_SNAP
		if(FloatingPoint.AlmostEqualEpsilon(position.x, newPosition.x, EPSILON) & FloatingPoint.AlmostEqualEpsilon(position.y, newPosition.y, EPSILON)
		& FloatingPoint.AlmostEqualEpsilon(position.z, newPosition.z, EPSILON))
		{
			position.x = newPosition.x;
			position.y = newPosition.y;
			position.z = newPosition.z;
			
			// Re-apply offset
			position.add(offset);
			
			return true;
		}
		
		// Distance Vector
		JCVector3f dv = new JCVector3f(newPosition.x - position.x, newPosition.y - position.y, newPosition.z - position.z);
		
		// Vector length
		float len = dv.length();
		
		// Distance unit vector
		JCVector3f du = MathVector3f.Unit(dv);
		
		// Scaled length
		float dl = (len * SCALED_PERCENTAGE);
		
		// Unit vector scaled by new length
		du.multiply(len - dl);
		
		// CamPos adjusted by scaled unit vector
		position.add(du);
		
		// Re-apply offset
		position.add(offset);
		
		return false;
	}
	
	@Override
	public boolean keyDown(int key)
	{
		if(key == Input.Keys.CONTROL_LEFT)
		{
			if(!Gdx.input.isCursorCatched())
			{
				Gdx.input.setCursorCatched(true);
				Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
			}
			else
			{
				Gdx.input.setCursorCatched(false);
			}
		}
		
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
	public boolean mouseMoved(int x, int y)
	{
		updateMouseRawXY(x, y);
		
		return false;
	}
	
	@Override
	public boolean scrolled(int val)
	{
		adjustZoom(val);
		
		return false;
	}
	
	private void adjustZoom(int val)
	{
		float tZoom = position.z;
		
		// Don'd try zooming in when target is min zoom
		if(FloatingPoint.AlmostEqualEpsilon(animateToPosition.z, minZoom, EPSILON) & val < 0)
		{
			return;
		}
		
		// Don'd try zooming in when target is max zoom
		if(FloatingPoint.AlmostEqualEpsilon(animateToPosition.z, maxZoom, EPSILON) & val > 0)
		{
			return;
		}
		
		cameraAnimate = false;
		zooming = false;
		zoomingIn = false;
		
		// Adjust the zoom
		if(val > 0)
		{
			tZoom *= 2;
			
			zoomingIn = false;
		}
		else
		{
			tZoom *= 0.5f;
			
			zoomingIn = true;
		}
		
		// Clamp the new zoom to the zoom range
		tZoom = MathCollision2f.ClampOnRange(tZoom, minZoom, maxZoom);
		
		// if(tZoom < minZoom)
		// {
		// tZoom = minZoom;
		// }
		// else if(tZoom > maxZoom)
		// {
		// tZoom = maxZoom;
		// }
		
		animateToPosition.z = tZoom;
		
		zooming = true;
		cameraAnimate = true;
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
				mouseXYDragging.x = x;
				mouseXYDragging.y = y;
				
				// Translate the current mouse coordinates.
				mouseRawXY.x = x;
				mouseRawXY.y = y;
				
				zooming = false;
				zoomingIn = false;
				
				long timeNow = System.currentTimeMillis();
				
				// Has a double click occurred
				if((timeNow - lastButton0Time) < DOUBLE_CLICK_TIMEOUT)
				{
					adjustZoom(-1);
				}
				else
				{
					// Abort Animation
					cameraAnimate = false;
				}
				
				lastButton0Time = System.currentTimeMillis();
			}
			break;
			case 1:
			{
				button1Pressed = true;
				
				// Reset Zoom
				cameraAnimate = true;
				
				// Reset just zoom
				animateToPosition.x = position.x;
				animateToPosition.y = position.y;
				animateToPosition.z = zoomDefault;
				
				// animateToPosition must be provided with out an offset
				animateToPosition.sub(offset);
				
				long timeNow = System.currentTimeMillis();
				
				// Has a double click occurred
				if((timeNow - lastButton1Time) < DOUBLE_CLICK_TIMEOUT)
				{
					// Center Camera at origin
					animateToPosition.x = 0;
					animateToPosition.y = 0;
				}
				
				lastButton1Time = System.currentTimeMillis();
			}
			break;
			case 2:
			{
				button2Pressed = true;
				
				// Abort Animation
				cameraAnimate = false;
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
			float previousX = mouseXYDragging.x;
			float previousY = mouseXYDragging.y;
			
			// Update newX/Y
			mouseXYDragging.x = x;
			mouseXYDragging.y = y;
			
			// How much did the mouse move.
			float diffX = previousX - mouseXYDragging.x;
			float diffY = previousY - mouseXYDragging.y;
			
			// -y
			position.add(diffX * mouseXYDraggingSmoothing, -diffY * mouseXYDraggingSmoothing, 0);
		}
		
		return false;
	}
	
	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		if(button0Pressed)
		{
			updateMouseRawXY(x, y);
			
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
	
	public void update(float targetX, float targetY, float targetZ)
	{
		// No buttons pressed and not animating
		if(!button0Pressed & !button1Pressed & !button2Pressed & !cameraAnimate)
		{
			renderer.screenToWorld(mouseRawXY, targetZ, mouseXYCursorProjected);
		}
		
		if(zooming)
		{
			if(zoomingIn)
			{
				if(!FloatingPoint.AlmostEqualEpsilon(position.z, minZoom, EPSILON))
				{
					animateToPosition.x = mouseXYCursorProjected.x;
					animateToPosition.y = mouseXYCursorProjected.y;
				}
				else
				{
					// Prevent zooming further
					animateToPosition.x = position.x;
					animateToPosition.y = position.y;
					animateToPosition.z = minZoom;
				}
				
				// zoomingIn = false;
			}
			else
			{
				animateToPosition.x = position.x;
				animateToPosition.y = position.y;
				
				if(FloatingPoint.AlmostEqualEpsilon(position.z, maxZoom, EPSILON))
				{
					// Prevent zooming out further
					animateToPosition.z = maxZoom;
				}
			}
			
			// animateToPosition must be provided with out an offset
			animateToPosition.sub(offset);
			
			zooming = false;
		}
		
		if(cameraAnimate)
		{
			if(animateToPosition(animateToPosition))
			{
				if(zoomingIn)
				{
					// mouseRawXY.x = Gdx.graphics.getWidth() * 0.5f;
					// mouseRawXY.y = Gdx.graphics.getHeight() * 0.5f;
					//
					// Gdx.input.setCursorPosition((int) mouseRawXY.x, (int) mouseRawXY.y);
					
					zoomingIn = false;
				}
				
				cameraAnimate = false;
			}
			
			renderer.screenToWorld(mouseRawXY, targetZ, mouseXYCursorProjected);
		}
		
		// Update camera X/Y/Z position
		camera.position.x = position.x;
		camera.position.y = position.y;
		camera.position.z = position.z;
		
		// float ratio = (zoomDefault / position.z);
		//
		// if(ratio > 2f)
		// {
		// ratio = 2f;
		// }
		//
		// if(ratio < 1f)
		// {
		// ratio = 1f;
		// }
		//
		// float offsetY = (ratio - 1f) * (100f);
		// //
		// camera.lookAt(position.x, position.y + offsetY, targetZ);
		
		// Ensure we are look at the location below the camera - similar to an orthographic view
		camera.lookAt(position.x, position.y, targetZ);
		//
		// Update the camera view projection
		camera.update();
		
		// Adjust mouse drag
		mouseXYDraggingSmoothing = (position.z / zoomDefault);
	}
	
	public int middleButtonMode()
	{
		return currentMiddleButtonMode;
	}
	
	// Wrapper method for allowing modification of raw mouse coordinates before application
	private void updateMouseRawXY(float x, float y)
	{
		// Direct
		mouseRawXY.x = x;
		mouseRawXY.y = y;
	}
}