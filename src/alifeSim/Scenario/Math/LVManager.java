package alifeSim.Scenario.Math;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Rectangle;

import alifeSim.Alife.GenericPlant.GenericPlant;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatInf;

public strictfp class LVManager
{
	/* Defaults */
	private double initial_prey_population = 800;
	private double initial_predator_population = 100;
		
	private double prey_growth = 0.5;
	private double predation_rate = 0.008;
	private double predator_death_rate = 0.8;
	private double predator_conversion_rate = 0.2;
	
	
	private double prey_population = initial_prey_population;
	private SingleStat stat_prey_population;	

	private double predator_population = initial_predator_population;
	private SingleStat stat_predator_population;

	private int t = 0;
	private int max_t = 10000;
	private int sub_steps = 256;
	private int draw_mod = 1;
	
	private double dt = 1/(double)sub_steps;
	
	private List<Point2D> values;
	
	public LVManager()
	{
		values = new LinkedList<Point2D>();
		
		setUpStats();
		
		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%d\t\t%5.2f\t\t%5.2f\n",t,initial_prey_population,initial_predator_population);
	}

	public void doStep()
	{
		if(t<max_t)
		{
			//predator_prey_rk4();
			
			predator_prey_euler();
									
			t++;
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
		g.clear();
		g.setAntiAlias(true);
		Point2D previous;
		
		float zoom = 1f;
		float xscale = 4f*zoom;
		float yscale = 1f*zoom;
		
		float ymax = 0f;
		float xmax = 0f;
		
		if(values!=null)
		{
			if(values.size() > 0)
			{
				previous = values.get(0);

				for (Point2D point : values) 
				{					
					g.setLineWidth(1f);
					
					g.setColor(new org.newdawn.slick.Color(255,255,255,16));
										
					float x = (float)point.getX();
					float y =(float)point.getY();
					
					g.draw(new Line((float)previous.getX()*xscale,(float)previous.getY()*yscale,x*xscale,y*yscale));
					
					if(x>xmax)
					{
						xmax = x;
					}
					
					if(y>ymax)
					{
						ymax = y;
					}

					previous = point;
					
				}
				
				g.setAntiAlias(false);
				
				g.setLineWidth(1f);
				g.setColor(new org.newdawn.slick.Color(255,255,255));

				g.draw(new Rectangle(0,0,xmax*xscale+10f,ymax*yscale+10f));
			
			}
		
		}
		
	}
	
}
