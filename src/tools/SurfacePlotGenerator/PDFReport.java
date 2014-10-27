package tools.SurfacePlotGenerator;

import jCompute.Thread.SimpleNamedThreadFactory;
import jCompute.util.FileUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

public class PDFReport
{
	private static ExecutorService imageExporter = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new SimpleNamedThreadFactory("Image Exporter"));

	public static void main(String args[])
	{
		String itemLog = "ItemLog.xml";
		
		final JFileChooser filechooser = new JFileChooser(new File("\\\\Nanoserv\\results\\ViewRange"));
		
		filechooser.setDialogTitle("Choose Directory");
		filechooser.setMultiSelectionEnabled(false);
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int val = filechooser.showOpenDialog(filechooser);
		
		if (val == JFileChooser.APPROVE_OPTION)
		{
			String fullPath = filechooser.getSelectedFile().getPath();
			System.out.println("Path : " + fullPath);
						
			// Level 0
			String documentName = filechooser.getSelectedFile().getName();
			System.out.println("Document Name will be : " + documentName);
			
			generateImages(fullPath,itemLog);
			
		}
		
		try
		{
			imageExporter.shutdown();
			imageExporter.awaitTermination(1, TimeUnit.MINUTES);
			System.out.println("Finished");
			System.exit(0);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		
	}
		
	private static void createDocument()
	{
		PDFont fontPlain = PDType1Font.HELVETICA;

		PDDocument document = new PDDocument();
		PDPage page = new PDPage(PDPage.PAGE_SIZE_A3);
		page.setRotation(90);
		PDRectangle pageBox = page.getMediaBox();

		document.addPage(page);

		PDPageContentStream cos;

		try
		{
			cos = new PDPageContentStream(document, page);
			cos.concatenate2CTM(0, 1, -1, 0, pageBox.getHeight(), 0);
			
			cos.beginText();
			cos.setFont(fontPlain, 12);
			cos.moveTextPositionByAmount(0, pageBox.getHeight() - 10);
			cos.drawString("Hello World");
			cos.endText();

			addImage(document,page,cos,"test.png",10,100,0.5f);
			
			addRectangle(document,page,cos,10,100,300,300,1f);
			cos.close();

			document.save("test.pdf");
			document.close();

		}
		catch(IOException | COSVisitorException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void addImage(PDDocument doc,PDPage page, PDPageContentStream cs, String file, float x, float y, float scale) throws IOException
	{
        
		BufferedImage awtImage = ImageIO.read(new File(file));
				
		PDXObjectImage ximage = new PDPixelMap(doc, awtImage);
		
		float width = ximage.getWidth() * scale;
		float height = ximage.getHeight() * scale;
		
		cs.drawXObject(ximage, x, -y+page.getMediaBox().getHeight()-height,  width, height );		

	}
	
	private static void addRectangle(PDDocument doc,PDPage page, PDPageContentStream cs, float x, float y, float width, float height, float lineWidth) throws IOException
	{
        cs.setLineWidth(lineWidth);
        cs.addRect(x, -y+page.getMediaBox().getHeight()-height , width, height);
        cs.closeAndStroke();
	}
	
	/**
	 * Transverse the path and generate images for the itemLogs found
	 * @param fullPath
	 * @param itemLog
	 */
	private static void generateImages(String fullPath, String itemLog)
	{
		// Level 1
		String level1dirs[] = FileUtil.getDirectoriesInDir(fullPath);		
		
		if(FileUtil.dirContainsFileNamed(fullPath + File.separator + level1dirs[0],itemLog))
		{
			// A single directory
		}
		else
		{
			for(String level1dir : level1dirs)
			{
				String level1Path = fullPath + File.separator +level1dir;
				System.out.println("L1 Path : " + level1Path);
				
				if(FileUtil.dirContainsFileNamed(level1Path,itemLog))
				{
					// A directory with 1 level of groups
					
				}
				else
				{
					// A directory with 2 levels of groups
					String level2dirs[] = FileUtil.getDirectoriesInDir(level1Path);
					
					for(String level2dir : level2dirs)
					{
						
						String level2Path = level1Path + File.separator +level2dir;
						System.out.println("L2 Path : " + level2Path);
						
						System.out.println(level2Path);

						if(FileUtil.dirContainsFileNamed(level2Path,itemLog))
						{
							String imagePath = fullPath + File.separator + "images";
							String exportPath = imagePath + File.separator + level1dir;
							String logPath = level2Path + File.separator + itemLog;
							
							String imageName = level2dir.substring(level2dir.lastIndexOf(']')+1, level2dir.length());
							
							FileUtil.createDirIfNotExist(imagePath);
							FileUtil.createDirIfNotExist(exportPath);
							
							imageExporter.submit(new ImageExporter(logPath, 0, exportPath, imageName));			
							imageExporter.submit(new ImageExporter(logPath, 1, exportPath, imageName+"-standard-deviation"));			

						}
						
					}
					
				}
				
			}
		
		}
	}
	
	public static class ImageExporter implements Runnable
	{
		private String sourceLog;
		private int mode;
		private String exportPath;
		private String imageName;

		public ImageExporter(String sourceLog, int mode, String exportPath, String imageName)
		{
			super();
			this.sourceLog = sourceLog;
			this.mode = mode;
			this.exportPath = exportPath;
			this.imageName = imageName;
		}

		@Override
		public void run()
		{
			ChartUtil.ExportSurfacePlot(sourceLog, mode, exportPath, imageName);			
		}
		
	}
	
}
