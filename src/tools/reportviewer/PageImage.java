package tools.reportviewer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PageImage
{
	private byte[][] imageBytes;
	
	private float[] size;
	
	private final int LARGE = 4096;
	private final int MEDIUM = 2048;
	private final int SMALL = 256;
	
	public PageImage(BufferedImage baseImage)
	{
		imageBytes = new byte[3][];
		
		int dMax = Math.max(baseImage.getWidth(), baseImage.getHeight());
		
		int largeImage = Math.min(LARGE, dMax);
		
		BufferedImage[] images = new BufferedImage[3];
		
		images[0] = createScaledImageFIX(baseImage, baseImage.getWidth(), baseImage.getHeight(), largeImage, false);
		size = new float[]
		{
			images[0].getWidth(), images[0].getHeight()
		};
		
		baseImage = null;
		
		images[1] = createScaledImageFIX(images[0], images[0].getWidth(), images[0].getHeight(), MEDIUM, false);
		images[2] = createScaledImageFIX(images[1], images[1].getWidth(), images[1].getHeight(), SMALL, false);
		
		for(int i = 0; i < imageBytes.length; i++)
		{
			System.out.println("1 " + images[i].getWidth() + " " + images[i].getHeight());
			imageBytes[i] = imageToPngBytes(images[i]);
		}
	}
	
	public BufferedImage pngBytesToBufferedImage(byte[] bytes)
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			
			return ImageIO.read(in);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			
			return null;
		}
	}
	
	public byte[] imageToPngBytes(BufferedImage image)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			ImageIO.write(image, "png", out);
			
			int inBytes = image.getRaster().getDataBuffer().getSize() * 4;
			int outBytes = out.size();
			
			System.out.println("Compressed size from " + inBytes + " to " + outBytes + " ratio " + ((float) inBytes / (float) outBytes));
			
			return out.toByteArray();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			
			return null;
		}
	}
	
	public float[] getSize()
	{
		return size;
	}
	
	public BufferedImage getImage(int res)
	{
		if(res < 0)
		{
			return pngBytesToBufferedImage(imageBytes[res]);
		}
		
		if(res > imageBytes.length)
		{
			return pngBytesToBufferedImage(imageBytes[imageBytes.length - 1]);
		}
		
		System.out.println("Res " + res);
		
		return pngBytesToBufferedImage(imageBytes[res]);
	}
	
	public static BufferedImage createScaledImageFIX(BufferedImage image, int width, int height, float maxSize, boolean fastScale)
	{
		int sWidth = width;
		int sHeight = height;
		
		if((image.getWidth() < maxSize) && (image.getHeight() < maxSize))
		{
			return image;
		}
		
		System.out.println("1Scaled " + sWidth + " " + sHeight);
		
		if(maxSize > 0)
		{
			float aspect;
			
			if(width > height)
			{
				aspect = height / (float) width;
				
				sWidth = (int) ((width < maxSize) ? width : maxSize);
				sHeight = (int) ((width < maxSize) ? height : (maxSize * aspect));
			}
			else
			{
				aspect = width / (float) height;
				
				sWidth = (int) ((width < maxSize) ? width : (aspect * maxSize));
				sHeight = (int) ((width < maxSize) ? height : maxSize);
			}
		}
		
		BufferedImage scaledImage = new BufferedImage(sWidth, sHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = scaledImage.createGraphics();
		
		if(fastScale)
		{
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		}
		else
		{
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
		
		System.out.println("2Scaled " + sWidth + " " + sHeight);
		
		g2d.drawImage(image, 0, 0, sWidth, sHeight, null);
		
		g2d.dispose();
		
		return scaledImage;
	}
	
}
