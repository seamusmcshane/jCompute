package jCompute.Gui.Component.Swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import jCompute.Gui.Cluster.TableRowItems.StandardLogRowItem;
import jCompute.Gui.Component.TableCell.TextHighLighterRenderer;
import jCompute.logging.Logging;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class LogTab extends JPanel implements TailerListener
{
	private static final long serialVersionUID = 758063954767436104L;
	private Tailer tailer;
	private Thread tailerThread;
	private TablePanel<Integer, StandardLogRowItem> standardLog;
	
	private final int MAX_ITEMS = 5000;
	private int linesAdded;
	private JLabel lblLinesVar;
	
	private LogTab self = this;
	
	public LogTab(SimpleTabPanel tabpanel)
	{
		setLayout(new BorderLayout());
		createTailer(self);
		standardLog = new TablePanel<Integer, StandardLogRowItem>(StandardLogRowItem.class, "Standard Log", true, false);
		standardLog.setColumWidth(0, 80);
		standardLog.setColumWidth(1, 80);
		standardLog.setColumWidth(2, 80);
		standardLog.setColumWidth(3, 140);
		
		Color[] colors = new Color[]
		{
			Color.GREEN, Color.ORANGE, Color.RED, Color.BLACK
		};
		
		String[] text = new String[]
		{
			" INFO", " WARN", "ERROR", "DEBUG"
		};
		
		standardLog.addColumRenderer(new TextHighLighterRenderer(colors, text), 2);
		
		this.add(standardLog, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(1, 6, 0, 0));
		
		JButton btnClose = new JButton("Close Tab");
		btnClose.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				synchronized(self)
				{
					tailer.stop();
					
					tabpanel.removeTab(self);
					standardLog.clearTable();
					
					standardLog = null;
					tailerThread = null;
				}
			}
		});
		
		JButton btnClear = new JButton("Clear View");
		btnClear.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				synchronized(self)
				{
					standardLog.clearTable();
					linesAdded = 0;
					refreshLabels();
				}
			}
		});
		
		JPanel panel_LogLines = new JPanel();
		panel.add(panel_LogLines);
		panel_LogLines.setLayout(new GridLayout(0, 2, 0, 0));
		
		JLabel lblLines = new JLabel("Lines");
		lblLines.setHorizontalAlignment(SwingConstants.CENTER);
		panel_LogLines.add(lblLines);
		
		lblLinesVar = new JLabel("");
		lblLinesVar.setHorizontalAlignment(SwingConstants.LEFT);
		panel_LogLines.add(lblLinesVar);
		panel.add(btnClear);
		
		JButton btnReload = new JButton("Reload Log");
		btnReload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				synchronized(self)
				{
					createTailer(self);
					
					tailerThread.start();
				}
			}
		});
		panel.add(btnReload);
		panel.add(btnClose);
	}
	
	private void createTailer(LogTab self)
	{
		if(tailer != null)
		{
			tailer.stop();
			
			standardLog.clearTable();
		}
		
		linesAdded = 0;
		refreshLabels();
		
		tailer = new Tailer(new File(Logging.getStandardLogPath()), self, 1000);
		tailerThread = new Thread(tailer);
		tailerThread.setDaemon(true);
		tailerThread.setName("Logging Tailer");
	}
	
	private void refreshLabels()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				lblLinesVar.setText(String.valueOf(linesAdded));
			}
		});
	}
	
	public void start()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				tailerThread.start();
			}
		});
	}
	
	@Override
	public void fileNotFound()
	{
		
	}
	
	@Override
	public void fileRotated()
	{
		
	}
	
	@Override
	public void handle(String line)
	{
		int start = line.indexOf('|', 0) + 2;
		int end = line.indexOf('|', start) - 1;
		String sindex = line.substring(start, end);
		
		int index = Integer.parseInt(sindex);
		
		start = line.indexOf('|', end) + 2;
		end = line.indexOf('[', start) - 1;
		String level = line.substring(start, end);
		
		start = end + 2;
		end = line.indexOf(']', start);
		String comp = line.substring(start, end);
		
		start = line.indexOf('-', end) + 1;
		end = line.length();
		String message = line.substring(start, end);
		
		synchronized(self)
		{
			// System.out.println("TAILER " + index + " " + level + " " + comp + " " + message + " start " + start + " " + " end " + end);
			standardLog.addRowTailed(new StandardLogRowItem(++linesAdded, index, level, comp, message), MAX_ITEMS);
			
			standardLog.scrollToBottom();
			
			refreshLabels();
		}
	}
	
	@Override
	public void handle(Exception e)
	{
		
	}
	
	@Override
	public void init(Tailer e)
	{
		// System.out.println("Tailer " + e.getFile() + " Delay " + e.getDelay());
	}
}
