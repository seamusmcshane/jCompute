package jCompute;

import jCompute.util.FileUtil;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconManager
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(IconManager.class);
	
	private static HashMap<String, ImageIcon> iconMap;

	private static String smallIconDirName = "16x16";
	private static String mediumIconDirName = "32x32";

	@SuppressWarnings("unused")
	private static IconManager iconManager;

	public static void init(String themeName)
	{
		iconManager = new IconManager(themeName);
	}

	public static ImageIcon getIcon(String iconName)
	{
		return iconMap.get(iconName + ".png");
	}

	private IconManager(String themeName)
	{
		iconMap = new HashMap<String, ImageIcon>();

		log.info("Loading icon theme " + themeName);

		String themePath = "/icons/" + themeName + "/";

		URL smallIconURL;
		URL mediumIconURL;

		smallIconURL = IconManager.class.getResource(themePath + smallIconDirName);
		mediumIconURL = IconManager.class.getResource(themePath + mediumIconDirName);

		loadIcons(FileUtil.getFilesInDir(smallIconURL));
		loadIcons(FileUtil.getFilesInDir(mediumIconURL));
	}

	private void loadIcons(File[] files)
	{
		for (int f = 0; f < files.length; f++)
		{
			log.debug("File : " + files[f].getName() + " " + files[f].getAbsolutePath());

			if (files[f].isFile())
			{
				iconMap.put(files[f].getName(), new ImageIcon(files[f].getAbsolutePath(), files[f].getName()));
			}
		}
	}


}
