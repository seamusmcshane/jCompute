package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.TextBatchLogProcessorMapper;
import jCompute.Batch.LogFileProcessor.XMLBatchLogProcessorMapper;
import jCompute.util.FileUtil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.colors.colormaps.ColorMapRBG;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class SurfacePlotterUtil implements ActionListener, WindowListener
{
	private static JFrame gui;
	private static JMenuBar menuBar;
	private static JMenu mnFile;
	private static JMenuItem mntmOpen;
	private static JMenuItem mntmExit;
	
	private static Chart chartAvg;
	
	private static Chart chartStdDev;
	private static boolean wireframeEnabled = true;
	
	private JMenuItem mntmExportImage;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	private JButton btnTop;
	private JButton btnLeft;
	private JButton btnISO;
	private JButton btnRight;
	
	private float rotateTick = (float) (Math.PI * (45f / 2f));
	private Coord3d defaultPos = new Coord3d(0.75f, 0.75f, 0);
	private JPanel chartContainerPanel;
	
	private static String saveCD = "./scenarios";
	private static String openCD = "./stats";
	
	private BatchLogInf mapper;
	private JButton btnLines;
	
	private int subDiv = 1;
	
	public SurfacePlotterUtil()
	{
		lookandFeel();
		
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		float ratio = 1f;
		float multi = 1f;
		
		float scaledMulti = ((0 + multi) * ratio);
		
		float width = 800 * scaledMulti;
		float height = 300 * scaledMulti;
		
		gui.setMinimumSize(new Dimension((int) width, (int) height));
		
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
		
		panel = new JPanel();
		gui.getContentPane().add(panel, BorderLayout.EAST);
		panel.setLayout(new BorderLayout(0, 0));
		
		panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		btnTop = new JButton("Top");
		btnTop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Coord3d coord = new Coord3d(0, 2f, 0);
				
				chartAvg.getView().setViewPoint(coord);
				chartAvg.getAxeLayout().setZAxeLabelDisplayed(false);
				chartAvg.getAxeLayout().setZTickLabelDisplayed(false);
				
				chartStdDev.getView().setViewPoint(coord);
				chartStdDev.getAxeLayout().setZAxeLabelDisplayed(false);
				chartStdDev.getAxeLayout().setZTickLabelDisplayed(false);
				
				chartAvg.getView().setViewPositionMode(ViewPositionMode.TOP);
				chartStdDev.getView().setViewPositionMode(ViewPositionMode.TOP);
				
			}
		});
		panel_2.add(btnTop, BorderLayout.NORTH);
		
		btnLeft = new JButton("Left");
		btnLeft.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Coord3d current = chartAvg.getView().getViewPoint();
				
				current.x = current.x - rotateTick;
				
				chartAvg.getView().setViewPoint(current);
				
				chartStdDev.getView().setViewPoint(current);
			}
		});
		panel_2.add(btnLeft, BorderLayout.WEST);
		
		btnISO = new JButton("Iso");
		btnISO.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Coord3d coord = new Coord3d(defaultPos.x, defaultPos.y, defaultPos.z);
				
				chartAvg.getView().setViewPoint(coord);
				chartAvg.getAxeLayout().setZAxeLabelDisplayed(true);
				chartAvg.getAxeLayout().setZTickLabelDisplayed(true);
				
				chartStdDev.getView().setViewPoint(coord);
				chartStdDev.getAxeLayout().setZAxeLabelDisplayed(true);
				chartStdDev.getAxeLayout().setZTickLabelDisplayed(true);
				
				chartAvg.getView().setViewPositionMode(ViewPositionMode.FREE);
				chartStdDev.getView().setViewPositionMode(ViewPositionMode.FREE);
				
			}
		});
		panel_2.add(btnISO, BorderLayout.CENTER);
		
		btnRight = new JButton("Right");
		btnRight.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Coord3d current = chartAvg.getView().getViewPoint();
				
				current.x = current.x + rotateTick;
				
				chartAvg.getView().setViewPoint(current);
				chartStdDev.getView().setViewPoint(current);
				
			}
		});
		panel_2.add(btnRight, BorderLayout.EAST);
		
		btnLines = new JButton("Lines");
		btnLines.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(wireframeEnabled == true)
				{
					wireframeEnabled = false;
				}
				else
				{
					wireframeEnabled = true;
				}
				
				List<AbstractDrawable> drawables = chartAvg.getScene().getGraph().getAll();
				
				for(AbstractDrawable drawable : drawables)
				{
					if(drawable.getClass().getName().equals("org.jzy3d.plot3d.primitives.Shape"))
					{
						Shape shape = (Shape) drawable;
						
						shape.setWireframeDisplayed(wireframeEnabled);
					}
					else
					{
						System.out.println(drawable.getClass().getName());
					}
				}
				
				drawables = chartStdDev.getScene().getGraph().getAll();
				
				for(AbstractDrawable drawable : drawables)
				{
					if(drawable.getClass().getName().equals("org.jzy3d.plot3d.primitives.Shape"))
					{
						Shape shape = (Shape) drawable;
						
						shape.setWireframeDisplayed(wireframeEnabled);
					}
				}
				
			}
		});
		panel_2.add(btnLines, BorderLayout.SOUTH);
		
		chartContainerPanel = new JPanel();
		gui.getContentPane().add(chartContainerPanel, BorderLayout.CENTER);
		GridBagLayout gbl_chartContainerPanel = new GridBagLayout();
		gbl_chartContainerPanel.columnWidths = new int[]
		{
			0, 0
		};
		gbl_chartContainerPanel.rowHeights = new int[]
		{
			0
		};
		gbl_chartContainerPanel.columnWeights = new double[]
		{
			1.0, 1.0
		};
		gbl_chartContainerPanel.rowWeights = new double[]
		{
			1.0
		};
		chartContainerPanel.setLayout(gbl_chartContainerPanel);
		
		gui.addWindowListener(this);
		gui.setVisible(true);
	}
	
	public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				new SurfacePlotterUtil();
			}
		});
		
	}
	
	public void addAvgChart()
	{
		Range avgXRange = new Range(mapper.getXMin(), mapper.getXMax());
		Range avgYRange = new Range(mapper.getYMin(), mapper.getYMax());
		
		Shape surfaceAvg = Builder.buildOrthonormal(new OrthonormalGrid(avgXRange, mapper.getXSteps() * subDiv,
				avgYRange, mapper.getYSteps() * subDiv), mapper.getAvg());
		surfaceAvg.setColorMapper(new ColorMapper(new ColorMapRainbow(), mapper.getZmin(), mapper.getZmax(), new Color(
				1, 1, 1, 1f)));
		surfaceAvg.setFaceDisplayed(true);
		surfaceAvg.setWireframeDisplayed(wireframeEnabled);
		surfaceAvg.setWireframeColor(Color.BLACK);
		
		chartAvg = AWTChartComponentFactory.chart(Quality.Intermediate, "awt");
		chartAvg.getAxeLayout().setXAxeLabel(mapper.getXAxisName());
		chartAvg.getAxeLayout().setYAxeLabel(mapper.getYAxisName());
		chartAvg.getAxeLayout().setZAxeLabel(mapper.getZAxisName());
		chartAvg.getScene().getGraph().add(surfaceAvg);
		
		AWTColorbarLegend avgColorBar = new AWTColorbarLegend(surfaceAvg, chartAvg.getView().getAxe().getLayout());
		surfaceAvg.setLegend(avgColorBar);
		
		// Tick mapping
		chartAvg.getAxeLayout().setXTickRenderer(mapper.getXTickMapper());
		chartAvg.getAxeLayout().setYTickRenderer(mapper.getYTickMapper());
		
		chartAvg.addMouseController();
		
		GridBagConstraints gbcAvg = new GridBagConstraints();
		gbcAvg.fill = GridBagConstraints.BOTH;
		gbcAvg.insets = new Insets(0, 0, 5, 0);
		gbcAvg.gridx = 0;
		gbcAvg.gridy = 0;
		chartContainerPanel.add((Component) chartAvg.getCanvas(), gbcAvg);
		
	}
	
	public void removeCharts()
	{
		if(chartStdDev != null)
		{
			chartContainerPanel.remove((Component) chartStdDev.getCanvas());
			chartStdDev.pauseAnimator();
			
			List<AbstractDrawable> drawables = chartStdDev.getScene().getGraph().getAll();
			
			List<Shape> shapes = new ArrayList<Shape>();
			
			for(AbstractDrawable drawable : drawables)
			{
				if(drawable.getClass().getName().equals("org.jzy3d.plot3d.primitives.Shape"))
				{
					Shape shape = (Shape) drawable;
					
					shapes.add(shape);
				}
				else
				{
					System.out.println(drawable.getClass().getName());
				}
			}
			
			for(AbstractDrawable shape : shapes)
			{
				chartStdDev.getScene().getGraph().remove(shape);
				
				shape.dispose();
			}
			
			chartStdDev.dispose();
			chartStdDev = null;
		}
		
		if(chartAvg != null)
		{
			chartContainerPanel.remove((Component) chartAvg.getCanvas());
			chartAvg.pauseAnimator();
			
			List<AbstractDrawable> drawables = chartAvg.getScene().getGraph().getAll();
			
			List<Shape> shapes = new ArrayList<Shape>();
			
			for(AbstractDrawable drawable : drawables)
			{
				if(drawable.getClass().getName().equals("org.jzy3d.plot3d.primitives.Shape"))
				{
					Shape shape = (Shape) drawable;
					
					shapes.add(shape);
				}
				else
				{
					System.out.println(drawable.getClass().getName());
				}
			}
			
			for(AbstractDrawable shape : shapes)
			{
				chartAvg.getScene().getGraph().remove(shape);
				
				shape.dispose();
			}
			
			chartAvg.dispose();
			chartAvg = null;
		}
	}
	
	public void addStdDevChart()
	{
		Range stdDevXRange = new Range(mapper.getXMin(), mapper.getXMax());
		Range stdDevYRange = new Range(mapper.getYMin(), mapper.getYMax());
		
		Shape surfaceStdDev = Builder.buildOrthonormal(new OrthonormalGrid(stdDevXRange, mapper.getXSteps() * subDiv,
				stdDevYRange, mapper.getYSteps() * subDiv), mapper.getStdDev());
		surfaceStdDev.setColorMapper(new ColorMapper(new ColorMapRBG(), surfaceStdDev.getBounds().getZmin(),
				surfaceStdDev.getBounds().getZmax(), new Color(1, 1, 1, 1f)));
		surfaceStdDev.setFaceDisplayed(true);
		surfaceStdDev.setWireframeDisplayed(wireframeEnabled);
		surfaceStdDev.setWireframeColor(Color.BLACK);
		
		chartStdDev = AWTChartComponentFactory.chart(Quality.Intermediate, "awt");
		chartStdDev.getAxeLayout().setXAxeLabel(mapper.getXAxisName());
		chartStdDev.getAxeLayout().setYAxeLabel(mapper.getYAxisName());
		chartStdDev.getAxeLayout().setZAxeLabel(mapper.getZAxisName());
		chartStdDev.getScene().getGraph().add(surfaceStdDev);
		
		AWTColorbarLegend stdDevColorBar = new AWTColorbarLegend(surfaceStdDev, chartStdDev.getView().getAxe()
				.getLayout());
		surfaceStdDev.setLegend(stdDevColorBar);
		
		chartStdDev.getAxeLayout().setXTickRenderer(mapper.getXTickMapper());
		chartStdDev.getAxeLayout().setYTickRenderer(mapper.getYTickMapper());
		
		chartStdDev.addMouseController();
		
		GridBagConstraints gbcStdev = new GridBagConstraints();
		gbcStdev.insets = new Insets(0, 0, 5, 0);
		gbcStdev.fill = GridBagConstraints.BOTH;
		gbcStdev.gridx = 1;
		gbcStdev.gridy = 0;
		chartContainerPanel.add((Component) chartStdDev.getCanvas(), gbcStdev);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == mntmOpen)
		{
			final JFileChooser filechooser = new JFileChooser(new File(openCD));
			
			System.out.println("Open Dialog");
			
			int val = filechooser.showOpenDialog(filechooser);
			
			if(val == JFileChooser.APPROVE_OPTION)
			{
				removeCharts();
				
				System.out.println("New File Choosen");
				
				String file = filechooser.getSelectedFile().getAbsolutePath();
				
				openCD = filechooser.getCurrentDirectory().getAbsolutePath();
				
				gui.setTitle(filechooser.getSelectedFile().getName());
				
				System.out.println(file);
				
				System.out.println("Creating Mapper");
				
				String ext = FileUtil.getFileNameExtension(file);
				System.out.println(ext);
				
				switch(ext)
				{
					case "xml":
						
						mapper = new XMLBatchLogProcessorMapper(file);
					
					break;
					
					case "log":
						
						mapper = new TextBatchLogProcessorMapper(file);
					
					break;
					default:
						System.out.println("Unsupported LogType " + ext);
					break;
				}
				
				System.out.println("Average Chart");
				addAvgChart();
				
				System.out.println("Standard Deviation Chart");
				addStdDevChart();
				
				// chart = new Chart(factory, Quality.Fastest);
				// MapperContourPictureGenerator avgContour = new
				// MapperContourPictureGenerator(avgMapper, avgXRange,
				// avgYRange);
				// ContourAxeBox avgCab = new
				// ContourAxeBox(chartAvg.getView().getAxe().getBoxBounds());
				// avgCab.setContourImg(avgContour.getHeightMap(new
				// DefaultContourColoringPolicy(new ColorMapper(new
				// ColorMapRainbow(), surfaceStdDev.getBounds().getZmin(),
				// surfaceStdDev.getBounds().getZmax(), new Color(1, 1, 1,
				// 0.95f))), (int)avgXRange.getMax(), (int)avgYRange.getMax(),
				// (int)avgMapper.getValueMax()), avgXRange, avgYRange);
				// chartAvg.getView().setAxe(avgCab);
				
				/*
				 * GLSLProgram.DEFAULT_STRICTNESS =
				 * Strictness.CONSOLE_NO_WARN_UNIFORM_NOT_FOUND;
				 * IChartComponentFactory factory = new
				 * PeelingComponentFactory(PeelingMethod.DUAL_PEELING_MODE);
				 * GLProfile profile = GLProfile.getMaxProgrammable(true);
				 * GLCapabilities capabilities = new GLCapabilities(profile);
				 * capabilities.setHardwareAccelerated(true);
				 */
				
				gui.validate();
				
				Coord3d coord = new Coord3d(defaultPos.x, defaultPos.y, defaultPos.z);
				
				chartAvg.getView().setViewPoint(coord);
				chartStdDev.getView().setViewPoint(coord);
				
			}
		}
		if(e.getSource() == mntmExit)
		{
			doProgramExit();
		}
		
		if(e.getSource() == mntmExportImage)
		{
			
			final JFileChooser filechooser = new JFileChooser(new File(saveCD));
			
			int val = filechooser.showSaveDialog(filechooser);
			
			if(val == JFileChooser.APPROVE_OPTION)
			{
				File file = filechooser.getSelectedFile();
				
				saveCD = filechooser.getCurrentDirectory().getAbsolutePath();
				
				String fileName = file.getAbsolutePath().toString();
				
				try
				{
					// Export chart textures
					File avgTempFile = new File(System.getProperty("java.io.tmpdir") + "avg.png");
					chartAvg.screenshot(avgTempFile);
					File stdDevTempFile = new File(System.getProperty("java.io.tmpdir") + "stddev.png");
					chartStdDev.screenshot(stdDevTempFile);
					
					// Read Textures
					BufferedImage avgImg = ImageIO.read(avgTempFile);
					avgTempFile.delete();
					
					BufferedImage stddevImg = ImageIO.read(stdDevTempFile);
					stdDevTempFile.delete();
					
					// Flip vertical
					AffineTransform stx = AffineTransform.getScaleInstance(1, -1);
					stx.translate(0, -avgImg.getHeight(null));
					AffineTransformOp avgOp = new AffineTransformOp(stx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
					
					AffineTransform dtx = AffineTransform.getScaleInstance(1, -1);
					dtx.translate(0, -stddevImg.getHeight(null));
					AffineTransformOp stddevOp = new AffineTransformOp(dtx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
					
					avgImg = avgOp.filter(avgImg, null);
					stddevImg = stddevOp.filter(stddevImg, null);
					
					BufferedImage exportImage = new BufferedImage(avgImg.getWidth() + stddevImg.getWidth(),
							avgImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
					
					exportImage.getGraphics().drawImage(avgImg, 0, 0, null);
					exportImage.getGraphics().drawImage(stddevImg, avgImg.getWidth(), 0, null);
					
					if(fileName.indexOf(".") > 0)
					{
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
					}
					
					// Save the screenshot
					File outputfile = new File(fileName + ".png");
					ImageIO.write(exportImage, "png", outputfile);
					
				}
				catch(IOException e1)
				{
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
				
				if(value == JOptionPane.YES_OPTION)
				{
					// Do EXIT
					System.exit(0);
				}
			}
		});
		
	}
	
	@Override
	public void windowActivated(WindowEvent arg0)
	{
		
	}
	
	@Override
	public void windowClosed(WindowEvent arg0)
	{
		
	}
	
	@Override
	public void windowClosing(WindowEvent arg0)
	{
		doProgramExit();
	}
	
	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
		
	}
	
	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
		
	}
	
	@Override
	public void windowIconified(WindowEvent arg0)
	{
		
	}
	
	@Override
	public void windowOpened(WindowEvent arg0)
	{
		
	}
	
	/* Use the java provided system look and feel */
	private void lookandFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch(InstantiationException e1)
		{
			e1.printStackTrace();
		}
		catch(IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch(UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
	}
	
}
