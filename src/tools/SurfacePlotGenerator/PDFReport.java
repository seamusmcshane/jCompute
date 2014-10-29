package tools.SurfacePlotGenerator;

import jCompute.Thread.SimpleNamedThreadFactory;
import jCompute.util.FileUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

public class PDFReport
{
	private final static int imageWidth = 600;
	private final static int imageHeight = 400;

	private final static String itemLog = "ItemLog.xml";

	private final static ExecutorService imageExporter = Executors.newFixedThreadPool(Runtime.getRuntime()
			.availableProcessors(), new SimpleNamedThreadFactory("Image Exporter"));

	private final static ArrayList<String> rowNames = new ArrayList<String>();
	private final static ArrayList<String> colNames = new ArrayList<String>();

	public static void main(String args[])
	{
		final JFileChooser filechooser = new JFileChooser(new File("\\\\Nanoserv\\results\\ViewRange"));

		filechooser.setDialogTitle("Choose Directory");
		filechooser.setMultiSelectionEnabled(false);
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int val = filechooser.showOpenDialog(filechooser);

		float scale = 1f;
		
		if(val == JFileChooser.APPROVE_OPTION)
		{
			String fullPath = filechooser.getSelectedFile().getPath();
			System.out.println("Path : " + fullPath);

			// Level 0
			String documentName = filechooser.getSelectedFile().getName();
			System.out.println("Document Name will be : " + documentName);

			generateMap(rowNames, colNames, fullPath, itemLog);

			generateImages(rowNames,colNames,fullPath,itemLog);

			try
			{
				imageExporter.shutdown();
				imageExporter.awaitTermination(1, TimeUnit.MINUTES);
				System.out.println("Images Finished");
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			generateReport(documentName,rowNames, colNames, fullPath, scale);
			
		}

		System.out.println("Report Finished");
		System.exit(0);

	}

	private static void generateReport(String documentName,ArrayList<String> rowNames, ArrayList<String> colNames, String fullPath, float scale)
	{
		PDDocument document = new PDDocument();
		
		addReportPage(documentName,document, rowNames, colNames, fullPath, scale, "Averages");
		addReportPage(documentName,document, rowNames, colNames, fullPath, scale, "Standard Deviations");

		try
		{
			document.save(fullPath + File.separator + documentName+".pdf");
			document.close();
		}
		catch(IOException | COSVisitorException e)
		{
			e.printStackTrace();
		}

	}

	private static void addReportPage(String documentName,PDDocument doc,ArrayList<String> rowNames, ArrayList<String> colNames, String fullPath, float scale, String pageTitle)
	{
		int colReal = colNames.size() / rowNames.size();
		
		int documentTitleSize = 32;
		int pageTitleSize = 18;
		
		float titleHeight = documentTitleSize+pageTitleSize;
		float xMargin = 50;
		float yMargin = 50;
		
		float pageWidth = (imageWidth*scale) * colReal + xMargin*2;
		float pageHeight = (imageHeight*scale) * rowNames.size() + yMargin*2 + titleHeight;
		
		String imagesSuffix = "";
		
		if(!pageTitle.equals("Averages"))
		{
			imagesSuffix ="-standard-deviation";
		}

		System.out.println("Page Width : " + pageWidth);
		System.out.println("Page Height : " + pageHeight);

		PDRectangle pageBox = new PDRectangle(pageWidth, pageHeight);

		PDPage page = new PDPage(pageBox);

		doc.addPage(page);
		
		PDPageContentStream cos = null;
				
		try
		{
			cos = new PDPageContentStream(doc, page);
		}
		catch(IOException e1)
		{
			e1.printStackTrace();
		}
		
		int rowY = 0;
		for(String row : rowNames)
		{
			int colX = 0;

			for(String col : colNames)
			{
				if(colX >= colReal)
				{
					break;
				}
				
				String path = fullPath + File.separator + row + File.separator + col;
				System.out.println("Path : " + path);

				String imagesPath = fullPath + File.separator + "images";
				String exportPath = imagesPath + File.separator + row;

				String imageName = path.substring(path.lastIndexOf(']') + 2, path.length());

				System.out.println("imagePath : " + imagesPath);
				System.out.println("exportPath : " + exportPath);
				System.out.println("imageName : " + imageName);

				String imagePath = exportPath + File.separator + imageName+imagesSuffix + ".png";

				System.out.println(imagePath);

				try
				{
					addImage(doc, page, cos, imagePath, imageWidth * colX + xMargin, imageHeight * rowY + yMargin+titleHeight, scale);
					cos.beginText();
					cos.setFont(PDType1Font.HELVETICA, 12);
					cos.moveTextPositionByAmount(10 + xMargin, pageBox.getHeight() - ((imageHeight * rowY)+24+yMargin+titleHeight));
					cos.drawString(row);
					cos.endText();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				
				colX++;
			}
			rowY++;
		}
		
		try
		{
			cos.beginText();
						
			cos.setFont(PDType1Font.HELVETICA_BOLD, documentTitleSize);
			cos.moveTextPositionByAmount(pageBox.getWidth()/2-(documentName.length()*PDType1Font.HELVETICA_BOLD.getFontWidth(24))/2, pageBox.getHeight() - documentTitleSize);
			cos.drawString(documentName);
			
			cos.endText();
			
			cos.beginText();

			cos.setFont(PDType1Font.HELVETICA_BOLD, pageTitleSize);
			cos.moveTextPositionByAmount(pageBox.getWidth()/2-(documentName.length()*PDType1Font.HELVETICA_BOLD.getFontWidth(18))/2, pageBox.getHeight() - documentTitleSize-pageTitleSize);
			cos.drawString(pageTitle);
			
			cos.endText();

			cos.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

	private static void addImage(PDDocument doc, PDPage page, PDPageContentStream cs, String file, float x, float y,
			float scale) throws IOException
	{
		BufferedImage awtImage = ImageIO.read(new File(file));

		PDXObjectImage ximage = new PDPixelMap(doc, awtImage);

		float width = ximage.getWidth() * scale;
		float height = ximage.getHeight() * scale;

		cs.drawXObject(ximage, x, -y + page.getMediaBox().getHeight() - height, width, height);
	}

	private static void addRectangle(PDDocument doc, PDPage page, PDPageContentStream cs, float x, float y,
			float width, float height, float lineWidth) throws IOException
	{
		cs.setLineWidth(lineWidth);
		cs.addRect(x, -y + page.getMediaBox().getHeight() - height, width, height);
		cs.closeAndStroke();
	}

	/**
	 * Transverse the path and generate images for the itemLogs found
	 * 
	 * @param colNames
	 * @param rowNames
	 * @param fullPath
	 * @param itemLog
	 */
	private static void generateMap(ArrayList<String> rowNames, ArrayList<String> colNames, String fullPath,
			String itemLog)
	{
		// Level 1
		String level1dirs[] = FileUtil.getDirectoriesInDir(fullPath);

		if(FileUtil.dirContainsFileNamed(fullPath + File.separator + level1dirs[0], itemLog))
		{
			// A single directory
		}
		else
		{
			for(String level1dir : level1dirs)
			{
				if(!level1dir.equals("images"))
				{

					String level1Path = fullPath + File.separator + level1dir;
					System.out.println("l1 Dir : " + level1dir);
					rowNames.add(level1dir);

					if(FileUtil.dirContainsFileNamed(level1Path, itemLog))
					{
						// A directory with 1 level of groups
					}
					else
					{
						// A directory with 2 levels of groups
						String level2dirs[] = FileUtil.getDirectoriesInDir(level1Path);

						for(String level2dir : level2dirs)
						{
							String level2Path = fullPath + File.separator + level1dir + File.separator + level2dir;

							if(FileUtil.dirContainsFileNamed(level2Path, itemLog))
							{
								System.out.println("L2 Dir : " + level2dir);
								colNames.add(level2dir);
							}

						}

					}
				}

			}

		}
	}

	/**
	 * Transverse the path and generate images for the itemLogs found
	 * 
	 * @param colNames
	 * @param rowNames
	 * @param fullPath
	 * @param itemLog
	 */
	private static void generateImages(ArrayList<String> rowNames, ArrayList<String> colNames, String fullPath,
			String itemLog)
	{
		for(String row : rowNames)
		{
			for(String col : colNames)
			{
				String path = fullPath + File.separator + row + File.separator + col;
				System.out.println("Path : " + path);

				if(FileUtil.dirContainsFileNamed(path, itemLog))
				{
					String imagePath = fullPath + File.separator + "images";
					String exportPath = imagePath + File.separator + row;
					String logPath = path + File.separator + itemLog;

					String imageName = path.substring(path.lastIndexOf(']') + 2, path.length());

					System.out.println("imagePath : " + imagePath);
					System.out.println("exportPath : " + exportPath);
					System.out.println("logPath : " + logPath);
					System.out.println("imageName : " + imageName);

					FileUtil.createDirIfNotExist(imagePath);
					FileUtil.createDirIfNotExist(exportPath);

					imageExporter.submit(new ImageExporter(imageWidth, imageHeight, logPath, 0, exportPath, imageName));
					imageExporter.submit(new ImageExporter(imageWidth, imageHeight, logPath, 1, exportPath, imageName
							+ "-standard-deviation"));

				}
			}
		}
	}

	public static class ImageExporter implements Runnable
	{
		private int width;
		private int height;
		private String sourceLog;
		private int mode;
		private String exportPath;
		private String imageName;

		public ImageExporter(int width, int height, String sourceLog, int mode, String exportPath, String imageName)
		{
			super();
			this.width = width;
			this.height = height;
			this.sourceLog = sourceLog;
			this.mode = mode;
			this.exportPath = exportPath;
			this.imageName = imageName;
		}

		@Override
		public void run()
		{
			ChartUtil.ExportSurfacePlot(width, height, sourceLog, mode, exportPath, imageName);
		}

	}

}
