package jCompute.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class Text
{
	/**
	 * Converts the longtime to D/H/M/Sec milliseconds
	 * @param time
	 */
	public static String longTimeToDHMS(long milliseconds)
	{
		milliseconds = milliseconds / 1000; // seconds
		int days = (int) (milliseconds / 86400); // to days
		int hrs = (int) (milliseconds / 3600) % 24; // to hrs
		int mins = (int) ((milliseconds / 60) % 60);	// to seconds
		int sec = (int) (milliseconds % 60);
		
		return String.format("%d:%02d:%02d:%02d", days, hrs, mins, sec);
	}
	
	/**
	 * Converts a string in D:H:M:Sec to a long in Second
	 * @param time
	 */
	public static long stringTimeDHMStoLongMilli(String time)
	{
		long longTime = 0;
		
		// String offsets
		int endIndex = time.indexOf(':');
		int startIndex = 0;
		
		int day_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (86400);
		
		startIndex = endIndex + 1;
		endIndex = time.indexOf(':', startIndex);
		int hour_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (3600);;
		
		startIndex = endIndex + 1;
		endIndex = time.indexOf(':', startIndex);
		int min_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (60);
		
		startIndex = endIndex + 1;
		endIndex = time.length();
		int seconds = Integer.parseInt(time.substring(startIndex, endIndex));
		
		// Seconds to millsecond
		longTime = (day_seconds + hour_seconds + min_seconds + seconds) * 1000;
		
		return longTime;
	}
	
	/**
	 * Converts the longtime to D/H/M/Sec/Mili
	 * @param time
	 */
	public static String longTimeToDHMSM(long milliseconds)
	{
		int msec = (int) (milliseconds % 1000); // miliseconds
		milliseconds = milliseconds / 1000; // seconds
		int days = (int) (milliseconds / 86400); // to days
		int hrs = (int) (milliseconds / 3600) % 24; // to hrs
		int mins = (int) ((milliseconds / 60) % 60);	// to seconds
		int sec = (int) (milliseconds % 60);
		
		return String.format("%d:%02d:%02d:%02d:%03d", days, hrs, mins, sec, msec);
	}
	
	public static String timeNowPlus(long milliseconds)
	{
		String stime = new DecimalFormatSymbols().getInfinity();
		if(milliseconds > 0)
		{
			int seconds = (int) (milliseconds / 1000);
			Calendar time = Calendar.getInstance();
			
			time.add(Calendar.SECOND, seconds);
			stime = time.getTime().toString();
		}
		
		return stime;
	}
	
	public static String timeNow()
	{
		return Calendar.getInstance().getTime().toString();
	}
	
	/**
	 * Reads in a text file and converts it to a string.
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String textFileToString(String filePath)
	{
		StringBuilder destination;
		BufferedReader bufferedReader;
		String text = null;
		
		try
		{
			destination = new StringBuilder();
			
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "ISO_8859_1"));
			
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
}
