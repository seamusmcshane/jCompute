package tools.PhasePlot3d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class VertexModel
{
	private ModelBuilder modelBuilder;
	private ModelInstance modelInstance;
	private boolean dynamic;
	
	public VertexModel(boolean dynamic)
	{
		this.dynamic = dynamic;

		modelBuilder = new ModelBuilder();		
	}
	
	public void setVertices(float[] vertices,int primitiveType)
	{
		if(modelInstance!=null)
		{
			modelInstance.model.dispose();
			
			modelInstance = null;
		}
		
		int neededIndicies = (vertices.length/7);
		
		int splitIndicie = 2;
		
		// Max indicie is the smaller of the following (if vertices.len < maxIndex then we do not need to split the list)
		int maxIndicie = Math.min(splitIndicie, neededIndicies);
		
		int meshes = neededIndicies/maxIndicie;
		int last = neededIndicies%maxIndicie;
		
		/*if(last > 0)
		{
			splits+=1;
			meshes = splits;
		}
		else if(last == 0)
		{
			//splits-=1;
			meshes=splits-1;
		}*/		
		
		int verticesSplit = splitIndicie*7;
		int lastSplitSize = last*7;

		System.out.println("setVertices vertices " + vertices.length);
		System.out.println("setVertices neededIndicies " + neededIndicies);
		System.out.println("setVertices verticesSplit " + verticesSplit);
		System.out.println("setVertices lastSplitSize " + lastSplitSize);
		//System.out.println("setVertices splits " + splits);
		System.out.println("setVertices last " + last);
		System.out.println("setVertices maxIndicie " + maxIndicie);		
		System.out.println("setVertices Meshes " + meshes);

		// Begin new Model
		modelBuilder.begin();
     
        modelBuilder.part("mesh_"+1, verticesToMesh(vertices), primitiveType, new Material(new BlendingAttribute()));

		
		/*if(meshes == 1)
		{
            modelBuilder.part("mesh_"+1, verticesToMesh(vertices), primitiveType, new Material(new BlendingAttribute()));
		}
		else
		{	*/
			/*float[] subVertices = new float[verticesSplit];
			System.arraycopy(vertices, 0, subVertices, 0, verticesSplit);
			modelBuilder.part("mesh_"+0, verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));*/
			/*
			
			float[] subVertices;
			
			int vLen = vertices.length;
			int v=0;
			while(v<vLen)
			{
				
				if(v%verticesSplit == 0)
				{
					
					int start  =v;
					
					System.out.println("Start " + v);
					
					if( (vLen-v) < verticesSplit)
					{
						System.out.println("vLEn " + v);
					}
					
				}
				
				v++;
			}
			*/
			
			/*int m=0;
			int i=0;
			for(i=1;i<v;i++)
			{
				if(i%verticesSplit == 0)
				{
					System.out.println(i+" MESH");
					subVertices = new float[verticesSplit+7];
					
					int start = i-verticesSplit;
					int end = (i+7);
					System.out.println("start " + start + " end " + end);

					System.arraycopy(vertices, start, subVertices, 0, verticesSplit+7);
					
					modelBuilder.part("mesh_"+(i++), verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));
					m++;
				}
			}
			
			int left = m*verticesSplit;
			System.out.println("I " + i + " left " + left);*/
			
			
			/*int v = vertices.length;
			float[] subVertices;
			int m=0;
			while(v>verticesSplit)
			{
				subVertices = new float[verticesSplit+7];
				
				System.out.println("v " + ((v-verticesSplit)-7) + " || " + (verticesSplit+7));

				System.arraycopy(vertices, (v-verticesSplit)-7, subVertices, 0, verticesSplit+7);
				
				modelBuilder.part("mesh_"+(m++), verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));
				
				v-=verticesSplit;
			}
			
			System.out.println("vleft" + v);*/

			
			/*for(int i=0;i<meshes;i++)
			{
				System.out.println("v" + i*7*meshes);

				int start = i*verticesSplit;
				int end = ((i+1)*verticesSplit)%v;
				
				System.out.println("Start " + start + " End " + end);
				

				
				subVertices = new float[split];
				
				//System.arraycopy(vertices, start, subVertices, 0, count);

				//modelBuilder.part("mesh_"+i, verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));
				
				//System.out.println(v + " Start " + start +  " COUNT " + count);
			}*/
			
			/*float[] subVertices;

			int verticies = vertices.length;
			boolean lastMesh = false;

			int m = 0;
			while(m<meshes)
			{
				int splitSize=verticesSplit;
				
				if(m==(meshes-1))
				{
					splitSize = lastSplitSize;
				}

				subVertices = new float[splitSize];
				
				System.arraycopy(vertices, m*verticesSplit, subVertices, 0, subVertices.length);

				modelBuilder.part("mesh_"+m, verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));
				
				System.out.println("LOOP setVertices splitSize " + splitSize);
				
				// Decrement
				m++;
				
				if(m<(meshes-1))
				{
	            	modelBuilder.node();					
				}
			}		*/	

		/*	for(int s=0;s<meshes;s++)
			{
				if(!lastMesh)
				{
					subVertices = new float[verticesSplit];
				}
				else
				{
					subVertices = new float[lastSplitSize];
				}
				
				System.out.println("setVertices s " + s);
				System.out.println("setVertices s*verticesSplit " + s*verticesSplit);
				System.out.println("setVertices subVertices.length " + subVertices.length);
				System.out.println("setVertices lastMesh " + lastMesh);
				
				System.arraycopy(vertices, s*verticesSplit, subVertices, 0, subVertices.length);
				
				modelBuilder.part("mesh_"+s, verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));
	            
	            if(s+1 == meshes-1)
	            {
	            	lastMesh = true;
	            }
	            else
	            {
	            	Node node = modelBuilder.node();
	            }
			}*/
		//}

		Model model = modelBuilder.end();
        
        modelInstance = new ModelInstance(model);
        
        if(!dynamic)
        {
        	modelBuilder = null;
        }
	}
	
	public void setVerticesALT(float[] vertices,int primitiveType)
	{
		if(modelInstance!=null)
		{
			modelInstance.model.dispose();
			
			modelInstance = null;
		}
		
		int neededIndicies = (vertices.length/7);
		
		int splitIndicie = 2;
		
		// Max indicie is the smaller of the following (if vertices.len < maxIndex then we do not need to split the list)
		int maxIndicie = Math.min(splitIndicie, neededIndicies);
		
		int splits = neededIndicies/maxIndicie;
		int last = neededIndicies%maxIndicie;
		int meshes = 1;
		
		if(last > 0)
		{
			splits+=1;
		}
		else if(last == 0)
		{
			//splits-=1;
		}
		
		meshes = splits;
		
		int verticesSplit = splitIndicie*7;
		int lastSplitSize = last*7;
		
		System.out.println("setVertices vertices " + vertices.length);
		System.out.println("setVertices neededIndicies " + neededIndicies);
		System.out.println("setVertices verticesSplit " + verticesSplit);
		System.out.println("setVertices lastSplitSize " + lastSplitSize);
		System.out.println("setVertices splits " + splits);
		System.out.println("setVertices last " + last);
		System.out.println("setVertices maxIndicie " + maxIndicie);
		
		System.out.println("setVertices Meshes " + meshes);

		// Begin new Model
		modelBuilder.begin();
     
		if(meshes == 1)
		{
            modelBuilder.part("mesh_"+1, verticesToMesh(vertices), primitiveType, new Material(new BlendingAttribute()));
		}
		else if(meshes>1)
		{
			float[] subVertices;

			boolean lastMesh = false;

			for(int s=0;s<meshes;s++)
			{
				if(!lastMesh)
				{
					subVertices = new float[verticesSplit];
				}
				else
				{
					subVertices = new float[lastSplitSize];
				}
				
				System.out.println("setVertices s " + s);
				System.out.println("setVertices s*verticesSplit " + s*verticesSplit);
				System.out.println("setVertices subVertices.length " + subVertices.length);
				System.out.println("setVertices lastMesh " + lastMesh);
				
				System.arraycopy(vertices, s*verticesSplit, subVertices, 0, subVertices.length);
				
				modelBuilder.part("mesh_"+s, verticesToMesh(subVertices), primitiveType, new Material(new BlendingAttribute()));
	            
	            if(s+1 == meshes-1)
	            {
	            	lastMesh = true;
	            }
	            else
	            {
	            	Node node = modelBuilder.node();
	            }
			}
		}

		Model model = modelBuilder.end();
        
        modelInstance = new ModelInstance(model);
        
        if(!dynamic)
        {
        	modelBuilder = null;
        }
	}
	
	public Mesh verticesToMesh(float[] vertices)
	{
		int iLen = (vertices.length/7);
		short[] indicies = new short[iLen];
		
		System.out.println("verticesToMesh vertices " + vertices.length);
		System.out.println("verticesToMesh points " + vertices.length/7);
		System.out.println("verticesToMesh indicies " + indicies.length);
		
		for(short i=0;i<iLen;i++)
		{
			indicies[i]=i;
		}
		
		Mesh mesh = new Mesh(true, vertices.length, iLen, new VertexAttribute(Usage.Position, 3, "a_position"),new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));
		mesh.setVertices(vertices);
		mesh.setIndices(indicies);
		
		return mesh;
	}
	
