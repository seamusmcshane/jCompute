package jCompute.Datastruct.knn.quadtree;

import java.util.ArrayList;

import jCompute.Datastruct.knn.KNNPosInf;
import jCompute.Datastruct.knn.KNNResult;
import jCompute.util.JCMath;

/**
 * RegionQuadTree
 * @author Seamus McShane
 */
public class RecursiveRegionQuadTree
{
	private final int MAX_OBJECTS_PER_NODE = 64;
	private final int MAX_LEVEL = 10;

	private int level;
	private RegionQuadTreeNode rootNode;

	private float size;

	private float treeCenterX;
	private float treeCenterY;

	private int points = 0;

	private final boolean debug = false;

	/**
	 * Creates an empty Tree
	 * @param size
	 */
	public RecursiveRegionQuadTree(float xOffset, float yOffset, float size)
	{
		this.size = size;

		level = 0;

		float center[] = new float[2];

		this.treeCenterX = xOffset;
		this.treeCenterY = yOffset;

		center[0] = treeCenterX;
		center[1] = treeCenterY;

		rootNode = new RegionQuadTreeNode(0, center, size, level, MAX_OBJECTS_PER_NODE);
	}

	/**
	 * Builds a tree in bulk from a list of objects
	 * @param size
	 * @param objects
	 */
	public RecursiveRegionQuadTree(float xOffset, float yOffset, float size, ArrayList<KNNPosInf> objects)
	{
		this.size = size;

		level = 0;

		float center[] = new float[2];

		this.treeCenterX = xOffset;
		this.treeCenterY = yOffset;

		center[0] = treeCenterX;
		center[1] = treeCenterY;

		if(debug)
		{
			System.out.println("Level " + level + "Center " + center[0] + "x" + center[1]);
		}

		rootNode = buildTree(objects, center, size, level);
	}

	private RegionQuadTreeNode buildTree(ArrayList<KNNPosInf> objects, float[] center, float size, int level)
	{
		// System.out.println("level " + level + " size " + objects.size());

		// New Node
		RegionQuadTreeNode node = new RegionQuadTreeNode(0, center, size, level, MAX_OBJECTS_PER_NODE);

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

			float halfSize = size * 0.5f;
			float quaterSize = halfSize * 0.5f;

			// From 0,0 Center (X+/Y+)
			float centers[][] = new float[4][2];

			// Top Left
			centers[0][0] = center[0] - quaterSize;
			centers[0][1] = center[1] - quaterSize;

			// Top Right
			centers[1][0] = center[0] + quaterSize;
			centers[1][1] = center[1] - quaterSize;

			// Bottom Left
			centers[2][0] = center[0] - quaterSize;
			centers[2][1] = center[1] + quaterSize;

			// Bottom Right
			centers[3][0] = center[0] + quaterSize;
			centers[3][1] = center[1] + quaterSize;

			if(debug)
			{
				System.out.println("SubDivide Node");
				System.out.println("Level " + level + "NoCenter " + center[0] + "x" + center[1]);
				System.out.println("Level " + level + "TLCenter " + centers[0][0] + "x" + centers[0][1]);
				System.out.println("Level " + level + "TRCenter " + centers[1][0] + "x" + centers[1][1]);
				System.out.println("Level " + level + "BLCenter " + centers[2][0] + "x" + centers[2][1]);
				System.out.println("Level " + level + "BRCenter " + centers[3][0] + "x" + centers[3][1]);
			}

			ArrayList<KNNPosInf> topLeft = new ArrayList<KNNPosInf>(MAX_OBJECTS_PER_NODE);
			ArrayList<KNNPosInf> topRight = new ArrayList<KNNPosInf>(MAX_OBJECTS_PER_NODE);
			ArrayList<KNNPosInf> bottomLeft = new ArrayList<KNNPosInf>(MAX_OBJECTS_PER_NODE);
			ArrayList<KNNPosInf> bottomRight = new ArrayList<KNNPosInf>(MAX_OBJECTS_PER_NODE);

			int numObjects = objects.size();

			for(int i = 0; i < numObjects; i++)
			{
				KNNPosInf object = objects.get(i);

				float point[] = object.getKNNPos();

				if(JCMath.SquareContainsPoint(centers[0][0], centers[0][1], halfSize, point[0], point[1]))
				{
					// Top Left
					topLeft.add(object);
				}
				else if(JCMath.SquareContainsPoint(centers[1][0], centers[1][1], halfSize, point[0], point[1]))
				{
					// Top Right
					topRight.add(object);
				}
				else if(JCMath.SquareContainsPoint(centers[2][0], centers[2][1], halfSize, point[0], point[1]))
				{
					// Bottom Left
					bottomLeft.add(object);
				}
				else if(JCMath.SquareContainsPoint(centers[3][0], centers[3][1], halfSize, point[0], point[1]))
				{
					// Bottom Right
					bottomRight.add(object);
				}
				else
				{
					System.out.println("ERROR " + point[0] + "x" + point[1]);
				}
			}

			// Top Left
			nodes[0] = buildTree(topLeft, centers[0], halfSize, level + 1);

			// Top Right
			nodes[1] = buildTree(topRight, centers[1], halfSize, level + 1);

			// Bottom Left
			nodes[2] = buildTree(bottomLeft, centers[2], halfSize, level + 1);

			// Bottom Right
			nodes[3] = buildTree(bottomRight, centers[3], halfSize, level + 1);

			// Set Sub Nodes
			node.setSubNodes(nodes);
		}

