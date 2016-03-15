package tools.ReportGenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;

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
	// Report Name
	private String reportFileName;
	
	// Row/Column Names
	private ArrayList<String> rowNames;
	private ArrayList<String> colNames;
	
	// Cells
	private Map<String, String> cells;
	
	private String fullPath;
	private float scale;
	
	private int imageWidth;
	private int imageHeight;
	
	public PDFReport(String reportFileName, ArrayList<String> rowNames, ArrayList<String> colNames, Map<String, String> cells, String fullPath, float scale, int imageWidth, int imageHeight)
	{
		super();
		this.reportFileName = reportFileName;
		this.rowNames = rowNames;
		this.colNames = colNames;
		this.cells = cells;
		
		this.fullPath = fullPath;
		this.scale = scale;
		
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}
	
	public void generate()
	{
		PDDocument document = new PDDocument();
		
		addReportPage(reportFileName, document, rowNames, colNames, fullPath, scale, "Averages");
		addReportPage(reportFileName, document, rowNames, colNames, fullPath, scale, "Standard Deviations");
		addReportPage(reportFileName, document, rowNames, colNames, fullPath, scale, "Max");
		
		try
		{
			document.save(fullPath + File.separator + reportFileName + ".pdf");
			document.close();
		}
		catch(IOException | COSVisitorException e)
		{
			e.printStackTrace();
		}
	}
	
	private void addReportPage(String reportFileName, PDDocument doc, ArrayList<String> rowNames, ArrayList<String> colNames, String fullPath, float scale, String pageTitle)
	{
		int documentTitleSize = 32;
		int pageTitleSize = 18;
		
		float titleHeight = documentTitleSize + pageTitleSize;
		float xMargin = 50;
		float yMargin = 50;
		
		float pageWidth = (imageWidth * scale) * colNames.size() + xMargin * 2;
		float pageHeight = (imageHeight * scale) * rowNames.size() + yMargin * 2 + titleHeight;
		
		String imagesSuffix = "";
		
		if(pageTitle.equals("Averages"))
		{
			imagesSuffix = "-avg";
		}
		else if(pageTitle.equals("Standard Deviations"))
		{
			imagesSuffix = "-standard-deviation";
		}
		else
		{
			imagesSuffix = "-max";
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
				String path = fullPath + File.separator + row + File.separator + col;
				System.out.println("Path : " + path);
				
				String imagesPath = fullPath + File.separator + "images";
				String exportPath = imagesPath + File.separator + row;
				
				String logDir = cells.get(row + col);
				String imageName = logDir.substring(logDir.lastIndexOf(']') + 2, logDir.length());
				
				System.out.println("imagePath : " + imagesPath);
				System.out.println("exportPath : " + exportPath);
				System.out.println("imageName : " + imageName);
				
				String imagePath = exportPath + File.separator + imageName + imagesSuffix + ".png";
				
				System.out.println(imagePath);
				
				try
				{
					addImage(doc, page, cos, imagePath, imageWidth * colX + xMargin, imageHeight * rowY + yMargin + titleHeight, scale);
					cos.beginText();
					cos.setFont(PDType1Font.HELVETICA, 12);
					cos.moveTextPositionByAmount(10 + xMargin, pageBox.getHeight() - ((imageHeight * rowY) + 24 + yMargin + titleHeight));
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
			cos.moveTextPositionByAmount(pageBox.getWidth() / 2 - (reportFileName.length() * PDType1Font.HELVETICA_BOLD.getFontWidth(24)) / 2, pageBox.getHeight() - documentTitleSize);
			cos.drawString(reportFileName);
			
			cos.endText();
			
			cos.beginText();
			
			cos.setFont(PDType1Font.HELVETICA_BOLD, pageTitleSize);
			cos.moveTextPositionByAmount(pageBox.getWidth() / 2 - (reportFileName.length() * PDType1Font.HELVETICA_BOLD.getFontWidth(18)) / 2, pageBox.getHeight() - documentTitleSize - pageTitleSize);
			cos.drawString(pageTitle);
			
			cos.endText();
			
			cos.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void addImage(PDDocument doc, PDPage page, PDPageContentStream cs, String file, float x, float y, float scale) throws IOException
	{
		BufferedImage awtImage = ImageIO.read(new File(file));
		
		PDXObjectImage ximage = new PDPixelMap(doc, awtImage);
		
		float width = ximage.getWidth() * scale;
		float height = ximage.getHeight() * scale;
		
		cs.drawXObject(ximage, x, -y + page.getMediaBox().getHeight() - height, width, height);
	}
	
	private void addRectangle(PDDocument doc, PDPage page, PDPageContentStream cs, float x, float y, float width, float height, float lineWidth) throws IOException
	{
		cs.setLineWidth(lineWidth);
		cs.addRect(x, -y + page.getMediaBox().getHeight() - height, width, height);
		cs.closeAndStroke();
	}
	
}
