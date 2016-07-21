package jcompute.datastruct.knn.quadtree;

import java.util.ArrayList;

import jcompute.datastruct.knn.KNNDataStruct;
import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.datastruct.knn.KNNResult;
import jcompute.math.MathCollision2f;
import jcompute.math.geom.JCVector2f;

/**
 * RegionQuadTree
 * 
 * @author Seamus McShane
 */
public class RecursiveRegionQuadTree implements KNNDataStruct
{
	// Z-Order = 0/1/2/3
	private final static int TL = 0;
	private final static int TR = 1;
	private final static int BL = 2;
	private final static int BR = 3;
	
	private final int MAX_OBJECTS_PER_NODE = 64;
	private final int MAX_LEVEL = 10;
	
	private int level;
	private RegionQuadTreeNode rootNode;
	
	public final JCVector2f treeCenter;
	private final float treeSize;
	
	private int points = 0;
	
	private final boolean debug = false;
	
	/**
	 * Creates an empty Tree
	 * 
	 * @param size
	 */
	public RecursiveRegionQuadTree(float xOffset, float yOffset, float treeSize)
	{
		this.treeSize = treeSize;
		
		level = 0;
		
		treeCenter = new JCVector2f(xOffset, yOffset);
		
		rootNode = new RegionQuadTreeNode(0, new JCVector2f(treeCenter), treeSize, level, MAX_OBJECTS_PER_NODE);
	}
	
	/**
	 * Builds a tree in bulk from a list of objects
	 * 
	 * @param size
	 * @param objects
	 */
	public RecursiveRegionQuadTree(float xOffset, float yOffset, float treeSize, ArrayList<KNNFloatPosInf> objects)
	{
		this.treeSize = treeSize;
		
		level = 0;
		
		treeCenter = new JCVector2f(xOffset, yOffset);
		
		rootNode = new RegionQuadTreeNode(0, new JCVector2f(treeCenter), treeSize, level, MAX_OBJECTS_PER_NODE);
		
		if(debug)
		{
			System.out.println("Level " + level + "Center " + treeCenter.toString());
		}
		
		rootNode = buildTree(objects, new JCVector2f(treeCenter), treeSize, level);
	}
	
	private RegionQuadTreeNode buildTree(ArrayList<KNNFloatPosInf> objects, JCVector2f center, float nodeSize, int level)
	{
		// System.out.println("level " + level + " size " + objects.size());
		
		// New Node
		RegionQuadTreeNode node = new RegionQuadTreeNode(0, center, nodeSize, level, MAX_OBJECTS_PER_NODE);
		
		if(objects.size() <= MAX_OBJECTS_PER_NODE || level == MAX_LEVEL)
		{
			node.setPoints(objects);
			
			if(debug)
			{
				System.out.println("Level " + level + " size " + objects.size());
			}
		}
		else
		{
			RegionQuadTreeNode nodes[] = new RegionQuadTreeNode[4];
			
			// From 0,0 Center (X+/Y+)
			JCVector2f[] centers = getSubdividedCenters(center, node.halfExtend);
			
			ArrayList<KNNFloatPosInf> topLeft = new ArrayList<KNNFloatPosInf>(MAX_OBJECTS_PER_NODE);
			ArrayList<KNNFloatPosInf> topRight = new ArrayList<KNNFloatPosInf>(MAX_OBJECTS_PER_NODE);
			ArrayList<KNNFloatPosInf> bottomRight = new ArrayList<KNNFloatPosInf>(MAX_OBJECTS_PER_NODE);
			ArrayList<KNNFloatPosInf> bottomLeft = new ArrayList<KNNFloatPosInf>(MAX_OBJECTS_PER_NODE);
			
			int numObjects = objects.size();
			
			for(int i = 0; i < numObjects; i++)
			{
				KNNFloatPosInf object = objects.get(i);
				
				JCVector2f point = object.getXY();
				
				int region = determineSubNode(point, node.center);
				
				switch(region)
				{
					case TL:
					{
						// Top Left
						topLeft.add(object);
					}
					break;
					case TR:
					{
						// Top Right
						topRight.add(object);
					}
					break;
					case BR:
					{
						// Bottom Right
						bottomRight.add(object);
					}
					break;
					case BL:
					{
						// Bottom Left
						bottomLeft.add(object);
					}
					break;
				}
			}
			
			// Half size = Quarter area
			float halfSize = nodeSize * 0.5f;
			
			// Top Left
			nodes[TL] = buildTree(topLeft, centers[TL], halfSize, level + 1);
			
			// Top Right
			nodes[TR] = buildTree(topRight, centers[TR], halfSize, level + 1);
			
			// Bottom Right
			nodes[BR] = buildTree(bottomRight, centers[BR], halfSize, level + 1);
			
			// Bottom Left
			nodes[BL] = buildTree(bottomLeft, centers[BL], halfSize, level + 1);
			
			// Set Sub Nodes
			node.setSubNodes(nodes);
		}
		
		return node;
	}
	
