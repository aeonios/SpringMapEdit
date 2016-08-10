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
 * BrushPattern.java 
 * Created on 09.10.2008
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import java.io.File;

import backend.FastMath;
import backend.image.ImageGrayscaleFloat;
import backend.image.ImageLoader;
import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 *
 */
public class BrushPattern implements BrushData
{
	public BrushPattern parent;
	public int patternID;
	public String stringID;
	
	private float[][] pattern;
	public int width;
	public int height;
	
	public boolean isLoaded;
	public File imageFile;
	
	private BrushPattern() 
	{
	}
	
	public BrushPattern(File imageFile, int brushPatternID)
	{
		this.imageFile = imageFile;
		this.isLoaded = false;
		this.parent = null;
		this.patternID = brushPatternID;
		this.stringID = imageFile.getName().substring(0, imageFile.getName().length() - 4);
	}

	public boolean loadFromFile()
	{
		if (isLoaded) return true;
		if (!imageFile.exists()) return false;
		
		try
		{
			ImageGrayscaleFloat image = ImageLoader.loadImageGrayscaleFloat(imageFile, false);
			width = image.width;
			height = image.height;
			pattern = image.data;
			isLoaded = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return isLoaded;
	}
	
	public float[][] getPattern()
	{
		loadFromFile();
		return pattern;
	}
	
	public BrushPattern getScaledChild(int width, int height, boolean keepAspect)
	{
		final BrushPattern newBrushPattern = new BrushPattern();
		newBrushPattern.parent = this;
		loadFromFile();
		newBrushPattern.isLoaded = isLoaded;
		newBrushPattern.patternID = patternID;
		newBrushPattern.stringID = stringID;
		newBrushPattern.imageFile = imageFile;
		
		//Copy scaled
		if (width < 0)
		{
			newBrushPattern.width = this.width;
			newBrushPattern.height = this.height;
		}
		else
		{
			if (keepAspect)
			{
				newBrushPattern.width = width;
				newBrushPattern.height = FastMath.round((float)width / ((float)this.width / (float)this.height));
			}
			else
			{
				newBrushPattern.width = width;
				newBrushPattern.height = height;
			}
		}
		final int newWidth = newBrushPattern.width;
		final int newHeight = newBrushPattern.height;
		newBrushPattern.pattern = new float[newWidth][newHeight];
		//Localize
		final float[][] newP = newBrushPattern.pattern;
		
		if ((this.width == newWidth) && (this.height == newHeight))
		{
			for (int x = 0; x < newWidth; x++)
			{
				System.arraycopy(pattern[x], 0, newP[x], 0, newHeight);
			}
		}
		else
		{
			int x;
			int y = 0;
			final float xPixels = (float)(this.width - 1) / newWidth;
			final float yPixels = (float)(this.height - 1) / newHeight;
			float curX, curY;
			int cX, cY;
			float v1, v2;
			float dX, dY, leftVal, rightVal;
			
			//Transform scaled
			while (y < newBrushPattern.height)
			{
				x = 0;
				while (x < newBrushPattern.width)
				{
					curX = x * xPixels;
					curY = y * yPixels;
					
					//Left upper
					cX = Math.min(Math.max((int)curX, 0), this.width - 1);
					cY = Math.min(Math.max((int)curY, 0), this.height - 1);
					dX = curX - cX;
					dY = curY - cY;
					v1 = pattern[cY][cX];
					
					//Left Lower
					cX = Math.min(Math.max((int)curX, 0), this.width - 1);
					cY = Math.min(Math.max((int)(curY + 1), 0), this.height - 1);
					v2 = pattern[cY][cX];
					
					leftVal = (v1 * (1 - dY)) + (v2 * dY);
					
					//Right upper
					cX = Math.min(Math.max((int)(curX + 1), 0), this.width - 1);
					cY = Math.min(Math.max((int)curY, 0), this.height - 1);
					v1 = pattern[cY][cX];
					
					//Right Lower
					cX = Math.min(Math.max((int)(curX + 1), 0), this.width - 1);
					cY = Math.min(Math.max((int)(curY + 1), 0), this.height - 1);
					v2 = pattern[cY][cX];
					
					rightVal = (v1 * (1 - dY)) + (v2 * dY);
					
					//Set calculated value
					newBrushPattern.pattern[x][y] = (leftVal * (1 - dX)) + (rightVal * dX);
					
					x++;
				}
				y++;
			}
		}
		return newBrushPattern;
	}
	
	public void rotate(boolean counterClockWise)
	{
		int oldWidth = width;
		int oldHeight = height;
		width = oldHeight;
		height = oldWidth;
		float[][] newPattern = new float[width][height];
		
		if (counterClockWise)
		{
			//90 Degree CCW
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
					newPattern[x][y] = pattern[(oldWidth - 1) - y][x];
		}
		else
		{
			//90 Degree CW
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
					newPattern[x][y] = pattern[y][(oldHeight - 1) - x];
		}
		pattern = newPattern;
	}
	
	public void mirror(boolean horizontal)
	{
		float[][] newPattern = new float[width][height];
		if (horizontal)
		{
			//Along the horizontal axis. (The actual mirror is vertical ;))
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
					newPattern[x][y] = pattern[(width - 1) - x][y];
		}
		else
		{
			//Along the vertical axis. (The actual mirror is horizontal ;))
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
					newPattern[x][y] = pattern[x][(height - 1) - y];
		}
		pattern = newPattern;
	}
	
	@Override
	public String toString()
	{
		return stringID;
	}

	@Override
	public boolean isLoaded()
	{
		return isLoaded;
	}

	@Override
	public Vector2Int getDimension()
	{
		return new Vector2Int(width, height);
	}
	
	@Override
	public void unload()
	{
		this.isLoaded = false;
		this.pattern = null;
	}
}
