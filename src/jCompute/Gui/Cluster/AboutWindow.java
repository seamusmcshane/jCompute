package jCompute.Gui.Cluster;

import jCompute.util.JVMInfo;

import javax.swing.JFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
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
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class AboutWindow extends JFrame
{
	private final AboutWindow self;
	private static final long serialVersionUID = -6597372249572030L;
	
	public AboutWindow()
	{
		self = this;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setType(Type.POPUP);
		setUndecorated(true);
		getRootPane().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
		
		setTitle("JVM Info");
		setMinimumSize(new Dimension(500, 250));
		setResizable(false);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel infoPanel = new JPanel();
		getContentPane().add(infoPanel);
		infoPanel.setLayout(new GridLayout(6, 0, 25, 0));
		
		JLabel label_2 = new JLabel("Max Memory");
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setFont(label_2.getFont().deriveFont(label_2.getFont().getStyle() | Font.BOLD));
		infoPanel.add(label_2);
		
		JLabel label_3 = new JLabel(String.valueOf(JVMInfo.getMaxMemory()));
		label_3.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(label_3);
		
		JLabel label_4 = new JLabel("Total Memory");
		label_4.setHorizontalAlignment(SwingConstants.CENTER);
		label_4.setFont(label_4.getFont().deriveFont(label_4.getFont().getStyle() | Font.BOLD));
		infoPanel.add(label_4);
		
		JLabel label_5 = new JLabel(String.valueOf(JVMInfo.getTotalJVMMemory()));
		label_5.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(label_5);
		
		JLabel label_6 = new JLabel("Used Memory");
		label_6.setHorizontalAlignment(SwingConstants.CENTER);
		label_6.setFont(label_6.getFont().deriveFont(label_6.getFont().getStyle() | Font.BOLD));
		infoPanel.add(label_6);
		
		JLabel label_7 = new JLabel(String.valueOf(JVMInfo.getUsedJVMMemory()));
		label_7.setHorizontalAlignment(SwingConstants.CENTER);
		infoPanel.add(label_7);
		
		JPanel titlePanel = new JPanel();
		getContentPane().add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new BorderLayout(0, 0));
		
		JPanel buildPanelCont = new JPanel();
		titlePanel.add(buildPanelCont);
		buildPanelCont.setLayout(new GridLayout(2, 2, 0, 5));
		
		JLabel lblBuilt = new JLabel("Built");
		lblBuilt.setFont(lblBuilt.getFont().deriveFont(lblBuilt.getFont().getStyle() | Font.BOLD));
		lblBuilt.setHorizontalAlignment(SwingConstants.CENTER);
		buildPanelCont.add(lblBuilt);
		
		JLabel lblJcompute_1 = new JLabel("JCOMPUTE");
		buildPanelCont.add(lblJcompute_1);
		lblJcompute_1.setHorizontalAlignment(SwingConstants.LEFT);
		
		JPanel titlePanelCont = new JPanel();
		FlowLayout flowLayout = (FlowLayout) titlePanelCont.getLayout();
		flowLayout.setHgap(10);
		flowLayout.setVgap(10);
		titlePanel.add(titlePanelCont, BorderLayout.NORTH);
		
		JLabel lblJcompute = new JLabel("JCompute");
		titlePanelCont.add(lblJcompute);
		lblJcompute.setFont(lblJcompute.getFont().deriveFont(lblJcompute.getFont().getStyle() | Font.BOLD,
				lblJcompute.getFont().getSize() + 6f));
		lblJcompute.setHorizontalAlignment(SwingConstants.CENTER);
		
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
			
			lblJcompute_1.setText(prop.getProperty("BuildDateTime"));
			
			JLabel label = new JLabel("JVM");
			buildPanelCont.add(label);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | Font.BOLD));
			
			JLabel label_1 = new JLabel(System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
			buildPanelCont.add(label_1);
			label_1.setHorizontalAlignment(SwingConstants.LEFT);
			
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}
		
	}
	
}
