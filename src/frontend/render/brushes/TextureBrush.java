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
 * TextureBrush.java 
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
public class TextureBrush extends Brush
{
	public enum TextureMode
	{
		Blend,
		Add,
		Multiply,
		Stamp
	}
	//Brush Manager
	private BrushDataManager<BrushTexture> brushTextureManager;
	public BrushPattern pattern[];
	public BrushTexture texture;
	public BrushTexture secondaryTexture;
	
	//Current Mode
	public int brushMode;
	
	public float strength[];
	
	public boolean usePrimary = true;
	
	//Brush in repeat mode, or stamp mode?
	boolean[] moduloMode;
	
	public int width[];
	public int height[];
	
	public TextureBrush(BrushDataManager<BrushPattern> brushPatternManager, BrushDataManager<BrushTexture> brushTextureManager, int patternID, int textureID, int newWidth, int newHeight, int textureWidth, int textureHeight, SpringMapEdit smeX)
	{
		this.brushPatternManager = brushPatternManager;
		this.brushTextureManager = brushTextureManager;
		
		brushMode = TextureMode.Blend.ordinal();
		int count = TextureMode.values().length;
		
		width = new int[count];
		width[TextureMode.Blend.ordinal()] = newWidth;
		width[TextureMode.Add.ordinal()] = newWidth;
		width[TextureMode.Multiply.ordinal()] = newWidth;
		width[TextureMode.Stamp.ordinal()] = newWidth;
		
		height = new int[count];
		height[TextureMode.Blend.ordinal()] = newHeight;
		height[TextureMode.Add.ordinal()] = newHeight;
		height[TextureMode.Multiply.ordinal()] = newHeight;
		height[TextureMode.Stamp.ordinal()] = newHeight;
		
		moduloMode = new boolean[count];
		moduloMode[TextureMode.Blend.ordinal()] = false;
		moduloMode[TextureMode.Add.ordinal()] = false;
		moduloMode[TextureMode.Multiply.ordinal()] = false;
		moduloMode[TextureMode.Stamp.ordinal()] = false;
		
		strength = new float[count];
		strength[TextureMode.Blend.ordinal()] = 0.25f;
		strength[TextureMode.Add.ordinal()] = 0.01f;
		strength[TextureMode.Multiply.ordinal()] = 0.01f;
		strength[TextureMode.Stamp.ordinal()] = 1f;
		
		//Set up pattern and texture
		pattern = new BrushPattern[count];
		pattern[TextureMode.Blend.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);
		pattern[TextureMode.Add.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);
		pattern[TextureMode.Multiply.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);
		pattern[TextureMode.Stamp.ordinal()] = brushPatternManager.getScaledBrushData(patternID, newWidth, newHeight, true);

		texture = brushTextureManager.getScaledBrushData(textureID, textureWidth, textureHeight, true);
		secondaryTexture = brushTextureManager.getScaledBrushData(textureID, textureWidth, textureHeight, true);
		
		//Set size
		if (pattern != null) {
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
		return width[brushMode];
	}
	
	public int getHeight()
	{
		return height[brushMode];
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
		return 100;
	}

	public float getStrength()
	{
		return strength[brushMode];
	}
	
	public int getStrengthInt()
	{
		if (brushMode == 0 || brushMode == 3) {
			return (int) (strength[brushMode] * 100f);
		}else{
			return (int) (strength[brushMode] * 1000f);
		}
	}

	public void setStrengthInt(int strength)
	{
		if (brushMode == 0 || brushMode == 3) {
			this.strength[brushMode] = strength / 100f;
		}else{
			this.strength[brushMode] = strength / 1000f;
		}
	}
	
	public boolean getModuloMode()
	{
		return moduloMode[brushMode];
	}
	
	public BrushPattern getPattern()
	{
		return pattern[brushMode];
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
	public void swapPrimarySecondary()
	{
		usePrimary = !usePrimary;
	}
	
	public BrushTexture getTexture()
	{
		if (usePrimary)
			return texture;
		else
			return secondaryTexture;
	}
	
	public void setTexture(int textureID)
	{
		if (usePrimary)
			texture = brushTextureManager.getScaledBrushData(textureID, -1, -1, true);
		else
			secondaryTexture = brushTextureManager.getScaledBrushData(textureID, -1, -1, true);
	}
	
	public void setTextureToColor(int r, int g, int b)
	{
		if (usePrimary)
			texture = new BrushTexture(r, g, b);
		else
			secondaryTexture = new BrushTexture(r, g, b);
	}
	
	public void rotateTexture(boolean counterClockWise)
	{
		if (usePrimary)
			texture.rotate(counterClockWise);
		else
			secondaryTexture.rotate(counterClockWise);
	}
	
	public void mirrorTexture(boolean horizontal)
	{
		if (usePrimary)
			texture.mirror(horizontal);
		else
			secondaryTexture.mirror(horizontal);
	}
	
	public void applyBrush(Vector2Int position, Brush brush, boolean invert)
	{
		switch (sme.mes.getTextureBrush().brushMode)
		{
			case 0: sme.map.textureMap.blendColorToTexture(position.x(), position.y(), this); break;
			case 1: sme.map.textureMap.addColorToTexture(position.x(), position.y(), this); break;
			case 2: sme.map.textureMap.multiplyColorToTexture(position.x(), position.y(), this); break;
			case 3: sme.map.textureMap.stampColorToTexture(position.x(), position.y(), this); break;
		}
	}
}