	/**
	 * Adds a point into the QuadTree
	 * 
	 * @param point
	 */
	@Override
	public void addPoint(KNNFloatPosInf point)
	{
		// Add a point starting from root node
		addPoint(point, rootNode);
	}
	
	private void addPoint(KNNFloatPosInf object, RegionQuadTreeNode node)
	{
		if(node.isLeaf())
		{
			if(debug)
			{
				System.out.println("Leaf Node");
			}
			
			if(node.canStorePoint() || node.level == MAX_LEVEL)
			{
				if(debug)
				{
					System.out.println("Add Point");
				}
				
				node.addPoint(object);
				
				points++;
			}
			else
			{
				if(debug)
				{
					System.out.println("Split");
				}
				
				// Split Node
				RegionQuadTreeNode nodes[] = new RegionQuadTreeNode[4];
				
				// From 0,0 Center (X+/Y+)
				JCVector2f[] centers = getSubdividedCenters(node.center, node.halfExtend);
				
				int parentIndex = node.nodeIndex;
				int parentLevel = node.level;
				
				int childIndexBase = (int) Math.pow(4, parentLevel) * parentIndex;
				
				// Quarter
				float quaterExtend = node.halfExtend * 0.5f;
				
				// Top Left
				childIndexBase++;
				nodes[TL] = new RegionQuadTreeNode(childIndexBase, centers[TL], quaterExtend, node.level + 1, MAX_OBJECTS_PER_NODE);
				
				// Top Right
				childIndexBase++;
				nodes[TR] = new RegionQuadTreeNode(childIndexBase, centers[TR], quaterExtend, node.level + 1, MAX_OBJECTS_PER_NODE);
				
				// Bottom Right
				childIndexBase++;
				nodes[BR] = new RegionQuadTreeNode(childIndexBase, centers[BR], quaterExtend, node.level + 1, MAX_OBJECTS_PER_NODE);
				
				// Bottom Left
				childIndexBase++;
				nodes[BL] = new RegionQuadTreeNode(childIndexBase, centers[BL], quaterExtend, node.level + 1, MAX_OBJECTS_PER_NODE);
				
				// Link the new nodes
				node.setSubNodes(nodes);
				
				// Get Sub objects
				ArrayList<KNNFloatPosInf> objects = node.removeObjects();
				
				// Add new objects
				objects.add(object);
				
				int numObjects = objects.size();
				
				for(int i = 0; i < numObjects; i++)
				{
					KNNFloatPosInf tObject = objects.get(i);
					
					JCVector2f point = tObject.getXY();
					
					int region = determineSubNode(point, node.center);
					
					addPoint(tObject, nodes[region]);
				}
			}
		}
		else
		{
			if(debug)
			{
				System.out.println("Checking SubNodes");
			}
			
			KNNFloatPosInf tObject = object;
			
			JCVector2f point = object.getXY();
			
			int region = determineSubNode(point, node.center);
			
			addPoint(tObject, node.nodes[region]);
		}
	}
	
