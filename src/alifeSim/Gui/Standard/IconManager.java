package alifeSim.Gui.Standard;

import java.io.File;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class IconManager
{
	private static HashMap<String, ImageIcon> iconMap;

	private static String smallIconDirName = "16x16";
	private static String mediumIconDirName = "32x32";
	
	public static void init(String themeName)
	{
		iconMap = new HashMap<String, ImageIcon>();
		
		System.out.println("Loading theme " + themeName);

		// Force lowercase dir names.
		String themeDir = "icons"+File.separatorChar+themeName.toLowerCase();
		String smallIconDir = themeDir+File.separatorChar+smallIconDirName;
		String mediumIconDir = themeDir+File.separatorChar+mediumIconDirName;
		
		System.out.println("Theme location " + themeDir);		
		
		// Check the icon dirs exist then proceed
		if(dirExistsNow(smallIconDir) && dirExistsNow(mediumIconDir))
		{
			loadIcons(getFilesInDir(smallIconDir));
			loadIcons(getFilesInDir(mediumIconDir));
		}
		else
		{
			System.out.println("Fatal - Did not load theme " + themeName);
		}

	}
	
	public static ImageIcon getIcon(String iconName)
	{
		return iconMap.get(iconName+".png");
	}
	
	private static void loadIcons(File[] files)
	{
		
		for(int f=0;f<files.length;f++)
		{
			System.out.println("File : " + files[f].getName() + " " + files[f].getAbsolutePath() );
			
			if (files[f].isFile())
			{
				iconMap.put(files[f].getName(), new ImageIcon(files[f].getAbsolutePath(),files[f].getName()));
			}
		}		
	}
	
	private static File[] getFilesInDir(String dir)
	{
		return new File(dir).listFiles();
	}
	
	private static boolean dirExistsNow(String dir)
	{
		System.out.println(dir);
		return (new File(dir).exists());
	}
}
