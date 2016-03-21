package tools.HeatMap;

import jCompute.Batch.LogFileProcessor.ItemLogProcessorInf.ComputedMetric;
import jCompute.Batch.LogFileProcessor.ItemLogProcessor;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface.SurfaceMetricInf.Type;
import jCompute.Gui.View.Misc.Palette;
import jCompute.Timing.TimerObj;
import jCompute.util.JCMath;
import jCompute.util.Text;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.util.stream.IntStream;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeatMap extends JPanel
{
	private static final long serialVersionUID = 6954044353596181009L;

	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(HeatMap.class);
	
	private TimerObj to = new TimerObj();
	
	private int heatMapWidth;
	private int heatMapHeight;
	
	private float fontSizeScale;
	private int imageScale;
	
	private boolean legend;
	
	private BufferedImage chartImage;
	private ColorModel linearRGB;
	
	private final int PALETTE_SIZE = 100;
	private int[] palette;
	
	private long totalTime;
	
	public HeatMap(int heatMapWidth, boolean legend, int scale)
	{
		this.heatMapWidth = heatMapWidth;
		
		if(legend)
		{
			this.heatMapHeight = (int) (heatMapWidth * 0.6);
		}
		else
		{
			this.heatMapHeight = this.heatMapWidth;
		}
		
		this.legend = legend;
		this.imageScale = scale;
		
		fontSizeScale = 14f * scale;
		
		this.setPreferredSize(new Dimension(heatMapWidth, heatMapHeight));
		this.setMinimumSize(new Dimension(heatMapWidth, heatMapHeight));
		this.setSize(new Dimension(heatMapWidth, heatMapHeight));
		
		palette = Palette.paletteFromPaletteName("LabSpecturmPalette", false, PALETTE_SIZE, true);
	}
	
	public void setLog(ItemLogProcessor logProcessor)
	{
		// Linear RGB ColorModel
		linearRGB = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), 32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000, false, DataBuffer.TYPE_INT);
		
		// Chart Image
		chartImage = new BufferedImage(linearRGB, linearRGB.createCompatibleWritableRaster(heatMapWidth, heatMapHeight), false, null);
		
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
		// System.out.println("ratio " + ratio);
		
		// Scales the lines
		cg.setColor(Color.black);
		cg.setStroke(new BasicStroke(imageScale));
		
		// Scales TickLens
		int tickLen = 20 * imageScale;
		
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
	
	private void addHeatmapImage(ItemLogProcessor logProcessor, Graphics2D target, int x, int y, int width, int height)
	{
		// Unscaled heatmap image 1/1
		BufferedImage rawValuesImage = createRawValuesImage(logProcessor);
		
		// Scaled
		BufferedImage heatMapPlotImage = createScaledHeapMapPlotImage(rawValuesImage, width, height);
		
		target.drawImage(heatMapPlotImage, x + 1, y + 1, width - 1, height - 1, null);
		
		// Add outline rect
		target.drawRect(x, y, width, height);
	}
	
	private void addColorMap(ItemLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int width, int height)
	{
		to.startTimer();
		
		int x = (int) (heatMapWidth * 0.83);
		int y = heatMapPlotYOffset;
		
		int paletteSize = palette.length;
		// System.out.println("paletteSize " + paletteSize);
		
		double pIncr = ((double) paletteSize / (double) height);
		// System.out.println("paletteSize " + paletteSize);
		// System.out.println("height " + height);
		// System.out.println("pIncr " + pIncr);
		
		double pVal = 0;
		for(int i = height; i > 0; i--)
		{
			cg.setColor(new Color(palette[(int) (Math.floor(pVal))]));
			
			cg.drawLine(x + 1, y + i, (x + width) - 1, y + i);
			
			pVal += pIncr;
			
			// System.out.println("pVal " + pVal);
		}
		
		cg.setColor(Color.BLACK);
		cg.drawRect(x, y, width, height);
		
		to.stopTimer();
		
		log.info("ColorMap : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
	}
	
	private void addColorMapTickAndLabels(ItemLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight, int tickLength)
	{
		to.startTimer();
		
		int width = (int) (heatMapWidth * 0.05);
		int x = (int) (heatMapWidth * 0.83) + width;
		
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(fontSizeScale);
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		double labelPad = (double) tickLength / 2;
		
		// zTicks
		int zSteps = (int) logProcessor.getZValRangeMax();
		int tickZ = zSteps / 1000;
		
		int maxTicks = 10;
		
		if(tickZ > maxTicks)
		{
			for(int i = maxTicks; i > 0; i--)
			{
				if(tickZ % i == 0)
				{
					tickZ = i;
					break;
				}
			}
		}
		// System.out.println("TickZ " + tickZ);
		
		double yIncr = (double) heatMapPlotHeight / (double) tickZ;
		// System.out.println("yIncr " + yIncr);
		
		// Vals
		double zValMin = logProcessor.getZValRangeMin();
		double zValMax = logProcessor.getZValRangeMax();
		// System.out.println("zValMin " + zValMin);
		// System.out.println("zValMax " + zValMax);
		
		double zTickStepInc = zSteps / tickZ;
		// System.out.println("zTickStepInc " + zTickStepInc);
		
		double zStepValInc = JCMath.round(((zValMax - zValMin) / (zSteps - 1)), 0);
		// System.out.println("zStepValInc " + zStepValInc);
		
		double zValStep = zTickStepInc * zStepValInc;
		// System.out.println("zValStep " + zValStep);
		
		double zDval = zValMin;
		
		boolean intValues = false;
		
		// Integer Detection
		if((zValMin == (int) zValMin) && (zValMax == (int) zValMax))
		{
			intValues = true;
		}
		
		for(int y = 0; y < tickZ + 1; y++)
		{
			double lineX = x;
			double lineY = (heatMapPlotHeight + heatMapPlotYOffset) - (yIncr * y);
			double lineWidth = x + tickLength;
			
			cg.drawLine((int) lineX, (int) lineY, (int) lineWidth, (int) lineY);
			
			String sval;
			
			if(intValues)
			{
				sval = String.valueOf((int) zDval);
			}
			else
			{
				// Correct usable numbers.
				if(zDval == Double.NEGATIVE_INFINITY)
				{
					zDval = -Double.MAX_VALUE;
				}
				
				if(zDval == Double.POSITIVE_INFINITY)
				{
					zDval = Double.MAX_VALUE;
				}
				
				// Round the doubles
				sval = String.valueOf(JCMath.round(zDval, 4));
			}
			
			int sWidth = metrics.stringWidth(sval) / 2;
			
			double sX = (lineWidth + sWidth) - sWidth + labelPad;
			double sY = lineY + (fontHeight / 4);
			
			// Draw String
			cg.drawString(sval, (float) sX, (float) sY);
			
			zDval += zValStep;
		}
		
		// Value Label
		double zAxisNameWidth = metrics.stringWidth(logProcessor.getZAxisName()) * 0.5;
		
		int cx = (int) ((heatMapWidth * 0.85) + (width * 0.5));
		double zAnx = (cx) - (zAxisNameWidth);
		cg.drawString(logProcessor.getZAxisName(), (float) zAnx, (float) (heatMapPlotYOffset - (fontHeight * 0.5)));
		
		to.stopTimer();
		
		log.info("ColorMap Ticks And Labels : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
	}
	
	private void addLegend(ItemLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight)
	{
		to.startTimer();
		
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(fontSizeScale);
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		
		String[] legend =
		{
			"X 		: " + logProcessor.getXAxisName(), "Y 		: " + logProcessor.getYAxisName(), "SRate : " + logProcessor.getComputedMetric(ComputedMetric.MAX_RATE), "ZMin : " + logProcessor.getZmin(), "ZMax : " + logProcessor.getZmax()
		};
		
		double max = 0;
		int pad = 20 * imageScale;
		
		for(int s = 0; s < legend.length; s++)
		{
			max = Math.max(max, metrics.stringWidth(legend[s]));
			
			cg.drawString(legend[s], (int) (heatMapWidth * 0.05) + pad, heatMapPlotYOffset + (fontHeight * (2 * (s + 1))));
		}
		
		cg.drawRect((int) (heatMapWidth * 0.05), heatMapPlotYOffset, (int) max + (pad * 2), ((fontHeight * (2 * (legend.length + 1)))) - fontHeight / 2);
		
		cg.setFont(fontLatch);
		
		to.stopTimer();
		
		log.info("Legend : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
	}
	
	private void addPlotTickAndLabels(ItemLogProcessor logProcessor, Graphics2D cg, int heatMapPlotXOffset, int heatMapPlotYOffset, int heatMapPlotWidth, int heatMapPlotHeight, int tickLength)
	{
		to.startTimer();
		
		// FONT
		cg.setColor(Color.black);
		Font fontLatch = cg.getFont();
		Font newFont = fontLatch.deriveFont(fontSizeScale);
		cg.setFont(newFont);
		FontMetrics metrics = cg.getFontMetrics(newFont);
		int fontHeight = metrics.getHeight() + 2;
		
		double labelPad = (double) tickLength / 2;
		
		// xTicks
		
		int xSteps = logProcessor.getXSteps();
		int tickX = logProcessor.getXSteps();
		
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
		// System.out.println("TickX " + tickX);
		
		double xIncr = (double) heatMapPlotWidth / (double) tickX;
		
		// Vals
		double xValMin = logProcessor.getXValMin();
		double xValMax = logProcessor.getXValMax();
		// System.out.println("xValMin " + xValMin);
		// System.out.println("xValMax " + xValMax);
		
		double xTickStepInc = xSteps / tickX;
		// System.out.println("xTickStepInc " + xTickStepInc);
		
		double xStepValInc = (xValMax - xValMin) / (xSteps - 1);
		// System.out.println("xStepValInc " + xStepValInc);
		
		double xValStep = xTickStepInc * xStepValInc;
		// System.out.println("xValStep " + xValStep);
		
		boolean intValues = false;
		
		// Integer Detection
		if((xValMin == (int) xValMin) && (xValMax == (int) xValMax))
		{
			intValues = true;
		}
		
		double xDval = xValMin;
		
		for(int x = 0; x < (tickX + 1); x++)
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
		
		// Y Ticks
		
		int ySteps = logProcessor.getYSteps();
		int tickY = logProcessor.getYSteps();
		
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
		
		// System.out.println("TickY " + tickY);
		
		double yIncr = (double) heatMapPlotHeight / (double) tickY;
		
		double yValMin = logProcessor.getYValMin();
		double yValMax = logProcessor.getYValMax();
		// System.out.println("yValMin " + yValMin);
		// System.out.println("yValMax " + yValMax);
		
		double yTickStepInc = ySteps / tickY;
		// System.out.println("yTickStepInc " + yTickStepInc);
		
		double yStepValInc = (yValMax - yValMin) / (ySteps - 1);
		// System.out.println("yStepValInc " + yStepValInc);
		
		double yValStep = yTickStepInc * yStepValInc;
		// System.out.println("yValStep " + yValStep);
		
		double yDval = yValMin;
		
		for(int y = 0; y < (tickY + 1); y++)
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
		
		to.stopTimer();
		
		log.info("PlotArea Ticks And Labels : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
	}
	
	private BufferedImage createScaledHeapMapPlotImage(BufferedImage rawValuesImage, int width, int height)
	{
		to.startTimer();
		
		// Resize
		Image tmp = rawValuesImage.getScaledInstance(width, height, Image.SCALE_FAST);
		
		BufferedImage heatMapPlot = new BufferedImage(linearRGB, linearRGB.createCompatibleWritableRaster(width, height), false, null);
		// BufferedImage heatMapPlot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = heatMapPlot.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		
		to.stopTimer();
		
		log.info("Created Scaled Image : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
		
		return heatMapPlot;
	}
	
	private BufferedImage createRawValuesImage(ItemLogProcessor logProcessor)
	{
		to.startTimer();
		
		double[] values = logProcessor.getDataMetricArray(Type.AVERAGE);
		
		int xSteps = logProcessor.getXSteps();
		int ySteps = logProcessor.getYSteps();
		double range = logProcessor.getZValRange();
		double rangeMin = logProcessor.getZValMin();
		
		// Note swap x/y depending on transpose
		BufferedImage rawValues = new BufferedImage(linearRGB, linearRGB.createCompatibleWritableRaster(xSteps, ySteps), false, null);
		
		// BufferedImage rawValues = new BufferedImage(xSteps, ySteps, BufferedImage.TYPE_INT_RGB);
		
		int[] pixelData = ((DataBufferInt) rawValues.getRaster().getDataBuffer()).getData();
		
		// Untransposed pixel data
		int[] pixelDataUT = new int[pixelData.length];
		
		// Untransposed pixel data
		// Each pixel parallel processed
		IntStream.range(0, ySteps).forEach(y -> IntStream.range(0, xSteps).forEach(x ->
		{
			int offset = x * ySteps + y;
			pixelDataUT[offset] = dataValueToIntRGBA(values[offset], rangeMin, range, palette);
		}));
		
		// copy data and transpose to real array
		IntStream.range(0, ySteps).forEach(y -> IntStream.range(0, xSteps).forEach(x ->
		{
			// Rotate Right 90 + Flip 180
			// int rowOffset = x * ySteps + y;
			// int colOffset = y * xSteps + x;
			
			// Rotate Left 90
			int rowOffset = (x * ySteps - y) + (ySteps - 1);
			int colOffset = y * xSteps + x;
			
			// Rotate Right 90
			// int rowOffset = x * ySteps + y;
			// int colOffset = (y * xSteps - x) + (xSteps-1);
			pixelData[colOffset] = pixelDataUT[rowOffset];
		}));
		
		to.stopTimer();
		
		log.info("Created Unscaled Image : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
		
		return rawValues;
	}
	
	private int dataValueToIntRGBA(double value, double dataRangeMin, double dataRange, int[] palette)
	{
		double per = (value - dataRangeMin) / dataRange;
		
		return palette[(int) ((palette.length - 1) * per)];
	}
	
	private void paintValues(Graphics g, int x, int y)
	{
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
	
	public long getTimeTaken()
	{
		return totalTime;
	}
}
