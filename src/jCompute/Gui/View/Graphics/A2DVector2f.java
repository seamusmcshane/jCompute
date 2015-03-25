package jCompute.Gui.View.Graphics;

public class A2DVector2f
{
	private final int X_POS = 0;
	private final int Y_POS = 1;
	
	private float pos[];
	
	public A2DVector2f(float x, float y)
	{
		this.pos = new float[2];
		
		this.pos[X_POS] = x;
		this.pos[Y_POS] = y;
	}
	
	public A2DVector2f(float[] pos)
	{
		this.pos = new float[2];
		
		this.pos[X_POS] = pos[X_POS];
		this.pos[Y_POS] = pos[Y_POS];
	}
	
	public A2DVector2f()
	{
		pos = new float[]
		{
			0, 0
		};
	}
	
	public float getX()
	{
		return pos[X_POS];
	}
	
	public void setX(float x)
	{
		pos[X_POS] = x;
	}
	
	public float getY()
	{
		return pos[Y_POS];
	}
	
	public void setY(float y)
	{
		pos[Y_POS] = y;
	}
	
	public void set(A2DVector2f vector)
	{
		pos[X_POS] = vector.getX();
		pos[Y_POS] = vector.getY();
	}
	
	public A2DVector2f sub(A2DVector2f vector)
	{
		pos[X_POS] -= vector.getX();
		pos[Y_POS] -= vector.getY();
		
		return this;
	}
	
	public A2DVector2f add(A2DVector2f vector)
	{
		pos[X_POS] += vector.getX();
		pos[Y_POS] += vector.getY();
		
		return this;
	}
	
	public A2DVector2f add(float x, float y)
	{
		pos[X_POS] += x;
		pos[Y_POS] += y;
		
		return this;
	}
	
	public A2DVector2f add(float theta)
	{
		setTheta(getTheta() + theta);
		
		return this;
	}
	
	/**
	 * Get the angle this vector is at
	 * @author Kevin Glass, Slick2d
	 * @return The angle this vector is at (in degrees)
	 */
	public double getTheta()
	{
		// Y/X
		double theta = StrictMath.toDegrees(StrictMath.atan2(pos[Y_POS], pos[X_POS]));
		if((theta < -360) || (theta > 360))
		{
			theta = theta % 360;
		}
		
		if(theta < 0)
		{
			theta = 360 + theta;
		}
		
		return theta;
	}
	
	/**
	 * Calculate the components of the vectors based on a angle
	 * @author Kevin Glass, Slick2d
	 * @param theta
	 *            The angle to calculate the components from (in degrees)
	 */
	private void setTheta(double theta)
	{
		// Next lines are to prevent numbers like -1.8369701E-16
		// when working with negative numbers
		if((theta < -360) || (theta > 360))
		{
			theta = theta % 360;
		}
		if(theta < 0)
		{
			theta = 360 + theta;
		}
		double oldTheta = getTheta();
		if((theta < -360) || (theta > 360))
		{
			oldTheta = oldTheta % 360;
		}
		if(theta < 0)
		{
			oldTheta = 360 + oldTheta;
		}
		
		float len = length();
		pos[X_POS] = len * (float) cos(StrictMath.toRadians(theta));
		pos[Y_POS] = len * (float) sin(StrictMath.toRadians(theta));
		
	}
	
	/**
	 * Get the cosine of an angle
	 * @author Kevin Glass, Slick2d
	 * @param radians
	 *            The angle
	 * @return The cosine of the angle
	 */
	public static double cos(double radians)
	{
		return sin(radians + Math.PI / 2);
	}
	
	/**
	 * Get the sine of an angle
	 * @author Kevin Glass, Slick2d
	 * @param radians
	 *            The angle
	 * @return The sine of the angle
	 */
	public static double sin(double radians)
	{
		radians = reduceSinAngle(radians); // limits angle to between -PI/2 and
											// +PI/2
		if(Math.abs(radians) <= Math.PI / 4)
		{
			return Math.sin(radians);
		}
		else
		{
			return Math.cos(Math.PI / 2 - radians);
		}
	}
	
	/**
	 * Fast Trig functions for x86.
	 * This forces the trig functiosn to stay within the safe area on the x86
	 * processor (-45 degrees to +45 degrees)
	 * The results may be very slightly off from what the Math and StrictMath
	 * trig functions give due to
	 * rounding in the angle reduction but it will be very very close.
	 * @author Kevin Glass, Slick2d
	 * @param radians
	 *            The original angle
	 * @return The reduced Sin angle
	 */
	private static double reduceSinAngle(double radians)
	{
		radians %= Math.PI * 2.0; // put us in -2PI to +2PI space
		if(Math.abs(radians) > Math.PI)
		{
			// put us in -PI to +PI space
			radians = radians - (Math.PI * 2.0);
		}
		if(Math.abs(radians) > Math.PI / 2)
		{// put us in -PI/2 to +PI/2 space
			radians = Math.PI - radians;
		}
		
		return radians;
	}
	
	public float length()
	{
		return (float) Math.sqrt(lengthSquared());
	}
	
	/**
	 * The length of the vector squared
	 * @author Kevin Glass, Slick2d
	 * @return The length of the vector squared
	 */
	public float lengthSquared()
	{
		return (pos[X_POS] * pos[X_POS]) + (pos[Y_POS] * pos[Y_POS]);
	}
	
	/**
	 * Get the distance from this point to another, squared. This
	 * can sometimes be used in place of distance and avoids the
	 * additional sqrt.
	 * @param other
	 *            The other point we're measuring to
	 * @return The distance to the other point squared
	 */
	public float distanceSquared(A2DVector2f other)
	{
		float dx = other.getX() - getX();
		float dy = other.getY() - getY();
		
		return (dx * dx) + (dy * dy);
	}
	
	/**
	 * Get the distance from this point to another, squared. This
	 * can sometimes be used in place of distance and avoids the
	 * additional sqrt.
	 * @param other
	 *            The other point we're measuring to
	 * @return The distance to the other point squared
	 */
	public float distanceSquared(float[] other)
	{
		float dx = other[0] - pos[X_POS];
		float dy = other[1] - pos[Y_POS];
		
		return (dx * dx) + (dy * dy);
	}
	
	public void set(float x, float y)
	{
		pos[X_POS] = x;
		pos[Y_POS] = y;
	}
	
	public float[] getArray()
	{
		return pos;
	}
	
}
