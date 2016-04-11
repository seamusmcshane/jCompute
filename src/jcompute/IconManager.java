package jcompute;

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

import jcompute.util.PearsonHash;

public final class IconManager
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(IconManager.class);
	
	// The icon manager singleton
	private static IconManager iconManager;
	
	// The permanent "ImageIcon" index using by the rest of the API for retrieving image icons.
	private ImageIcon[] imageIconIndex;
	
	/*
	 * ***************************************************************************************************
	 * Public Static Methods
	 *****************************************************************************************************/
	
	/**
	 * Initialise the icon manager.
	 * The icon manager will then attempt to load the icon theme.
	 *
	 * @param themeName
	 */
	public synchronized static boolean initialiseWithTheme(String themeName)
	{
		if(iconManager == null)
		{
			iconManager = new IconManager(themeName);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Retrieves the an icon from the IconManager.
	 *
	 * @param index
	 * @return
	 * null if IconManager is not initialised, there is no theme loaded or the icon is missing from the theme.
	 */
	public static ImageIcon retrieveIcon(IconIndex index)
	{
		if(iconManager == null)
		{
			return null;
		}
		
		return iconManager.retrieveImageIcon(index);
	}
	
	/*
	 * ***************************************************************************************************
	 * Private singleton Methods
	 *****************************************************************************************************/
	
	/**
	 * Private constructor.
	 *
	 * @param themeName
	 */
	private IconManager(String themeName)
	{
		if(themeName.equals("none"))
		{
			return;
		}
		
		try
		{
			// An indexer object that maps icon names to indexes
			IconIndexer indexer = new IconIndexer();
			
			// Size the array to total IconIndex enum length
			imageIconIndex = new ImageIcon[IconIndex.values().length];
			
			log.info("Loading icon theme " + themeName);
			
			String themeURI = "/icons/" + themeName + "/";
			
			readIconsViaMapping(themeURI, indexer);
			
			// Check the the icons are all loaded.
			for(int i = 0; i < imageIconIndex.length; i++)
			{
				if(imageIconIndex[i] == null)
				{
					log.warn("Theme " + themeName + " has no Icon Mapping for " + indexer.getIconIndexbyValue(i).name);
				}
			}
		}
		catch(IOException | URISyntaxException e)
		{
			log.warn("Cannot read mapping file for " + themeName);
		}
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
	 * @param indexer
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private void readIconsViaMapping(String themeURI, IconIndexer indexer) throws IOException, URISyntaxException
	{
		LinkedList<String[]> crossRefs = new LinkedList<String[]>();
		
		// Locate the theme icons path. (Note URL)
		URL baseURL = IconManager.class.getResource(themeURI);
		
		if(baseURL == null)
		{
			log.error("Icon Theme and/or mapping file not found");
			
			return;
		}
		
		// Find the full path to the mapping file (URLtoPath via URI)
		String basePath = new File(baseURL.toURI()).getAbsolutePath();
		String mappingFilePath = basePath + File.separator + "icon.mapping";
		
		log.info("Performing Icon Mapping using : " + mappingFilePath);
		
		final String lineEmptyMessage = "Empty line in mapping file on line ";
		final String lineParseErrorMessage = "Error parsing mapping on line ";
		final String lineIndexErrorMessage = "Did not find an index for the mapping name on line ";
		final String iconFileErrorMessage = "Did not find the icon file for mapping on line ";
		
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
							switch(addIcon(jcName, basePath + File.separator + path, indexer))
							{
								case -10:
								{
									// The index / icon name was not found
									errorMessage = lineIndexErrorMessage;
									
									error = true;
								}
								break;
								case -20:
								{
									// Problem loading icon file.
									errorMessage = iconFileErrorMessage;
									
									error = true;
								}
								break;
								case 0:
								{
									// Created
									log.debug("Created mapping " + jcName);
								}
								break;
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
			switch(addCrossRef(crossRef[0], crossRef[1], indexer))
			{
				case -10:
				{
					// Mapping index bad
					log.error(crossRefMappingErrorMessage + " mapping " + crossRef[0] + " reference " + crossRef[1]);
				}
				break;
				case -20:
				{
					// Reference index bad
					log.error(crossRefReferenceErrorMessage + " mapping " + crossRef[0] + " reference " + crossRef[1]);
				}
				break;
				case 0:
				{
					// Created
					log.debug("Created crossRef mapping " + crossRef[0] + " reference " + crossRef[1]);
				}
				break;
			}
		}
		
		// Mapping file parsed.
		reader.close();
		
		log.info("Icon Mapping Finished");
	}
	
	/**
	 * Adds loads an image icon into the index.
	 *
	 * @param index
	 * @param iconPath
	 * @param indexer
	 */
	private int addIcon(String name, String iconPath, IconIndexer indexer)
	{
		IconIndex index = indexer.getIconIndexbyName(name);
		
		if(index == null)
		{
			return -10;
		}
		
		// File will replace invalid file separators chars (for current system e.g. / to \ ) with a valid system separator
		String filePath = new File(iconPath).toString();
		
		log.debug(index.name + " " + filePath);
		
		// Assign the icon
		imageIconIndex[index.value] = new ImageIcon(filePath, index.name);
		
		if(imageIconIndex[index.value].getImageLoadStatus() != MediaTracker.COMPLETE)
		{
			imageIconIndex[index.value] = null;
			
			// We had a correct name but the icon file was not loaded.
			return -20;
		}
		
		return 0;
	}
	
	/**
	 * Adds a cross ref icon into the index using the target mapping.
	 *
	 * @param name
	 * @param reference
	 * @param indexer
	 * @return
	 */
	private int addCrossRef(String name, String reference, IconIndexer indexer)
	{
		IconIndex mappingIndex = indexer.getIconIndexbyName(name);
		IconIndex referenceIndex = indexer.getIconIndexbyName(reference);
		
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
		imageIconIndex[mappingIndex.value] = imageIconIndex[referenceIndex.value];
		
		// Cross reference was created successfully
		return 0;
	}
	
	/**
	 * Retrieves the an icon from the IconManager.
	 *
	 * @param index
	 * @return
	 * null if IconManager is not initialised or there is no theme loaded.
	 */
	public ImageIcon retrieveImageIcon(IconIndex index)
	{
		if(imageIconIndex == null)
		{
			return null;
		}
		
		return imageIconIndex[index.value];
	}
	
	/*
	 * ***************************************************************************************************
	 * Private API object
	 *****************************************************************************************************/
	
	private class IconIndexer
	{
		// "IconIndex" value to IconIndex lookup table.
		private IconIndex[] iconIndex = new IconIndex[IconIndex.values().length];
		
		// Hash function for looking up the iconIndex by string.
		private PearsonHash pearsonHash;
		
		public IconIndexer()
		{
			// This allows looking up the icons by array index at runtime vs the former sting + permanent hashmap approach
			// Index the enums by there internal value
			// If the enum orders are ever changed (and dont not match the value order) they the index may needed sorted by internal value before generating
			// pearson hash.
			for(IconIndex index : IconIndex.values())
			{
				// Note using the IconIndex value field not enum ordinal.
				iconIndex[index.value] = index;
			}
			
			// Create an array of strings that we will create a perfect minimal hash for. (as we know the complete set)
			String[] textList = new String[IconIndex.values().length];
			
			// Effectively an array copy but just the index.names
			for(IconIndex index : IconIndex.values())
			{
				textList[index.value] = index.name;
			}
			
			// Value may need adjusting when indexes increase. (temporary)
			pearsonHash = new PearsonHash(textList, 10);
		}
		
		// Avoidance of enum iteration at runtime at the cost of a lookup table.
		public IconIndex getIconIndexbyValue(int value)
		{
			// if out of range, then return null else do the lookup
			return ((value >= 0) && (value < iconIndex.length)) ? iconIndex[value] : null;
		}
		
		public IconIndex getIconIndexbyName(String name)
		{
			int hash = pearsonHash.getHash(name.getBytes());
			
			int[] minimalKeyList = pearsonHash.getHashKeys();
			
			for(int i = 0; i < minimalKeyList.length; i++)
			{
				// Integer comparison
				if(hash == minimalKeyList[i])
				{
					return getIconIndexbyValue(i);
				}
			}
			
			// Icon not found
			return null;
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Public API Enum
	 *****************************************************************************************************/
	
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
		
		public final int value;
		public final String name;
		
		private IconIndex(int value, String name)
		{
			this.value = value;
			this.name = name;
		}
	}
}
