package backend.map;

import java.io.File;
import java.util.Random;

import frontend.render.brushes.Brush;
import frontend.render.brushes.VegetationBrush;

import backend.FileHandler;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;
import backend.image.ImageGrayscaleFloat;
import backend.image.ImageLoader;
import backend.image.ImageSaver;

public class Vegetationmap  {
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual heightmap-size. (do not forget to add 1 pixel to heightmap)
	 */
	public static final int springMapsizeVegetationmapFactor = Map.springMapSizeHeightmapFactor / 4;
	
	private byte[][] heightMap; 
	private int mapWidth;
	private int heightmapLength;
	
	private byte[] buffer;
	int bufferWidth = 0;
	
	public Vegetationmap(int length, int width)
	{
		this.mapWidth = width * springMapsizeVegetationmapFactor;
		this.heightmapLength = length * springMapsizeVegetationmapFactor;
		this.heightMap = new byte[heightmapLength][mapWidth];
		this.buffer = new byte[0];
	}
	
	public int getHeightmapLength()
	{
		return heightmapLength;
	}
	
	public int getHeightmapWidth()
	{
		return mapWidth;
	}
	
	public void setHeightMap(byte[][] map)
	{
		for (int i = 0; i < mapWidth; i++)
			for (int j = 0; j < heightmapLength; j++)
				heightMap[i][j] = map[i][j];
	}
	
	public byte[][] getHeightMap()
	{
		return heightMap;
	}
	
	public float getHeigth(int px, int py)
	{
		if (!validPosition(px, py))
			return 0;
		return heightMap[py][px];
	}
	
	private boolean validPosition(int px, int py) {
		return validLength(py) && validWidth(px);
	}

	public float[][] getHeigth(int x, int y, int xe, int ye)
	{
		float[][] hm = new float[ye - y][xe - x];
		if (!validPosition(x, y))
			return null;
		if (!validWidth(xe))
			xe = mapWidth;
		if (!validLength(ye))
			ye = heightmapLength;
		for (int i = y; i < ye; i++)
			System.arraycopy(heightMap[i], x, hm[i - y], 0, xe - x);
		return hm;
	}
	
	private boolean validLength(int y) {
		if (y >= 0 && y < heightmapLength)
			return true;
		else 
			return false;
	}

	private boolean validWidth(int x) {
		if (x >= 0 && x < mapWidth)
			return true;
		else 
			return false;
	}

	
	
	
	
	public void switchMapAxis()
	{
		int t = mapWidth;
		mapWidth = heightmapLength;
		heightmapLength = t;
		byte[][] newMap = new byte[heightmapLength][mapWidth];
		for (int y = 0; y < heightmapLength; y++)
			for (int x = 0; x < mapWidth; x++)
				newMap[y][x] = heightMap[x][y];
		heightMap = newMap;
	}
	
	public void resizeMap(int NewHeight, int NewWidth)
	{
		byte[][] newMap = new byte[NewHeight][NewWidth];
		int width = NewWidth;
		if (width > mapWidth)
			width = mapWidth;
		int height = NewHeight;
		if (height > heightmapLength)
			height = heightmapLength;
		for (int y = 0; y < height; y++)
			System.arraycopy(heightMap[y], 0, newMap[y], 0, width);
		heightMap = newMap;
		mapWidth = NewWidth;
		heightmapLength = NewHeight;
	}
	
