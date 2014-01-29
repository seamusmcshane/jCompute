package alifeSim.Scenario.Math;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatInf;

public class LVManager
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

	private double t = 0;
	private int sub_steps = 64;
	private double dt = 1/(double)sub_steps;
	
	public LVManager()
	{
		setUpStats();
		
		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,initial_prey_population,initial_predator_population);
	}

	public void doStep()
	{
		predator_prey_rk4();
		t++;
		
		stat_predator_population.addSample((int)predator_population);
		
		stat_prey_population.addSample((int)prey_population);
		
	}
	
	public void predator_prey_euler()
	{							
		for(int i=1;i<sub_steps;i++)
		{
			t = i*dt;
			
			/* Prey */
			prey_population += calculate_prey(prey_population,predator_population)*dt;
			
			/* Predator */
			predator_population += calculate_predator(prey_population,predator_population)*dt;
			
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
	
}
