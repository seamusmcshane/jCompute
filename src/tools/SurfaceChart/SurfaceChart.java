package tools.SurfaceChart;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.TextBatchLogProcessorMapper;
import jCompute.Batch.LogFileProcessor.XMLBatchLogProcessorMapper;
import jCompute.util.FileUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;

import org.lwjgl.opengl.Display;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class SurfaceChart implements WindowListener, ActionListener
{
	private static String openCD = "./stats";
	
	private static JFrame gui;
	
	private JMenuItem mntmOpen;
	
	private int width = 900;
	private int height = 450;
	
	private SurfacePlotEnv glEnv;
	
	public SurfaceChart()
	{
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gui.setMinimumSize(new Dimension((int) width, (int) height));
		
		LwjglApplicationConfiguration.disableAudio = true;
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		
		cfg.title = "Bar Surface";
		cfg.samples = 16;
		cfg.vSyncEnabled = true;
		cfg.useGL30 = false;
		
		glEnv = new SurfacePlotEnv(width, height);
		
		LwjglCanvas canvas = new LwjglCanvas(glEnv, cfg);
		
		gui.getContentPane().add(canvas.getCanvas(), BorderLayout.CENTER);
		canvas.getGraphics().setVSync(true);
		
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
				new SurfaceChart();
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
				BatchLogInf mapper = null;
				
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
				
				glEnv.setData(mapper);
				
				gui.validate();
				
			}
		}
	}
}