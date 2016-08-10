/**
 * PrefabManager.java 
 * Created on 29.09.2009
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import frontend.render.AppSettings;

import backend.math.Vector2Int;
import backend.tdf.TDFDocument;
import backend.tdf.TDFKeyValue;
import backend.tdf.TDFSection;


/**
 * @author Heiko Schmitt
 *
 */
public class PrefabManager
{
	public static String defaultPrefabPath = "prefabs/";
	
	public BrushDataManager<BrushPattern> brushHeightmapManager;
	public BrushDataManager<BrushTexture> brushTexturemapManager;
	private String prefabDataPath;
	
	private final int maxPrefabs = 10240;
	private PrefabData[] prefabData;
	private int prefabDataCount;
	private LinkedHashMap<String, List<Integer>> categoryMap;
	
	private AppSettings as;
	
	private enum PDSections
	{
		CATEGORY
	}
	private enum PDKeys
	{
		Height,
		LevelOverlap
	}
	
	/**
	 * 
	 */
	public PrefabManager(String prefabDataPath, AppSettings as)
	{
		this.as = as;
		this.prefabDataPath = prefabDataPath;
		this.prefabDataCount = 0;
		this.prefabData = new PrefabData[maxPrefabs];
		this.categoryMap = new LinkedHashMap<String, List<Integer>>();
		
		/*
		 * We pass the LinkedHashmaps down to the BrushDataManager.
		 * The BrushDataManager fills in the corresponding Identifiers,
		 * which we read back here.
		 */
		LinkedHashMap<File, Integer> heightmapPaths = new LinkedHashMap<File, Integer>();
		LinkedHashMap<File, Integer> texturemapPaths = new LinkedHashMap<File, Integer>();
		scanAvailablePrefabData(heightmapPaths, texturemapPaths);
		brushHeightmapManager = new BrushDataManager<BrushPattern>(heightmapPaths, new BrushPatternFactory(), "PrefabHeightmap", this.as);
		brushTexturemapManager = new BrushDataManager<BrushTexture>(texturemapPaths, new BrushTextureFactory(), "PrefabTexturemap", this.as);
		readBackIdentifiers(heightmapPaths, texturemapPaths);
	}

	private void readBackIdentifiers(LinkedHashMap<File, Integer> heightmapPaths, LinkedHashMap<File, Integer> texturemapPaths)
	{
		for (int i = 0; i < prefabDataCount; i++)
		{
			prefabData[i].heightmapID = -1;
			prefabData[i].texturemapID = -1;
			prefabData[i].featuremapID = -1;
			if (prefabData[i].heightmapFilename != null)
			{
				File fileName = new File(prefabData[i].heightmapFilename);
				if (fileName.exists())
					prefabData[i].heightmapID = heightmapPaths.get(fileName);
			}
			if (prefabData[i].texturemapFilename != null)
			{
				File fileName = new File(prefabData[i].texturemapFilename);
				if (fileName.exists())
					prefabData[i].texturemapID = texturemapPaths.get(fileName);
			}
		}
	}

	private void scanAvailablePrefabData(LinkedHashMap<File, Integer> heightmapPaths, LinkedHashMap<File, Integer> texturemapPaths)
	{
		//Initialize array
		for (int i = 0; i < maxPrefabs; i++)
		{
			prefabData[i] = null;
		}
		
		//Scan prefabDataPath for folders
		File prefabDir = new File(prefabDataPath);
		//Try to create dir
		if (!prefabDir.exists())
		{
			prefabDir.mkdirs();
		}
		if (prefabDir.exists())
		{
			File[] files = prefabDir.listFiles();
			int fileCount = files.length;
			
			for (int i = 0; i < fileCount; i++)
			{
				if (files[i].isDirectory())
					scanPrefabDirectory(files[i]);
			}
		}
		
		//Now prepare heightmap and texturemap Sets
		for (int i = 0; i < prefabDataCount; i++)
		{
			if (prefabData[i].heightmapFilename != null)
			{
				File fileName = new File(prefabData[i].heightmapFilename);
				if (fileName.exists())
					heightmapPaths.put(fileName, -1);
				else
					System.out.println("PrefabManager heightmap does not exist: " + fileName.getAbsolutePath());
			}
			if (prefabData[i].texturemapFilename != null)
			{
				File fileName = new File(prefabData[i].texturemapFilename);
				if (fileName.exists())
					texturemapPaths.put(fileName, -1);
				else
					System.out.println("PrefabManager texturemap does not exist: " + fileName.getAbsolutePath());
			}
		}
	}
	
