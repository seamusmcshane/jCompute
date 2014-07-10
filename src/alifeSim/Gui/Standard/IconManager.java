package alifeSim.Gui.Standard;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.ImageIcon;

public class IconManager
{
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

		System.out.println("Loading theme " + themeName);

		String themePath = "/icons/" + themeName + "/";

		URL smallIconURL;
		URL mediumIconURL;

		smallIconURL = IconManager.class.getResource(themePath + smallIconDirName);
		mediumIconURL = IconManager.class.getResource(themePath + mediumIconDirName);

		loadIcons(getFilesInDir(smallIconURL));
		loadIcons(getFilesInDir(mediumIconURL));
	}

	private void loadIcons(File[] files)
	{
		for (int f = 0; f < files.length; f++)
		{
			System.out.println("File : " + files[f].getName() + " " + files[f].getAbsolutePath());

			if (files[f].isFile())
			{
				iconMap.put(files[f].getName(), new ImageIcon(files[f].getAbsolutePath(), files[f].getName()));
			}
		}
	}

	private File[] getFilesInDir(URL url)
	{
		File file = null;

		try
		{
			if(url!=null)
			{
				System.out.println(url.toURI());
				file = new File(url.toURI());
			}
		}
		catch (URISyntaxException e)
		{
			System.out.println(e.getCause());
		}

		if (file != null)
		{
			return file.listFiles();
		}

		return new File[]{};
	}
}
