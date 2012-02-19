package alife;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;

import java.awt.BorderLayout;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SimGui
{

	private boolean simLaunched = false;

	private JFrame frame;
	
	
	static JSpinner simStepDiv; 
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					SimGui window = new SimGui();
					window.frame.setVisible(true);					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SimGui()
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Dimension screenSize = toolkit.getScreenSize();
		frame = new JFrame();
		frame.setBounds((int) ((screenSize.getWidth()/2)-(810)), screenSize.height/2-400, 400, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem("Save As..");
		mnFile.add(mntmSaveAs);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mnFile.add(mntmQuit);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel simControlsPannel = new JPanel();
		simControlsPannel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		frame.getContentPane().add(simControlsPannel, BorderLayout.NORTH);

		JButton btnStartSim = new JButton("Start");
		btnStartSim.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{

			}
		});

		JButton btnLaunch = new JButton("Launch");
		btnLaunch.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if (simLaunched == false)
				{
					launchSim();
					simLaunched = true;
				}
			}
		});
		simControlsPannel.add(btnLaunch);
		simControlsPannel.add(btnStartSim);

		JButton btnPause = new JButton("Pause");
		simControlsPannel.add(btnPause);

		JButton btnStop = new JButton("Stop");
		simControlsPannel.add(btnStop);

		JLabel lblStepDiv = new JLabel("Sim Rate");
		simControlsPannel.add(lblStepDiv);

		simStepDiv = new JSpinner();
		simStepDiv.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) 
			{
				Simulation.draw_div = (int) simStepDiv.getValue();
			}
		});
		simStepDiv.setModel(new SpinnerNumberModel(new Integer(1), new Integer(0), null, new Integer(1)));
		simControlsPannel.add(simStepDiv);

		JPanel leftSidePannel = new JPanel();
		frame.getContentPane().add(leftSidePannel, BorderLayout.WEST);

		JPanel statusBar = new JPanel();
		frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
	}

	/**
	 * Background Threaded Simulation Launch (due to Swing)
	 *  
	 */
	private void launchSim()
	{
		/* Need to use a background thread or swing will  sit here waiting for this method to return */
		Thread launcher = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Simulation.Launch(null);				
			}			
		}
		);		
		launcher.start();

	}
}
