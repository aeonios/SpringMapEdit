package backend.map;

import java.io.File;

import backend.FileHandler;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;
import backend.image.ImageGrayscaleFloat;
import backend.image.ImageLoader;
import backend.image.ImageSaver;
import frontend.render.brushes.Brush;
import frontend.render.brushes.PrefabBrush;

public abstract class AbstractMap {
	protected final int mapSizeFactor = 1; // Ratio to which a map size corresponds to internal size

	protected byte[][] map; 
	protected int mapWidth;
	protected int mapLength;

	private int bufferWidth;
	private byte[] buffer;
	
	public AbstractMap(int length, int width)
	{
		this.mapWidth = width;
		this.mapLength = length;
		this.map = new byte[length][width];
		this.buffer = new byte[0]; // So length is readable
	}
	
	public int getMapLength()
	{
		return mapLength;
	}
	
	public int getMapWidth()
	{
		return mapWidth;
	}
	
	/**
	 * @param the map of values to copy into internal map variable
	 * @return
	 */
	public void setMap(byte[][] map)
	{
		for (int y = 0; y < mapLength; y++)
			for (int x = 0; x < mapWidth; x++)
				this.map[y][x] = map[y][x];
	}
	
	public byte[][] getMap()
	{
		return map;
	}

	public boolean validLength(int py)
	{
		if (py < 0)
			return false;
		if (py > mapLength)
			return false;
		return true;
	}
	
	public boolean validWidth(int px)
	{
		if (px < 0)
			return false;
		if (px > mapWidth)
			return false;
		return true;
	}
	
	public boolean validPosition(int px, int py)
	{
		return validWidth(px) && validLength(py);
	}
	
	/**
	 * Retrives value at specified points
	 * @param px
	 * @param py
	 * @return
	 */
	public float getValue(int px, int py)
	{
		if (!validPosition(px, py))
			return 0;
		return map[py][px];
	}
	
	public float[][] getHeigth(int x, int y, int xe, int ye)
	{
		float[][] hm = new float[ye - y][xe - x];
		if (!validPosition(x, y))
			return null;
		if (!validWidth(xe))
			xe = mapWidth;
		if (!validLength(ye))
			ye = mapLength;
		for (int i = y; i < ye; i++)
			System.arraycopy(map[i], x, hm[i - y], 0, xe - x);
		return hm;
	}
	
	public void saveMap(File mapFile, FileFormat fileFormat)
	{
		if (fileFormat == FileFormat.PNG16Bit)
			ImageSaver.saveImageGrayscaleFloat(mapFile, new ImageGrayscaleFloat(map), fileFormat, true);
		else
			new Bitmap(fileFormat).saveDataFromByteMap(mapFile, map, mapWidth, mapLength);
	}
	
	/**
	 * Loads image data into map
	 * NOTE: This skips RAW checking. Do not use for loading RAW's!
	 * @param mapFile
	 * @return
	 */
	public void loadDataIntoMap(File mapFile)
	{
		/*if (FileHandler.isHandledByBitmap(mapFile))
		{*/
			Bitmap bitmap = new Bitmap(mapFile);
			if (bitmap.width != mapWidth)
				throw new IllegalArgumentException("Image width must be: " + mapWidth);
			if (bitmap.height != mapLength)
				throw new IllegalArgumentException("Image length must be: " + mapLength);
			bitmap.loadDataIntoBytemap(this.map);
		/*}
		else
		{
			ImageGrayscaleFloat image = ImageLoader.loadImageGrayscaleFloat(mapFile, true);
			byte[][] data = image.data;
			for (int y = 0; y < mapLength; y++) // Warning axis references are inverted!
				for (int x = 0; x < mapWidth; x++)
					map[y][x] = data[x][y]; //System.arraycopy(data[y], 0, heightMap[y], 0, image.width);
		}*/
	}
	
