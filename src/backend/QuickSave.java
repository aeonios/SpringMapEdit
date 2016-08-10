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
 * QuickSave.java 
 * Created on 03.03.2009
 * by Heiko Schmitt
 */
package backend;

import java.util.ArrayList;

import backend.io.ByteOutputStream;
import frontend.render.features.FeatureMapContainer;

/**
 * @author Heiko Schmitt
 *
 */
public class QuickSave
{
	public static final int BACKUP_BLOCK_SIZE = 1024 * 1024 * 10;
	
	//General
	public boolean backupStored;
	public ByteOutputStream backupStorage;
	public ArrayList<FeatureMapContainer>[] featureListBackup;
	
	//Quicksave properties
	public boolean isCompressed;
	public boolean hasHeightmap;
	public boolean hasTexturemap;
	public boolean hasMetalmap;
	public boolean hasTypemap;
	public boolean hasVegetationmap;
	public boolean hasFeaturemap;
	
	public QuickSave()
	{
		backupStored = false;
	}

	public void free()
	{
		backupStored = false;
		backupStorage = null;
	}

	public void allocate()
	{
		backupStored = false;
		backupStorage = new ByteOutputStream(BACKUP_BLOCK_SIZE);
	}

	public void prepareSave()
	{
		//Allocate if necessary
		if (backupStorage == null) allocate();
		
		//Reset
		backupStorage.reset();
		
		//Initialize values
		isCompressed = false;
		hasHeightmap = false;
		hasTexturemap = false;
		hasMetalmap = false;
		hasTypemap = false;
		hasVegetationmap = false;
		hasFeaturemap = false;
	}

}
