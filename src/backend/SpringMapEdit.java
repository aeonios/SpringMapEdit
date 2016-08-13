/*
 * SpringMapEdit -- A 3D map editor for the Spring engine
 *
 * Copyright (C) 2008-2009  Heiko Schmitt <heikos23@web.de>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 * SpringMapEdit.java 
 * Created on 03.07.2008
 * by Heiko Schmitt
 */
package backend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import frontend.render.AppSettings;
import frontend.render.MapRenderer;
import frontend.render.brushes.HeightBrush;
import frontend.render.brushes.PrefabBrush;
import frontend.render.brushes.TextureBrush;
import frontend.render.brushes.TypeBrush;
import frontend.render.brushes.VegetationBrush;
import frontend.render.features.FeatureManager;
import frontend.render.features.FeatureMapContainer;

import backend.FileHandler.FileFormat;
import backend.image.Bitmap;
import backend.io.ByteInputStream;
import backend.io.DataInputStream;
import backend.io.DataOutputStream;
import backend.map.Map;
import backend.sm2.SM2File;
import backend.sm3.SM3Layer;

/**
 * @author Heiko Schmitt
 *
 */
public class SpringMapEdit
{
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual heightmap-size. (do not forget to add 1 pixel to heightmap)
	 */
	public static final int springMapsizeHeightmapFactor = 64;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual texture-size.
	 */
	public static final int springMapsizeTexturemapFactor = springMapsizeHeightmapFactor * 8;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual metalmap-size.
	 */
	public static final int springMapsizeMetalmapFactor = springMapsizeHeightmapFactor / 2;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual typemap-size.
	 */
	public static final int springMapsizeTypemapFactor = springMapsizeHeightmapFactor / 2;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual vegetationmap-size.
	 */
	public static final int springMapsizeVegetationmapFactor = springMapsizeHeightmapFactor / 4;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual slopemap-size.
	 */
	public static final int springMapsizeSlopemapFactor = springMapsizeHeightmapFactor / 2;
	
	/**
	 * This is the factor we need to multiply the mapsize with,<BR>
	 * to get the actual texture-size.
	 */
	public static final int heightmapSizeTextureFactor = springMapsizeTexturemapFactor / springMapsizeHeightmapFactor;
	
	/**
	 * This is the factor we need to divide the mapsize by,<BR>
	 * to get the actual metalmap-size.
	 */
	public static final int heightmapSizeMetalmapDivisor = springMapsizeHeightmapFactor / springMapsizeMetalmapFactor;
	
	/**
	 * This is the factor we need to divide the mapsize by,<BR>
	 * to get the actual typemap-size.
	 */
	public static final int heightmapSizeTypemapDivisor = springMapsizeHeightmapFactor / springMapsizeTypemapFactor;
	
	/**
	 * This is the factor we need to divide the mapsize by,<BR>
	 * to get the actual typemap-size.
	 */
	public static final int heightmapSizeVegetationmapDivisor = springMapsizeHeightmapFactor / springMapsizeVegetationmapFactor;
	
	/**
	 * This is the virtual (openGL) size of one Tile.<BR>
	 * This is needed for feature Placement.
	 */
	public static int tileSize = 2;
	
	/**
	 * Width and Height in spring units
	 */
	public int width, height;
	public float diag;
	
	public Map map; 
	
	/**
	 * NOTE: This array is indexed [y][x] for faster transfer to VRAM
	 * SM2 only
	 */

	public SM3Layer[] sm3Layers;

	public FeatureManager featureManager;
	
	public byte[][] typeMapColorTable;
	
	public int[] slopes;
	public byte[][] slopeMapColorTable;
	
	//Editing stuff
	public MapEditSettings mes;
	public AppSettings as;

	public String CurrentMap = "";
	
	//Quicksave/Quickload stuff
	private QuickSave quicksave;
		
	/**
	 * Create a new map.<BR>
	 * Sizes are in Spring map units!
	 */
	public SpringMapEdit(int width, int height, MapEditSettings mes, AppSettings as)
	{
		this.as = as;
		SpringMapEdit.tileSize = as.quadSize;
		
		if (mes == null) 
			this.mes = new MapEditSettings(as, this);
		else
			this.mes = mes;
		init();
		newMap(height, width, 0, false);
		map.maxHeight = tileSize * 80;
	}
	
	/**
	 * 
	 */
	public SpringMapEdit(File heightmapFile, MapEditSettings mes)
	{
		if (mes == null) 
			this.mes = new MapEditSettings(as, this);
		else
			this.mes = mes;
		init();
		map.heightmap.loadDataIntoHeightmap(heightmapFile);
	}
		
