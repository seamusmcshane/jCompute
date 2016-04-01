package jCompute;

import java.awt.MediaTracker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jCompute.util.PearsonHash;

public final class IconManager
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(IconManager.class);
	
	private static ImageIcon[] icons;
	
	@SuppressWarnings("unused")
	private static IconManager iconManager;
	
	public static void initialiseWithTheme(String themeName)
	{
		iconManager = new IconManager(themeName);
	}
	
	private IconManager(String themeName)
	{
		if(themeName.equals("none"))
		{
			return;
		}
		
		try
		{
			// Size the array to total IconIndex enum length
			icons = new ImageIcon[IconIndex.values().length];
			
			log.info("Loading icon theme " + themeName);
			
			String themeURI = "/icons/" + themeName + "/";
			
			readIconsViaMapping(themeURI);
			
			// Check the the icons are all loaded.
			for(int i = 0; i < icons.length; i++)
			{
				if(icons[i] == null)
				{
					log.warn("Theme " + themeName + " has no Icon Mapping for " + IconIndex.fromValue(i).getName());
				}
			}
		}
		catch(IOException | URISyntaxException e)
		{
			log.warn("Cannot read mapping file for " + themeName);
		}
	}
	
	/**
	 * Retrieves the an icon from the IconManager.
	 *
	 * @param index
	 * @return
	 * null if IconManager is not initialised or there is no theme loaded.
	 */
	public static ImageIcon retrieveIcon(IconIndex index)
	{
		if(icons == null)
		{
			return null;
		}
		
		return icons[index.value];
	}
	
	/**
	 * Mapping file format
	 * Purpose to map original icon names to jCompute internal names and avoid needing to rename icons.
	 * Lines starting with # are comments and ignored.
	 * # Comment
	 * Lines that start with a space will trigger a log warning.
	 * Mappings declarations are comma separated name,relative path pairs
	 * iconIndexName,relativePath
	 * Mappings that have a relative path starting with a ! are cross reference mappings.
	 * iconIndexName,!otherIconIndex
	 * Cross reference mappings declarations - point to an existing mapping in the format name,!existingMappingName.
	 * Cross reference mappings declarations can be in any location in the mapping file, including before the target mapping declaration.
	 *
	 * @param themeURI
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void readIconsViaMapping(String themeURI) throws IOException, URISyntaxException
	{
		LinkedList<String[]> crossRefs = new LinkedList<String[]>();
		
		// Locate the theme icons path. (Note URL)
		URL baseURL = IconManager.class.getResource(themeURI);
		
		// Find the full path to the mapping file (URLtoPath via URI)
		String basePath = new File(baseURL.toURI()).getAbsolutePath();
		String mappingFilePath = basePath + File.separator + "icon.mapping";
		
		log.info("Parsing " + mappingFilePath);
		
		final String lineEmptyMessage = "Empty line in mapping file on line ";
		final String lineParseErrorMessage = "Error parsing mapping on line ";
		final String lineIndexErrorMessage = "Did not find a matching index for mapping on line ";
		
		final String crossRefMappingErrorMessage = "A cross reference was not created due to the mapping not matching an index ";
		final String crossRefReferenceErrorMessage = "A cross reference was not created due to the reference not matching an index ";
		
		// The last error message
		String errorMessage = null;
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(mappingFilePath)));
		
		String line = reader.readLine();
		
		int lineNo = 0;
		
		while(line != null)
		{
			boolean skipLine = false;
			
			// An invalid empty line
			if(line.length() == 0)
			{
				log.warn(lineEmptyMessage + lineNo);
				
				skipLine = true;
			}
			
			// A comment line
			if((line.charAt(0) == '#'))
			{
				skipLine = true;
			}
			
			if(!skipLine)
			{
				boolean error = false;
				try
				{
					// Read the line
					String jcName = line.substring(0, line.indexOf(','));
					String path = line.substring(line.indexOf(',') + 1, line.length());
					
					if((jcName == null) || (path == null))
					{
						errorMessage = lineParseErrorMessage;
						error = true;
					}
					else
					{
						if(path.charAt(0) == '!')
						{
							String ref = path.substring(1, path.length());
							
							// Path in this case is a crossref
							crossRefs.add(new String[]
							{
								jcName, ref
							});
						}
						else
						{
							IconIndex index = lookupMappingByName(jcName);
							
							if(index == null)
							{
								errorMessage = lineIndexErrorMessage;
								
								error = true;
							}
							else
							{
								// Add the mapping
								addIcon(index, basePath + File.separator + path);
							}
						}
					}
				}
				catch(StringIndexOutOfBoundsException e)
				{
					errorMessage = lineParseErrorMessage;
					error = true;
				}
				
				if(error)
				{
					log.error(errorMessage + lineNo);
				}
			}
			
			lineNo++;
			line = reader.readLine();
		}
		
		// Map the cross refs.
		for(String[] crossRef : crossRefs)
		{
			switch(addCrossRef(crossRef[0], crossRef[1]))
			{
				
				case -10:
				{
					// Mapping index bad
					log.error(crossRefMappingErrorMessage + " mapping " + crossRef[0] + " reference " + crossRef[1]);
				}
				break;
				case -20:
				{
					// reference index bad
					log.error(crossRefReferenceErrorMessage + " mapping " + crossRef[0] + " reference " + crossRef[1]);
				}
				break;
				case 0:
				{
					log.debug("Created crossRef mapping " + crossRef[0] + " reference " + crossRef[1]);
					// Created
				}
				break;
			}
		}
		
		// Mapping file parsed.
		reader.close();
		
		log.info("Parsing Completed");
	}
	
	private void addIcon(IconIndex index, String iconPath)
	{
		// File will replace invalid file separators chars (for current system e.g. / to \ ) with a valid system separator
		String filePath = new File(iconPath).toString();
		
		log.debug(index.name + " " + filePath);
		
		// Assign the icon
		icons[index.value] = new ImageIcon(filePath, index.name);
		
		if(icons[index.value].getImageLoadStatus() != MediaTracker.COMPLETE)
		{
			icons[index.value] = null;
		}
	}
	
	private int addCrossRef(String name, String reference)
	{
		IconIndex mappingIndex = lookupMappingByName(name);
		IconIndex referenceIndex = lookupMappingByName(reference);
		
		if(mappingIndex == null)
		{
			// Mapping index bad
			return -10;
		}
		
		if(referenceIndex == null)
		{
			// reference index bad
			return -20;
		}
		
		// Point to the original reference - it could still be null if the reference is was not created, checked else where
		icons[mappingIndex.value] = icons[referenceIndex.value];
		
		// Cross reference was created successfully
		return 0;
	}
	
	/**
	 * Looks up the current mappings for a target string
	 *
	 * @param name
	 * @return
	 * null if the name does match an IconIndex name, or the IconIndex.
	 */
	private IconIndex lookupMappingByName(String name)
	{
		return IconIndex.fromName(name);
	}
	
	/**
	 * Allows looking up the icons by array index at runtime vs the former sting/hashmap approach
	 *
	 * @author Seamus McShane
	 */
	public enum IconIndex
	{
		// Enum Types
		addSimTab32(0, "addSimTab32"), simListTab32(1, "simListTab32"), simTabStatusNew32(2, "simTabStatusNew32"),
		simTabStatusRunning32(3, "simTabStatusRunning32"), simTabStatusPaused32(4, "simTabStatusPaused32"), simTabStatusFinished32(5, "simTabStatusFinished32"),
		openScenario32(6, "openScenario32"), saveScenario32(7, "saveScenario32"), generateSim16(8, "generateSim16"), startSim16(9, "startSim16"),
		pauseSim16(10, "pauseSim16"), resumeSim16(11, "resumeSim16"), scenarioEditor16(12, "scenarioEditor16"), chartTab16(13, "chartTab16"),
		charts16(14, "charts16"), batchTab32(15, "batchTab32"), clusterTab32(16, "clusterTab32"), loggingTab32(17, "loggingTab32"),
		benchmarkTab32(18, "benchmarkTab32"), addBatch32(19, "addBatch32"), removeBatch32(20, "removeBatch32"), moveForward32(21, "moveForward32"),
		moveBackward32(22, "moveBackward32"), moveToFront32(23, "moveToFront32"), moveToBack32(24, "moveToBack32"), start32(25, "start32"),
		stop32(26, "stop32"), pause32(27, "pause32"), std32(28, "std32"), high32(29, "high32"), start16(30, "start16"), pause16(31, "pause16"),
		stop16(32, "stop16"), paused16(33, "paused16"), resume16(34, "resume16"), simListTab16(35, "simListTab16"), loggingTab16(36, "loggingTab16"),
		nodesTab16(37, "nodesTab16");
		
		// IconIndex value to IconIndex lookup table
		private static final IconIndex[] indexes = new IconIndex[IconIndex.values().length];
		private static final PearsonHash pearsonHash;
		
		static
		{
			// Index the enums by there internal value - TODO sort by internal value before generating pearson hash.
			for(IconIndex index : IconIndex.values())
			{
				// Note using the IconIndex value field not enum ordinal.
				indexes[index.value] = index;
			}
			
			String[] textList = new String[IconIndex.values().length];
			
			for(IconIndex index : IconIndex.values())
			{
				textList[index.getValue()] = index.getName();
			}
			
			// Value may need adjusting when indexes increase.
			pearsonHash = new PearsonHash(textList, 10);
		}
		
		private final int value;
		private final String name;
		
		private IconIndex(int value, String name)
		{
			this.value = value;
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getValue()
		{
			return value;
		}
		
		// Avoidance of enum iteration at runtime at the cost of a lookup table.
		public static IconIndex fromValue(int value)
		{
			return ((value >= 0) && (value < indexes.length)) ? indexes[value] : null;
		}
		
		public static IconIndex fromName(String name)
		{
			int hash = pearsonHash.getHash(name.getBytes());
			
			int[] minimalKeyList = pearsonHash.getHashKeys();
			
			for(int i = 0; i < minimalKeyList.length; i++)
			{
				if(hash == minimalKeyList[i])
				{
					return IconIndex.fromValue(i);
				}
			}
			
			return null;
		}
	}
}
