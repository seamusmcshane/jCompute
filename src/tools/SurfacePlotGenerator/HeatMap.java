package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class HeatMap extends JPanel
{
	private BufferedImage image;

	public HeatMap(BatchLogInf mapper)
	{
		image = new BufferedImage(mapper.getXSteps(),mapper.getYSteps(),BufferedImage.TYPE_INT_RGB);
		
		MapperRemapper reMapper = (MapperRemapper) mapper.getAvg();		
		
		reMapper.populateImage(image);
	}
	
	@Override 
	public void paintComponent(Graphics g)
	{		
		g.setColor(Color.black);
		
		g.drawString("TEST", 100,100);
		
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		g.drawImage(image, 0, 0, 800, 800, null);
	}
	
}
