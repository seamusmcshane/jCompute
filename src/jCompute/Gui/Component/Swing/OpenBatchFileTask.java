package jCompute.Gui.Component.Swing;

import java.awt.Component;
import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.BatchManager.BatchManager;

public class OpenBatchFileTask extends SwingWorker<Void, Void>
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(OpenBatchFileTask.class);
	
	private Component parent;
	private JComputeProgressMonitor openBatchProgressMonitor;
	private BatchManager batchManager;
	private File[] files;
	
	private float progressInc;
	private int loaded = 0;
	private int error = 0;
	
	public OpenBatchFileTask(Component parent, JComputeProgressMonitor openBatchProgressMonitor, BatchManager batchManager, File[] files)
	{
		this.parent = parent;
		this.openBatchProgressMonitor = openBatchProgressMonitor;
		this.batchManager = batchManager;
		this.files = files;
		
		log.info("Requested that " + files.length + " Batch Files be loaded");
		
		progressInc = 100f / files.length;
	}
	
	@Override
	public Void doInBackground()
	{
		int progress = 0;
		setProgress(progress);
		
		StringBuilder errorMessage = new StringBuilder();
		
		for(File file : files)
		{
			String batchFile = file.getAbsolutePath();
			
			log.info("Batch File : " + batchFile);
			
			if(!batchManager.addBatch(batchFile))
			{
				log.error("Error Creating Batch from : " + batchFile);
				
				if(error == 0)
				{
					errorMessage.append("Error Creating Batch(s) from - \n");
					
				}
				
				errorMessage.append(error + " " + batchFile + "\n");
				
				error++;
			}
			else
			{
				loaded++;
			}
			
			progress += progressInc;
			
			updateProgressMonitor(Math.min(progress, 100));
		}
		
		if(error > 0)
		{
			MessageBox.popup(errorMessage.toString(), parent);
		}
		
		updateProgressMonitor(100);
		
		return null;
	}
	
	private void updateProgressMonitor(int progress)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				openBatchProgressMonitor.setProgress(progress);
			}
		});
	}
	
	public void start()
	{
		updateProgressMonitor(0);
		this.execute();
	}
	
	@Override
	public void done()
	{
		log.info(loaded + " Batch Files were loaded");
		log.info(error + " Batch Files were NOT loaded!");
	}
}