package jcompute.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(FileUtil.class);

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
			log.error(e.getCause());
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

		return file.listFiles(new java.io.FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.isFile();
			}
		});
	}

	public static URL[] getFilesInDirAsURLS(String path) throws MalformedURLException
	{
		File[] files = getFilesInDir(path);

		URL[] urls = new URL[files.length];

		for(int f = 0; f < files.length; f++)
		{
			urls[f] = files[f].toURI().toURL();
		}

		return urls;
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
					log.debug(fileName + " Found : " + file.getAbsolutePath());

					return true;
				}
			}
		}

		return false;
	}

	public static boolean dirContainsFileNamedMinusExt(String path, String fileNameNoExt)
	{
		File files[] = FileUtil.getFilesInDir(path);

		if(files != null)
		{
			for(File file : files)
			{
				String fileFound = file.getName();
				fileFound = fileFound.substring(0, fileFound.lastIndexOf('.'));

				if(fileFound.equals(fileNameNoExt))
				{
					log.debug(fileNameNoExt + " Found : " + file.getAbsolutePath());

					return true;
				}
			}
		}

		return false;
	}

	public static String getFileWithExtInDirMatchingName(String path, String name)
	{
		File files[] = FileUtil.getFilesInDir(path);

		if(files != null)
		{
			for(File file : files)
			{
				String fileFound = file.getName();
				String fileFoundNoExt = fileFound.substring(0, fileFound.lastIndexOf('.'));

				if(fileFoundNoExt.equals(name))
				{
					log.debug(name + " Found : " + file.getAbsolutePath());

					return fileFound;
				}
			}
		}

		return null;
	}

	public static void createDirIfNotExist(String dir)
	{
		File directory = new File(dir);

		if(!directory.exists())
		{
			directory.mkdir();
		}
	}

	public static javax.swing.filechooser.FileFilter batchFileFilter()
	{
		return new javax.swing.filechooser.FileFilter()
		{
			@Override
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

	public static byte[] compressBytes(byte[] contents, final int level)
	{
		byte[] compressedContents = null;

		// Compress the string
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			GZIPOutputStream gzip = new GZIPOutputStream(out)
			{
				{
					def.setLevel(level);
				}
			};

			gzip.write(contents);
			gzip.close();

			compressedContents = out.toByteArray();
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}

		return compressedContents;
	}

	public static byte[] decompressBytes(byte[] contentBytes)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(contentBytes)), out);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		return out.toByteArray();
	}

	public static String HashFile(byte[] contents)
	{
		String fileHash = null;

		try
		{
			// Contents hashed as Bytes
			byte[] byteHash = MessageDigest.getInstance("MD5").digest(contents);

			// Hash to valid String
			StringBuilder buffer = new StringBuilder();
			for(int d = 0; d < byteHash.length; d++)
			{
				buffer.append(Integer.toString((byteHash[d] & 0xff) + 0x100, 16).substring(1));
			}

			fileHash = buffer.toString();
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

		return fileHash;
	}

	/**
	 * Wrapper Around ApacheIO
	 *
	 * @param path
	 * @return
	 */
	public static String getFileNameExtension(String path)
	{
		return FilenameUtils.getExtension(path);
	}

	public static javax.swing.filechooser.FileFilter scenarioFileFilter()
	{
		return new javax.swing.filechooser.FileFilter()
		{
			@Override
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

	public static String stringAsValidFileName(String text)
	{
		try
		{
			return URLEncoder.encode(text, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			// Unlikely to ever happen unless the JVM is broke - UTF-8 is a standard java charset.
			return "CharSetEncoderNotFound";
		}
	}

	public static String getFileName(String filePath)
	{
		Path path = Paths.get(filePath);

		if(path == null)
		{
			return null;
		}

		Path fileName = path.getFileName();

		if(fileName == null)
		{
			return null;
		}

		return fileName.toString();
	}

	public static String getPath(String filePath)
	{
		Path path = Paths.get(filePath);

		if(path == null)
		{
			return null;
		}

		Path dir = path.getParent();

		if(dir == null)
		{
			return null;
		}

		return dir.toString();
	}
	
	public static String getClassDirectory(Class<?> jClass)
	{
		return new File(jClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
	}
	
	public static String getJCDirectory()
	{
		return getClassDirectory(FileUtil.class);
	}
}
