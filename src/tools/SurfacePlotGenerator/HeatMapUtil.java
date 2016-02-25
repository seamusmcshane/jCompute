package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchInfoLogProcessor;
import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.TextBatchLogProcessorMapper;
import jCompute.Batch.LogFileProcessor.XMLBatchLogProcessorMapper;
import jCompute.util.FileUtil;
import jCompute.util.LookAndFeel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class HeatMapUtil implements WindowListener
{
	private static JFrame gui;
	
	private JScrollPane sp;
	private static HeatMap hm;
	
	private static String openCD = "C:\\Users\\Seamie\\Desktop\\PHD\\WorkSpace\\jCompute\\stats\\Prg3_84-90x100\\2016-02-01@1942[0] 84HungerThresholdVsInitialPopulation";
	private static String saveCD = "";
	private JButton btnSave;
	
	public HeatMapUtil()
	{
		System.setProperty("log4j.configurationFile", "log/config/log4j2-consoleonly.xml");
		
		LookAndFeel.setLookandFeel("default");
		
		boolean legend = true;
		int scale = 10;
		
		int iWidth = 1000 * scale;
		
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		gui.addWindowListener(this);
		
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
					loadLogfile(filechooser.getSelectedFile().getPath(), filechooser.getCurrentDirectory().toString(), filechooser.getSelectedFile().getName());
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
		
		hm = new HeatMap(iWidth, legend, scale);
		sp = new JScrollPane(hm);
		
		sp.setPreferredSize(new Dimension(1024, 768));
		
		gui.getContentPane().add(sp, BorderLayout.CENTER);
		
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		gui.pack();
		gui.setVisible(true);
	}
	
	private static void loadLogfile(String fullPath, String currentDirectory, String documentName)
	{
		System.out.println("Path : " + fullPath);
		
		// Level 0
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
				
				BatchInfoLogProcessor ilp = new BatchInfoLogProcessor(currentDirectory + File.separator + "infoLog.log");
				
				batchLog = new TextBatchLogProcessorMapper(fullPath, ilp.getMaxSteps());
				
			break;
			default:
				System.out.println("Unsupported LogType " + ext);
			break;
		}
		
		openCD = fullPath;
		
		hm.setLog(batchLog);
		
		gui.setTitle(fullPath);
		
		gui.pack();
		gui.repaint();
		
		System.out.println("Report Finished");
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
