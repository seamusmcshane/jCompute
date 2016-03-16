package jCompute.util;

public final class NumericConstants
{
	/*
	 * ***************************************************************************************************
	 * SI/Metric - (Base 10) - Disk Based Storage and Information Transfer
	 *****************************************************************************************************/
	public enum SI
	{
		KILO(0, "Kilo", "K"), MEGA(1, "Mega", "M"), GIGA(2, "Giga", "G"), TERA(3, "Terra", "T");
		
		public final String prefix;
		public final String symbol;
		public final long value;
		
		private SI(int power, String prefix, String symbol)
		{
			this.value = (long) Math.pow(1000, power);
			this.prefix = prefix;
			this.symbol = symbol;
		}
	}
	
	// Herts * SI Unit
	public final static long SI_HERTZ = 1;
	
	/*
	 * ***************************************************************************************************
	 * IEC Standard Binary Prefixes (Base 2)
	 *****************************************************************************************************/
	public enum IEC
	{
		KIBI(0, "Kibi", "Ki"), MEBI(1, "Mebi", "Mi"), GIBI(2, "Gibi", "Gi"), TEBI(3, "Tebi", "Ti");
		
		public final String prefix;
		public final String symbol;
		public final long value;
		
		private IEC(int power, String prefix, String symbol)
		{
			this.value = (long) Math.pow(1024, power);
			this.prefix = prefix;
			this.symbol = symbol;
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * JDEC Standard (Base 2) - Chip Based Storage and Serial Transfer Rate
	 *****************************************************************************************************/
	
	public enum JDEC
	{
		BYTE(0, 1, "Byte", "B"), KILOBYTE(1, 1, "KiloByte", "KB"), MEGABYTE(2, 1, "MegaByte", "MB"), GIGABYTE(3, 1, "GigaByte", "GB"),
		BIT(0, 8, "Bit", "b"), KILOBIT(1, 8, "KiloBit", "Kb"), MEGABIT(2, 8, "MegaBit", "Mb"), GIGABIT(3, 8, "GigaBit", "Gb");
		
		public final String prefix;
		public final String symbol;
		
		public final long byteValue;
		public final long bitValue;
		
		private JDEC(int power, int unit, String prefix, String symbol)
		{
			this.byteValue = (long) (Math.pow(1024, power) / unit);
			this.bitValue = byteValue * 8;
			
			this.prefix = prefix;
			this.symbol = symbol;
		}
		
	}
	
}
