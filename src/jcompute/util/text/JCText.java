package jcompute.util.text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import jcompute.math.JCMath;

public class JCText
{
	/**
	 * Reads in a text file and converts it to a string.
	 *
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String textFileToString(String filePath)
	{
		String text = null;
		
		try
		{
			StringBuilder destination = new StringBuilder();
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "ISO_8859_1"));
			
			String sCurrentLine;
			
			while((sCurrentLine = bufferedReader.readLine()) != null)
			{
				destination.append(sCurrentLine);
			}
			
			bufferedReader.close();
			
			text = destination.toString();
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return text;
	}
	
	public static String stackTraceToString(StackTraceElement[] elements, boolean html)
	{
		String lineEnd;
		
		if(html)
		{
			lineEnd = "<br>";
		}
		else
		{
			lineEnd = "\n";
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(StackTraceElement element : elements)
		{
			sb.append(element.toString());
			sb.append(lineEnd);
		}
		
		return sb.toString();
	}
	
	public static String SpacePaddedString(String text, int maxSize)
	{
		return String.format("%" + maxSize + "s", text);
	}
	
	public static String SpacePaddedString(float f, int maxSize)
	{
		return SpacePaddedString(String.valueOf(f), maxSize);
	}
	
	public static String SpacePaddedString(double d, int maxSize)
	{
		return SpacePaddedString(String.valueOf(d), maxSize);
	}
	
	public static String ZeroPaddedValue(long value, int maxSize)
	{
		return String.format("%0" + maxSize + "d", value);
	}
	
	public static String CharRepeatBounded(char c, int repeat)
	{
		// Catch negatives and roll overs
		int chars = JCMath.absModBase2(repeat, 256);
		
		// Don't repeat ok..
		if(chars == 0)
		{
			return "";
		}
		
		char[] text = new char[chars];
		
		Arrays.fill(text, c);
		
		return new String(text);
	}
}
