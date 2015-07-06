package jCompute.Stats.Logs;

import jCompute.Stats.Trace.SingleStat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVLogParser
{
	private ArrayList<SingleStat> statGroup;
	private int lineNo;
	
	public CSVLogParser(String filePath)
	{
		statGroup = new ArrayList<SingleStat>();
		
		File file = new File(filePath);
		
		try
		{
			BufferedReader inputFile = new BufferedReader(new FileReader(file));
			
			// Read Header Line
			String line = inputFile.readLine();
			
			// Columns
			int numColumns = 0;
			
			String[] headings = line.split(",");
			
			numColumns = headings.length;
			
			System.out.println(numColumns);
			
			SingleStat[] stats = new SingleStat[numColumns];
			
			for(int c = 0; c < numColumns; c++)
			{
				stats[c] = new SingleStat(headings[c]);
				statGroup.add(stats[c]);
				
				SingleStat stat = statGroup.get(c);
				System.out.println(c + " " + stat.getStatName());
			}
			
			lineNo = 0;
			while((line = inputFile.readLine()) != null)
			{
				String[] data = line.split(",");
				
				for(int c = 0; c < numColumns; c++)
				{
					stats[c].addSample(Double.parseDouble(data[c]));
					
					// System.out.print(lineNo + " " + stats[c].getStatName() +
					// " " + data[c] + " ");
				}
				// System.out.println();
				lineNo++;
				
			}
			
			inputFile.close();
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public int getSampleNum()
	{
		return lineNo;
	}
	
	public ArrayList<SingleStat> getStats()
	{
		return statGroup;
	}
}
