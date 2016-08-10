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
 * LEDataInputStream.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package backend.io;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author Heiko Schmitt
 *
 */
public class LEDataInputStream 
{
	private InputStream inStream;
	private byte[] buf;
	
	public LEDataInputStream(InputStream inStream)
	{
		this.inStream = inStream;
		buf = new byte[8];
	}
		
	public void skip(long n) throws IOException
	{
		inStream.skip(n);
	}
	
	public boolean readBoolean() throws IOException
	{
		return (inStream.read() != 0);
	}

	public int readUnsignedByte() throws IOException
	{
		return inStream.read();
	}

	public double readDouble() throws IOException
	{
		throw new IOException("readDouble not implemented!");
	}

	public float readFloat() throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}

	public int readInt() throws IOException
	{
		inStream.read(buf, 0, 4);
		return (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8) + ((buf[2] & 0xFF) << 16) + ((buf[3] & 0xFF) << 24);
	}

	/**
	 * Note: reads just ascii (i think)
	 * @param count
	 * @return
	 * @throws IOException
	 */
	public String readString(int count) throws IOException
	{
		byte[] tempBuf = new byte[count];
		inStream.read(tempBuf, 0, count);
		return new String(tempBuf, 0, count);
	}
	
	public String readString() throws IOException
	{
		StringBuilder sb = new StringBuilder();
		buf[0] = (byte)inStream.read();
		while (buf[0] != 0)
		{
			sb.append((char)buf[0]);
			buf[0] = (byte)inStream.read();
		}
		return sb.toString();
	}

	public long readLong() throws IOException
	{
		inStream.read(buf, 0, 8);
		return (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8) + ((buf[2] & 0xFF) << 16) + ((buf[3] & 0xFF) << 24) + ((buf[4] & 0xFF) << 32) + ((buf[5] & 0xFF) << 40) + ((buf[6] & 0xFF) << 48) + ((buf[7] & 0xFF) << 56);
	}

	public int readUnsignedShort() throws IOException
	{
		inStream.read(buf, 0, 2);
		return (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8);
	}

	public void read(byte[] b, int off, int len) throws IOException
	{
		inStream.read(b, off, len);
	}
	
	public void close() throws IOException
	{
		inStream.close();
	}
}
