package jCompute.util;


import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.filechooser.FileFilter;

public class FileUtil
{	
	public static File[] getFilesInDir(URL url)
	{
		File file = null;

		try
		{
			if(url != null)
			{
				file = new File(url.toURI());
			}
		}
		catch(URISyntaxException e)
		{
			System.out.println(e.getCause());
		}

		if(file != null)
		{
			return file.listFiles();
		}

		return new File[]{};
	}

	public static File[] getFilesInDir(String path)
	{
		File file = new File(path);

		return file.listFiles();
	}

	public static String[] getDirectoriesInDir(String path)
	{
		return new File(path).list(new FilenameFilter()
		{
			@Override
			public boolean accept(File current, String name)
			{
				return new File(current, name).isDirectory();
			}
		});
	}

	public static boolean dirContainsFileNamed(String path, String fileName)
	{
		File files[] = FileUtil.getFilesInDir(path);

		if(files != null)
		{
			for(File file : files)
			{

				if(file.getName().equals(fileName))
				{
					System.out.println(file.getAbsolutePath());

					return true;
				}
			}
		}

		return false;
	}
	
	public static void createDirIfNotExist(String dir)
	{
		File directory = new File(dir);

		if (!directory.exists())
		{
			directory.mkdir();
		}

	}
	
	public static FileFilter batchFileFilter()
	{
		return new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.getName().toLowerCase().endsWith(".batch") || f.isDirectory();
			}

			@Override
			public String getDescription()
			{
				return "Batch Files";
			}
		};
	}
	
	public static FileFilter scenarioFileFilter()
	{
		return new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.getName().toLowerCase().endsWith(".scenario") || f.isDirectory();
			}

			@Override
			public String getDescription()
			{
				return "Scenario Files";
			}
		};
	}

}