	private void scanPrefabDirectory(File prefabDirectory)
	{
		if (prefabDirectory.exists())
		{
			//Check category.tdf
			File tdfFile = new File(prefabDirectory, "category.tdf");
			if (!tdfFile.exists())
			{
				System.out.println("No category.tdf found inside '" + prefabDirectory.getAbsolutePath() + "'. Ignoring Directory.");
				return;
			}
			
			try
			{
				TDFDocument doc = new TDFDocument(tdfFile);
				TDFSection main = doc.getSection(PDSections.CATEGORY.name());
				if (main == null) throw new IOException("No \"" + PDSections.CATEGORY.name() + "\" section found in " + tdfFile.getName());
				
				//Extract Height
				TDFKeyValue kv = main.getKeyValue(PDKeys.Height.name());
				if (kv == null) throw new IOException("No \"" + PDKeys.Height.name() + "\" key found in " + tdfFile.getName());
				float heightZ = Float.parseFloat(kv.getValue());
				
				//Extract LevelOverlap
				kv = main.getKeyValue(PDKeys.LevelOverlap.name());
				if (kv == null) throw new IOException("No \"" + PDKeys.LevelOverlap.name() + "\" key found in " + tdfFile.getName());
				float levelOverlap = Float.parseFloat(kv.getValue());
				
				File[] files = prefabDirectory.listFiles(new FilenameFilter() 
				{
					public boolean accept(File dir, String name)
					{
						return (name.endsWith("_hm.png"));
					}
				});
				
				List<Integer> prefabIdList = new ArrayList<Integer>();
				int fileCount = files.length;
				for (int i = 0; i < fileCount; i++)
				{
					String prefabName = files[i].getName();
					prefabName = prefabName.substring(0, prefabName.length() - 7);
					addPrefabDataFromName(prefabIdList, prefabDirectory, prefabName, heightZ, levelOverlap);
				}
				categoryMap.put(prefabDirectory.getName(), prefabIdList);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void addPrefabDataFromName(List<Integer> prefabIdList, File prefabDir, String prefabName, float heightZ, float levelOverlap)
	{
		PrefabData newData = new PrefabData(prefabDataCount, prefabDir.getName(), prefabName, heightZ, levelOverlap, new File(prefabDir, prefabName + "_hm.png"), new File(prefabDir, prefabName + "_tm.png"));
		prefabData[prefabDataCount] = newData;
		prefabIdList.add(prefabDataCount);
		prefabDataCount++;
		
		System.out.println("PrefabManager added Prefab: " + newData.toString());
	}
	
	public String getPrefabName(int prefabDataID)
	{
		if (prefabDataID < prefabDataCount)
		{
			return prefabData[prefabDataID].name;
		}
		return "";
	}
	
	public PrefabData getPrefabData(int prefabDataID)
	{
		if (prefabDataID < prefabDataCount)
		{
			return prefabData[prefabDataID];
		}
		return null;
	}
	
	public BrushPattern getScaledPrefabHeightmap(int prefabDataID, int width, int height, boolean keepAspect)
	{
		if (prefabDataID < prefabDataCount)
		{
			return brushHeightmapManager.getScaledBrushData(prefabData[prefabDataID].heightmapID, width, height, keepAspect);
		}
		return null;
	}
	
	public BrushTexture getScaledPrefabTexturemap(int prefabDataID, int width, int height, boolean keepAspect)
	{
		if (prefabDataID < prefabDataCount)
		{
			return brushTexturemapManager.getScaledBrushData(prefabData[prefabDataID].texturemapID, width, height, keepAspect);
		}
		return null;
	}
	
	public int getPrefabDataCount()
	{
		return prefabDataCount;
	}
	
	public List<Integer> getCategoryIDList(String categoryName)
	{
		return categoryMap.get(categoryName);
	}
	
	public Set<String> getCategoryNameSet()
	{
		return categoryMap.keySet();
	}
	
	public float getScaleFactorHeightmap(int prefabDataID, int width)
	{
		if (prefabDataID < prefabDataCount)
		{
			Vector2Int hmDim = brushHeightmapManager.getBrushDataDimension(prefabData[prefabDataID].heightmapID);
			if (hmDim != null)
			{
				return (float)width / (float)hmDim.x();
			}
		}
		return 1;
	}
	
	public Vector2Int getPrefabOriginalDimension(int prefabDataID)
	{
		if (prefabDataID < prefabDataCount)
		{
			Vector2Int hmDim = brushHeightmapManager.getBrushDataDimension(prefabData[prefabDataID].heightmapID);
			Vector2Int tmDim = brushTexturemapManager.getBrushDataDimension(prefabData[prefabDataID].texturemapID);
			if (hmDim != null)
			{
				return hmDim;
			}
			else if (tmDim != null)
			{
				return new Vector2Int((tmDim.vector[0] / 8) + 1, (tmDim.vector[1] / 8) + 1);
			}
		}
		return null;
	}
}
