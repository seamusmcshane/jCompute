package alifeSim.Gui.View;

import java.util.Timer;
import java.util.TimerTask;

import alifeSimGeom.A2DVector2f;

public class SimViewCam
{
	private final float minZoom = 0.1f;
	private final float maxZoom = 10f;
	private final float zoomIncr = 0.125f;
	
	private float zoomDefault = 1f;
	
	private A2DVector2f camOffset; 
	
	private A2DVector2f camPos;
	private float camZoom;

	private Timer viewTimer = new Timer();
	
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
		camOffset = new A2DVector2f(0,0);
		
		camPos = new A2DVector2f(0,0);
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
		this.camZoom += zoomAdj*zoomIncr;
		
		if(camZoom < minZoom)
		{
			camZoom = minZoom;
		}
		
		if(camZoom > maxZoom)
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
		viewTimer.schedule(new TimerTask() 
		{
			  @Override
			  public void run() 
			  {
				try
				{
				  while(camZoom>zoomDefault)
				  {
					  camZoom-=(zoomIncr/25);
					  Thread.sleep(1);

				  }	
				  
				  while(camZoom<zoomDefault)
				  {
					  camZoom+=(zoomIncr/25);
					  Thread.sleep(1);
				  }	
				  
				  camZoom = zoomDefault;
				  
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			  }
			}, 0);		
		
	}
	
	public void resetCamPos(final float x, final float y)
	{
		viewTimer.schedule(new TimerTask() 
		{
			  @Override
			  public void run() 
			  {
				float finalX = x-camOffset.getX();
				float finalY = y-camOffset.getY();
				
				float currentX = camPos.getX();
				float currentY = camPos.getY();
				
				boolean XFinished = false;
				boolean YFinished = false;
					
				float incr = 1f;
				
				try
				{
					while(!XFinished || !YFinished)
					{
						
						if( currentX < finalX)
						{
							currentX+=incr;
						}
						
						if( currentY < finalY)
						{
							currentY+=incr;
						}	
						
						if( currentX > finalX)
						{
							currentX-=incr;
						}
						
						if( currentY > finalY)
						{
							currentY-=incr;
						}	
						
						if(currentX==finalX)
						{
							XFinished=true;
						}
						
						if(currentY==finalY)
						{
							YFinished=true;
						}

						camPos.set(currentX,currentY);
						
						Thread.sleep(1);
					}

				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			  }
		}, 0);	
	}

	public void moveCam(float x, float y)
	{
		camPos.set(x,y);				
	}	
}
