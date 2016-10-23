package jcompute.stats.logparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.stats.trace.Trace;
import jcompute.stats.trace.Trace.TraceDataType;

public class CSV
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CSV.class);
	
	private ArrayList<Trace> traceList;
	private int lineNo;
	
	public CSV(String filePath) throws IOException
	{
		traceList = new ArrayList<Trace>();
		
		File file = new File(filePath);
		
		try
		{
			BufferedReader inputFile = new BufferedReader(new FileReader(file));
			
			// Read Header Line
			String line = inputFile.readLine();
			
			if(line == null)
			{
				inputFile.close();
				
				throw new IOException();
			}
			
			// Columns
			int numColumns = 0;
			
			String[] headings = line.split(",");
			
			numColumns = headings.length;
			
			log.debug(numColumns);
			
			Trace[] stats = new Trace[numColumns];
			
			for(int c = 0; c < numColumns; c++)
			{
				stats[c] = new Trace(headings[c], TraceDataType.Decimal);
				traceList.add(stats[c]);
				
				// Trace stat = traceList.get(c);
				// log.debug(c + " " + stat.name);
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
			log.error("CSV Error reading File");
			
			throw new IOException(e);
		}
	}
	
	public int getSampleNum()
	{
		return lineNo;
	}
	
	public ArrayList<Trace> getTraces()
	{
		return traceList;
	}
}
