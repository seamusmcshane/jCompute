package jcompute.webinterface;


import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import jcompute.cluster.batchmanager.BatchManager;

public class TestHandler extends AbstractHandler
{
	private BatchManager bm;
	private ServletResponse response;
	
	public TestHandler(BatchManager bm)
	{
		this.bm = bm;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		
		response.getWriter().println("<h1>"+"Batch List"+"</h1>");		
					
		String[] list = bm.getBatchList();		

		
		for(String string : list)
		{
			System.out.println(string);
			
			response.getWriter().println(string);
		}
	}
}
