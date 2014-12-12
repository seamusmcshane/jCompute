package tools.SurfaceChart;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import org.lwjgl.opengl.Display;

public class SurfaceChart implements WindowListener
{
	private static JFrame gui;
	
	public SurfaceChart()
	{
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		/*LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		
		cfg.title = "Bar Surface";
		cfg.samples = 16;
		cfg.vSyncEnabled = true;*/
		//LwjglCanvas canvas = new LwjglCanvas(new Basic3DTest(),cfg);
		
		LwjglApplicationConfiguration.disableAudio = true;
		
		LwjglAWTCanvas canvas = new LwjglAWTCanvas(new Basic3DTest(), true);
		gui.getContentPane().add(canvas.getCanvas(), BorderLayout.CENTER);
		canvas.getGraphics().setVSync(true);
		
		gui.pack();
		gui.setVisible(true);
		gui.setSize(800, 800);

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

	}}