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
 * FeatureManager.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package frontend.render.features;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.media.opengl.GL;

import backend.tdf.TDFDocument;
import backend.tdf.TDFSection;

/**
 * @author Heiko Schmitt
 *
 */
public class FeatureManager
{
	private Feature[] features;
	private HashMap<String, Integer> featureStringIDMap;
	
	private String objectPath = "objects3d/";
	private String texturePath = "unittextures/";
	private String featurePath = "features/";
	
	private FeatureLoader featureLoader;
	private int featureCount;
	
	private final int maxFeatures = 2048;
	
	/**
	 * 
	 */
	public FeatureManager()
	{
		features = new Feature[maxFeatures];
		featureCount = 0;
		featureLoader = new FeatureLoader();
		featureStringIDMap = new LinkedHashMap<String, Integer>();
		
		scanAvailableFeatures();
	}
		
	private void scanAvailableFeatures()
	{
		for (int i = 0; i < maxFeatures; i++)
			features[i] = null;
		
		//Scan featurePath for .tdf
		File featureDir = new File(featurePath);

		if (!featureDir.exists())
			featureDir.mkdirs();
		//If it exists now, scan it
		if (featureDir.exists())
		{
			File[] files = featureDir.listFiles(new FilenameFilter() 
			{
				public boolean accept(File dir, String name)
				{
					return name.endsWith(".tdf");
				}
			});
			int fileCount = files.length;
			for (int i = 0; i < fileCount; i++)
				addFeaturesFromTDF(files[i]);
		}
	}
	
	private void addFeaturesFromTDF(File tdfFile)
	{
		TDFDocument doc = new TDFDocument(tdfFile);
		Iterator<TDFSection> it = doc.iteratorSection();
		while (it.hasNext())
		{
			addFeature(it.next());
			//if (!loadFeature(gl, features[features.length - 1]))
				;//new DialogBox (null, "Feature error", e.getMessage());
		}
	}
	
	private void addFeature(TDFSection featureTDF)
	{
		Feature newFeature = new Feature(featureTDF, featureCount);
		features[newFeature.featureID] = newFeature;
		featureStringIDMap.put(newFeature.stringID.toLowerCase(), featureCount);
		featureCount++;
		System.out.println("FeatureManager: added Feature: " + newFeature.stringID);
	}
	
	public void removeFeature(int featureID)
	{
		features[featureID] = null;
	}

	public boolean isFeatureLoaded(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].isLoaded;
		return false;
	}
	
	public int getDisplayListID(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].displayListID;
		return -1;
	}
	
	public int getBaseTextureID(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].textureID;
		return -1;
	}
	
	public int getAlphaTextureID(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].alphaTextureID;
		return -1;
	}
	
	public boolean loadFeature(GL gl, int featureID)
	{
		if (featureID < featureCount)
			return featureLoader.loadFeature(gl, objectPath, texturePath, features[featureID]);
		return false;
	}
	
	public int getTriangleCount(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].triangleCount;
		return 0;
	}
	
	public String getFeatureName(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].stringID;
		return "";
	}
	
	public float getMaxHeight(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].maxHeight;
		return 0;
	}
	
	public float getMaxWidth(int featureID)
	{
		if (featureID < featureCount)
			return features[featureID].maxWidth;
		return 0;
	}
	
	public int getFeatureCount()
	{
		return featureCount;
	}
	
	public int getFeatureID(String featureName)
	{
		Integer id = featureStringIDMap.get(featureName.toLowerCase());
		if (id == null)
			return -1;
		else
			return id;
	}
}
