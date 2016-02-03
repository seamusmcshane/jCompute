package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchInfoLogProcessor;
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
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JToolBar;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class HeatMapUtil implements WindowListener
{
	private static int width, height;
	
	private static JFrame gui;
	private static HeatMap hm;
	
	private static String openCD = "";
	private static String saveCD = "";
	private JButton btnSave;
	
	public HeatMapUtil()
	{
		LookAndFeel.setLookandFeel("default");
		
		width = 800;
		height = 800;
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		gui.addWindowListener(this);
		
		gui.setMinimumSize(new Dimension((int) width + 15, (int) height + 35));
		
		gui.getContentPane().setLayout(new BorderLayout());
		
		JToolBar toolBar = new JToolBar();
		gui.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				final JFileChooser filechooser = new JFileChooser(new File(openCD));
				
				filechooser.setDialogTitle("Choose Directory");
				filechooser.setMultiSelectionEnabled(false);
				filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int val = filechooser.showOpenDialog(filechooser);
				
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
							
							BatchInfoLogProcessor ilp = new BatchInfoLogProcessor(filechooser.getCurrentDirectory() + File.separator + "infoLog.log");
							
							batchLog = new TextBatchLogProcessorMapper(fullPath, ilp.getMaxSteps());
							
						break;
						default:
							System.out.println("Unsupported LogType " + ext);
						break;
					}
					
					openCD = fullPath;
					
					hm.setLog(batchLog);
					
					gui.setTitle(fullPath);
					gui.repaint();
					
					System.out.println("Report Finished");
				}
				else
				{
					System.out.println("Report Cancelled");
				}
			}
		});
		toolBar.add(btnOpen);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				BufferedImage image = hm.getImage();
				
				if(image != null)
				{
					final JFileChooser filechooser = new JFileChooser(new File(saveCD));
					
					filechooser.setDialogTitle("Choose Directory");
					filechooser.setMultiSelectionEnabled(false);
					filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int val = filechooser.showSaveDialog(filechooser);
					
					if(val == JFileChooser.APPROVE_OPTION)
					{
						try
						{
							File outputfile = new File(filechooser.getSelectedFile().getAbsolutePath());
							ImageIO.write(image, "png", outputfile);
							saveCD = filechooser.getSelectedFile().getPath();
						}
						catch(IOException ioe)
						{
							System.out.println("Failed to write image");
						}
					}
				}
				else
				{
					JOptionPane.showMessageDialog(gui, "No image to save", "Save image", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		toolBar.add(btnSave);
		
		hm = new HeatMap();
		gui.getContentPane().add(hm);
		
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
