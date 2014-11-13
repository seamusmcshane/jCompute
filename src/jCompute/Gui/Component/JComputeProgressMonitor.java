package jCompute.Gui.Component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class JComputeProgressMonitor
{
	private final int width = 300;
	private final int height = 50;
	private JFrame frame;
	private JProgressBar progressBar;
	private int max;
	private Component comp;
	
	public JComputeProgressMonitor(Component comp, String title, int min,int max)
	{
		this.comp = comp;
		this.max = max;
		
		frame = new JFrame(title);
		frame.setType(JFrame.Type.UTILITY);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setPreferredSize(new Dimension(width,height));
		frame.setMinimumSize(new Dimension(width,height));
		frame.setResizable(false);
		frame.getContentPane().setLayout(new BorderLayout());
		progressBar = new JProgressBar(min,max);
		progressBar.setStringPainted(true);
		frame.add(progressBar,BorderLayout.CENTER);
		frame.setAlwaysOnTop(true);
		frame.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
		
		if(comp!=null)
		{
			frame.setLocationRelativeTo(comp);
		}	
	}
	
	public void setProgress(int progress)
	{
		this.progressBar.setValue(progress);

		if(progress==0)
		{
			frame.setVisible(true);		
		}
				
		if(progress>=max)
		{
			frame.setVisible(false);
		}
	}
	
}
