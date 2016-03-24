package tools.ReportViewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class ImageViewerPanel extends JPanel
{
	private static final long serialVersionUID = -5989735518293388852L;
	
	private Timer imagePaintTimer;
	
	// Page target and current view image
	private PageImage pageImage;
	private BufferedImage paintImage;
	
	// View X/Y
	private float[] pos;
	
	// Scale modes
	private boolean scaleModeFitWindowSize;
	private boolean scaleModeFitWindowWidth;
	private float scale;
	
	// Rendering
	private int imageMode;
	private boolean continousUpdate;
	
	// TimerVars
	private long lastActivityTime;
	private long timerLastScale;
	
	// View Border
	private final int PAD_SIZE = 40;
	private final int PAD = PAD_SIZE / 2;
	private final int LINE = 3;
	private Color BK_COLOR = Color.GRAY;
	private Color BORDER_COLOR = Color.BLACK;
	
	public ImageViewerPanel()
	{
		pos = new float[2];
		
		createTimer();
		
		scale = 1f;
		
		revalidate();
		repaint();
	}
	
	/*
	 * *******************************************************************************************************************************************
	 * Page Target
	 ********************************************************************************************************************************************/
	
	public void setImage(PageImage pageImage)
	{
		this.pageImage = pageImage;
		
		paintImage = pageImage.getImage(0);
		
		imageMode = 2;
		
		// A resize is needed
		lastActivityTime = System.currentTimeMillis();
		
		// Invalidate the scale
		timerLastScale = -1;
		
		revalidate();
		repaint();
	}
	
	/*
	 * *******************************************************************************************************************************************
	 * Move View
	 ********************************************************************************************************************************************/
	
	public void translate(float dx, float dy)
	{
		// Correct deltas by magnitude of scale
		float mag = 1f / scale;
		
		// Position is then corrected by scale later
		pos[0] += (dx * mag);
		pos[1] += (dy * mag);
	}
	
	/*
	 * *******************************************************************************************************************************************
	 * Scale Modes
	 ********************************************************************************************************************************************/
	
	public void setScaleModeFitWindowSize()
	{
		scaleModeFitWindowSize = true;
		scaleModeFitWindowWidth = false;
		
		pos[0] = 0;
		pos[0] = 0;
		
		// A resize is needed
		lastActivityTime = System.currentTimeMillis();
		
		// Invalidate the scale
		timerLastScale = -1;
	}
	
	public void setScaleModeFitWindowWidth()
	{
		scaleModeFitWindowWidth = true;
		scaleModeFitWindowSize = false;
		
		pos[0] = 0;
		pos[0] = 0;
		
		// A resize is needed
		lastActivityTime = System.currentTimeMillis();
		
		// Invalidate the scale
		timerLastScale = -1;
	}
	
	public void setScaleModeScale(float scaleMulti)
	{
		// Disable other modes
		scaleModeFitWindowSize = false;
		scaleModeFitWindowWidth = false;
		
		// A new absolute scale
		scale = 1f * scaleMulti;
		
		// A resize is needed
		lastActivityTime = System.currentTimeMillis();
		
		// Invalidate the scale
		timerLastScale = -1;
	}
	
	public void adjScaleModeScale(float dScale)
	{
		// Disable other modes
		scaleModeFitWindowSize = false;
		scaleModeFitWindowWidth = false;
		
		// Adjust the current scale
		scale += dScale;
		
		// A resize is needed
		lastActivityTime = System.currentTimeMillis();
		
		// Invalidate the scale
		timerLastScale = -1;
	}
	
	/*
	 * *******************************************************************************************************************************************
	 * Rendering
	 ********************************************************************************************************************************************/
	
	public void allowImageSelect(boolean imageSelect)
	{
		continousUpdate = !imageSelect;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(BK_COLOR);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		if(paintImage != null)
		{
			if(!continousUpdate)
			{
				switch(imageMode)
				{
					case 0:
						paintImage = pageImage.getImage(imageMode);
						g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					break;
					case 1:
						paintImage = pageImage.getImage(imageMode);
						g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
					break;
					case 2:
						g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
						paintImage = pageImage.getImage(imageMode);
					break;
				}
			}
			else
			{
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			}
			
			float[] pageImageSize = pageImage.getSize();
			
			float iWidth = pageImageSize[0] * scale;
			float iHeight = pageImageSize[1] * scale;
			
			float newWidth = iWidth;
			float newHeight = iHeight;
			
			float panelPadWidth = getWidth();
			float panelPadHeight = getHeight();
			
			if(newWidth > panelPadWidth)
			{
				if(scaleModeFitWindowWidth || scaleModeFitWindowSize)
				{
					// panelPadHeight = getHeight() - 50;
					
					// Too wide - Scaled to Width
					newWidth = panelPadWidth;
					newHeight = iHeight * (panelPadWidth / iWidth);
					
					System.out.println("1 Width " + newWidth);
					System.out.println("1 Height " + newHeight);
					
					if(scaleModeFitWindowSize)
					{
						// Too long - Scaled height also
						if(newHeight > panelPadHeight)
						{
							newWidth = newWidth * (panelPadHeight / newHeight);
							newHeight = panelPadHeight;
							
							System.out.println("2 Width " + newWidth);
							System.out.println("2 Height " + newHeight);
						}
					}
				}
			}
			else
			{
				if(scaleModeFitWindowWidth || scaleModeFitWindowSize)
				{
					// panelPadHeight = getHeight() - 50;
					
					// Too small - Scaled to Width
					newWidth = panelPadWidth - PAD_SIZE;
					newHeight = iHeight * (panelPadWidth / iWidth);
					
					System.out.println("3 Width " + newWidth);
					System.out.println("3 Height " + newHeight);
					
					if(scaleModeFitWindowSize)
					{
						// Too long - Scaled height also
						if(newHeight > panelPadHeight)
						{
							newWidth = newWidth * (panelPadHeight / newHeight);
							newHeight = panelPadHeight;
							
							System.out.println("4 Width " + newWidth);
							System.out.println("4 Height " + newHeight);
						}
					}
				}
			}
			
			// If a fit scaling mode is set work out what scale it use using, so the not fit scaling starting point is correct if fit is switched off.
			if(scaleModeFitWindowWidth || scaleModeFitWindowSize)
			{
				scale = 1f * (newWidth / pageImageSize[0]);
				
				// Adjust pad
				newWidth = newWidth - PAD_SIZE;
			}
			
			float imageHalfWidth = (newWidth / 2);
			float imageHalfHeight = (newHeight / 2);
			
			// Centre
			float iX = ((pos[0] * scale) + imageHalfWidth) - (panelPadWidth / 2);
			float iY;
			
			if(scaleModeFitWindowWidth)
			{
				// Don't Centre height
				iY = ((pos[1] * scale));
			}
			else
			{
				iY = ((pos[1] * scale) + imageHalfHeight) - (panelPadHeight / 2);
			}
			
			g2.setStroke(new BasicStroke(LINE));
			
			g2.setColor(BORDER_COLOR);
			g2.drawRect(((int) -iX) - 1, ((int) -iY) - 1, ((int) newWidth) + 2, ((int) newHeight) + 2);
			
			g2.drawImage(paintImage, (int) -iX, (int) -iY, (int) newWidth, (int) newHeight, null);
		}
	}
	
	/*
	 * *******************************************************************************************************************************************
	 * Render mode switch timer
	 ********************************************************************************************************************************************/
	
	private void createTimer()
	{
		// 1000hz
		if(imagePaintTimer == null)
		{
			imagePaintTimer = new Timer("Image Paint Timer");
			
			imagePaintTimer.scheduleAtFixedRate(new TimerTask()
			{
				private boolean fullResDrawDone;
				private boolean fullQualDone;
				
				private long lastScaleTime = System.currentTimeMillis();
				private long lastFullDrawTime = System.currentTimeMillis();
				
				private long minScaleDrawTime = 10;
				private long minFullDrawTime = 100;
				private long fullDrawTimeOut = 500;
				
				@Override
				public void run()
				{
					long timeNow = System.currentTimeMillis();
					
					if(continousUpdate)
					{
						revalidate();
						repaint();
					}
					{
						if((timeNow - lastActivityTime) < minScaleDrawTime)
						{
							if((timeNow - lastScaleTime) > minScaleDrawTime)
							{
								// To avoid sluggish dragging
								if(timerLastScale != (int) (scale * 100000))
								{
									
									timerLastScale = (int) (scale * 100000);
									System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> fastDraw");
									
									fullResDrawDone = false;
									
									imageMode = 2;
									revalidate();
									repaint();
									lastScaleTime = System.currentTimeMillis();
								}
							}
						}
						
						if((timeNow - lastScaleTime) > minFullDrawTime)
						{
							if(!fullResDrawDone)
							{
								System.out.println("============================================================================= Draw");
								fullResDrawDone = true;
								fullQualDone = false;
								
								imageMode = 1;
								revalidate();
								repaint();
								lastFullDrawTime = System.currentTimeMillis();
							}
							
							if(((timeNow - lastFullDrawTime) > fullDrawTimeOut) && !fullQualDone)
							{
								System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TIMEOUT");
								
								imageMode = 0;
								revalidate();
								repaint();
								
								fullQualDone = true;
							}
						}
					}
				}
				
			}, 0, 1);
		}
	}
}