	/**
	 * Returns the number of points in the tree
	 * 
	 * @return
	 */
	public int getPoints()
	{
		return points;
	}
	
	/**
	 * Finds all the neighbours with in a search distance (radius).
	 * 
	 * @param point
	 * @param maxDistance
	 * @return
	 */
	@Override
	public ArrayList<KNNFloatPosInf> findNearestNeighbours(JCVector2f point, float maxDistance)
	{
		ArrayList<KNNFloatPosInf> nearestObjects = new ArrayList<KNNFloatPosInf>();
		
		findKNN(nearestObjects, rootNode, point, maxDistance);
		
		return nearestObjects;
	}
	
	private void findKNN(ArrayList<KNNFloatPosInf> result, RegionQuadTreeNode node, JCVector2f point, float maxDistance)
	{
		if(node.isLeaf())
		{
			// Get the objects inside the maxDis in this leaf
			node.getNearestObjects(result, point, maxDistance);
		}
		else
		{
			for(RegionQuadTreeNode subnode : node.nodes)
			{
				float snSize = subnode.halfExtend * 2f;
				if(MathCollision2f.CircleCollidesWithRectangle(point, maxDistance, subnode.center.x - subnode.halfExtend, subnode.center.y - subnode.halfExtend,
				snSize, snSize))
				{
					findKNN(result, subnode, point, maxDistance);
				}
			}
		}
	}
	
	/**
	 * Find the nearest neighbour to point with in maxDistance and sets the
	 * nearest neighbour in a result object.
	 * 
	 * @param result
	 * @param point
	 * @param maxDistance
	 */
	@Override
	public void setNearestNeighbour(KNNResult result, JCVector2f point)
	{
		// Start at the root node
		find1NN(result, rootNode, point);
	}
	
	private void find1NN(KNNResult result, RegionQuadTreeNode node, JCVector2f point)
	{
		if(node.isLeaf())
		{
			if(debug)
			{
				System.out.println("Node is Leaf" + node.isLeaf());
			}
			
			node.setNearestObject(result, point);
		}
		else
		{
			// Nodes with shifted origin to BL,BR.
			for(RegionQuadTreeNode subnode : node.nodes)
			{
				float snSize = subnode.halfExtend * 2;
				if(MathCollision2f.CircleCollidesWithRectangle(point, result.getDis(), subnode.center.x - subnode.halfExtend, subnode.center.y
				- subnode.halfExtend, snSize, snSize))
				{
					find1NN(result, subnode, point);
				}
			}
		}
	}
	
	/**
	 * Remove a point from the tree.
	 * 
	 * @param point
	 */
	public void removePoint(KNNFloatPosInf point)
	{
		removePoint(point, rootNode, 0);
	}
	
	private void removePoint(KNNFloatPosInf searchPoint, RegionQuadTreeNode node, int level)
	{
		if(node.isLeaf())
		{
			if(debug)
			{
				System.out.println("Remove @ " + level);
			}
			
			node.removePoint(searchPoint);
			
			points--;
		}
		else
		{
			JCVector2f point = searchPoint.getXY();
			
			int region = determineSubNode(point, node.center);
			
			System.out.println("Region " + level);
			
			removePoint(searchPoint, node.getSubNodeNode(region), level + 1);
			
			// Only the status is important not the order
			boolean empty1 = node.nodes[0].isEmpty();
			boolean empty2 = node.nodes[1].isEmpty();
			boolean empty3 = node.nodes[2].isEmpty();
			boolean empty4 = node.nodes[3].isEmpty();
			
			boolean isLeaf1 = node.nodes[0].isLeaf();
			boolean isLeaf2 = node.nodes[1].isLeaf();
			boolean isLeaf3 = node.nodes[2].isLeaf();
			boolean isLeaf4 = node.nodes[3].isLeaf();
			
			if(empty1 && empty2 && empty3 && empty4 && isLeaf1 && isLeaf2 && isLeaf3 && isLeaf4)
			{
				node.collapseNode();
			}
		}
	}
	
