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
 * ByteOutputStream.java 
 * Created on 12.02.2009
 * by Heiko Schmitt
 */
package backend.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * @author Heiko Schmitt
 *
 */
public class ByteOutputStream extends OutputStream
{
	private int blockSize;
	private ArrayList<byte[]> blockList;
	private int currentBlockIndex;
	private byte[] currentBlock;
	private int currentOffset;
	private int size;
	
	/**
	 * Creates an new ByteOutputStream with given blockSize;
	 * @param blockSize determines how large one memory block will be. Allocation happens blockwise.
	 */
	public ByteOutputStream(int blockSize)
	{
		this.blockSize = blockSize;
		
		currentBlockIndex = -1;
		blockList = new ArrayList<byte[]>();
		moveToNextBlock();
	}
	
	private void moveToNextBlock()
	{
		currentBlockIndex++;
		currentOffset = 0;
		
		if (blockList.size() <= currentBlockIndex)
		{
			currentBlock = new byte[blockSize];
			blockList.add(currentBlock);
		}
		else
			currentBlock = blockList.get(currentBlockIndex);
	}
	
	@Override
	public void write(int b) throws IOException
	{
		currentBlock[currentOffset] = (byte)b;
		currentOffset++;
		size++;
		
		if (currentOffset >= blockSize) moveToNextBlock();
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		int bytesLeft = len;
		int maxBytesToWrite = blockSize - currentOffset;
		while (bytesLeft > maxBytesToWrite)
		{
			System.arraycopy(b, off, currentBlock, currentOffset, maxBytesToWrite);
			off += maxBytesToWrite;
			bytesLeft -= maxBytesToWrite;
			moveToNextBlock();
			
			maxBytesToWrite = blockSize - currentOffset;
		}
		System.arraycopy(b, off, currentBlock, currentOffset, bytesLeft);
		currentOffset += bytesLeft;
		size += len;
	}

	/**
	 * Next write will start from the beginning.<BR>
	 * The allocated blocks are NOT released.
	 */
	public void reset()
	{
		size = 0;
		currentBlockIndex = -1;
		moveToNextBlock();
	}
	
	/**
	 * Returns a ByteInputStream which shares data from this Stream.<BR>
	 * This avoids copying the data. (which cannot be avoided for native JAVA streams)
	 * @return
	 */
	public ByteInputStream getByteInputStream()
	{
		return new ByteInputStream(blockSize, blockList, size);
	}

	public int size()
	{
		return size;
	}
}
