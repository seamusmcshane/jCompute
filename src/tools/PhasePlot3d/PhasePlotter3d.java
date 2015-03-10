package tools.PhasePlot3d;

import jCompute.Stats.Logs.CSVLogParser;
import jCompute.Stats.Trace.SingleStat;
import jCompute.Stats.Trace.StatSample;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.lwjgl.opengl.Display;

import tools.Common.LibGDXGLPanel;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

public class PhasePlotter3d implements WindowListener, ActionListener
{
	private static JFrame gui;
	private int width = 1800;
	private int height = 900;

	private JMenuItem mntmOpen;

	private LibGDXGLPanel plotPanel;

	private PhasePlotEnviroment glEnv;
	private JMenu mnChartSettings;
	private JMenu mnLineWidths;

	// Plot
	private JMenu mnPlotLineWidth;
	private JRadioButton[] plotLineWidthButton;
	private final ButtonGroup plotButtonGroup = new ButtonGroup();

	// Grid
	private JMenu mnGridLineWidth;
	private JRadioButton[] gridLineWidthButton;
	private final ButtonGroup gridButtonGroup = new ButtonGroup();

	// MinMax
	private JMenu mnMinMaxLineWidth;
	private JRadioButton[] minMaxLineWidthButton;
	private final ButtonGroup minMaxButtonGroup = new ButtonGroup();

	// Width Values
	private final String[] lineWidthValues =
	{
			"0.5", "1", "2", "3", "4"
	};