	/**
	 * Returns the partition lines of the tree as a list of vertices.
	 * 
	 * @return
	 */
	@Override
	public float[][] getPartitionLines()
	{
		ArrayList<float[]> partitions = new ArrayList<float[]>();
		
		if(debug)
		{
			System.out.println("treeCenter " + treeCenter);
			System.out.println("treeSize " + treeSize);
		}
		
		getQuadTreePartitionLines(partitions, rootNode, treeCenter, treeSize);
		
		int partNum = partitions.size();
		
		float[][] partsArray = new float[partNum][];
		
		for(int i = 0; i < partNum; i++)
		{
			partsArray[i] = partitions.get(i);
		}
		
		return partsArray;
	}
	
	private void getQuadTreePartitionLines(ArrayList<float[]> list, RegionQuadTreeNode node, JCVector2f nodeCenter, float size)
	{
		if(node.isLeaf())
		{
			return;
		}
		else
		{
			RegionQuadTreeNode nodes[] = node.nodes;
			
			float[][] lines = getNodeSplitLines(nodeCenter.x, nodeCenter.y, node.halfExtend);
			
			for(int l = 0; l < lines.length; l++)
			{
				list.add(lines[l]);
			}
			// From 0,0 Center (X+/Y+)
			JCVector2f[] centers = getSubdividedCenters(node.center, node.halfExtend);
			
			if(debug)
			{
				System.out.println("SubDivide Node");
				System.out.println("Level " + level + "NoCenter " + nodeCenter);
				System.out.println("Level " + level + "TLCenter " + centers[0]);
				System.out.println("Level " + level + "TRCenter " + centers[1]);
				System.out.println("Level " + level + "BLCenter " + centers[2]);
				System.out.println("Level " + level + "BRCenter " + centers[3]);
			}
			
			int numNodes = nodes.length;
			
			for(int i = 0; i < numNodes; i++)
			{
				getQuadTreePartitionLines(list, nodes[i], centers[i], size);
			}
		}
	}
	
	private float[][] getNodeSplitLines(float cx, float cy, float size)
	{
		float[][] lines = new float[4][];
		
		float halfSize = size * 0.5f;
		
		// Vertical
		lines[0] = new float[]
		{
			cx, cy + halfSize
		};
		lines[1] = new float[]
		{
			cx, cy - halfSize
		};
		
		// Right
		lines[2] = new float[]
		{
			cx - halfSize, cy
		};
		lines[3] = new float[]
		{
			cx + halfSize, cy
		};
		
		return lines;
	}
	
	private static JCVector2f[] getSubdividedCenters(JCVector2f center, float size)
	{
		// From 0,0 Centre (X+/Y+)
		JCVector2f[] centers = new JCVector2f[4];
		
		// Offset to the centres of the new nodes (DIAGONAL)
		float quaterSize = size * 0.25f;
		
		// Top Left
		centers[TL] = new JCVector2f(center.x - quaterSize, center.y + quaterSize);
		
		// Top Right
		centers[TR] = new JCVector2f(center.x + quaterSize, center.y + quaterSize);
		
		// Bottom Right
		centers[BR] = new JCVector2f(center.x + quaterSize, center.y - quaterSize);
		
		// Bottom Left
		centers[BL] = new JCVector2f(center.x - quaterSize, center.y - quaterSize);
		
		return centers;
	}
	
	private static int determineSubNode(JCVector2f point, JCVector2f nodeCenter)
	{
		if(point.x <= nodeCenter.x)
		{
			// LEFT
			if(point.y > nodeCenter.y)
			{
				// TOP
				return TL;
			}
			else
			{
				// BOTTOM
				return BL;
			}
		}
		else
		{
			// RIGHT
			if(point.y > nodeCenter.y)
			{
				// TOP
				return TR;
			}
			else
			{
				// BOTTOM
				return BR;
			}
		}
	}
}
