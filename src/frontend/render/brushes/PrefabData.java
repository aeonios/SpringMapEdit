/**
 * PrefabData.java 
 * Created on 29.09.2009
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import java.io.File;

/**
 * @author Heiko Schmitt
 *
 */
public class PrefabData
{
	public int prefabID;
	
	public String name;
	public String category;
	public String heightmapFilename;
	public String texturemapFilename;
	public float heightZ;
	public float levelOverlap;
	
	public int heightmapID;
	public int texturemapID;
	public int featuremapID;
	
	/**
	 * 
	 */
	public PrefabData(int prefabID, String category, String name, float heightZ, float levelOverlap, File heightmapFile, File texturemapFile)
	{
		this.prefabID = prefabID;
		this.category = category;
		this.name = name;
		this.heightZ = heightZ;
		this.levelOverlap = levelOverlap;
		this.heightmapFilename = (heightmapFile.exists() ? heightmapFile.getPath() : null);
		this.texturemapFilename = (texturemapFile.exists() ? texturemapFile.getPath() : null);
	}

	@Override
	public String toString()
	{
		return name;
	}
}
