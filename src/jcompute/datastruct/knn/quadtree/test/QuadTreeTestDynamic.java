package jcompute.datastruct.knn.quadtree.test;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;

import jcompute.cluster.computenode.weightingbenchmark.TreeBenchObject;
import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.datastruct.knn.KNNResult;
import jcompute.datastruct.knn.quadtree.RecursiveRegionQuadTree;
import jcompute.math.geom.JCVector2f;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class QuadTreeTestDynamic
{
	public static final int WIDTH = 1024;
	public static final int HEIGHT = 1024;
	public static final int SIZE = Math.max(WIDTH, HEIGHT);
	public static QuadPanel qpanel;
	public static RecursiveRegionQuadTree quadTree = new RecursiveRegionQuadTree(WIDTH * 0.5f, HEIGHT * 0.5f, SIZE);
	
	public static Semaphore listLock = new Semaphore(1, true);
	public static ArrayList<KNNFloatPosInf> list = new ArrayList<KNNFloatPosInf>();
	
	public static int mouseX = 0;
	public static int mouseY = 0;
	public static boolean mouse1Pressed = false;
	public static boolean mouse3Pressed = false;
	public static JFrame frame;
	
	public static boolean searchEntered = false;
	private static JCVector2f search;
	
	private static float pointRadius = 20f;
	private static float viewRange = 100f;
	private static float searchRange = viewRange + (pointRadius * 0.5f);
	
	private static boolean checkCollisions = true;
	
	public static void main(String args[])
	{
		addRandom(100);
		
		System.out.println("Initial Points " + list.size());
		
		search = new JCVector2f(0, 0);
		
		// Frame
		frame = new JFrame();
		
		qpanel = new QuadPanel(WIDTH, HEIGHT, pointRadius);
		qpanel.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				mouseX = e.getX();
				mouseY = e.getY();
				
				if(mouse1Pressed)
				{
					listLock.acquireUninterruptibly();
					
					if(list.size() > 2)
					{
						listLock.release();
						
						return;
					}
					
					list.add(new TreeBenchObject(list.size(), mouseX, mouseY));
					
					listLock.release();
				}
				else if(mouse3Pressed)
				{
					if(!searchEntered)
					{
						search.x = e.getX();
						search.y = e.getY();
						
						searchEntered = true;
					}
				}
				
			}
			
			@Override
			public void mouseMoved(MouseEvent e)
			{
				mouseX = e.getX();
				mouseY = e.getY();
			}
		});
		qpanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					mouse1Pressed = true;
				}
				else if(e.getButton() == MouseEvent.BUTTON3)
				{
					mouse3Pressed = true;
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				mouse1Pressed = false;
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(e.getButton() == MouseEvent.BUTTON1)
				{
					listLock.acquireUninterruptibly();
					
					list.add(new TreeBenchObject(list.size(), mouseX, mouseY));
					
					listLock.release();
				}
				else if(e.getButton() == MouseEvent.BUTTON3)
				{
					System.out.println("Button 3");
					
					if(!searchEntered)
					{
						search.x = e.getX();
						search.y = e.getY();
						
						searchEntered = true;
					}
					
				}
			}
		});
		
		frame.getContentPane().add(qpanel, BorderLayout.CENTER);
		
		// Size
		frame.setSize(WIDTH + 20, HEIGHT + 40);
		frame.setVisible(true);
		
		// For now
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initThread();
		
	}
	
	/**
	 * Randomly adds a specified number of points in the tree.
	 * if collisions are enable it will take this into account when positioning them.
	 * 
	 * @param num
	 */
	private static void addRandom(int num)
	{
		listLock.acquireUninterruptibly();
		
		for(int i = 0; i < num; i++)
		{
			boolean overlap = false;
			
			JCVector2f pos = new JCVector2f(0, 0);
			
			do
			{
				pos.x = ThreadLocalRandom.current().nextInt(WIDTH);
				pos.y = ThreadLocalRandom.current().nextInt(HEIGHT);
				
				if(checkCollisions)
				{
					ArrayList<KNNFloatPosInf> overlaps = quadTree.findNearestNeighbours(pos, pointRadius * pointRadius);
					
					if(overlaps.size() > 0)
					{
						overlap = true;
					}
				}
				
			}
			while(overlap);
			
			list.add(new TreeBenchObject(list.size(), pos));
			
		}
		
		listLock.release();
		
	}
	
	private static void initThread()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				while(true)
				{
					try
					{
						listLock.acquireUninterruptibly();
						
						Iterator<KNNFloatPosInf> itr = list.iterator();
						
						while(itr.hasNext())
						{
							KNNFloatPosInf next = itr.next();
							
							boolean collision = false;
							
							if(checkCollisions)
							{
								ArrayList<KNNFloatPosInf> overlaps = quadTree.findNearestNeighbours(next.getXY(), pointRadius * pointRadius);
								
								if(overlaps != null)
								{
									if(overlaps.size() > 0)
									{
										collision = true;
									}
								}
							}
							
							if(!collision)
							{
								quadTree.addPoint(next);
							}
							
							itr.remove();
						}
						
						listLock.release();
						
						if(qpanel != null)
						{
							// Build Quad
							// quadTree = new RegionQuadTree(size, list);
							
							float[][] lines = quadTree.getPartitionLines();
							
							qpanel.setLines(lines);
							
							ArrayList<KNNFloatPosInf> allList = quadTree.findNearestNeighbours(search, 10240 * 10240);
							qpanel.setPoints(allList);
							
							if(searchEntered)
							{
								qpanel.clearSearch();
								
								// System.out.println("Search " +
								// search[0]+"x"+search[1]);
								
								float searchRangeSqr = searchRange * searchRange;
								
								KNNResult result = new KNNResult(null, searchRangeSqr);
								// KNNResult result = new KNNResult(null, viewRange * viewRange);
								
								ArrayList<KNNFloatPosInf> nearestNeighbours = new ArrayList<KNNFloatPosInf>();
								
								// This amplifies the performance cost of each search - such that performance changes are more apparent as object numbers
								// increase
								int x = 0;
								int max = 1;
								
								for(int i = 0; i < max; i++)
								{
									quadTree.setNearestNeighbour(result, search);
									
									nearestNeighbours = quadTree.findNearestNeighbours(search, searchRangeSqr);
									x += 1;
								}
								
								System.out.println("x" + x + " : " + quadTree.getPoints());
								
								// Display the Search point
								qpanel.showSearchPointAndRange(search, viewRange);
								
								// Display the Nearest Neighbours
								if(nearestNeighbours.size() > 0)
								{
									qpanel.setShowKNNResult(nearestNeighbours);
								}
								
								// Display the single nearest neighbour
								if(result.getPos() != null)
								{
									qpanel.setShow1NNResult(result.getPos().getXY());
									
									if(result.getDis() < (pointRadius * pointRadius))
									{
										System.out.println("Remove" + result.getPos());
										
										// KNNPosInf test = new TreeBenchObject(-1, new float[]{0, 0});
										
										// Remove from quad tree
										// quadTree.removePoint(test);
										quadTree.removePoint(result.getPos());
										
										// Remove from list or it will be re-added
										// on next loop
										// list.remove(result.getPos());
									}
								}
								
								searchEntered = false;
							}
							
							// qpanel.repaint();
							frame.repaint();
							// System.out.println("Points " + list.size());
							
						}
						// Frames per second~
						Thread.sleep(1000 / 60);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					
				}
			}
		}.start();
	}
	
}
