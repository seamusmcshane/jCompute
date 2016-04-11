package jcompute.gui.view;

import jcompute.gui.view.graphics.A2DVector2f;

public class ViewCam
{
	// Zoom and Reset Animation Steps
	private final int animationSteps = 20;

	// Zoom Animation Control
	private boolean zoomInterSet = false;
	private float aniZoomInter = 0;

	// Error Margin for Zoom Equality
	private final float ZOOM_SNAP = 1f;

	// Zoom Parameters
	private final float zoomIncr = 50f;
	private final float minAniZoomInter = 50f;
	private float zoomDefault = 800f;
	private float minZoom = zoomIncr;
	private float maxZoom = zoomDefault;

	// Current CamZoom
	private float camZoom;

	// Camera Offset
	private A2DVector2f camOffset;

	// Current CamPos
	private A2DVector2f camPos;

	// Error Margin for Animation Equality
	private final float ANIMATE_SNAP = 1f;

	// X Animation Reset
	private float finalX = 0;
	private float aniXresetInter = 0;
	private boolean xAnimateInterSet = false;

	// Y Animation Reset
	private float finalY = 0;
	private float aniYresetInter = 0;
	private boolean yAnimateInterSet = false;

	public ViewCam(A2DVector2f camPos, A2DVector2f camOffset)
	{
		super();

		this.camPos = camPos;

		camZoom = zoomDefault;
		zoomDefault = camZoom;

		this.camOffset = new A2DVector2f(new float[]
		{
			camOffset.getX(), camOffset.getY()
		});

		camPos.add(camOffset);
	}

	public ViewCam()
	{
		camOffset = new A2DVector2f(new float[]
		{
			0, 0
		});
		camPos = new A2DVector2f(new float[]
		{
			0, 0
		});
		camZoom = zoomDefault;

		camPos.add(camOffset);
	}

	public void setZoomLimits(float min, float max)
	{
		minZoom = min;
		maxZoom = max;
	}

	public void setCamOffset(A2DVector2f camOffset)
	{
		// Remove Old offset
		camPos.sub(this.camOffset);

		this.camOffset = camOffset;

		camPos.add(camOffset);
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

		zoomInterSet = false;
	}

	public boolean resetZoom()
	{
		boolean zoomNeeded = true;
		boolean zoomFinished = true;

		if((camZoom < (zoomDefault - ZOOM_SNAP)) || (camZoom > (zoomDefault + ZOOM_SNAP)))
		{
			zoomFinished = false;

			if(!zoomInterSet)
			{
				aniZoomInter = Math.abs(camZoom - zoomDefault) / animationSteps;

				if(aniZoomInter < minAniZoomInter)
				{
					aniZoomInter = minAniZoomInter;
				}

				zoomInterSet = true;
			}
		}
		else
		{
			zoomNeeded = false;
		}

		if(zoomNeeded)
		{
			if(camZoom > zoomDefault)
			{
				if((camZoom - aniZoomInter) < zoomDefault)
				{
					camZoom = zoomDefault;

					zoomFinished = true;
				}
				else
				{
					camZoom -= aniZoomInter;
				}
			}

			if(camZoom < zoomDefault)
			{
				if((camZoom + aniZoomInter) > zoomDefault)
				{
					camZoom = zoomDefault;

					zoomFinished = true;
				}
				else
				{
					camZoom += aniZoomInter;
				}

			}
		}
		else
		{
			zoomFinished = true;
		}

		if(zoomFinished)
		{
			zoomInterSet = false;
		}

		return zoomFinished;
	}

	public float getCamPosX()
	{
		return camPos.getX();
	}

	public float getCamPosY()
	{
		return camPos.getY();
	}

	private boolean resetCamX(final float x)
	{
		boolean resetFinished = true;
		boolean resetNeeded = true;

		if((camPos.getX() < (finalX - ANIMATE_SNAP)) || (camPos.getX() > (finalX + ANIMATE_SNAP)))
		{
			resetFinished = false;

			if(!xAnimateInterSet)
			{
				finalX = x + camOffset.getX();
				aniXresetInter = (Math.abs(camPos.getX()) + Math.abs(finalX)) / animationSteps;

				xAnimateInterSet = true;
			}

		}
		else
		{
			resetNeeded = false;
		}

		if(resetNeeded)
		{
			if((camPos.getX() > (finalX - ANIMATE_SNAP)) && (camPos.getX() < (finalX + ANIMATE_SNAP)))
			{
				camPos.setX(finalX);
				resetFinished = true;
			}

			if(camPos.getX() > finalX)
			{
				if((camPos.getX() - aniXresetInter) < finalX)
				{
					camPos.setX(finalX);
					resetFinished = true;
				}
				else
				{
					camPos.setX(camPos.getX() - aniXresetInter);
				}
			}

			if(camPos.getX() < finalX)
			{

				if((camPos.getX() + aniXresetInter) > finalX)
				{
					camPos.setX(finalX);
					resetFinished = true;
				}
				else
				{
					camPos.setX(camPos.getX() + aniXresetInter);
				}
			}
		}

		if(resetFinished)
		{
			resetNeeded = false;
		}

		return resetFinished;
	}

	private boolean resetCamY(final float y)
	{
		boolean resetFinished = true;
		boolean resetNeeded = true;

		if((camPos.getY() < (finalY - ANIMATE_SNAP)) || (camPos.getY() > (finalY + ANIMATE_SNAP)))
		{
			resetFinished = false;

			if(!yAnimateInterSet)
			{
				finalY = y + camOffset.getY();
				aniYresetInter = (Math.abs(camPos.getY()) + Math.abs(finalY)) / animationSteps;

				yAnimateInterSet = true;
			}

		}
		else
		{
			resetNeeded = false;
		}

		if(resetNeeded)
		{
			if((camPos.getY() > (finalY - ANIMATE_SNAP)) && (camPos.getY() < (finalY + ANIMATE_SNAP)))
			{
				camPos.setY(finalY);
				resetFinished = true;
			}

			if(camPos.getY() > finalY)
			{
				if((camPos.getY() - aniYresetInter) < finalY)
				{
					camPos.setY(finalY);
					resetFinished = true;
				}
				else
				{
					camPos.setY(camPos.getY() - aniYresetInter);
				}
			}

			if(camPos.getY() < finalY)
			{
				if((camPos.getY() + aniYresetInter) > finalY)
				{
					camPos.setY(finalY);
					resetFinished = true;
				}
				else
				{
					camPos.setY(camPos.getY() + aniYresetInter);
				}
			}
		}

		if(resetFinished)
		{
			resetNeeded = false;
		}

		return resetFinished;
	}

	public void moveCam(float x, float y)
	{
		camPos.add(x * (camZoom / 500), y * (camZoom / 500));

		xAnimateInterSet = false;
		yAnimateInterSet = false;
	}

	public boolean reset()
	{
		boolean x = resetCamX(0);
		boolean y = resetCamY(0);
		boolean z = resetZoom();

		return x & y & z;
	}
}
