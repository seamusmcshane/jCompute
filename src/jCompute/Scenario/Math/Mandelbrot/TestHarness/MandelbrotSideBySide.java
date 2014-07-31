package jCompute.Scenario.Math.Mandelbrot.TestHarness;
import jCompute.Scenario.Math.Mandelbrot.Lib.AparapiUtil;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotAnimate;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotAparapiKernel;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotConstants;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotJavaKernel;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotKernelInterface;
import jCompute.Scenario.Math.Mandelbrot.Lib.MandelbrotPallete;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class MandelbrotSideBySide
{
	private static int[] pallete;

	private static int frameWidth = 1920;
	private static int frameHeight = 1005;
	
	private static int imageWidth = frameWidth/2;
	private static int imageHeight = frameHeight;
	
	private final static BufferedImage dest1 = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	private final static BufferedImage dest2 = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

	private static JFrame frame;
	private static JComponent renderer;

	private static MandelbrotKernelInterface kernel1;
	private static MandelbrotKernelInterface kernel2;
	
	private static boolean exiting=false;
	
	public static void main(String args[])
	{
		frame = new JFrame();

		System.out.println("Starting");
		
		/* Palette */
		pallete = MandelbrotPallete.HUEPalete(false);

		kernel1 = new MandelbrotJavaKernel(imageWidth, imageHeight);
		//kernel1 = new MandelbrotAparapiKernel(AparapiUtil.selectDevByVendorAndType("INTEL","CPU"), imageWidth, imageHeight);
		kernel1.setDest(((DataBufferInt) dest1.getRaster().getDataBuffer()).getData(),pallete);
		
		kernel2 = new MandelbrotAparapiKernel(AparapiUtil.selectDevByVendorAndType("NVIDIA","GPU"), imageWidth, imageHeight);
		kernel2.setDest(((DataBufferInt) dest2.getRaster().getDataBuffer()).getData(),pallete);
		
		Thread thread1 = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				int iterations = MandelbrotConstants.PALETTE_SIZE*8;

				double coords[] =
				{
					-0.16276694186, -1.039998280378, - 0.0810000500525, 0.657500000505005, 0, 1, -0.74595012645, 0.1000001225, 0.34490000104, 0.065252
				};

				int x = 0;
				int y = 1;
				int x2 = 2;
				int y2 = 3;
				
				// Entry
				MandelbrotAnimate.animate(kernel1,0,0,coords[x], coords[y],iterations);
				
				while (!exiting)
				{
					MandelbrotAnimate.animate(kernel1,coords[x], coords[y], coords[x2], coords[y2], iterations);
					
					x = (x + 2) % coords.length;
					y = (y + 2) % coords.length;
					x2 = (x2 + 2) % coords.length;
					y2 = (y2 + 2) % coords.length;					
				}
			}
		});
		
		Thread thread2 = new Thread(new Runnable()
		{

			@Override
			public void run() 
			{
				int iterations = MandelbrotConstants.PALETTE_SIZE*8;

				double coords[] =
				{
					-0.16276694186, -1.039998280378, - 0.0810000500525, 0.657500000505005, 0, 1, -0.74595012645, 0.1000001225, 0.34490000104, 0.065252
				};

				int x = 0;
				int y = 1;
				int x2 = 2;
				int y2 = 3;
				
				// Entry
				MandelbrotAnimate.animate(kernel2,0,0,coords[x], coords[y],iterations);
				
				while (!exiting)
				{
					MandelbrotAnimate.animate(kernel2,coords[x], coords[y], coords[x2], coords[y2], iterations);

					x = (x + 2) % coords.length;
					y = (y + 2) % coords.length;
					x2 = (x2 + 2) % coords.length;
					y2 = (y2 + 2) % coords.length;					
				}
			}
		});
		thread1.start();
		thread2.start();
		
		renderer = new JComponent()
		{
			private static final long serialVersionUID = 2248560884884539855L;
			int margin = 50;
			Font font = new Font("Sans Serif", Font.BOLD, 22);
			
			@Override
			public void paintComponent(Graphics g)
			{
				g.drawImage(dest1, 0, 0, imageWidth, imageHeight, this);
				g.drawImage(dest2, imageWidth, 0, imageWidth, imageHeight, this);
				
				g.setColor(Color.YELLOW);
				g.setFont(font);
				g.drawString(kernel1.getComputeMethodString(), margin, imageHeight-margin);
				g.drawString(kernel2.getComputeMethodString(), imageWidth+margin, imageHeight-margin);
				
				g.drawString(String.valueOf(kernel1.getCount()), margin, imageHeight-margin+20);
				g.drawString(String.valueOf(kernel2.getCount()), imageWidth+margin, imageHeight-margin+20);
				
				g.drawString(String.valueOf((float)kernel1.getCount()/(float)kernel2.getCount()), margin, imageHeight-margin+40);
				g.drawString(String.valueOf((float)kernel2.getCount()/(float)kernel1.getCount()), imageWidth+margin, imageHeight-margin+40);
			}
		};

		renderer.setPreferredSize(new Dimension(frameWidth, frameHeight));
		frame.add(renderer);
		frame.pack();
		frame.setVisible(true);
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run() 
			{
				renderer.repaint();
			}
		}, 0,(long) (1000f/60f));
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) 
			{
				if(timer!=null)
				{
					timer.cancel();
				}
				
				exiting = true;
				
				kernel2.destroy();
				
				System.exit(0);
			}
		});
		
		try
		{
			thread1.join();
			thread2.join();
		}
		catch (InterruptedException e)
		{
			System.out.println("Error with thread join");
		}

	}
	
}
