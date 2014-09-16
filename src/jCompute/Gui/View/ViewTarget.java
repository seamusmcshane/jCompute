package jCompute.Gui.View;

public interface ViewTarget
{
	public boolean hasViewCam();
	
	public ViewCam getSimViewCam();
	
	public void draw(View simView,boolean viewRangeDrawing,boolean viewsDrawing);
	
	public String getInfo();
}
