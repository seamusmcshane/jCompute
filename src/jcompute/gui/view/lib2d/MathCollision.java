package jcompute.gui.view.lib2d;

import jcompute.util.math.FloatingPoint;

public class MathCollision
{
	private MathCollision()
	{
		
	}
	
	public static boolean VectorCollidesWithVector(JCVector2f a, JCVector2f b)
	{
		return a.equals(b);
	}
	
	public static boolean LineInfiniteCollidesWithLineInfinite(JCLineInfinite a, JCLineInfinite b)
	{
		if(MathVector.VectorsAreParallel(a.direction, b.direction))
		{
			return InfiniteLinesAreEquivalent(a, b);
		}
		else
		{
			return true;
		}
	}
	
	public static boolean LineSegmentCollidesWithLineSegment(JCLineSegment a, JCLineSegment b)
	{
		JCLineInfinite axisA = new JCLineInfinite(a.start, MathVector.Subtracted(a.end, a.start));
		
		if(LineSegmentOnOneSide(axisA, b))
		{
			return false;
		}
		
		JCLineInfinite axisB = new JCLineInfinite(b.start, MathVector.Subtracted(b.end, b.start));
		
		if(LineSegmentOnOneSide(axisB, a))
		{
			return false;
		}
		
		if(MathVector.VectorsAreParallel(axisA.direction, axisB.direction))
		{
			JCRange rangeA = ProjectLineSegment(a, axisA.direction);
			JCRange rangeB = ProjectLineSegment(b, axisB.direction);
			
			return RangesOverlap(rangeA, rangeB);
		}
		else
		{
			return true;
		}
		
	}
	
	public static boolean RectangleCollidesWithRectangle(JCRectangle a, JCRectangle b)
	{
		float aLeft = a.origin.x;
		float aRight = aLeft + a.size.x;
		
		float bLeft = b.origin.x;
		float bRight = bLeft + b.size.x;
		
		// 0,0 bottom Left
		float aBottom = a.origin.y;
		float aTop = aBottom + a.size.y;
		
		float bBottom = b.origin.y;
		float bTop = bBottom + b.size.y;
		
		return Overlapping(aLeft, aRight, bLeft, bRight) && Overlapping(aBottom, aTop, bBottom, bTop);
	}
	
	public static boolean CircleCollidesWithCircle(JCCircle a, JCCircle b)
	{
		// Squared
		float radiusSumSqr = (a.radius + b.radius) * (a.radius + b.radius);
		
		// Distance
		JCVector2f distance = a.center.copy();
		distance.sub(b.center);
		
		// Squared
		return distance.lengthSquared() <= radiusSumSqr;
	}
	
	public static boolean CircleCollidesWithVector(JCCircle a, JCVector2f b)
	{
		JCVector2f distance = MathVector.Subtracted(a.center, b);
		
		// Squared
		return distance.lengthSquared() <= (a.radius * a.radius);
	}
	
	public static boolean CircleCollidesWithLineInfinite(JCCircle a, JCLineInfinite b)
	{
		JCVector2f lc = MathVector.Subtracted(a.center, b.base);
		
		JCVector2f projected = MathVector.Projected(lc, b.direction);
		
		JCVector2f nearest = MathVector.Added(b.base, projected);
		
		return CircleCollidesWithVector(a, nearest);
	}
	
	public static boolean CircleCollidesWithLineSegment(JCCircle a, JCLineSegment b)
	{
		if(CircleCollidesWithVector(a, b.start))
		{
			return true;
		}
		
		if(CircleCollidesWithVector(a, b.end))
		{
			return true;
		}
		
		JCVector2f d = MathVector.Subtracted(b.end, b.start);
		
		JCVector2f lc = MathVector.Subtracted(a.center, b.start);
		
		JCVector2f p = MathVector.Projected(lc, d);
		
		JCVector2f nearest = MathVector.Added(b.start, p);
		
		// Squared Len
		return CircleCollidesWithVector(a, nearest) && p.lengthSquared() <= d.lengthSquared() && 0 <= p.dotProduct(d);
	}
	
	public static boolean CircleCollidesWithRectangle(JCCircle a, JCRectangle b)
	{
		JCVector2f clamped = ClampOnRectangle(a.center, b);
		
		return CircleCollidesWithVector(a, clamped);
	}
	
	/*
	 * ***************************************************************************************************
	 * Shared Methods + Helpers
	 *****************************************************************************************************/
	
	private static boolean LineSegmentOnOneSide(JCLineInfinite axis, JCLineSegment segment)
	{
		JCVector2f d1 = MathVector.Subtracted(segment.start, axis.base);
		
		JCVector2f d2 = MathVector.Subtracted(segment.end, axis.base);
		
		JCVector2f n = MathVector.RotatedC90(axis.direction);
		
		return (n.dotProduct(d1) * n.dotProduct(d2)) > 0;
	}
	
	private static JCRange SortRange(JCRange r)
	{
		JCRange sorted = r;
		
		if(r.min > r.max)
		{
			// sorted = r
			float t = r.min;
			
			sorted.min = r.max;
			sorted.max = t;
		}
		
		return sorted;
	}
	
	public static float ClampOnRange(float x, float min, float max)
	{
		if(x < min)
		{
			return min;
		}
		else if(max < x)
		{
			return max;
		}
		else
		{
			return x;
		}
	}
	
	public static JCVector2f ClampOnRectangle(JCVector2f p, JCRectangle r)
	{
		float clx = ClampOnRange(p.x, r.origin.x, r.origin.x + r.size.x);
		float cly = ClampOnRange(p.y, r.origin.y, r.origin.y + r.size.y);
		
		JCVector2f clamp = new JCVector2f(clx, cly);
		
		return clamp;
	}
	
	private static JCRange ProjectLineSegment(JCLineSegment s, JCVector2f onTo)
	{
		JCVector2f ontoUnit = MathVector.Unit(onTo);
		
		JCRange r = new JCRange();
		
		r.min = ontoUnit.dotProduct(s.start);
		r.max = ontoUnit.dotProduct(s.end);
		r = SortRange(r);
		
		return r;
	}
	
	private static boolean RangesOverlap(JCRange a, JCRange b)
	{
		return Overlapping(a.min, a.max, b.min, b.max);
	}
	
	private static boolean InfiniteLinesAreEquivalent(JCLineInfinite a, JCLineInfinite b)
	{
		if(!MathVector.VectorsAreParallel(a.direction, b.direction))
		{
			return false;
		}
		
		JCVector2f d = a.base.copy();
		d.sub(b.base);
		
		return MathVector.VectorsAreParallel(d, a.direction);
	}
	
	private static boolean Overlapping(float minA, float maxA, float minB, float maxB)
	{
		return minB <= maxA && minA <= maxB;
	}
}
