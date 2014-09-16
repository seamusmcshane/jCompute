package jCompute.Gui.View;

import jCompute.Gui.View.Graphics.A2DVector2f;

import java.util.Timer;
import java.util.TimerTask;

public class ViewCam
{
	int animationSteps = 250;

	private final float minZoom = 0.125f;
	private final float maxZoom = 12.5f;
	private final float zoomIncr = 0.125f;

	private Timer zoomTimer;
	private float zoomDefault = 1f;
	float aniZoomIncr = 0;

	private A2DVector2f camOffset;

	private A2DVector2f camPos;
	private float camZoom;

	float finalX = 0;
	private Timer centerX;
	private float aniXreset = 0;

	float finalY = 0;
	private Timer centerY;
	private float aniYreset = 0;

	public ViewCam(A2DVector2f camPos, float camZoom, A2DVector2f camOffset)
	{
		super();

		this.camPos = camPos;

		this.zoomDefault = camZoom;
		this.camZoom = zoomDefault;

		this.camOffset = new A2DVector2f((int)camOffset.getX(),(int)camOffset.getY());

	}

	public ViewCam()
	{
		camOffset = new A2DVector2f(0, 0);

		camPos = new A2DVector2f(0, 0);
		camZoom = zoomDefault;

	}

	public void setCamOffset(A2DVector2f camOffset)
	{
		this.camOffset = camOffset;
	}

	public void setCamPos(A2DVector2f camPos)
	{
		this.camPos = camPos;
	}

	public float getCamZoom()
	{
		return camZoom;
	}

	public void adjCamZoom(float zoomAdj)
	{
		if (zoomAdj > 0)
		{
			this.camZoom += zoomIncr;

		}
		else
		{
			this.camZoom -= zoomIncr;
		}

		if (camZoom < minZoom)
		{
			camZoom = minZoom;
		}

		if (camZoom > maxZoom)
		{
			camZoom = maxZoom;
		}
		
	}

	public float getCamPosX()
	{
		return camPos.getX();
	}

	public float getCamPosY()
	{
		return camPos.getY();
	}

	public void resetCamZoom()
	{
		boolean zoom = true;

		if (camZoom > zoomDefault)
		{
			aniZoomIncr = Math.abs(camZoom) / animationSteps;
		}
		else if (camZoom < zoomDefault)
		{
			aniZoomIncr = Math.abs(zoomDefault / animationSteps);
		}
		else
		{
			zoom = false;
		}

		if (zoom)
		{
			//System.out.println("Zoom");

			zoomTimer = new Timer("Zoom Animator Timer");

			zoomTimer.schedule(new TimerTask()
			{

				@Override
				public void run()
				{
					if (camZoom > zoomDefault)
					{
						if (camZoom - aniZoomIncr < zoomDefault)
						{
							camZoom = zoomDefault;

							zoomTimer.cancel();
						}					
						
						camZoom -= aniZoomIncr;
					}

					if (camZoom < zoomDefault)
					{
						if (camZoom + aniZoomIncr > zoomDefault)
						{
							camZoom = zoomDefault;

							zoomTimer.cancel();
						}
						else
						{
							camZoom += aniZoomIncr;
						}

					}

				}

			}, 0, 250 / animationSteps);
		}
	}

	public void resetCamX(final float x)
	{
		boolean resetX = true;
		finalX = x - camOffset.getX();

		if (camPos.getX() > finalX)
		{
			aniXreset = Math.abs(camPos.getX() / animationSteps);
		}
		else if (camPos.getX() < finalX)
		{
			aniXreset = Math.abs(finalX / animationSteps);
		}
		else
		{
			resetX = false;
		}
		
		if (resetX)
		{
			//System.out.println("Center X");

			if(centerX!=null)
			{
				centerX.cancel();
			}
			centerX = new Timer("Center Animator Timer");

			centerX.schedule(new TimerTask()
			{
				@Override
				public void run()
				{	
					if( (camPos.getX() > (finalX - 10) ) && (camPos.getX() < (finalX + 10) ))
					{
						//System.out.println("Center Snap X");

						camPos.setX(finalX);
						centerX.cancel();
					}
					
					if (camPos.getX() > finalX)
					{
						//System.out.println("Center X >");

						if (camPos.getX() - aniXreset < finalX)
						{
							camPos.setX(finalX);
							centerX.cancel();
						}

						camPos.setX(camPos.getX() - aniXreset);
					}

					if (camPos.getX() < finalX)
					{
						//System.out.println("Center X <");
						//System.out.println("Center X <" + camPos.getX() + " " + finalX + " " + aniXreset);

						if (camPos.getX() + aniXreset > finalX)
						{
							camPos.setX(finalX);
							centerX.cancel();
						}

						camPos.setX(camPos.getX() + aniXreset);
					}

				}

			}, 0, 250 / animationSteps);
		}
		
	}
	
	public void resetCamY(final float y)
	{
		boolean resetY = true;
		finalY = y - camOffset.getY();

		if (camPos.getY() > finalY)
		{
			aniYreset = Math.abs(camPos.getY() / animationSteps);
		}
		else if (camPos.getY() < finalY)
		{
			aniYreset = finalY / animationSteps;
		}
		else
		{
			resetY = false;
		}
		
		if (resetY)
		{
			if(centerY!=null)
			{
				centerY.cancel();
			}
			centerY = new Timer("Center Animator Timer");

			//System.out.println("Center Y");
			
			centerY.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					//System.out.println("camPos : " + camPos.getX() + " " + camPos.getY());
					//System.out.println("Final : " + finalX + " " + finalY);
					
					// Snap the last pixel incase we get a floating point error
					if( (camPos.getY() > (finalY - 1) ) && (camPos.getY() < (finalY + 1) ))
					{
						camPos.setY(finalY);
						centerY.cancel();
					}
					
					if (camPos.getY() > finalY)
					{
						if (camPos.getY() - aniYreset < finalY)
						{
							camPos.setY(finalY);
							centerY.cancel();
						}

						camPos.setY(camPos.getY() - aniYreset);

					}

					if (camPos.getY() < finalY)
					{
						if (camPos.getY() + aniYreset > finalY)
						{
							camPos.setY(finalY);
							centerY.cancel();
						}

						camPos.setY(camPos.getY() + aniYreset);
					}
				}
				
			}, 0, 250 / animationSteps);
		}
	}
	
	public void resetCamPos(float x,float y)
	{
		resetCamX(x);
		resetCamY(y);
	}

	public void moveCam(float x, float y)
	{	
		camPos.add(x*camZoom,y*camZoom);
	}

}
