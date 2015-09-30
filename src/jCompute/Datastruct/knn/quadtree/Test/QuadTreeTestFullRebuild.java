package jCompute.Datastruct.knn.quadtree.Test;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;

import jCompute.Datastruct.knn.KNNPosInf;
import jCompute.Datastruct.knn.KNNResult;
import jCompute.Datastruct.knn.benchmark.TreeBenchObject;
import jCompute.Datastruct.knn.quadtree.RegionQuadTree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class QuadTreeTestFullRebuild
{
	public static int size = 768;
	public static QuadPanel qpanel;
	public static RegionQuadTree quadTree = new RegionQuadTree(size);
	public static ArrayList<KNNPosInf> list = new ArrayList<KNNPosInf>();
	
	public static int mouseX = 0;
	public static int mouseY = 0;
	public static boolean mouse1Pressed = false;
	public static boolean mouse3Pressed = false;
	public static JFrame frame;
	
	public static boolean searchEntered = false;
	private static float[] search;
	
	public static void main(String args[])
	{
		search = new float[2];
		
		// Frame
		frame = new JFrame();
		
		qpanel = new QuadPanel(size, 10f);
		qpanel.addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				mouseX = e.getX();
				mouseY = e.getY();
				
				if(mouse1Pressed)
				{
					list.add(new TreeBenchObject(list.size(), new float[]
					{
						mouseX, mouseY
					}));
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
					list.add(new TreeBenchObject(list.size(), new float[]
					{
						mouseX, mouseY
					}));
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
						if(qpanel != null)
						{
							quadTree = new RegionQuadTree(size, list);
							
							float[][] lines = quadTree.getQuadTreePartitionLines();
							
							qpanel.setLines(lines);
							qpanel.setPoints(list);
							
							if(searchEntered)
							{
								qpanel.clearSearch();
								
								// System.out.println("Search " +
								// search[0]+"x"+search[1]);
								
								float removeDis = 10f;
								float distance = 100f;
								
								KNNResult result = new KNNResult(null, distance);
								
								quadTree.setNearestNeighbour(result, search, distance);
								ArrayList<KNNPosInf> nearestNeighbours = quadTree.findNearestNeighbours(search, distance);
								
								// Display the Search point
								qpanel.showSearchPointAndRange(search, distance);
								
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
								
								if(result.getDis() < removeDis)
								{
									System.out.println("Remove" + result.getPos().getKNNPos()[0] + "x" + result.getPos().getKNNPos()[1]);
									
									// Remove from quad tree
									// quadTree.removePoint(result.getPos());
									
									// Remove from list or it will be re-added
									// on next loop
									// list.remove(result.getPos());
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
