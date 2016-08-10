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
 * TextureLoader.java 
 * Created on 21.01.2009
 * by Heiko Schmitt
 */
package frontend.render;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;

/**
 * Utility class for Texture Loading (Just when TextureIO fails for some reason...)
 * @author Heiko Schmitt
 */
public class TextureLoader
{
	/**
	 * May be used for GIF, JPG and PNG
	 * @param gl
	 * @param file
	 * @return
	 */
	public static int getTextureFromToolkit(GL gl, File file)
	{
		try
		{
			Image image = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
			while (image.getWidth(null) < 0)
			{
				Thread.sleep(1);
			}
			int width = image.getWidth(null);
			int height = image.getHeight(null);
			int[] pixels = new int[width * height];
			byte[] rgba = new byte[width * height * 4];
			int scanlineSize = width * 4;
			PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
			pg.grabPixels();
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					rgba[(x * 4) + ((height - y - 1) * scanlineSize) + 0] = (byte)((pixels[x + (y * width)] >> 16) & 0xFF);
					rgba[(x * 4) + ((height - y - 1) * scanlineSize) + 1] = (byte)((pixels[x + (y * width)] >>  8) & 0xFF);
					rgba[(x * 4) + ((height - y - 1) * scanlineSize) + 2] = (byte)((pixels[x + (y * width)]      ) & 0xFF);
					rgba[(x * 4) + ((height - y - 1) * scanlineSize) + 3] = (byte)((pixels[x + (y * width)] >> 24) & 0xFF);
				}
			}
			
			int[] tmp = new int[1];
			gl.glGenTextures(1, tmp, 0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, tmp[0]);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(rgba));
			
			return tmp[0];
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
}
