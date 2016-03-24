package tools.ReportGenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

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
	
	public PDFReport(String reportFileName, ArrayList<String> rowNames, ArrayList<String> colNames, Map<String, String> cells, String fullPath, float scale,
	int imageWidth, int imageHeight)
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
	
	public String generate() throws IOException
	{
		String filePath = fullPath + File.separator + reportFileName + ".pdf";
		
		PDDocument document = new PDDocument();
		
		addReportPage(reportFileName, document, rowNames, colNames, fullPath, scale, "Averages");
		addReportPage(reportFileName, document, rowNames, colNames, fullPath, scale, "Standard Deviations");
		addReportPage(reportFileName, document, rowNames, colNames, fullPath, scale, "Max");
		
		document.save(filePath);
		
		document.close();
		
		return filePath;
	}
	
	private void addReportPage(String reportFileName, PDDocument doc, ArrayList<String> rowNames, ArrayList<String> colNames, String fullPath, float scale,
	String pageTitle) throws IOException
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
		
		cos = new PDPageContentStream(doc, page);
		
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
				
				addImage(doc, page, cos, imagePath, imageWidth * colX + xMargin, imageHeight * rowY + yMargin + titleHeight, scale);
				addRectangle(doc, page, cos, imageWidth * colX + xMargin, imageHeight * rowY + yMargin + titleHeight, imageWidth * scale, imageHeight * scale,
				2f * scale);
				
				cos.beginText();
				cos.setFont(PDType1Font.HELVETICA, 12);
				cos.newLineAtOffset(10 + xMargin, pageBox.getHeight() - ((imageHeight * rowY) + 24 + yMargin + titleHeight));
				cos.showText(row);
				cos.endText();
				
				colX++;
			}
			rowY++;
		}
		
		float[] pos = addText(0, 0, reportFileName, doc, page, cos, PDType1Font.HELVETICA_BOLD, documentTitleSize);
		
		addText(0, pos[1], pageTitle, doc, page, cos, PDType1Font.HELVETICA_BOLD, pageTitleSize);
		
		cos.close();
		
	}
	
	/**
	 * Add text to a page and returns the width/height which can be used for offset calculation.
	 * @param xOffset
	 * @param yOffset
	 * @param text
	 * @param doc
	 * @param page
	 * @param cs
	 * @param font
	 * @param size
	 * @return
	 * @throws IOException
	 */
	private float[] addText(float xOffset, float yOffset, String text, PDDocument doc, PDPage page, PDPageContentStream cs, PDFont font, float size)
	throws IOException
	{
		cs.beginText();
		
		cs.setFont(font, size);
		
		float center = page.getTrimBox().getWidth() / 2;
		float stringWidth = ((font.getStringWidth(text) / 1000f) * size) / 2;
		float fontHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f) * size;
		
		System.out.println("center " + center);
		System.out.println("String " + stringWidth);
		
		cs.newLineAtOffset((center - stringWidth) + xOffset, (page.getTrimBox().getHeight() - fontHeight) - yOffset);
		cs.showText(text);
		
		cs.endText();
		
		return new float[]
		{
			stringWidth, fontHeight
		};
	}
	
	private void addImage(PDDocument doc, PDPage page, PDPageContentStream cs, String file, float x, float y, float scale) throws IOException
	{
		BufferedImage awtImage = ImageIO.read(new File(file));
		
		PDImageXObject ximage = LosslessFactory.createFromImage(doc, awtImage);
		
		float width = ximage.getWidth() * scale;
		float height = ximage.getHeight() * scale;
		
		cs.drawImage(ximage, x, -y + page.getMediaBox().getHeight() - height, width, height);
	}
	
	private void addRectangle(PDDocument doc, PDPage page, PDPageContentStream cs, float x, float y, float width, float height, float lineWidth)
	throws IOException
	{
		cs.setLineWidth(lineWidth);
		cs.addRect(x, -y + page.getMediaBox().getHeight() - height, width, height);
		cs.closeAndStroke();
	}
	
}
