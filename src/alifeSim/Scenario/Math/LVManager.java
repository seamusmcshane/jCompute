package alifeSim.Scenario.Math;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

import alifeSim.Alife.GenericPlant.GenericPlant;
import alifeSim.Simulation.Simulation;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatInf;

public class LVManager
{
	/* Defaults */
	private double initial_prey_population;
	private double initial_predator_population;
		
	private double prey_growth;
	private double predation_rate;
	private double predator_death_rate;
	private double predator_conversion_rate;
		
	private double prey_population;
	private SingleStat stat_prey_population;	

	private double predator_population;
	private SingleStat stat_predator_population;

	private int t = 0;
	//private int max_t = 10000;
	
	/* 0 - Euler, 1 - RK4 */
	private int intMethod; 
	private int sub_steps;
	
	// Ratio of SubSteps to draw (1=all)
	private int draw_mod = 1;
	
	private double dt;
	
	private List<Point2D> values;
	
	/** Off screen buffer */
	private Graphics bufferGraphics;
	private Image buffer;
	Point2D previous;
	
	private boolean resize = true;
	
	/* This will be too large on old graphics hardware */
	private float bufferWidth = 2048;
	private float bufferHeight = 2048;
	
	private float pointsHue=0f;
	
	public LVManager(LVSettings settings)
	{
		values = new LinkedList<Point2D>();
		
		initial_prey_population = settings.getInitialPreyPopulation();
		initial_predator_population = settings.getInitialPredatorPopulation();
		
		prey_population = initial_prey_population;
		predator_population = initial_predator_population;
		
		prey_growth = settings.getPreyGrowth();
		predation_rate = settings.getPredationRate();
		predator_death_rate = settings.getPredatorDeathRate();
		predator_conversion_rate = settings.getPredatorConversionRate();
		
		sub_steps = settings.getSubSteps();
		
		dt = 1/(double)sub_steps;
		
		intMethod = setIntMethod(settings.getIntMethod());
		
		setUpStats();
		
		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%d\t\t%5.2f\t\t%5.2f\n",t,initial_prey_population,initial_predator_population);
	}

	private int setIntMethod(String settingValue)
	{
		if(settingValue.equalsIgnoreCase("Euler"))
		{
			return 0;
		}
		else if(settingValue.equalsIgnoreCase("RK4"))
		{
			return 1;
		}
		
		System.out.println("INVALID INT METHOD in Settings - defaulted to Euler");
		
		return 0;
	}
	
	public void doStep()
	{
		//if(t<max_t)
		{
			switch(intMethod)
			{
				case 0:
					predator_prey_euler();
				break;
				case 1:
					predator_prey_rk4();
				break;
			}
				
			t++;
		}
		
		pointsHue=pointsHue+0.001f;
		
		if(pointsHue>=1f)
		{
			pointsHue=0;
		}
		
	}
	
	public void predator_prey_euler()
	{				
		
		double dx;
		double dy;
		
		for(int i=1;i<sub_steps;i++)
		{			
			/* Prey */
			dx = calculate_prey(prey_population,predator_population);
			
			/* Predator */
			dy = calculate_predator(prey_population,predator_population);
			
			prey_population += dx*dt;
			predator_population += dy*dt;
			
			// Stats
			stat_predator_population.addSample(predator_population);
			stat_prey_population.addSample(prey_population);
			
			// Draw 
			addDrawVal(i);			
		}
	}
	
