package jcompute.gui.component.swing.swingworker;

import java.awt.Component;
import java.io.File;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.cluster.batchmanager.BatchManager;
import jcompute.gui.component.swing.MessageBox;
import jcompute.gui.component.swing.jpanel.JComputeProgressMonitor;

public class OpenBatchFileTask extends SwingWorker<Void, Void>
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(OpenBatchFileTask.class);
	
	private Component parent;
	private BatchManager batchManager;
	private File[] files;
	
	private float progressInc;
	private int loaded = 0;
	private int error = 0;
	
	public OpenBatchFileTask(Component parent, JComputeProgressMonitor openBatchProgressMonitor, BatchManager batchManager, File[] files)
	{
		this.parent = parent;
		
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
			
			setProgress(Math.min(progress, 100));
		}
		
		if(error > 0)
		{
			MessageBox.popup(errorMessage.toString(), parent);
		}
		
		setProgress(100);
		
		return null;
	}
	
	public void start()
	{
		this.execute();
	}
	
	@Override
	public void done()
	{
		log.info(loaded + " Batch Files were loaded");
		log.info(error + " Batch Files were NOT loaded!");
	}
}