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
 * ImageLoader.java 
 * Created on 09.10.2009
 * by Heiko Schmitt
 */
package backend.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * @author Heiko Schmitt
 *
 */
public class ImageLoader
{
	private ImageLoader() {}

	/*
	 * Reading a 2000x2000 RGB PNG image
	 * 1. image.getData -> raster.getSamples:           395 ms
	 * 2. image.getRaster -> raster.getSamples:         378 ms
	 * 3. image.getRaster -> raster.dataBuffer.getData: 395 ms
	 * A problem all 3 implementations have is:
	 * We need to touch every element for normalization. (/255 or /65535)
	 * Basically this is what MMX was designed for :/
	 * 
	 * Reading a 2000x2000 GRAY PNG image
	 * 2. image.getRaster -> raster.getSamples:         266 ms
	 * 3. image.getRaster -> raster.dataBuffer.getData: 174 ms
	 * Using real grayscale images is faster.
	 * 
	 * General problems:
	 * We read images in memory via BufferedImage.
	 * Then we copy it into our designated array.
	 * 
	 * It would be faster and save memory if we could directly read the imagedata
	 * into our target array. But this would require handling of each and every imagetype by ourself.
	 * (The same which we have done with Bitmaps and Raw...)
	 * 
	 * Optimizations left:
	 * Nothing here, since we need to move from Byte/Short/Int to Float and Normalize anyway.
	 * For RGBA images there might be an optimization. Look below
	 * 
	 * TODO: Check if float[] (instead float[][]) would be usable throughout the code.
	 * This would improve cache-hits for sequential access. (which is often the case)
	 */
	public static ImageGrayscaleFloat loadImageGrayscaleFloat(File filename, boolean showProgress)
	{
		long start = System.nanoTime();
		
		ImageGrayscaleFloat result = null;
		try
		{
			final BufferedImage image = ImageIO.read(filename);
			final Raster raster = image.getRaster();
			final int width = raster.getWidth();
			final int length = raster.getHeight();
			final int oneTenthsOfHeight = Math.max(length / 10, 1);
			result = new ImageGrayscaleFloat(width, length);
			final float[][] newData = result.data;
			
			//Method using dataBuffer.getData
			DataBuffer buffer = raster.getDataBuffer();
			int dataType = buffer.getDataType();
			switch (buffer.getDataType())
			{
				case DataBuffer.TYPE_BYTE:
				{
					final float factor = 255;
					final DataBufferByte b = (DataBufferByte)buffer;
					final int sampleSize = (b.getSize() / b.getNumBanks()) / (width * length);
					final byte[] data = b.getData();
					for (int y = 0; y < length; y++)
					{
						for (int x = 0; x < width; x++)
							newData[y][x] = (data[(x * sampleSize) + (y * width * sampleSize)] & 0xFF) / factor;
						if ((showProgress) && (y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
					break;
				}
				case DataBuffer.TYPE_USHORT:
				{
					final float factor = 65535;
					final DataBufferUShort b = (DataBufferUShort)buffer;
					final short[] data = b.getData();
					for (int y = 0; y < length; y++)
					{
						for (int x = 0; x < width; x++)
							newData[y][x] = (data[x + (y * width)] & 0xFFFF) / factor;
						if ((showProgress) && (y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
					break;
				}
				case DataBuffer.TYPE_INT:
				{
					final float factor = 255;
					final DataBufferInt b = (DataBufferInt)buffer;
					final int[] data = b.getData();
					for (int y = 0; y < length; y++)
					{
						for (int x = 0; x < width; x++)
							newData[y][x] = (data[x + (y * width)] & 0xFF) / factor;
						if ((showProgress) && (y % oneTenthsOfHeight) == 0)
							System.out.print("#");
					}
					break;
				}
				default:
					throw new Exception("ImageLoader: Unsupported Pattern Image Data Type: " + translateDataType(dataType));
			}
			if (showProgress)
				System.out.println(" loading " + filename.getName() + " (" + width + "x" + length + ") in " + ((System.nanoTime() - start) / 1000000L) + " ms");
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private static String translateDataType(int dataType)
	{
		switch (dataType)
		{
			case DataBuffer.TYPE_BYTE: return "BYTE";
			case DataBuffer.TYPE_DOUBLE: return "DOUBLE";
			case DataBuffer.TYPE_FLOAT: return "FLOAT";
			case DataBuffer.TYPE_INT: return "INT";
			case DataBuffer.TYPE_SHORT: return "SHORT";
			case DataBuffer.TYPE_UNDEFINED: return "UNDEFINED";
			case DataBuffer.TYPE_USHORT: return "USHORT";
			default: return "Unknown Data Type";
		}
	}
	
	private static String translateImageType(int imageType)
	{
		switch (imageType)
		{
			case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR";
			case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR";
			case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE";
			case BufferedImage.TYPE_BYTE_BINARY: return "BYTE_BINARY";
			case BufferedImage.TYPE_BYTE_GRAY: return "BYTE_GRAY";
			case BufferedImage.TYPE_BYTE_INDEXED: return "BYTE_INDEXED";
			case BufferedImage.TYPE_CUSTOM: return "CUSTOM";
			case BufferedImage.TYPE_INT_ARGB: return "INT_ARGB";
			case BufferedImage.TYPE_INT_ARGB_PRE: return "INT_ARGB_PRE";
			case BufferedImage.TYPE_INT_BGR: return "INT_BGR";
			case BufferedImage.TYPE_INT_RGB: return "INT_RGB";
			case BufferedImage.TYPE_USHORT_555_RGB: return "USHORT_555_RGB";
			case BufferedImage.TYPE_USHORT_565_RGB: return "USHORT_565_RGB";
			case BufferedImage.TYPE_USHORT_GRAY: return "USHORT_GRAY";
			default: return "Unknown Image Type";
		}
	}
	
	/*
	 * Optimizations to try:
	 * 1. For byte images, which correspond directly with our internal format,
	 * we could directly use the data.
	 * 
	 * 2. For images, which need byte reordering:
	 * Since the data is interleaved, we have no choice but touching each element.
	 * 
	 * Currently our pattern has each color component in its own array,
	 * which means byte ordering is irrelevant as we have to touch each sample anyway.
	 * BUT
	 * if 1. is given, we could use arraycopy here :)  
	 * 
	 * Since PNG ist quite standard, we just make ARGB (or whatever comes native from PNG)
	 * our default format.
	 * 
	 * WHY?
	 * -for patterns this introduced 16bit depth,
	 * but here we would only gain speed. eventually...
	 * -For more image type support:
	 * TextureIO fails for some formats(PNG with alpha). Toolkit fails for other formats (compressed TGA)
	 */
	public static ImageRGBAByte getImageRGBAByte(File filename)
	{
		try
		{
			BufferedImage image = ImageIO.read(filename);
			Raster raster = image.getData();	//COPY!!! :/
			//((DataBufferByte)raster.getDataBuffer()).getData()
			//image.getColorModel().getColorSpace().getName(idx)
			int imageType = image.getType();
			switch (imageType)
			{
				case BufferedImage.TYPE_3BYTE_BGR: return read_TYPE_3BYTE_BGR(raster);
				case BufferedImage.TYPE_4BYTE_ABGR: return read_TYPE_4BYTE_RGBA(raster);
				case BufferedImage.TYPE_BYTE_GRAY: return read_TYPE_3BYTE_BGR(raster);
				case BufferedImage.TYPE_INT_ARGB: return read_TYPE_3BYTE_BGR(raster);
				case BufferedImage.TYPE_INT_BGR: return read_TYPE_3BYTE_BGR(raster);
				case BufferedImage.TYPE_INT_RGB: return read_TYPE_3BYTE_BGR(raster);
				case BufferedImage.TYPE_USHORT_GRAY: return read_TYPE_3BYTE_BGR(raster);
				default: throw new Exception("ImageLoader: Unsupported Image Type: " + translateImageType(imageType));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private static ImageRGBAByte read_TYPE_3BYTE_BGR(Raster source)
	{
		final int w = source.getWidth();
		final int h = source.getHeight();
		final ImageRGBAByte result = new ImageRGBAByte(w, h);
		final byte[] newData = result.data;
		
		//Extract Data out of source Raster
		int data[] = new int[w * h * 3];
		source.getPixels(0, 0, w, h, data);
		
		//Copy Data into result ImageRGBAByte
		int scanlineSizeSource = w * 3;
		int yOffsetSource = 0;
		int scanlineSizeDestination = w * result.SAMPLES_PER_PIXEL;
		int yOffsetDestination = 0;
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				newData[x + yOffsetDestination + 0] = (byte)data[x + scanlineSizeSource + 2];
				newData[x + yOffsetDestination + 1] = (byte)data[x + scanlineSizeSource + 1];
				newData[x + yOffsetDestination + 2] = (byte)data[x + scanlineSizeSource + 0];
				newData[x + yOffsetDestination + 3] = (byte)0;
			}
			yOffsetSource += scanlineSizeSource;
			yOffsetDestination += scanlineSizeDestination;
		}
		return result;
	}
	
	private static ImageRGBAByte read_TYPE_4BYTE_RGBA(Raster source)
	{
		final int w = source.getWidth();
		final int h = source.getHeight();
		final ImageRGBAByte result = new ImageRGBAByte(w, h);
		final byte[] newData = result.data;
		
		//Extract Data out of source Raster
		int data[] = new int[w * h * 3];
		source.getPixels(0, 0, w, h, data);
		
		//Copy Data into result ImageRGBAByte
		int scanlineSizeSource = w * 3;
		int yOffsetSource = 0;
		int scanlineSizeDestination = w * result.SAMPLES_PER_PIXEL;
		int yOffsetDestination = 0;
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				newData[x + yOffsetDestination + 0] = (byte)data[x + scanlineSizeSource + 2];
				newData[x + yOffsetDestination + 1] = (byte)data[x + scanlineSizeSource + 1];
				newData[x + yOffsetDestination + 2] = (byte)data[x + scanlineSizeSource + 0];
				newData[x + yOffsetDestination + 3] = (byte)0;
			}
			yOffsetSource += scanlineSizeSource;
			yOffsetDestination += scanlineSizeDestination;
		}
		return result;
	}
}
