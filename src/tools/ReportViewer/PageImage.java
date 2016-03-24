package tools.ReportViewer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class PageImage
{
	private BufferedImage[] images;

	private float[] size;

	private final int LARGE = 8192;
	private final int MEDIUM = 2048;
	private final int SMALL = 1024;

	public PageImage(BufferedImage baseImage)
	{
		images = new BufferedImage[3];

		images[0] = createScaledImageFIX(baseImage, baseImage.getWidth(), baseImage.getHeight(), LARGE, false);

		size = new float[]
		{
			images[0].getWidth(), images[0].getHeight()
		};

		baseImage = null;

		images[1] = createScaledImageFIX(images[0], images[0].getWidth(), images[0].getHeight(), MEDIUM, false);

		images[2] = createScaledImageFIX(images[1], images[1].getWidth(), images[1].getHeight(), SMALL, false);

		System.out.println("images[0] " + images[0].getWidth() + " " + images[0].getHeight());
		System.out.println("images[1] " + images[1].getWidth() + " " + images[1].getHeight());
		System.out.println("images[2] " + images[2].getWidth() + " " + images[2].getHeight());
	}

	public float[] getSize()
	{
		return size;
	}

	public BufferedImage getImage(int res)
	{
		if(res < 0)
		{
			return images[0];
		}

		if(res > images.length)
		{
			return images[images.length - 1];
		}

		System.out.println("Res " + res);

		return images[res];
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
			}
			else
			{
				aspect = width / (float) height;
			}

			sWidth = (int) ((width < maxSize) ? width : maxSize);
			sHeight = (int) ((width < maxSize) ? height : (maxSize * aspect));
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
