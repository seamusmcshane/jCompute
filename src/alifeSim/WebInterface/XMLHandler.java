package alifeSim.WebInterface;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import alifeSim.Simulation.Simulation;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationsManager;
import alifeSim.Stats.StatManager;

public class XMLHandler extends AbstractHandler
{
	private SimulationsManager simsManager;
	private ServletResponse response;
	
	public XMLHandler(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{

		System.out.println(request.getPathInfo());
		
		String fileName = request.getPathInfo();
		
		if(fileName.equals("/SimulationsList.xml"))
		{
			simulationListXML(baseRequest,response);
		}
				
		return;		
	}
	
	private void simulationListXML(Request baseRequest,HttpServletResponse response)
	{
		response.setContentType("text/xml;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		
		List<Integer> simList = simsManager.getSimIdList();
		
		try
		{
		
			// Simulations Tag
			response.getWriter().print("<Simulations>");
		
				for(Integer id : simList)
				{
					
					String simStatus = simsManager.getSimStatus(id).toString();
					
					// Simulation Tag		
					response.getWriter().print("<Simulation>");
					
					// Sim Values
					response.getWriter().print("<ID>");response.getWriter().print(id);response.getWriter().print("</ID>");
					response.getWriter().print("<Status>");response.getWriter().print(simStatus);response.getWriter().print("</Status>");
					//response.getWriter().print("<TotalTime>");response.getWriter().print(formatTime(perfStats.getTotalTime()));response.getWriter().print("</TotalTime>");
					//response.getWriter().print("<Steps>");response.getWriter().print(perfStats.getSimulationSteps());response.getWriter().print("</Steps>");
					//response.getWriter().print("<AvgSPS>");response.getWriter().print(perfStats.getAverageStepRate());response.getWriter().print("</AvgSPS>");
					
					// End Simulation Tag
					response.getWriter().print("</Simulation>");
				}
		
			// End Simulations Tag
			response.getWriter().print("</Simulations>");
			response.getWriter().flush();
					
		}
		catch (IOException e)
		{
			System.out.println("IOException - : " + e.getStackTrace()[0].getMethodName());
		}
	}
	
	private String formatTime(long time)
	{
		time = time / 1000; // seconds
		int days = (int) (time / 86400); // to days
		int hrs = (int) (time / 3600) % 24; // to hrs
		int mins = (int) ((time / 60) % 60);	// to seconds
		int sec = (int) (time % 60);

		return String.format("%d:%02d:%02d:%02d", days, hrs, mins, sec);
	}
}
