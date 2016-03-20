package tools.SurfaceChart;

import jCompute.Batch.LogFileProcessor.BatchInfoLogProcessor;
import jCompute.Batch.LogFileProcessor.BatchLogProcessor;
import jCompute.Gui.Component.Swing.MessageBox;
import jCompute.util.FileUtil;
import jCompute.util.LookAndFeel;
import jCompute.util.Text;
import tools.Common.LibGDXGLPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import org.lwjgl.opengl.Display;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class SurfaceChartUtil implements WindowListener, ActionListener
{
	private static String openCD = "./stats";
	
	private static JFrame gui;
	
	private JMenuItem mntmOpen;
	
	private int width = 900;
	private int height = 450;
	
	private SurfacePlotEnv glEnv;
	private LibGDXGLPanel libGDXGLPanel;
	
	public SurfaceChartUtil()
	{
		LookAndFeel.setLookandFeel("default");
		
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gui.setMinimumSize(new Dimension((int) width, (int) height));
		
		LwjglApplicationConfiguration.disableAudio = true;
		
		glEnv = new SurfacePlotEnv(width, height);
		
		libGDXGLPanel = new LibGDXGLPanel(glEnv, 0, false, "Surface Chart");
		
		gui.getContentPane().add(libGDXGLPanel, BorderLayout.CENTER);
		
		gui.pack();
		gui.setVisible(true);
		gui.setSize(width, height);
		
		JMenuBar menuBar = new JMenuBar();
		gui.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(this);
		mnFile.add(mntmOpen);
		
		gui.addWindowListener(this);
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new SurfaceChartUtil();
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
			final JFileChooser filechooser = new JFileChooser(new File(openCD));
			
			System.out.println("Open Dialog");
			
			int val = filechooser.showOpenDialog(filechooser);
			
			if(val == JFileChooser.APPROVE_OPTION)
			{
				System.out.println("New File Choosen");
				
				String file = filechooser.getSelectedFile().getAbsolutePath();
				
				openCD = filechooser.getCurrentDirectory().getAbsolutePath();
				
				gui.setTitle(filechooser.getSelectedFile().getName());
				
				System.out.println(file);
				
				System.out.println("Creating Mapper");
				String ext = FileUtil.getFileNameExtension(file);
				System.out.println(ext);
				
				BatchInfoLogProcessor ilp = null;
				try
				{
					ilp = new BatchInfoLogProcessor(filechooser.getCurrentDirectory() + File.separator + "infoLog.log");
				}
				catch(IOException e1)
				{
					String st = Text.stackTraceToString(e1.getStackTrace(), false);
					String message = "Error Reading info log " + "\n" + e1.getMessage() + "\n" + st;
					
					MessageBox.popup(message, gui);
				}
				
				BatchLogProcessor logProcessor;
				try
				{
					// If there is an info log - use the range limits 0 to max steps possible, else range limits will be that of the data.
					logProcessor = (ilp != null) ? new BatchLogProcessor(file, 0, ilp.getMaxSteps()) : new BatchLogProcessor(file);
					
					glEnv.setData(logProcessor);
				}
				catch(IOException e1)
				{
					String st = Text.stackTraceToString(e1.getStackTrace(), true);
					
					String message = "Error Reading Item log " + "\n" + e1.getMessage() + "\n" + st;
					
					MessageBox.popup(message, gui);
				}
			}
			
			gui.validate();
		}
	}
}