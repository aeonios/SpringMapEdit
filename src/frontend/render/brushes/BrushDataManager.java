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
 * BrushDataManager.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.LinkedHashMap;

import frontend.render.AppSettings;

import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 *
 */
public class BrushDataManager<BrushType extends BrushData>
{
	public static String brushPatternPath = "brushpatterns/";
	public static String brushTexturePath = "brushtextures/";
	
	private String dataDescription;
	private BrushDataFactory<BrushType> dataFactory;
	private BrushType[] brushData;
	private String brushDataPath;
	
	private int brushDataCount;
	private int lastScaledChild;
	
	private final int maxBrushes = 10240;
	private AppSettings as;

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public BrushDataManager(String brushDataPath, BrushDataFactory<BrushType> dataFactory, String dataDescription, AppSettings settings)
	{
		lastScaledChild = -1;
		this.as = settings;
		this.dataDescription = dataDescription;
		this.dataFactory = dataFactory;
		this.brushDataPath = brushDataPath;
		this.brushData = (BrushType[])(new BrushData[maxBrushes]);
		this.brushDataCount = 0;
		scanAvailableBrushData();
	}
	
	@SuppressWarnings("unchecked")
	public BrushDataManager(LinkedHashMap<File, Integer> files, BrushDataFactory<BrushType> dataFactory, String dataDescription, AppSettings settings)
	{
		lastScaledChild = -1;
		this.as = settings;
		this.dataDescription = dataDescription;
		this.dataFactory = dataFactory;
		this.brushDataPath = null;
		this.brushData = (BrushType[])(new BrushData[files.size()]);
		this.brushDataCount = 0;
		loadBrushDataFiles(files);
	}
	
	private void loadBrushDataFiles(LinkedHashMap<File, Integer> files)
	{
		Iterator<File> it = files.keySet().iterator();
		while (it.hasNext())
		{
			File currentFile = it.next();
			if (currentFile != null)
			{
				BrushType newData = dataFactory.create(currentFile, brushDataCount);
				files.put(currentFile, brushDataCount);
				brushData[brushDataCount] = newData;
				brushDataCount++;
				
				System.out.println("BrushManager added " + dataDescription + ": " + newData.toString());
			}
		}
	}
	
	private void scanAvailableBrushData()
	{
		//Initialize array
		for (int i = 0; i < maxBrushes; i++)
			brushData[i] = null;
		
		//Scan featurePath for image files
		File brushDir = new File(brushDataPath);
		if (!brushDir.exists())
			brushDir.mkdirs();
		File[] files = brushDir.listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
					return (name.endsWith(".bmp") || name.endsWith(".tga") || name.endsWith(".png") || name.endsWith(".jpg"));
			}
		});
		int fileCount = files.length;
			
		LinkedHashMap<File, Integer> fileList = new LinkedHashMap<File, Integer>();
		for (int i = 0; i < fileCount; i++)
			fileList.put(files[i], -1);
		loadBrushDataFiles(fileList);
	}
	
	public void addBrushData(File file)
	{
		BrushType newData = dataFactory.create(file, brushDataCount);
		brushData[brushDataCount] = newData;
		brushDataCount++;
		
		System.out.println("BrushManager added " + dataDescription + ": " + newData.toString());
	}
	
	public boolean isBrushDataLoaded(int brushDataID)
	{
		if ((brushDataID < brushDataCount) && (brushDataID >= 0))
			return brushData[brushDataID].isLoaded();
		else
			return false;
	}
	
	public boolean loadBrushData(int brushDataID)
	{
		if ((brushDataID < brushDataCount) && (brushDataID >= 0))
			return brushData[brushDataID].loadFromFile();
		return false;
	}
	
	public boolean unloadBrushData(int brushDataID)
	{
		if ((brushDataID < brushDataCount) && (brushDataID >= 0))
		{
			if (!as.keepBruhesInRam)
				brushData[brushDataID].unload();
			return true;
		}
		return false;
	}
	
	public String getBrushDataName(int brushDataID)
	{
		if ((brushDataID < brushDataCount) && (brushDataID >= 0))
			return brushData[brushDataID].toString();
		return "";
	}
	
	public int getBrushDataCount()
	{
		return brushDataCount;
	}
	
	public Vector2Int getBrushDataDimension(int brushDataID)
	{
		if ((brushDataID < brushDataCount) && (brushDataID >= 0))
			return brushData[brushDataID].getDimension();
		return null;
	}
	
	public BrushType getScaledBrushData(int brushDataID, int width, int height, boolean keepAspect)
	{
		if ((brushDataID < brushDataCount) && (brushDataID >= 0))
		{
			if (brushDataID != lastScaledChild)
			{
				//Free Last one's data:
				unloadBrushData(lastScaledChild);
				lastScaledChild = brushDataID;
			}
			return (BrushType)brushData[brushDataID].getScaledChild(width, height, keepAspect);
		}
		return null;
	}
}
