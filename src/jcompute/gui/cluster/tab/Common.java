package jcompute.gui.cluster.tab;

import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.SwingWorker.StateValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.gui.component.swing.MessageBox;
import jcompute.gui.component.swing.swingworker.LoadableResult;
import jcompute.gui.component.swing.swingworker.LoadableTask;

public class Common
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(Common.class);
	
	/**
	 * This static method handle parsing of identical PropertyChangeEvent in benchmark and batch tab.
	 * It entirely for displaying a notice if one or more batches failed to load from LoadableTask.
	 * 
	 * @param component
	 * @param e
	 * @param indexes
	 * @param filePaths
	 */
	public static void handlePropertyChangeEvent(JComponent component, PropertyChangeEvent e, int[] indexes, String[] filePaths)
	{
		// Check for the BackgroundLoadableTasks
		if(e.getSource() instanceof LoadableTask)
		{
			// Interested the state changing
			if("state".equals(e.getPropertyName()))
			{
				// Use the properties state.
				StateValue state = (StateValue) e.getNewValue();
				
				// Is the state done
				if(state == StateValue.DONE)
				{
					// Safe for now until there are other tasks
					LoadableTask task = ((LoadableTask) e.getSource());
					
					// If there are errors then the task result indexes are marked with fails
					if(task.getTotalFails() > 0)
					{
						// Get the result indexes
						int[] results = task.getResult();
						
						// Build up the error message as there may be more than 1 fail so coalesce error result into one pop up
						StringBuilder errorMessage = new StringBuilder();
						
						// Also record successes - may aid debugging why the others failed
						StringBuilder okBatches = new StringBuilder();
						
						// Work out which batches failed/succeeded and group them for outputting
						for(int i = 0; i < results.length; i++)
						{
							if(results[i] == LoadableResult.FAILED_MARK)
							{
								// This batch failed
								errorMessage.append("Batch creation failed for " + filePaths[i] + " \n");
							}
							else
							{
								// This batch succeeded
								okBatches.append("Batch creation succeeded " + filePaths[i] + " \n");
							}
						}
						
						// Fails are already added, append a footer for them
						errorMessage.append("Total Fails " + task.getTotalFails() + " \n\n");
						
						// Now append the successful batches.
						errorMessage.append(okBatches);
						
						// And a footer
						errorMessage.append("Total Successes " + task.getTotalSuccesses() + " \n");
						
						// Log the error - the user may not be able to copy or see the message if swing has gone down.
						log.error(errorMessage);
						
						MessageBox.popup(errorMessage.toString(), component);
					}
				}
			}
		}
	}
}