		return node;
	}

	public float getCenterX()
	{
		return treeCenterX;
	}

	public float getCenterY()
	{
		return treeCenterY;
	}

	/**
	 * Adds a point into the QuadTree
	 * @param point
	 */
	public void addPoint(KNNPosInf point)
	{
		// Add a point starting from root node
		addPoint(point, rootNode);
	}

	private void addPoint(KNNPosInf object, RegionQuadTreeNode node)
	{
		if(JCMath.SquareContainsPoint(node.center, node.size, object.getKNNPos()))
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

					float halfSize = node.size * 0.5f;
					float quaterSize = halfSize * 0.5f;

					// From 0,0 Center (X+/Y+)
					float centers[][] = new float[4][2];

					int parentIndex = node.nodeIndex;
					int parentLevel = node.level;

					int childIndexBase = (int) Math.pow(4, parentLevel) * parentIndex;

					// Top Left
					childIndexBase++;
					centers[0][0] = node.center[0] - quaterSize;
					centers[0][1] = node.center[1] - quaterSize;
					nodes[0] = new RegionQuadTreeNode(childIndexBase, centers[0], halfSize, node.level + 1, MAX_OBJECTS_PER_NODE);

					// Top Right
					childIndexBase++;
					centers[1][0] = node.center[0] + quaterSize;
					centers[1][1] = node.center[1] - quaterSize;
					nodes[1] = new RegionQuadTreeNode(childIndexBase, centers[1], halfSize, node.level + 1, MAX_OBJECTS_PER_NODE);

					// Bottom Left
					childIndexBase++;
					centers[2][0] = node.center[0] - quaterSize;
					centers[2][1] = node.center[1] + quaterSize;
					nodes[2] = new RegionQuadTreeNode(childIndexBase, centers[2], halfSize, node.level + 1, MAX_OBJECTS_PER_NODE);

					// Bottom Right
					childIndexBase++;
					centers[3][0] = node.center[0] + quaterSize;
					centers[3][1] = node.center[1] + quaterSize;
					nodes[3] = new RegionQuadTreeNode(childIndexBase, centers[3], halfSize, node.level + 1, MAX_OBJECTS_PER_NODE);

					// Link the new nodes
					node.setSubNodes(nodes);

					// Get Sub objects
					ArrayList<KNNPosInf> objects = node.removeObjects();

					// Add new objects
					objects.add(object);

					int numObjects = objects.size();

					for(int i = 0; i < numObjects; i++)
					{
						KNNPosInf tObject = objects.get(i);

						float point[] = tObject.getKNNPos();

						if(JCMath.SquareContainsPoint(centers[0][0], centers[0][1], halfSize, point[0], point[1]))
						{
							// Top Left
							addPoint(tObject, nodes[0]);
						}
						else if(JCMath.SquareContainsPoint(centers[1][0], centers[1][1], halfSize, point[0], point[1]))
						{
							// Top Right
							addPoint(tObject, nodes[1]);
						}
						else if(JCMath.SquareContainsPoint(centers[2][0], centers[2][1], halfSize, point[0], point[1]))
						{
							// Bottom Left
							addPoint(tObject, nodes[2]);
						}
						else if(JCMath.SquareContainsPoint(centers[3][0], centers[3][1], halfSize, point[0], point[1]))
						{
							// Bottom Right
							addPoint(tObject, nodes[3]);
						}
						else
						{
							System.out.println("ERROR " + point[0] + "x" + point[1]);
						}
					}

				}
			}
			else
			{
				if(debug)
				{
					System.out.println("Checking SubNodes");
				}

				// recurse into sub nodes
				if(JCMath.SquareContainsPoint(node.nodes[0].center, node.nodes[0].size, object.getKNNPos()))
				{
					// Top Left
					addPoint(object, node.nodes[0]);
				}
				else if(JCMath.SquareContainsPoint(node.nodes[1].center, node.nodes[1].size, object.getKNNPos()))
				{
					// Top Right
					addPoint(object, node.nodes[1]);
				}
				else if(JCMath.SquareContainsPoint(node.nodes[2].center, node.nodes[2].size, object.getKNNPos()))
				{
					// Bottom Left
					addPoint(object, node.nodes[2]);
				}
				else if(JCMath.SquareContainsPoint(node.nodes[3].center, node.nodes[3].size, object.getKNNPos()))
				{
					// Bottom Right
					addPoint(object, node.nodes[3]);
				}
				else
				{
					System.out.println("Point not in tree boundary 2");
				}

			}

		}
		else
		{
			System.out.println("Point not in tree boundary 1");
		}
	}

	/**
	 * Returns the number of points in the tree
	 * @return
	 */
	public int getPoints()
	{
		return points;
	}

	/**
	 * Finds all the neighbours with in a search distance (radius).
	 * @param point
	 * @param maxDistance
	 * @return
	 */
	public ArrayList<KNNPosInf> findNearestNeighbours(float[] point, float maxDistance)
	{
		ArrayList<KNNPosInf> nearestObjects = new ArrayList<KNNPosInf>();

		findKNN(nearestObjects, rootNode, point, maxDistance);

		return nearestObjects;
	}

	private void findKNN(ArrayList<KNNPosInf> result, RegionQuadTreeNode node, float[] point, float maxDistance)
	{
		if(node.isLeaf())
		{
			// Get the objects inside the maxDis in this leaf
			node.getNearestObjects(result, point, maxDistance);
		}
		else
		{
			// Top Left
			if(point[0] - maxDistance <= node.center[0] && point[1] - maxDistance <= node.center[1])
			{
				findKNN(result, node.getSubNodeNode(0), point, maxDistance);
			}

			// Top Right
			if(point[0] + maxDistance >= node.center[0] && point[1] - maxDistance <= node.center[1])
			{
				findKNN(result, node.getSubNodeNode(1), point, maxDistance);
			}

			// Bottom Left
			if(point[0] - maxDistance <= node.center[0] && point[1] + maxDistance >= node.center[1])
			{
				findKNN(result, node.getSubNodeNode(2), point, maxDistance);
			}

			// Bottom Right
			if(point[0] + maxDistance >= node.center[0] && point[1] + maxDistance >= node.center[1])
			{
				findKNN(result, node.getSubNodeNode(3), point, maxDistance);
			}
		}
	}

	/**
	 * Find the nearest neighbour to point with in maxDistance and sets the
	 * nearest neighbour in a result object.
	 * @param result
	 * @param point
	 * @param maxDistance
	 */
	public void setNearestNeighbour(KNNResult result, float[] point, float maxDistance)
	{
		// Start at the root node
		find1NN(result, rootNode, point, maxDistance);
	}

	private void find1NN(KNNResult result, RegionQuadTreeNode node, float[] point, float maxDistance)
	{
		if(node.isLeaf())
		{
			if(debug)
			{
				System.out.println("Node is Leaf" + node.isLeaf());
			}

			node.setNearestObject(result, point, maxDistance);
		}
		else
		{
			// Top Left
			if(point[0] - result.getDis() <= node.center[0] && point[1] - result.getDis() <= node.center[1])
			{
				find1NN(result, node.getSubNodeNode(0), point, maxDistance);
			}

			// Top Right
			if(point[0] + result.getDis() >= node.center[0] && point[1] - result.getDis() <= node.center[1])
			{
				find1NN(result, node.getSubNodeNode(1), point, maxDistance);
			}

			// Bottom Left
			if(point[0] - result.getDis() <= node.center[0] && point[1] + result.getDis() >= node.center[1])
			{
				find1NN(result, node.getSubNodeNode(2), point, maxDistance);
			}

			// Bottom Right
			if(point[0] + result.getDis() >= node.center[0] && point[1] + result.getDis() >= node.center[1])
			{
				find1NN(result, node.getSubNodeNode(3), point, maxDistance);
			}
		}
	}

	/**
	 * Remove a point from the tree.
	 * @param point
	 */
	public void removePoint(KNNPosInf point)
	{
		removePoint(point, rootNode, 0);
	}

	private void removePoint(KNNPosInf searchPoint, RegionQuadTreeNode node, int level)
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
			float[] searchPos = searchPoint.getKNNPos();

			// Top Left
			if(searchPos[0] <= node.center[0] && searchPos[1] <= node.center[1])
			{
				if(debug)
				{
					System.out.println("Top Left " + level);
				}

				removePoint(searchPoint, node.getSubNodeNode(0), level + 1);
			}

			// Top Right
			if(searchPos[0] >= node.center[0] && searchPos[1] <= node.center[1])
			{
				if(debug)
				{
					System.out.println("Top Right " + level);
				}
				removePoint(searchPoint, node.getSubNodeNode(1), level + 1);
			}

			// Bottom Left
			if(searchPos[0] <= node.center[0] && searchPos[1] >= node.center[1])
			{
				if(debug)
				{
					System.out.println("Bottom Left " + level);
				}
				removePoint(searchPoint, node.getSubNodeNode(2), level + 1);
			}

			// Bottom Right
			if(searchPos[0] >= node.center[0] && searchPos[1] >= node.center[1])
			{
				if(debug)
				{
					System.out.println("Bottom Right " + level);
				}
				removePoint(searchPoint, node.getSubNodeNode(3), level + 1);
			}

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
	 * @return
	 */
	public float[][] getQuadTreePartitionLines()
	{
		ArrayList<float[]> partitions = new ArrayList<float[]>();

		if(debug)
		{
			System.out.println("treeCenterX " + treeCenterX + " treeCenterY" + treeCenterY);
			System.out.println("size " + size);
		}

		getQuadTreePartitionLines(partitions, rootNode, new float[]
		{
			treeCenterX, treeCenterY
		}, size);

		int partNum = partitions.size();

		float[][] partsArray = new float[partNum][];

		for(int i = 0; i < partNum; i++)
		{
			partsArray[i] = (float[]) partitions.get(i);
		}

		return partsArray;
	}

	/**
	 * Fills an array list with the partition lines of the tree.
	 * @param list
	 * @param node
	 * @param nodeCenter
	 * @param size
	 */
	private void getQuadTreePartitionLines(ArrayList<float[]> list, RegionQuadTreeNode node, float nodeCenter[], float size)
	{
		if(node.isLeaf())
		{
			return;
		}
		else
		{
			RegionQuadTreeNode nodes[] = node.nodes;

			float halfSize = size * 0.5f;
			float quaterSize = halfSize * 0.5f;

			// From 0,0 Center (X+/Y+)
			float centers[][] = new float[4][2];

			// Top Left
			centers[0][0] = nodeCenter[0] - quaterSize;
			centers[0][1] = nodeCenter[1] - quaterSize;

			// Top Right
			centers[1][0] = nodeCenter[0] + quaterSize;
			centers[1][1] = nodeCenter[1] - quaterSize;

			// Bottom Left
			centers[2][0] = nodeCenter[0] - quaterSize;
			centers[2][1] = nodeCenter[1] + quaterSize;

			// Bottom Right
			centers[3][0] = nodeCenter[0] + quaterSize;
			centers[3][1] = nodeCenter[1] + quaterSize;

			if(debug)
			{
				System.out.println("SubDivide Node");
				System.out.println("Level " + level + "NoCenter " + nodeCenter[0] + "x" + nodeCenter[1]);
				System.out.println("Level " + level + "TLCenter " + centers[0][0] + "x" + centers[0][1]);
				System.out.println("Level " + level + "TRCenter " + centers[1][0] + "x" + centers[1][1]);
				System.out.println("Level " + level + "BLCenter " + centers[2][0] + "x" + centers[2][1]);
				System.out.println("Level " + level + "BRCenter " + centers[3][0] + "x" + centers[3][1]);
			}

			int numNodes = nodes.length;

			for(int i = 0; i < numNodes; i++)
			{
				float[][] lines = getSquareToLineVertices(centers[i][0], centers[i][1], halfSize);

				for(int l = 0; l < lines.length; l++)
				{
					list.add(lines[l]);
				}

				getQuadTreePartitionLines(list, nodes[i], centers[i], halfSize);
			}
		}

		getSquareToLineVertices(0, 0, 1);
	}

	/**
	 * Internal method to ease mapping lines to squares.
	 * @param cx
	 * @param cy
	 * @param size
	 * @return
	 */
	private float[][] getSquareToLineVertices(float cx, float cy, float size)
	{
		float[][] lines = new float[8][];

		float hSize = size * 0.5f;

		// Top
		lines[0] = new float[]
		{
			cx - hSize, cy - hSize
		};
		lines[1] = new float[]
		{
			cx + hSize, cy - hSize
		};

		// Right
		lines[2] = new float[]
		{
			cx + hSize, cy - hSize
		};
		lines[3] = new float[]
		{
			cx + hSize, cy + hSize
		};

		// Bottom
		lines[4] = new float[]
		{
			cx - hSize, cy + hSize
		};
		lines[5] = new float[]
		{
			cx - hSize, cy + hSize
		};

		// Left
		lines[6] = new float[]
		{
			cx - hSize, cy + hSize
		};
		lines[7] = new float[]
		{
			cx - hSize, cy - hSize
		};

		return lines;
	}
}
