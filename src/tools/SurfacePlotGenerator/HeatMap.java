package tools.SurfacePlotGenerator;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class HeatMap extends JPanel
{
	private BufferedImage image;
	
	public HeatMap()
	{
	
	}
	
	public void setLog(BatchLogInf mapper)
	{
		image = new BufferedImage(mapper.getXSteps(), mapper.getYSteps(), BufferedImage.TYPE_INT_RGB);
		
		MapperRemapper reMapper = (MapperRemapper) mapper.getAvg();
		
		reMapper.populateImage(image);
		
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		
		// Resize
		Image tmp = image.getScaledInstance(800, 800, Image.SCALE_FAST);
		image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		g.setColor(Color.black);
		g.drawImage(image, 0, 0, 800, 800, null);
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
}
