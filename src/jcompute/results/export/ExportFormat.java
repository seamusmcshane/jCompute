package jcompute.results.export;

public enum ExportFormat
{
	CSV("Comma Separated Values", "csv"), XML("Extensible Markup Language", "xml"), ARFF("Attribute-Relation File Format", "arff"),
	ZXML("Extensible Markup Language in a Zip Archive", "xml"), ZCSV("Comma Separated Values in a Zip Archive", "csv");
	
	private final String description;
	private final String extension;
	
	private ExportFormat(String description, String extension)
	{
		this.description = description;
		this.extension = extension;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getExtension()
	{
		return extension;
	}
	
	public static ExportFormat fromInt(int v)
	{
		ExportFormat format = null;
		switch(v)
		{
			case 0:
				format = ExportFormat.XML;
			break;
			case 1:
				format = ExportFormat.CSV;
			break;
			case 2:
				format = ExportFormat.ARFF;
			break;
			case 3:
				format = ExportFormat.ZXML;
			break;
			case 4:
				format = ExportFormat.ZCSV;
			break;
			default:
				/* Invalid Usage */
				format = null;
		}
		
		return format;
	}
}