	private void init()
	{
		quicksave = new QuickSave();
		
		//Slope Colors
		int slopeCount = 4;
		slopes = new int[slopeCount];
		slopeMapColorTable = new byte[slopeCount][3];
		slopeMapColorTable[0][0] = (byte) 127;
		slopeMapColorTable[0][1] = (byte) 127;
		slopeMapColorTable[0][2] = (byte) 127;
		slopes[0] = (int)FastMath.round(0.08 * 255);
		
		slopeMapColorTable[1][0] = (byte) 127;
		slopeMapColorTable[1][1] = (byte) 255;
		slopeMapColorTable[1][2] = (byte) 127;
		slopes[1] = (int)FastMath.round(0.22 * 255);
		
		slopeMapColorTable[2][0] = (byte) 127;
		slopeMapColorTable[2][1] = (byte) 127;
		slopeMapColorTable[2][2] = (byte) 255;
		slopes[2] = (int)FastMath.round(0.56 * 255);
		
		slopeMapColorTable[3][0] = (byte) 255;
		slopeMapColorTable[3][1] = (byte) 127;
		slopeMapColorTable[3][2] = (byte) 127;
		slopes[3] = (int)255;
		
		//Type Colors
		int colors = 256;
		typeMapColorTable = new byte[colors][3];
		for (int i = 0; i < colors; i++)
		{
			typeMapColorTable[i][0] = (byte) i;
			typeMapColorTable[i][1] = (byte) 0;
			typeMapColorTable[i][2] = (byte) 0;
		}
		//Some distinguishable colors for first types
		typeMapColorTable[1][0] = (byte) 255;
		typeMapColorTable[1][1] = (byte) 255;
		typeMapColorTable[1][2] = (byte) 0;
		
		typeMapColorTable[2][0] = (byte) 0;
		typeMapColorTable[2][1] = (byte) 255;
		typeMapColorTable[2][2] = (byte) 0;
		
		typeMapColorTable[3][0] = (byte) 0;
		typeMapColorTable[3][1] = (byte) 255;
		typeMapColorTable[3][2] = (byte) 255;
		
		typeMapColorTable[4][0] = (byte) 0;
		typeMapColorTable[4][1] = (byte) 0;
		typeMapColorTable[4][2] = (byte) 255;
		
		typeMapColorTable[5][0] = (byte) 255;
		typeMapColorTable[5][1] = (byte) 0;
		typeMapColorTable[5][2] = (byte) 255;
		
		typeMapColorTable[6][0] = (byte) 255;
		typeMapColorTable[6][1] = (byte) 158;
		typeMapColorTable[6][2] = (byte) 0;
		
		typeMapColorTable[7][0] = (byte) 158;
		typeMapColorTable[7][1] = (byte) 255;
		typeMapColorTable[7][2] = (byte) 0;
		
		typeMapColorTable[8][0] = (byte) 0;
		typeMapColorTable[8][1] = (byte) 255;
		typeMapColorTable[8][2] = (byte) 158;
		
		typeMapColorTable[9][0] = (byte) 158;
		typeMapColorTable[9][1] = (byte) 0;
		typeMapColorTable[9][2] = (byte) 255;
		
		featureManager = new FeatureManager();
	}

	public void saveSlopeMap(File slopemapFile)
	{
		new Bitmap(FileFormat.Bitmap8Bit).saveDataFromSlopemap(slopemapFile, map.slopeMap, map.slopeMapWidth, map.slopeMapHeight);
	}
	
	public void saveSM2Map(File filename, MapRenderer renderer)
	{
		SM2File sm2fh = new SM2File(this, renderer);
		if (!as.minimapFilename.equals(""))
			sm2fh.setExternalMinimap(as.minimapFilename);
		if (!as.texturemapFilename.equals(""))
			sm2fh.setExternalTexturemap(as.texturemapFilename);
		sm2fh.save(filename, new File(FileHandler.removeExtension(filename).getAbsolutePath() + ".smt"));
	}
	
	public void saveAllMaps(File filename, MapRenderer renderer)
	{
		String name = filename.getName();
		File dir = filename.getParentFile();
		
		map.saveAllMaps(filename);//, MapRenderer renderer)
		map.featuremap.saveFeatureMap(new File(dir, name + "_Feature"), renderer.getFeatureManager());
	}
	
	public void loadSM2Map(File filename, MapRenderer renderer)
	{		
		new SM2File(this, renderer).load(filename, new File(FileHandler.removeExtension(filename) + ".smt"));
	}
	
