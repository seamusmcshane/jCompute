package jcompute;

public class CommandLineArg
{
	private String name;
	private String value;
	private String description;
	
	public CommandLineArg(String name,String value, String description)
	{
		this.name = name;
		this.value = value;
		this.description = description;
	}

	public String getValue()
	{
		return value;
	}

	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
}
