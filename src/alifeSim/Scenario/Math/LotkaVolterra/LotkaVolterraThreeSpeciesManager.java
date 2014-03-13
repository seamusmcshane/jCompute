package alifeSim.Scenario.Math.LotkaVolterra;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatManager;
import alifeSimGeom.A2RGBA;
import alifeSimGeom.A3DVector3f;

public class LotkaVolterraThreeSpeciesManager implements LotkaVolterraSubTypeInf
{
	/* Initial */
	private double initial_prey_population;
	private double initial_predator_population;
	private double initial_plant_population;
		
	/* Constants */
	private double predator_predation_rate;
	private double predator_conversion_rate;
	private double prey_plant_conversion_rate;
	private double prey_plant_consumption_rate;
	
	private double predator_death_rate;
	private double prey_death_rate;
	
	private double plant_growth_rate;
	
	/* Supported Stats */
	private double plant_population;
	private SingleStat stat_plant_population;
	private double plant_min_population;
	private double plant_max_population;	
	
	private SingleStat stat_prey_population;	
	private double prey_min_population;
	private double prey_population;
	private double prey_max_population;	
	
	private SingleStat stat_predator_population;
	private double predator_population;
	private double predator_min_population;
	private double predator_max_population;	
	
	private int t = 0;
	//private int max_t = 10000;
	
	/* 0 - Euler, 1 - RK4 */
	private int intMethod; 
	private int sub_steps;
	
	// Ratio of SubSteps to draw (1=all)
	private int draw_mod = 1;
	
	private double dt;
	
	private LinkedList<A3DVector3f> values;
	
	/* Previous Point */
	A3DVector3f previous;
		
	private int maxPoints;
		
	private float pointsHue=0f;
	private float scale;
	
	private StatManager statManager;
	
	public LotkaVolterraThreeSpeciesManager(LotkaVolterraTwoAndThreeSpeciesSettings settings)
	{
		values = new LinkedList<A3DVector3f>();
		
		prey_min_population = Double.POSITIVE_INFINITY;
		prey_max_population = Double.NEGATIVE_INFINITY;;
		predator_min_population = Double.POSITIVE_INFINITY;
		predator_max_population = Double.NEGATIVE_INFINITY;
		
		scale = settings.getViewScale();
		
		initial_prey_population = settings.getInitialPreyPopulation();
		initial_predator_population = settings.getInitialPredatorPopulation();
		initial_plant_population = settings.getInitialPlantPopulation();

		prey_population = initial_prey_population;
		predator_population = initial_predator_population;
		plant_population = initial_plant_population;

		predator_predation_rate = settings.getPredatorPredationRate();		
		predator_conversion_rate = settings.getPredatorConversionRate();
		
		prey_plant_consumption_rate = settings.getPreyPlantConsumptionRate();
		prey_plant_conversion_rate = settings.getPreyPlantConversionRate();

		plant_growth_rate = settings.getPlantGrowthRate();

		predator_death_rate = settings.getPredatorDeathRate();
		prey_death_rate = settings.getPreyDeathRate();
		
		System.out.println("prey_population : " + prey_population);
		System.out.println("predator_population : " + predator_population);
		System.out.println("plant_population : " + plant_population);
		System.out.println("predator_predation_rate : " + predator_predation_rate);
		System.out.println("predator_conversion_rate : " + predator_conversion_rate);
		System.out.println("prey_plant_consumption_rate : " + prey_plant_consumption_rate);
		System.out.println("prey_plant_conversion_rate : " + prey_plant_conversion_rate);
		System.out.println("plant_growth_rate : " + plant_growth_rate);
		System.out.println("predator_death_rate : " + predator_death_rate);
		System.out.println("prey_death_rate : " + prey_death_rate);
		
		sub_steps = settings.getSubSteps();
		
		maxPoints = sub_steps*10;
		
		dt = 1/(double)sub_steps;
		
		intMethod = setIntMethod(settings.getIntMethod());
		
		setUpStats();
		
		System.out.println(predator_population + " " + prey_population + " " + plant_population);

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
			
			statManager.notifiyStatListeners();

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
		
		double plk1;
		double plk2;
		double plk3;
		double plk4;

		for(int i=1;i<sub_steps;i++)
		{
			
			
			// K1
			pdk1 =  calculate_predator(predator_population,prey_population,plant_population);
			pyk1 =  calculate_prey(predator_population,prey_population,plant_population);
			plk1 =  calculate_plants(predator_population,prey_population,plant_population);
			
			// K2 - calculate a half step based on k1 delta
			pdk2 = calculate_predator(predator_population+(pdk1*0.5*dt),prey_population+(pyk1*0.5*dt),plant_population+(plk1*0.5*dt));
			pyk2 = calculate_prey(predator_population+(pdk1*0.5*dt),prey_population+(pyk1*0.5*dt),plant_population+(plk1*0.5*dt));
			plk2 = calculate_plants(predator_population+(pdk1*0.5*dt),prey_population+(pyk1*0.5*dt),plant_population+(plk1*0.5*dt));
			
			// K3+ (t*0.5) - calculate again based on k2 delta
			pdk3 = calculate_predator(predator_population+(pdk2*0.5*dt),prey_population+(pyk2*0.5*dt),plant_population+(plk2*0.5*dt));
			pyk3 = calculate_prey(predator_population+(pdk2*0.5*dt),prey_population+(pyk2*0.5*dt),plant_population+(plk2*0.5*dt));
			plk3 = calculate_plants(predator_population+(pdk2*0.5*dt),prey_population+(pyk2*0.5*dt),plant_population+(plk2*0.5*dt));
			
			// K4+ (t*0.5) - calculate full step based on k3 delta
			pdk4 = calculate_predator(predator_population+pdk3*dt,prey_population+pyk3*dt,plant_population+plk3*dt);
			pyk4 = calculate_prey(predator_population+pdk3*dt,prey_population+pyk3*dt,plant_population+plk3*dt);
			plk4 = calculate_plants(predator_population+pdk3*dt,prey_population+pyk3*dt,plant_population+plk3*dt);
			 
			predator_population		+= (pdk1 + (2.0 * pdk2) + (2.0 * pdk3) + pdk4)*(1.0/6.0)*dt;
			prey_population			+= (pyk1 + (2.0 * pyk2) + (2.0 * pyk3) + pyk4)*(1.0/6.0)*dt;
			plant_population		+= (plk1 + (2.0 * plk2) + (2.0 * plk3) + plk4)*(1.0/6.0)*dt;
			
			// Stats
			stat_predator_population.addSample(predator_population);
			stat_prey_population.addSample(prey_population);
			stat_plant_population.addSample(plant_population);
			
			// Draw 
			addDrawVal(i);
			
			statManager.notifiyStatListeners();			
		}
		
		//System.out.println("PR " + predator_population + " PY " + prey_population + " PL " + plant_population);
		
	}
	
