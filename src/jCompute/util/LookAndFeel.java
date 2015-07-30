package jCompute.util;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LookAndFeel
{
	/* If possible set the requested look and feel else use system default */
	public static void setLookandFeel(String conflookandfeel)
	{
		// Default to the system provided look and feel
		String lookandfeel = UIManager.getSystemLookAndFeelClassName();
		
		UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
		
		for(int i = 0; i < lookAndFeels.length; i++)
		{
			if(lookAndFeels[i].getClassName().toLowerCase().contains(conflookandfeel))
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
	}
}
