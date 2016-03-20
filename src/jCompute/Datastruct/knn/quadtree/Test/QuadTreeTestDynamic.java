package jCompute.Datastruct.knn.quadtree.Test;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;

import jCompute.Cluster.Node.WeightingBenchmark.TreeBenchObject;
import jCompute.Datastruct.knn.KNNPosInf;
import jCompute.Datastruct.knn.KNNResult;
import jCompute.Datastruct.knn.quadtree.RecursiveRegionQuadTree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class QuadTreeTestDynamic
{
	public static int size = 768;
	public static QuadPanel qpanel;
	public static RecursiveRegionQuadTree quadTree = new RecursiveRegionQuadTree(size*0.5f,size*0.5f,size);
	
	public static Semaphore listLock = new Semaphore(1,true);
	public static ArrayList<KNNPosInf> list = new ArrayList<KNNPosInf>();
	
	public static int mouseX = 0;
	public static int mouseY = 0;
	public static boolean mouse1Pressed = false;
	public static boolean mouse3Pressed = false;
	public static JFrame frame;
	
	public static boolean searchEntered = false;
	private static float[] search;
	
	private static float pointRadius = 8f;
	private static float viewRange = 200f;
	
	private static boolean checkCollisions = true;
	
	public static void main(String args[])
	{
		addRandom(100);
		
		System.out.println("Initial Points " + list.size());
		
		search = new float[2];
		
		// Frame
		frame = new JFrame();
		
		qpanel = new QuadPanel(size,pointRadius);
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
					
					list.add(new TreeBenchObject(list.size(), new float[]
					{
						mouseX, mouseY
					}));
					
					listLock.release();
				}
				else if(mouse3Pressed)
				{
					if(!searchEntered)
					{
						search[0] = e.getX();
						search[1] = e.getY();
						
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

					list.add(new TreeBenchObject(list.size(), new float[]
					{
						mouseX, mouseY
					}));
					
					listLock.release();
				}
				else if(e.getButton() == MouseEvent.BUTTON3)
				{
					System.out.println("Button 3");
					
					if(!searchEntered)
					{
						search[0] = e.getX();
						search[1] = e.getY();
						
						searchEntered = true;
					}
					
				}
			}
		});
		
		frame.getContentPane().add(qpanel, BorderLayout.CENTER);
		
		// Size
		frame.setSize(size + 20, size + 40);
		frame.setVisible(true);
		
		// For now
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initThread();
		
	}
	
	/**
	 * Randomly adds a specified number of points in the tree.
	 * if collisions are enable it will take this into account when positioning them.
	 * @param num
	 */
	private static void addRandom(int num)
	{
		listLock.acquireUninterruptibly();

		for(int i=0;i<num;i++)
		{
			boolean overlap = false;
			
			int x = 0;
			int y = 0;
			
			float[] pos;
			
			do
			{
				x = ThreadLocalRandom.current().nextInt(size);
				y = ThreadLocalRandom.current().nextInt(size);
				
				pos = new float[]{x,y};
				
				if(checkCollisions)
				{
					ArrayList<KNNPosInf> overlaps = quadTree.findNearestNeighbours(pos, pointRadius*pointRadius);
					
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
			public void run()
			{
				while(true)
				{
					try
					{
						listLock.acquireUninterruptibly();

						Iterator<KNNPosInf> itr = list.iterator();
						
						while(itr.hasNext())
						{
							KNNPosInf next = itr.next();
							
							boolean collision = false;
							
							if(checkCollisions)
							{
								ArrayList<KNNPosInf> overlaps = quadTree.findNearestNeighbours(next.getKNNPos(), pointRadius*pointRadius);

								if(overlaps !=null)
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
							
							float[][] lines = quadTree.getQuadTreePartitionLines();
							
							qpanel.setLines(lines);
							
							ArrayList<KNNPosInf> allList = quadTree.findNearestNeighbours(search, 10240*10240);
							qpanel.setPoints(allList);
							
							if(searchEntered)
							{
								qpanel.clearSearch();
								
								// System.out.println("Search " +
								// search[0]+"x"+search[1]);
								
								KNNResult result = new KNNResult(null, viewRange*viewRange);
								
								ArrayList<KNNPosInf> nearestNeighbours = new ArrayList<KNNPosInf>();
								
								// This amplifies the performance cost of each search - such that performance changes are more apparent as object numbers increase
								int x=0;
								int max = 0;
								for(int i=0;i<max;i++)
								{
									quadTree.setNearestNeighbour(result, search, viewRange*viewRange);

									nearestNeighbours = quadTree.findNearestNeighbours(search, viewRange*viewRange);
									x+=1;
								}
								
								System.out.println("x"+x + " : "+ quadTree.getPoints());
								
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
									qpanel.setShow1NNResult(result.getPos().getKNNPos());
								}
								
								if(result.getDis() < (pointRadius*pointRadius))
								{
									// System.out.println("Remove" + result.getPos().getKNNPos()[0] + "x" + result.getPos().getKNNPos()[1]);
									
									//KNNPosInf test = new TreeBenchObject(-1, new float[]{0, 0}); 
									
									// Remove from quad tree
									//quadTree.removePoint(test);
									quadTree.removePoint(result.getPos());
									
									// Remove from list or it will be re-added
									// on next loop
									//list.remove(result.getPos());
								}
								
								searchEntered = false;
							}
							
							
							//qpanel.repaint();
							frame.repaint();
							// System.out.println("Points " + list.size());
							

						}
						// Frames per second~
						Thread.sleep(1000/60);
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
