package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchInfoLogProcessor;
import jCompute.Batch.LogFileProcessor.BatchLogProcessor;
import jCompute.util.JCMath;
import jCompute.util.Text;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.colors.colormaps.ColorMapRainbowNoBorder;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

public class ChartUtil
{
	/**
	 * @param file
	 *            - item log
	 * @param mode
	 *            - avg /std-dev
	 */
	private static boolean zfixScale = false;
	private static boolean scaleSet = false;
	private static int maxScale = 0;
	
	public void ExportSurfacePlot(final int width, final int height, String sourceFilePath, String exportPath, String fileName)
	{
		BatchInfoLogProcessor ilp = null;
		try
		{
			if(!scaleSet)
			{
				// Get the info path
				String infoPath = sourceFilePath.substring(0, sourceFilePath.lastIndexOf(File.separator));
				
				System.out.println(infoPath);
				
				ilp = new BatchInfoLogProcessor(infoPath + File.separator + "infoLog.log");
				
				ilp.dump();
				
				maxScale = ilp.getMaxSteps();
				
				scaleSet = true;
				
				zfixScale = true;
			}
		}
		catch(IOException e1)
		{
			String st = Text.stackTrackToString(e1.getStackTrace(), false);
			String message = "Error Reading info log " + "\n" + e1.getMessage() + "\n" + st;
			System.out.println(message);
		}
		
		if(ilp != null)
		{
			BatchLogProcessor logProcessor;
			try
			{
				logProcessor = new BatchLogProcessor(sourceFilePath, ilp.getMaxSteps());
				
				exportChartImage(logProcessor, width, height, sourceFilePath, exportPath, fileName + "-avg", 0);
				exportChartImage(logProcessor, width, height, sourceFilePath, exportPath, fileName + "-standard-deviation", 1);
				exportChartImage(logProcessor, width, height, sourceFilePath, exportPath, fileName + "-max", 2);
				
				logProcessor = null;
				
			}
			catch(IOException e1)
			{
				String st = Text.stackTrackToString(e1.getStackTrace(), true);
				
				String message = "Error Reading Item log " + "\n" + e1.getMessage() + "\n" + st;
				
				System.out.println(message);
			}
		}
	}
	
	private void exportChartImage(BatchLogProcessor logProcessor, final int width, final int height, String sourceFile, String exportPath, String fileName, int mode)
	{
		int legendWidth = 160;
		
		Range xRange = new Range(logProcessor.getXMin(), logProcessor.getXMax());
		Range yRange = new Range(logProcessor.getYMin(), logProcessor.getYMax());
		
		Mapper map;
		Shape surface;
		
		double maxRate = 0;
		
		if(mode == 0)
		{
			double zMin = logProcessor.getZmin();
			double zMax = logProcessor.getZmax();
			
			if(zfixScale)
			{
				zMin = 0;
				zMax = maxScale;
			}
			
			map = logProcessor.getAvg();
			
			surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, logProcessor.getXSteps(), yRange, logProcessor.getYSteps()), map);
			
			surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), zMin, zMax, new Color(1, 1, 1, 1f)));
		}
		else if(mode == 1)
		{
			map = logProcessor.getStdDev();
			
			surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, logProcessor.getXSteps(), yRange, logProcessor.getYSteps()), map);
			
			double zMin = surface.getBounds().getZmin();
			double zMax = surface.getBounds().getZmax();
			
			if(zfixScale)
			{
				zMin = 0;
				zMax = maxScale;
			}
			
			surface.setColorMapper(new ColorMapper(new ColorMapRainbowNoBorder(), zMin, zMax, new Color(1, 1, 1, 1f)));
		}
		else// (mode == 2)
		{
			map = logProcessor.getMax();
			
			surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, logProcessor.getXSteps(), yRange, logProcessor.getYSteps()), map);
			
			double zMin = surface.getBounds().getZmin();
			double zMax = surface.getBounds().getZmax();
			
			if(zfixScale)
			{
				zMin = 0;
				zMax = maxScale;
			}
			
			surface.setColorMapper(new ColorMapper(new ColorMapRainbowNoBorder(), zMin, zMax, new Color(1, 1, 1, 1f)));
		}
		
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(false);
		surface.setWireframeColor(Color.BLACK);
		
		// depthActivated, alphaActivated, smoothColor, smoothPoint, smoothLine, smoothPolygon, disableDepth
		Quality chartQual = new Quality(false, true, false, true, true, false, false);
		
		Chart chart = AWTChartComponentFactory.chart(chartQual, "newt");
		
		// chart.getAxeLayout().setXAxeLabel(mapper.getXAxisName());
		// chart.getAxeLayout().setYAxeLabel(mapper.getYAxisName());
		// chart.getAxeLayout().setZAxeLabel(mapper.getZAxisName());
		chart.getScene().getGraph().add(surface);
		AWTColorbarLegend stdDevColorBar = new AWTColorbarLegend(surface, chart.getView().getAxe().getLayout());
		surface.setLegend(stdDevColorBar);
		
		// Tick mapping
		chart.getAxeLayout().setXTickRenderer(logProcessor.getXTickMapper());
		chart.getAxeLayout().setYTickRenderer(logProcessor.getYTickMapper());
		
		chart.getView().setViewPositionMode(ViewPositionMode.TOP);
		
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(width - legendWidth, height));
		frame.getContentPane().add((Component) chart.getCanvas());
		
		frame.pack();
		frame.setVisible(true);
		
		System.out.println("Frame");
		
		// Hash the input vars so this method is some what reentrant.
		String tfileName = String.valueOf(sourceFile.hashCode() + mode + exportPath.hashCode() + fileName.hashCode()) + ".png";
		
		File temp = new File(System.getProperty("java.io.tmpdir") + tfileName);
		
		try
		{
			chart.screenshot(temp);
			frame.remove((Component) chart.getCanvas());
			chart.dispose();
			chart = null;
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		}
		catch(IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try
		{
			
			BufferedImage image = ImageIO.read(temp);
			temp.delete();
			
			AffineTransform stx = AffineTransform.getScaleInstance(1, -1);
			stx.translate(0, -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(stx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			
			image = op.filter(image, null);
			
			int titlePad = 20;
			BufferedImage exportImage = new BufferedImage(image.getWidth() + legendWidth, image.getHeight() + titlePad, BufferedImage.TYPE_INT_ARGB);
			
			final int coffset = -20;
			Graphics2D g2d = (Graphics2D) exportImage.getGraphics();
			
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			// Blank Image white
			g2d.setColor(java.awt.Color.WHITE);
			g2d.fillRect(0, 0, exportImage.getWidth(), exportImage.getHeight());
			
			// Graph Image
			exportImage.getGraphics().drawImage(image, legendWidth, titlePad, null);
			
			// Back to black..
			g2d.setColor(java.awt.Color.BLACK);
			
			// Title
			String title = fileName;
			FontMetrics fm = g2d.getFontMetrics();
			int titleWidth = fm.stringWidth(title);
			
			g2d.drawString(title, (int) (width / 2 - titleWidth / 2.), 20);
			
			// Legend box
			g2d.setStroke(new BasicStroke(1.0f));
			g2d.drawRect(10, height / 2 - 50 + coffset, legendWidth, 120);
			
			// Legend Data
			g2d.drawString("X : " + logProcessor.getXAxisName(), 15, height / 2 - 25 + coffset);
			g2d.drawString("Y : " + logProcessor.getYAxisName(), 15, height / 2 + coffset);
			
			String zAxisLabel;
			
			if(mode == 0)
			{
				zAxisLabel = "Z : " + logProcessor.getZAxisName() + " (avg)";
			}
			else if(mode == 1)
			{
				zAxisLabel = "Z : " + logProcessor.getZAxisName() + " (StdDev)";
			}
			else
			{
				zAxisLabel = "Z : " + logProcessor.getZAxisName() + " (Max)";
			}
			
			g2d.drawString(zAxisLabel, 15, height / 2 + 25 + coffset);
			
			if(zfixScale)
			{
				maxRate = JCMath.round(logProcessor.getMaxRate(), 8);
				
				System.out.println("Max Rate : " + maxRate);
				
				g2d.drawString("Stability : " + String.valueOf(maxRate), 15, height / 2 + 50 + coffset);
			}
			
			File outputfile = new File(exportPath + File.separator + fileName + ".png");
			
			System.out.println("Writing " + outputfile.getAbsolutePath());
			
			// Write File
			ImageIO.write(exportImage, "png", outputfile);
			
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
}
