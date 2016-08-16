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
 * VegetationBrush.java 
 * Created on 10.10.2008
 * by Heiko Schmitt
 */
package frontend.render.brushes;

/**
 * @author Heiko Schmitt
 *
 */
public class VegetationBrush extends Brush
{
	public int typeID;
	
	public VegetationBrush(BrushDataManager<BrushPattern> brushPatternManager, int typeID, int patternID, int width, int height)
	{
		this.typeID = typeID;
		
		this.brushPatternManager = brushPatternManager;
		
		//Set up pattern and texture
		this.pattern[0] = brushPatternManager.getScaledBrushData(patternID, width, height, true);
		
		//Set size
		if (pattern[0] != null) {
			this.width = pattern[0].width;
			this.height = pattern[0].height;
		}
	}

	@Override
	public int getMaxStrengthInt()
	{
		return 100;
	}

	@Override
	public float getStrength()
	{
		return typeID;
	}

	@Override
	public int getStrengthInt()
	{
		return typeID + 1;
	}

	@Override
	public void setStrengthInt(int strength)
	{
		typeID = strength - 1;
	}
}