	public PhasePlotter3d()
	{
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gui.setPreferredSize(new Dimension(width, height));
		gui.setMinimumSize(new Dimension(width, height));

		// GL Env
		glEnv = new PhasePlotEnviroment(width, height);

		// PlotPanel
		plotPanel = new LibGDXGLPanel(glEnv);

		// Plot Panel on Frame
		gui.getContentPane().add(plotPanel, BorderLayout.CENTER);

		gui.pack();

		JMenuBar menuBar = new JMenuBar();
		gui.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(this);
		mnFile.add(mntmOpen);

		mnChartSettings = new JMenu("Settings");
		menuBar.add(mnChartSettings);

		mnLineWidths = new JMenu("Line Widths");
		mnChartSettings.add(mnLineWidths);

		// Plot Line Widths
		mnPlotLineWidth = new JMenu("Plot");
		mnLineWidths.add(mnPlotLineWidth);

		plotLineWidthButton = new JRadioButton[lineWidthValues.length];
		for(int i = 0; i < lineWidthValues.length; i++)
		{
			plotLineWidthButton[i] = new JRadioButton(lineWidthValues[i]);
			plotButtonGroup.add(plotLineWidthButton[i]);
			plotLineWidthButton[i].addActionListener(this);
			mnPlotLineWidth.add(plotLineWidthButton[i]);
		}
		plotLineWidthButton[2].setSelected(true);

		// Grid Line Widths
		mnGridLineWidth = new JMenu("Grid");
		mnLineWidths.add(mnGridLineWidth);

		gridLineWidthButton = new JRadioButton[lineWidthValues.length];
		for(int i = 0; i < lineWidthValues.length; i++)
		{
			gridLineWidthButton[i] = new JRadioButton(lineWidthValues[i]);
			gridButtonGroup.add(gridLineWidthButton[i]);
			gridLineWidthButton[i].addActionListener(this);
			mnGridLineWidth.add(gridLineWidthButton[i]);
		}
		gridLineWidthButton[1].setSelected(true);

		// MinMax Line Widths
		mnMinMaxLineWidth = new JMenu("MinMax");
		mnLineWidths.add(mnMinMaxLineWidth);

		minMaxLineWidthButton = new JRadioButton[lineWidthValues.length];
		for(int i = 0; i < lineWidthValues.length; i++)
		{
			minMaxLineWidthButton[i] = new JRadioButton(lineWidthValues[i]);
			minMaxButtonGroup.add(minMaxLineWidthButton[i]);
			minMaxLineWidthButton[i].addActionListener(this);
			mnMinMaxLineWidth.add(minMaxLineWidthButton[i]);
		}
		minMaxLineWidthButton[1].setSelected(true);

		gui.setVisible(true);

		gui.addWindowListener(this);
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new PhasePlotter3d();
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
					// LWJGL
					Display.destroy();

					// Do EXIT
					System.exit(0);
				}
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == mntmOpen)
		{

			final JFileChooser filechooser = new JFileChooser(new File("./stats"));

			System.out.println("Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if(val == JFileChooser.APPROVE_OPTION)
			{

				// OPEN FILE
				System.out.println("New File Choosen");

				String file = filechooser.getSelectedFile().getAbsolutePath();
				String fname = filechooser.getSelectedFile().getName();

				gui.setTitle(fname);

				System.out.println(file);

				CSVLogParser parser = new CSVLogParser(file);

				ArrayList<SingleStat> stats = parser.getStats();
				int numSamples = parser.getSampleNum();

				// names = new String[stats.size()];
				StatSample[][] histories = new StatSample[stats.size()][];

				String[] names = new String[stats.size()];

				for(int st = 0; st < stats.size(); st++)
				{
					SingleStat temp = stats.get(st);
					names[st] = new String(temp.getStatName());
					// historyListModel.addElement(names[st]);
					histories[st] = temp.getHistoryAsArray();
				}
				System.out.println("Got Arrays");

				float envScale = glEnv.getScale();

				float[] points = new float[numSamples * 3];

				float xMax = Float.NEGATIVE_INFINITY;
				float yMax = Float.NEGATIVE_INFINITY;
				float zMax = Float.NEGATIVE_INFINITY;

				float xMin = Float.POSITIVE_INFINITY;
				float yMin = Float.POSITIVE_INFINITY;
				float zMin = Float.POSITIVE_INFINITY;

				int point = 0;
				// Assumes Population chart
				for(int i = 0; i < numSamples; i++)
				{
					// Plants, Predator, Prey
					float x = (float) histories[1][i].getSample();
					float y = (float) histories[0][i].getSample();
					float z = (float) histories[2][i].getSample();

					points[point] = x;

					if(x > xMax)
					{
						xMax = (float) x;
					}

					if(x < xMin)
					{
						xMin = (float) x;
					}

					points[point + 1] = y;

					if(y > yMax)
					{
						yMax = (float) y;
					}

					if(y < yMin)
					{
						yMin = (float) y;
					}

					points[point + 2] = z;

					if(z > zMax)
					{
						zMax = (float) z;
					}

					if(z < zMin)
					{
						zMin = (float) z;
					}

					point += 3;
				}

				System.out.println("Scaling Points");

				boolean sameScale = true;

				/*
				 * float dMax = Math.max(xMax, yMax);
				 * dMax = Math.max(dMax, zMax);
				 */

				float xScale = ((envScale * 2) / xMax);
				float yScale = ((envScale * 2) / yMax);
				float zScale = ((envScale * 2) / zMax);

				float xMid = 0;
				float yMid = 0;
				float zMid = 0;

				if(sameScale)
				{
					float scale = Math.min(xScale, yScale);
					scale = Math.min(scale, zScale);

					for(int p = 0; p < (numSamples * 3); p += 3)
					{
						points[p] = (points[p] * scale) - envScale;
						points[p + 1] = (points[p + 1] * scale) - envScale;
						points[p + 2] = (points[p + 2] * scale) - envScale;
					}

					xMid = (((xMax / 2) + (xMin / 2)) * scale) - envScale;
					yMid = (((yMax / 2) + (yMin / 2)) * scale) - envScale;
					zMid = (((zMax / 2) + (zMin / 2)) * scale) - envScale;
				}
				else
				{
					for(int p = 0; p < (numSamples * 3); p += 3)
					{
						points[p] = (points[p] * xScale) - envScale;
						points[p + 1] = (points[p + 1] * yScale) - envScale;
						points[p + 2] = (points[p + 2] * zScale) - envScale;
					}
					xMid = (((xMax / 2) + (xMin / 2)) * xScale) - envScale;
					yMid = (((yMax / 2) + (yMin / 2)) * yScale) - envScale;
					zMid = (((zMax / 2) + (zMin / 2)) * zScale) - envScale;
				}

				System.out.println("Setting Points");

				glEnv.setPlotPoints(points, new float[]
				{
						xMid, yMid, zMid
				}, names[0], names[1], names[2]);
			}
		}
		else if((e.getSource() == plotLineWidthButton[0] | e.getSource() == plotLineWidthButton[1]
				| e.getSource() == plotLineWidthButton[2] | e.getSource() == plotLineWidthButton[3] | e.getSource() == plotLineWidthButton[4]))
		{

			for(JRadioButton button : plotLineWidthButton)
			{
				if(button.isSelected() == true)
				{
					float val = Float.parseFloat(button.getText());

					glEnv.setPlotLineWidth(val);
				}
			}

		}
		else if((e.getSource() == gridLineWidthButton[0] | e.getSource() == gridLineWidthButton[1]
				| e.getSource() == gridLineWidthButton[2] | e.getSource() == gridLineWidthButton[3] | e.getSource() == gridLineWidthButton[4]))
		{

			for(JRadioButton button : gridLineWidthButton)
			{
				if(button.isSelected() == true)
				{
					float val = Float.parseFloat(button.getText());

					glEnv.setGridLineWidth(val);
				}
			}

		}
		else if((e.getSource() == minMaxLineWidthButton[0] | e.getSource() == minMaxLineWidthButton[1]
				| e.getSource() == minMaxLineWidthButton[2] | e.getSource() == minMaxLineWidthButton[3] | e.getSource() == minMaxLineWidthButton[4]))
		{
			for(JRadioButton button : minMaxLineWidthButton)
			{
				if(button.isSelected() == true)
				{
					float val = Float.parseFloat(button.getText());

					glEnv.setMinMaxLineWidth(val);
				}
			}

		}
	}
}