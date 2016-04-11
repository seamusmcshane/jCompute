package jCompute.Gui.Component.swing.jpanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import jCompute.Gui.Component.swing.jcomponent.JComputeProgressBar;

public class JComputeProgressMonitor extends JPanel implements PropertyChangeListener
{
	private JComputeProgressBar progressBar;
	
	public JComputeProgressMonitor(int min, int max)
	{
		setLayout(new BorderLayout());
		
		progressBar = new JComputeProgressBar(getFont().deriveFont(getFont().getSize() * .75f));
		
		setMinimumSize(new Dimension(10, 20));
		setPreferredSize(new Dimension(10, 20));
		
		progressBar.prepare(0, false);
		
		add(progressBar, BorderLayout.CENTER);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		if("progress".equals(e.getPropertyName()))
		{
			progressBar.setSize(getWidth(), getHeight());
			
			progressBar.prepare((((SwingWorker) e.getSource()).getProgress()), false);
			
			progressBar.repaint();
		}
	}
	
	public void reset()
	{
		progressBar.prepare(0, false);
	}
}