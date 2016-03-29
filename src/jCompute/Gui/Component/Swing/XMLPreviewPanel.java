package jCompute.Gui.Component.Swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

public class XMLPreviewPanel extends JPanel implements PropertyChangeListener
{
	private static final long serialVersionUID = 7753963396925709079L;
	
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(XMLPreviewPanel.class);

	private RTextScrollPane scenarioEditorRTextScrollPane;
	private RSyntaxTextArea scenarioEditor;
	
	public XMLPreviewPanel()
	{
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(500, 300));
		setPreferredSize(new Dimension(500, 300));
		
		scenarioEditor = new RSyntaxTextArea();
		scenarioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		scenarioEditor = new RSyntaxTextArea();
		scenarioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		
		scenarioEditor.setCloseMarkupTags(false);
		scenarioEditor.setCloseCurlyBraces(false);
		scenarioEditor.setAnimateBracketMatching(false);
		scenarioEditor.setUseSelectedTextColor(false);
		scenarioEditor.setHyperlinksEnabled(false);
		scenarioEditor.setHighlightSecondaryLanguages(false);
		scenarioEditor.setRoundedSelectionEdges(false);
		scenarioEditor.setAutoIndentEnabled(false);
		scenarioEditor.setTabSize(2);
		scenarioEditor.setFadeCurrentLineHighlight(false);
		scenarioEditor.setBracketMatchingEnabled(false);
		scenarioEditor.setEditable(false);
		scenarioEditor.setHighlightCurrentLine(false);
		
		try
		{
			InputStream in = new FileInputStream(new File("editor-themes" + File.separator + "dark-mod.xml"));
			Theme theme = Theme.load(in);
			theme.apply(scenarioEditor);
			in.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		scenarioEditorRTextScrollPane = new RTextScrollPane(scenarioEditor);
		this.add(scenarioEditorRTextScrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		String pname = e.getPropertyName();
		JFileChooser source = (JFileChooser) e.getSource();
		
		File file = null;
		if(source.isMultiSelectionEnabled())
		{
			File[] files = source.getSelectedFiles();
			
			if(files.length > 0)
			{
				file = files[0];
			}
		}
		else
		{
			if(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(pname))
			{
				file = (File) e.getNewValue();
			}
		}
		
		if(file != null)
		{
			if((source.isMultiSelectionEnabled() && file.getName().toLowerCase().endsWith(".batch")) || (!source.isMultiSelectionEnabled() && file.getName()
			.toLowerCase().endsWith(".scenario")))
			{
				try
				{
					BufferedReader bufferedReader = new BufferedReader(new FileReader(source.getSelectedFile()));
					String sCurrentLine;
					scenarioEditor.setText("");
					
					while((sCurrentLine = bufferedReader.readLine()) != null)
					{
						scenarioEditor.append(sCurrentLine + "\n");
					}
					
					bufferedReader.close();
				}
				catch(FileNotFoundException e1)
				{
					log.error("File Not Found");
					e1.printStackTrace();
				}
				catch(IOException e1)
				{
					log.error("I/O Error");
					e1.printStackTrace();
				}
			}
			else
			{
				scenarioEditor.setText("Not Valid");
			}
			
		}
		else
		{
			scenarioEditor.setText("");
		}
		
	}
	
}
