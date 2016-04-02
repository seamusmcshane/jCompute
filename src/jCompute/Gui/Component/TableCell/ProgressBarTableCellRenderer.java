package jCompute.Gui.Component.TableCell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormatSymbols;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressBarTableCellRenderer implements TableCellRenderer
{
	private ProgressBar pb;
	
	public ProgressBarTableCellRenderer(JTable table)
	{
		pb = new ProgressBar(table);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		pb.setSelected(isSelected);
		
		pb.setProgress((int) value);
		
		return pb;
	}
	
	private class ProgressBar extends JComponent
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
		
		public ProgressBar(JTable table)
		{
			selected = false;
			progress = 0;
			
			fg = Color.black;
			barFill = ColorConstants.BabyBlue;
			barOutline = ColorConstants.BabyBlue.darker();
			bg = ColorConstants.WhiterSmoke;
			
			selectedColor = table.getSelectionBackground();
			font = table.getFont().deriveFont(table.getFont().getSize() * .75f);
		}
		
		public void setProgress(int value)
		{
			if(value < 100)
			{
				progress = value;
			}
			else
			{
				progress = 100;
			}
		}
		
		private String progressToString()
		{
			if(progress < 0)
			{
				return new DecimalFormatSymbols().getInfinity();
			}
			
			return Integer.toString(progress) + " %";
		}
		
		@Override
		protected void paintComponent(Graphics g1)
		{
			super.paintComponent(g1);
			
			Graphics2D g2 = (Graphics2D) g1;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			// Selection Bar
			Rectangle clip = g2.getClipBounds();
			
			if(!selected)
			{
				g2.setColor(super.getBackground());
			}
			else
			{
				g2.setColor(selectedColor);
			}
			
			// Cell BG
			g2.fillRect(clip.x, clip.y, clip.width, clip.height);
			
			// Progress Bar BG
			g2.setColor(bg);
			g2.fillRect(clip.x + pad, clip.y + pad, ((clip.width - (pad * 2))) - 1, clip.height - (pad * 2) - 1);
			
			// Progress Bar Percent Fill
			float barWidth = ((float) progress / 100);
			g2.setColor(barFill);
			g2.fillRect(clip.x + pad, clip.y + pad, (int) ((clip.width - (pad * 2)) * barWidth) - 1, clip.height - (pad * 2) - 1);
			
			// Outline
			g2.setColor(barOutline);
			g2.drawRect(clip.x + pad, clip.y + pad, ((clip.width - (pad * 2))) - 1, clip.height - (pad * 2) - 1);
			
			g2.setFont(font);
			
			// String to draw
			String sProgress = progressToString();
			
			// Bounds
			Rectangle2D stringBounds = font.createGlyphVector(g2.getFontRenderContext(), sProgress).getVisualBounds();
			
			// Draw Label
			g2.setColor(fg);
			
			g2.drawString(sProgress, (int) (clip.width - stringBounds.getWidth()) / 2, (int) (((clip.height - stringBounds.getHeight()) / 2) - (stringBounds
			.getY())));
		}
		
		public void setSelected(boolean isSelected)
		{
			selected = isSelected;
		}
	}
	
}
