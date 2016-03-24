package tools.ReportViewer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PDFPageRenderer
{
	private PDDocument document;
	private PDFRenderer ren;

	private final int PAGES;

	private int lastIndex;
	private HashMap<Integer, PageImage> pageCache;
	private int MAX = 16;

	private float dpi = 200;

	public PDFPageRenderer(String filePath) throws IOException
	{
		document = PDDocument.load(new File(filePath));

		PAGES = document.getNumberOfPages();

		pageCache = new HashMap<Integer, PageImage>(MAX, 0.75f);

		ren = new PDFRenderer(document);

		int limit = (PAGES > MAX) ? MAX : PAGES;

		for(int pIndex = 0; pIndex < limit; pIndex++)
		{
			putCache(pIndex, new PageImage(ren.renderImageWithDPI(pIndex, dpi)));
		}

		lastIndex = 0;
	}

	private void putCache(int index, PageImage pageImage)
	{
		pageCache.put(index, pageImage);
	}

	private PageImage checkCache(int index) throws IOException
	{
		return pageCache.get(index);
	}

	public PageImage getPageImage(int pageIndex, int size) throws IOException
	{
		int index = pageIndex;

		if(index < 0)
		{
			index = 0;
		}

		if(index >= PAGES)
		{
			index = PAGES - 1;
		}

		PageImage page = checkCache(index);

		// We have to load this page - TODO remove a page
		if(page == null)
		{
			putCache(index, new PageImage(ren.renderImageWithDPI(index, dpi)));
		}

		lastIndex = index;

		System.out.println("Index " + index);
		return page;
	}

	public void close() throws IOException
	{
		document.close();
	}

	public PageImage getImageNext() throws IOException
	{
		return getPageImage(lastIndex + 1, 0);
	}

	public PageImage getImagePrev() throws IOException
	{
		return getPageImage(lastIndex - 1, 0);
	}
}
