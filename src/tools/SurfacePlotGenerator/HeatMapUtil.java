package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.XMLBatchLogProcessorMapper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class HeatMapUtil implements WindowListener
{
	private static int width,height;
	
	private static JFrame gui;
	private static HeatMap hm;
	
	
	public HeatMapUtil()
	{
		width = 800;
		height = 800;
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		gui.addWindowListener(this);
		
		gui.setMinimumSize(new Dimension((int) width, (int) height));

		gui.setLayout(new BorderLayout());
		
		XMLBatchLogProcessorMapper batchLog = new XMLBatchLogProcessorMapper("stats/ItemLog-nohunger-prey.xml");

		hm = new HeatMap(batchLog);
		
		gui.add(hm);
		
		gui.setVisible(true);	
	
	}
	
	public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new HeatMapUtil();
			}
		});
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
}
