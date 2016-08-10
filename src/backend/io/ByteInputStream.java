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
 * ByteInputStream.java 
 * Created on 12.02.2009
 * by Heiko Schmitt
 */
package backend.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Heiko Schmitt
 *
 */
public class ByteInputStream extends InputStream
{
	private int blockSize;
	private ArrayList<byte[]> blockList;
	private int currentBlockIndex;
	private byte[] currentBlock;
	private int currentOffset;
	private int currentGlobalOffset;
	private int size;
	
	/**
	 * 
	 */
	public ByteInputStream(int blockSize, ArrayList<byte[]> blockList, int size)
	{
		this.blockSize = blockSize;
		this.blockList = blockList;
		this.size = size;
		currentBlockIndex = -1;
		
		moveToNextBlock();
	}
	
	private void moveToNextBlock()
	{
		currentBlockIndex++;
		currentOffset = 0;
		currentGlobalOffset = (currentBlockIndex * blockSize);
		
		if (blockList.size() > currentBlockIndex)
			currentBlock = blockList.get(currentBlockIndex);	
	}

	@Override
	public int read() throws IOException
	{
		if (currentGlobalOffset < size)
		{
			int result = (currentBlock[currentOffset] & 0xFF); 
			currentOffset++;
			currentGlobalOffset++;
			if (currentOffset >= blockSize) moveToNextBlock();
			
			return result;
		}
		else
			return -1;
	}
	
	@Override
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (currentGlobalOffset < size)
		{
			len = Math.min(len, size - currentGlobalOffset);
			int bytesLeft = len;
			int maxBytesToRead = blockSize - currentOffset;
			while (bytesLeft > maxBytesToRead)
			{
				System.arraycopy(currentBlock, currentOffset, b, off, maxBytesToRead);
				off += maxBytesToRead;
				bytesLeft -= maxBytesToRead;
				moveToNextBlock();
				
				maxBytesToRead = blockSize - currentOffset;
			}
			System.arraycopy(currentBlock, currentOffset, b, off, bytesLeft);
			currentOffset += bytesLeft;
			currentGlobalOffset += len;
			
			return len;
		}
		else 
			return -1;
	}
	
}
