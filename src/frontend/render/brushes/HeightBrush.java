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
 * HeightBrush.java 
 * Created on 10.10.2008
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import backend.SpringMapEdit;
import backend.map.Heightmap;
import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 */
public class HeightBrush extends Brush
{
	public enum HeightMode
	{
		Raise,
		Stamp,
		Set,
		Smooth
	}
	
	public BrushPattern[] pattern = new BrushPattern[1];
	
	//Current Brush mode
	public int brushMode;
	
	//Brush settings
	public float[] strength;
	
	//Brush in repeat mode, or stamp mode?
	public boolean[] moduloMode;
	
	public int[] width;
	public int[] height;
	
	public HeightBrush(BrushDataManager<BrushPattern> brushPatternManager, int patternID, int newWidth, int newHeight, SpringMapEdit smeX)
	{
		this.brushPatternManager = brushPatternManager;
		
		brushMode = HeightMode.Raise.ordinal();
		int count = HeightMode.values().length;

		strength = new float[count];
		strength[HeightMode.Raise.ordinal()] = 0.05f;
		strength[HeightMode.Stamp.ordinal()] = 0.1f;
		strength[HeightMode.Set.ordinal()] = 0.5f;
		strength[HeightMode.Smooth.ordinal()] = 0.25f;

		moduloMode = new boolean[count];
		moduloMode[HeightMode.Raise.ordinal()] = false;
		moduloMode[HeightMode.Stamp.ordinal()] = false;
		moduloMode[HeightMode.Set.ordinal()] = false;
		moduloMode[HeightMode.Smooth.ordinal()] = false;
		
		//Set up pattern and texture
		pattern = new BrushPattern[count];
		pattern[HeightMode.Raise.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);
		pattern[HeightMode.Stamp.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);
		pattern[HeightMode.Set.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);
		pattern[HeightMode.Smooth.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);
		
		width = new int[count];
		width[HeightMode.Raise.ordinal()] = newWidth;
		width[HeightMode.Stamp.ordinal()] = newWidth;
		width[HeightMode.Set.ordinal()] = newWidth;
		width[HeightMode.Smooth.ordinal()] = newWidth;
		
		height = new int[count];
		height[HeightMode.Raise.ordinal()] = newHeight;
		height[HeightMode.Stamp.ordinal()] = newHeight;
		height[HeightMode.Set.ordinal()] = newHeight;
		height[HeightMode.Smooth.ordinal()] = newHeight;
		
		//Set size
		if (pattern[brushMode] != null)
		{
			width[brushMode] = pattern[brushMode].width;
			height[brushMode] = pattern[brushMode].height;
		}
		else
		{
			width[brushMode] = newWidth;
			height[brushMode] = newHeight;
		}
		sme = smeX;
	}
	
	public int getWidth()
	{
		return pattern[brushMode].width;
	}
	
	public int getHeight()
	{
		return pattern[brushMode].height;
	}

	public void setSize(int width, int height)
	{
		this.width[brushMode] = width;
		this.height[brushMode] = height;
		setPattern(pattern[brushMode].patternID);
	}

	@Override
	public int getMaxStrengthInt()
	{
		if (brushMode == 3){
			return 100;
		}else{
			return sme.map.maxHeight;
		}
	}
	
	public int getStrengthInt()
	{
		int result = 0;
		switch (brushMode)
		{
			case 0: result = (int)(strength[brushMode] * (float) sme.map.maxHeight); break;
			case 1: result = (int)(strength[brushMode] * (float) sme.map.maxHeight); break;
			case 2: result = (int)(strength[brushMode] * (float) sme.map.maxHeight); break;
			case 3: result = (int)(strength[brushMode] * 100f); break;
		}
		return result;
	}
	
	public void setStrengthInt(int newStrength)
	{
		switch (brushMode)
		{
		case 0:
			this.strength[brushMode] = Math.min(newStrength / (float) sme.map.maxHeight, 1f); break;
		case 1:
			this.strength[brushMode] = Math.min(newStrength / (float) sme.map.maxHeight, 1f); break;
		case 2:
			this.strength[brushMode] = Math.min(newStrength / (float) sme.map.maxHeight, 1f); break;
		case 3:
			this.strength[brushMode] = Math.min(newStrength / 100f, 1f); break;
		}
	}
	
	public float getStrength()
	{
		return strength[brushMode];
	}
	
	public boolean getModuloMode()
	{	
		return moduloMode[brushMode];
	}

	public boolean isVertexOriented()
	{
		return true;
	}
	
	public BrushPattern getPattern()
	{
		return this.pattern[brushMode];
	}
	
	public void setPattern(int patternID)
	{
		pattern[brushMode] = brushPatternManager.getScaledBrushData(patternID, width[brushMode], height[brushMode], true);
		width[brushMode] = pattern[brushMode].width;
		height[brushMode] = pattern[brushMode].height;
	}
	
	public void mirror(boolean horizontal)
	{
		pattern[brushMode].mirror(horizontal);
	}
	
	public void rotate(boolean counterClockWise)
	{
		pattern[brushMode].rotate(counterClockWise);
		width[brushMode] = pattern[brushMode].width;
		height[brushMode] = pattern[brushMode].height;
	}
	
	public void applyBrush(Vector2Int position, boolean invert)
	{
		Heightmap heightMap = sme.map.heightmap;
		switch (brushMode)
		{
			case 0: heightMap.modifyHeight(position.x(), position.y(), this, invert); break;
			case 1: heightMap.modifyHeight(position.x(), position.y(), this, invert); break;
			case 2: heightMap.setHeight(position.x(), position.y(), this); break;
			case 3: heightMap.smoothHeight(position.x(), position.y(), this, getStrength()); break;
		}
	}
}
