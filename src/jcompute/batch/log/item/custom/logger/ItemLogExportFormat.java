package jcompute.batch.log.item.custom.logger;

public enum ItemLogExportFormat
{
	CSV("Comma Separated Values", "csv"), TextV1("Item Log V1 Format", "log"), TextV2("Item Log V2 Format", "v2log"), XML("Item Log XML Format", "xml");
	
	private final String description;
	private final String extension;
	
	private ItemLogExportFormat(String description, String extension)
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
	
	public static ItemLogExportFormat fromInt(int v)
	{
		ItemLogExportFormat format = null;
		switch(v)
		{
			case 0:
				format = ItemLogExportFormat.CSV;
			break;
			case 1:
				format = ItemLogExportFormat.TextV1;
			break;
			case 2:
				format = ItemLogExportFormat.TextV2;
			break;
			case 3:
				format = ItemLogExportFormat.XML;
			break;
			default:
				/* Invalid Usage */
				format = null;
		}
		
		return format;
	}
	
	public static ItemLogExportFormat fromString(String v)
	{
		ItemLogExportFormat format = null;
		switch(v)
		{
			case "CSV":
				format = ItemLogExportFormat.CSV;
			break;
			case "TextV1":
				format = ItemLogExportFormat.TextV1;
			break;
			case "TextV2":
				format = ItemLogExportFormat.TextV2;
			break;
			case "XML":
				format = ItemLogExportFormat.XML;
			break;
			default:
				/* Invalid Usage or unsupported format */
				format = null;
		}
		
		return format;
	}
}
