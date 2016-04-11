package jcompute.gui.view.lib3d;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class MeshHelper
{
	private static final int COLOR_WIDTH = 4;
	private static final int POS_WIDTH = 3;

	/**
	 * Converts Color Packed Vertices to an indexed mesh
	 * @param vert
	 * @return
	 */
	public static Mesh verticesToMesh(float[] vert)
	{
		/*
		 * System.out.println("verticesToMesh vertices " + vert.length);
		 * System.out.println("verticesToMesh points " + vert.length/7);
		 * System.out.println("verticesToMesh indicies " + (vert.length/7));
		 */

		short iLen = (short) (vert.length / (POS_WIDTH + COLOR_WIDTH));
		short[] indicies = new short[iLen];

		for(short i = 0; i < iLen; i++)
		{
			indicies[i] = i;
		}

		Mesh mesh = new Mesh(true, vert.length, iLen, new VertexAttribute(Usage.Position, POS_WIDTH, "a_position"),
				new VertexAttribute(Usage.ColorUnpacked, COLOR_WIDTH, "a_color"));
		mesh.setVertices(vert);
		mesh.setIndices(indicies);

		return mesh;
	}

	/**
	 * Shades all vertices the same color
	 * @param vertices
	 * @param rgba
	 * @return
	 */
	public static float[] colorAllVertices(float[] inVertices, float[] rgba)
	{
		int indicies = inVertices.length / POS_WIDTH;

		int vLen = inVertices.length;

		float[] colorVertices = new float[indicies * (POS_WIDTH + COLOR_WIDTH)];

		int point = 0;
		for(int i = 0; i < vLen; i += 3)
		{
			colorVertices[point] = inVertices[i];
			colorVertices[point + 1] = inVertices[i + 1];
			colorVertices[point + 2] = inVertices[i + 2];

			colorVertices[point + 3] = rgba[0];
			colorVertices[point + 4] = rgba[1];
			colorVertices[point + 5] = rgba[2];
			colorVertices[point + 6] = rgba[3];

			point += 7;
		}

		return colorVertices;
	}

	/**
	 * Shades all vertices in RGB based on the value of xyz within <min-max> and
	 * the order of the axis (xyz,yzx,zyx etc..).
	 * @param inVertices
	 * @param colorBoost
	 * @param alpha
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param zMin
	 * @param zMax
	 * @param scaleHalf
	 * @param redAxis
	 * @param greenAxis
	 * @param blueAxis
	 * @return
	 */
	public static float[] colorAllVerticesRGBA(float[] inVertices, float colorBoost, float alpha, float xMin,
			float xMax, float yMin, float yMax, float zMin, float zMax, float scaleHalf, int redAxis, int greenAxis,
			int blueAxis)
	{
		int indicies = inVertices.length / POS_WIDTH;
		float[] colorVertices = new float[indicies * (POS_WIDTH + COLOR_WIDTH)];
		int vLen = inVertices.length;

		if(colorBoost > 1f)
		{
			colorBoost = 1f;
		}

		if(colorBoost < 0)
		{
			colorBoost = 0;
		}

		float[] colorsScales = new float[3];

		colorsScales[redAxis] = (1f - colorBoost) / (xMax - xMin);
		colorsScales[greenAxis] = (1f - colorBoost) / (yMax - yMin);
		colorsScales[blueAxis] = (1f - colorBoost) / (zMax - zMin);

		int point = 0;
		for(int i = 0; i < vLen; i += 3)
		{
			colorVertices[point] = inVertices[i];
			colorVertices[point + 1] = inVertices[i + 1];
			colorVertices[point + 2] = inVertices[i + 2] + scaleHalf;

			float[] rgb = new float[3];
			rgb[0] = (1f * (colorsScales[redAxis] * inVertices[i])) + colorBoost;
			rgb[1] = (1f * (colorsScales[greenAxis] * inVertices[i + 1])) + colorBoost;
			rgb[2] = (1f * (colorsScales[blueAxis] * inVertices[i + 2])) + colorBoost;

			colorVertices[point + 3] = rgb[redAxis];
			colorVertices[point + 4] = rgb[greenAxis];
			colorVertices[point + 5] = rgb[blueAxis];
			colorVertices[point + 6] = alpha;

			point += 7;
		}

		return colorVertices;
	}

}
