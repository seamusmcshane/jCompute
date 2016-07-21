package jcompute.math;

import jcompute.math.geom.JCCircle;
import jcompute.math.geom.JCLineInfinite;
import jcompute.math.geom.JCLineSegment;
import jcompute.math.geom.JCRectangle;
import jcompute.math.geom.JCVector2f;

public class MathCollision2fTest
{
	public static void main(String args[])
	{
		JCRectangleCollisions();
		JCCircleCollisions();
		JCVector2fCollisions();
		JCLineInfiniteCollisions();
		JCLineSegmentCollisions();
		JCCircleCollidesWithVector();
		JCCircleCollidesWithJCLineInfinite();
		JCCircleCollidesWithJCLineSegment();
		JCCircleCollidesWithRectangle();
	}
	
	public static void JCRectangleCollisions()
	{
		System.out.println("JCRectangle");
		
		JCRectangle a = new JCRectangle(1, 1, 4, 4);
		JCRectangle b = new JCRectangle(2, 2, 5, 5);
		JCRectangle c = new JCRectangle(6, 4, 4, 2);
		JCRectangle d = new JCRectangle(5.0000001f, 5.0000001f, 4, 4);
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		
		System.out.println("(true)  a with b " + MathCollision2f.RectangleCollidesWithRectangle(a, b));
		System.out.println("(true)  b with c " + MathCollision2f.RectangleCollidesWithRectangle(b, c));
		System.out.println("(false) a with c " + MathCollision2f.RectangleCollidesWithRectangle(a, c));
		System.out.println("(true)  a with d " + MathCollision2f.RectangleCollidesWithRectangle(a, d));
	}
	
	public static void JCCircleCollisions()
	{
		System.out.println("JCCircle");
		
		JCCircle a = new JCCircle(4, 4, 2);
		JCCircle b = new JCCircle(7, 4, 2);
		JCCircle c = new JCCircle(10, 4, 2);
		JCCircle d = new JCCircle(8.000001f, 4, 2);
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		
		System.out.println("(true)  a with b " + MathCollision2f.CircleCollidesWithCircle(a, b));
		System.out.println("(true)  b with c " + MathCollision2f.CircleCollidesWithCircle(b, c));
		System.out.println("(false) a with c " + MathCollision2f.CircleCollidesWithCircle(a, c));
		System.out.println("(false) a with d " + MathCollision2f.CircleCollidesWithCircle(a, d));
	}
	
	public static void JCVector2fCollisions()
	{
		System.out.println("JCVector2f");
		
		JCVector2f a = new JCVector2f(2, 3);
		JCVector2f b = new JCVector2f(2, 3);
		JCVector2f c = new JCVector2f(3, 4);
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		
		System.out.println("(true)  a with b " + MathCollision2f.VectorCollidesWithVector(a, b));
		System.out.println("(true)  b with c " + MathCollision2f.VectorCollidesWithVector(b, c));
		System.out.println("(false) a with c " + MathCollision2f.VectorCollidesWithVector(a, c));
		System.out.println("(false) b with c " + MathCollision2f.VectorCollidesWithVector(b, c));
	}
	
	public static void JCLineInfiniteCollisions()
	{
		System.out.println("JCLineInfinite");
		
		JCLineInfinite a = new JCLineInfinite(3, 5, 5, -1);
		JCLineInfinite b = new JCLineInfinite(3, 5, 5, 2);
		JCLineInfinite c = new JCLineInfinite(3, 2, 5, 2);
		JCLineInfinite d = new JCLineInfinite(8, 4, 5, -1);
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		
		System.out.println("(true)  a with b " + MathCollision2f.LineInfiniteCollidesWithLineInfinite(a, b));
		System.out.println("(true)  a with c " + MathCollision2f.LineInfiniteCollidesWithLineInfinite(a, c));
		System.out.println("(false) b with c " + MathCollision2f.LineInfiniteCollidesWithLineInfinite(b, c));
		System.out.println("(true) a with d " + MathCollision2f.LineInfiniteCollidesWithLineInfinite(a, d));
	}
	
	public static void JCLineSegmentCollisions()
	{
		System.out.println("JCLineInfinite");
		
		JCLineSegment a = new JCLineSegment(3, 4, 11, 1);
		JCLineSegment b = new JCLineSegment(8, 4, 11, 7);
		
		System.out.println(a);
		System.out.println(b);
		
		System.out.println("(false)  a with b " + MathCollision2f.LineSegmentCollidesWithLineSegment(a, b));
	}
	
	public static void JCCircleCollidesWithVector()
	{
		System.out.println("JCCircleCollidesWithVector");
		
		JCCircle a = new JCCircle(6, 4, 3);
		JCVector2f b = new JCVector2f(8, 3);
		JCVector2f c = new JCVector2f(11, 7);
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		
		System.out.println("(true)  a with b " + MathCollision2f.CircleCollidesWithVector(a, b));
		System.out.println("(false)  a with c " + MathCollision2f.CircleCollidesWithVector(a, c));
	}
	
	public static void JCCircleCollidesWithJCLineInfinite()
	{
		System.out.println("JCCircleCollidesWithJCLineInfinite");
		
		JCCircle a = new JCCircle(6, 3, 2);
		JCLineInfinite b = new JCLineInfinite(4, 7, 5, -1);
		
		System.out.println(a);
		System.out.println(b);
		
		System.out.println("(false)  a with b " + MathCollision2f.CircleCollidesWithLineInfinite(a, b));
	}
	
	public static void JCCircleCollidesWithJCLineSegment()
	{
		System.out.println("JCCircleCollidesWithJCLineSegment");
		
		JCCircle a = new JCCircle(4, 4, 3);
		JCLineSegment b = new JCLineSegment(8, 6, 13, 6);
		JCLineSegment c = new JCLineSegment(4.5f, 4.5f, 5f, 5f);
		JCLineSegment d = new JCLineSegment(0.9f, 0.9f, 7.1f, 7.1f);
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		
		System.out.println("(false)  a with b " + MathCollision2f.CircleCollidesWithLineSegment(a, b));
		System.out.println("(true)  a with c " + MathCollision2f.CircleCollidesWithLineSegment(a, c));
		System.out.println("(true)  a with d " + MathCollision2f.CircleCollidesWithLineSegment(a, d));
	}
	
	public static void JCCircleCollidesWithRectangle()
	{
		System.out.println("JCCircleCollidesWithRectangle");
		
		JCCircle a = new JCCircle(5, 4, 1);
		JCCircle b = new JCCircle(7, 8, 1);
		JCRectangle c = new JCRectangle(3, 2, 6, 4);
		
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		
		System.out.println("(true)  a with b " + MathCollision2f.CircleCollidesWithRectangle(a, c));
		System.out.println("(false)  a with c " + MathCollision2f.CircleCollidesWithRectangle(b, c));
	}
}
