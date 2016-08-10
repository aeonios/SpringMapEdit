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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.io.File;

import backend.FastMath;
import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 *
 */
public class BrushTexture implements BrushData
{
	public BrushTexture parent;
	public int textureID;
	public String stringID;
	
	/**
	 * If you use those arrays directly, make sure to call loadFromFile at least once before!
	 */
	public byte[][] textureR;
	public byte[][] textureG;
	public byte[][] textureB;
	public byte[][] textureA;
	public int width;
	public int height;
	
	public boolean isLoaded;
	public File imageFile;
	
	private BrushTexture()
	{
	}
	
	public BrushTexture(int r, int g, int b)
	{
		this.imageFile = null;
		this.isLoaded = false;
		this.parent = null;
		this.textureID = -1;
		this.stringID = null;
		
		createFromColor(r, g, b);
	}
	
	public BrushTexture(File imageFile, int brushTextureID)
	{
		this.imageFile = imageFile;
		this.isLoaded = false;
		this.parent = null;
		this.textureID = brushTextureID;
		this.stringID = imageFile.getName().substring(0, imageFile.getName().length() - 4);
	}

	private void createFromColor(int r, int g, int b)
	{
		width = 8;
		height = 8;
		textureR = new byte[width][height];
		textureG = new byte[width][height];
		textureB = new byte[width][height];
		textureA = new byte[width][height];
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				textureA[x][y] = (byte)0xFF;
				textureR[x][y] = (byte)r;
				textureG[x][y] = (byte)g;
				textureB[x][y] = (byte)b;
			}
		}
		
		isLoaded = true;
	}
	
	public boolean loadFromFile()
	{
		if (isLoaded) return true;
		if (!imageFile.exists()) return false;
		
		try
		{
			Image image = Toolkit.getDefaultToolkit().getImage(imageFile.getAbsolutePath());
			while (image.getWidth(null) < 0)
			{
				Thread.sleep(1);
			}
			width = image.getWidth(null);
			height = image.getHeight(null);
			textureR = new byte[width][height];
			textureG = new byte[width][height];
			textureB = new byte[width][height];
			textureA = new byte[width][height];
			int[] pixels = new int[width * height];
			PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
			pg.grabPixels();
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					textureA[x][y] = (byte)((pixels[x + (y * width)] >> 24) & 0xFF);
					textureR[x][y] = (byte)((pixels[x + (y * width)] >> 16) & 0xFF);
					textureG[x][y] = (byte)((pixels[x + (y * width)] >>  8) & 0xFF);
					textureB[x][y] = (byte)((pixels[x + (y * width)]      ) & 0xFF);
				}
			}
			
			isLoaded = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return isLoaded;
	}
	
	public byte[][] getTextureR()
	{
		loadFromFile();
		return textureR;
	}
	
	public byte[][] getTextureG()
	{
		loadFromFile();
		return textureG;
	}
	
	public byte[][] getTextureB()
	{
		loadFromFile();
		return textureB;
	}
	
	public byte[][] getTextureA()
	{
		loadFromFile();
		return textureA;
	}
	
	public BrushTexture getScaledChild(int width, int height, boolean keepAspect)
	{
		long start = System.nanoTime();
		
		final BrushTexture newBrushTexture = new BrushTexture();
		newBrushTexture.parent = this;
		loadFromFile();
		newBrushTexture.isLoaded = isLoaded;
		newBrushTexture.textureID = textureID;
		newBrushTexture.stringID = stringID;
		newBrushTexture.imageFile = imageFile;
		
		//Copy scaled
		if (width < 0)
		{
			newBrushTexture.width = this.width;
			newBrushTexture.height = this.height;
		}
		else
		{
			if (keepAspect)
			{
				newBrushTexture.width = width;
				newBrushTexture.height = FastMath.round((float)width / ((float)this.width / (float)this.height));
			}
			else
			{
				newBrushTexture.width = width;
				newBrushTexture.height = height;
			}
		}
		final int newWidth = newBrushTexture.width;
		final int newHeight = newBrushTexture.height;
		newBrushTexture.textureR = new byte[newBrushTexture.width][newBrushTexture.height];
		newBrushTexture.textureG = new byte[newBrushTexture.width][newBrushTexture.height];
		newBrushTexture.textureB = new byte[newBrushTexture.width][newBrushTexture.height];
		newBrushTexture.textureA = new byte[newBrushTexture.width][newBrushTexture.height];
		//Localize:
		final byte[][] ntR = newBrushTexture.textureR;
		final byte[][] ntG = newBrushTexture.textureG;
		final byte[][] ntB = newBrushTexture.textureB;
		final byte[][] ntA = newBrushTexture.textureA;
		
		if ((this.width == newWidth) && (this.height == newHeight))
		{
			for (int x = 0; x < newWidth; x++)
			{
				System.arraycopy(textureR[x], 0, ntR[x], 0, newHeight);
				System.arraycopy(textureG[x], 0, ntG[x], 0, newHeight);
				System.arraycopy(textureB[x], 0, ntB[x], 0, newHeight);
				System.arraycopy(textureA[x], 0, ntA[x], 0, newHeight);
			}
		}
		else
		{
			final int oneTenthsOfHeight = Math.max(newHeight / 10, 1);
			
			int x;
			int y = 0;
			final float xPixels = (float)(this.width - 1) / newWidth;
			final float yPixels = (float)(this.height - 1) / newHeight;
			float curX, curY;
			float r1, g1, b1, a1, r2, g2, b2, a2;
			float leftR, leftG, leftB, leftA;
			float rightR, rightG, rightB, rightA;
			int cX, cY;
			float dX, dY;
			
			//Transform scaled
			while (y < newHeight)
			{
				x = 0;
				while (x < newWidth)
				{
					curX = x * xPixels;
					curY = y * yPixels;
					
					//Left upper
					cX = Math.min(Math.max((int)curX, 0), this.width - 1);
					cY = Math.min(Math.max((int)curY, 0), this.height - 1);
					dX = curX - cX;
					dY = curY - cY;
					r1 = (textureR[cX][cY] & 0xFF);
					g1 = (textureG[cX][cY] & 0xFF);
					b1 = (textureB[cX][cY] & 0xFF);
					a1 = (textureA[cX][cY] & 0xFF);
					
					//Left Lower
					cX = Math.min(Math.max((int)curX, 0), this.width - 1);
					cY = Math.min(Math.max((int)(curY + 1), 0), this.height - 1);
					r2 = (textureR[cX][cY] & 0xFF);
					g2 = (textureG[cX][cY] & 0xFF);
					b2 = (textureB[cX][cY] & 0xFF);
					a2 = (textureA[cX][cY] & 0xFF);
					
					leftR = (r1 * (1 - dY)) + (r2 * dY);
					leftG = (g1 * (1 - dY)) + (g2 * dY);
					leftB = (b1 * (1 - dY)) + (b2 * dY);
					leftA = (a1 * (1 - dY)) + (a2 * dY);
					
					//Right upper
					cX = Math.min(Math.max((int)(curX + 1), 0), this.width - 1);
					cY = Math.min(Math.max((int)curY, 0), this.height - 1);
					r1 = (textureR[cX][cY] & 0xFF);
					g1 = (textureG[cX][cY] & 0xFF);
					b1 = (textureB[cX][cY] & 0xFF);
					a1 = (textureA[cX][cY] & 0xFF);
					
					//Right Lower
					cX = Math.min(Math.max((int)(curX + 1), 0), this.width - 1);
					cY = Math.min(Math.max((int)(curY + 1), 0), this.height - 1);
					r2 = (textureR[cX][cY] & 0xFF);
					g2 = (textureG[cX][cY] & 0xFF);
					b2 = (textureB[cX][cY] & 0xFF);
					a2 = (textureA[cX][cY] & 0xFF);
					
					rightR = (r1 * (1 - dY)) + (r2 * dY);
					rightG = (g1 * (1 - dY)) + (g2 * dY);
					rightB = (b1 * (1 - dY)) + (b2 * dY);
					rightA = (a1 * (1 - dY)) + (a2 * dY);
					
					//Set calculated value
					ntR[x][y] = (byte)((leftR * (1 - dX)) + (rightR * dX));
					ntG[x][y] = (byte)((leftG * (1 - dX)) + (rightG * dX));
					ntB[x][y] = (byte)((leftB * (1 - dX)) + (rightB * dX));
					ntA[x][y] = (byte)((leftA * (1 - dX)) + (rightA * dX));
					
					x++;
				}
				y++;
				
				//Status output
				if ((y % oneTenthsOfHeight) == 0) System.out.print("#");
			}
		}
		
		System.out.println(" Done scaling Texture from "  + this.width + "x" + this.height + " to " + newWidth + "x" + newHeight + ": " + ((System.nanoTime() - start) / 1000000L) + " ms");
		
		return newBrushTexture;
	}
	
	public void rotate(boolean counterClockWise)
	{
		int oldWidth = width;
		int oldHeight = height;
		width = oldHeight;
		height = oldWidth;
		byte[][] newTextureR = new byte[width][height];
		byte[][] newTextureG = new byte[width][height];
		byte[][] newTextureB = new byte[width][height];
		byte[][] newTextureA = new byte[width][height];
		
		if (counterClockWise)
		{
			//90 Degree CCW
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
				{
					newTextureR[x][y] = textureR[(oldWidth - 1) - y][x];
					newTextureG[x][y] = textureG[(oldWidth - 1) - y][x];
					newTextureB[x][y] = textureB[(oldWidth - 1) - y][x];
					newTextureA[x][y] = textureA[(oldWidth - 1) - y][x];
				}
		}
		else
		{
			//90 Degree CW
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
				{
					newTextureR[x][y] = textureR[y][(oldHeight - 1) - x];
					newTextureG[x][y] = textureG[y][(oldHeight - 1) - x];
					newTextureB[x][y] = textureB[y][(oldHeight - 1) - x];
					newTextureA[x][y] = textureA[y][(oldHeight - 1) - x];
				}
		}
		textureR = newTextureR;
		textureG = newTextureG;
		textureB = newTextureB;
		textureA = newTextureA;
	}
	
	public void mirror(boolean horizontal)
	{
		byte[][] newTextureR = new byte[width][height];
		byte[][] newTextureG = new byte[width][height];
		byte[][] newTextureB = new byte[width][height];
		byte[][] newTextureA = new byte[width][height];
		if (horizontal)
		{
			//Along the horizontal axis. (The actual mirror is vertical ;))
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
				{
					newTextureR[x][y] = textureR[(width - 1) - x][y];
					newTextureG[x][y] = textureG[(width - 1) - x][y];
					newTextureB[x][y] = textureB[(width - 1) - x][y];
					newTextureA[x][y] = textureA[(width - 1) - x][y];
				}
		}
		else
		{
			//Along the vertical axis. (The actual mirror is horizontal ;))
			int x, y;
			for (y = 0; y < height; y++)
				for (x = 0; x < width; x++)
				{
					newTextureR[x][y] = textureR[x][(height - 1) - y];
					newTextureG[x][y] = textureG[x][(height - 1) - y];
					newTextureB[x][y] = textureB[x][(height - 1) - y];
					newTextureA[x][y] = textureA[x][(height - 1) - y];
				}
		}
		textureR = newTextureR;
		textureG = newTextureG;
		textureB = newTextureB;
		textureA = newTextureA;
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
		this.textureR = null;
		this.textureG = null;
		this.textureB = null;
		this.textureA = null;
	}
}
