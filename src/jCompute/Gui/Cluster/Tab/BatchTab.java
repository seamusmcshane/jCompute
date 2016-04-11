package jCompute.gui.cluster.tab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.DaveKoelle.AlphanumFileNameComparator;
import com.google.common.eventbus.Subscribe;

import jCompute.IconManager;
import jCompute.IconManager.IconIndex;
import jCompute.batch.Batch;
import jCompute.cluster.batchmanager.BatchManager;
import jCompute.cluster.batchmanager.event.BatchAddedEvent;
import jCompute.cluster.batchmanager.event.BatchFinishedEvent;
import jCompute.cluster.batchmanager.event.BatchPositionEvent;
import jCompute.cluster.batchmanager.event.BatchProgressEvent;
import jCompute.gui.cluster.tablerowitems.BatchCompletedRowItem;
import jCompute.gui.cluster.tablerowitems.BatchQueueRowItem;
import jCompute.gui.cluster.tablerowitems.SimpleInfoRowItem;
import jCompute.gui.component.swing.MessageBox;
import jCompute.gui.component.swing.jpanel.GlobalProgressMonitor;
import jCompute.gui.component.swing.jpanel.TablePanel;
import jCompute.gui.component.swing.jpanel.XMLPreviewPanel;
import jCompute.gui.component.swing.swingworker.Loadable;
import jCompute.gui.component.swing.swingworker.LoadableTask;
import jCompute.gui.component.tablecell.BooleanIconRenderer;
import jCompute.gui.component.tablecell.EmptyCellColorRenderer;
import jCompute.gui.component.tablecell.HeaderRowRenderer;
import jCompute.gui.component.tablecell.ProgressBarTableCellRenderer;
import jCompute.JComputeEventBus;
import jCompute.util.FileUtil;
import jCompute.util.TimeString;
import jCompute.util.TimeString.TimeStringFormat;