	public void loadSM2Map(File smfFilename, File smtFilename, MapRenderer renderer)
	{		
		new SM2File(this, renderer).load(smfFilename, smtFilename);
	}
	
	public void loadAllMaps(File filename, MapRenderer renderer)
	{		
		String name = filename.getName();
		File dir = filename.getParentFile();
		
		//remove extension
		if (name.endsWith("_Height.png"))
			name = name.substring(0, name.length() - 11);
		if (name.endsWith("_Metal.bmp"))
			name = name.substring(0, name.length() - 10);
		if (name.endsWith("_Feature.fmf"))
			name = name.substring(0, name.length() - 12);
		if (name.endsWith("_Type.bmp"))
			name = name.substring(0, name.length() - 9);
		if (name.endsWith("_Vegetation.bmp"))
			name = name.substring(0, name.length() - 15);
		if (name.endsWith("_Texture.bmp"))
			name = name.substring(0, name.length() - 12);
		
		map.loadAllMaps(filename, renderer);
		map.featuremap.loadDataIntoFeaturemap(new File(dir, name + "_Feature.fmf"), renderer.getFeatureManager());
	}
	
	private void resetMap()
	{
		width = 0;
		height = 0;
		map = null;
		
		quicksave.free();
		
		//Free Resources
		System.gc();
		System.gc();
	}
	
