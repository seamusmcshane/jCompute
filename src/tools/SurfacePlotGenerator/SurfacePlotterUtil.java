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
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart.factories.IChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.contour.DefaultContourColoringPolicy;
import org.jzy3d.contour.MapperContourPictureGenerator;
import org.jzy3d.io.glsl.GLSLProgram;
import org.jzy3d.io.glsl.GLSLProgram.Strictness;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.maths.Scale;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.axes.ContourAxeBox;
import org.jzy3d.plot3d.primitives.axes.IAxe;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.ddp.PeelingComponentFactory;
import org.jzy3d.plot3d.rendering.ddp.algorithms.PeelingMethod;
import org.jzy3d.plot3d.rendering.legends.colorbars.AWTColorbarLegend;

import alifeSim.Debug.DebugLogger;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JComboBox;

import java.awt.BorderLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SurfacePlotterUtil implements ActionListener, WindowListener
{
	private static JFrame gui;
	private static JMenuBar menuBar;
	private static JMenu mnFile;
	private static JMenuItem mntmOpen;
	private static JMenuItem mntmExit;

	private static Shape surfaceAvg;
	private static Shape surfaceStdDev;
	private static Chart chartAvg;
	private static Chart chartStdDev;
	
	private JMenuItem mntmExportImage;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_2;
	private JButton btnTop;
	private JButton btnLeft;
	private JButton btnISO;
	private JButton btnRight;

	private float rotateTick = (float) (Math.PI*(45f/2f));
	private Coord3d defaultPos =  new Coord3d(0.75f,0.75f,0);				
	private JPanel chartContainerPanel;
	
	private static String saveCD =  "./scenarios";
	private static String openCD =  "./stats";
	
	public SurfacePlotterUtil()
	{
		lookandFeel();

		DebugLogger.setDebug(true);

		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		float ratio = 800/300;
		float multi = 0.5f;
		
		float scaledMulti = ((0+multi)*ratio);
		
		float width = 800*scaledMulti;
		float height = 300*scaledMulti;
		
		gui.setMinimumSize(new Dimension((int)width, (int)height));

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
		btnTop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				Coord3d coord = new Coord3d(0,2f,0);

				chartAvg.getView().setViewPoint(coord);								
				chartAvg.getAxeLayout().setZAxeLabelDisplayed(false);
				chartAvg.getAxeLayout().setZTickLabelDisplayed(false);

				chartStdDev.getView().setViewPoint(coord);	
				chartStdDev.getAxeLayout().setZAxeLabelDisplayed(false);
				chartStdDev.getAxeLayout().setZTickLabelDisplayed(false);

				
			}
		});
		panel_2.add(btnTop, BorderLayout.NORTH);

		btnLeft = new JButton("Left");
		btnLeft.addActionListener(new ActionListener() {
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
				Coord3d coord = new Coord3d(defaultPos.x,defaultPos.y,defaultPos.z);
				
				chartAvg.getView().setViewPoint(coord);	
				chartAvg.getAxeLayout().setZAxeLabelDisplayed(true);
				chartAvg.getAxeLayout().setZTickLabelDisplayed(true);
				
				chartStdDev.getView().setViewPoint(coord);				
				chartStdDev.getAxeLayout().setZAxeLabelDisplayed(true);
				chartStdDev.getAxeLayout().setZTickLabelDisplayed(true);


			}
		});
		panel_2.add(btnISO, BorderLayout.CENTER);

		btnRight = new JButton("Right");
		btnRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				Coord3d current = chartAvg.getView().getViewPoint();
				
				current.x = current.x + rotateTick;
				
				chartAvg.getView().setViewPoint(current);
				chartStdDev.getView().setViewPoint(current);

			}
		});
		panel_2.add(btnRight, BorderLayout.EAST);
		
		chartContainerPanel = new JPanel();
		gui.getContentPane().add(chartContainerPanel, BorderLayout.CENTER);
		GridBagLayout gbl_chartContainerPanel = new GridBagLayout();
		gbl_chartContainerPanel.columnWidths = new int[] {0, 0};
		gbl_chartContainerPanel.rowHeights = new int[] {0};
		gbl_chartContainerPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_chartContainerPanel.rowWeights = new double[]{1.0};
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
				SurfacePlotterUtil util = new SurfacePlotterUtil();
			}
		});

	}

	public void addAvgChart(String file)
	{
		if (surfaceAvg != null)
		{
			chartContainerPanel.remove((Component) chartAvg.getCanvas());
			chartAvg = null;
			surfaceAvg.dispose();
			surfaceAvg = null;
		}
		
		BatchLogProcessorMapper avgMapper = new BatchLogProcessorMapper(file, 0);

		Range avgXRange = new Range(avgMapper.getXmin(), avgMapper.getXmax());
		Range avgYRange = new Range(avgMapper.getYmin(), avgMapper.getYmax());
		
		surfaceAvg = Builder.buildOrthonormal(new OrthonormalGrid(avgXRange, avgMapper.getXSteps(), avgYRange, avgMapper.getYSteps()), avgMapper);
		surfaceAvg.setColorMapper(new ColorMapper(new ColorMapRainbow(), surfaceAvg.getBounds().getZmin(), surfaceAvg.getBounds().getZmax(), new Color(1, 1, 1, 0.95f)));
		surfaceAvg.setFaceDisplayed(true);
		surfaceAvg.setWireframeDisplayed(true);
		surfaceAvg.setWireframeColor(Color.BLACK);
		
		chartAvg = AWTChartComponentFactory.chart(Quality.Intermediate, "awt");
		chartAvg.getAxeLayout().setXAxeLabel(avgMapper.getXAxisName());
		chartAvg.getAxeLayout().setYAxeLabel(avgMapper.getYAxisName());
		chartAvg.getAxeLayout().setZAxeLabel(avgMapper.getZAxisName());
		chartAvg.getScene().getGraph().add(surfaceAvg);
		
		AWTColorbarLegend avgColorBar = new AWTColorbarLegend(surfaceAvg, chartAvg.getView().getAxe().getLayout());
		surfaceAvg.setLegend(avgColorBar);
		
		// Tick mapping
		chartAvg.getAxeLayout().setXTickRenderer(avgMapper.getXTickMapper());
		chartAvg.getAxeLayout().setYTickRenderer(avgMapper.getYTickMapper());
		
		chartAvg.addMouseController();
		
		GridBagConstraints gbcAvg = new GridBagConstraints();
		gbcAvg.fill = GridBagConstraints.BOTH;
		gbcAvg.insets = new Insets(0, 0, 5, 0);
		gbcAvg.gridx = 0;
		gbcAvg.gridy = 0;
		chartContainerPanel.add((Component) chartAvg.getCanvas(), gbcAvg);
		
	}
	
	public void addStdDevChart(String file)
	{
		if (surfaceStdDev != null)
		{
			chartContainerPanel.remove((Component) chartStdDev.getCanvas());
			chartStdDev = null;
			surfaceStdDev.dispose();
			surfaceStdDev = null;
		}
		
		BatchLogProcessorMapper stdDevMapper = new BatchLogProcessorMapper(file, 1);

		Range stdDevXRange = new Range(stdDevMapper.getXmin(), stdDevMapper.getXmax());
		Range stdDevYRange = new Range(stdDevMapper.getYmin(), stdDevMapper.getYmax());
		
		surfaceStdDev = Builder.buildOrthonormal(new OrthonormalGrid(stdDevXRange, stdDevMapper.getXSteps(), stdDevYRange, stdDevMapper.getYSteps()), stdDevMapper);
		surfaceStdDev.setColorMapper(new ColorMapper(new ColorMapRainbow(), surfaceStdDev.getBounds().getZmin(), surfaceStdDev.getBounds().getZmax(), new Color(1, 1, 1, 0.95f)));
		surfaceStdDev.setFaceDisplayed(true);
		surfaceStdDev.setWireframeDisplayed(true);
		surfaceStdDev.setWireframeColor(Color.BLACK);
		
		chartStdDev = AWTChartComponentFactory.chart(Quality.Intermediate, "awt");
		chartStdDev.getAxeLayout().setXAxeLabel(stdDevMapper.getXAxisName());
		chartStdDev.getAxeLayout().setYAxeLabel(stdDevMapper.getYAxisName());
		chartStdDev.getAxeLayout().setZAxeLabel(stdDevMapper.getZAxisName());
		chartStdDev.getScene().getGraph().add(surfaceStdDev);
		
		AWTColorbarLegend stdDevColorBar = new AWTColorbarLegend(surfaceStdDev, chartStdDev.getView().getAxe().getLayout());
		surfaceStdDev.setLegend(stdDevColorBar);
		
		// Tick mapping
		chartStdDev.getAxeLayout().setXTickRenderer(stdDevMapper.getXTickMapper());
		chartStdDev.getAxeLayout().setYTickRenderer(stdDevMapper.getYTickMapper());
		
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
		if (e.getSource() == mntmOpen)
		{
			final JFileChooser filechooser = new JFileChooser(new File(openCD));

			DebugLogger.output("Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				DebugLogger.output("New File Choosen");

				String file = filechooser.getSelectedFile().getAbsolutePath();
				
				openCD = filechooser.getCurrentDirectory().getAbsolutePath();
				
				gui.setTitle(filechooser.getSelectedFile().getName());
				
				DebugLogger.output(file);

				addAvgChart(file);
				
				addStdDevChart(file);

				// chart = new Chart(factory, Quality.Fastest);
				//MapperContourPictureGenerator avgContour = new MapperContourPictureGenerator(avgMapper, avgXRange, avgYRange);
				//ContourAxeBox avgCab = new ContourAxeBox(chartAvg.getView().getAxe().getBoxBounds());
				//avgCab.setContourImg(avgContour.getHeightMap(new DefaultContourColoringPolicy(new ColorMapper(new ColorMapRainbow(), surfaceStdDev.getBounds().getZmin(), surfaceStdDev.getBounds().getZmax(), new Color(1, 1, 1, 0.95f))), (int)avgXRange.getMax(), (int)avgYRange.getMax(), (int)avgMapper.getValueMax()), avgXRange, avgYRange);
				//chartAvg.getView().setAxe(avgCab);				
				
				/*GLSLProgram.DEFAULT_STRICTNESS = Strictness.CONSOLE_NO_WARN_UNIFORM_NOT_FOUND;
				IChartComponentFactory factory = new PeelingComponentFactory(PeelingMethod.DUAL_PEELING_MODE);

				GLProfile profile = GLProfile.getMaxProgrammable(true);
				GLCapabilities capabilities = new GLCapabilities(profile);
				capabilities.setHardwareAccelerated(true);*/

				gui.validate();
				
				Coord3d coord = new Coord3d(defaultPos.x,defaultPos.y,defaultPos.z);
				
				chartAvg.getView().setViewPoint(coord);				
				chartStdDev.getView().setViewPoint(coord);				

			}
		}
		if (e.getSource() == mntmExit)
		{
			doProgramExit();
		}

		if (e.getSource() == mntmExportImage)
		{

			final JFileChooser filechooser = new JFileChooser(new File(saveCD));

			int val = filechooser.showSaveDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
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

					BufferedImage exportImage = new BufferedImage(avgImg.getWidth()+stddevImg.getWidth(), avgImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
					
					exportImage.getGraphics().drawImage(avgImg, 0, 0, null);
					exportImage.getGraphics().drawImage(stddevImg, avgImg.getWidth(), 0, null);
					
					if (fileName.indexOf(".") > 0)
					{
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
					}

					// Save the screenshot
					File outputfile = new File(fileName + ".png");
					ImageIO.write(exportImage, "png", outputfile);					

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
		});

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
