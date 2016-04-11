package jCompute.gui.component.swing.swingworker;

import javax.swing.SwingWorker;

/**
 * This task will initiate the load operations of implementers of {@link Loadable#load}
 * The task implements LoadableResult allowing inspecting the result of the operations.
 * 
 * @author Seamus McShane
 * @see Loadable
 * @see LoadableResult
 * @see SwingWorker
 */
public class LoadableTask extends SwingWorker<Void, Void> implements LoadableResult
{
	private float progressInc;
	
	private int successes = 0;
	private int errors = 0;
	
	private Loadable loadable;
	private int[] indexes;
	
	public LoadableTask(Loadable loadable)
	{
		// The loadables indexes
		int[] loadableIndexs = loadable.getIndexes();
		
		// Our Indexs
		this.indexes = new int[loadableIndexs.length];
		
		// Copy the loadables indexes as LoadableTask replaces index values with pass/fail marks
		System.arraycopy(loadableIndexs, 0, indexes, 0, loadableIndexs.length);
		
		this.loadable = loadable;
		
		progressInc = 100f / indexes.length;
	}
	
	@Override
	protected Void doInBackground() throws Exception
	{
		int progress = 0;
		
		setProgress(progress);
		
		for(int i = 0; i < indexes.length; i++)
		{
			if(!loadable.load(indexes[i]))
			{
				// Add failed mark
				indexes[i] = LoadableResult.FAILED_MARK;
				
				errors++;
			}
			else
			{
				// Add Succeeded mark
				indexes[i] = LoadableResult.SUCCEEDED_MARK;
				
				successes++;
			}
			
			progress += progressInc;
			
			setProgress(progress);
		}
		
		return null;
	}
	
	@Override
	public int[] getResult()
	{
		return indexes;
	}
	
	@Override
	public int getTotalSuccesses()
	{
		return successes;
	}
	
	@Override
	public int getTotalFails()
	{
		return errors;
	}
}