package jcompute.gui.view;

import jcompute.gui.view.renderer.ViewRendererInf;

public interface ViewTarget
{
	public ViewRendererInf getRenderer();
	
	public String getInfo();
	
	public String getHelpTitleText();
	
	public String[] getHelpKeyList();
}
