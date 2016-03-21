package tools.ReportGenerator;

import jCompute.Batch.LogFileProcessor.InfoLogProcessor;
import jCompute.Batch.LogFileProcessor.ItemLogProcessor;
import jCompute.Batch.LogFileProcessor.ItemLogProcessor.ComputedMetric;
import jCompute.util.FileUtil;
import jCompute.util.JCMath;
import jCompute.util.Text;
import sun.misc.Cleaner;
import tools.SurfacePlotGenerator.Lib.SurfaceChartHelper;

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
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

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
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

import com.jogamp.opengl.util.texture.TextureData;

public class SurfacePlotImageExporter
{
	// Image Width/Height
	private int imageWidth;
	private int imageHeight;
	
	// Row/Column Names
	private ArrayList<String> rowNames;
	private ArrayList<String> colNames;
	
	// Cells
	private Map<String, String> cells;
	
	// Working Path
	private String fullPath;
	
	// LogFile Name
	private String itemLog;
	
	// Auto detected and Set
	private boolean zfixScale = false;
	private boolean scaleSet = false;
	private int maxScale = 0;
	
	public SurfacePlotImageExporter(int imageWidth, int imageHeight, ArrayList<String> rowNames, ArrayList<String> colNames, Map<String, String> cells, String fullPath, String itemLog)
	{
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		
		this.rowNames = rowNames;
		this.colNames = colNames;
		this.cells = cells;
		
		this.fullPath = fullPath;
		this.itemLog = itemLog;
	}
	
	/**
	 * @param rowNames
	 * @param colNames
	 * @param cells
	 * @param fullPath
	 * @param itemLog
	 */
	public void export()
	{
		for(String row : rowNames)
		{
			for(String col : colNames)
			{
				String path = fullPath + File.separator + row + File.separator + col;
				System.out.println("Path : " + path);
				
				String imagesPath = fullPath + File.separator + "images";
				String exportPath = imagesPath + File.separator + row;
				
				String logDir = cells.get(row + col);
				String imageName = logDir.substring(logDir.lastIndexOf(']') + 2, logDir.length());
				String logPath = logDir + File.separator + itemLog;
				System.out.println("logDir : " + logDir);
				
				System.out.println("imagesPath : " + imagesPath);
				System.out.println("exportPath : " + exportPath);
				System.out.println("logPath : " + logPath);
				System.out.println("imageName : " + imageName);
				
				FileUtil.createDirIfNotExist(imagesPath);
				FileUtil.createDirIfNotExist(exportPath);
				
				exportChartImages(imageWidth, imageHeight, logPath, exportPath, imageName);
			}
		}
	}
	
	/**
	 * @param width
	 * @param height
	 * @param sourceFilePath
	 * @param exportPath
	 * @param fileName
	 */
	private void exportChartImages(final int width, final int height, String sourceFilePath, String exportPath, String fileName)
	{
		InfoLogProcessor ilp = null;
		try
		{
			if(!scaleSet)
			{
				// Get the info path
				String infoPath = sourceFilePath.substring(0, sourceFilePath.lastIndexOf(File.separator));
				
				System.out.println(infoPath);
				
				ilp = new InfoLogProcessor(infoPath + File.separator + "infoLog.log");
				
				ilp.dump();
				
				maxScale = ilp.getMaxSteps();
				
				scaleSet = true;
				
				zfixScale = true;
			}
		}
		catch(IOException e1)
		{
			System.out.println("Info log not found skipping... this disables zfixScale");
		}
		
		try
		{
			// If there is an info log - use the range limits 0 to max steps possible, else range limits will be that of the data.
			ItemLogProcessor logProcessor = (ilp != null) ? new ItemLogProcessor(sourceFilePath, 0, ilp.getMaxSteps()) : new ItemLogProcessor(sourceFilePath);
			
			exportImage(logProcessor, width, height, sourceFilePath, exportPath, fileName + "-avg", 0);
			exportImage(logProcessor, width, height, sourceFilePath, exportPath, fileName + "-standard-deviation", 1);
			exportImage(logProcessor, width, height, sourceFilePath, exportPath, fileName + "-max", 2);
			
			logProcessor = null;
		}
		catch(IOException e1)
		{
			String st = Text.stackTraceToString(e1.getStackTrace(), true);
			
			String message = "Error Reading Item log " + "\n" + e1.getMessage() + "\n" + st;
			
			System.out.println(message);
		}
	}
	
