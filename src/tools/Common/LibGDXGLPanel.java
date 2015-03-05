package tools.Common;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;

public class LibGDXGLPanel extends JPanel
{
	private static final long serialVersionUID = 1994385404068550662L;

	public LibGDXGLPanel()
	{
		this.add(new JLabel("No GLEnv Set"));
	}
	
	public LibGDXGLPanel(ApplicationListener glEnv)
	{
		LwjglApplicationConfiguration.disableAudio = true;
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
				
		cfg.title = "PhasePlot3d";
		cfg.samples = 8;
		cfg.vSyncEnabled = true;
		cfg.useGL30 = false;
		
		LwjglCanvas canvas = new LwjglCanvas(glEnv,cfg);		
		
		this.setLayout(new BorderLayout());
		this.add(canvas.getCanvas(), BorderLayout.CENTER);
		canvas.getGraphics().setVSync(true);
	}	
}
