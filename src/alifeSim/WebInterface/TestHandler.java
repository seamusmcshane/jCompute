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
import alifeSim.Simulation.SimulationPerformanceStats;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationsManager;
import alifeSim.Stats.StatManager;

public class TestHandler extends AbstractHandler
{
	private SimulationsManager simsManager;
	private ServletResponse response;
	
	public TestHandler(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		List<Integer> simList = simsManager.getSimIdList();
				
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		
		response.getWriter().println("<h1>"+"Simulations"+"</h1>");		
					
		
		for(Integer id : simList)
		{
			
			SimulationPerformanceStats perfStats = simsManager.getSimPerformanceStats(id);
			
			response.getWriter().println("Simulation : " + id);
			response.getWriter().println("State		 : " + simsManager.getSimState(id).toString());
			response.getWriter().println("Total Time : " + formatTime(perfStats.getTotalTime()));
			response.getWriter().println("Steps      : " + perfStats.getSimulationSteps());
			response.getWriter().println("AvgSPS     : " + perfStats.getAverageStepRate());		
			response.getWriter().println("</br>");
		}
		
		return;		
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
