package alifeSim.Scenario.Math.LotkaVolterra;

public class LotkaVolterraTest
{
	// A = prey_growth
	// B = predation_rate
	// C = predator_death_rate
	// D = predator_conversion_rate

	static double initial_prey_population = 800;
	static double initial_predator_population = 100;
		
	static double prey_growth = 0.5;
	static double predation_rate = 0.008;
	static double predator_death_rate = 0.8;
	static double predator_conversion_rate = 0.2;
	
	public static void main(String args[])
	{
		//int sub_steps = 1024*57;
		int sub_steps = 64;

		//predator_prey_euler(1000,sub_steps);
		
		predator_prey_rk4(1000,sub_steps);

		//predator_prey_rkO(5,1000,sub_steps);

	}
		
	public static void predator_prey_rkO(double order,int steps,double sub_steps)
	{
		double prey_population = initial_prey_population;
		double predator_population = initial_predator_population;
		double t = 0;

		double dt = 1/sub_steps;
		
		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);
				
		double pdk1;
		double pdk2;
		double pdk3;
		double pdk4;
		
		double pyk1;
		double pyk2;
		double pyk3;
		double pyk4;

		for(int i=1;i<(steps-1)*sub_steps;i++)
		{
			t = i*dt;
			
			// K1
			pdk1 = calculate_predator(prey_population,predator_population);
			pyk1 = calculate_prey(prey_population,predator_population);
			
			// K2
			pdk2 = calculate_predator(prey_population+(pyk1*0.5*dt),predator_population+(pdk1*0.5*dt));
			pyk2 = calculate_prey(prey_population+(pyk1*0.5*dt),predator_population+(pdk1*0.5*dt));
			
			// K3
			pdk3 = calculate_predator	( prey_population + (0.5-(1.0/order))*pyk1*dt + 1.0/order*pyk2*dt , predator_population + (0.5-(1.0/order))*pdk1*dt + 1.0/order*pdk2*dt );
			pyk3 = calculate_prey		( prey_population + (0.5-(1.0/order))*pyk1*dt + 1.0/order*pyk2*dt , predator_population + (0.5-(1.0/order))*pdk1*dt + 1.0/order*pdk2*dt );
			
			// K4
			pdk4 = calculate_predator	( prey_population + (1.0 - order/2.0 )* pyk2*dt+ (order/2.0)*pyk3*dt, predator_population + (1.0 - order/2.0 )* pdk2*dt+ (order/2.0)*pdk3*dt );
			pyk4 = calculate_prey		( prey_population + (1.0 - order/2.0 )* pyk2*dt+ (order/2.0)*pyk3*dt, predator_population + (1.0 - order/2.0 )* pdk2*dt+ (order/2.0)*pdk3*dt );
			
			predator_population += (pdk1 + ((4.0-order) * pdk2) + (order * pdk3) + pdk4)*(1.0/6.0)*dt;
			prey_population		+= (pyk1 + ((4.0-order) * pyk2) + (order * pyk3) + pyk4)*(1.0/6.0)*dt;
		
			if(t%1==0)
			System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);
		}
		
	}

	public static void predator_prey_rk4(int steps,double sub_steps)
	{
		double prey_population = initial_prey_population;
		double predator_population = initial_predator_population;
		double t = 0;

		double dt = 1/sub_steps;
		
		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);
				
		double pdk1;
		double pdk2;
		double pdk3;
		double pdk4;
		
		double pyk1;
		double pyk2;
		double pyk3;
		double pyk4;

		for(int i=1;i<(steps-1)*sub_steps;i++)
		{
			t = i*dt;
			
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
		
			if(t%1==0)
			System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);
		}
		
	}
	
	public static void predator_prey_euler(int steps, int sub_steps)
	{	
		double prey_population = initial_prey_population;
		double predator_population = initial_predator_population;
		double t = 0;

		double dt = 1/(double)sub_steps;
		
		System.out.println("Time"+"\t\t"+"Predators"+"\t\t"+"Prey");
		System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);
				
		for(int i=1;i<(steps-1)*sub_steps;i++)
		{
			t = i*dt;
			
			/* Prey */
			prey_population += calculate_prey(prey_population,predator_population)*dt;
			
			/* Predator */
			predator_population += calculate_predator(prey_population,predator_population)*dt;
			
			if(t%1==0)
			System.out.printf("%5.3f\t\t%5.2f\t\t%5.2f\n",t,predator_population,prey_population);
			
		}
	}
	
	public static double calculate_prey(double prey_population,double predator_population)
	{
		return prey_growth*prey_population - predation_rate*prey_population*predator_population;
	}
	
	public static double calculate_predator(double prey_population,double predator_population)
	{
		return predator_conversion_rate*predation_rate*prey_population*predator_population-predator_death_rate*predator_population; 
	}
		
}
