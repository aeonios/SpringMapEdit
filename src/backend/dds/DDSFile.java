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
 * DDSFile.java 
 * Created on 02.03.2009
 * by Heiko Schmitt
 */
package backend.dds;

import java.io.File;
import java.io.IOException;

import backend.io.LERandomAccessFile;

/**
 * @author Heiko Schmitt
 *
 */
public class DDSFile
{
	private LERandomAccessFile inData;

	
	//file data
	private int width;
	private int height;
	private int widthInBlocks;
	private int heightInBlocks;
	private int mipmapCount;
	
	//File locations we need later...
	private long[] mipLevelOffsets;
	
	//Constants
	public static final String DDS_MAGIC = "DDS ";
	public static final String DXT1_MAGIC = "DXT1";
	public static final int SURFACEDESCRIPTION_SIZE = 124;
	public static final int PIXELFORMAT_SIZE = 32;
	private static final int DXT1_BlockSizeInByte = 8;
	private static final int DXT1_BlockSizeInPixel = 4;
	
	/**
	 * 
	 */
	public DDSFile()
	{
		
	}

	public void open(File fname)
	{
		try
		{
			inData = new LERandomAccessFile(fname, "r");
			
			loadDDSHeader();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			inData.close();
			inData = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * DWORD               dwMagic;
	 * DDSURFACEDESC2      header;
	 */
	private void loadDDSHeader()
	{
		try
		{
			//Load Magic
			String magic = inData.readString(4); 
			if (!magic.equals(DDS_MAGIC)) throw new IOException("DDS Header: Expected " + DDS_MAGIC + ", found: " + magic);
			
			loadSurfaceDescription();
			
			//Store current position and calculate mipLevelOffsets
			mipLevelOffsets = new long[mipmapCount];
			mipLevelOffsets[0] = inData.getFilePointer();
			int w = widthInBlocks;
			int h = heightInBlocks;
			for (int i = 1; i < mipmapCount; i++)
			{
				mipLevelOffsets[i] = mipLevelOffsets[i - 1] + (w * h * DXT1_BlockSizeInByte);
				w = w / 2;
				h = h / 2;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	/*
	 * DWORD dwSize;
	 * DWORD dwFlags;
	 * DWORD dwHeight;
	 * DWORD dwWidth;
	 * DWORD dwPitchOrLinearSize;
	 * DWORD dwDepth;
	 * DWORD dwMipMapCount;
	 * DWORD dwReserved1[11];
	 * DDPIXELFORMAT ddpfPixelFormat;
	 * DDSCAPS2 ddsCaps;
	 * DWORD dwReserved2;
	 */
	private void loadSurfaceDescription()
	{
		try
		{
			//Size of SurfaceDesc
			int size = inData.readInt();
			if (size != SURFACEDESCRIPTION_SIZE) throw new IOException("SurfaceDescription: Expected size " + SURFACEDESCRIPTION_SIZE + ", found: " + size);
			
			//Valid flags
			/*int flags = */inData.readInt();
			
			//Height
			height = inData.readInt();
			heightInBlocks = height / DXT1_BlockSizeInPixel;
			
			//Width
			width = inData.readInt();
			widthInBlocks = width / DXT1_BlockSizeInPixel;
			
			//PitchOrLinearSize
			inData.skip(4);
			
			//Depth
			inData.skip(4);
			
			//MipMapCount
			mipmapCount = inData.readInt();
			
			//Skip Reserved[11]
			inData.skip(11 * 4);
			
			//PixelFormat
			loadPixelFormat();
			
			//Caps2
			loadCaps2();
			
			//Reserved2
			inData.skip(4);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * DWORD dwSize;
   	 * DWORD dwFlags;
     * DWORD dwFourCC;
     * DWORD dwRGBBitCount;
     * DWORD dwRBitMask, DWORD dwGBitMask, DWORD dwBBitMask;
     * DWORD dwRGBAlphaBitMask;
	 */
	private void loadPixelFormat()
	{
		try
		{
			//Size of PixelFormat
			int size = inData.readInt();
			if (size != PIXELFORMAT_SIZE) throw new IOException("PixelFormat: Expected size " + PIXELFORMAT_SIZE + ", found: " + size);
			
			//Valid Flags
			/*int flags = */inData.readInt();
			
			String fourCC = inData.readString(4);
			if (!fourCC.equals(DXT1_MAGIC)) throw new IOException("PixelFormat: Expected FourCC " + DXT1_MAGIC + ", found: " + fourCC);
			
			/*int bitCount = */inData.readInt();
			
			//Skip color masks
			inData.skip(4 * 4);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * DWORD dwCaps1;
	 * DWORD dwCaps2;
	 * DWORD Reserved[2];
	 */
	private void loadCaps2()
	{
		try
		{
			//not needed...skip entirely
			inData.skip(4 * 4);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void getCompressedTileData(byte[] data, int pixelX, int pixelY, int tileSizeInPixels) throws IOException
	{
		/*
		 * DXT1 stores a 4x4 Pixel block into 8 Byte
		 * 4x4 = 16 Pixels. 16 * 3 = 48
		 * 48 / 8 = 6 (==compression factor)
		 * 
		 * There won't be any padding I hope. (makes not too much sense for compressed 4x4 blocks anyway...)
		 * So we always MUST copy at minimum 8 Bytes. (a 4x4 Block)
		 * 
		 * MipLevels:
		 * 32x32
		 * 16x16
		 *  8x8
		 *  4x4
		 */
		int currentOffset = 0;
		long currentMipOffset;
		int xBlock;
		int yBlock;
		for (int mipLevel = 0; mipLevel < 4; mipLevel++)
		{
			currentMipOffset = mipLevelOffsets[mipLevel];
			yBlock = pixelY / DXT1_BlockSizeInPixel;
			for (int y = 0; y < (tileSizeInPixels / DXT1_BlockSizeInPixel); y++)
			{
				xBlock = pixelX / DXT1_BlockSizeInPixel;
				for (int x = 0; x < (tileSizeInPixels / DXT1_BlockSizeInPixel); x++)
				{
					inData.seek(currentMipOffset + ((xBlock * DXT1_BlockSizeInByte) + ((yBlock * DXT1_BlockSizeInByte) * widthInBlocks)));
					inData.read(data, currentOffset, DXT1_BlockSizeInByte);
					currentOffset += DXT1_BlockSizeInByte;
					
					xBlock++;
				}
				yBlock++;
			}
			tileSizeInPixels = tileSizeInPixels / 2;
		}
	}

	public void writeDataToStream(LERandomAccessFile outStream, int size)
	{
		try
		{
			byte[] buf = new byte[size];
			inData.read(buf, 0, size);
			outStream.write(buf, 0, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
}
