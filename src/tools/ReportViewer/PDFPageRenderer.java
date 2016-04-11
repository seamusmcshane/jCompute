package tools.ReportViewer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import jCompute.gui.component.swing.swingworker.Loadable;

public class PDFPageRenderer implements Loadable
{
	private PDDocument document;
	private PDFRenderer ren;
	
	private final int PAGES;
	
	private int lastIndex;
	private HashMap<Integer, PageImage> pageCache;
	private int MAX = 1000;
	
	private float dpi = 96;
	
	public PDFPageRenderer(String filePath) throws IOException
	{
		document = PDDocument.load(new File(filePath));
		
		PAGES = document.getNumberOfPages();
		
		pageCache = new HashMap<Integer, PageImage>(MAX, 0.75f);
		
		ren = new PDFRenderer(document);
		
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
			page = new PageImage(ren.renderImageWithDPI(index, dpi));
			
			putCache(index, page);
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
	
	public int loadedPages()
	{
		return pageCache.size();
	}
	
	public int getNumPages()
	{
		return PAGES;
	}
	
	@Override
	public int[] getIndexes()
	{
		int limit = (PAGES > MAX) ? MAX : PAGES;
		
		int[] indexes = new int[limit];
		
		// page 1 should be already loaded (hence+1)
		for(int i = 0; i < limit - 1; i++)
		{
			indexes[i] = i + 1;
			
			System.out.println("getIndexes " + indexes[i]);
		}
		
		return indexes;
	}
	
	@Override
	public boolean load(int index)
	{
		try
		{
			System.out.println("Load " + index);
			
			putCache(index, new PageImage(ren.renderImageWithDPI(index, dpi)));
			
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}
	
	@Override
	public boolean makeDelegate(Loadable requester)
	{
		return false;
	}
	
	@Override
	public boolean delegateLoad(Loadable requester, String info)
	{
		return false;
	}
	
	@Override
	public void releaseDelegate(Loadable requester)
	{
		
	}
}
