package alife;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.border.EtchedBorder;

public class StatsPanel extends JPanel
{

	/* Panel Contents */
	private static JPanel simStatCountPanel = new JPanel();
	private static JPanel alifeInfoRow = new JPanel();
	private static JLabel lblPlants = new JLabel("Plants");
	private static JLabel lblPlantNo = new JLabel("0");
	private static JLabel lblPredators = new JLabel("Predators");
	private static JLabel lblPredatorsNo = new JLabel("0");
	private static JLabel lblPrey = new JLabel("Prey");
	private static JLabel lblPreyNo = new JLabel("0");
	private static JPanel simulationInfoRow = new JPanel();
	private static JLabel lblASPS = new JLabel("ASPS");
	private static JLabel lblASPSNo = new JLabel("0");
	private static JLabel lblRunTime = new JLabel("Run Time");
	private static JLabel lblRunTimeNo = new JLabel("0");
	private static StatsGraphPanel graphPanel;

	/* Counters */
	private static int ASPS=0; // Average Steps per second
	private static int plantNo = 0;
	private static int preyNo = 0;
	private static int predNo = 0;

	/* Graph Samples - 15 sps * 60 seconds = 900 samples for a minute etc.. */
	private static int samplePeriod=60;
	private static int sampleNum = 9000;
	
	private static int plantSamples[] = new int[sampleNum];	
	private static int preySamples[] = new int[sampleNum];
	private static int predSamples[] = new int[sampleNum];
	
	/* Record of max values for graph scaling */
	private static int plantsMax = 0;
	private static int preyMax = 0;
	private static int predMax = 0;

	private static boolean tiePredPreyMax = true;

	private static int maxVal = 0;
	private final JLabel lblStep = new JLabel("Step");
	private final JLabel lblStepNo = new JLabel("0");

	public StatsPanel()
	{
		setBorder(new TitledBorder(null, "Stats", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));

		simStatCountPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Stats", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(simStatCountPanel, BorderLayout.SOUTH);
		simStatCountPanel.setLayout(new GridLayout(2, 6, 0, 0));

		simStatCountPanel.add(alifeInfoRow);
		alifeInfoRow.setLayout(new GridLayout(0, 6, 0, 0));

		lblPlants.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPlants);
		lblPlantNo.setForeground(Color.GREEN);

		lblPlantNo.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPlantNo);

		lblPredators.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPredators);
		lblPredatorsNo.setForeground(Color.RED);

		lblPredatorsNo.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPredatorsNo);

		lblPrey.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPrey);
		lblPreyNo.setForeground(Color.BLUE);

		lblPreyNo.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPreyNo);

		simStatCountPanel.add(simulationInfoRow);
		simulationInfoRow.setLayout(new GridLayout(0, 6, 0, 0));

		lblASPS.setHorizontalAlignment(SwingConstants.CENTER);
		simulationInfoRow.add(lblASPS);

		lblASPSNo.setHorizontalAlignment(SwingConstants.CENTER);
		simulationInfoRow.add(lblASPSNo);
						lblStep.setHorizontalAlignment(SwingConstants.CENTER);
						
						simulationInfoRow.add(lblStep);
								lblStepNo.setHorizontalAlignment(SwingConstants.CENTER);
								
								simulationInfoRow.add(lblStepNo);
						
								lblRunTime.setHorizontalAlignment(SwingConstants.CENTER);
								simulationInfoRow.add(lblRunTime);
								
										lblRunTimeNo.setHorizontalAlignment(SwingConstants.CENTER);
										simulationInfoRow.add(lblRunTimeNo);

		graphPanel = new StatsGraphPanel(plantSamples,preySamples,predSamples,sampleNum,samplePeriod);
		
		graphPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		graphPanel.setBackground(Color.gray);
		add(graphPanel, BorderLayout.CENTER);
	}

	public static void addSamplePlantsGraph(int pSample)
	{
		/* Assume pSample is the max */
		plantsMax = pSample;
		
		// Moves the previous samples back by 1, leaves space for the new sps sample
		for (int i = 0; i < (sampleNum - 1); i++)
		{		
			plantSamples[i] = plantSamples[(i + 1)];
			
			/* Max Value */
			if(plantSamples[i] > plantsMax)
			{
				plantsMax = plantSamples[i];
			}			
		}

		plantSamples[sampleNum - 1] = pSample;			// Store the new sps sample
				
	}

	public static void addSamplePreyGraph(int pSample)
	{
		/* Assume pSample is the max */
		preyMax = pSample;
		
		// Moves the previous samples back by 1, leaves space for the new sps sample
		for (int i = 0; i < (sampleNum - 1); i++)
		{		
			preySamples[i] = preySamples[(i + 1)];
			
			/* Max Value */
			if(preySamples[i] > preyMax)
			{
				preyMax = preySamples[i];
			}			
		}

		preySamples[sampleNum - 1] = pSample;			// Store the new sps sample
				
	}	

	public static void addSamplePredGraph(int pSample)
	{
		/* Assume pSample is the max */
		predMax = pSample;
		
		// Moves the previous samples back by 1, leaves space for the new sps sample
		for (int i = 0; i < (sampleNum - 1); i++)
		{		
			predSamples[i] = predSamples[(i + 1)];
			
			/* Max Value */
			if(predSamples[i] > predMax)
			{
				predMax = predSamples[i];
			}			
		}

		predSamples[sampleNum - 1] = pSample;			// Store the new sps sample
				
	}	
	
	public static void setPlantNo(int no)
	{
		plantNo = no;
		
		lblPlantNo.setText(Integer.toString(plantNo));

		addSamplePlantsGraph(no);
	}

	public static void setPreyNo(int no)
	{
		preyNo = no;
		
		lblPreyNo.setText(Integer.toString(preyNo));
		
		addSamplePreyGraph(no);
	}

	public static void setPredNo(int no)
	{
		predNo = no;
		
		lblPredatorsNo.setText(Integer.toString(predNo));
		
		addSamplePredGraph(predNo);
	}

	public static void setASPS(int no)
	{
		ASPS=no;
		lblASPSNo.setText(Integer.toString(ASPS));
	}
	
	public void updateGraph()
	{	
				
		graphPanel.updateGraph(plantsMax,preyMax,predMax,tiePredPreyMax);
	}
		
	public static void clearStats()
	{
		// Moves the previous samples back by 1, leaves space for the new sps sample
		for (int i = 0; i < (sampleNum - 1); i++)
		{		
			plantSamples[i] = 0;
			preySamples[i] = 0;
			predSamples[i] = 0;
		
		}		
	}

}
