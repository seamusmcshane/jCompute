package jCompute.Gui.Component.Swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class MessageBox
{
	public static boolean popup(String text, Component parentComponent)
	{
		if(GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		else
		{
			JTextPane messageHolder = new JTextPane();
			
			messageHolder.setContentType("text");
			messageHolder.setText(text);
			messageHolder.setEditable(false);
			messageHolder.setCaretPosition(0);
			
			JScrollPane messageBox = new JScrollPane(messageHolder);
			messageBox.setPreferredSize(new Dimension(700, 200));
			messageBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			JOptionPane.showMessageDialog(parentComponent, messageBox);
			
			return true;
		}
	}
}