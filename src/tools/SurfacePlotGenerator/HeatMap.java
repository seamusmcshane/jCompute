package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchLogProcessor;
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
	
	private float fontSizeScale;
	private int imageScale;
	
	private boolean legend;
	
	private BufferedImage chartImage;
	
	public HeatMap(int heatMapWidth, boolean legend, int scale)
	{
		this.heatMapWidth = heatMapWidth;
		
		if(legend)
		{
			this.heatMapHeight = (int) ((double) heatMapWidth * 0.6);
		}
		else
		{
			this.heatMapHeight = this.heatMapWidth;
		}
		
		this.legend = legend;
		this.imageScale = scale;
		
		fontSizeScale = 14f * (float) scale;
		
		this.setPreferredSize(new Dimension(heatMapWidth, heatMapHeight));
		this.setMinimumSize(new Dimension(heatMapWidth, heatMapHeight));
		this.setSize(new Dimension(heatMapWidth, heatMapHeight));
	}
	
	public void setLog(BatchLogProcessor logProcessor)
	{
		// Chart Image
		chartImage = new BufferedImage(heatMapWidth, heatMapHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D cg = chartImage.createGraphics();
		
		// Default Font
		Font defaultFont = new Font("Sans-Serif", Font.BOLD | Font.BOLD, 12);
		cg.setFont(defaultFont);
		// Font smoothing
		cg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Blank Chart Area
		cg.setColor(Color.white);
		cg.fillRect(0, 0, heatMapWidth - 1, heatMapHeight - 1);
		cg.setColor(Color.black);
		cg.drawRect(0, 0, heatMapWidth - 1, heatMapHeight - 1);
		
		// HeatMapPlot Scaling
		double heatMapPlotImageSizeScale;
		
		// HeatMapPlot Scaling
		double heatMapPlotXOffsetScale;
		
		if(legend)
		{
			heatMapPlotImageSizeScale = 0.4;
			heatMapPlotXOffsetScale = 0.4;
		}
		else
		{
			heatMapPlotImageSizeScale = 0.8;
			heatMapPlotXOffsetScale = 0.1;
		}
		
		// Base Ratio
		double ratio = (double) heatMapWidth / (double) heatMapHeight;
		System.out.println("ratio " + ratio);
		
		// Scales the lines
		cg.setColor(Color.black);
		cg.setStroke(new BasicStroke(imageScale));
		
		// Scales TickLens
		int tickLen = (int) (20 * imageScale);
		
		int heatMapPlotWidth = (int) (heatMapWidth * heatMapPlotImageSizeScale);
		int heatMapPlotHeight = (int) ((heatMapHeight * heatMapPlotImageSizeScale) * ratio);
		
		int heatMapPlotXOffset = (int) (heatMapWidth * heatMapPlotXOffsetScale) + tickLen / 2;
		int heatMapPlotYOffset = (int) ((heatMapHeight / 2 - ((tickLen + heatMapPlotHeight + tickLen) * 0.5)));
		
		cg.setColor(Color.black);
		// Draw HeatMapPlot to Chart
		addHeatmapImage(logProcessor, cg, heatMapPlotXOffset, heatMapPlotYOffset, heatMapPlotWidth, heatMapPlotHeight);
		
		cg.setColor(Color.black);
		addPlotTickAndLabels(logProcessor, cg, heatMapPlotXOffset, heatMapPlotYOffset, heatMapPlotWidth, heatMapPlotHeight, tickLen);
		
		if(legend)
		{
			addLegend(logProcessor, cg, heatMapPlotXOffset, heatMapPlotYOffset, heatMapPlotWidth, heatMapPlotHeight);
			addColorMap(logProcessor, cg, heatMapPlotXOffset, heatMapPlotYOffset, (int) (heatMapWidth * 0.05f), heatMapPlotHeight);
			addColorMapTickAndLabels(logProcessor, cg, heatMapPlotXOffset, heatMapPlotYOffset, (int) (heatMapWidth * 0.05f), heatMapPlotHeight, tickLen);
		}
		
		cg.dispose();
	}
	
	private void addHeatmapImage(BatchLogProcessor logProcessor, Graphics2D target, int x, int y, int width, int height)
	{
		// un-scaled heatmap image 1/1
		BufferedImage rawValues = createMapValues(logProcessor);
		
		// scaled
		BufferedImage heatMapPlotImage = heapMapPlotImage(rawValues, width, height);
		
		target.drawImage(heatMapPlotImage, x + 1, y + 1, width - 1, height - 1, null);
		
		// Add outline rect
		target.drawRect(x, y, width, height);
	}
	
	private void addColorMap(BatchLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int width, int height)
	{
		int x = (int) (heatMapWidth * 0.83);
		int y = heatMapPlotYOffset;
		
		int[] palette = reMapper.getPalette();
		int paletteSize = palette.length - 1;
		
		double pIncr = ((double) paletteSize / (double) height);
		System.out.println("paletteSize " + paletteSize);
		System.out.println("height " + height);
		System.out.println("pIncr " + pIncr);
		
		double pVal = 0;
		for(int i = height - 1; i >= 0; i--)
		{
			cg.setColor(new Color(palette[(int) (pVal)]));
			// cg.fillRect(x, y - 1 + i, width - 1, 2);
			
			cg.drawLine(x + 1, y + 1 + i, x - 1 + width, y + 1 + i);
			pVal += pIncr;
			
			// System.out.println("pVal " + pVal);
		}
		
		cg.setColor(Color.BLACK);
		cg.drawRect(x, y, width, height);
	}
	
	private void addColorMapTickAndLabels(BatchLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight, int tickLength)
	{
		int width = (int) (heatMapWidth * 0.05);
		int x = (int) (heatMapWidth * 0.83) + width;
		
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(fontSizeScale);
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		double labelPad = (double) tickLength / 2;
		
		int tickY = 100000;
		int maxTicks = 10;
		
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
		
		double yIncr = (double) (heatMapPlotHeight) / (double) tickY;
		
		double yValMin = (double) logProcessor.getZValMin();
		double yValMax = (double) logProcessor.getZValMax();
		
		boolean intValues = false;
		
		if(yValMin == (int) yValMin && yValMax == (int) yValMax)
		{
			intValues = true;
		}
		
		NiceTickScaler tickScaler = new NiceTickScaler(logProcessor.getZValMin(), logProcessor.getZValMax());
		tickScaler.setMaxTicks(tickY);
		
		double yValStep = tickScaler.getTickSpacing();
		
		double yDval = tickScaler.getNiceMin();
		for(int y = 0; y < tickY + 1; y++)
		{
			double lineX = x;
			double lineY = (heatMapPlotHeight + heatMapPlotYOffset) - (yIncr * y);
			double lineWidth = x + tickLength;
			
			cg.drawLine((int) lineX, (int) lineY, (int) lineWidth, (int) lineY);
			
			String sval;
			
			if(intValues)
			{
				sval = String.valueOf((int) yDval);
			}
			else
			{
				// Correct usable numbers.
				if(yDval == Double.NaN)
				{
					yDval = 0.0;
				}
				
				if(yDval == Double.NEGATIVE_INFINITY)
				{
					yDval = -Double.MAX_VALUE;
				}
				
				if(yDval == Double.POSITIVE_INFINITY)
				{
					yDval = Double.MAX_VALUE;
				}
				
				// Round the doubles
				sval = String.valueOf(JCMath.round(yDval, 4));
			}
			
			int sWidth = metrics.stringWidth(sval) / 2;
			
			double sX = (lineWidth + sWidth) - sWidth + labelPad;
			double sY = lineY + (fontHeight / 4);
			
			// Draw String
			cg.drawString(sval, (float) sX, (float) sY);
			
			yDval += yValStep;
		}
		
		// Value Label
		double zAxisNameWidth = metrics.stringWidth(logProcessor.getZAxisName()) * 0.5;
		
		int cx = (int) ((heatMapWidth * 0.85) + (width * 0.5));
		double zAnx = (cx) - (zAxisNameWidth);
		cg.drawString(logProcessor.getZAxisName(), (float) zAnx, (float) (heatMapPlotYOffset - (fontHeight * 0.5)));
	}
	
	private void addLegend(BatchLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight)
	{
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(fontSizeScale);
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		
		String[] legend =
		{
			"X 		: " + logProcessor.getXAxisName(), "Y 		: " + logProcessor.getYAxisName(), "SRate : " + logProcessor.getMaxRate()
		};
		
		double max = 0;
		int pad = (int) (20 * imageScale);
		
		for(int s = 0; s < legend.length; s++)
		{
			max = Math.max(max, metrics.stringWidth(legend[s]));
			
			cg.drawString(legend[s], (int) (heatMapWidth * 0.05) + pad, (int) (heatMapPlotYOffset + (fontHeight * (2 * (s + 1)))));
		}
		
		cg.drawRect((int) (heatMapWidth * 0.05), heatMapPlotYOffset, (int) max + (pad * 2), (int) ((fontHeight * (2 * (legend.length + 1)))) - fontHeight / 2);
		
		cg.setFont(fontLatch);
	}
	
	private void addPlotTickAndLabels(BatchLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight, int tickLength)
	{
		int tickX = logProcessor.getXSteps() - 1;
		int tickY = logProcessor.getYSteps() - 1;
		
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
		
		double xIncr = (double) heatMapPlotWidth / (double) tickX;
		double yIncr = (double) (heatMapPlotHeight) / (double) tickY;
		
		cg.setColor(Color.black);
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(fontSizeScale);
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		
		double labelPad = (double) tickLength / 2;
		
		boolean intValues = false;
		
		// Vals
		double xValMin = (double) logProcessor.getXValMin();
		double xValMax = (double) logProcessor.getXValMax();
		NiceTickScaler xTickScaler = new NiceTickScaler(logProcessor.getXValMin(), logProcessor.getXValMax());
		xTickScaler.setMaxTicks(tickY);
		double xValStep = xTickScaler.getTickSpacing();
		
		double yValMin = (double) logProcessor.getYValMin();
		double yValMax = (double) logProcessor.getYValMax();
		NiceTickScaler yTickScaler = new NiceTickScaler(logProcessor.getYValMin(), logProcessor.getYValMax());
		yTickScaler.setMaxTicks(tickY);
		double yValStep = yTickScaler.getTickSpacing();
		
		// Integer Detection
		if((xValMin == (int) xValMin) && (xValMax == (int) xValMax))
		{
			intValues = true;
		}
		
		System.out.println("Equal " + (xValMin == (int) xValMin));
		
		double xDval = xTickScaler.getNiceMin();
		double yDval = yTickScaler.getNiceMin();
		
		for(int x = 0; x < tickX + 1; x++)
		{
			double lineX = heatMapPlotXOffset + (xIncr * x);
			double lineY = heatMapPlotHeight + heatMapPlotYOffset;
			double lineHeight = lineY + tickLength;
			
			cg.drawLine((int) lineX, (int) lineY, (int) lineX, (int) lineHeight);
			
			String sval;
			
			if(intValues)
			{
				sval = String.valueOf((int) xDval);
			}
			else
			{
				// Correct usable numbers.
				if(xDval == Double.NaN)
				{
					xDval = 0.0;
				}
				
				if(xDval == Double.NEGATIVE_INFINITY)
				{
					xDval = -Double.MAX_VALUE;
				}
				
				if(xDval == Double.POSITIVE_INFINITY)
				{
					xDval = Double.MAX_VALUE;
				}
				
				// Round the doubles
				sval = String.valueOf(JCMath.round(xDval, 4));
			}
			
			double sWidth = (metrics.stringWidth(sval) * 0.5) + 2;
			
			double sX = lineX - sWidth;
			double sY = lineHeight + labelPad + (fontHeight * 0.5);
			
			// Draw String
			cg.drawString(sval, (float) sX, (float) sY);
			
			xDval += xValStep;
		}
		
		// Value Label
		double xAxisNameWidth = metrics.stringWidth(logProcessor.getXAxisName()) * 0.5;
		
		int cX = (int) ((heatMapPlotXOffset) + (heatMapPlotWidth * 0.5));
		double xAnx = (cX) - (xAxisNameWidth);
		cg.drawString(logProcessor.getXAxisName(), (float) xAnx, (float) ((heatMapPlotHeight + heatMapPlotYOffset + tickLength + fontHeight + labelPad) + labelPad + (fontHeight / 2)));
		
		for(int y = 0; y < tickY + 1; y++)
		{
			double lineX = heatMapPlotXOffset;
			double lineY = (heatMapPlotHeight + heatMapPlotYOffset) - (yIncr * y);
			double lineWidth = heatMapPlotXOffset - tickLength;
			
			cg.drawLine((int) lineX, (int) lineY, (int) lineWidth, (int) lineY);
			
			String sval;
			
			if(intValues)
			{
				sval = String.valueOf((int) yDval);
			}
			else
			{
				// Correct usable numbers.
				if(yDval == Double.NaN)
				{
					yDval = 0.0;
				}
				
				if(yDval == Double.NEGATIVE_INFINITY)
				{
					yDval = -Double.MAX_VALUE;
				}
				
				if(yDval == Double.POSITIVE_INFINITY)
				{
					yDval = Double.MAX_VALUE;
				}
				
				// Round the doubles
				sval = String.valueOf(JCMath.round(yDval, 4));
			}
			
			int sWidth = metrics.stringWidth(sval) / 2;
			
			double sX = (lineWidth - sWidth) - sWidth - labelPad;
			double sY = lineY + (fontHeight / 4);
			
			// Draw String
			cg.drawString(sval, (float) sX, (float) sY);
			
			yDval += yValStep;
		}
		
		// Value Label
		double yAxisNameWidth = metrics.stringWidth(logProcessor.getYAxisName()) * 0.5;
		
		int cY = (int) ((heatMapPlotYOffset) + (heatMapPlotHeight * 0.5));
		double yAny = (cY) - (fontHeight / 2);
		
		AffineTransform at = new AffineTransform();
		
		double rX = (heatMapPlotXOffset - tickLength - labelPad - fontHeight - labelPad - fontHeight);
		double rY = yAny + yAxisNameWidth;
		
		at.setToRotation(Math.toRadians(-90), rX, rY);
		cg.setTransform(at);
		cg.drawString(logProcessor.getYAxisName(), (float) rX, (float) rY);
		at.setToRotation(0);
		cg.setTransform(at);
		
		// Reset Font
		cg.setFont(fontLatch);
	}
	
	private BufferedImage heapMapPlotImage(BufferedImage rawValues, int width, int height)
	{
		// Resize
		Image tmp = rawValues.getScaledInstance(width, height, Image.SCALE_FAST);
		BufferedImage heatMapPlot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = heatMapPlot.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		
		return heatMapPlot;
	}
	
	private BufferedImage createMapValues(BatchLogProcessor logProcessor)
	{
		int maxSteps = Math.max(logProcessor.getXSteps(), logProcessor.getYSteps());
		double stepsRatio = (double) logProcessor.getXSteps() / (double) logProcessor.getYSteps();
		
		System.out.println("stepsRatio " + stepsRatio);
		
		BufferedImage rawValues = new BufferedImage(logProcessor.getXSteps(), logProcessor.getYSteps(), BufferedImage.TYPE_INT_RGB);
		
		reMapper = (MapperRemapper) logProcessor.getAvg();
		
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
