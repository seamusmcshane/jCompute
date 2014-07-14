package jCompute.util;

public class Text
{
	/**
	 * Converts the longtime to D/H/M/Sec
	 * milliseconds
	 * @param time
	 */
	public static String longTimeToDHMS(long time)
	{
		time = time / 1000; // seconds
		int days = (int) (time / 86400); // to days
		int hrs = (int) (time / 3600) % 24; // to hrs
		int mins = (int) ((time / 60) % 60);	// to seconds
		int sec = (int) (time % 60);

		return String.format("%d:%02d:%02d:%02d", days, hrs, mins, sec);	
	}
	
}
