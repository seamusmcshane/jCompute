package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;
import jCompute.util.JCMath;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class HeatMap extends JPanel
{
	private MapperRemapper reMapper;
	private int heatMapWidth;
	private int heatMapHeight;
	private float ratio;
	private float fontSizeScale;
	
	private BufferedImage chartImage;
	
	public HeatMap(int heatMapWidth, int heatMapHeight)
	{
		this.heatMapWidth = heatMapWidth;
		this.heatMapHeight = heatMapHeight;
		
		// Ratio
		ratio = (float) heatMapWidth / (float) heatMapHeight;
		System.out.println("ratio " + ratio);
		
		float fscale = heatMapWidth/1000f;
		
		fontSizeScale = fscale*0.75f;
		
		this.setPreferredSize(new Dimension(heatMapWidth, heatMapHeight));
		this.setMinimumSize(new Dimension(heatMapWidth, heatMapHeight));
		this.setSize(new Dimension(heatMapWidth, heatMapHeight));
	}
	
	public void setLog(BatchLogInf mapper)
	{
		// Chart Area
		chartImage = new BufferedImage(heatMapWidth, heatMapHeight, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D cg = chartImage.createGraphics();
		
		cg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		Font defaultFont = new Font("Sans-Serif", Font.BOLD | Font.BOLD, 12);
		
		cg.setFont(defaultFont);
		
		cg.setColor(Color.white);
		cg.fillRect(0, 0, heatMapWidth - 1, heatMapHeight - 1);
		cg.setColor(Color.black);
		cg.drawRect(0, 0, heatMapWidth - 1, heatMapHeight - 1);
		
		BufferedImage rawValues = createMapValues(mapper);
		
		// HeatMapPlot
		BufferedImage heatMapPlotImage = heapMapPlotImage(rawValues);
		float heatMapPlotImageSizeScale = 0.4f;
		float heatMapPlotXOffsetScale = 0.4f;
		float heatMapPlotYOffsetScale = 0.50f;
		
		int heatMapPlotWidth = (int) (heatMapWidth * heatMapPlotImageSizeScale);
		int heatMapPlotHeight = (int) ((heatMapHeight * heatMapPlotImageSizeScale) * ratio);
		int heatMapPlotXOffset = (int) (heatMapWidth * heatMapPlotXOffsetScale);
		int heatMapPlotYOffset = (int) ((heatMapHeight * (heatMapPlotYOffsetScale)) - (heatMapPlotHeight / 2));
		
		// Draw HeatMapPlot to Chart
		cg.drawImage(heatMapPlotImage, heatMapPlotXOffset, heatMapPlotYOffset, heatMapPlotWidth, heatMapPlotHeight, null);
		
		cg.setColor(Color.black);
		cg.drawRect(heatMapPlotXOffset - 1, heatMapPlotYOffset - 1, heatMapPlotWidth + 1, heatMapPlotHeight + 1);
		
		addTickAndLabels(mapper, cg, heatMapPlotXOffset, heatMapPlotYOffset, heatMapPlotWidth, heatMapPlotHeight, 15, ratio);
		
		addLegend(mapper, cg, heatMapPlotXOffset, heatMapPlotYOffset, heatMapPlotWidth, heatMapPlotHeight, ratio);
		
		addColorMap(mapper, cg, heatMapPlotXOffset, heatMapPlotYOffset, heatMapPlotWidth, heatMapPlotHeight, ratio);
		
		cg.dispose();
	}
	
	private void addColorMap(BatchLogInf mapper, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight, float ratio)
	{
		int x = (int) (heatMapWidth * 0.85f);
		int y = heatMapPlotYOffset;
		
		int width = (int) (heatMapWidth * 0.05f);
		int height = heatMapPlotHeight;
		
		int size = heatMapPlotHeight;
		
		int[] palette = reMapper.getPalette();
		int paletteSize = palette.length;
		
		float pIncr = ((float) paletteSize / (float) size);
		System.out.println("paletteSize " + paletteSize);
		System.out.println("Size " + size);
		System.out.println("pIncr " + pIncr);
		
		float pVal = 0;
		for(int i = size; i > 0; i--)
		{
			cg.setColor(new Color(palette[(int) pVal]));
			cg.fillRect(x, y + i, width, 2);
			pVal += pIncr;
			
			// System.out.println("pVal " + pVal);
		}
		
		cg.setColor(Color.BLACK);
		cg.drawRect(x - 1, y, width + 1, height + 1);
	}
	
	private void addLegend(BatchLogInf mapper, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight, float ratio)
	{
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(((fontLatch.getSize() * ratio) * fontSizeScale));
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		
		cg.drawRect((int) (heatMapWidth * 0.05f), heatMapPlotYOffset, (int) (heatMapWidth * 0.25f), (int) (heatMapHeight * 0.4f));
		
		 cg.drawString("X 	: " + mapper.getXAxisName(), (int) (heatMapWidth * 0.05f)+20, heatMapPlotYOffset + (fontHeight*2));
		 cg.drawString("Y 	: " + mapper.getYAxisName(), (int) (heatMapWidth * 0.05f)+20, heatMapPlotYOffset + (fontHeight*4));
		 cg.drawString("Sta : " + mapper.getMaxRate(), (int) (heatMapWidth * 0.05f)+20, heatMapPlotYOffset + (fontHeight*6));
		 
		 cg.setFont(fontLatch);
	}
	
	private void addTickAndLabels(BatchLogInf mapper, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight, int tickLength, float ratio)
	{
		int tickX = mapper.getXSteps() - 1;
		int tickY = mapper.getYSteps() - 1;
		int tickLen = (int) (tickLength * ratio);
		
		int maxTicks = 10;
		
		if(tickX > maxTicks)
		{
			for(int i = maxTicks; i > 0; i--)
			{
				if(tickX % i == 0)
				{
					tickX = i;
					break;
				}
			}
		}
		
		if(tickY > maxTicks)
		{
			for(int i = maxTicks; i > 0; i--)
			{
				if(tickY % i == 0)
				{
					tickY = i;
					break;
				}
			}
		}
		
		int strokeSize = 1;
		float xIncr = (float) (heatMapPlotWidth + 1) / (float) tickX;
		float yIncr = (float) (heatMapPlotHeight + 1) / (float) tickY;
		
		cg.setColor(Color.black);
		cg.setStroke(new BasicStroke(strokeSize));
		
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(((fontLatch.getSize() * ratio) * fontSizeScale));
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		
		boolean intValues = false;
		
		// Vals
		float xValMin = (float) mapper.getXValMin();
		float xValMax = (float) mapper.getXValMax();
		float xValStep = (float) ((xValMax - xValMin) / tickX);
		
		float yValMin = (float) mapper.getYValMin();
		float yValMax = (float) mapper.getYValMax();
		float yValStep = (float) ((yValMax - yValMin) / tickY);
		
		// Integer Detection
		if((xValMin == (int) xValMin) && (xValMax == (int) xValMax))
		{
			intValues = true;
		}
		
		System.out.println("Equal " + (xValMin == (int) xValMin));
		
		// To center Y Strings Vertically
		int maxStringWidth = 0;
		
		float xOffset = 0;
		float xDval = xValMin;
		for(int x = 0; x < tickX + 1; x++)
		{
			cg.drawLine(heatMapPlotXOffset - 1 + (int) xOffset, heatMapPlotHeight + heatMapPlotYOffset, heatMapPlotXOffset - 1 + (int) xOffset, heatMapPlotYOffset - 1 + heatMapPlotHeight + tickLen);
			
			String sval;
			
			if(intValues)
			{
				sval = String.valueOf((int) xDval);
			}
			else
			{
				// Correct usable numbers.
				if(xDval == Float.NaN)
				{
					xDval = 0.0f;
				}
				
				if(xDval == Float.NEGATIVE_INFINITY)
				{
					xDval = -Float.MAX_VALUE;
				}
				
				if(xDval == Float.POSITIVE_INFINITY)
				{
					xDval = Float.MAX_VALUE;
				}
				
				// Round the floats
				sval = String.valueOf(JCMath.round(xDval, 4));
			}
			
			int sWidth = (metrics.stringWidth(sval) / 2) + 2;
			
			maxStringWidth = Math.max(maxStringWidth, sWidth);
			
			cg.drawString(sval, (heatMapPlotXOffset + (int) xOffset) - sWidth, (heatMapPlotHeight + heatMapPlotYOffset) + (tickLen + fontHeight));
			
			xDval += xValStep;
			xOffset += xIncr;
		}
		
		float yDval = yValMin;
		float yOffset = 1;
		for(int y = 0; y < tickY + 1; y++)
		{
			cg.drawLine(heatMapPlotXOffset - 1 - tickLen, (heatMapPlotHeight - (int) yOffset) + heatMapPlotYOffset + 1, heatMapPlotXOffset - 1,
					(heatMapPlotHeight - (int) yOffset) + heatMapPlotYOffset + 1);
					
			String sval;
			
			if(intValues)
			{
				sval = String.valueOf((int) yDval);
			}
			else
			{
				// Correct usable numbers.
				if(yDval == Float.NaN)
				{
					yDval = 0.0f;
				}
				
				if(yDval == Float.NEGATIVE_INFINITY)
				{
					yDval = -Float.MAX_VALUE;
				}
				
				if(yDval == Float.POSITIVE_INFINITY)
				{
					yDval = Float.MAX_VALUE;
				}
				
				// Round the floats
				sval = String.valueOf(JCMath.round(yDval, 4));
			}
			
			int sWidth = metrics.stringWidth(sval) / 2;
			
			cg.drawString(sval, ((heatMapPlotXOffset - 1 - sWidth) - (tickLen)) - maxStringWidth, (heatMapPlotHeight - (int) yOffset) + heatMapPlotYOffset + 1 + (fontHeight / 4));
			
			yDval += yValStep;
			yOffset += yIncr;
		}
		
		// Reset Font
		cg.setFont(fontLatch);
	}
	
	private BufferedImage heapMapPlotImage(BufferedImage rawValues)
	{
		int width = rawValues.getWidth();
		int height = rawValues.getHeight();
		
		// Resize
		Image tmp = rawValues.getScaledInstance(width, height, Image.SCALE_FAST);
		BufferedImage heatMapPlot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = heatMapPlot.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		
		// g2d.setColor(Color.black);
		// g2d.drawRect(0, 0, width - 1, height - 1);
		
		g2d.dispose();
		
		return heatMapPlot;
	}
	
	private BufferedImage createMapValues(BatchLogInf mapper)
	{
		int maxSteps = Math.max(mapper.getXSteps(), mapper.getYSteps());
		float stepsRatio = (float) mapper.getXSteps() / (float) mapper.getYSteps();
		
		System.out.println("stepsRatio " + stepsRatio);
		
		BufferedImage rawValues = new BufferedImage(mapper.getXSteps(), mapper.getYSteps(), BufferedImage.TYPE_INT_RGB);
		
		reMapper = (MapperRemapper) mapper.getAvg();
		
		reMapper.populateImage(rawValues);
		
		// Flip
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -rawValues.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		rawValues = op.filter(rawValues, null);
		
		return rawValues;
	}
	
	private void paintValues(Graphics g, int x, int y)
	{
		Graphics2D g2d = (Graphics2D) g;
		
		if(chartImage != null)
		{
			g.drawImage(chartImage, 0, 0, chartImage.getWidth(), chartImage.getHeight(), null);
		}
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		g.setColor(Color.black);
		paintValues(g, 0, 0);
	}
	
	public BufferedImage getImage()
	{
		return chartImage;
	}
}