	/**
	 * Move portion of heightmap from start to length by amount either vertically or horizontally
	 * @param Start of the move
	 * @param Length from start to which movement happens
	 * @param The amount by which the values are moved
	 * @param vertically
	 */
	public void moveMap(int start, int length, int amount, boolean vertically)
	{
		byte[][] t;
		if (amount <= 0 || amount >= length)
			return;
		if (vertically)
		{
			if (start >= mapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > mapWidth)
				length = mapWidth - start;
			if (length <= 0)
				return;
			t = new byte[heightmapLength][amount];
			for (int y = 0; y < heightmapLength; y++)
				for (int x = 0; x < amount; x++)
					t[y][x] = heightMap[y][start + length - amount + x];
			for (int y = 0; y < heightmapLength; y++)
				for (int x = length - amount - 1; x >= 0; x--)
					heightMap[y][start + amount + x] = heightMap[y][start + x];
			for (int y = 0; y < heightmapLength; y++)
				for (int x = 0; x < amount; x++)
					heightMap[y][start + x] = t[y][x];
		}
		else
		{
			if (start >= heightmapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > heightmapLength)
				length = heightmapLength - start;
			if (length <= 0)
				return;
			t = new byte[amount][mapWidth];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < mapWidth; x++)
					t[y][x] = heightMap[start + length - amount + y][x];
			for (int y = length - amount - 1; y >= 0; y--)
				for (int x = 0; x < mapWidth; x++)
					heightMap[start + amount + y][x] = heightMap[start + y][x];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < mapWidth; x++)
					heightMap[start + y][x] = t[y][x];
		}
	}
	
	public void mirrorMap(int start, int length, int offset, boolean vertically)
	{
		if (vertically)
		{
			if (start >= mapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > mapWidth)
				length = mapWidth - start;
			if (start + offset >= mapWidth)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > mapWidth)
				length = mapWidth - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < heightmapLength; y++)
				for (int x = 0; x < length; x++)
					heightMap[y][offset - x] = heightMap[y][start + x];
		}
		else
		{
			if (start >= heightmapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > heightmapLength)
				length = heightmapLength - start;
			if (offset + start >= heightmapLength)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > heightmapLength)
				length = heightmapLength - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < mapWidth; x++)
					heightMap[offset - y][x] = heightMap[start + y][x];
			//System.arraycopy(heightMap, start + y, heightMap, offset - y, length);
		}
	}
	
	public void flipMap(int start, int length, int offset, boolean vertically)
	{
		byte t;
		if (vertically)
		{
			if (start >= mapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > mapWidth)
				length = mapWidth - start;
			if (start + offset >= mapWidth)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > mapWidth)
				length = mapWidth - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < heightmapLength; y++)
				for (int x = 0; x < length; x++)
				{
					t = heightMap[y][offset - x];
					heightMap[y][offset - x] = heightMap[y][start + x];
					heightMap[y][start + x] = t;
				}
		}
		else
		{
			if (start >= heightmapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > heightmapLength)
				length = heightmapLength - start;
			if (offset + start >= heightmapLength)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > heightmapLength)
				length = heightmapLength - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < mapWidth; x++)
				{
					t = heightMap[offset - y][x];
					heightMap[offset - y][x] = heightMap[start + y][x];
					heightMap[start + y][x] = t;
				}
		}
	}
	
	public void copy(int px, int py, int height, int width)
	{
		if (py >= heightmapLength || px >= mapWidth)
			return;
		if (px < 0)
		{
			width =+ px;
			px = 0;
		}
		if (py < 0)
		{
			height += py;
			py = 0;
		}
		if (py + height >= heightmapLength)
			height = heightmapLength - py;
		if (px + width  >= mapWidth)
			width = mapWidth - px;
		if (height <= 0 || width <= 0)
			return;
		buffer = new byte[height * width];
		bufferWidth = width;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				buffer[y * width + x] = heightMap[py + y][px + x];
	}
	
	public void paste(int px, int py, Brush brush)
	{
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		byte[] tempBuffer = buffer;
		int tempBufferWidth = bufferWidth;
		int height = brush.getHeight();
		int width = brush.getWidth();
		if (py >= heightmapLength || px >= mapWidth)
			return;
		if (px < 0)
		{
			width += px;
			tempBufferWidth += px;
			if (width <= 0 || tempBufferWidth <= 0)
				return;
			tempBuffer = new byte[buffer.length / bufferWidth * tempBufferWidth];
			for (int y = 0; y < buffer.length / bufferWidth; y++)
				System.arraycopy(buffer, y * bufferWidth - px, tempBuffer, y * tempBufferWidth, tempBufferWidth);
			px = 0;
		}
		if (py < 0)
		{
			height += py;
			if (height <= 0 || tempBufferWidth <= 0 || (buffer.length / tempBufferWidth) + py <= 0)
				return;
			tempBuffer = new byte[((buffer.length / tempBufferWidth) + py) * tempBufferWidth];
			System.arraycopy(buffer, -py * tempBufferWidth, tempBuffer, 0, ((buffer.length / tempBufferWidth) + py) * tempBufferWidth);
			py = 0;
		}
		if (height + py >= heightmapLength)
			height = heightmapLength - py;
		if (width + px >= mapWidth)
			width = mapWidth - px;
		if (width > tempBufferWidth)
			width = tempBufferWidth;
		if (width <= 0 || tempBufferWidth <= 0)
			return;
		if (height > buffer.length / tempBufferWidth)
			height = buffer.length / tempBufferWidth;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				heightMap[py + y][px + x] = tempBuffer[y * tempBufferWidth + x];// + (amount * pattern[x - px][y - py]);
	}
	
	public void randomizeVegetation(int px, int py, VegetationBrush brush, float amount)
	{
		Random r = new Random();
		float amountHalf = amount / 2; //brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		float[][] newPattern;
		int height = brush.getHeight();
		int width = brush.getWidth();
		if (px >= mapWidth || py >= heightmapLength)
			return;
		if (px < 0)
		{
			width += px;
			if (width <= 0)
				return;
			newPattern = new float[width][height];
			System.arraycopy(pattern, -px - 1, newPattern, 0, width);
			pattern = newPattern;
			px = 0;
		}
		if (py < 0)
		{
			height += py;
			if (height <= 0)
				return;
			newPattern = new float[width][height];
			for (int x = 0; x < width; x++)
				System.arraycopy(pattern[x], -py - 1, newPattern[x], 0, height);
			pattern = newPattern;
			py = 0;
		}
		if (height + py >= heightmapLength)
			height = heightmapLength - py;
		if (width + px >= mapWidth)
			width = mapWidth - px;
		if (width <= 0)
			return;
		for (int y = py; y < py + height; y++)
			for (int x = px; x < px + width; x++)
			{
				//heightMap[y][x] = heightMap[y][x] + ((r.nextFloat() * amount) - amountHalf) * pattern[x - px][y - py];
				if (heightMap[y][x] > 1)
					heightMap[y][x] = 1;
				else if (heightMap[y][x] < 0)
					heightMap[y][x] = 0;
			}
	}
}
