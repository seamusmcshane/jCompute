package jCompute.Batch.LogFileProcessor;

import java.util.ArrayList;

public class TextBatchLogProcessorMapperV2
{
	public static final int HEADER_LINE_OPTS = 4;
	public static final int MAX_LINE_OPTS = 7;
	
	public static final char OPTION_DELIMITER = ',';
	public static final char SUBOPTION_DELIMITER = ';';
	public static final char FIELD_DELIMITER = '=';
	
	private ArrayList<TextBatchLogItem> logItems;
	
	public TextBatchLogProcessorMapperV2(String filePath, int maxVal)
	{
	
	}
}
