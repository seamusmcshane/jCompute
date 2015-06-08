package jCompute.Stats;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchiveTest
{
	public static void main(String args[])
	{
		long bytes_wrote = 0;
		long total_bytes = 0;
		int DATA_LEN = 512;
		int BUFF_LEN = 1024;
		int FLUSH = BUFF_LEN*1024*16;
		
		int size = 15000;
		int num_files = 1000*size;
		
		byte[] data = new byte[DATA_LEN];
		
		// Fill with values
		for(int i=0;i<DATA_LEN;i++)
		{
			data[i]=(byte)i;
		}
		
		BufferedOutputStream bos = null;
		try
		{
			FileOutputStream fo = new FileOutputStream("test.zip");
			FileDescriptor fd = fo.getFD();
			
			bos = new BufferedOutputStream(fo,
					BUFF_LEN);
			
			System.out.println("Creating Archive");
			
			ZipOutputStream zipOut = new ZipOutputStream(bos);
			zipOut.setMethod(ZipOutputStream.DEFLATED);
			zipOut.setLevel(1);
			
			zipOut.putNextEntry(new ZipEntry("Data/"));
			zipOut.closeEntry();
			
			System.out.println("Adding Files");
			
			for(int f=0;f<num_files;f++)
			{
				// Entry start
				zipOut.putNextEntry(new ZipEntry("Data/"+f));
				
				// Data
				zipOut.write(data);
				
				// Entry end
				zipOut.closeEntry();

				total_bytes+=data.length;
				
				bytes_wrote+=data.length;
				
				if(bytes_wrote >= FLUSH)
				{
					zipOut.flush();
					bytes_wrote=0;
				}
				
			}
			
			zipOut.flush();
			fd.sync();
			zipOut.close();
			
			System.out.println("Sync Buffer (MB): " + BUFF_LEN/1024/1024);
			System.out.println("Bytes : " + total_bytes/1000/1000);
			System.out.println("Files : " + num_files);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
}
