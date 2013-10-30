package alifeSim.Gui;

import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import java.awt.Color;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Panel;

public class NewGUI 
{
	private static JFrame guiFrame;
	
	public static void main(String args[])
	{
		lookandFeel();
		
		setUpGUI();
		
		guiFrame.setVisible(true);
		guiFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		
		JTabbedPane simTabs = new JTabbedPane(JTabbedPane.LEFT);
		guiFrame.getContentPane().add(simTabs, BorderLayout.CENTER);
		
		JPanel sysInfo = new JPanel();
		simTabs.addTab("SysInfo", null, sysInfo, null);	
		
		JTabbedPane simTab = new JTabbedPane(JTabbedPane.TOP);
		simTabs.addTab("Simulation ", null, simTab, null);
		
		JPanel simInfo = new JPanel();
		simTab.addTab("Information", null, simInfo, null);
		
		Panel graphPanel = new Panel();
		simTab.addTab("Graph Panel", null, graphPanel, null);
		
		JMenuBar menuBar = new JMenuBar();
		guiFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpenScenario = new JMenuItem("OpenScenario");
		mnFile.add(mntmOpenScenario);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
	}
	
	private static void setUpGUI()
	{
		guiFrame = new JFrame();
	}
	
	/* Use the java provided system look and feel */
	private static void lookandFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (InstantiationException e1)
		{
			e1.printStackTrace();
		}
		catch (IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
	}
}
