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
 * TextureGeneratorSetup.java 
 * Created on 21.01.2009
 * by Heiko Schmitt
 */
package backend;

import java.io.File;
import java.io.IOException;

import backend.tdf.TDFDocument;
import backend.tdf.TDFKeyValue;
import backend.tdf.TDFSection;

import frontend.render.brushes.BrushDataManager;
import frontend.render.brushes.BrushTexture;

/**
 * A container class for auto texturing settings
 * @author Heiko Schmitt
 */
public class TextureGeneratorSetup
{
	public int levels;
	public BrushTexture[] brushesFlat;
	public BrushTexture[] brushesSteep;
	public float[] heightTransitions;
	public float[] steepTransitions;
		
	public static final String textureScriptPath = "texturescripts/";
	
	private enum TGSSections
	{
		TEXTUREGENERATORSETUP,
		FLAT,
		STEEP,
		HEIGHTTRANSITIONS,
		STEEPTRANSITIONS
	}
	private enum TGSKeys
	{
		Levels,
		Texture,
		Start,
		End,
		Flat,
		Steep
	}
	
	/**
	 * Initializes arrays to required sizes for given level count
	 * @param levels
	 */
	public TextureGeneratorSetup(int levels)
	{
		setLevels(levels);
	}
	
	private void setLevels(int levels)
	{
		this.levels = levels;
		brushesFlat = new BrushTexture[levels];
		brushesSteep = new BrushTexture[levels];
		if (levels == 1)
		{
			heightTransitions = new float[1];
			heightTransitions[0] = 1.0f;
		}
		else
			heightTransitions = new float[(levels - 1) * 2];
		steepTransitions = new float[levels * 2];
	}

	public TextureGeneratorSetup(int levels, BrushTexture[] brushesFlat, BrushTexture[] brushesSteep, float[] heightTransitions, float[] steepTransitions)
	{
		this.levels = levels;
		this.brushesFlat = brushesFlat;
		this.brushesSteep = brushesSteep;
		this.heightTransitions = heightTransitions;
		this.steepTransitions = steepTransitions;
	}
	
	/**
	 * Load setup from given tdfFile
	 * @param tdfFile
	 */
	public TextureGeneratorSetup(File tdfFile) throws IOException
	{
		try
		{
			TDFDocument doc = new TDFDocument(tdfFile);
			TDFSection main = doc.getSection(TGSSections.TEXTUREGENERATORSETUP.name());
			if (main == null) throw new IOException("No \"" + TGSSections.TEXTUREGENERATORSETUP.name() + "\" section found in " + tdfFile.getName());
			
			//Extract level count
			TDFKeyValue kv = main.getKeyValue(TGSKeys.Levels.name());
			if (kv == null) throw new IOException("No \"" + TGSKeys.Levels.name() + "\" key found in " + tdfFile.getName());
			setLevels(Integer.parseInt(kv.getValue()));
			
			//flat and steep
			TDFSection flat = main.getSection(TGSSections.FLAT.name());
			if (flat == null) throw new IOException("No \"" + TGSSections.FLAT.name() + "\" section found in " + tdfFile.getName());
			TDFSection steep = main.getSection(TGSSections.STEEP.name());
			if (steep == null) throw new IOException("No \"" + TGSSections.STEEP.name() + "\" section found in " + tdfFile.getName());
			for (int i = 0; i < levels; i++)
			{
				kv = flat.getKeyValue(TGSKeys.Texture.name() + (i + 1));
				if (kv == null) throw new IOException("No \"" + TGSKeys.Texture.name() + (i + 1) + "\" key (flat) found in " + tdfFile.getName());
				brushesFlat[i] = new BrushTexture(new File(BrushDataManager.brushTexturePath + kv.getValue()), -1);
				
				kv = steep.getKeyValue(TGSKeys.Texture.name() + (i + 1));
				if (kv == null) throw new IOException("No \"" + TGSKeys.Texture.name() + (i + 1) + "\" key (steep) found in " + tdfFile.getName());
				brushesSteep[i] = new BrushTexture(new File(BrushDataManager.brushTexturePath + kv.getValue()), -1);
			}
			
			//heightTransitions
			if (levels > 1)
			{
				TDFSection heights = main.getSection(TGSSections.HEIGHTTRANSITIONS.name());
				if (heights == null) throw new IOException("No \"" + TGSSections.HEIGHTTRANSITIONS.name() + "\" section found in " + tdfFile.getName());
				for (int i = 0; i < ((levels - 1) * 2); i++)
				{
					String keyName = (((i % 2) == 0) ? TGSKeys.End.name() : TGSKeys.Start.name()) + (((i + 1) / 2) + 1);
					kv = heights.getKeyValue(keyName);
					if (kv == null) throw new IOException("No \"" + keyName + i + "\" key found in " + tdfFile.getName());
					heightTransitions[i] = Float.parseFloat(kv.getValue());
				}
			}
			
			//steepTransitions
			TDFSection steeps = main.getSection(TGSSections.STEEPTRANSITIONS.name());
			if (steeps == null) throw new IOException("No \"" + TGSSections.STEEPTRANSITIONS.name() + "\" section found in " + tdfFile.getName());
			for (int i = 0; i < (levels * 2); i++)
			{
				String keyName = (((i % 2) == 0) ? TGSKeys.Flat.name() : TGSKeys.Steep.name()) + ((i / 2) + 1);
				kv = steeps.getKeyValue(keyName);
				if (kv == null) throw new IOException("No \"" + keyName + i + "\" key found in " + tdfFile.getName());
				steepTransitions[i] = Float.parseFloat(kv.getValue());
			}
		}
		catch (NumberFormatException nfe)
		{
			throw new IOException(nfe);
		}
	}
	
	/**
	 * Loads all brushes textures (call this once after setting/modifying brushesFlat and brushesSteep)
	 */
	public void loadBrushTextures() throws IOException
	{
		for (int i = 0; i < brushesFlat.length; i++)
		{
			brushesFlat[i].loadFromFile();
			if (!brushesFlat[i].isLoaded) throw new IOException("Error when loading: " + brushesFlat[i].imageFile.getAbsolutePath());
		}
		for (int i = 0; i < brushesSteep.length; i++)
		{
			brushesSteep[i].loadFromFile();
			if (!brushesSteep[i].isLoaded) throw new IOException("Error when loading: " + brushesSteep[i].imageFile.getAbsolutePath());
		}
	}
}