	public void predator_prey_rk4()
	{

		double pdk1;
		double pdk2;
		double pdk3;
		double pdk4;
		
		double pyk1;
		double pyk2;
		double pyk3;
		double pyk4;

		for(int i=1;i<sub_steps;i++)
		{			
			// K1
			pdk1 = calculate_predator(prey_population,predator_population);
			pyk1 = calculate_prey(prey_population,predator_population);
			
			// K2 - calculate a half step based on k1 delta
			pdk2 = calculate_predator(prey_population+(pyk1*0.5*dt),predator_population+(pdk1*0.5*dt));
			pyk2 = calculate_prey(prey_population+(pyk1*0.5*dt),predator_population+(pdk1*0.5*dt));
			
			// K3+ (t*0.5) - calculate again based on k2 delta
			pdk3 = calculate_predator(prey_population+(pyk2*0.5*dt),predator_population+(pdk2*0.5*dt));
			pyk3 = calculate_prey(prey_population+(pyk2*0.5*dt),predator_population+(pdk2*0.5*dt));
			
			// K4+ (t*0.5) - calculate full step based on k3 delta
			pdk4 = calculate_predator(prey_population+pyk3*dt,predator_population+pdk3*dt);
			pyk4 = calculate_prey(prey_population+pyk3*dt,predator_population+pdk3*dt);
			 
			predator_population		+= (pdk1 + (2.0 * pdk2) + (2.0 * pdk3) + pdk4)*(1.0/6.0)*dt;
			prey_population			+= (pyk1 + (2.0 * pyk2) + (2.0 * pyk3) + pyk4)*(1.0/6.0)*dt;
			
			// Stats
			stat_predator_population.addSample(predator_population);
			stat_prey_population.addSample(prey_population);
			
			// Draw 
			addDrawVal(i);
			
		}
		
	}
	
	private void addDrawVal(int i)
	{
		if(i%draw_mod == 0)
		{
			// Draw 
			values.add(new Point2D.Double(predator_population,prey_population));			
		}

	}
	
	/* LOTKA-VOLTERA - PREY */
	private double calculate_prey(double prey_population,double predator_population)
	{
		return prey_growth*prey_population - predation_rate*prey_population*predator_population;
	}
	
	/* LOTKA-VOLTERA - PREDATOR */
	private double calculate_predator(double prey_population,double predator_population)
	{
		return predator_conversion_rate*predation_rate*prey_population*predator_population-predator_death_rate*predator_population; 
	}
	
	private void setUpStats()
	{
		stat_predator_population = new SingleStat("Predator");
		stat_predator_population.setColor(Color.red);
		
		stat_prey_population = new SingleStat("Prey");
		stat_prey_population.setColor(Color.blue);
	}
	
	public List<StatInf> getPopulationStats()
	{

		List<StatInf> stat = new LinkedList<StatInf>();
		
		stat.add(stat_predator_population);
		stat.add(stat_prey_population);
		
		return stat;
	}
	
	public void drawLV(Graphics g)
	{	
		if(resize==true)
		{
			setUpImageBuffer();
			resize=false;
		}
		g.setWorldClip(null);
		
		drawPoints(bufferGraphics);
		
		g.draw(new Rectangle(0,0,bufferWidth,bufferHeight));
		
		g.drawImage(buffer, 0, 0);
	}
	
	private void drawPoints(Graphics g)
	{
		//g.clear();
		
		g.setAntiAlias(true);
		
		float zoom = 1f;
		float xscale = 4f*zoom;
		float yscale = 1f*zoom;
		
		float xmax = 0;
		float ymax = 0;
		
		if(values!=null)
		{
			if(values.size() > 0)
			{
				if(previous==null)
				{
					previous = values.get(0);
				}

				for (Point2D point : values) 
				{					
					g.setLineWidth(2f);
											
					g.setColor(new org.newdawn.slick.Color(Color.HSBtoRGB(pointsHue,1f,1f)));
										
					float x = (float)point.getX();
					float y =(float)point.getY();

					if(x>xmax)
					{
						xmax = x;
					}
					
					if(y>ymax)
					{
						ymax = y;
					}
										
					g.draw(new Line((float)previous.getX()*xscale,(float)previous.getY()*yscale,x*xscale,y*yscale));
					
					previous = point;
					
				}
				
				values = new LinkedList<Point2D>();
				
			}
		}
	}
	
	/** Sets up the off-screen buffer image */
	private void setUpImageBuffer()
	{
		try
		{			
			buffer = new Image((int)bufferWidth, (int)bufferHeight);
			
			bufferGraphics = buffer.getGraphics();

		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
	}
	
}
