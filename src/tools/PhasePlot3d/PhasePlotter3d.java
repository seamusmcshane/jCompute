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


public class PhasePlotter3d implements WindowListener, ActionListener
{
	private static JFrame gui;
	private int width = 1800;
	private int height = 900;
	
	private JMenuItem mntmOpen;
	
	private LibGDXGLPanel plotPanel;
	
	private PhasePlotEnviroment glEnv;
	
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

				//names = new String[stats.size()];
				StatSample[][] histories = new StatSample[stats.size()][];				

				for(int st = 0; st < stats.size(); st++)
				{
					SingleStat temp = stats.get(st);
					//names[st] = new String(temp.getStatName());
					//historyListModel.addElement(names[st]);
					histories[st] = temp.getHistoryAsArray();
				}
				System.out.println("Got Arrays");

				float envScale = glEnv.getScale();
				
				float[] points = new float[numSamples*3];
				
				float xMax = Float.NEGATIVE_INFINITY;
				float yMax = Float.NEGATIVE_INFINITY;
				float zMax = Float.NEGATIVE_INFINITY;
				
				float xMin = Float.POSITIVE_INFINITY;
				float yMin = Float.POSITIVE_INFINITY;
				float zMin = Float.POSITIVE_INFINITY;
				
				int point = 0;
				// Assumes Population chart
				for(int i=0;i<numSamples;i++)
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
					
					points[point+1] = y;
					
		            if(y > yMax)
		            {
		            	yMax = (float) y;
		            }
		            
		            if(y < yMin)
		            {
		            	yMin = (float) y;
		            }
		            
					points[point+2] = z;
					
		            
		            if(z > zMax)
		            {
		            	zMax = (float) z;
		            }
		            
		            if(z < zMin)
		            {
		            	zMin = (float) z;
		            }
					
					point+=3;
				}
				
				System.out.println("Scaling Points");

				boolean sameScale = false;
				
				/*float dMax = Math.max(xMax, yMax);
				dMax = Math.max(dMax, zMax);*/				
				
				float xScale = ((envScale*2)/xMax);
				float yScale = ((envScale*2)/yMax);
				float zScale = ((envScale*2)/zMax);
				
				float xMid = 0;
				float yMid = 0;
				float zMid = 0;
				
				if(sameScale)
				{					
					float scale = Math.min(xScale, yScale);
					scale = Math.min(scale, zScale);	
					
					for(int p=0;p<(numSamples*3);p+=3)
					{
						points[p] = (points[p]*scale)-envScale;
						points[p+1] = (points[p+1]*scale)-envScale;
						points[p+2] = (points[p+2]*scale)-envScale;
					}
					
					xMid = (((xMax/2)+(xMin/2))*scale)-envScale;
					yMid = (((yMax/2)+(yMin/2))*scale)-envScale;
					zMid = (((zMax/2)+(zMin/2))*scale)-envScale;
				}
				else
				{
					for(int p=0;p<(numSamples*3);p+=3)
					{
						points[p] = (points[p]*xScale)-envScale;
						points[p+1] = (points[p+1]*yScale)-envScale;
						points[p+2] = (points[p+2]*zScale)-envScale;
					}
					xMid = (((xMax/2)+(xMin/2))*xScale)-envScale;
					yMid = (((yMax/2)+(yMin/2))*yScale)-envScale;
					zMid = (((zMax/2)+(zMin/2))*zScale)-envScale;
				}				
				
				System.out.println("Setting Points");

				glEnv.setPlotPoints(points,new float[]{xMid,yMid,zMid});
			}
		}
	}
}