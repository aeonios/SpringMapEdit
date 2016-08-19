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
 * PrefabBrush.java 
 * Created on 28.09.2009
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import frontend.render.brushes.HeightBrush.HeightMode;
import backend.FastMath;
import backend.SpringMapEdit;
import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 *
 */
public class PrefabBrush extends Brush
{
	//Manager for PrefabData
	private PrefabManager prefabManager;
	
	//Prefab Data
	public PrefabData prefab;
	public BrushPattern heightmap;
	public BrushTexture texturemap;
	
	Vector2Int origDimension;
	double ratio;
	double dWidth;
	
	//Current Mode
	public PrefabMode mode;
	
	//Brush settings
	public float[] strength;
	
	public enum PrefabMode
	{
		Set,
		Add
	}

	/**
	 * @param width
	 * @param height
	 */
	public PrefabBrush(PrefabManager prefabManager, BrushDataManager<BrushPattern> brushPatternManager, int prefabID, int width, int height, SpringMapEdit smeX)
	{
		this.prefabManager = prefabManager;
		this.brushPatternManager = brushPatternManager;
		
		mode = PrefabMode.Set;
		int count = PrefabMode.values().length;
		
		strength = new float[count];
		strength[PrefabMode.Set.ordinal()] = 1f;
		strength[PrefabMode.Add.ordinal()] = 1f;
		
		pattern = new BrushPattern[count];
		pattern[HeightMode.Raise.ordinal()] = brushPatternManager.getScaledBrushData(0, 0, 0, true);
		
		//Set up pattern and texture
		this.prefab = prefabManager.getPrefabData(prefabID);
		this.heightmap = prefabManager.getScaledPrefabHeightmap(prefabID, width, height, true);
		//Set size
		if (heightmap != null)
		{
			this.width = heightmap.width;
			this.height = heightmap.height;
			this.texturemap = prefabManager.getScaledPrefabTexturemap(prefabID, (heightmap.width - 1) * SpringMapEdit.heightmapSizeTextureFactor, (heightmap.height - 1) * SpringMapEdit.heightmapSizeTextureFactor, false);
		}
		else
		{
			this.texturemap = prefabManager.getScaledPrefabTexturemap(prefabID, (width - 1) * SpringMapEdit.heightmapSizeTextureFactor, (height - 1) * SpringMapEdit.heightmapSizeTextureFactor, true);
			if (texturemap != null)
			{
				this.width = (texturemap.width / 8) + 1;
				this.height = (texturemap.height / 8) + 1;
			}
			else
			{
				this.width = 1;
				this.height = 1;
			}
		}
		dWidth = this.width;
		updateRatio();
		sme = smeX;
	}
	
	private void updateRatio()
	{
		origDimension = prefabManager.getPrefabOriginalDimension(prefab.prefabID);
		if (origDimension != null)
		{
			ratio = dWidth / (double)origDimension.vector[0];
		}
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}

	@Override
	public int getMaxStrengthInt()
	{
		return 2;
	}
	
	public float getStrength()
	{
		return strength[mode.ordinal()];
	}
	
	public int getStrengthInt()
	{
		int result = 0;
		switch (mode)
		{
			case Set:    return (int)strength[mode.ordinal()];
			case Add:    return (int)strength[mode.ordinal()];
		}
		return result;
	}
	
	public void setStrengthInt(int strength)
	{
		this.strength[mode.ordinal()] = strength;
	}
	
	public void setSize(int width, int height)
	{	
		setPrefab(prefab.prefabID);
		this.width = width;
		this.height = height;
		updateRatio();
	}
	
	public void setPrefab(int prefabID)
	{
		if (width < 0)
		{
			Vector2Int newDimension = prefabManager.getPrefabOriginalDimension(prefabID);
			if (newDimension != null)
			{
				dWidth = newDimension.vector[0] * ratio;
				width = FastMath.round(dWidth);
			}
		}
		
		prefab = prefabManager.getPrefabData(prefabID);
		heightmap = prefabManager.getScaledPrefabHeightmap(prefabID, width, height, true);
		if (heightmap != null)
		{
			width = heightmap.width;
			height = heightmap.height;
			texturemap = prefabManager.getScaledPrefabTexturemap(prefabID, (heightmap.width - 1) * SpringMapEdit.heightmapSizeTextureFactor, (heightmap.height - 1) * SpringMapEdit.heightmapSizeTextureFactor, false);
		}
		else
		{
			this.texturemap = prefabManager.getScaledPrefabTexturemap(prefabID, (width - 1) * SpringMapEdit.heightmapSizeTextureFactor, (height - 1) * SpringMapEdit.heightmapSizeTextureFactor, true);
			if (texturemap != null)
			{
				width = (texturemap.width / 8) + 1;
				height = (texturemap.height / 8) + 1;
			}
			else
			{
				width = 1;
				height = 1;
			}
		}
		if (FastMath.round(dWidth) != width)
			dWidth = width;
	}
	
	public BrushPattern getPattern()
	{
		return pattern[0];
	}
	
	public void setPattern(int patternID)
	{
	}
	
	public int getPrefabID()
	{
		return prefab.prefabID;
	}

	public void rotate(boolean counterClockWise)
	{
		if (heightmap != null)
		{
			heightmap.rotate(!counterClockWise);
			width = heightmap.width;
			height = heightmap.height;
		}
		if (texturemap != null)
		{
			texturemap.rotate(counterClockWise);
			if (heightmap == null)
			{
				width = (texturemap.width / 8) + 1;
				height = (texturemap.height / 8) + 1;
			}
		}
	}
	
	public void mirror(boolean horizontal)
	{
		if (heightmap != null)
			heightmap.mirror(horizontal);
		if (texturemap != null)
			texturemap.mirror(horizontal);
	}
	
	public void applybrush (int x, int y, PrefabBrush brush, boolean inverted)
	{
		switch (mode)
		{
			case Set:
			{
				sme.setPrefab(x, y, brush);
				break;
			}
			case Add:
			{
				sme.addPrefab(x, y, brush, inverted);
				break;
			}
		}
	}
	
	public void copy (int px, int py, PrefabBrush brush)
	{
		if (true)
			sme.map.copy(px, py, this);
	}
	
	public void paste (int px, int py, PrefabBrush brush)
	{
		if (true)
			sme.map.paste(px, py, this);
	}
}
