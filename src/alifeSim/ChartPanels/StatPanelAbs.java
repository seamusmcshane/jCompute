package alifeSim.ChartPanels;

import javax.swing.JPanel;

public abstract class StatPanelAbs extends JPanel
{
	private static final long serialVersionUID = -6980172412079040658L;
	public abstract void update();
	public abstract void destroy();
}
