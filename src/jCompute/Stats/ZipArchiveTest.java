package jCompute.Stats;

import jCompute.util.JVMInfo;

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
		int BUFF_LEN = 1024 * 64;
		int FLUSH = BUFF_LEN * 1024 * 16;
		
		int combos = 200 * 200;
		int statfiles = 1;
		int averages = 20;
		
		int items = combos;
		int item_configs = items;
		int item_stats = items * statfiles;
		int item_realated_entries = item_configs + item_stats;
		int num_files = item_realated_entries * averages;
		
		byte[] data = new byte[DATA_LEN];
		
		// Fill with values
		for(int i = 0; i < DATA_LEN; i++)
		{
			data[i] = (byte) i;
		}
		
		BufferedOutputStream bos = null;
		try
		{
			JVMInfo jvmInfo = JVMInfo.getInstance();
			
			System.out.println("Max Heap Memory (MB)" + jvmInfo.getMaxMemory());
			System.out.println("Current Heap Memory (MB)" + jvmInfo.getTotalJVMMemory());
			System.out.println("Used Heap Memory (MB)" + jvmInfo.getUsedJVMMemory());
			
			FileOutputStream fo = new FileOutputStream("test.zip");
			FileDescriptor fd = fo.getFD();
			
			bos = new BufferedOutputStream(fo, BUFF_LEN);
			
			System.out.println("Creating Archive");
			
			ZipOutputStream zipOut = new ZipOutputStream(bos);
			zipOut.setMethod(ZipOutputStream.DEFLATED);
			zipOut.setLevel(1);
			
			zipOut.putNextEntry(new ZipEntry("Data/"));
			zipOut.closeEntry();
			
			System.out.println("Adding Files : " + num_files);
			
			System.out.println("%" + "\t\t\t\t" + "Files" + "\t\t\t\t\t\t" + "Bytes Compressed (MB)" + "\t\t\t" + "Heap Mem Used (MB)");
			for(int f = 0; f < num_files; f++)
			{
				// Entry start
				zipOut.putNextEntry(new ZipEntry("Data/" + f));
				
				// Data
				zipOut.write(data);
				
				// Entry end
				zipOut.closeEntry();
				
				total_bytes += data.length;
				
				bytes_wrote += data.length;
				
				if(bytes_wrote >= FLUSH)
				{
					zipOut.flush();
					bytes_wrote = 0;
				}
				
				if(f % item_realated_entries == 0 && (f != 0))
				{
					System.out.println(((float) f / num_files) + "\t\t\t\t" + f + "\t\t\t\t\t\t" + total_bytes / 1024 / 1024 + "\t\t\t\t\t" + jvmInfo.getUsedJVMMemory() + " :: "
							+ jvmInfo.getUsedJVMMemoryPercentage() + " / " + jvmInfo.getFreeJVMMemoryPercentage());
				}
				
			}
			zipOut.flush();
			fd.sync();
			zipOut.close();
			
			System.out.println("Buffer (MB): " + BUFF_LEN / 1024);
			System.out.println("Bytes : " + total_bytes / 1000 / 1000);
			System.out.println("Files : " + num_files);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
}