	private void exportImage(ItemLogProcessor logProcessor, final int width, final int height, String sourceFile, String exportPath, String fileName, int mode)
	{
		// TODO HardCoded
		int legendWidth = 160;
		
		Range xRange = new Range(logProcessor.getXMin(), logProcessor.getXMax());
		Range yRange = new Range(logProcessor.getYMin(), logProcessor.getYMax());
		
		Mapper map;
		Shape surface;
		
		double maxRate = 0;
		
		if(mode == 0)
		{
			// Average Plot
			
			double zMin = logProcessor.getZmin();
			double zMax = logProcessor.getZmax();
			
			if(zfixScale)
			{
				zMin = 0;
				zMax = maxScale;
			}
			
			map = SurfaceChartHelper.getAvg(logProcessor);
			
			surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, logProcessor.getXSteps(), yRange, logProcessor.getYSteps()), map);
			
			surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), zMin, zMax, new Color(1, 1, 1, 1f)));
		}
		else if(mode == 1)
		{
			// Standard Dev Plot
			
			map = SurfaceChartHelper.getStdDev(logProcessor);
			
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
			// Max Surface Plot
			
			map = SurfaceChartHelper.getMax(logProcessor);
			
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
		ITickRenderer xTicks = SurfaceChartHelper.getTickMapper(logProcessor.getXMax(), logProcessor.getXValMax());
		ITickRenderer yTicks = SurfaceChartHelper.getTickMapper(logProcessor.getYMax(), logProcessor.getYValMax());
		
		chart.getAxeLayout().setXTickRenderer(xTicks);
		chart.getAxeLayout().setYTickRenderer(yTicks);
		
		chart.getView().setViewPositionMode(ViewPositionMode.TOP);
		
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(width - legendWidth, height));
		frame.getContentPane().add((Component) chart.getCanvas());
		
		frame.pack();
		frame.setVisible(true);
		
		System.out.println("Frame");
		
		// Get the image on screen as a texture
		TextureData data = chart.screenshot();
		
		int tWidth = data.getWidth();
		int tHeight = data.getHeight();
		
		// Get the underlying bytebuffer
		ByteBuffer buffer = (ByteBuffer) data.getBuffer();
		
		// Exit the gui
		frame.remove((Component) chart.getCanvas());
		chart.dispose();
		chart = null;
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		
		try
		{
			BufferedImage image = new BufferedImage(tWidth, tHeight, BufferedImage.TYPE_INT_RGB);
			
			int[] pixelData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			
			int size = bytes.length;
			
			int p = 0;
			for(int b = 0; b < size; b += 3)
			{
				// Bytes are unsigned
				int red = bytes[b] & 0xff;
				int green = bytes[b + 1] & 0xff;
				int blue = bytes[b + 2] & 0xff;
				
				pixelData[p] = (0xff << 24) | (red << 16) | (green << 8) | blue;
				
				p++;
			}
			
			// We want to Free the buffer memory now
			Field cleanerField = buffer.getClass().getDeclaredField("cleaner");
			cleanerField.setAccessible(true);
			Cleaner cleaner = (Cleaner) cleanerField.get(buffer);
			cleaner.clean();
			
			buffer = null;
			
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
				maxRate = JCMath.round(logProcessor.getComputedMetric(ComputedMetric.MAX_RATE), 8);
				
				System.out.println("Max Rate : " + maxRate);
				
				g2d.drawString("Stability : " + String.valueOf(maxRate), 15, height / 2 + 50 + coffset);
			}
			
			File outputfile = new File(exportPath + File.separator + fileName + ".png");
			
			System.out.println("Writing " + outputfile.getAbsolutePath());
			
			// Write File
			ImageIO.write(exportImage, "png", outputfile);
			
		}
		catch(IOException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
}
