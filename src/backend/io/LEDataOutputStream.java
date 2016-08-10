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
 * LEDataOutputStream.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package backend.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Heiko Schmitt
 *
 */
public class LEDataOutputStream
{
	private OutputStream outStream;
	private byte[] buf;

	public LEDataOutputStream(OutputStream outStream)
	{
		this.outStream = outStream;
		buf = new byte[8];
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		outStream.write(b, off, len);
	}

	public void writeBoolean(boolean v) throws IOException
	{
		outStream.write(v ? 0xFF : 0);
	}

	public void writeUnsignedByte(int v) throws IOException
	{
		outStream.write(v);
	}

	public void writeString(String s, boolean appendNull) throws IOException
	{
		int len = s.length();
		byte[] tmpBuf = new byte[len];
		
		for (int i = 0; i < len; i++)
			tmpBuf[i] = (byte)s.charAt(i);
		
		outStream.write(tmpBuf, 0, len);

		if (appendNull)
			outStream.write(0);
	}

	public void writeDouble(double v) throws IOException
	{
		throw new IOException("writeDouble not implemented!");
	}

	public void writeFloat(float v) throws IOException
	{
		throw new IOException("writeFloat not implemented!");
	}

	public void writeInt(int v) throws IOException
	{
		buf[0] = (byte)(v & 0xFF);
		buf[1] = (byte)((v >> 8) & 0xFF);
		buf[2] = (byte)((v >> 16) & 0xFF);
		buf[3] = (byte)((v >> 24) & 0xFF);
		outStream.write(buf, 0, 4);
	}

	public void writeLong(long v) throws IOException
	{
		buf[0] = (byte)(v & 0xFF);
		buf[1] = (byte)((v >> 8) & 0xFF);
		buf[2] = (byte)((v >> 16) & 0xFF);
		buf[3] = (byte)((v >> 24) & 0xFF);
		buf[4] = (byte)((v >> 32) & 0xFF);
		buf[5] = (byte)((v >> 40) & 0xFF);
		buf[6] = (byte)((v >> 48) & 0xFF);
		buf[7] = (byte)((v >> 56) & 0xFF);
		outStream.write(buf, 0, 8);
	}

	public void writeUnsignedShort(int v) throws IOException
	{
		buf[0] = (byte)(v & 0xFF);
		buf[1] = (byte)((v >> 8) & 0xFF);
		outStream.write(buf, 0, 2);
	}
	
	public void zeroFill(int n) throws IOException
	{
		byte[] tmpBuf = new byte[n];
		outStream.write(tmpBuf, 0, n);
	}

	public void close() throws IOException
	{
		outStream.close();
	}
}
