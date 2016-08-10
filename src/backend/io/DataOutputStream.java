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
 * DataOutputStream.java 
 * Created on 13.02.2009
 * by Heiko Schmitt
 */
package backend.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Heiko Schmitt
 *
 */
public class DataOutputStream extends FilterOutputStream
{
	public DataOutputStream(OutputStream out)
	{
		super(out);
	}

	@Override
	public void write(int b) throws IOException
	{
		out.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		out.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		out.write(b, off, len);
	}
	
	public void write(float[] b, int off, int len) throws IOException
	{
		byte[] buf = new byte[4];
		int intBits;
		for (int i = 0; i < len; i++)
		{
			intBits = Float.floatToIntBits(b[off + i]);
			buf[0] = (byte)(intBits & 0xFF);
			buf[1] = (byte)((intBits >> 8) & 0xFF);
			buf[2] = (byte)((intBits >> 16) & 0xFF);
			buf[3] = (byte)((intBits >> 24) & 0xFF);
			out.write(buf, 0, 4);
		}
	}
}
