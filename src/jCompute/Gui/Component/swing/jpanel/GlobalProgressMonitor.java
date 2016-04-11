package jCompute.Gui.Component.swing.jpanel;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

public class GlobalProgressMonitor extends JPanel implements PropertyChangeListener
{
	// The singleton
	private static GlobalProgressMonitor progressMonitor;
	
	// real progress drawing component
	private final JComputeProgressMonitor progress;
	
	private GlobalProgressMonitor()
	{
		setLayout(new BorderLayout());
		
		progress = new JComputeProgressMonitor(0, 100);
		
		add(progress, BorderLayout.CENTER);
		
		// GlobalProgressMonitor hides when not in use
		progress.setVisible(false);
	}
	
	public synchronized static GlobalProgressMonitor getInstance()
	{
		if(progressMonitor == null)
		{
			progressMonitor = new GlobalProgressMonitor();
		}
		
		return progressMonitor;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		// GlobalProgressMonitor hides when not in use
		if("state".equals(e.getPropertyName()))
		{
			StateValue state = ((SwingWorker) e.getSource()).getState();
			
			if(state == StateValue.DONE)
			{
				progress.setVisible(false);
			}
			else
			{
				progress.setVisible(true);
			}
		}
		
		// Forward the event to the real progress drawing component
		progress.propertyChange(e);
	}
}
