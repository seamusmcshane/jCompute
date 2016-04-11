package jCompute.gui.component.swing.jpanel;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleGridPanel extends JPanel
{
	private static final long serialVersionUID = -5209653908016192280L;
	
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(SimpleGridPanel.class);
	
	private final Font labelFont;
	private final Font valFont;
	
	private final JLabel[] lblFields;
	private final JLabel[] lblVals;
	
	private final int rows;
	
	private int rowsAdded;
	
	// Prevent Calling
	@SuppressWarnings("unused")
	private SimpleGridPanel()
	{
		lblFields = null;
		lblVals = null;
		
		rows = 0;
		rowsAdded = 0;
		
		labelFont = null;
		valFont = null;
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public SimpleGridPanel(int rows)
	{
		super();
		
		labelFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
		valFont = UIManager.getFont("Label.font");
		this.rows = rows;
		rowsAdded = 0;
		
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
			100, 100
		};
		layout.columnWeights = new double[]
		{
			1.0, 1.0
		};
		layout.rowWeights = new double[]{};
		
		layout.rowHeights = new int[rows];
		lblFields = new JLabel[rows];
		lblVals = new JLabel[rows];
		
		int rowHeight = 20;
		
		for(int r = 0; r < rows; r++)
		{
			layout.rowHeights[r] = rowHeight;
		}
		
		this.setLayout(layout);
		
	}
	
	public int addRow(String labelText, String intialValueText)
	{
		if(rowsAdded == rows)
		{
			log.error("All rows are full");
			return -1;
		}
		
		// Label
		lblFields[rowsAdded] = new JLabel(labelText);
		lblFields[rowsAdded].setFont(labelFont);
		lblFields[rowsAdded].setHorizontalAlignment(SwingConstants.LEFT);
		
		GridBagConstraints gbc_lblLabel = new GridBagConstraints();
		gbc_lblLabel.fill = GridBagConstraints.BOTH;
		gbc_lblLabel.gridx = 0;
		gbc_lblLabel.gridy = rowsAdded;
		this.add(lblFields[rowsAdded], gbc_lblLabel);
		
		// Val
		lblVals[rowsAdded] = new JLabel(intialValueText);
		lblVals[rowsAdded].setFont(valFont);
		lblVals[rowsAdded].setHorizontalAlignment(SwingConstants.LEFT);
		
		GridBagConstraints gbc_lblval = new GridBagConstraints();
		gbc_lblval.fill = GridBagConstraints.BOTH;
		gbc_lblval.gridx = 1;
		gbc_lblval.gridy = rowsAdded;
		
		this.add(lblVals[rowsAdded], gbc_lblval);
		
		return rowsAdded++;
	}
	
	public void changeValText(int index, String text)
	{
		if(index >= rows || index < 0)
		{
			log.error("Invalid Row Index");
			
			return;
		}
		
		lblVals[index].setText(text);
	}
}
