package tools.TimeSeriesAnalysis;

import java.awt.Dimension;

import javax.swing.JFrame;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Insets;

public class PhaseTool implements ChangeListener
{
	final JSlider slider;
	final private PoincarePlotArea poincare;
	private PhasePlotArea phase;
	private JFrame frame;
	private int size = 500;
	
	public PhaseTool(String names[],double xvals[], double yvals[])
	{
		frame = new JFrame(names[0] + " vs " + names[1]);
		frame.setPreferredSize(new Dimension(1600, size));
		frame.setMinimumSize(new Dimension(1600, size));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		phase = new PhasePlotArea(xvals, yvals);
		phase.setPreferredSize(new Dimension(size, size));
		phase.setMinimumSize(new Dimension(size, size));
		
		GridBagConstraints gbc_phase = new GridBagConstraints();
		gbc_phase.insets = new Insets(0, 0, 5, 5);
		gbc_phase.fill = GridBagConstraints.BOTH;
		gbc_phase.gridx = 0;
		gbc_phase.gridy = 0;
		frame.getContentPane().add(phase, gbc_phase);
		poincare = new PoincarePlotArea(0,xvals, yvals);
		
				GridBagConstraints gbc_poincare = new GridBagConstraints();
				gbc_poincare.insets = new Insets(0, 0, 5, 0);
				gbc_poincare.fill = GridBagConstraints.BOTH;
				gbc_poincare.gridx = 1;
				gbc_poincare.gridy = 0;
				frame.getContentPane().add(poincare, gbc_poincare);
				
				
				poincare.setPreferredSize(new Dimension(size, size));
				poincare.setMinimumSize(new Dimension(size, size));
				
				slider = new JSlider();
				slider.addChangeListener(this);
				slider.setPaintLabels(true);
				slider.setMinorTickSpacing(15);
				slider.setMajorTickSpacing(90);
				slider.setSnapToTicks(true);
				slider.setPaintTicks(true);
				slider.setValue(0);
				slider.setMaximum(359);
				slider.setOrientation(1);
				GridBagConstraints gbc_slider = new GridBagConstraints();
				gbc_slider.fill = GridBagConstraints.VERTICAL;
				gbc_slider.insets = new Insets(0, 0, 0, 5);
				gbc_slider.gridx = 2;
				gbc_slider.gridy = 0;
				frame.getContentPane().add(slider, gbc_slider);
		
		
	}
	
	public void display()
	{
		frame.setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if(e.getSource() == slider)
		{
			int value = slider.getValue();
			
			//System.out.println(value);
			poincare.setIntersect(value);
			phase.setAngle(value);
		}


	}
	
}
