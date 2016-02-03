package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchInfoLogProcessor;
import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.TextBatchLogProcessorMapper;
import jCompute.Batch.LogFileProcessor.XMLBatchLogProcessorMapper;
import jCompute.util.FileUtil;
import jCompute.util.JCMath;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
	
	public void ExportSurfacePlot(final int width, final int height, String sourceFile, String exportPath, String fileName)
	{
		String ext = FileUtil.getFileNameExtension(sourceFile);
		System.out.println(ext);
		
		BatchLogInf mapper = null;
		
		switch(ext)
		{
			case "xml":
				
				mapper = new XMLBatchLogProcessorMapper(sourceFile);
				
			break;
			
			case "log":
				
				if(!scaleSet)
				{
					String infoPath = sourceFile.substring(0, sourceFile.lastIndexOf(File.separator));
					
					System.out.println(infoPath);
					
					BatchInfoLogProcessor ilp = new BatchInfoLogProcessor(infoPath + File.separator + "infoLog.log");
					ilp.dump();
					
					maxScale = ilp.getMaxSteps();
					
					scaleSet = true;
				}
				
				zfixScale = true;
				
				mapper = new TextBatchLogProcessorMapper(sourceFile, maxScale);
				
			break;
			default:
				System.out.println("Unsupported LogType " + ext);
			break;
		}
		
		exportChartImage(mapper, width, height, sourceFile, exportPath, fileName + "-avg", 0);
		exportChartImage(mapper, width, height, sourceFile, exportPath, fileName + "-standard-deviation", 1);
		exportChartImage(mapper, width, height, sourceFile, exportPath, fileName + "-max", 2);
		
		mapper.clear();
		
		mapper = null;
	}
	
	private void exportChartImage(BatchLogInf mapper, final int width, final int height, String sourceFile, String exportPath, String fileName, int mode)
	{
		int legendWidth = 160;
		
		Range xRange = new Range(mapper.getXMin(), mapper.getXMax());
		Range yRange = new Range(mapper.getYMin(), mapper.getYMax());
		
		Mapper map;
		Shape surface;
		
		double maxRate = 0;
		
		if(mode == 0)
		{
			double zMin = mapper.getZmin();
			double zMax = mapper.getZmax();
			
			if(zfixScale)
			{
				zMin = 0;
				zMax = maxScale;
			}
			
			map = mapper.getAvg();
			
			surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, mapper.getXSteps(), yRange, mapper.getYSteps()), map);
			
			surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), zMin, zMax, new Color(1, 1, 1, 1f)));
		}
		else if(mode == 1)
		{
			map = mapper.getStdDev();
			
			surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, mapper.getXSteps(), yRange, mapper.getYSteps()), map);
			
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
			map = mapper.getMax();
			
			surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, mapper.getXSteps(), yRange, mapper.getYSteps()), map);
			
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
		
		Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
		
		// chart.getAxeLayout().setXAxeLabel(mapper.getXAxisName());
		// chart.getAxeLayout().setYAxeLabel(mapper.getYAxisName());
		// chart.getAxeLayout().setZAxeLabel(mapper.getZAxisName());
		chart.getScene().getGraph().add(surface);
		AWTColorbarLegend stdDevColorBar = new AWTColorbarLegend(surface, chart.getView().getAxe().getLayout());
		surface.setLegend(stdDevColorBar);
		
		// Tick mapping
		chart.getAxeLayout().setXTickRenderer(mapper.getXTickMapper());
		chart.getAxeLayout().setYTickRenderer(mapper.getYTickMapper());
		
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
			g2d.drawString("X : " + mapper.getXAxisName(), 15, height / 2 - 25 + coffset);
			g2d.drawString("Y : " + mapper.getYAxisName(), 15, height / 2 + coffset);
			
			String zAxisLabel;
			
			if(mode == 0)
			{
				zAxisLabel = "Z : " + mapper.getZAxisName() + " (avg)";
			}
			else if(mode == 1)
			{
				zAxisLabel = "Z : " + mapper.getZAxisName() + " (StdDev)";
			}
			else
			{
				zAxisLabel = "Z : " + mapper.getZAxisName() + " (Max)";
			}
			
			g2d.drawString(zAxisLabel, 15, height / 2 + 25 + coffset);
			
			if(zfixScale)
			{
				maxRate = JCMath.round(mapper.getMaxRate(), 8);
				
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
