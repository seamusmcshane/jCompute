package jCompute.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

public class Text
{
	/**
	 * Converts the longtime to D/H/M/Sec milliseconds
	 * 
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

	/**
	 * Reads in a text file and converts it to a string.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String textFileToString(String filePath) throws IOException
	{
		StringBuilder destination = new StringBuilder();
		BufferedReader bufferedReader;

		bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "ISO_8859_1"));

		String sCurrentLine;

		while((sCurrentLine = bufferedReader.readLine()) != null)
		{
			destination.append(sCurrentLine);
		}

		bufferedReader.close();

		return destination.toString();
	}
}