/*	public void setVerticesOLD(float[] vertices,int primitiveType)
	{
		if(modelInstance!=null)
		{
			modelInstance.model.dispose();
			
			modelInstance = null;
		}
				
        // Begin new Model
        modelBuilder.begin();
		
        Mesh[] meshes = decomposeInToMeshes(vertices);
        
    	for(int m=0;m<meshes.length;m++)
        {
    		modelBuilder.node();
        	        	
            modelBuilder.part("mesh_"+m, meshes[m], primitiveType, new Material(new BlendingAttribute()));
            
    		System.out.println("mesh_"+m);
        }
                
        Model model = modelBuilder.end();
        
        modelInstance = new ModelInstance(model);
        
        if(!dynamic)
        {
        	modelBuilder = null;
        }
	}
		*/
	public Mesh[] decomposeInToMeshes(float[] vertices)
	{
		int neededIndicies = (vertices.length/7);
		int maxIndex = 30000;
		int splits = neededIndicies/maxIndex;
		int last = neededIndicies%maxIndex;
		
		// Special case
		if(last == 0)
		{
			splits-=1;
			
			// if splits was 1
			/*if(splits<0)
			{
				splits = 1;
			}*/
		}

		int numMeshes = splits+1;

		System.out.println("Vertices " + vertices.length);
		System.out.println("Indices Needed " + neededIndicies);
		System.out.println("Indices Limit " + maxIndex);
		System.out.println("Splits " + splits);
		System.out.println("last " + last);
		System.out.println("numMeshes " + numMeshes);

		// Reused
		short[] indicies = new short[maxIndex];
		for(short i=0;i<maxIndex;i++)
		{
			indicies[i]=i;
		}		
		
		Mesh[] meshes;

		if(splits>0)
		{			
			// Multiple Meshes
			meshes = new Mesh[numMeshes];
			
			if(last==0)
			{
				for(int s=0;s<numMeshes;s++)
				{					
					meshes[s] = new Mesh(true, maxIndex, maxIndex, new VertexAttribute(Usage.Position, 3, "a_position"),new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));
					meshes[s].setVertices(vertices,s*maxIndex,maxIndex);
					meshes[s].setIndices(indicies,0,maxIndex);
					
					System.out.println("S " + s);
					System.out.println("s*maxIndex " + s*maxIndex);
					System.out.println("maxIndex " + maxIndex);
				}
			}
			else
			{
				for(int s=0;s<numMeshes-1;s++)
				{
					meshes[s] = new Mesh(true, vertices.length, indicies.length, new VertexAttribute(Usage.Position, 3, "a_position"),new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));
					meshes[s].setVertices(vertices,s*maxIndex,maxIndex);
					meshes[s].setIndices(indicies,s*maxIndex,maxIndex);
					
					System.out.println("s " + s);
				}
				
				meshes[numMeshes-1] = new Mesh(true, vertices.length, indicies.length, new VertexAttribute(Usage.Position, 3, "a_position"),new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));
				meshes[numMeshes-1].setVertices(vertices,(splits-1)*maxIndex,last);
				meshes[numMeshes-1].setIndices(indicies,0,last);
				
				System.out.println("s " + (numMeshes-1));
			}
		}
		else
		{
			// One Mesh
			
			numMeshes = 1;
			maxIndex = neededIndicies;
			
			meshes = new Mesh[numMeshes];
			
			meshes[0] = new Mesh(true, vertices.length, maxIndex, new VertexAttribute(Usage.Position, 3, "a_position"),new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));
			meshes[0].setVertices(vertices);
			meshes[0].setIndices(indicies,0,maxIndex);
			
		}
		
		return meshes;
	}
	
	private short[] createIndicies(short size)
	{
		short[] indicies = new short[size];
		for(short i=0;i<size;i++)
		{
			indicies[i]=i;
		}
		
		return indicies;
	}
	
	public ModelInstance getModelInstace()
	{
		return modelInstance;
	}
	
}
