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
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.border.EtchedBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
	private static JLabel lblASPS = new JLabel("Avg Steps/Sec");
	private static JLabel lblASPSNo = new JLabel("0");
	private static JLabel lblRunTime = new JLabel("Run Time");
	private static JLabel lblRunTimeNo = new JLabel("0");
	private static StatsGraphPanel graphPanel;

	/* Counters */
	private static int ASPS=0; // Average Steps per second
	private static int stepNo=0;
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

	/* Graph Scales - click graph */
	private int scale_mode = 0; /* 0 = all on own scale, 1 - plants on own scale, prey+pred tied, 2 - all tied */	

	private static int maxVal = 0;
	private final JLabel lblStep = new JLabel("Step");
	private final static JLabel lblStepNo = new JLabel("0");
	
	public StatsPanel()
	{
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Population Graph", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));

		simStatCountPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Simulation Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(simStatCountPanel, BorderLayout.SOUTH);
		simStatCountPanel.setLayout(new GridLayout(2, 6, 0, 0));

		simStatCountPanel.add(alifeInfoRow);
		alifeInfoRow.setLayout(new GridLayout(0, 6, 0, 0));

		lblPlants.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPlants);
		lblPlantNo.setForeground(new Color(0, 128, 0));

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
		graphPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) 
			{
				// Clicking graph changes mode
				scale_mode = (scale_mode + 1) % 3; // Wrap at 3				
			}
		});
		
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
	
	public static void setStepNo(int no)
	{
		stepNo=no;
		lblStepNo.setText(Integer.toString(no));
	}
		
	// Called in the update sim loop
	public static void setTime(long time)
	{
			time = time / 1000; // seconds
			int days= (int) (time / 86400); // to days
			int hrs= (int) (time / 3600)%24; // to hrs
			int mins= (int) ((time/60)%60);	// to seconds
			int sec= (int) (time%60);

			lblRunTimeNo.setText(String.format("%d:%02d:%02d:%02d", days,hrs,mins,sec));			
	}
	
	public void updateGraph()
	{			
		graphPanel.updateGraph(plantsMax,preyMax,predMax,scale_mode);
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
