package jcompute.batch.itemgenerator;

public class Parameter
{
	final String TYPE;
	final String PATH;
	final String GROUP_NAME;
	final String PARAMETER_NAME;
	final int VAL_INITIAL;
	final int VAL_INCREMENT;
	final int VAL_COMBINATIONS;
	
	public Parameter(String type, String path, String groupName, String parameterName, int initialVal, int valIncrement, int valCombinations)
	{
		this.TYPE = type;
		this.PATH = path;
		this.GROUP_NAME = groupName;
		this.PARAMETER_NAME = parameterName;
		this.VAL_INITIAL = initialVal;
		this.VAL_INCREMENT = valIncrement;
		this.VAL_COMBINATIONS = valCombinations;
	}
}
