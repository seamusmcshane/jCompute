package jcompute.math;

import jcompute.math.geom.JCVector2f;
import jcompute.math.trig.JCTrig;
import jcompute.util.JCText;

public class MathVector2fTest
{
	
	public static void main(String[] args)
	{
		init();
		
		add();
		
		sub();
		
		negate();
		
		negated();
		
		equals();
		
		multiply();
		
		divide();
		
		length();
		
		lengthSquared();
		
		unit();
		
		rotate();
		
		dotProduct();
		
		enclosedAngle();
		
		projected();
		
		test();
		
		headingTest();
	}
	
	public static void init()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		// Init
		JCVector2f vector = new JCVector2f(0, 0);
		
		System.out.println("init " + vector);
	}
	
	public static void add()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(3, 5);
		JCVector2f vector2 = new JCVector2f(8, 2);
		vector1.add(vector2);
		System.out.println("add " + vector1);
	}
	
	public static void sub()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(7, 4);
		JCVector2f vector2 = new JCVector2f(3, -3);
		vector1.sub(vector2);
		System.out.println("sub " + vector1);
	}
	
	public static void negate()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(7, 4);
		vector1.negate();
		
		System.out.println("negate " + vector1);
	}
	
	public static void negated()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(7, 4);
		JCVector2f vector2 = MathVector2f.Negated(vector1);
		
		System.out.println("negated " + vector2);
	}
	
	public static void equals()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(3, 5);
		JCVector2f vector2 = new JCVector2f(3, 5);
		JCVector2f vector3 = new JCVector2f(3, 6);
		
		System.out.println("equals " + vector1.equals(vector2));
		System.out.println("!equals " + !vector1.equals(vector3));
	}
	
	public static void multiply()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(3, 5);
		vector1.multiply(3);
		System.out.println("multiply" + vector1);
	}
	
	public static void divide()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(3, 5);
		vector1.divide(3);
		System.out.println("divide" + vector1);
	}
	
	public static void length()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(3, 5);
		System.out.println("length" + vector1.length());
	}
	
	public static void lengthSquared()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(3, 5);
		System.out.println("lengthSquared" + vector1.lengthSquared());
	}
	
	public static void unit()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(3, 5);
		JCVector2f vector2 = MathVector2f.Unit(vector1);
		System.out.println("unit" + vector2);
	}
	
	public static void rotate()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(10, 10);
		
		float steps = 8f;
		float stepSize = 360f / steps;
		
		System.out.println("rotate " + 0 + " " + vector1);
		for(int i = 0; i < steps; i++)
		{
			vector1.rotate(stepSize);
			System.out.println("rotate " + stepSize * (i + 1) + " " + vector1);
		}
		
		System.out.println("vector1 " + vector1);
		System.out.println("rotated C90 " + MathVector2f.RotatedC90(vector1));
		System.out.println("rotated CC90 " + MathVector2f.RotatedCC90(vector1));
		
		System.out.println("vector1 " + vector1);
		vector1.rotateC90();
		System.out.println("rotate C90 " + vector1);
		vector1.rotateCC90();
		System.out.println("rotate(back) CC90 " + vector1);
		vector1.rotateCC90();
		System.out.println("rotate CC90 " + vector1);
	}
	
	public static void dotProduct()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(8, 2);
		JCVector2f vector2 = new JCVector2f(-2, 8);
		
		System.out.println("dotProduct" + vector1.dotProduct(vector2));
	}
	
	public static void enclosedAngle()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(8, 2);
		JCVector2f vector2 = new JCVector2f(-2, 8);
		float angleV1V2 = vector1.enclosedAngle(vector2);
		System.out.println("enclosedAngle " + angleV1V2);
	}
	
	public static void projected()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(12, 5);
		JCVector2f vector2 = new JCVector2f(5, 6);
		JCVector2f vector3 = MathVector2f.Projected(vector2, vector1);
		System.out.println("vector1 " + vector1);
		System.out.println("vector2 " + vector2);
		System.out.println("project " + vector3);
		
	}
	
	public static void test()
	{
		System.out.println(JCText.CharRepeatBounded('-', 80));
		
		JCVector2f vector1 = new JCVector2f(0, 10);
		
		float steps = 8f;
		float stepSize = 360f / steps;
		
		JCVector2f vector2 = new JCVector2f(0, 0);
		
		for(int i = 0; i < steps; i++)
		{
			System.out.println("enclosedAngle " + vector1.enclosedAngle(vector2));
			System.out.println("atan2 " + JCTrig.toDegreesFloat(JCTrig.atan2Float(vector1.y, vector1.x)));
			
			vector1.rotate(stepSize);
			System.out.println("rotate " + stepSize * (i + 1) + " " + vector1);
		}
	}
	
	public static void headingTest()
	{
		JCVector2f origin = new JCVector2f(0.0001f, 0.0001f);
		
		// Right - Polar 0
		JCVector2f vector1 = new JCVector2f(1, 0);
		
		int tab = 40;
		
		System.out.println(JCText.SpacePaddedString("i", tab) + " " + JCText.SpacePaddedString("Vector", tab) + " " + JCText.SpacePaddedString("Location", tab)
		+ " " + JCText.SpacePaddedString("new Location", tab) + " " + JCText.SpacePaddedString("Heading", tab) + " " + JCText.SpacePaddedString(
		"Enclosed Angle", tab));
		
		System.out.println("origin " + origin);
		for(int i = 0; i < 361; i++)
		{
			JCVector2f t = vector1.copy();
			JCVector2f t2 = vector1.copy();
			t2.rotate(i);
			
			// float angle = JCTrig.toDegreesFloat(t2.angleTo(origin));
			float heading = t2.headingTo(origin);
			float angle = t2.enclosedAngle(origin);
			
			System.out.println(JCText.SpacePaddedString(i, tab) + " " + JCText.SpacePaddedString("vector1", tab) + " " + JCText.SpacePaddedString(t.toString(),
			tab) + " " + JCText.SpacePaddedString(t2.toString(), tab) + " " + JCText.SpacePaddedString(heading, tab) + " " + JCText.SpacePaddedString(angle,
			tab));
		}
	}
}
