package jCompute.Gui.Component.Swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import jCompute.Gui.Cluster.TableRowItems.StandardLogRowItem;
import jCompute.Gui.Component.TableCell.TextHighLighterRenderer;
import jCompute.logging.Logging;

public class LogTab extends JPanel implements TailerListener
{
	private static final long serialVersionUID = 758063954767436104L;
	private Thread tailerThread;
	private TablePanel<Integer, StandardLogRowItem> standardLog;

	private final int MAX_ITEMS = 5000;
	private int linesAdded;

	public LogTab()
	{
		setLayout(new BorderLayout());
		Tailer tailer = new Tailer(new File(Logging.getStandardLogPath()), this, 1000);
		tailerThread = new Thread(tailer);
		tailerThread.setDaemon(true);

		standardLog = new TablePanel<Integer, StandardLogRowItem>(StandardLogRowItem.class, "Standard Log", true, true);
		standardLog.setColumWidth(0, 80);
		standardLog.setColumWidth(1, 80);
		standardLog.setColumWidth(2, 80);
		standardLog.setColumWidth(3, 80);

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

		//System.out.println("TAILER " + index + " " + level + " " + comp + " " + message + " start " + start + " " + " end " + end);

		standardLog.addRow(new StandardLogRowItem(linesAdded++, index, level, comp, message));

		if(standardLog.getRows() >= MAX_ITEMS)
		{
			standardLog.removeFirstRow();
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				standardLog.scrollToBottom();
			}
		});
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
