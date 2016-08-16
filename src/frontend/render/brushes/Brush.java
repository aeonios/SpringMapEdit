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
 * Brush.java 
 * Created on 05.07.2008
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import backend.SpringMapEdit;
import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 *
 */
public class Brush
{
	public BrushDataManager<BrushPattern> brushPatternManager;
	public BrushPattern[] pattern = new BrushPattern[1];
	
	//Current Brush mode
	public int brushMode = 0;
	
	//Brush settings
	public float strength;
	
	//Brush in repeat mode, or stamp mode?
	boolean moduloMode;
	
	public int width;
	public int height;

	public SpringMapEdit sme;
	
	public Brush()
	{
		this.width = -1;
		this.height = -1;
	}
	
	public Brush(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	public Brush(BrushDataManager<BrushPattern> brushPatternManager, int patternID, int newWidth, int height)
	{
		this.brushPatternManager = brushPatternManager;
		
		//Set up pattern and texture
		this.pattern[0] = brushPatternManager.getScaledBrushData(patternID, newWidth, height, true);
		
		//Set size
		if (pattern[0] != null) {
			width = pattern[0].width;
			this.height = pattern[0].height;
		}
		else
		{
			width = newWidth;
			this.height = height;
		}
	}
	
	public int getWidth()
	{
		return pattern[0].width;
	}
	
	public int getHeight()
	{
		return pattern[0].height;
	}
	
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
		setPattern(pattern[0].patternID);
	}
	
	public float getStrength()
	{
		return strength;
	}
	
	/**
	 * This method should return the Strength value scaled to the max range.
	 */
	public int getStrengthInt()
	{
		return (int)strength;
	}

	/**
	 * This method should return the maximum Strength value as an int (that the brush strength slider will scale to).
	 */
	public int getMaxStrengthInt()
	{
		return (int)strength;
	}
	
	/**
	 * This method expects the Strength value stretched to range 1-1000
	 * @param strength
	 */
	public void setStrengthInt(int strength)
	{
		this.strength = strength;
	}

				
	public boolean getModuloMode()
	{
		return moduloMode;
	}
	
	/**
	 * Whether this brush operates on vertices, or on tiles
	 * @return
	 */
	public boolean isVertexOriented()
	{
		return false;
	}
	
	public BrushPattern getPattern()
	{
		return pattern[0];
	}
	
	public void setPattern(int patternID)
	{
		pattern[0] = brushPatternManager.getScaledBrushData(patternID, width, height, true);
		width = pattern[0].width;
		height = pattern[0].height;
	}
	
	public void mirror(boolean horizontal)
	{
		pattern[0].mirror(horizontal);
	}
	
	public void rotate(boolean counterClockWise)
	{
		pattern[0].rotate(counterClockWise);
		width = pattern[0].width;
		height = pattern[0].height;
	}
	
	public void applyBrush(Vector2Int pos, boolean invert)
	{
	}
}
