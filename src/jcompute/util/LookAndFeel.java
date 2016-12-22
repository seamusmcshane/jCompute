package jcompute.util;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LookAndFeel
{
	private static Logger log = LogManager.getLogger(LookAndFeel.class);
	
	/* If possible set the requested look and feel else use system default */
	public static void setLookandFeel(String conflookandfeel)
	{
		// To simplify name matching.
		String lcaseName = conflookandfeel.toLowerCase();
		
		// Default to the system provided look and feel (But don't set it)
		String lookandfeel = UIManager.getSystemLookAndFeelClassName();

		UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
				
		for(int i = 0; i < lookAndFeels.length; i++)
		{
			String name = lookAndFeels[i].getClassName().toLowerCase();
			
			log.info("Look : " + name);
			
			if(name.contains(lcaseName))
			{
				lookandfeel = lookAndFeels[i].getClassName();
				break;
			}
		}
		
		try
		{
			UIManager.setLookAndFeel(lookandfeel);
		}
		catch(ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch(InstantiationException e1)
		{
			e1.printStackTrace();
		}
		catch(IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch(UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
		
		log.info("Selected : " + lookandfeel);
	}
}
