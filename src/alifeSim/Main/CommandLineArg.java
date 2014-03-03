package alifeSim.Main;

public class CommandLineArg
{
	private String name;
	private int value;
	private String description;
	
	public CommandLineArg(String name,int value, String description)
	{
		this.name = name;
		this.value = value;
		this.description = description;
	}

	public int getValue()
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
