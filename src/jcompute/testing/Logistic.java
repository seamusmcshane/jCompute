package jcompute.testing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;

public class Logistic
{
	private static int frameWidth = 1920 / 2;
	private static int frameHeight = 1080 / 2;
	
	private static JFrame gui;
	
	public static void main(String args[])
	{
		gui = new JFrame();
		
		JPanel renderer = new JPanel()
		{
			private static final long serialVersionUID = 1L;
			
			Color color = new Color(0f, 0f, 0f, 0.01f);
			
			@Override
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D) g;
				
				RenderingHints rhaa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				RenderingHints rhi = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				
				g2.setRenderingHints(rhaa);
				g2.setRenderingHints(rhi);
				
				int width = this.getWidth();
				int height = this.getHeight();
				
				int x1, y1, x2, y2;
				
				double r = 2.4;
				
				double nextr = 0;
				
				double nextx = 0.5, thisx = 0.5;
				
				double yscale = height;
				
				double xmax = width;
				double incrX = 0.00005;
				double incrR = (incrX / xmax) * (4.0 - r);
				
				g.setColor(Color.white);
				
				g.fillRect(0, 0, width, height);
				
				g2.setPaint(color);
				
				for(double x = 0; x < xmax; x += incrX)
				{
					// LMAP
					nextx = (r + nextr) * thisx * (1.0 - thisx);
					
					// Inc R
					nextr = nextr + (incrR);
					
					// Scales
					x1 = (int) x;
					thisx = nextx;
					y1 = (int) (yscale * thisx);
					
					x2 = x1;
					y2 = y1;
					
					g2.drawLine(x1, y1, x2, y2);
				}
				
				System.out.println("Done");
				
			}
		};
		
		renderer.setPreferredSize(new Dimension(frameWidth, frameHeight));
		gui.getContentPane().add(renderer);
		gui.pack();
		gui.setVisible(true);
		
		gui.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				System.exit(0);
			}
		});
	}
}