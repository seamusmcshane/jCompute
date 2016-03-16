package jCompute.Gui.Component.Swing;

import jCompute.util.JComputeInfo;
import jCompute.util.JVMInfo;
import jCompute.util.OSInfo;
import jCompute.util.Text;

import javax.swing.JFrame;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class AboutWindow extends JFrame
{
	private final AboutWindow self;
	private static final long serialVersionUID = -6597372249572030L;
	
	private Timer updateTimer;
	
	private JLabel lblUsedVal;
	private JLabel lblTotalVal;
	private JLabel lblMaxVal;
	
	public AboutWindow()
	{
		setResizable(false);
		self = this;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setType(Type.NORMAL);
		
		setTitle("jCompute");
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		BorderLayout borderLayout = new BorderLayout();
		getContentPane().setLayout(borderLayout);
		
		// Mem Info
		JPanel memInfoPanel = new JPanel();
		memInfoPanel.setLayout(new GridLayout(0, 6, 0, 0));
		
		JLabel lblUsed = new JLabel("Used Memory");
		lblUsed.setHorizontalAlignment(SwingConstants.CENTER);
		lblUsed.setFont(lblUsed.getFont().deriveFont(lblUsed.getFont().getStyle() | Font.BOLD));
		memInfoPanel.add(lblUsed);
		
		lblUsedVal = new JLabel("");
		lblUsedVal.setHorizontalAlignment(SwingConstants.CENTER);
		memInfoPanel.add(lblUsedVal);
		
		JLabel lblTotal = new JLabel("Total Memory");
		lblTotal.setHorizontalAlignment(SwingConstants.CENTER);
		lblTotal.setFont(lblTotal.getFont().deriveFont(lblTotal.getFont().getStyle() | Font.BOLD));
		memInfoPanel.add(lblTotal);
		
		lblTotalVal = new JLabel("");
		lblTotalVal.setHorizontalAlignment(SwingConstants.CENTER);
		memInfoPanel.add(lblTotalVal);
		
		JLabel lblMax = new JLabel("Max Memory");
		lblMax.setHorizontalAlignment(SwingConstants.CENTER);
		lblMax.setFont(lblMax.getFont().deriveFont(lblMax.getFont().getStyle() | Font.BOLD));
		memInfoPanel.add(lblMax);
		
		lblMaxVal = new JLabel("");
		lblMaxVal.setHorizontalAlignment(SwingConstants.CENTER);
		memInfoPanel.add(lblMaxVal);
		
		// Bottom Pad Panel
		JPanel bottomPad = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottomPad.getLayout();
		flowLayout.setVgap(10);
		flowLayout.setHgap(10);
		getContentPane().add(bottomPad, BorderLayout.SOUTH);
		
		// MemInfo
		bottomPad.add(memInfoPanel, BorderLayout.NORTH);
		
		// Grid Panel
		int numberOfRows = 8;
		SimpleGridPanel gridPanel = new SimpleGridPanel(numberOfRows);
		
		// Row and Indexes
		int buildIndex = gridPanel.addRow("Built Date", "NOT_SET");
		int jvmIndex = gridPanel.addRow("JVM Name and Version", "NOT_SET");
		int osIndex = gridPanel.addRow("Operating System", "NOT_SET");
		int archIndex = gridPanel.addRow("System Architecture", "NOT_SET");
		int hwThreadsIndex = gridPanel.addRow("Hardware Threads", "NOT_SET");
		int phyMemIndex = gridPanel.addRow("Physical Memory", "NOT_SET");
		int launchIndex = gridPanel.addRow("Launched", "NOT_SET");
		final int runTimeIndex = gridPanel.addRow("Running Time", "NOT_SET");
		
		// Set JC Values
		String[] jcInfo = retrieveJComputeBuildInfo();
		gridPanel.changeValText(buildIndex, jcInfo[0]);
		gridPanel.changeValText(launchIndex, jcInfo[1]);
		
		// Set JVM Values
		gridPanel.changeValText(jvmIndex, retrieveJVMInfo());
		
		// Set Values OS
		String[] osInfo = retrieveOSInfo();
		gridPanel.changeValText(osIndex, osInfo[0]);
		gridPanel.changeValText(archIndex, osInfo[1]);
		gridPanel.changeValText(hwThreadsIndex, osInfo[2]);
		gridPanel.changeValText(phyMemIndex, osInfo[3]);
		
		// Text
		BorderLayout bl_topPanel = new BorderLayout();
		bl_topPanel.setHgap(10);
		JPanel topPanel = new JPanel(bl_topPanel);
		getContentPane().add(topPanel, BorderLayout.NORTH);
		
		// Left Pad
		JPanel leftPad = new JPanel(new BorderLayout());
		// leftPad.setBackground(Color.BLACK);
		leftPad.setPreferredSize(new Dimension(50, 20));
		leftPad.setMinimumSize(new Dimension(50, 20));
		topPanel.add(leftPad, BorderLayout.WEST);
		
		// Grid
		topPanel.add(gridPanel, BorderLayout.CENTER);
		
		JPanel titlePanel = new JPanel();
		topPanel.add(titlePanel, BorderLayout.NORTH);
		JLabel lblJcompute = new JLabel("jCompute");
		titlePanel.add(lblJcompute);
		lblJcompute.setHorizontalAlignment(SwingConstants.CENTER);
		lblJcompute.setFont(lblJcompute.getFont().deriveFont(lblJcompute.getFont().getStyle() | Font.BOLD, lblJcompute.getFont().getSize() + 6f));
		
		// Auto Refresh Data
		updateTimer = new Timer("About Update Timer");
		try
		{
			updateTimer.scheduleAtFixedRate(new TimerTask()
			{
				JVMInfo jvmInfo = JVMInfo.getInstance();
				JComputeInfo jComputeInfo = JComputeInfo.getInstance();
				
				@Override
				public void run()
				{
					lblUsedVal.setText(String.valueOf(jvmInfo.getUsedJVMMemory()));
					lblTotalVal.setText(String.valueOf(jvmInfo.getTotalJVMMemory()));
					lblMaxVal.setText(String.valueOf(jvmInfo.getMaxMemory()));
					
					gridPanel.changeValText(runTimeIndex, Text.longTimeToDHMS(jComputeInfo.getRuntime()));
				}
			}, 0, 5000);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		// Close on loose focus
		addFocusListener(new FocusListener()
		{
			
			@Override
			public void focusGained(FocusEvent arg0)
			{
			
			}
			
			@Override
			public void focusLost(FocusEvent arg0)
			{
				self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
			
		});
		
		this.pack();
	}
	
	private String[] retrieveJComputeBuildInfo()
	{
		String val[] =
		{
			"Error Retrieving Build Information", "Error Retrieving Build Information"
		};
		
		try
		{
			JComputeInfo info = JComputeInfo.getInstance();
			
			val[0] = info.getBuildDate();
			val[1] = info.getLaunched();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			
			MessageBox.popup("Error with JComputeInfo", this);
		}
		
		return val;
	}
	
	private String retrieveJVMInfo()
	{
		JVMInfo jvmInfo = JVMInfo.getInstance();
		
		return jvmInfo.getJVMName() + " " + jvmInfo.getJVMVersion();
	}
	
	private String[] retrieveOSInfo()
	{
		OSInfo osInfo = OSInfo.getInstance();
		
		return new String[]
		{
			osInfo.getOSName(), osInfo.getSystemArch(), Integer.toString(osInfo.getHWThreads()), Integer.toString(osInfo
			.getSystemPhysicalMemorySize())
		};
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		
		if(updateTimer != null)
		{
			updateTimer.cancel();
		}
		
		updateTimer = null;
	}
	
}
