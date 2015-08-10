package jCompute.Gui.Component.Swing;

import jCompute.util.JVMInfo;

import javax.swing.JFrame;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Dialog.ModalExclusionType;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

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
		setMinimumSize(new Dimension(600, 150));
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel infoPanel = new JPanel();
		getContentPane().add(infoPanel);
		infoPanel.setLayout(new GridLayout(0, 6, 0, 0));
		
		JLabel lblUsed = new JLabel("Used Memory");
		lblUsed.setHorizontalAlignment(SwingConstants.CENTER);
		lblUsed.setFont(lblUsed.getFont().deriveFont(lblUsed.getFont().getStyle() | Font.BOLD));
		infoPanel.add(lblUsed);
		
		lblUsedVal = new JLabel("");
		lblUsedVal.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(lblUsedVal);
		
		JLabel lblTotal = new JLabel("Total Memory");
		lblTotal.setHorizontalAlignment(SwingConstants.CENTER);
		lblTotal.setFont(lblTotal.getFont().deriveFont(lblTotal.getFont().getStyle() | Font.BOLD));
		infoPanel.add(lblTotal);
		
		lblTotalVal = new JLabel("");
		lblTotalVal.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(lblTotalVal);
		
		JLabel lblMax = new JLabel("Max Memory");
		lblMax.setHorizontalAlignment(SwingConstants.CENTER);
		lblMax.setFont(lblMax.getFont().deriveFont(lblMax.getFont().getStyle() | Font.BOLD));
		infoPanel.add(lblMax);
		
		lblMaxVal = new JLabel("");
		lblMaxVal.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(lblMaxVal);
		
		JPanel titlePanel = new JPanel();
		getContentPane().add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new BorderLayout(0, 0));
		
		JPanel titlePanelCont = new JPanel();
		FlowLayout flowLayout = (FlowLayout) titlePanelCont.getLayout();
		flowLayout.setHgap(10);
		flowLayout.setVgap(10);
		titlePanel.add(titlePanelCont, BorderLayout.NORTH);
		
		JLabel lblJcompute = new JLabel("jCompute");
		titlePanelCont.add(lblJcompute);
		lblJcompute.setFont(lblJcompute.getFont().deriveFont(lblJcompute.getFont().getStyle() | Font.BOLD,
				lblJcompute.getFont().getSize() + 6f));
		
		JPanel buildPanelCont = new JPanel();
		titlePanel.add(buildPanelCont);
		GridBagLayout gbl_buildPanelCont = new GridBagLayout();
		gbl_buildPanelCont.columnWidths = new int[]
		{
			100, 100
		};
		gbl_buildPanelCont.rowHeights = new int[]
		{
			20, 20
		};
		gbl_buildPanelCont.columnWeights = new double[]
		{
			1.0, 1.0
		};
		gbl_buildPanelCont.rowWeights = new double[]
		{
			1.0
		};
		buildPanelCont.setLayout(gbl_buildPanelCont);
		
		JLabel lblBuilt = new JLabel("Built");
		lblBuilt.setFont(lblBuilt.getFont().deriveFont(lblBuilt.getFont().getStyle() | Font.BOLD));
		GridBagConstraints gbc_lblBuilt = new GridBagConstraints();
		gbc_lblBuilt.fill = GridBagConstraints.VERTICAL;
		gbc_lblBuilt.gridx = 0;
		gbc_lblBuilt.gridy = 0;
		buildPanelCont.add(lblBuilt, gbc_lblBuilt);
		
		JLabel lblBuildDate = new JLabel("JCOMPUTE");
		GridBagConstraints gbc_lblBuildDate = new GridBagConstraints();
		gbc_lblBuildDate.fill = GridBagConstraints.BOTH;
		gbc_lblBuildDate.gridx = 1;
		gbc_lblBuildDate.gridy = 0;
		buildPanelCont.add(lblBuildDate, gbc_lblBuildDate);
		lblBuildDate.setHorizontalAlignment(SwingConstants.LEFT);
		
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
		
		FileInputStream input;
		try
		{
			input = new FileInputStream("BuildDateTime");
			Properties prop = new Properties();
			prop.load(input);
			
			lblBuildDate.setText(prop.getProperty("BuildDateTime"));
			
			JLabel lblJvm = new JLabel("JVM");
			GridBagConstraints gbc_lblJvm = new GridBagConstraints();
			gbc_lblJvm.fill = GridBagConstraints.VERTICAL;
			gbc_lblJvm.gridx = 0;
			gbc_lblJvm.gridy = 1;
			buildPanelCont.add(lblJvm, gbc_lblJvm);
			lblJvm.setFont(lblJvm.getFont().deriveFont(lblJvm.getFont().getStyle() | Font.BOLD));
			
			lblJvm.setFont(lblJvm.getFont().deriveFont(lblJvm.getFont().getStyle() | Font.BOLD));
			
			JLabel lblJvmNameVer = new JLabel();
			GridBagConstraints gbc_lblJvmNameVer = new GridBagConstraints();
			gbc_lblJvmNameVer.fill = GridBagConstraints.BOTH;
			gbc_lblJvmNameVer.gridx = 1;
			gbc_lblJvmNameVer.gridy = 1;
			lblJvmNameVer.setText(JVMInfo.getJVMName() + " " + JVMInfo.getJVMVersion());
			buildPanelCont.add(lblJvmNameVer, gbc_lblJvmNameVer);
			
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.SOUTH);
			
			updateTimer = new Timer("About Update Timer");
			updateTimer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					lblUsedVal.setText(String.valueOf(JVMInfo.getUsedJVMMemory()));
					lblTotalVal.setText(String.valueOf(JVMInfo.getTotalJVMMemory()));
					lblMaxVal.setText(String.valueOf(JVMInfo.getMaxMemory()));
				}
			}, 0, 5000);
			
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}

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