	public void newMap(int newWidth, int newHeight, float initialHeight, boolean random)
	{
		try
		{
			resetMap();
			width = newWidth;
			height = newHeight;
			diag = (float) Math.sqrt((width * width * 128 * 128) + (height * height * 128 * 128));
			map = new Map(height, width);
			if (random)
				map.randomizeMap(mes.getTerraGenSetup());
			
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	
	public void genColorsByHeight(int px, int py, TextureBrush brush)
	{
		map.genColorsByHeight(px, py, brush, mes.getTextureGeneratorSetup());
	}
	
	public void erodeMapWet(int px, int py, HeightBrush brush)
	{
		map.heightmap.erodeMapWet(px, py, brush.getWidth(), brush.getHeight(), mes.getErosionSetup());
	}

	public void erodeMapDryWet(int px, int py, int width, int height, boolean hydroErosion)
	{
		map.heightmap.erodeMapDryWet(px, py, width, height, hydroErosion, mes.getErosionSetup());
	}
	
	public void setPrefab(int px, int py, PrefabBrush brush)
	{
		if (brush.heightmap != null)
			map.heightmap.setPrefabHeightMap(px, py, brush, mes.brushHeightAlign, map.maxHeight);
		if (brush.texturemap != null)
			map.textureMap.setPrefabTextureMap(px, py, brush);
	}
	
	public void addPrefab(int px, int py, PrefabBrush brush, boolean invert)
	{
		if (brush.heightmap != null)
			map.heightmap.addPrefabHeightMap(px, py, brush, invert);
		if (brush.texturemap != null)
			map.textureMap.addPrefabTextureMap(px, py, brush);
	}
	
	@SuppressWarnings("unchecked")
	public void backupToMemory()
	{
		try
		{
			long start = System.nanoTime();
			
			//Init storage
			quicksave.prepareSave();
			
			//Compression
			OutputStream compressionStream = null;
			if (as.quicksave_compress)
			{
				quicksave.isCompressed = true;
				compressionStream = new BufferedOutputStream(new DeflaterOutputStream(quicksave.backupStorage, new Deflater(Deflater.BEST_SPEED, true), QuickSave.BACKUP_BLOCK_SIZE), QuickSave.BACKUP_BLOCK_SIZE);
			}
			else
				compressionStream = new DataOutputStream(quicksave.backupStorage);
				
			DataOutputStream dataStream = new DataOutputStream(compressionStream);
			
			if (as.quicksave_heightmap)
			{
				for (int y = 0; y < map.heightmap.getHeightmapLength(); y++)
					dataStream.write(map.heightmap.getHeightMap()[y], 0, map.heightmap.getHeightmapWidth());
				quicksave.hasHeightmap = true;
			}
			if (as.quicksave_texturemap)
			{
				for (int y = 0; y < map.textureMap.getLength(); y++)
					dataStream.write(map.textureMap.textureMap[y], 0, map.textureMap.getWidth() * 3);
				quicksave.hasTexturemap = true;
			}
			if (as.quicksave_metalmap)
			{
				for (int y = 0; y < map.metalmap.getMapLength(); y++)
					dataStream.write(map.metalmap.getMap()[y], 0, map.metalmap.getMapWidth());
				quicksave.hasMetalmap = true;
			}
			if (as.quicksave_typemap)
			{
				for (int y = 0; y < map.typemap.getMapLength(); y++)
					dataStream.write(map.typemap.getMap()[y], 0, map.typemap.getMapWidth());
				quicksave.hasTypemap = true;
			}
			if (as.quicksave_vegetationmap)
			{
				for (int x = 0; x < map.vegetationMapWidth; x++)
					dataStream.write(map.vegetationMap[x], 0, map.vegetationMapHeight);
				quicksave.hasVegetationmap = true;
			}
			dataStream.flush();
			compressionStream.close();
			
			if (as.quicksave_featuremap)
			{
				if (quicksave.featureListBackup != null)
					for (int i = 0; i < map.featuremap.featureBlockCount; i++) quicksave.featureListBackup[i] = null;
				quicksave.featureListBackup = (ArrayList<FeatureMapContainer>[])new ArrayList[map.featuremap.featureBlockCount];
				for (int i = 0; i < map.featuremap.featureBlockCount; i++)
					quicksave.featureListBackup[i] = new ArrayList<FeatureMapContainer>();
				for (int i = 0; i < map.featuremap.featureBlockCount; i++)
				{
					Iterator<FeatureMapContainer> it = map.featuremap.featureList[i].iterator();
					while (it.hasNext())
						try {
							quicksave.featureListBackup[i].add((FeatureMapContainer)it.next().clone());
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
				}
				quicksave.hasFeaturemap = true;
			}
			quicksave.backupStored = true;
			dataStream.close();
			System.out.println("Done Quicksaving. ( " + ((System.nanoTime() - start) / 1000000) + " ms ): " + "Backup Size: " + (quicksave.backupStorage.size() / 1024 / 1024) + " MB used");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean[] restoreFromMemory()
	{
		boolean[] result = new boolean[3];
		result[0] = false;
		result[1] = false;
		result[2] = false;
		
		if (quicksave.backupStored)
		{
			try
			{
				long start = System.nanoTime();
				
				ByteInputStream backupInput = quicksave.backupStorage.getByteInputStream();
				InputStream deCompressionStream = null;
				if (quicksave.isCompressed)
					deCompressionStream = new BufferedInputStream(new InflaterInputStream(backupInput, new Inflater(true),
							QuickSave.BACKUP_BLOCK_SIZE), QuickSave.BACKUP_BLOCK_SIZE);
				else
					deCompressionStream = new DataInputStream(backupInput);
				
				DataInputStream dataStream = new DataInputStream(deCompressionStream);
				
				if (quicksave.hasHeightmap)
				{
					float[][] tempMap = new float[map.heightmap.getHeightmapLength()][map.heightmap.getHeightmapWidth()];
					for (int y = 0; y < map.heightmap.getHeightmapLength(); y++)
						dataStream.read(tempMap[y], 0, map.heightmap.getHeightmapWidth());
					map.heightmap.setHeightMap(tempMap);
					result[0] = true;
				}
				if (quicksave.hasTexturemap)
				{
					for (int y = 0; y < map.textureMap.getLength(); y++)
						dataStream.read(map.textureMap.textureMap[y], 0, map.textureMap.getWidth() * 3);
					result[1] = true;
				}
				if (quicksave.hasMetalmap)
				{
					for (int y = 0; y < map.metalmap.getMapLength(); y++)
						dataStream.read(map.metalmap.getMap()[y], 0, map.metalmap.getMapWidth());
					result[1] = true;
				}
				if (quicksave.hasTypemap)
				{
					for (int y = 0; y < map.typemap.getMapLength(); y++)
						dataStream.read(map.typemap.getMap()[y], 0, map.typemap.getMapWidth());
					result[1] = true;
				}
				if (quicksave.hasVegetationmap)
				{
					for (int x = 0; x < map.vegetationMapWidth; x++)
						dataStream.read(map.vegetationMap[x], 0, map.vegetationMapHeight);
					result[1] = true;
				}
				if (quicksave.hasFeaturemap)
				{
					/*for (int i = 0; i < map.featuremap.featureBlockCount; i++)
						map.featuremap.featureList[i].clear();
					for (int i = 0; i < map.featuremap.featureBlockCount; i++)
					{
						Iterator<FeatureMapContainer> it = quicksave.featureListBackup[i].iterator();
						while (it.hasNext())
							map.featuremap.featureList[i].add((FeatureMapContainer)it.next().clone());
					}*/
					result[2] = true;
				}
				System.out.println("Done Quickloading. ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
				dataStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return result;
	}
}