public class BatchTab extends JPanel implements Loadable, PropertyChangeListener
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(BatchTab.class);
	private static final long serialVersionUID = 662715907114338723L;
	
	private BatchTab self;
	
	// Left Split
	private JPanel batchQueuedAndCompletePanel;
	private TablePanel<Integer, BatchQueueRowItem> batchQueuedTable;
	private TablePanel<Integer, BatchCompletedRowItem> batchCompletedTable;
	
	// BatchInfo selection
	private int queuedSelectedBatchRowIndex = -1;
	private int completedSelectedBatchRowIndex = -1;
	// 0 = none,1 queued,2 completed
	private int queuedOrCompleted = 0;
	
	// Right Split
	private TablePanel<String, SimpleInfoRowItem> batchInfo;
	private int rightPanelsMinWidth;
	
	// Toolbar
	private JToolBar toolBar;
	// Batch Add
	private JButton btnAdd;
	private JButton btnRemove;
	private JButton btnStart;
	private JButton btnStop;
	private JButton btnMoveForward;
	private JButton btnMoveFirst;
	private JButton btnMoveBackward;
	private JButton btnMoveLast;
	
	// Queue Table Positions
	private int positionColumn = 0;
	private int idColumn = 1;
	private int nameColumn = 2;
	private int statusColumn = 3;
	private int progressColumn = 4;
	private int estimatedFinishColumn = 5;
	
	private int numericColumnWidth = 60;
	private int iconColumnWidth = 50;
	private int dateColumnWidth = 220;
	private int timeColumnWidth = 100;
	private int progressColumnWidth = 80;
	
	private BatchManager batchManager;
	
	private Timer batchInfoUpdateTimer;
	
	// Loadable
	private int[] indexes;
	private String[] filePaths;
	private Loadable requester;
	
	public BatchTab(int rightPanelsMinWidth, boolean buttonText)
	{
		// Reference to self
		self = this;
		
		// Min Width of rightPanel
		this.rightPanelsMinWidth = rightPanelsMinWidth;
		
		// Panel Layout
		setLayout(new BorderLayout());
		
		createBatchQueuedAndCompletePanel();
		
		this.add(batchQueuedAndCompletePanel, BorderLayout.CENTER);
		
		createBatchInfoPanel();
		
		this.add(batchInfo, BorderLayout.EAST);
		
		// Always Visible but not always populated
		batchInfo.setVisible(true);
		
		// Tool Bar
		createToolbar(buttonText);
		add(toolBar, BorderLayout.NORTH);
		
		registerTableMouseListeners();
		
		// Register on the event bus
		JComputeEventBus.register(this);
		
		batchInfoUpdateTimer = new Timer("Batch Info Timer");
		batchInfoUpdateTimer.scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				// Update if Visible
				if(batchInfo.isVisible())
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							updateBatchInfo(queuedOrCompleted);
						}
					});
				}
			}
		}, 0, 1000);
	}
	
	private void createToolbar(boolean buttonText)
	{
		toolBar = new JToolBar();
		
		toolBar.setFloatable(false);
		
		btnStart = new JButton();
		btnStart.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				startBatch();
			}
		});
		
		btnAdd = new JButton();
		btnAdd.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser filechooser = new JFileChooser(new File("./Batch"));
				
				filechooser.setFileFilter(FileUtil.batchFileFilter());
				
				filechooser.setPreferredSize(new Dimension(1000, 600));
				filechooser.setMultiSelectionEnabled(true);
				
				XMLPreviewPanel xmlPreview = new XMLPreviewPanel();
				filechooser.setAccessory(xmlPreview);
				filechooser.addPropertyChangeListener(xmlPreview);
				Action details = filechooser.getActionMap().get("viewTypeDetails");
				details.actionPerformed(null);
				
				log.info("Batch Open Dialog");
				
				int val = filechooser.showOpenDialog(self);
				
				if(val == JFileChooser.APPROVE_OPTION)
				{
					log.info("New Batch Choosen");
					
					// Get list of files choosen
					File[] selectedFiles = filechooser.getSelectedFiles();
					
					// Sort Files Alpha Numerically by FileName
					Arrays.sort(selectedFiles, new AlphanumFileNameComparator());
					
					// The total
					int numBatchesToLoad = selectedFiles.length;
					
					/*
					 * Loadable setup section
					 */
					
					// Names and Index order
					filePaths = new String[numBatchesToLoad];
					indexes = new int[numBatchesToLoad];
					
					// Populate data structures for Loadable
					for(int f = 0; f < numBatchesToLoad; f++)
					{
						// Add the path to the list
						filePaths[f] = selectedFiles[f].getAbsolutePath();
						
						// Set the index order (as is)
						indexes[f] = f;
					}
					
					// BatchTab is the "Loadable" here to avoid swing worker(GUI) related code in the batch manager.
					LoadableTask task = new LoadableTask(self);
					
					// Send progress to the GlobalProgressMonitor
					task.addPropertyChangeListener(GlobalProgressMonitor.getInstance());
					
					// BatchTab needs to know if batches failed to be added
					task.addPropertyChangeListener(self);
					
					task.execute();
				}
			}
		});
		btnAdd.setIcon(IconManager.retrieveIcon(IconIndex.addBatch32));
		toolBar.add(btnAdd);
		
		btnRemove = new JButton();
		btnRemove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String[] info = getSelectedBatchInfo();
				
				if(info != null)
				{
					StringBuilder sb = new StringBuilder();
					
					String batch = "Batch";
					String name = "Name";
					String progress = "Progress (%)";
					String batchVal = info[0];
					String nameVal = info[1];
					String progressVal = info[2];
					
					int pad = progress.length() + 2;
					
					String font = "monospace";
					
					sb.append("<html>");
					sb.append("<h3>");
					sb.append("Remove Batch ?");
					sb.append("</h3>");
					sb.append("<font face='");
					sb.append(font);
					sb.append("'>");
					sb.append(StringUtils.rightPad(batch, pad, '\u00A0'));
					sb.append(" : ");
					sb.append("<font color=red>");
					sb.append(batchVal);
					sb.append("</font>");
					sb.append("<br>");
					sb.append(StringUtils.rightPad(name, pad, '\u00A0'));
					sb.append(" : ");
					sb.append("<font color=red>");
					sb.append(nameVal);
					sb.append("</font>");
					sb.append("<br>");
					sb.append(StringUtils.rightPad(progress, pad, '\u00A0'));
					sb.append(" : ");
					sb.append("<font color=red>");
					sb.append(progressVal);
					sb.append("</font>");
					sb.append("</font>");
					sb.append("</html>");
					
					JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
					
					messagePanel.add(new JLabel(sb.toString()));
					
					int result = JOptionPane.showConfirmDialog(self, messagePanel, "Remove Batch", JOptionPane.YES_NO_OPTION);
					
					if(result == JOptionPane.YES_OPTION)
					{
						removeBatch();
					}
					
				}
			}
		});
		btnRemove.setIcon(IconManager.retrieveIcon(IconIndex.removeBatch32));
		toolBar.add(btnRemove);
		toolBar.addSeparator();
		
		btnStart.setIcon(IconManager.retrieveIcon(IconIndex.start32));
		toolBar.add(btnStart);
		
		btnStop = new JButton();
		btnStop.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				stopBatch();
			}
		});
		btnStop.setIcon(IconManager.retrieveIcon(IconIndex.stop32));
		toolBar.add(btnStop);
		
		toolBar.addSeparator();
		
		btnMoveLast = new JButton();
		btnMoveLast.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				moveLast();
			}
		});
		
		btnMoveFirst = new JButton();
		btnMoveFirst.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				moveFirst();
			}
		});
		btnMoveFirst.setIcon(IconManager.retrieveIcon(IconIndex.moveToFront32));
		toolBar.add(btnMoveFirst);
		
		btnMoveForward = new JButton();
		btnMoveForward.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				moveForward();
			}
		});
		btnMoveForward.setIcon(IconManager.retrieveIcon(IconIndex.moveForward32));
		toolBar.add(btnMoveForward);
		
		btnMoveBackward = new JButton();
		btnMoveBackward.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				moveBackward();
			}
		});
		btnMoveBackward.setIcon(IconManager.retrieveIcon(IconIndex.moveBackward32));
		toolBar.add(btnMoveBackward);
		btnMoveLast.setIcon(IconManager.retrieveIcon(IconIndex.moveToBack32));
		toolBar.add(btnMoveLast);
		
		toolBar.addSeparator();
		
		// Icons or Text Only
		if(buttonText)
		{
			btnAdd.setText("Add");
			btnRemove.setText("Remove");
			btnStart.setText("Start");
			btnStop.setText("Stop");
			btnMoveForward.setText("Forward");
			btnMoveBackward.setText("Backward");
			btnMoveFirst.setText("First");
			btnMoveLast.setText("Last");
		}
	}
	
	private void createBatchInfoPanel()
	{
		batchInfo = new TablePanel<String, SimpleInfoRowItem>(SimpleInfoRowItem.class, "Batch Info", false, false);
		batchInfo.setColumWidth(0, 125);
		batchInfo.setMinimumSize(new Dimension(rightPanelsMinWidth, 150));
		batchInfo.setPreferredSize(new Dimension(rightPanelsMinWidth, 150));
		batchInfo.setDefaultRenderer(Object.class, new EmptyCellColorRenderer());
		
		batchInfo.addColumRenderer(new HeaderRowRenderer(batchInfo.getJTable()), 0);
		batchInfo.setVisible(false);
	}
	
	private void createBatchQueuedAndCompletePanel()
	{
		// Left Tables (Queue and Completed)
		batchQueuedAndCompletePanel = new JPanel();
		
		// Top Queue Batches Table
		GridBagLayout gbl_batchQueuedCompletePanel = new GridBagLayout();
		gbl_batchQueuedCompletePanel.columnWidths = new int[]
		{
			0, 0
		};
		gbl_batchQueuedCompletePanel.rowHeights = new int[]
		{
			0, 0
		};
		gbl_batchQueuedCompletePanel.columnWeights = new double[]
		{
			1.0, Double.MIN_VALUE
		};
		gbl_batchQueuedCompletePanel.rowWeights = new double[]
		{
			1.0, 1.0
		};
		batchQueuedAndCompletePanel.setLayout(gbl_batchQueuedCompletePanel);
		
		batchQueuedTable = new TablePanel<Integer, BatchQueueRowItem>(BatchQueueRowItem.class, "Queued", true, true);
		
		// Batch State
		batchQueuedTable.addColumRenderer(new BooleanIconRenderer(IconManager.retrieveIcon(IconIndex.start16), IconManager.retrieveIcon(IconIndex.stop16)),
		statusColumn);
		
		// Progress Column uses a progress bar for display
		batchQueuedTable.addColumRenderer(new ProgressBarTableCellRenderer(batchQueuedTable.getJTable()), progressColumn);
		
		GridBagConstraints gbc_batchQueuedTable = new GridBagConstraints();
		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;
		
		gbc_batchQueuedTable.fill = GridBagConstraints.BOTH;
		gbc_batchQueuedTable.gridx = 0;
		gbc_batchQueuedTable.gridy = 0;
		
		batchQueuedTable.setColumWidth(positionColumn, numericColumnWidth);
		batchQueuedTable.setColumWidth(idColumn, numericColumnWidth);
		
		// batchQueuedTable.setColumWidth(nameColumn, 175);
		batchQueuedTable.setColumWidth(statusColumn, iconColumnWidth);
		batchQueuedTable.setColumWidth(progressColumn, progressColumnWidth);
		batchQueuedTable.setColumWidth(estimatedFinishColumn, dateColumnWidth);
		// batchQueuedTable.setColumWidth(6, 40);
		// batchQueuedTable.setColumWidth(7, 40);
		// batchQueuedTable.setColumWidth(8, 60);
		
		batchQueuedAndCompletePanel.add(batchQueuedTable, gbc_batchQueuedTable);
		
		// Bottom Completed Batches
		batchCompletedTable = new TablePanel<Integer, BatchCompletedRowItem>(BatchCompletedRowItem.class, "Completed", true, true);
		
		GridBagConstraints gbc_batchCompleteTable = new GridBagConstraints();
		gbc_batchCompleteTable.gridx = 0;
		gbc_batchCompleteTable.gridy = 1;
		gbc_batchCompleteTable.fill = GridBagConstraints.BOTH;
		
		batchCompletedTable.setColumWidth(0, numericColumnWidth);
		batchCompletedTable.setColumWidth(2, timeColumnWidth);
		batchCompletedTable.setColumWidth(3, dateColumnWidth);
		// batchCompletedTable.setColumWidth(3, 50);
		
		batchQueuedAndCompletePanel.add(batchCompletedTable, gbc_batchCompleteTable);
	}
	
	private void clearQueuedSelection()
	{
		queuedSelectedBatchRowIndex = -1;
		
		batchQueuedTable.clearSelection();
	}
	
	private void clearCompletedSelection()
	{
		completedSelectedBatchRowIndex = -1;
		
		batchCompletedTable.clearSelection();
	}
	
	private void registerTableMouseListeners()
	{
		batchQueuedTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();
					
					queuedOrCompleted = 1;
					
					if(queuedSelectedBatchRowIndex == table.rowAtPoint(p))
					{
						queuedOrCompleted = 0;
						
						clearQueuedSelection();
					}
					else
					{
						queuedSelectedBatchRowIndex = table.rowAtPoint(p);
						
						updateBatchInfo(queuedOrCompleted);
					}
					
					// Clear any selection in the completed table
					clearCompletedSelection();
				}
			}
		});
		
		batchCompletedTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if(e.getButton() == 1)
				{
					JTable table = (JTable) e.getSource();
					Point p = e.getPoint();
					
					queuedOrCompleted = 2;
					
					if(completedSelectedBatchRowIndex == table.rowAtPoint(p))
					{
						queuedOrCompleted = 0;
						
						clearCompletedSelection();
					}
					else
					{
						completedSelectedBatchRowIndex = table.rowAtPoint(p);
						
						updateBatchInfo(queuedOrCompleted);
					}
					
					// Clear any selection in the queued table
					clearQueuedSelection();
				}
			}
		});
	}
	
	private void updateBatchInfo(int srcTable)
	{
		int batchId = 0;
		
		// Should the data fetch be skipped
		boolean skipData = false;
		
		// find the table and batch the row relates to.
		if(srcTable > 0)
		{
			if(srcTable == 1)
			{
				if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0))
				{
					queuedSelectedBatchRowIndex = 0;
					batchQueuedTable.clearSelection();
					srcTable = 0;
					
					// invalid row selected
					skipData = true;
				}
				else
				{
					batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
				}
			}
			else
			{
				
				if((completedSelectedBatchRowIndex < 0) || (batchCompletedTable.getRowsCount() == 0))
				{
					completedSelectedBatchRowIndex = 0;
					batchCompletedTable.clearSelection();
					srcTable = 0;
					
					// invalid row selected
					skipData = true;
				}
				else
				{
					batchId = (int) batchCompletedTable.getValueAt(completedSelectedBatchRowIndex, 0);
				}
			}
		}
		else
		{
			// No table selected
			skipData = true;
		}
		
		if(!skipData)
		{
			String info[] = batchManager.getBatchInfo(batchId);
			
			// Batch Info
			int batchInfoLength = info.length;
			
			// Batch was just selected
			if(batchInfo.getRowsCount() <= 0)
			{
				for(int i = 0; i < batchInfoLength; i += 2)
				{
					batchInfo.addRow(new SimpleInfoRowItem(info[i], info[i + 1]));
				}
			}
			else
			{
				// Displayed info and returned info not equal in length
				// (generated batch / generating batch / non generated batch)
				if(batchInfo.getRowsCount() != (info.length / 2))
				{
					batchInfo.clearTable();
					
					for(int i = 0; i < batchInfoLength; i += 2)
					{
						batchInfo.addRow(new SimpleInfoRowItem(info[i], info[i + 1]));
					}
					
				}
				
				for(int i = 0; i < batchInfoLength; i += 2)
				{
					batchInfo.updateRow(info[i], new SimpleInfoRowItem(info[i], info[i + 1]));
				}
			}
			
		}
		else
		{
			// Clear Batch info tables
			batchInfo.clearTable();
		}
	}
	
	private void startBatch()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0))
		{
			queuedSelectedBatchRowIndex = 0;
			
			// invalid row selected
			return;
		}
		
		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
		
		batchManager.setBatchEnabled(batchId, true);
	}
	
	private String[] getSelectedBatchInfo()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0))
		{
			queuedSelectedBatchRowIndex = 0;
			
			// invalid row selected
			return null;
		}
		
		String batchId = String.valueOf((int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn));
		String name = (String) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, nameColumn);
		String progress = String.valueOf((int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, progressColumn));
		
		return new String[]
		{
			batchId, name, progress
		};
	}
	
	private void removeBatch()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0))
		{
			queuedSelectedBatchRowIndex = 0;
			
			// invalid row selected
			return;
		}
		
		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
		
		clearQueuedSelection();
		
		batchQueuedTable.removeRow(batchId);
		batchManager.removeBatch(batchId);
	}
	
	private void stopBatch()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0))
		{
			// invalid row selected
			return;
		}
		
		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
		
		batchManager.setBatchEnabled(batchId, false);
	}
	
	private void moveLast()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0) || (queuedSelectedBatchRowIndex == (batchQueuedTable.getRowsCount()
		- 1)))
		{
			// invalid row selected
			return;
		}
		
		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
		
		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToEnd...");
		
		batchManager.moveToEnd(batchId);
		
		queuedSelectedBatchRowIndex = batchQueuedTable.getRowsCount() - 1;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);
		
		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");
	}
	
	private void moveBackward()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0) || (queuedSelectedBatchRowIndex == (batchQueuedTable.getRowsCount()
		- 1)))
		{
			// invalid row selected
			return;
		}
		
		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
		
		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToBack...");
		
		batchManager.moveBackward(batchId);
		
		queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex + 1;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);
		
		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");
	}
	
	private void moveForward()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0))
		{
			// invalid row selected
			return;
		}
		
		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
		
		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToFront...");
		
		batchManager.moveForward(batchId);
		
		queuedSelectedBatchRowIndex = queuedSelectedBatchRowIndex - 1;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);
	}
	
	private void moveFirst()
	{
		if((queuedSelectedBatchRowIndex < 0) || (batchQueuedTable.getRowsCount() == 0))
		{
			queuedSelectedBatchRowIndex = 0;
			
			// invalid row selected
			return;
		}
		
		int batchId = (int) batchQueuedTable.getValueAt(queuedSelectedBatchRowIndex, idColumn);
		
		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " moveToFront...");
		
		batchManager.moveToFront(batchId);
		
		queuedSelectedBatchRowIndex = 0;
		batchQueuedTable.setSelection(queuedSelectedBatchRowIndex, 0);
		
		log.debug("queuedSelectedBatchRowIndex " + queuedSelectedBatchRowIndex + " Batch ID " + batchId + " MOVED...");
	}
	
	@Subscribe
	public void batchQueuePositionChanged(BatchPositionEvent event)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Batch batch = event.getBatch();
				batchQueuedTable.updateRow(batch.getBatchId(), new BatchQueueRowItem(batch));
				
				log.debug("batchQueuePositionChanged " + batch.getBatchId() + " Pos" + batch.getPosition());
			}
		});
	}
	
	@Subscribe
	public void batchProgress(BatchProgressEvent event)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Batch batch = event.getBatch();
				
				batchQueuedTable.updateRow(batch.getBatchId(), new BatchQueueRowItem(batch));
			}
		});
	}
	
	@Subscribe
	public void batchAdded(BatchAddedEvent event)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// add new row
				batchQueuedTable.addRow(new BatchQueueRowItem(event.getBatch()));
			}
		});
	}
	
	@Subscribe
	public void batchFinished(BatchFinishedEvent event)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Batch batch = event.getBatch();
				
				log.info("Batch Finished " + batch.getBatchId());
				
				// remove row
				batchQueuedTable.removeRow(batch.getBatchId());
				
				batchCompletedTable.addRow(new BatchCompletedRowItem(batch.getBatchId(), batch.getFileName(), TimeString.timeInMillisAsFormattedString(batch
				.getRunTime(), TimeStringFormat.DHMS), batch.getFinished()));
				
				queuedSelectedBatchRowIndex = -1;
				
				if(queuedOrCompleted == 1)
				{
					queuedOrCompleted = 0;
				}
				
				batchQueuedTable.clearSelection();
			}
		});
	}
	
	// Batch Manager
	public void setBatchManager(BatchManager batchManager)
	{
		this.batchManager = batchManager;
	}
	
	@Override
	public boolean load(int index)
	{
		if(requester != null)
		{
			MessageBox.popup("Cannot add batches at this at this time", self);
			
			return false;
		}
		
		return batchManager.addBatch(filePaths[index]);
	}
	
	@Override
	public int[] getIndexes()
	{
		return indexes;
	}
	
	@Override
	public boolean makeDelegate(Loadable requester)
	{
		if(this.requester != null || requester == null)
		{
			return false;
		}
		
		this.requester = requester;
		
		return true;
	}
	
	@Override
	public boolean delegateLoad(Loadable requester, String info)
	{
		if(this.requester == requester)
		{
			return batchManager.addBatch(info);
		}
		
		return false;
	}
	
	@Override
	public void releaseDelegate(Loadable requester)
	{
		if(this.requester == requester)
		{
			this.requester = null;
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		Common.handlePropertyChangeEvent(self, e, indexes, filePaths);
	}
}
