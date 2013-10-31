package alifeSim.Gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;

public class SimulationInfoTabPanel extends JPanel
{
	private static final long serialVersionUID = 76641721672552215L;

	public SimulationInfoTabPanel()
	{
		super();
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400,300));
		add(panel, BorderLayout.NORTH);
		
		JLabel lblTest = new JLabel("TEST");
		panel.add(lblTest);
	}
	
}
