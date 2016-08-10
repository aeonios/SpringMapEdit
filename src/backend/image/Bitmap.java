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
 * Bitmap.java 
 * Created on 11.07.2008
 * by Heiko Schmitt
 */
package backend.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import backend.FastMath;
import backend.FileHandler;
import backend.FileHandler.FileFormat;
import backend.io.LERandomAccessFile;

/**
 * @author Heiko Schmitt
 *
 */
public class Bitmap
{
	//File Reading
	public File file;
	private LERandomAccessFile inStream;
	private LERandomAccessFile outStream;
	private boolean requiresSizeInfo;
	private int rawFileSize;
	
	//File properties
	public int fileSize;
	public int dataOffset;
	
	public int dibHeaderSize;
	public int rawImageSize;
	
	//Image properties
	public FileFormat fileFormat;
	public int width;
	public int height;
	public int bitsPerPixel;
	public int compressionMethod;
	public int numberOfColorsInPalette;
	
	public byte[] colorTable;
	
	public Bitmap(File file)
	{
		this.requiresSizeInfo = false;
		this.file = file;
		try
		{
			this.rawFileSize = (int)file.length();
			
			boolean isRawFile = file.getName().endsWith(".raw");
			this.inStream = new LERandomAccessFile(file, "r");
			
			if (isRawFile) 
				loadRAWHeader();
			else
				loadBMPHeader();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public Bitmap(FileFormat fileFormat)
	{
		this.requiresSizeInfo = false;
		this.file = null;
		this.fileFormat = fileFormat;
	}
	
	public boolean requiresSizeInfo()
	{
		return requiresSizeInfo;
	}
	
	public boolean setSizeInfo(int width)
	{
		this.width = width;
		this.height = (rawFileSize / 2) / width;
		
		boolean fileSizeOk = (((rawFileSize / 2) % width) == 0);
		if (!fileSizeOk) System.out.println("Bitmap load: given filesize seems wrong. Width: " + width + " would give Height: " + height + " With Rest: " + ((rawFileSize / 2) % width));
		return fileSizeOk;
	}

	public void loadDataIntoBytemap(byte[][] map)
	{
		try
		{
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			
			if (bitsPerPixel == 8)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				int x, y;
				for (y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (x = 0; x < width; x++)
					{
						map[x][height - y - 1] = scanline[currentByte];
						currentByte++;
					}
					//Status output
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 24)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						map[x][height - y - 1] = scanline[currentByte + 1];
						currentByte += 3;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 32)
			{
				scanlineSize = width * 4;
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						map[x][height - y - 1] = scanline[currentByte + 1];
						currentByte += 4;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Bytemap load: Can only load 8bit, 24bit or 32bit images");
			
			System.out.println(" Done loading metalmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromByteMap(File outFile, byte[][] map, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap8Bit:
				{
					bitsPerPixel = 8;
					numberOfColorsInPalette = 256;
					compressionMethod = 0;
					
					//Determine scanlinesize in bytes
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
					
					//Prepare color palette
					colorTable = new byte[numberOfColorsInPalette * 3];
					for (int i = 0; i < numberOfColorsInPalette; i++)
					{
						// B - G - R - unused
						colorTable[0 + (i * 3)] = (byte)i;
						colorTable[1 + (i * 3)] = (byte)i;
						colorTable[2 + (i * 3)] = (byte)i;
					}
					
					saveBMPHeader();
					break;
				}
				case Bitmap24Bit:
				{
					bitsPerPixel = 24;
					numberOfColorsInPalette = 0;
					compressionMethod = 0;
					
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
										
					saveBMPHeader();
					break;
				}
				case Raw16Bit:
				case Raw16BitSM:
				case PNG16Bit:
				throw new IOException("Metalmap save: Can only save 8 or 24bit bitmap");
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap8Bit)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte] = map[x][height - y - 1];
						currentByte++;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (fileFormat == FileFormat.Bitmap24Bit)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				//Initialize Scanline buffer
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte + 0] = (byte)0;
						scanline[currentByte + 1] = map[x][height - y - 1];
						scanline[currentByte + 2] = (byte)0;
						currentByte += 3;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Metalmap save: Can only save 8 or 24bit bitmap");
			
			//Finished
			outStream.close();
			System.out.println(" Done saving metalmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadDataIntoFloatMap(float[][] map)
	{
		try
		{
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			
			if ((fileFormat == FileFormat.Raw16Bit) || (fileFormat == FileFormat.Raw16BitSM))
			{
				scanlineSize = width * 2;
				scanline = new byte[scanlineSize];
				int x, y;
				float value;
				for (y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (x = 0; x < width; x++)
					{
						value = ((float)(scanline[currentByte + 0] & 0xFF) + ((scanline[currentByte + 1] & 0xFF) << 8)) / 0xFFFF;
						map[y][x] = value;
						currentByte += 2;
					}
					//Status output
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
			{
				//BMP Format
				if (bitsPerPixel == 8)
				{
					scanlineSize = width;
					//Account for padding
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					scanline = new byte[scanlineSize];
					for (int y = 0; y < height; y++)
					{
						inStream.read(scanline, 0, scanlineSize);
						currentByte = 0;
						for (int x = 0; x < width; x++)
						{
							if (numberOfColorsInPalette > 0)
								map[height - y - 1][x] = Math.max(Math.max((colorTable[((scanline[currentByte] & 0xFF) * 3) + 0] & 0xFF) / 255f,
										(colorTable[((scanline[currentByte] & 0xFF) * 3) + 1] & 0xFF) / 255f),
										(colorTable[((scanline[currentByte] & 0xFF) * 3) + 2] & 0xFF) / 255f);
							else
								map[height - y - 1][x] = ((scanline[currentByte] & 0xFF) / 255f);
							currentByte++;
						}
						if ((y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
				}
				else if (bitsPerPixel == 24)
				{
					scanlineSize = width * 3;
					//Account for padding
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					scanline = new byte[scanlineSize];
					int x,y;
					for (y = 0; y < height; y++)
					{
						inStream.read(scanline, 0, scanlineSize);
						currentByte = 0;
						for (x = 0; x < width; x++)
						{
							map[height - y - 1][x] = Math.max(Math.max((scanline[currentByte + 0] & 0xFF) / 255f,
									(scanline[currentByte + 1] & 0xFF) / 255f), (scanline[currentByte + 2] & 0xFF) / 255f);
							currentByte += 3;
						}
						if ((y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
				}
				else if (bitsPerPixel == 32)
				{
					scanlineSize = width * 4;
					scanline = new byte[scanlineSize];
					for (int y = 0; y < height; y++)
					{
						inStream.read(scanline, 0, scanlineSize);
						currentByte = 0;
						for (int x = 0; x < width; x++)
						{
							map[height - y - 1][x] = Math.max(Math.max((scanline[currentByte + 0] & 0xFF) / 255f,
									(scanline[currentByte + 1] & 0xFF) / 255f), (scanline[currentByte + 2] & 0xFF) / 255f);
							currentByte += 4;
						}
						if ((y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
				}
				else
					throw new IOException("Bitmap load: Can only load 8bit, 24bit or 32bit images");
			}
			inStream.close();
			System.out.println(" Done loading heightmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromFloatMap(File outFile, float[][] heightmap, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ((fileFormat == FileFormat.Raw16Bit) || (fileFormat == FileFormat.Raw16BitSM)) ? ".raw" : ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap8Bit:
				{
					bitsPerPixel = 8;
					numberOfColorsInPalette = 256;
					compressionMethod = 0;
					
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
					
					//Prepare color palette
					colorTable = new byte[numberOfColorsInPalette * 3];
					for (int i = 0; i < numberOfColorsInPalette; i++)
					{
						// B - G - R - unused
						colorTable[0 + (i * 3)] = (byte)i;
						colorTable[1 + (i * 3)] = (byte)i;
						colorTable[2 + (i * 3)] = (byte)i;
					}
					
					saveBMPHeader();
					break;
				}
				case Raw16Bit:
				case Raw16BitSM:
				{
					bitsPerPixel = 16;
					saveRAWHeader();
					break;
				}
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap8Bit)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				int x,y;
				for (y = 0; y < height; y++)
				{
					currentByte = 0;
					for (x = 0; x < width; x++)
					{
						scanline[currentByte] = (byte)FastMath.round(255 * heightmap[height - y - 1][x]);
						currentByte++;
					}
					//Write scanline to file
					outStream.write(scanline, 0, scanlineSize);
					
					//Status output
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if ((fileFormat == FileFormat.Raw16Bit) || (fileFormat == FileFormat.Raw16BitSM))
			{
				scanlineSize = width * 2;
				scanline = new byte[scanlineSize];
				int x,y, value;
				for (y = 0; y < height; y++)
				{
					currentByte = 0;
					for (x = 0; x < width; x++)
					{
						value = FastMath.round(0xFFFF * heightmap[y][x]);
						scanline[currentByte] = (byte)(value & 0xFF);
						scanline[currentByte + 1] = (byte)((value >> 8) & 0xFF);
						
						currentByte += 2;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Heightmap save: Can only save 8bit bitmap or 16bit raw");
			
			outStream.close();
			System.out.println(" Done saving heightmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadDataIntoHeightmap(float[][] heightmap)
	{
		try
		{
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			
			if (fileFormat == FileFormat.Raw16Bit || fileFormat == FileFormat.Raw16BitSM)
			{
				scanlineSize = width * 2;
				scanline = new byte[scanlineSize];
				float value;
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						value = ((float)(scanline[currentByte + 0] & 0xFF) + ((scanline[currentByte + 1] & 0xFF) << 8)) / 0xFFFF;
						heightmap[y][x] = value;
						currentByte += 2;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else //BMP Format
			{
				if (bitsPerPixel == 8)
				{
					scanlineSize = width;
					//Account for padding
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					scanline = new byte[scanlineSize];
					for (int y = 0; y < height; y++)
					{
						inStream.read(scanline, 0, scanlineSize);
						currentByte = 0;
						for (int x = 0; x < width; x++)
						{
							if (numberOfColorsInPalette > 0)
								heightmap[height - y - 1][x] = Math.max(Math.max((colorTable[((scanline[currentByte] & 0xFF) * 3) + 0] & 0xFF) / 255f,
										(colorTable[((scanline[currentByte] & 0xFF) * 3) + 1] & 0xFF) / 255f),
										(colorTable[((scanline[currentByte] & 0xFF) * 3) + 2] & 0xFF) / 255f);
							else
								heightmap[height - y - 1][x] = ((scanline[currentByte] & 0xFF) / 255f);
							currentByte++;
						}
						if (y % oneTenthsOfHeight == 0)
							System.out.print("#");
					}
				}
				else if (bitsPerPixel == 24)
				{
					scanlineSize = width * 3;
					//Account for padding
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					scanline = new byte[scanlineSize];
					for (int y = 0; y < height; y++)
					{
						inStream.read(scanline, 0, scanlineSize);
						currentByte = 0;
						for (int x = 0; x < width; x++)
						{
							heightmap[height - y - 1][x] = Math.max(Math.max((scanline[currentByte + 0] & 0xFF) / 255f,
									(scanline[currentByte + 1] & 0xFF) / 255f), (scanline[currentByte + 2] & 0xFF) / 255f);
							currentByte += 3;
						}
						if ((y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
				}
				else if (bitsPerPixel == 32)
				{
					scanlineSize = width * 4;
					scanline = new byte[scanlineSize];
					for (int y = 0; y < height; y++)
					{
						inStream.read(scanline, 0, scanlineSize);
						currentByte = 0;
						for (int x = 0; x < width; x++)
						{
							heightmap[height - y - 1][x] = Math.max(Math.max((scanline[currentByte + 0] & 0xFF) / 255f,
									(scanline[currentByte + 1] & 0xFF) / 255f), (scanline[currentByte + 2] & 0xFF) / 255f);
							currentByte += 4;
						}
						if ((y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
				}
				else
					throw new IOException("Bitmap load: Can only load 8bit, 24bit or 32bit images");
			}
			inStream.close();
			System.out.println(" Done loading heightmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromHeightmap(File outFile, float[][] heightmap, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ((fileFormat == FileFormat.Raw16Bit) || (fileFormat == FileFormat.Raw16BitSM)) ? ".raw" : ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap8Bit:
				{
					bitsPerPixel = 8;
					numberOfColorsInPalette = 256;
					compressionMethod = 0;
					
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
					
					//Prepare color palette
					colorTable = new byte[numberOfColorsInPalette * 3];
					for (int i = 0; i < numberOfColorsInPalette; i++)
					{
						// B - G - R - unused
						colorTable[0 + (i * 3)] = (byte)i;
						colorTable[1 + (i * 3)] = (byte)i;
						colorTable[2 + (i * 3)] = (byte)i;
					}
					
					saveBMPHeader();
					break;
				}
				case Raw16Bit:
				case Raw16BitSM:
				{
					bitsPerPixel = 16;
					saveRAWHeader();
					break;
				}
				case PNG16Bit:
				case Bitmap24Bit:
					throw new IOException("Heightmap save: Can only save 8bit bitmap or 16bit raw");
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap8Bit)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte] = (byte)FastMath.round(255 * heightmap[height - y - 1][x]);
						currentByte++;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if ((fileFormat == FileFormat.Raw16Bit) || (fileFormat == FileFormat.Raw16BitSM))
			{
				scanlineSize = width * 2;
				//Initialize Scanline buffer
				scanline = new byte[scanlineSize];
				int value;
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						value = FastMath.round(0xFFFF * heightmap[y][x]);
						scanline[currentByte] = (byte)(value & 0xFF);
						scanline[currentByte + 1] = (byte)((value >> 8) & 0xFF);
						
						currentByte += 2;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Heightmap save: Can only save 8bit bitmap or 16bit raw");
			
			outStream.close();
			System.out.println(" Done saving heightmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromTexturemap(File outFile, byte[][] textureMap, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap24Bit:
				{
					bitsPerPixel = 24;
					numberOfColorsInPalette = 0;
					compressionMethod = 0;
					
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
										
					saveBMPHeader();
					break;
				}
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap24Bit)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte + 0] = textureMap[height - y - 1][(x * 3) + 2];
						scanline[currentByte + 1] = textureMap[height - y - 1][(x * 3) + 1];
						scanline[currentByte + 2] = textureMap[height - y - 1][(x * 3) + 0];
						currentByte += 3;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Texture save: Can only save 24bit bitmap");
			
			outStream.close();
			System.out.println(" Done saving texturemap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromMetalmap(File outFile, byte[][] metalMap, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap8Bit:
				{
					bitsPerPixel = 8;
					numberOfColorsInPalette = 256;
					compressionMethod = 0;
					
					//Determine scanlinesize in bytes
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
					
					//Prepare color palette
					colorTable = new byte[numberOfColorsInPalette * 3];
					for (int i = 0; i < numberOfColorsInPalette; i++)
					{
						// B - G - R - unused
						colorTable[0 + (i * 3)] = (byte)i;
						colorTable[1 + (i * 3)] = (byte)i;
						colorTable[2 + (i * 3)] = (byte)i;
					}
					
					saveBMPHeader();
					break;
				}
				case Bitmap24Bit:
				{
					bitsPerPixel = 24;
					numberOfColorsInPalette = 0;
					compressionMethod = 0;
					
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
										
					saveBMPHeader();
					break;
				}
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap8Bit)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte] = metalMap[height - y - 1][x];
						currentByte++;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (fileFormat == FileFormat.Bitmap24Bit)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte + 0] = (byte)0;
						scanline[currentByte + 1] = metalMap[height - y - 1][x];
						scanline[currentByte + 2] = (byte)0;
						currentByte += 3;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Metalmap save: Can only save 8 or 24bit bitmap");

			outStream.close();
			System.out.println(" Done saving metalmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromTypemap(File outFile, byte[][] typeMap, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap8Bit:
				{
					bitsPerPixel = 8;
					numberOfColorsInPalette = 256;
					compressionMethod = 0;
					
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
					
					//Prepare color palette
					colorTable = new byte[numberOfColorsInPalette * 3];
					for (int i = 0; i < numberOfColorsInPalette; i++)
					{
						// B - G - R - unused
						colorTable[0 + (i * 3)] = (byte)i;
						colorTable[1 + (i * 3)] = (byte)i;
						colorTable[2 + (i * 3)] = (byte)i;
					}
					
					saveBMPHeader();
					break;
				}
				case Bitmap24Bit:
				{
					bitsPerPixel = 24;
					numberOfColorsInPalette = 0;
					compressionMethod = 0;
					
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
										
					saveBMPHeader();
					break;
				}
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap8Bit)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte] = typeMap[height - y - 1][x];
						currentByte++;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (fileFormat == FileFormat.Bitmap24Bit)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte + 0] = (byte)0;
						scanline[currentByte + 1] = (byte)0;
						scanline[currentByte + 2] = typeMap[height - y - 1][x];
						currentByte += 3;
					}
					outStream.write(scanline, 0, scanlineSize);

					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Typemap save: Can only save 8 or 24bit bitmap");

			outStream.close();
			System.out.println(" Done saving typemap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromVegetationmap(File outFile, byte[][] vegetationMap, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap8Bit:
				{
					bitsPerPixel = 8;
					numberOfColorsInPalette = 256;
					compressionMethod = 0;
					
					//Determine scanlinesize in bytes
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
					
					//Prepare color palette
					colorTable = new byte[numberOfColorsInPalette * 3];
					for (int i = 0; i < numberOfColorsInPalette; i++)
					{
						// B - G - R - unused
						colorTable[0 + (i * 3)] = (byte)i;
						colorTable[1 + (i * 3)] = (byte)i;
						colorTable[2 + (i * 3)] = (byte)i;
					}
					
					saveBMPHeader();
					break;
				}
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap8Bit)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte] = ((vegetationMap[x][height - y - 1] & 0xFF) == 1 ? (byte) 255 : (byte) 0);
						currentByte++;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Vegetationmap save: Can only save 8bit bitmap");
			
			outStream.close();
			System.out.println(" Done saving vegetationmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveDataFromSlopemap(File outFile, byte[][] slopeMap, int width, int height)
	{
		try
		{
			outStream = new LERandomAccessFile(FileHandler.setFileExtension(outFile, ".bmp"), "rw");
			
			this.width = width;
			this.height = height;
			
			switch (fileFormat)
			{
				case Bitmap8Bit:
				{
					bitsPerPixel = 8;
					numberOfColorsInPalette = 256;
					compressionMethod = 0;
					
					//Determine scanlinesize in bytes
					int scanlineSize = width * (bitsPerPixel / 8);
					if ((scanlineSize % 4) > 0)
						scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
					rawImageSize = (scanlineSize * height);
					
					//Prepare color palette
					colorTable = new byte[numberOfColorsInPalette * 3];
					for (int i = 0; i < numberOfColorsInPalette; i++)
					{
						// B - G - R - unused
						colorTable[0 + (i * 3)] = (byte)i;
						colorTable[1 + (i * 3)] = (byte)i;
						colorTable[2 + (i * 3)] = (byte)i;
					}
					
					saveBMPHeader();
					break;
				}
			}
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			if (fileFormat == FileFormat.Bitmap8Bit)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						scanline[currentByte] = slopeMap[height - y - 1][x];
						currentByte++;
					}
					outStream.write(scanline, 0, scanlineSize);
					
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Slopemap save: Can only save 8bit bitmap");
			
			outStream.close();
			System.out.println(" Done saving slopemap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadDataIntoTexturemap(byte[][] textureMap)
	{
		try
		{
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			int multiplier;
			
			if (bitsPerPixel == 8)
			{
				multiplier = 1;
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) != 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				//Initialize Scanline buffer
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						textureMap[height - y - 1][(x * 3) + 0] = scanline[currentByte];
						textureMap[height - y - 1][(x * 3) + 1] = scanline[currentByte];
						textureMap[height - y - 1][(x * 3) + 2] = scanline[currentByte];
						currentByte += multiplier;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 24)
			{
				multiplier = 3;
				scanlineSize = width * multiplier;
				//Account for padding
				if ((scanlineSize % 4) != 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						textureMap[height - y - 1][(x * 3) + 0] = scanline[currentByte + 2];
						textureMap[height - y - 1][(x * 3) + 1] = scanline[currentByte + 1];
						textureMap[height - y - 1][(x * 3) + 2] = scanline[currentByte + 0];
						currentByte += multiplier;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 32)
			{
				multiplier = 4;
				scanlineSize = width * multiplier;
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						textureMap[height - y - 1][(x * 3) + 0] = scanline[currentByte + 2];
						textureMap[height - y - 1][(x * 3) + 1] = scanline[currentByte + 1];
						textureMap[height - y - 1][(x * 3) + 2] = scanline[currentByte + 0];
						currentByte += multiplier;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Texture load: Can only load 8bit, 24bit or 32bit images");
			System.out.println(" Done loading texturemap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadDataIntoMetalmap(byte[][] metalMap)
	{
		try
		{
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			
			if (bitsPerPixel == 8)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						metalMap[height - y - 1][x] = scanline[currentByte];
						currentByte++;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 24)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						metalMap[height - y - 1][x] = scanline[currentByte + 1];
						currentByte += 3;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 32)
			{
				scanlineSize = width * 4;
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						metalMap[height - y - 1][x] = scanline[currentByte + 1];
						currentByte += 4;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Metalmap load: Can only load 8bit, 24bit or 32bit images");
			
			System.out.println(" Done loading metalmap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadDataIntoTypemap(byte[][] typeMap)
	{
		try
		{
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			
			if (bitsPerPixel == 8)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						typeMap[height - y - 1][x] = scanline[currentByte];
						currentByte++;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 24)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						typeMap[height - y - 1][x] = scanline[currentByte + 2];
						currentByte += 3;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 32)
			{
				scanlineSize = width * 4;
				scanline = new byte[scanlineSize];
				for (int y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (int x = 0; x < width; x++)
					{
						typeMap[height - y - 1][x] = scanline[currentByte + 2];
						currentByte += 4;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Typemap load: Can only load 8bit, 24bit or 32bit images");
			
			System.out.println(" Done loading typemap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadDataIntoVegetationmap(byte[][] vegetationMap)
	{
		try
		{
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int currentByte;
			int scanlineSize;
			byte[] scanline;
			
			if (bitsPerPixel == 8)
			{
				scanlineSize = width;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				int x,y;
				for (y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (x = 0; x < width; x++)
					{
						vegetationMap[x][height - y - 1] = (((scanline[currentByte] & 0xFF) == 255) ? (byte) 1 : (byte) 0);
						currentByte++;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 24)
			{
				scanlineSize = width * 3;
				//Account for padding
				if ((scanlineSize % 4) > 0)
					scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
				scanline = new byte[scanlineSize];
				int x,y;
				for (y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (x = 0; x < width; x++)
					{
						vegetationMap[x][height - y - 1] = (((scanline[currentByte + 1] & 0xFF) == 255) ? (byte) 1 : (byte) 0);
						currentByte += 3;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else if (bitsPerPixel == 32)
			{
				scanlineSize = width * 4;
				scanline = new byte[scanlineSize];
				int x,y;
				for (y = 0; y < height; y++)
				{
					inStream.read(scanline, 0, scanlineSize);
					currentByte = 0;
					for (x = 0; x < width; x++)
					{
						vegetationMap[x][height - y - 1] = (((scanline[currentByte + 1] & 0xFF) == 255) ? (byte) 1 : (byte) 0);
						currentByte += 4;
					}
					if ((y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
			}
			else
				throw new IOException("Typemap load: Can only load 8bit, 24bit or 32bit images");
			
			//Finished
			System.out.println(" Done loading typemap");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadPalette()
	{
		try
		{
			byte[] arr4Byte = new byte[4];
			colorTable = new byte[numberOfColorsInPalette * 3];
			for (int i = 0; i < numberOfColorsInPalette; i++)
			{
				// B - G - R - unused
				inStream.read(arr4Byte, 0, 4);
				colorTable[0 + (i * 3)] = arr4Byte[2];
				colorTable[1 + (i * 3)] = arr4Byte[1];
				colorTable[2 + (i * 3)] = arr4Byte[0];
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void savePalette()
	{
		try
		{
			byte[] arr4Byte = new byte[4];
			for (int i = 0; i < numberOfColorsInPalette; i++)
			{
				// B - G - R - unused
				arr4Byte[0] = colorTable[2 + (i * 3)];
				arr4Byte[1] = colorTable[1 + (i * 3)];
				arr4Byte[2] = colorTable[0 + (i * 3)];
				arr4Byte[3] = 0;
				outStream.write(arr4Byte, 0, 4);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadWindowsV3Header()
	{
		try
		{
			width = inStream.readInt();
			
			height = inStream.readInt();
			
			//Color Planes (ignore)
			inStream.skip(2);
			
			bitsPerPixel = inStream.readUnsignedShort();
			
			compressionMethod = inStream.readInt();
			if (compressionMethod != 0)
				throw new IOException("Bitmap load: Can only load uncompressed Bitmaps");
			
			//Raw Image Size (ignored)
			inStream.skip(4);
			
			//Horizontal Resolution (ignored)
			inStream.skip(4);
			
			//Vertical Resolution (ignored)
			inStream.skip(4);
			
			//Number of colors in palette
			numberOfColorsInPalette = inStream.readInt();
			
			//Number of important colos (ignored)
			inStream.skip(4);
			
			loadPalette();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveWindowsV3Header()
	{
		try
		{
			outStream.writeInt(width);
			outStream.writeInt(height);
			
			//Color Planes (ignore)
			outStream.writeUnsignedShort(1);
			
			outStream.writeUnsignedShort(bitsPerPixel);
			
			outStream.writeInt(compressionMethod);
			
			outStream.writeInt(rawImageSize);
			
			//Horizontal Resolution (ignored)
			outStream.zeroFill(4);
			
			//Vertical Resolution (ignored)
			outStream.zeroFill(4);
			
			outStream.writeInt(numberOfColorsInPalette);
			
			//Number of important colors (ignored)
			outStream.zeroFill(4);
			
			savePalette();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadBMPHeader()
	{
		try
		{
			//Magic Bytes
			if (!inStream.readString(2).equals("BM"))
				throw new IOException("Bitmap load: Did not find magic BM marker");
			
			fileSize = inStream.readInt();
			
			//Discard next 4 Bytes
			inStream.skip(4);
			
			//Data offset
			dataOffset = inStream.readInt();
			
			//DIB HeaderSize
			dibHeaderSize = inStream.readInt();
			if (dibHeaderSize == 40)
				loadWindowsV3Header();
			else
				throw new IOException("Bitmap load: Only Bitmap V3 Header supported");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadRAWHeader()
	{
		try
		{
			//Magic Bytes (check for real RAW, or our modified)
			if (!inStream.readString(4).equals("SRAW"))
			{
				fileFormat = FileFormat.Raw16Bit;
				
				//Reset to beginning
				inStream.seek(0);
				
				//We need the user to enter the size
				requiresSizeInfo = true;
			}
			else
			{
				fileFormat = FileFormat.Raw16BitSM;
				this.width = inStream.readInt();
				this.height = inStream.readInt();
				requiresSizeInfo = false;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveRAWHeader()
	{
		try
		{
			if (fileFormat == FileFormat.Raw16BitSM)
			{
				//Magic Bytes
				outStream.writeString("SRAW", false);
				outStream.writeInt(width);
				outStream.writeInt(height);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveBMPHeader()
	{
		try
		{
			//Magic Bytes
			outStream.writeString("BM", false);
				
			//FileSize
			int scanlineSize = width * (bitsPerPixel / 8);
			if ((scanlineSize % 4) > 0)
				scanlineSize = scanlineSize + (4 - (scanlineSize % 4));
			fileSize = 54 + (scanlineSize * height) + (numberOfColorsInPalette * 4); //14 + 40 + width * height * (bpp / 8) + palette
			outStream.writeInt(fileSize);
			
			//Discard next 4 Bytes
			outStream.zeroFill(4);
			
			//Data offset
			dataOffset = 54 + (numberOfColorsInPalette * 4); //14 + 40 + palette
			outStream.writeInt(dataOffset);
			
			//DIB HeaderSize
			dibHeaderSize = 40;
			outStream.writeInt(dibHeaderSize);
			
			saveWindowsV3Header();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
