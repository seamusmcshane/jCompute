package tools.SurfaceChart.Surface;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class Bar
{
	private ModelInstance bar;
	private Model barModel;
	
	private float gridTrans;
	private float barSize;
	private float barMax = 100f;
	private float height;
	private float scaleReset;
	
	private int[] pallete;
	
	public Bar(ModelBuilder modelBuilder, float gridTrans, float barSize, int[] pallete)
	{
		this.barSize = barSize;
		this.pallete = pallete;
		this.gridTrans = gridTrans;
		
		barModel = modelBuilder.createBox(barSize, barSize, barMax,
				new Material(ColorAttribute.createDiffuse(Color.WHITE)), Usage.Position | Usage.Normal);
		bar = new ModelInstance(barModel);
		// bar.transform.trn(0,0,height/2);
		height = barMax;
		scaleReset = height / barMax;
		bar.transform.trn(0, 0, (height / 2));
		setHeight(100);
		// bar.transform.rotate(1, 0, 0, 90);
	}
	
	public void setBarLocation(int x, int y)
	{
		bar.transform.trn((y * barSize) - gridTrans + (barSize / 2), (x * barSize) - gridTrans + (barSize / 2), 0);
	}
	
	public void setHeight(float percentage)
	{
		float scale = (percentage / 100f);
		float newHeight = barMax * scale;
		
		// Reset Transform + scaling
		bar.transform.scale(1f, 1f, scaleReset);
		bar.transform.trn(0, 0, -(height / 2));
		
		// Cannot scale by 0.
		if(scale <= 0)
		{
			scale = 0.001f;
		}
		
		// New Transform and scaling
		bar.transform.scale(1f, 1f, scale);
		bar.transform.trn(0, 0, (newHeight / 2));
		
		int pval = (int) (scale * (pallete.length - 1));
		
		// New Colour
		bar.materials.get(0).set(ColorAttribute.createDiffuse(new Color(pallete[pval])));
		
		// Capture height
		height = newHeight;
		
		// Capture correct scale to reset by
		scaleReset = barMax / height;
	}
	
	public ModelInstance getInstance()
	{
		return bar;
	}
	
	public float getHeight()
	{
		return height;
	}
	
	public void dispose()
	{
		barModel.dispose();
	}
	
}
