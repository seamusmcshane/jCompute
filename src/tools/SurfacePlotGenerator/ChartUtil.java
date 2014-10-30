package tools.SurfacePlotGenerator;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
	 * @param file
	 *            - item log
	 * @param mode
	 *            - avg /std-dev
	 */
	public static void ExportSurfacePlot(final int width, final int height, String sourceFile, int mode, String exportPath,
			String fileName)
	{
		int legendWidth = 140;

		final BatchLogProcessorMapper mapper = new BatchLogProcessorMapper(sourceFile, mode);

		Range stdDevXRange = new Range(mapper.getXmin(), mapper.getXmax());
		Range stdDevYRange = new Range(mapper.getYmin(), mapper.getYmax());

		Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(stdDevXRange, mapper.getXSteps(),
				stdDevYRange, mapper.getYSteps()), mapper);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface
				.getBounds().getZmax(), new Color(1, 1, 1, 0.95f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(true);
		surface.setWireframeColor(Color.BLACK);

		Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "awt");
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
		frame.setMinimumSize(new Dimension(width-legendWidth, height));
		frame.getContentPane().add((Component) chart.getCanvas());

		frame.pack();
		frame.setVisible(true);

		try
		{
			// Hash the input vars so this method is some what reentrant.
			String tfileName = String.valueOf(sourceFile.hashCode() + mode + exportPath.hashCode()
					+ fileName.hashCode())
					+ ".png";

			File temp = new File(System.getProperty("java.io.tmpdir") + tfileName);
			chart.screenshot(temp);

			BufferedImage image = ImageIO.read(temp);
			temp.delete();

			AffineTransform stx = AffineTransform.getScaleInstance(1, -1);
			stx.translate(0, -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(stx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

			image = op.filter(image, null);

			int titlePad = 20;
			BufferedImage exportImage = new BufferedImage(image.getWidth()+legendWidth, image.getHeight()+titlePad, BufferedImage.TYPE_INT_ARGB);
			
			
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
			
			g2d.drawString(title , (int) (width/2-titleWidth/2.), 20);
			
			// Legend box
			g2d.setStroke(new BasicStroke(1.0f));
			g2d.drawRect(10, height/2-50+coffset, legendWidth, 100);		
			
			// Legend Data
			g2d.drawString("X : " + mapper.getXAxisName() , 15, height/2-25+coffset);
			g2d.drawString("Y : " + mapper.getYAxisName() , 15, height/2+coffset);
			
			String zAxisLabel;
			
			if(mode == 0)
			{
				zAxisLabel = "Z : " + mapper.getZAxisName() + " (avg)";
			}
			else
			{
				zAxisLabel = "Z : " + mapper.getZAxisName() + " (StdDev)";
			}
			
			g2d.drawString(zAxisLabel , 15, height/2+25+coffset);
			
			// File Location
			File outputfile = new File(exportPath + File.separator + fileName + ".png");
			
			// Write File
			ImageIO.write(exportImage, "png", outputfile);

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
