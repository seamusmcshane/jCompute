package alifeSim.Scenario.Math;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Pixmap;

import alifeSim.Gui.NewSimView;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatInf;
import alifeSimGeom.A2RGBA;
import alifeSimGeom.A3DVector3f;

public class LVThreeSpeciesManager
{
	/* Initial */
	private double initial_prey_population;
	private double initial_predator_population;
	private double initial_plant_population;
		
	/* Constants */
	private double predator_death_rate;
	private double predator_conversion_rate;
	
	private double prey_growth_rate;
	private double predation_rate;
	private double prey_plant_consumption_rate;
	private double prey_death_rate;
	
	private double plant_growth_rate;
	
	/* Supported Stats */
	private double predator_population;
	private SingleStat stat_predator_population;
	
	private double prey_population;
	private SingleStat stat_prey_population;	

	private double plant_population;
	private SingleStat stat_plant_population;

	private int t = 0;
	//private int max_t = 10000;
	
	/* 0 - Euler, 1 - RK4 */
	private int intMethod; 
	private int sub_steps;
	
	// Ratio of SubSteps to draw (1=all)
	private int draw_mod = 1;
	
	private double dt;
	
	private List<A3DVector3f> values;
	
	/* Previous Point */
	A3DVector3f previous;
	
	private boolean resize = true;
	
	/* This will be too large on old graphics hardware */
	private int bufferWidth = 2048;
	private int bufferHeight = 2048;
	
	Pixmap pixmap = new Pixmap(bufferWidth,bufferHeight, Pixmap.Format.RGBA8888);
	
	private float pointsHue=0f;
	
	private double scale;
	
	public LVThreeSpeciesManager(LVSettings settings)
	{
		values = new LinkedList<A3DVector3f>();
		
		scale = settings.getViewScale();
		
		initial_prey_population = settings.getInitialPreyPopulation();
		initial_predator_population = settings.getInitialPredatorPopulation();
		
		prey_population = initial_prey_population;
		predator_population = initial_predator_population;
		
		prey_growth_rate = settings.getPreyGrowth();
		predation_rate = settings.getPredationRate();
		predator_death_rate = settings.getPredatorDeathRate();
		predator_conversion_rate = settings.getPredatorConversionRate();
		
		sub_steps = settings.getSubSteps();
		
		dt = 1/(double)sub_steps;
		
		intMethod = setIntMethod(settings.getIntMethod());
		
		setUpStats();
		
		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%d\t\t%5.2f\t\t%5.2f\n",t,initial_prey_population,initial_predator_population);
		
        pixmap.setColor(0,0,0,0);
        pixmap.fill();
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
				/*case 1:
					predator_prey_rk4();
				break;*/
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
		double dz;
		
		for(int i=1;i<sub_steps;i++)
		{			
			/* Prey */
			dx = calculate_prey(predator_population,prey_population,plant_population);
			
			/* Predator */
			dy = calculate_predator(predator_population,prey_population,plant_population);
			
			dz = calculate_plants(predator_population,prey_population,plant_population);
			
			prey_population += dx*dt;
			predator_population += dy*dt;
			plant_population += dz*dt;
			
			// Stats
			stat_predator_population.addSample(predator_population);
			stat_prey_population.addSample(prey_population);
			stat_plant_population.addSample(plant_population);
			
			// Draw 
			addDrawVal(i);			
		}
	}
	
	/*public void predator_prey_rk4()
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
		
	}*/
	
	private void addDrawVal(int i)
	{
		if(i%draw_mod == 0)
		{
			// Draw 
			values.add(new A3DVector3f(predator_population,prey_population,plant_population));			
		}

	}
		

	
	/* Modified LOTKA-VOLTERA - PREDATOR */
	private double calculate_predator(double predator_population,double prey_population,double plant_population)
	{
		return (predator_conversion_rate*prey_population*predator_population) - (predator_death_rate*predator_population); 
	}
	
	/* Modified LOTKA-VOLTERA - PREY */
	private double calculate_prey(double predator_population,double prey_population,double plant_population)
	{
		return (prey_growth_rate*prey_population*plant_population) - (prey_death_rate*prey_population) - (predation_rate*prey_population*predator_population);
	}
	
	/* Modified LOTKA-VOLTERA - PLANT */
	private double calculate_plants(double predator,double prey_population,double plant_population)
	{
		return plant_growth_rate - (prey_plant_consumption_rate*prey_population*plant_population);
	}
	
	private void setUpStats()
	{
		stat_predator_population = new SingleStat("Predator");
		stat_predator_population.setColor(Color.red);
		
		stat_prey_population = new SingleStat("Prey");
		stat_prey_population.setColor(Color.blue);
		
		stat_plant_population = new SingleStat("Plants");
		stat_plant_population.setColor(Color.GREEN);
	}
	
	public List<StatInf> getPopulationStats()
	{
		List<StatInf> statList = new LinkedList<StatInf>();
		
		statList.add(stat_predator_population);
		statList.add(stat_prey_population);
		statList.add(stat_plant_population);
		
		return statList;
	}
	
	public void drawLV(NewSimView simView)
	{	
		
		drawPoints(simView);
		
		simView.drawPixMap(pixmap, 0, 0);
		
		simView.drawRectangle(0,0,bufferWidth,bufferHeight,new A2RGBA(1f,1f,1f,1f));
		
	}
	
	private void drawLAPoint(NewSimView simView, float x, float y, float z)
	{
	    double px = x*(-10*x+10*y);
	    double py = y*(28*x-y-x*z);
	    double pz = z*(-8*z/3+x*y);	    
	    
	}
	
	private void drawPoints(NewSimView simView)
	{			
		float xscale = (float) scale;
		float yscale = (float) scale;
		
		float xmax = 0;
		float ymax = 0;
		
		Color color;
		
		if(values!=null)
		{
			if(values.size() > 0)
			{
				if(previous==null)
				{
					previous = values.get(0);
				}

				for (A3DVector3f point : values) 
				{											
					color = new Color(Color.HSBtoRGB(pointsHue,1f,1f));
					
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
						
					pixmap.setColor(1f/255f*(float)color.getRed(),1f/255f*(float)color.getGreen(),1f/255f*(float)color.getBlue(),1f/255f*(float)color.getAlpha());
					
					pixmap.drawLine((int)(previous.getX()*xscale),(int)(previous.getY()*yscale),(int)(x*xscale),(int)(y*yscale));
														
					previous = point;
					
				}
				
				values = new LinkedList<A3DVector3f>();
				
			}
		}
	}
	
}
