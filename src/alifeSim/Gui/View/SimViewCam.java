package alifeSim.Gui.View;

import java.util.Timer;
import java.util.TimerTask;

import alifeSimGeom.A2DVector2f;

public class SimViewCam
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

	public SimViewCam(A2DVector2f camPos, float camZoom, A2DVector2f camOffset)
	{
		super();

		this.camPos = camPos;

		this.zoomDefault = camZoom;
		this.camZoom = zoomDefault;

		this.camOffset = camOffset;

	}

	public SimViewCam()
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
			aniZoomIncr = zoomDefault / animationSteps;
		}
		else
		{
			zoom = false;
		}

		if (zoom)
		{
			zoomTimer = new Timer("Zoom Animator Timer");

			zoomTimer.schedule(new TimerTask()
			{

				@Override
				public void run()
				{
					System.out.println("Resting Zoom");

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

	public void resetCamPos(final float x, final float y)
	{
		boolean resetX = true;
		boolean resetY = true;

		System.out.println("ResetCam");
		
		finalX = x - camOffset.getX();
		finalY = y - camOffset.getY();

		if (camPos.getX() > finalX)
		{
			aniXreset = Math.abs(camPos.getX() / animationSteps);
			
			System.out.println(">"+aniXreset);

		}
		else if (camPos.getX() < finalX)
		{
			aniXreset = finalX / animationSteps;
			
			System.out.println("<" + aniXreset);

		}
		else
		{
			resetX = false;
		}
		
		if (camPos.getY() > finalY)
		{
			aniYreset = Math.abs(camPos.getY() / animationSteps);
			
			System.out.println(">"+aniYreset);

		}
		else if (camPos.getY() < finalY)
		{
			aniYreset = finalY / animationSteps;
			
			System.out.println("<" + aniYreset);

		}
		else
		{
			resetY = false;
		}
		
		if (resetX)
		{
			centerX = new Timer("Center Animator Timer");

			centerX.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					System.out.println("Center X");

					if (camPos.getX() > finalX)
					{
						if (camPos.getX() - aniXreset < finalX)
						{
							camPos.setX(finalX);
							centerX.cancel();
						}
						
						camPos.setX(camPos.getX()-aniXreset);

					}

					if (camPos.getX() < finalX)
					{
						if (camPos.getX() + aniXreset > finalX)
						{
							camPos.setX(finalX);
							centerX.cancel();
						}
						
						camPos.setX(camPos.getX()+aniXreset);
					}

				}

			}, 0, 250 / animationSteps);
		}
		
		if (resetY)
		{
			centerY = new Timer("Center Animator Timer");

			centerY.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					System.out.println("Center Y");

					if (camPos.getY() > finalY)
					{
						if (camPos.getY() - aniYreset < finalY)
						{
							camPos.setY(finalY);
							centerY.cancel();
						}
						
						camPos.setY(camPos.getY()-aniYreset);

					}

					if (camPos.getY() < finalY)
					{
						if (camPos.getY() + aniYreset > finalY)
						{
							camPos.setY(finalY);
							centerY.cancel();
						}
						
						camPos.setY(camPos.getY()+aniYreset);
					}

				}

			}, 0, 250 / animationSteps);
		}

	}

	public void moveCam(float x, float y)
	{
		camPos.set(x, y);
	}
}
