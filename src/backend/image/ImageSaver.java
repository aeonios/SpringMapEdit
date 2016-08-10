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
 * ImageSaver.java 
 * Created on 11.10.2009
 * by Heiko Schmitt
 */
package backend.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.io.File;

import javax.imageio.ImageIO;

import backend.FileHandler;
import backend.FileHandler.FileFormat;

/**
 * @author Heiko Schmitt
 *
 */
public class ImageSaver
{
	private ImageSaver() {}
	
	public static void saveImageGrayscaleFloat(File filename, ImageGrayscaleFloat image, FileFormat fileFormat, boolean showProgress)
	{
		long start = System.nanoTime();
		final int oneTenthsOfHeight = Math.max(image.height / 10, 1);
		
		if (fileFormat == FileFormat.PNG16Bit)
		{
			try
			{
				filename = FileHandler.setFileExtension(filename, ".png");
				final int w = image.width;
				final int h = image.height;
				final float factor = 65535;
				final BufferedImage outImage = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
				final Raster raster = outImage.getRaster();
				final DataBufferUShort buffer = (DataBufferUShort)raster.getDataBuffer();
				final short[] outData = buffer.getData();
				final float[][] imageData = image.data;
				for (int y = 0; y < h; y++)
				{
					for (int x = 0; x < w; x++)
						outData[y * w + x] = (short)Math.max(Math.min(imageData[y][x] * factor, factor), 0);
					if ((showProgress) && (y % oneTenthsOfHeight) == 0)
						System.out.print("#");
				}
				ImageIO.write(outImage, "png", filename);
				
				if (showProgress)
					System.out.println(" saving " + filename.getName() + " (" + w + "x" + h + ") in " + ((System.nanoTime() - start) / 1000000L) + " ms");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
