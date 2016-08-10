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
 * ImageRGBAByte.java 
 * Created on 09.10.2009
 * by Heiko Schmitt
 */
package backend.image;

/**
 * @author Heiko Schmitt
 *
 */
public class ImageRGBAByte extends Image
{
	public byte[] data;
	
	/**
	 * 
	 */
	public ImageRGBAByte(int width, int height)
	{
		this.SAMPLES_PER_PIXEL = 4;
		this.width = width;
		this.height = height;
		data = new byte[this.width * this.height * SAMPLES_PER_PIXEL];
	}

}
