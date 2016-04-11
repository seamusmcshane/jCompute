package jCompute.gui.component.swing.jcomponent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormatSymbols;

import javax.swing.JComponent;

import jCompute.gui.component.tablecell.ColorConstants;

public class JComputeProgressBar extends JComponent
{
	private static final long serialVersionUID = -5090932584537058142L;

	private final int pad = 4;

	private int progress;
	private Color fg;
	private Color bg;
	private Color barFill;
	private Color barOutline;

	private boolean selected;
	private Color selectedColor;
	private Font font;

	public JComputeProgressBar(Font font)
	{
		this(font, Color.white);
	}

	public JComputeProgressBar(Font font, Color selectedColor)
	{
		this.font = font;
		this.selectedColor = selectedColor;

		selected = false;
		progress = 0;

		fg = Color.black;
		barFill = ColorConstants.BabyBlue;
		barOutline = ColorConstants.BabyBlue.darker();
		bg = ColorConstants.WhiterSmoke;
	}

	public void prepare(int value, boolean isSelected)
	{
		if(value < 100)
		{
			progress = value;
		}
		else
		{
			progress = 100;
		}

		selected = isSelected;
	}

	private String progressToString()
	{
		if(progress < 0)
		{
			return new DecimalFormatSymbols().getInfinity();
		}

		return Integer.toString(progress);
	}

	@Override
	protected void paintComponent(Graphics g1)
	{
		super.paintComponent(g1);

		Graphics2D g2 = (Graphics2D) g1;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if(!selected)
		{
			g2.setColor(super.getBackground());
		}
		else
		{
			g2.setColor(selectedColor);
		}

		// Cell BG
		g2.fillRect(0, 0, getWidth(), getHeight());

		// String to draw
		String sProgress = progressToString();

		// Bounds
		Rectangle2D stringBounds = font.createGlyphVector(g2.getFontRenderContext(), sProgress).getVisualBounds();

		// Progress Bar Percent Fill
		float barWidth = ((float) progress / 100);

		// Wide enough for nice percentage bar
		if(getWidth() > (pad * 8))
		{
			// Progress Bar BG
			g2.setColor(bg);
			g2.fillRect(pad, pad, ((getWidth() - (pad * 2))) - 1, getHeight() - (pad * 2) - 1);

			// Progress Bar
			g2.setColor(barFill);
			g2.fillRect(pad, pad, (int) ((getWidth() - (pad * 2)) * barWidth) - 1, getHeight() - (pad * 2) - 1);

			// Outline
			g2.setColor(barOutline);
			g2.drawRect(pad, pad, ((getWidth() - (pad * 2))) - 1, getHeight() - (pad * 2) - 1);

			// Append %
			sProgress += " %";
		}
		else
		{
			// Progress Bar ALT (when too small)
			g2.setColor(barFill);
			g2.fillRect(0, 0, (int) (getWidth() * barWidth) - 1, getHeight() - 1);
		}

		g2.setFont(font);

		// Draw Label
		g2.setColor(fg);

		g2.drawString(sProgress, (int) (getWidth() - stringBounds.getWidth()) / 2, (int) (((getHeight() - stringBounds.getHeight()) / 2) - (stringBounds
		.getY())));
	}
}