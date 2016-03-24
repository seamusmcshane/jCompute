package tools.ReportViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import jCompute.Gui.Component.Swing.MessageBox;

public class PDFViewer extends JFrame
{
	private static final long serialVersionUID = 1320429389739545477L;
	
	private static JFrame pdfViewer;
	
	private ImageViewerPanel viewerPanel;
	private PDFPageRenderer ren;
	
	private final float SCALE_REF = 1f;
	
	public static void main(String args[]) throws IOException
	{
		pdfViewer = new PDFViewer();
	}
	
	public PDFViewer() throws IOException
	{
		pdfViewer = this;
		setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent w)
			{
				doProgramExit();
			}
		});
		
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(500, 500));
		
		viewerPanel = new ImageViewerPanel();
		
		getContentPane().add(viewerPanel, BorderLayout.CENTER);
		
		JToolBar toolBar = new JToolBar();
		getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[]
		{
			"Fit Window", "Fit Width", "12", "25", "50", "75", "100", "125", "150", "200", "300"
		}));
		comboBox.setFocusable(false);
		
		comboBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				@SuppressWarnings("unchecked")
				String sval = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
				
				switch(sval)
				{
					case "Fit Window":
						viewerPanel.setScaleModeFitWindowSize();
						System.out.println("Fit Window");
					break;
					case "Fit Width":
						viewerPanel.setScaleModeFitWindowWidth();
						System.out.println("Fit Width");
					break;
					case "12":
					case "25":
					case "50":
					case "75":
					case "100":
					case "125":
					case "150":
					case "200":
					case "300":
						// Int's
					default:
						// dunno's
						
						// 100%
						int iVal = 100;
						try
						{
							iVal = Integer.parseInt(sval);
						}
						catch(NumberFormatException ex)
						{
							System.out.println("Non a number");
						}
						
						float newScale = (SCALE_REF * iVal) * 0.01f;
						System.out.println("Zooming on " + newScale);
						viewerPanel.setScaleModeScale(newScale);
					// viewerPanel.setScaleMode(newScale, 0);
					break;
				}
				
			}
		});
		
		JButton btnLoadFile = new JButton("Load File");
		btnLoadFile.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				requestPDFFile();
			}
		});
		toolBar.add(btnLoadFile);
		
		toolBar.add(comboBox);
		
		MouseAdapter mouseInputs = new MouseAdapter()
		{
			float baseInc = 0.006f;
			
			final float[] cpos = new float[2];
			
			@Override
			public void mousePressed(MouseEvent e)
			{
				// TODO remove
				viewerPanel.allowImageSelect(false);
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				viewerPanel.allowImageSelect(true);
			}
			
			@Override
			public void mouseDragged(MouseEvent e)
			{
				viewerPanel.translate(cpos[0] - e.getX(), cpos[1] - e.getY());
				
				cpos[0] = e.getX();
				cpos[1] = e.getY();
			}
			
			// Need to track mouse or it will snap the view
			@Override
			public void mouseMoved(MouseEvent e)
			{
				cpos[0] = e.getX();
				cpos[1] = e.getY();
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				int amount = e.getScrollAmount() * e.getWheelRotation();
				
				float inc = baseInc * -amount;
				
				System.out.println("inc " + inc);
				
				float adjInc = inc;
				if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
				{
					adjInc = inc * 3;
				}
				
				viewerPanel.adjScaleModeScale(adjInc);
				// viewerPanel.setScaleMode(scale, 0);
			}
			
		};
		
		pdfViewer.addMouseMotionListener(mouseInputs);
		pdfViewer.addMouseWheelListener(mouseInputs);
		pdfViewer.addMouseListener(mouseInputs);
		
		viewerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "PrevPage");
		viewerPanel.getActionMap().put("PrevPage", new PageAction(PageAction.PREV_PAGE));
		viewerPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "NextPage");
		viewerPanel.getActionMap().put("NextPage", new PageAction(PageAction.NEXT_PAGE));
		
		pdfViewer.pack();
		pdfViewer.setVisible(true);
		
		requestPDFFile();
	}
	
	private class PageAction extends AbstractAction
	{
		/**
		 *
		 */
		private static final long serialVersionUID = 5316940342199300618L;
		public static final int PREV_PAGE = 0;
		public static final int NEXT_PAGE = 1;
		
		private int actionId;
		
		PageAction(int actionId)
		{
			this.actionId = actionId;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				switch(actionId)
				{
					case PREV_PAGE:
						drawImage(ren.getImagePrev());
					break;
					case NEXT_PAGE:
						drawImage(ren.getImageNext());
					break;
				}
			}
			catch(IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void drawImage(PageImage image)
	{
		if(image == null)
		{
			MessageBox.popup("No Page", pdfViewer);
		}
		
		viewerPanel.setImage(image);
	}
	
	public void requestPDFFile()
	{
		final JFileChooser filechooser = new JFileChooser(new File("."));
		
		filechooser.setDialogTitle("Choose Directory");
		filechooser.setMultiSelectionEnabled(false);
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		filechooser.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
			}
			
			@Override
			public String getDescription()
			{
				return "Portable Document Format";
			}
		});
		
		int val = filechooser.showOpenDialog(filechooser);
		
		if(val == JFileChooser.APPROVE_OPTION)
		{
			String fullPath = filechooser.getSelectedFile().getPath();
			System.out.println("Path : " + fullPath);
			
			try
			{
				if(ren != null)
				{
					ren.close();
				}
				
				ren = new PDFPageRenderer(fullPath);
				
				try
				{
					drawImage(ren.getPageImage(0, 0));
				}
				catch(IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
			catch(IOException e)
			{
				System.out.println("PDF Report Error");
				e.printStackTrace();
			}
			
			System.out.println("Report Finished");
		}
		else
		{
			System.out.println("Report Cancelled");
		}
		
	}
	
	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				String message;
				message = "Do you want to quit?";
				
				JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
				
				// Center Dialog on the GUI
				JDialog dialog = pane.createDialog(pdfViewer, "Close Application");
				
				dialog.pack();
				dialog.setVisible(true);
				
				int value = ((Integer) pane.getValue()).intValue();
				
				if(value == JOptionPane.YES_OPTION)
				{
					// Do EXIT
					System.exit(0);
				}
			}
		});
		
	}
}
