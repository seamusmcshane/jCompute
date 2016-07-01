package jcompute.math;

public final class NumericConstants
{
	/**
	 * Helper Method for working with the various representations of binary numbers
	 * @author Seamus McShane
	 */
	public enum BinaryPrefix
	{
		BYTE(1, "Bytes", "B"),
		/*
		 * ***************************************************************************************************
		 * SI/Metric - (Base 10) - Disk Based Storage and Information Transfer
		 *****************************************************************************************************/
		SI_KILOBIT(1000, "KiloBit", "kb"), SI_MEGABIT(1000, 2, "MegaBit", "Mb"), SI_GIGABIT(1000, 3, "GigaBit", "Gb"), SI_TERABIT(1000, 4, "TeraBit", "Tb"),
		SI_KILOBYTE(1000, 1, "KiloByte", "kB"), SI_MEGABYTE(1000, 2, "MegaByte", "MB"), SI_GIGABYTE(1000, 3, "GigaByte", "GB"),
		SI_TERABYTE(1000, 4, "TeraByte", "TB"),
		/*
		 * ***************************************************************************************************
		 * JDEC Standard (Base 2) - Chip Based Storage and Serial Transfer Rate
		 *****************************************************************************************************/
		JDEC_KILOBYTE(1024, "KiloByte", "KB"), JDEC_MEGABYTE(1024, 2, "MegaByte", "MB"), JDEC_GIGABYTE(1024, 3, "GigaByte", "GB"),
		JDEC_TERABYTE(1024, 4, "TeraByte", "TB"),
		/*
		 * ***************************************************************************************************
		 * IEC Standard Binary Prefixes (Base 2)
		 *****************************************************************************************************/
		KIBIBYTE(1024, "KibiByte", "KiB"), MEBIBYTE(1024, 2, "MebiByte", "MiB"), GIBIBYTE(1024, 3, "GibiByte", "GiB"), TEBIBYTE(1024, 4, "TebiByte", "TiB"),
		KIBIBIT(1024, "KibiBit", "Kib"), MEBI(1024, 2, "MebiBit", "Mib"), GIBI(1024, 3, "GibiBit", "Gib"), TEBI(1024, 4, "TebiBit", "Tib");
		
		// The Unit Name
		public final String prefix;
		
		// The Unit Symbol
		public final String symbol;
		
		// Byte Value
		public final long byteValue;
		
		// Bit Value
		public final long bitValue;
		
		// Takes a base and multiples it by a power to get a byte value
		private BinaryPrefix(int base, int power, String prefix, String symbol)
		{
			this.byteValue = (long) (Math.pow(base, power));
			this.bitValue = byteValue * 8;
			
			this.prefix = prefix;
			this.symbol = symbol;
		}
		
		// Takes a byte value
		private BinaryPrefix(int byteValue, String prefix, String symbol)
		{
			this.byteValue = byteValue;
			this.bitValue = byteValue * 8;
			
			this.prefix = prefix;
			this.symbol = symbol;
		}
	}
}
