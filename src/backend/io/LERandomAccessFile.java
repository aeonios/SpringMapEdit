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
 * LERandomAccessFile.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package backend.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Heiko Schmitt
 *
 */
public class LERandomAccessFile
{
	private RandomAccessFile data;
	private byte[] buf;
	
	/**
	 * Opens a file for binary access.<BR>
	 * mode may be "r" or "rw"
	 * @param file
	 * @param mode
	 * @throws FileNotFoundException
	 */
	public LERandomAccessFile(File file, String mode) throws FileNotFoundException
	{
		data = new RandomAccessFile(file, mode);
		buf = new byte[8];
	}
	
	public long getFilePointer() throws IOException
	{
		return data.getFilePointer();
	}
	
	public void seek(long pos) throws IOException
	{
		data.seek(pos);
	}
	
	public void skip(int n) throws IOException
	{
		data.skipBytes(n);
	}
	
	public boolean readBoolean() throws IOException
	{
		return (data.readUnsignedByte() != 0);
	}

	public int readUnsignedByte() throws IOException
	{
		return data.readUnsignedByte();
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
		data.read(buf, 0, 4);
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
		data.read(tempBuf, 0, count);
		return new String(tempBuf, 0, count);
	}
	
	public String readString() throws IOException
	{
		StringBuilder sb = new StringBuilder();
		buf[0] = (byte)data.readUnsignedByte();
		while (buf[0] != 0)
		{
			sb.append((char)buf[0]);
			buf[0] = (byte)data.readUnsignedByte();
		}
		return sb.toString();
	}

	public long readLong() throws IOException
	{
		data.read(buf, 0, 8);
		return (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8) + ((buf[2] & 0xFF) << 16) + ((buf[3] & 0xFF) << 24) + ((buf[4] & 0xFF) << 32) + ((buf[5] & 0xFF) << 40) + ((buf[6] & 0xFF) << 48) + ((buf[7] & 0xFF) << 56);
	}

	public int readUnsignedShort() throws IOException
	{
		data.read(buf, 0, 2);
		return (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8);
	}

	public void read(byte[] b, int off, int len) throws IOException
	{
		data.read(b, off, len);
	}
	
	public void close() throws IOException
	{
		data.close();
	}
	
	public void writeUnsignedByte(int v) throws IOException
	{
		data.write(v);
	}
	
	public void writeBoolean(boolean v) throws IOException
	{
		data.write(v ? 0xFF : 0);
	}
	
	public void writeDouble(double v) throws IOException
	{
		throw new IOException("writeDouble not implemented!");
	}

	public void writeFloat(float v) throws IOException
	{
		writeInt(Float.floatToIntBits(v));
	}
	
	public void writeInt(int v) throws IOException
	{
		buf[0] = (byte)(v & 0xFF);
		buf[1] = (byte)((v >> 8) & 0xFF);
		buf[2] = (byte)((v >> 16) & 0xFF);
		buf[3] = (byte)((v >> 24) & 0xFF);
		data.write(buf, 0, 4);
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
		data.write(buf, 0, 8);
	}
	
	public void writeUnsignedShort(int v) throws IOException
	{
		buf[0] = (byte)(v & 0xFF);
		buf[1] = (byte)((v >> 8) & 0xFF);
		data.write(buf, 0, 2);
	}
	
	public void writeString(String s, boolean appendNull) throws IOException
	{
		byte[] tempBuf = s.getBytes();
		data.write(tempBuf, 0, s.length());
		
		if (appendNull)
			data.write(0);
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		data.write(b, off, len);
	}
	
	public void zeroFill(int n) throws IOException
	{
		byte[] tmpBuf = new byte[n];
		data.write(tmpBuf, 0, n);
	}
}
