package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.TextBatchLogProcessorMapper;
import jCompute.Batch.LogFileProcessor.XMLBatchLogProcessorMapper;
import jCompute.util.FileUtil;
import jCompute.util.LookAndFeel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
		
		LookAndFeel.setLookandFeel("default");
		
		final JFileChooser filechooser = new JFileChooser(new File("\\\\Nanoserv\\results\\"));
		
		filechooser.setDialogTitle("Choose Directory");
		filechooser.setMultiSelectionEnabled(false);
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int val = filechooser.showOpenDialog(filechooser);
		
		float scale = 1f;
		
		if(val == JFileChooser.APPROVE_OPTION)
		{
			String fullPath = filechooser.getSelectedFile().getPath();
			System.out.println("Path : " + fullPath);
			
			// Level 0
			String documentName = filechooser.getSelectedFile().getName();
			System.out.println("Document Name will be : " + documentName);
			
			BatchLogInf batchLog = null;
			
			String ext = FileUtil.getFileNameExtension(documentName);
			System.out.println("File ext : " + ext);
			
			switch(ext)
			{
				case "xml":
					
					batchLog = new XMLBatchLogProcessorMapper(fullPath);
					
				break;
				
				case "log":
					
					batchLog = new TextBatchLogProcessorMapper(fullPath);
					
				break;
				default:
					System.out.println("Unsupported LogType " + ext);
				break;
			}
			
			hm = new HeatMap(batchLog);
			
			gui.add(hm);
			
			gui.setVisible(true);	
			
			System.out.println("Report Finished");
		}
		else
		{
			System.out.println("Report Cancelled");
		}
		

	
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
