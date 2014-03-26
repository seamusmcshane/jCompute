package tools.SurfacePlotGenerator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.io.glsl.GLSLProgram;
import org.jzy3d.io.glsl.GLSLProgram.Strictness;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.ddp.PeelingComponentFactory;
import org.jzy3d.plot3d.rendering.ddp.algorithms.PeelingMethod;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;

import alifeSim.Debug.DebugLogger;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class SurfacePlotterUtil implements ActionListener,WindowListener
{
	private static JFrame gui;
	private static JMenuBar menuBar;
	private static JMenu mnFile;
	private static JMenuItem mntmOpen;
	private static JMenuItem mntmExit;
	
	private static Shape surface;
	private static Chart chart;
	private JMenuItem mntmExportImage;
	
	public SurfacePlotterUtil()
	{
		lookandFeel();
		
		DebugLogger.setDebug(true);
		
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		gui.setMinimumSize(new Dimension(400,400));

		menuBar = new JMenuBar();
		gui.setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		mntmOpen.addActionListener(this);
		
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(this);
		
		mntmExportImage = new JMenuItem("Export Image");
		mntmExportImage.addActionListener(this);
		mnFile.add(mntmExportImage);
		mnFile.add(mntmExit);
		
		gui.addWindowListener(this);
		gui.setVisible(true);	
	}
	
	public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run()
			{
				SurfacePlotterUtil util = new SurfacePlotterUtil();				
			}}
		);  
		

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == mntmOpen)
		{
			final JFileChooser filechooser = new JFileChooser(new File("./stats"));

			DebugLogger.output("Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				DebugLogger.output("New File Choosen");
				
				String file = filechooser.getSelectedFile().getAbsolutePath();

				DebugLogger.output(file);
				
		        if(surface!=null)
		        {
		        	gui.remove((Component) chart.getCanvas());
		        	chart = null;
		        	surface.dispose();
		        	surface = null;
		        }
		        
		        BatchLogProcessorMapper mapper = new BatchLogProcessorMapper(file);
		        Range xRange = new Range(mapper.getXmin(),mapper.getXmax());
		        Range yRange = new Range(mapper.getYmin(),mapper.getYmax());
		        
		        surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, mapper.getXSteps(), yRange, mapper.getYSteps()), mapper);
		        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, 0.95f)));
		        surface.setFaceDisplayed(true);
		        surface.setWireframeDisplayed(true);
		        surface.setWireframeColor(Color.BLACK);

		        GLSLProgram.DEFAULT_STRICTNESS = Strictness.CONSOLE_NO_WARN_UNIFORM_NOT_FOUND;
		        IChartComponentFactory factory = new PeelingComponentFactory(PeelingMethod.DUAL_PEELING_MODE);
		        
		        GLProfile profile = GLProfile.getMaxProgrammable(true);
		        GLCapabilities capabilities = new GLCapabilities(profile);
		        capabilities.setHardwareAccelerated(true);
		        
		        //chart = new Chart(factory, Quality.Fastest);
		        
		        chart = AWTChartComponentFactory.chart(Quality.Advanced,"awt");
		        
		        chart.getAxeLayout().setXAxeLabel(mapper.getXAxisName());
		        chart.getAxeLayout().setYAxeLabel(mapper.getYAxisName());
		        chart.getAxeLayout().setZAxeLabel(mapper.getZAxisName());
		        chart.getScene().getGraph().add(surface);
		        
		        AWTColorbarLegend colorBar = new AWTColorbarLegend(surface, chart.getView().getAxe().getLayout());
		        surface.setLegend(colorBar);
		        
		        // Tick mapping
		        chart.getAxeLayout().setXTickRenderer(mapper.getXTickMapper());
		        chart.getAxeLayout().setYTickRenderer(mapper.getYTickMapper());
		        		        
		        chart.addMouseController();

		        gui.getContentPane().add((Component)chart.getCanvas());
		        gui.validate();
		        
			}
		}
		if(e.getSource() == mntmExit)
		{
			doProgramExit();
		}
		
		if(e.getSource() == mntmExportImage)
		{
			
			final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));


			int val = filechooser.showSaveDialog(filechooser);
			
			
			if (val == JFileChooser.APPROVE_OPTION)
			{
				File file = filechooser.getSelectedFile();

				String fileName = file.getAbsolutePath().toString();
				
		        try
				{
		        	// Convert Texture to file...
			        File tempFile = new File(System.getProperty("java.io.tmpdir")+"temp.png");
					chart.screenshot(tempFile);

					// Read file to BufferedImage
			        BufferedImage img = ImageIO.read(tempFile);

			        // Flip the image vertically
			        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			        tx.translate(0, -img.getHeight(null));
			        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			        img = op.filter(img, null);

					if (fileName.indexOf(".") > 0)
					{
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
					}
			        
			        // Save the screenshot
					File outputfile = new File(fileName+".png");
					ImageIO.write(img, "png", outputfile);
			        
					// Remove Temp File
					tempFile.delete();

				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
		
	}
	
	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() 
		{
		    public void run() 
		    {
				String message;
				message = "Do you want to quit?";

				JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

				// Center Dialog on the GUI
				JDialog dialog = pane.createDialog(gui, "Close Application");

				dialog.pack();
				dialog.setVisible(true);

				int value = ((Integer) pane.getValue()).intValue();

				if (value == JOptionPane.YES_OPTION)
				{
					// Do EXIT
					System.exit(0);
				}
		    }
		}
		);
		
	}

	@Override
	public void windowActivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		doProgramExit();		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
	
	/* Use the java provided system look and feel */
	private void lookandFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (InstantiationException e1)
		{
			e1.printStackTrace();
		}
		catch (IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
	}
	
}
