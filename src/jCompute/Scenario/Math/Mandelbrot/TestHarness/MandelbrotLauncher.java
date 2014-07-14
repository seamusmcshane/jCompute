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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MandelbrotLauncher
{
	private static int[] pallete;

	private static int frameWidth = 1920/2;
	private static int frameHeight = 1005/2;
	
	private static int imageWidth = frameWidth;
	private static int imageHeight = frameHeight;
	
	private final static BufferedImage dest = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	
	private static JFrame frame;
	private static JComponent renderer;

	private static MandelbrotKernelInterface kernel;
	
	private static boolean exiting = false;
	
	private static Timer timer;
	
	private static int Aparapi = 1;
	
	public static void main(String args[])
	{
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) 
			{
				if(timer!=null)
				{
					timer.cancel();
				}
				
				exiting = true;
				
				kernel.destroy();
				
				System.exit(0);
			}
		});
		
		System.out.println("Starting");
		
		/* Palette */
		pallete = MandelbrotPallete.HUEPalete(false);

		if(Aparapi==1)
		{
			kernel = new MandelbrotAparapiKernel(AparapiUtil.chooseOpenCLDevice(), imageWidth, imageHeight);
		}
		else
		{
			kernel = new MandelbrotJavaKernel(imageWidth, imageHeight);	
		}
		
		kernel.setDest(((DataBufferInt) dest.getRaster().getDataBuffer()).getData(),pallete);
		
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				int iterations = MandelbrotConstants.PALETTE_SIZE*6;

				double coords[] =
				{
					-0.16276694186, -1.039998280378, - 0.0810000500525, 0.657500000505005, 0, 1, -0.74595012645, 0.1000001225, 0.34490000104, 0.065252
				};

				int x = 0;
				int y = 1;
				int x2 = 2;
				int y2 = 3;
				
				// Entry from 0,0
				MandelbrotAnimate.animate(kernel,0,0,coords[x], coords[y],iterations);
				
				while (!exiting)
				{
					MandelbrotAnimate.animate(kernel,coords[x], coords[y], coords[x2], coords[y2], iterations);
					x = (x + 2) % coords.length;
					y = (y + 2) % coords.length;
					x2 = (x2 + 2) % coords.length;
					y2 = (y2 + 2) % coords.length;
				}
			}
		});
		thread.start();
		
		renderer = new JPanel()
		{
			private static final long serialVersionUID = 1L;
			
			int margin = 50;
			Font font = new Font("Sans Serif", Font.BOLD, 22);
			
			@Override
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;
			
				g2.setColor(new java.awt.Color(0,0,0));
				g2.fillRect(0, 0,(int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());
								
				// Update Canvas
				if(dest!=null)
				{
			        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.drawImage(dest, 0, 0, renderer.getWidth(), renderer.getHeight(), this);
				}
				
				g.setColor(Color.YELLOW);
				g.setFont(font);
			
				g.drawString(String.valueOf(kernel.getCount()), margin, renderer.getHeight()-margin+20);
				
				if(Aparapi==1)
				{
					g.drawString("Aparapi", margin, renderer.getHeight()-margin+40);
				}
				else
				{
					g.drawString("Java", margin, renderer.getHeight()-margin+40);
				}
			}
		};

		renderer.setPreferredSize(new Dimension(frameWidth, frameHeight));
		frame.getContentPane().add(renderer);
		frame.pack();
		frame.setVisible(true);
		
		timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run() 
			{
				renderer.repaint();
			}
		}, 0,(long) (1000f/60f));
		
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			System.out.println("Error with thread join");
		}

	}
	
}
