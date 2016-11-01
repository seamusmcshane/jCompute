package tools.heatmap;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.log.info.processor.InfoLogProcessor;
import jcompute.batch.log.item.processor.ItemLogProcessor;
import jcompute.gui.component.swing.MessageBox;
import jcompute.logging.Logging;
import jcompute.util.LookAndFeel;
import jcompute.util.JCText;
import jcompute.util.TimeString;
import jcompute.util.TimeString.TimeStringFormat;

import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class HeatMapUtil implements WindowListener
{
	// Log4j2 Logger
	private static Logger log;
	
	private static JFrame gui;
	
	private JScrollPane sp;
	private static HeatMap hm;
	
	private static String openCD = "C:\\Users\\Seamie\\Desktop\\PHD\\WorkSpace\\jCompute\\stats\\Prg3_80-100\\2016-03-02@1059[0] 84HungerThresholdVsInitialPopulation";
	private static String saveCD = "";
	private JButton btnSave;
	
	private static String openedLogName;
	
	public HeatMapUtil()
	{
		Logging.initTestLevelLogging();
		
		log = LogManager.getLogger(HeatMapUtil.class);
		
		LookAndFeel.setLookandFeel("default");
		
		boolean legend = true;
		int scale = 1;
		
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
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				final JFileChooser filechooser = new JFileChooser(new File(openCD));
				
				filechooser.setDialogTitle("Choose Directory");
				filechooser.setMultiSelectionEnabled(false);
				filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int val = filechooser.showOpenDialog(gui);
				
				if(val == JFileChooser.APPROVE_OPTION)
				{
					loadLogfile(filechooser.getSelectedFile().getPath(), filechooser.getCurrentDirectory().toString(), filechooser.getSelectedFile().getName());
				}
				else
				{
					log.info("Report Cancelled");
				}
			}
		});
		toolBar.add(btnOpen);
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				BufferedImage image = hm.getImage();
				
				if(image != null)
				{
					final JFileChooser filechooser = new JFileChooser(new File(saveCD));
					
					filechooser.setDialogTitle("Choose Directory");
					filechooser.setMultiSelectionEnabled(false);
					filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					filechooser.setSelectedFile(new File(openedLogName + ".png"));
					int val = filechooser.showSaveDialog(gui);
					
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
							log.error("Failed to write image");
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
		
		btnOpen.doClick();
	}
	
	private void loadLogfile(String fullPath, String currentDirectory, String documentName)
	{
		log.info("Path : " + fullPath);
		
		InfoLogProcessor ilp = null;
		try
		{
			ilp = new InfoLogProcessor(currentDirectory + File.separator + "infoLog.log");
		}
		catch(IOException e1)
		{
			String message = "Error Reading info log\n" + e1.getMessage();
			if(!MessageBox.popup(message, gui))
			{
				log.error(message);
			}
		}
		
		ItemLogProcessor logProcessor;
		try
		{
			// If there is an info log - use the range limits 0 to max steps possible, else range limits will be that of the data.
			logProcessor = (ilp != null) ? new ItemLogProcessor(fullPath, 0, ilp.getMaxSteps()) : new ItemLogProcessor(fullPath);
			
			openedLogName = (ilp != null) ? ilp.getDescription() : logProcessor.getLogFileName();
			
			long timeTaken = logProcessor.getTimeTaken();
			
			openCD = fullPath;
			
			hm.setLog(logProcessor);
			
			timeTaken += hm.getTimeTaken();
			
			log.info("Total Time : " + TimeString.timeInMillisAsFormattedString(timeTaken, TimeStringFormat.SM));
			
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					gui.setTitle(fullPath);
					gui.pack();
					gui.repaint();
				}
			});
		}
		catch(IOException e1)
		{
			String st = JCText.stackTraceToString(e1.getStackTrace(), false);
			
			String message = "Error Reading Item log " + "\n" + e1.getMessage() + "\n" + st;
			
			if(!MessageBox.popup(message, gui))
			{
				log.error(message);
			}
		}
	}
	
	public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@SuppressWarnings("unused")
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
			@Override
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
