package tools.ReportGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;

import com.DaveKoelle.AlphanumComparator;

import jCompute.util.FileUtil;
import jCompute.util.LookAndFeel;

public class ReportUtility
{
	private final static int imageWidth = 600;
	private final static int imageHeight = 400;
	
	public static void main(String args[])
	{
		LookAndFeel.setLookandFeel("default");
		
		// Rows/Colum Names
		ArrayList<String> rowNames = new ArrayList<String>();
		ArrayList<String> colNames = new ArrayList<String>();
		
		// Cells
		Map<String, String> cells = new HashMap<String, String>();
		
		final JFileChooser filechooser = new JFileChooser(new File("\\\\Nanoserv\\results\\"));
		
		filechooser.setDialogTitle("Choose Directory");
		filechooser.setMultiSelectionEnabled(false);
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int val = filechooser.showOpenDialog(filechooser);
		
		float scale = 1f;
		
		String itemLogName = "ItemLog";
		
		if(val == JFileChooser.APPROVE_OPTION)
		{
			String fullPath = filechooser.getSelectedFile().getPath();
			System.out.println("Path : " + fullPath);
			
			// Level 0
			String documentName = filechooser.getSelectedFile().getName();
			System.out.println("Document Name will be : " + documentName);
			
			// Item LogName detected
			String itemLogNameWithExt = generateMap(rowNames, colNames, cells, fullPath, itemLogName);
			
			System.out.println("LogFile detected as : " + itemLogNameWithExt);
			
			Collections.sort(rowNames, new AlphanumComparator());
			Collections.sort(colNames, new AlphanumComparator());
			
			SurfacePlotImageExporter imageExporter = new SurfacePlotImageExporter(imageWidth, imageHeight, rowNames, colNames, cells, fullPath, itemLogNameWithExt);
			
			imageExporter.export();
			
			PDFReport pdfReport = new PDFReport(documentName, rowNames, colNames, cells, fullPath, scale, imageWidth, imageHeight);
			
			pdfReport.generate();
			
			System.out.println("Report Finished");
		}
		else
		{
			System.out.println("Report Cancelled");
		}
		
		System.exit(0);
		
	}
	
	/**
	 * @param rowNames
	 * @param colNames
	 * @param cells
	 * @param fullPath
	 * @param filename
	 * @return itemLog
	 */
	private static String generateMap(ArrayList<String> rowNames, ArrayList<String> colNames, Map<String, String> cells, String fullPath, String filename)
	{
		boolean extDetected = false;
		
		String itemLogWithExt = null;
		
		// Level 1
		String level1dirs[] = FileUtil.getDirectoriesInDir(fullPath);
		
		if(FileUtil.dirContainsFileNamedMinusExt(fullPath + File.separator, filename))
		{
			// A single directory
			System.out.println("Single Dir");
		}
		else
		{
			// Detect special cases
			boolean doColumns = true;
			boolean doRows = true;
			
			for(String level1dir : level1dirs)
			{
				if(!level1dir.equals("images"))
				{
					String level1Path = fullPath + File.separator + level1dir;
					System.out.println("l1 Dir : " + level1dir);
					
					// Row Name
					String rowName;
					
					if(FileUtil.dirContainsFileNamedMinusExt(level1Path, filename))
					{
						if(!extDetected)
						{
							itemLogWithExt = FileUtil.getFileWithExtInDirMatchingName(level1Path, filename);
							
							extDetected = true;
						}
						
						// A directory with 1 level of groups
						System.out.println("Group Dir");
						
						rowName = fullPath.substring(fullPath.lastIndexOf(File.separator) + 1, fullPath.length());
						
						if(doRows)
						{
							doRows = false;
							rowNames.add(rowName);
							System.out.println("Row Name : " + rowName);
						}
						
						String colName = level1dir;
						colNames.add(colName);
						System.out.println("Column Name : " + colName);
						
						String index = rowName + colName;
						
						// Add Cell Index - item log locations
						cells.put(index, level1Path);
						
						System.out.println("Added Cell " + cells.size());
						
					}
					else
					{
						// Row Name
						rowName = level1dir;
						rowNames.add(rowName);
						System.out.println("Row Name : " + rowName);
						
						// A directory with 2 levels of groups
						String level2dirs[] = FileUtil.getDirectoriesInDir(level1Path);
						
						if(doColumns)
						{
							doColumns = false;
							// Detect Column Names - Assumes all sub directories
							// match the first directories layout.
							String columnDir = level2dirs[0];
							
							String columPath = fullPath + File.separator + level1dir + File.separator + columnDir;
							
							String columDirs[] = FileUtil.getDirectoriesInDir(level1Path);
							
							for(String dir : columDirs)
							{
								String colName = dir.substring(dir.lastIndexOf(']') + 2, dir.length());
								
								colNames.add(colName);
								
								System.out.println("Column Name : " + colName);
							}
							
						}
						// Now Index Cells
						for(String level2dir : level2dirs)
						{
							String level2Path = fullPath + File.separator + level1dir + File.separator + level2dir;
							
							if(FileUtil.dirContainsFileNamedMinusExt(level2Path, filename))
							{
								if(!extDetected)
								{
									itemLogWithExt = FileUtil.getFileWithExtInDirMatchingName(level2Path, filename);
									
									extDetected = true;
								}
								
								System.out.println("L2 Dir : " + level2dir);
								
								String colName = level2dir.substring(level2dir.lastIndexOf(']') + 2, level2dir.length());
								
								String index = rowName + colName;
								
								// Add Cell Index - item log locations
								cells.put(index, level2Path);
								
								System.out.println("Added Cell " + cells.size());
							}
							
						}
						
					}
				}
				
			}
			
		}
		
		return itemLogWithExt;
	}
}