	/**
	 * Change the size of the map possibly removing values that cannot fit in if downsized
	 * @return
	 */
	public void resizeMap(int newLength, int NewWidth)
	{
		byte[][] newMap = new byte[newLength][NewWidth];
		int width = NewWidth;
		if (width > mapWidth)
			width = mapWidth;
		int height = newLength;
		if (height > mapLength)
			height = mapLength;
		for (int y = 0; y < height; y++)
			System.arraycopy(map[y], 0, newMap[y], 0, width);
		map = newMap;
		mapWidth = NewWidth;
		mapLength = newLength;
	}
	
	/**
	 * Switch map axis making all values in their horizontal position vertical
	 * @return
	 */
	public void switchMapAxis()
	{
		int temp = mapWidth;
		mapWidth = mapLength;
		mapLength = temp;
		byte[][] newMap = new byte[mapLength][mapWidth];
		for (int y = 0; y < mapLength; y++)
			for (int x = 0; x < mapWidth; x++)
				newMap[y][x] = map[x][y];
		map = newMap;
	}
	
	/**
	 * Move portion of map from start to length by amount either vertically or horizontally
	 * @param start
	 * @param length
	 * @param amount
	 * @param vertically
	 * @return
	 */
	public void moveMap(int start, int length, int amount, boolean vertically)
	{
		byte[][] temp;
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
			temp = new byte[mapLength][amount];
			for (int y = 0; y < mapLength; y++)
				for (int x = 0; x < amount; x++)
					temp[y][x] = map[y][start + length - amount + x];
			for (int y = 0; y < mapLength; y++)
				for (int x = length - amount - 1; x >= 0; x--)
					map[y][start + amount + x] = map[y][start + x];
			for (int y = 0; y < mapLength; y++)
				for (int x = 0; x < amount; x++)
					map[y][start + x] = temp[y][x];
		}
		else
		{
			if (start >= mapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > mapLength)
				length = mapLength - start;
			if (length <= 0)
				return;
			temp = new byte[amount][mapWidth];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < mapWidth; x++)
					temp[y][x] = map[start + length - amount + y][x];
			for (int y = length - amount - 1; y >= 0; y--)
				for (int x = 0; x < mapWidth; x++)
					map[start + amount + y][x] = map[start + y][x];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < mapWidth; x++)
					map[start + y][x] = temp[y][x];
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
			if (offset + start >= mapWidth)
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
			for (int y = 0; y < mapLength; y++)
				for (int x = 0; x < length; x++)
					map[y][offset - x] = map[y][start + x];
		}
		else
		{
			if (start >= mapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > mapLength)
				length = mapLength - start;
			if (offset + start >= mapLength)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > mapLength)
				length = mapLength - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < mapWidth; x++)
					map[offset - y][x] = map[start + y][x];
		}
	}
	
	public void flipMap(int start, int length, int offset, boolean vertically)
	{
		byte temp;
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
			if (offset + start >= mapWidth)
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
			for (int y = 0; y < mapLength; y++)
				for (int x = 0; x < length; x++)
				{
					temp = map[y][offset - x];
					map[y][offset - x] = map[y][start + x];
					map[y][start + x] = temp;
				}
		}
		else
		{
			if (start >= mapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > mapLength)
				length = mapLength - start;
			if (offset + start >= mapLength)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > mapLength)
				length = mapLength - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < mapWidth; x++)
				{
					temp = map[offset - y][x];
					map[offset - y][x] = map[start + y][x];
					map[start + y][x] = temp;
				}
		}
	}
	
	/**
	 * Copy a part of the map to internal buffer
	 * @param px
	 * @param py
	 * @param height
	 * @param width
	 * @return
	 */
	public void copy(int py, int px, int height, int width)
	{
		if (py >= mapLength || px >= mapWidth)
			return;
		if (py < 0)
		{
			height += py;
			py = 0;
		}
		if (px < 0)
		{
			width =+ px;
			px = 0;
		}
		if (py + height >= mapLength)
			height = mapLength - py;
		if (px + width  >= mapWidth)
			width = mapWidth - px;
		if (height <= 0 || width <= 0)
			return;
		buffer = new byte[height * width];
		bufferWidth = width;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				buffer[y * width + x] = map[py + y][px + x];
	}
	
	public void paste(int py, int px, int height, int width)
	{
		/*float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();*/
		byte[] tempBuffer = buffer;
		int tempBufferWidth = bufferWidth;
		if (px >= mapWidth || py >= mapLength)
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
		if (height + py >= mapLength)
			height = mapLength - py;
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
				map[py + y][px + x] = tempBuffer[y * tempBufferWidth + x];// + (amount * pattern[x - px][y - py]);
	}
	
	public void modify(int px, int py, Brush brush, boolean invert)
	{
		float amount = brush.getStrength();
		if (invert)
			amount = -amount;
		float[][] pattern = brush.getPattern().getPattern();
		for (int y = py; y < py + brush.getHeight(); y++)
			for (int x = px; x < px + brush.getWidth(); x++)
				if ((y < mapLength) && (y >= 0) && (x < mapWidth) && (x >= 0))
				{
					map[y][x] += amount * pattern[x - px][y - py];
					if (map[y][x] > 1)
						map[y][x] = 1;
					else if (map[y][x] < 0)
						map[y][x] = 0;
				}
	}
	
	public void set(int px, int py, Brush brush, byte setHeight, boolean invert)
	{
		float[][] pattern = brush.getPattern().getPattern();
		for (int y = py; y < py + brush.getHeight(); y++)
			for (int x = px; x < px + brush.getWidth(); x++)
				if ((x >= 0) && (x < mapWidth) && (y >= 0) && (y < mapLength))
				{
					map[y][x] += (setHeight - map[x][y]) * pattern[x - px][y - py];
					if (map[y][x] > 1)
						map[y][x] = 1;
					else if (map[y][x] < 0)
						map[y][x] = 0;
				}
	}
	
	public void addToMap(byte setHeight)
	{
		for (int y = 0; y < mapLength; y++)
			for (int x = 0; x < mapWidth; x++) {
				map[y][x] += setHeight;
				if (map[y][x] > 1)
					map[y][x] = 1;
				else if (map[y][x] < 0)
					map[y][x] = 0;	
			}	
	}
	
	public void setToMap(byte setHeight)
	{
		for (int y = 0; y < mapLength; y++)
			for (int x = 0; x < mapWidth; x++)
				map[y][x] = setHeight;
	}
	
	public void smoothMap(int px, int py, Brush brush, float limit)
	{
		byte[][] smoothedMap = new byte[brush.getHeight()][brush.getWidth()];
		float[][] pattern = brush.getPattern().getPattern();
		float[][] newPattern = pattern;
		int length = brush.getHeight();
		int width = brush.getWidth();
		if (px >= mapWidth || py >= mapLength)
			return;
		if (px < 0)
		{
			width += px;
			if (width <= 0)
				return;
			newPattern = new float[width][length];
			System.arraycopy(pattern, -px - 1, newPattern, 0, width);
			pattern = newPattern;
			px = 0;
		}
		if (py < 0)
		{
			length += py;
			if (length <= 0)
				return;
			newPattern = new float[width][length];
			for (int x = 0; x < width; x++)
				System.arraycopy(pattern[x], -py - 1, newPattern[x], 0, length);
			pattern = newPattern;
			py = 0;
		}
		if (length + py >= mapLength)
			length = mapLength - py;
		if (width + px >= mapWidth)
			width = mapWidth - px;
		if (width <= 0 || length <= 0)
			return;
		byte yStart = map[py][px + width / 2];
		byte xStart = map[py + length / 2][px];
		//byte yGradient = (map[py + length - 1][px + width / 2] - yStart) / length;
		//byte xGradient = (map[py + length / 2][px + width - 1] - xStart) / width;
		for (int y = 0; y < length - 2; y++)
			for (int x = 0; x < width - 2; x++)
			{
				//smoothedMap[y][x] = newSmooth(limit, px + 1 + x, py + 1 + y, xStart + xGradient * x) + newSmooth(limit, px + 1 + x, py + 1 + y, yStart + yGradient); //smooth9HeightmapBorderAware(limit, px + x, py + y);
			}
		for (int y = 0; y < length - 2; y++)
			for (int x = 0; x < width - 2; x++)
				map[py + 1 + y][px + 1 + x] += smoothedMap[y][x] * pattern[x][y];
	}
}
