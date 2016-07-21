package jcompute.datastruct.knn.quadtree.test;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;

import jcompute.cluster.computenode.weightingbenchmark.TreeBenchObject;
import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.datastruct.knn.KNNResult;
import jcompute.datastruct.knn.quadtree.RecursiveRegionQuadTree;
import jcompute.math.geom.JCVector2f;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class QuadTreeTestFullRebuild
{
	public static int size = 768;
	public static QuadPanel qpanel;
	public static RecursiveRegionQuadTree quadTree = new RecursiveRegionQuadTree(size * 0.5f, size * 0.5f, size);
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
	
	public static void main(String args[])
	{
		search = new JCVector2f(0, 0);
		
		// Frame
		frame = new JFrame();
		
		qpanel = new QuadPanel(size, size, pointRadius);
		qpanel.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				mouseX = e.getX();
				mouseY = e.getY();
				
				if(mouse1Pressed)
				{
					list.add(new TreeBenchObject(list.size(), mouseX, mouseY));
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
					list.add(new TreeBenchObject(list.size(), mouseX, mouseY));
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
		frame.setSize(size + 20, size + 40);
		frame.setVisible(true);
		
		// For now
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initThread();
		
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
						if(qpanel != null)
						{
							quadTree = new RecursiveRegionQuadTree(size * 0.5f, size * 0.5f, size, list);
							
							float[][] lines = quadTree.getPartitionLines();
							
							qpanel.setLines(lines);
							qpanel.setPoints(list);
							
							if(searchEntered)
							{
								qpanel.clearSearch();
								
								// System.out.println("Search " +
								// search[0]+"x"+search[1]);
								
								float searchRangeSqr = searchRange * searchRange;
								
								KNNResult result = new KNNResult(null, searchRangeSqr);
								
								quadTree.setNearestNeighbour(result, search);
								ArrayList<KNNFloatPosInf> nearestNeighbours = quadTree.findNearestNeighbours(search, searchRangeSqr);
								
								// Display the Search point
								qpanel.showSearchPointAndRange(search, viewRange);
								
								// Display the Nearest Neighbours
								if(nearestNeighbours.size() > 0)
								{
									qpanel.setShowKNNResult(nearestNeighbours);
									
									if(result.getDis() < (pointRadius * pointRadius))
									{
										System.out.println("Remove" + result.getPos().getXY());
										
										// Remove from quad tree
										// quadTree.removePoint(result.getPos());
										
										// Remove from list or it will be re-added
										// on next loop
										// list.remove(result.getPos());
									}
								}
								
								// Display the single nearest neighbour
								if(result.getPos() != null)
								{
									qpanel.setShow1NNResult(result.getPos().getXY());
								}
								
								searchEntered = false;
							}
							
							// System.out.println("Points " + list.size());
							
							qpanel.repaint();
							frame.repaint();
						}
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
