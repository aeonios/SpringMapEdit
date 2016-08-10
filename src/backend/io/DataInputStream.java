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
 * DataInputStream.java 
 * Created on 13.02.2009
 * by Heiko Schmitt
 */
package backend.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Heiko Schmitt
 *
 */
public class DataInputStream extends FilterInputStream
{
	
	public DataInputStream(InputStream in)
	{
		super(in);
	}

	@Override
	public int read() throws IOException
	{
		return in.read();
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return in.read(b);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		return in.read(b, off, len);
	}
	
	public int read(float[] b, int off, int len) throws IOException
	{
		byte[] buf = new byte[4];
		int intBits;
		for (int i = 0; i < len; i++)
		{
			in.read(buf, 0, 4);
			intBits = (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8) + ((buf[2] & 0xFF) << 16) + ((buf[3] & 0xFF) << 24);
			b[off + i] = Float.intBitsToFloat(intBits);
		}
		return len;
	}
}
