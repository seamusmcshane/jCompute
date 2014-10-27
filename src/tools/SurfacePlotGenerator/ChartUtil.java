package tools.SurfacePlotGenerator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

public class ChartUtil
{	
	/**
	 * @param file - item log
	 * @param mode - avg /std-dev
	 */
	public static void ExportSurfacePlot(String sourceFile, int mode, String exportPath, String fileName)
	{

		BatchLogProcessorMapper stdDevMapper = new BatchLogProcessorMapper(sourceFile, mode);

		Range stdDevXRange = new Range(stdDevMapper.getXmin(), stdDevMapper.getXmax());
		Range stdDevYRange = new Range(stdDevMapper.getYmin(), stdDevMapper.getYmax());
		
		Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(stdDevXRange, stdDevMapper.getXSteps(), stdDevYRange, stdDevMapper.getYSteps()), stdDevMapper);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, 0.95f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
		
		Chart chart = AWTChartComponentFactory.chart(Quality.Intermediate, "awt");
		chart.getAxeLayout().setXAxeLabel(stdDevMapper.getXAxisName());
		chart.getAxeLayout().setYAxeLabel(stdDevMapper.getYAxisName());
		chart.getAxeLayout().setZAxeLabel(stdDevMapper.getZAxisName());
		chart.getScene().getGraph().add(surface);
		
		AWTColorbarLegend stdDevColorBar = new AWTColorbarLegend(surface, chart.getView().getAxe().getLayout());
		surface.setLegend(stdDevColorBar);
		
		// Tick mapping
		chart.getAxeLayout().setXTickRenderer(stdDevMapper.getXTickMapper());
		chart.getAxeLayout().setYTickRenderer(stdDevMapper.getYTickMapper());
		
		chart.getView().setViewPositionMode(ViewPositionMode.TOP);
		
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(800,800));
		frame.getContentPane().add((Component) chart.getCanvas());
		
		frame.pack();
		frame.setVisible(true);
		

		try
		{
			// Hash the input vars so this method is some what reentrant.
			String tfileName = String.valueOf(sourceFile.hashCode()+mode+exportPath.hashCode()+fileName.hashCode())+".png";
			
			File temp = new File(System.getProperty("java.io.tmpdir") + tfileName);
			chart.screenshot(temp);
			
			BufferedImage image = ImageIO.read(temp);
			temp.delete();
			
			AffineTransform stx = AffineTransform.getScaleInstance(1, -1);
			stx.translate(0, -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(stx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			
			image = op.filter(image, null);
			
			File outputfile = new File(exportPath + File.separator + fileName + ".png");
			
			ImageIO.write(image, "png", outputfile);
			
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		frame.setVisible(false);
		
		surface.dispose();
		
		frame.dispose();
		
	}

}
