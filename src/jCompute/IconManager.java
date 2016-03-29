package jCompute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IconManager
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(IconManager.class);

	private static HashMap<String, ImageIcon> iconMap;

	@SuppressWarnings("unused")
	private static IconManager iconManager;

	private IconManager(String themeName)
	{
		if(themeName.equals("none"))
		{
			return;
		}

		try
		{
			iconMap = new HashMap<String, ImageIcon>();

			log.info("Loading icon theme " + themeName);

			String themeURI = "/icons/" + themeName + "/";

			readIconsViaMapping(themeURI);
		}
		catch(IOException | URISyntaxException e)
		{
			log.warn("Cannot read mapping file for " + themeName);
		}
	}

	public static void init(String themeName)
	{
		iconManager = new IconManager(themeName);
	}

	public static ImageIcon retrieveIcon(String iconName)
	{
		if(iconMap == null)
		{
			return null;
		}

		ImageIcon icon = iconMap.get(iconName);

		if(icon == null)
		{
			log.warn("No matching icon found for " + iconName);

			Thread.dumpStack();
		}

		return icon;
	}

	/**
	 * Mapping file format
	 * Purpose to map original icon names to jCompute internal names and avoid needing to rename icons.
	 * Lines starting with # are comments and ignored.
	 * Lines that start with a space will trigger a log warning.
	 * Mappings declarations are comma separated name,relative path pairs
	 * Mappings that have a relative path starting with a ! are cross reference mappings.
	 * Cross reference mappings declarations - point to an existing mapping in the format name,!existingMappingName.
	 * Cross reference mappings declarations can be in any location in the mapping file, including before the exiting mapping declaration.
	 * Mapping List
	 * ##########################################
	 * # Interactive GUI
	 * ##########################################
	 * #
	 * # Main Tabs
	 * ##########################################
	 * addSimTab32,
	 * simListTab32,
	 * #
	 * # Simulation Tab Status
	 * ##########################################
	 * simTabStatusNew32,!stop32
	 * simTabStatusRunning32,!start32
	 * simTabStatusPaused32,!pause32
	 * simTabStatusFinished32,
	 * #
	 * # Scenario Editor Buttons
	 * ##########################################
	 * openScenario32,
	 * saveScenario32,
	 * #
	 * # Sim Control Buttons
	 * ##########################################
	 * generateSim16,
	 * startSim16,!start16
	 * pauseSim16,!paused16
	 * resumeSim16,!resume16
	 * #
	 * # Simulation Tab - Tabs
	 * ##########################################
	 * scenarioEditor16,
	 * chartTab16,
	 * charts16,
	 * #
	 * #
	 * ##########################################
	 * # Batch GUI
	 * ##########################################
	 * #
	 * # Main Tabs
	 * ##########################################
	 * batchTab32,
	 * clusterTab32,
	 * #
	 * # Dynamic Tabs
	 * ##########################################
	 * loggingTab32,
	 * benchmarkTab32,
	 * #
	 * # Batch Tab
	 * ##########################################
	 * addBatch32,
	 * removeBatch32,
	 * moveForward32,
	 * moveBackward32,
	 * moveToFront32,
	 * moveToBack32,
	 * start32,
	 * stop32,
	 * pause32,
	 * #
	 * # Batch Priority (Fifo/Fair)
	 * ##########################################
	 * std32,
	 * high32,
	 * #
	 * # Simulation/Batch Tables Status
	 * ##########################################
	 * start16,
	 * pause16,
	 * stop16,
	 * paused16,
	 * resume16,
	 * #
	 * # Cluster Info Tabs
	 * ##########################################
	 * simListTab16,
	 * loggingTab16,
	 * nodesTab16,
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

		log.info(mappingFilePath);

		BufferedReader reader = new BufferedReader(new FileReader(new File(mappingFilePath)));

		String line = reader.readLine();

		int lineNo = 0;

		while(line != null)
		{
			boolean skipLine = false;

			if(line.length() == 0)
			{
				log.warn("Got empty line in mapping file at " + lineNo);

				skipLine = true;
			}

			if(!skipLine)
			{
				if(!(line.charAt(0) == '#'))
				{
					boolean error = false;
					try
					{
						
						// Read the line
						String jcName = line.substring(0, line.indexOf(','));
						String path = line.substring(line.indexOf(',') + 1, line.length());

						if((jcName == null) || (path == null))
						{
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
								addIcon(jcName, basePath + File.separator + path);
							}
						}
					}
					catch(StringIndexOutOfBoundsException e)
					{
						error = true;
					}

					if(error)
					{
						log.error("Problem reading mapping in file at Line " + lineNo);
					}
				}
				else
				{
					// a line Comment
					// # .....
				}

			}

			lineNo++;
			line = reader.readLine();
		}

		// Map the cross refs.
		for(String[] crossRef : crossRefs)
		{
			addCrossRef(crossRef[0], crossRef[1]);
		}

		reader.close();

	}

	private void addCrossRef(String name, String reference)
	{
		ImageIcon icon = iconMap.get(reference);

		if(icon == null)
		{
			log.error("Cross Ref for " + name + " is not valid");
		}
		else
		{
			log.debug("Icon Cross Ref " + name + " " + reference);

			iconMap.put(name, icon);
		}
	}

	private void addIcon(String name, String iconPath)
	{
		// File will replace invalid file separators chars (for current system e.g. / to \ ) with a valid system separator
		String filePath = new File(iconPath).toString();

		log.debug(name + " " + filePath);

		iconMap.put(name, new ImageIcon(filePath, name));
	}
}
