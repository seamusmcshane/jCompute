package alifeSim.Scenario.Math;

public class LVTest
{
	// A = prey_growth
	// B = predation_rate
	// C = predator_death_rate
	// D = predator_conversion_rate
		
	/* Prey */
	// dx = (prey_growth*prey_population) - (predation_rate*prey_population*predator_population)
	
	/* Predator */
	// dy = (-predator_death_rate*predator_population) +  (predator_conversion_rate*prey_population*predator_population);

	public strictfp static void main(String args[])
	{
		double prey_population = 250;
		double predator_population = 10;
		
		double prey_growth = 0.2;
		
		double predation_rate = 0.008;
		
		double predator_death_rate = 0.8;
		
		double predator_conversion_rate = 0.004;
		
		double dx=0;
		double dy=0;
		
		double sub_steps = 800;
		double dt = 1/sub_steps;
		
		double t = 0;

		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);

		
		for(int i=1;i<(1000-1)*sub_steps;i++)
		{
			t = i*dt;
			
			/* Prey */
			dx = (prey_growth*prey_population) - (predation_rate*prey_population*predator_population);
			
			/* Predator */
			dy = (-predator_death_rate*predator_population) +  (predator_conversion_rate*prey_population*predator_population);

			prey_population		+= 	(dx*dt);
			predator_population	+=	(dy*dt);
			
			//if(t%1==0)
			System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);
			
		}

	}
	
}
