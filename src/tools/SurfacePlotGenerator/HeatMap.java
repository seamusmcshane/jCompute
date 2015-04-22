package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.XMLBatchLogProcessorMapper;
import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class HeatMap extends JPanel
{
	private BufferedImage image;

	public HeatMap(XMLBatchLogProcessorMapper mapper)
	{
		image = new BufferedImage(mapper.getXSteps(),mapper.getYSteps(),BufferedImage.TYPE_INT_RGB);
		
		MapperRemapper reMapper = mapper.getStdDev();		
		
		reMapper.populateImage(image);
	}
	
	@Override 
	public void paintComponent(Graphics g)
	{		
		g.setColor(Color.black);
		
		g.drawString("TEST", 100,100);
		
		g.drawImage(image, 0, 0, 800, 800, null);
	}
	
}
