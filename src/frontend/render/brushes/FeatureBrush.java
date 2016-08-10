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
 * FeatureBrush.java 
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
public class FeatureBrush extends Brush
{
	//Current mode
	public FeatureMode mode;
	
	//Brush settings
	public int featureID;
	public float rotateStrength;
	
	//Brush in repeat mode, or stamp mode?
	boolean[] moduloMode;
	
	public enum FeatureMode
	{
		Add,
		Rotate,
		RotateSameRandom,
	}
	
	public FeatureBrush(BrushDataManager<BrushPattern> brushPatternManager, int patternID, int width, int height, SpringMapEdit sme)
	{
		mode = FeatureMode.Add;
		featureID = 0;
		rotateStrength = 0.5f;
		
		pattern[0] = brushPatternManager.getScaledBrushData(patternID, width, height, true);
		
		moduloMode = new boolean[FeatureMode.values().length];
		moduloMode[FeatureMode.Add.ordinal()] = false;
		moduloMode[FeatureMode.Rotate.ordinal()] = false;
		moduloMode[FeatureMode.RotateSameRandom.ordinal()] = false;

		this.width = width;
		this.height = height;
		this.sme = sme;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getStrengthInt()
	{
		int result = 0;
		switch (mode)
		{
			case Add:
				result = featureID;
				break;
			case Rotate:
				result = (int)(rotateStrength * 10);
				break;
			case RotateSameRandom:
				result = (int)(rotateStrength * 10);
				break;
		}
		return result;
	}
	
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}
	
	public void setStrengthInt(int strength)
	{
		switch (mode)
		{
			case Add: 
				featureID = strength;
				break;
			case Rotate:
			case RotateSameRandom:
				rotateStrength = strength / 10f;
		}
	}
	
	public float getStrength()
	{
		float result = 0;
		switch (mode)
		{
			case 
				Add: result = featureID;
				break;
			case Rotate:
				result = rotateStrength;
				break;
			case RotateSameRandom:
				result = rotateStrength;
				break;
		}
		return result;
	}
	
	public BrushPattern getPattern()
	{
		return this.pattern[0];
	}
	
	public void setPattern(int patternID)
	{
		/*pattern[0] = brushPatternManager.getScaledBrushData(patternID, width, height, true);
		width = pattern[0].width;
		height = pattern[0].height;*/
	}
	
	public void mirror(boolean horizontal)
	{
	}
	
	public void rotate(boolean counterClockWise)
	{
	}
	
	public void applyBrush(Vector2Int position, FeatureBrush brush, boolean invert)
	{
		switch (mode)
		{
		case Add:
			if (invert)
				sme.map.featuremap.removeFromFeaturemap(position.x(), position.y(), this);
			else
				sme.map.featuremap.addToFeaturemap(position.x(), position.y(), this);
			break;
		case Rotate:
			sme.map.featuremap.rotateFeaturemap(position.x(), position.y(), this, invert);
			break;
		case RotateSameRandom:
			sme.map.featuremap.rotateSameRandom(position.x(), position.y(), this);
			break;
		}
	}
}