	private void findMinMax()
	{
		for (A3DVector3f vector : values) 
		{
			/* predator_population */
			if(predator_population > predator_max_population)
			{
				predator_max_population = predator_population;
			}
			
			if(predator_population < predator_min_population)
			{
				predator_min_population = predator_population;
			}
			
			/* prey_population */
			if(prey_population > prey_max_population)
			{
				prey_max_population = prey_population;
			}
			
			if(prey_population < prey_min_population)
			{
				prey_min_population = prey_population;
			}
			
			/* plant_population */
			if(plant_population > plant_max_population)
			{
				plant_max_population = plant_population;
			}
			
			if(plant_population < plant_min_population)
			{
				plant_min_population = plant_population;
			}
		}
	}
	
	private void addDrawVal(int i)
	{		

		
		if(i%draw_mod == 0)
		{
			// Draw 
			values.add(new A3DVector3f(predator_population,prey_population,plant_population,null));	
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
		return (prey_plant_conversion_rate*prey_population*plant_population) - (prey_death_rate*prey_population) - (predator_predation_rate*prey_population*predator_population);
	}
	
	/* Modified LOTKA-VOLTERA - PLANT */
	private double calculate_plants(double predator_population,double prey_population,double plant_population)
	{
		return plant_growth_rate*plant_population - (prey_plant_consumption_rate*prey_population*plant_population);
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
	
	public List<SingleStat> getPopulationStats()
	{
		List<SingleStat> statList = new LinkedList<SingleStat>();
		
		statList.add(stat_predator_population);
		statList.add(stat_prey_population);
		statList.add(stat_plant_population);
		
		return statList;
	}
	
	public void draw(GUISimulationView simView)
	{			
		drawPoints(simView);
	}
	

	private void drawPoints(GUISimulationView simView)
	{			
		float xscale = scale;
		float yscale = scale;
		
		float xmax = 0;
		float ymax = 0;

		if(values!=null)
		{
			if(values.size() > 0)
			{
				findMinMax();
				
				if(previous==null)
				{
					previous = values.get(0);
				}

				for (A3DVector3f vector : values) 
				{																
					float x = (float)vector.getX();
					float y =(float)vector.getY();

					if(x>xmax)
					{
						xmax = x;
					}
					
					if(y>ymax)
					{
						ymax = y;
					}

					
					
					float lx =  (vector.getX()/vector.getZ())*xscale;
					float ly =  (vector.getY()/vector.getZ())*yscale;
					float plx = (previous.getX()/previous.getZ())*xscale;
					float ply = (previous.getY()/previous.getZ())*yscale;
					
					A2RGBA color = new A2RGBA(new Color(Color.HSBtoRGB((float) ((1f/(plant_max_population*scale))*(vector.getZ()*scale))*0.5f ,1f,(float) ((1f/(plant_max_population*scale))*(vector.getZ()*scale))*0.5f )));
					vector.setColor(color);
					
					simView.drawLine(lx,ly, plx,ply,new A2RGBA(new Color(vector.getColor().getRed(),vector.getColor().getGreen(),vector.getColor().getBlue(),vector.getColor().getAlpha())),false);
					
					previous = vector;
					
				}
								
				if(values.size() > maxPoints)
				{			
					while(values.size() > maxPoints)
					{
						values.remove(0);
					}
				}
				
				previous = null;
			}
		}
	}

	@Override
	public void setStatManager(StatManager statManager)
	{
		this.statManager = statManager;		
	}
	
}
