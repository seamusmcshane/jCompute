package jcompute.util.text;

import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jcompute.util.file.FileUtil;

public class TimeString
{
	public enum TimeStringFormat
	{
		/** Days Hours Minutes Seconds */
		DHMS,
		/** Hours Minutes Seconds */
		HMS,
		/** Minutes Seconds Milliseconds */
		MSM,
		/** Seconds Milliseconds */
		SM
	}
	
	/**
	 * @param milliseconds
	 * @param format
	 * @return
	 */
	@SuppressWarnings("fallthrough")
	public static String timeInMillisAsFormattedString(long milliseconds, TimeStringFormat format)
	{
		switch(format)
		{
			case DHMS:
				return timeInMillisAsFormattedStringDHMS(milliseconds);
			case HMS:
				return timeInMillisAsFormattedStringHMS(milliseconds);
			case MSM:
				return timeInMillisAsFormattedStringMSM(milliseconds);
			/* Deliberate fallthrough */
			case SM:
			default:
				return timeInMillisAsFormattedStringSM(milliseconds);
		}
	}
	
	/**
	 * @param milliseconds
	 * @param format
	 * @return
	 */
	public static long formattedTimeStringToMilliseconds(String time, TimeStringFormat format)
	{
		switch(format)
		{
			case DHMS:
				return formattedTimeStringDHMStoMilli(time);
			case HMS:
				return formattedTimeStringHMStoMilli(time);
			case MSM:
				return formattedTimeStringMSMtoMilli(time);
			/* Deliberate fallthrough */
			case SM:
			default:
				return formattedTimeStringSMtoMilli(time);
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * String Generators and Parsers
	 *****************************************************************************************************/
	
	/**
	 * Given a value representing a time period in milliseconds returns a formated string;
	 *
	 * @param milliseconds
	 * @return
	 * A string formatted as days:hrs:mins:seconds
	 */
	private static String timeInMillisAsFormattedStringDHMS(long milliseconds)
	{
		// int allows the following mod ops use IMOD vs LMOD
		int tSeconds = (int) (milliseconds / 1000L);
		
		return String.format("%d:%02d:%02d:%02d", (tSeconds / 86400), ((tSeconds / 3600) % 24), ((tSeconds / 60) % 60), (tSeconds % 60));
	}
	
	/**
	 * Given a formatted string and converts it to the equivalent time in milliseconds.
	 *
	 * @param time
	 * in the format days:hrs:mins:seconds
	 * @return
	 * Time in milliseconds
	 */
	private static long formattedTimeStringDHMStoMilli(String time)
	{
		// String offsets
		int endIndex = time.indexOf(':');
		int startIndex = 0;
		
		long day_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (86400);
		
		startIndex = endIndex + 1;
		endIndex = time.indexOf(':', startIndex);
		long hour_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (3600);
		
		startIndex = endIndex + 1;
		endIndex = time.indexOf(':', startIndex);
		long min_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (60);
		
		startIndex = endIndex + 1;
		endIndex = time.length();
		long seconds = Integer.parseInt(time.substring(startIndex, endIndex));
		
		// Seconds to milliseconds (*1000L)
		return (day_seconds + hour_seconds + min_seconds + seconds) * 1000L;
	}
	
	/**
	 * Given a value representing a time period in milliseconds returns a formated string;
	 *
	 * @param milliseconds
	 * @return
	 * A string formatted as hrs:mins:seconds
	 */
	private static String timeInMillisAsFormattedStringHMS(long milliseconds)
	{
		// int allows the following mod ops use IMOD vs LMOD
		int tSeconds = (int) (milliseconds / 1000L);
		
		return String.format("%02d:%02d:%02d", ((tSeconds / 3600) % 24), ((tSeconds / 60) % 60), (tSeconds % 60));
	}
	
	/**
	 * Given a formatted string and converts it to the equivalent time in milliseconds.
	 *
	 * @param time
	 * in the format days:hrs:mins:seconds
	 * @return
	 * Time in milliseconds
	 */
	private static long formattedTimeStringHMStoMilli(String time)
	{
		// String offsets
		int endIndex = time.indexOf(':');
		int startIndex = 0;
		
		long hour_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (3600);
		
		startIndex = endIndex + 1;
		endIndex = time.indexOf(':', startIndex);
		long min_seconds = (Integer.parseInt(time.substring(startIndex, endIndex))) * (60);
		
		startIndex = endIndex + 1;
		endIndex = time.length();
		long seconds = Integer.parseInt(time.substring(startIndex, endIndex));
		
		// Seconds to milliseconds (*1000L)
		return (hour_seconds + min_seconds + seconds) * 1000L;
	}
	
	/**
	 * Given a value representing a time period in milliseconds returns a formated string;
	 *
	 * @param milliseconds
	 * @return
	 * A string formatted as mins:seconds:milliseconds
	 */
	private static String timeInMillisAsFormattedStringMSM(long milliseconds)
	{
		// int allows the following mod ops use IMOD vs LMOD
		int tSeconds = (int) (milliseconds / 1000L);
		
		return String.format("%02d:%02d:%03d", (tSeconds / 60) % 60, (tSeconds % 60), (milliseconds % 1000));
	}
	
	/**
	 * Given a formatted string and converts it to the equivalent time in milliseconds.
	 *
	 * @param time
	 * in the format mins:seconds:milliseconds
	 * @return
	 * Time in milliseconds
	 */
	private static long formattedTimeStringMSMtoMilli(String time)
	{
		// String offsets
		int endIndex = time.indexOf(':');
		int startIndex = 0;
		
		long min_millis = (Integer.parseInt(time.substring(startIndex, endIndex))) * (60000L);
		
		startIndex = endIndex + 1;
		endIndex = time.indexOf(':', startIndex);
		long seconds_millis = Integer.parseInt(time.substring(startIndex, endIndex)) * (1000L);
		
		startIndex = endIndex + 1;
		endIndex = time.length();
		long millis = Integer.parseInt(time.substring(startIndex, endIndex));
		
		return(min_millis + seconds_millis + millis);
	}
	
	/**
	 * Given a value representing a time period in milliseconds returns a formated string;
	 *
	 * @param milliseconds
	 * @return
	 * A string formatted as seconds:milliseconds
	 */
	private static String timeInMillisAsFormattedStringSM(long milliseconds)
	{
		return String.format("%02d:%03d", (milliseconds / 1000), (milliseconds % 1000));
	}
	
	/**
	 * Given a formatted string and converts it to the equivalent time in milliseconds.
	 *
	 * @param time
	 * in the format mins:seconds:milliseconds
	 * @return
	 * Time in milliseconds
	 */
	private static long formattedTimeStringSMtoMilli(String time)
	{
		// String offsets
		int endIndex = time.indexOf(':');
		int startIndex = 0;
		
		long seconds_millis = Integer.parseInt(time.substring(startIndex, endIndex)) * (1000L);
		
		startIndex = endIndex + 1;
		endIndex = time.length();
		long millis = Integer.parseInt(time.substring(startIndex, endIndex));
		
		return(seconds_millis + millis);
	}
	
	/*
	 * ***************************************************************************************************
	 * Time Helpers
	 *****************************************************************************************************/
	
	/**
	 * Returns the current date as a string in the default locale format.
	 * 
	 * @param milliseconds
	 * @return
	 */
	public static String timeNow()
	{
		return Calendar.getInstance().getTime().toString();
	}
	
	/**
	 * Returns the date offset by the specified milliseconds as a string in the default locale format.
	 * 
	 * @param milliseconds
	 * @return
	 */
	public static String timeNowPlus(long milliseconds)
	{
		if(milliseconds > 0)
		{
			return longTimeToDateString(System.currentTimeMillis() + milliseconds);
		}
		
		return DecimalFormatSymbols.getInstance().getInfinity();
	}
	
	/**
	 * Returns the date string in the default locale format.
	 * 
	 * @param milliseconds
	 * @return
	 */
	public static String longTimeToDateString(long milliseconds)
	{
		Calendar time = Calendar.getInstance();
		
		time.setTimeInMillis(milliseconds);
		
		return time.getTime().toString();
	}
	
	/**
	 * Returns the date string in a directory sort friendly format that is safe to be used as part of a filename.
	 * 
	 * @param milliseconds
	 * @return
	 */
	public static String longTimeToDateSafeString(long milliseconds)
	{
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTimeInMillis(milliseconds);
		
		String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
		String time = new SimpleDateFormat("HHmm").format(calendar.getTime());
		
		return FileUtil.stringAsValidFileName(date + "_" + time);
	}
}
