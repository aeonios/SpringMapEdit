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
 * MetalBrush.java 
 * Created on 10.10.2008
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import backend.SpringMapEdit;
import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 *
 */
public class MetalBrush extends Brush
{
	//Brush Manager
	private BrushDataManager<BrushPattern> brushPatternManager;
	
	//Current Brush mode
	public MetalMode mode;
	
	//Brush in repeat mode, or stamp mode?
	boolean[] moduloMode;
	
	public float[] strength;
	
	//Modes
	public enum MetalMode
	{
		Add,
		Set
	}
	
	public MetalBrush(BrushDataManager<BrushPattern> brushPatternManager, int patternID, int width, int height, SpringMapEdit smeX)
	{
		this.brushPatternManager = brushPatternManager;
		
		mode = MetalMode.Add;
		int count = MetalMode.values().length;
		
		this.sme = smeX;
		
		moduloMode = new boolean[count];
		moduloMode[MetalMode.Add.ordinal()] = false;
		moduloMode[MetalMode.Set.ordinal()] = false;
		                         
		strength = new float[count];
		strength[MetalMode.Add.ordinal()] = 10f;
		strength[MetalMode.Set.ordinal()] = 0.5f;
		
		//Set up pattern and texture
		this.pattern[mode.ordinal()] = brushPatternManager.getScaledBrushData(patternID, width, height, true);
		
		//Set size
		if (pattern != null) {
			this.width = pattern[mode.ordinal()].width;
			this.height = pattern[mode.ordinal()].height;
		}
		else
		{
			
		}
	}

	@Override
	public int getMaxStrengthInt()
	{
		return 255;
	}

	@Override
	public float getStrength()
	{
		return strength[mode.ordinal()];
	}
	
	public int getStrengthInt()
	{
		int result = 0;
		switch (mode)
		{
			case Add: result = (int)strength[mode.ordinal()]; break;
			case Set: result = (int)(strength[mode.ordinal()] * 255); break;
		}
		return result;
	}
	
	public void setStrengthInt(int strength)
	{
		switch (mode)
		{
			case Add: this.strength[mode.ordinal()] = strength; break;
			case Set: this.strength[mode.ordinal()] = strength / 255f; break;
		}
	}
	
	public boolean getModuloMode()
	{
		return moduloMode[mode.ordinal()];
	}
	
	public void setPattern(int patternID)
	{
		this.pattern[0] = brushPatternManager.getScaledBrushData(patternID, width, height, true);
		this.width = pattern[0].width;
		this.height = pattern[0].height;
	}
	
	public void applyBrush(Vector2Int position, boolean invert)
	{
		switch (mode)
		{
		case Set:
			sme.map.metalmap.setToMetalmap(position.x(), position.y(), this);
			break;
		case Add:
			sme.map.metalmap.addToMetalmap(position.x(), position.y(), this, invert);
			break;
		}											
	}
}
