package backend.map;

import java.io.File;
import java.util.Random;

import frontend.render.brushes.Brush;
import frontend.render.brushes.HeightBrush;
import frontend.render.brushes.PrefabBrush;

import backend.ErosionSetup;
import backend.FastMath;
import backend.FileHandler;
import backend.TerraGenSetup;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;
import backend.image.ImageGrayscaleFloat;
import backend.image.ImageLoader;
import backend.image.ImageSaver;

public class Heightmap extends AbstractMap {
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual heightmap-size. (do not forget to add 1 pixel to heightmap)
	 */
	public static final int springMapsizeHeightmapFactor = 64;
	
	private float[][] heightMap; 
	private int heightMapWidth;
	private int heightmapLength;
	
	private float[] buffer;
	int bufferWidth = 0;
	
	public Heightmap(int length, int width)
	{
		super((length * springMapsizeHeightmapFactor) + 1, (width * springMapsizeHeightmapFactor) + 1);
		this.heightMapWidth = (width * springMapsizeHeightmapFactor) + 1;
		this.heightmapLength = (length * springMapsizeHeightmapFactor) + 1;
		this.heightMap = new float[heightmapLength][heightMapWidth];
		this.buffer = new float[0];
	}
	
	public int getHeightmapLength()
	{
		return heightmapLength;
	}
	
	public int getHeightmapWidth()
	{
		return heightMapWidth;
	}
	
	public void setHeightMap(float[][] map)
	{
		for (int i = 0; i < heightMapWidth; i++)
			for (int j = 0; j < heightmapLength; j++)
				heightMap[i][j] = map[i][j];
	}
	
	public float[][] getHeightMap()
	{
		return heightMap;
	}
	
	public float getHeigth(int px, int py)
	{
		if (!validPosition(px, py))
			return 0;
		return heightMap[py][px];
	}
	
	public float[][] getHeigth(int x, int y, int xe, int ye)
	{
		float[][] hm = new float[ye - y][xe - x];
		if (!validPosition(x, y))
			return null;
		if (!validWidth(xe))
			xe = heightMapWidth;
		if (!validLength(ye))
			ye = heightmapLength;
		for (int i = y; i < ye; i++)
			System.arraycopy(heightMap[i], x, hm[i - y], 0, xe - x);
		return hm;
	}
	
	public void saveHeightMap(File heightmapFile, FileFormat fileFormat)
	{
		if (fileFormat == FileFormat.PNG16Bit)
			ImageSaver.saveImageGrayscaleFloat(heightmapFile, new ImageGrayscaleFloat(heightMap), fileFormat, true);
		else
			new Bitmap(fileFormat).saveDataFromHeightmap(heightmapFile, heightMap, heightMapWidth, heightmapLength);
	}
	
	/**
	 * NOTE: This skips RAW checking. Do not use for loading RAW's!
	 * @param heightmapFile
	 */
	public void loadDataIntoHeightmap(File heightmapFile)
	{
		if (FileHandler.isHandledByBitmap(heightmapFile))
		{
			Bitmap bitmap = new Bitmap(heightmapFile);
			if (bitmap.width != heightMapWidth)
				throw new IllegalArgumentException("Image width must be: " + heightMapWidth);
			if (bitmap.height != heightmapLength)
				throw new IllegalArgumentException("Image length must be: " + heightmapLength);
			bitmap.loadDataIntoHeightmap(heightMap);
		}
		else
			loadDataIntoHeightmap(ImageLoader.loadImageGrayscaleFloat(heightmapFile, true));
	}
	
	public void loadDataIntoHeightmap(Bitmap bitmap)
	{
		if (bitmap.width != heightMapWidth)
			throw new IllegalArgumentException("Image width must be: " + heightMapWidth);
		if (bitmap.height != heightmapLength)
			throw new IllegalArgumentException("Image length must be: " + heightmapLength);
		bitmap.loadDataIntoHeightmap(this.heightMap);
	}
	
	public void loadDataIntoHeightmap(ImageGrayscaleFloat image)
	{
		if (image.width != heightMapWidth)
			throw new IllegalArgumentException("Image width must be: " + heightMapWidth);
		if (image.height != heightmapLength)
			throw new IllegalArgumentException("Image length must be: " + heightmapLength);
		float[][] data = image.data;
		for (int y = 0; y < heightmapLength; y++) // Warning: axis references are inverted!
			for (int x = 0; x < heightMapWidth; x++)
				heightMap[y][x] = data[y][x]; //System.arraycopy(data[y], 0, heightMap[y], 0, image.width);
	}
	
	public void switchMapAxis()
	{
		int t = heightMapWidth;
		heightMapWidth = heightmapLength;
		heightmapLength = t;
		float[][] newMap = new float[heightmapLength][heightMapWidth];
		for (int y = 0; y < heightmapLength; y++)
			for (int x = 0; x < heightMapWidth; x++)
				newMap[y][x] = heightMap[x][y];
		heightMap = newMap;
	}
	
	public void resizeMap(int NewHeight, int NewWidth)
	{
		float[][] newMap = new float[NewHeight][NewWidth];
		int width = NewWidth;
		if (width > heightMapWidth)
			width = heightMapWidth;
		int height = NewHeight;
		if (height > heightmapLength)
			height = heightmapLength;
		for (int y = 0; y < height; y++)
			System.arraycopy(heightMap[y], 0, newMap[y], 0, width);
		heightMap = newMap;
		heightMapWidth = NewWidth;
		heightmapLength = NewHeight;
	}
	
	public void moveMap(int start, int length, int amount, boolean vertically)
	{
		float[][] t;
		if (amount <= 0 || amount >= length)
			return;
		if (vertically)
		{
			if (start >= heightMapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > heightMapWidth)
				length = heightMapWidth - start;
			if (length <= 0)
				return;
			t = new float[heightmapLength][amount];
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
			t = new float[amount][heightMapWidth];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < heightMapWidth; x++)
					t[y][x] = heightMap[start + length - amount + y][x];
			for (int y = length - amount - 1; y >= 0; y--)
				for (int x = 0; x < heightMapWidth; x++)
					heightMap[start + amount + y][x] = heightMap[start + y][x];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < heightMapWidth; x++)
					heightMap[start + y][x] = t[y][x];
		}
	}
	
	public void mirrorMap(int start, int length, int offset, boolean vertically)
	{
		if (vertically)
		{
			if (start >= heightMapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > heightMapWidth)
				length = heightMapWidth - start;
			if (start + offset >= heightMapWidth)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > heightMapWidth)
				length = heightMapWidth - start - offset;
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
				for (int x = 0; x < heightMapWidth; x++)
					heightMap[offset - y][x] = heightMap[start + y][x];
			//System.arraycopy(heightMap, start + y, heightMap, offset - y, length);
		}
	}
	
	public void flipMap(int start, int length, int offset, boolean vertically)
	{
		float t;
		if (vertically)
		{
			if (start >= heightMapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > heightMapWidth)
				length = heightMapWidth - start;
			if (start + offset >= heightMapWidth)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > heightMapWidth)
				length = heightMapWidth - start - offset;
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
				for (int x = 0; x < heightMapWidth; x++)
				{
					t = heightMap[offset - y][x];
					heightMap[offset - y][x] = heightMap[start + y][x];
					heightMap[start + y][x] = t;
				}
		}
	}
	
	public void copy(int px, int py, int height, int width)
	{
		if (py >= heightmapLength || px >= heightMapWidth)
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
		if (px + width  >= heightMapWidth)
			width = heightMapWidth - px;
		if (height <= 0 || width <= 0)
			return;
		buffer = new float[height * width];
		bufferWidth = width;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				buffer[y * width + x] = heightMap[py + y][px + x];
	}
	
	public void paste(int px, int py, Brush brush)
	{
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		float[] tempBuffer = buffer;
		int tempBufferWidth = bufferWidth;
		int height = brush.getHeight();
		int width = brush.getWidth();
		if (py >= heightmapLength || px >= heightMapWidth)
			return;
		if (px < 0)
		{
			width += px;
			tempBufferWidth += px;
			if (width <= 0 || tempBufferWidth <= 0)
				return;
			tempBuffer = new float[buffer.length / bufferWidth * tempBufferWidth];
			for (int y = 0; y < buffer.length / bufferWidth; y++)
				System.arraycopy(buffer, y * bufferWidth - px, tempBuffer, y * tempBufferWidth, tempBufferWidth);
			px = 0;
		}
		if (py < 0)
		{
			height += py;
			if (height <= 0 || tempBufferWidth <= 0 || (buffer.length / tempBufferWidth) + py <= 0)
				return;
			tempBuffer = new float[((buffer.length / tempBufferWidth) + py) * tempBufferWidth];
			System.arraycopy(buffer, -py * tempBufferWidth, tempBuffer, 0, ((buffer.length / tempBufferWidth) + py) * tempBufferWidth);
			py = 0;
		}
		if (height + py >= heightmapLength)
			height = heightmapLength - py;
		if (width + px >= heightMapWidth)
			width = heightMapWidth - px;
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
	
	public void modifyHeight(int px, int py, HeightBrush brush, boolean invert)
	{
		float amount = brush.getStrength();
		if (invert)
			amount = -amount;
		float[][] pattern = brush.getPattern().getPattern();
		float[][] newPattern = pattern;
		int height = brush.getHeight();
		int width = brush.getWidth();
		if (px >= heightMapWidth || py >= heightmapLength)
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
			py = 0;
		}
		if (height + py >= heightmapLength)
			height = heightmapLength - py;
		if (width + px >= heightMapWidth)
			width = heightMapWidth - px;
		if (width <= 0)
			return;
		for (int y = py; y < py + height; y++)
			for (int x = px; x < px + width; x++)
			{
				heightMap[y][x] += amount * newPattern[x - px][y - py];
				if (heightMap[y][x] > 1)
					heightMap[y][x] = 1;
				else if (heightMap[y][x] < 0)
					heightMap[y][x] = 0;
			}
	}
	
	public void setHeight(int px, int py, HeightBrush brush)
	{
		float amount = Math.min(1f, brush.getStrength());
		float[][] pattern = brush.getPattern().getPattern();
		float[][] newPattern = pattern;
		int height = brush.getHeight();
		int width = brush.getWidth();
		if (px >= heightMapWidth || py >= heightmapLength)
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
			py = 0;
		}
		if (height + py >= heightmapLength)
			height = heightmapLength - py;
		if (width + px >= heightMapWidth)
			width = heightMapWidth - px;
		if (width <= 0)
			return;
		for (int y = py; y < py + height; y++)
			for (int x = px; x < px + width; x++)
			{
				if (newPattern[x - px][y - py] > 0) {
					heightMap[y][x] = amount;
				}
			}
	}
	
	public void addHeightToMap(float setHeight)
	{
		for (int y = 0; y < heightmapLength; y++)
			for (int x = 0; x < heightMapWidth; x++)
			{
				heightMap[y][x] = heightMap[y][x] + setHeight;
				if (heightMap[y][x] > 1)
					heightMap[y][x] = 1;
				else if (heightMap[y][x] < 0)
					heightMap[y][x] = 0;
			}
	}
	
	public void setHeightToMap(float setHeight)
	{
		for (int y = 0; y < heightmapLength; y++)
			for (int x = 0; x < heightMapWidth; x++)
				heightMap[y][x] = setHeight;
	}
	
	public void smoothHeight(int px, int py, HeightBrush brush, float strength)
	{
		int length = brush.getHeight();
		int width = brush.getWidth();
		if (width <= 2 || length <= 2)
			return;
		float[][] smoothedMap;
		float[][] pattern = brush.getPattern().getPattern();
		float[][] newPattern;
		if (px >= heightMapWidth || py >= heightmapLength)
			return;
		if (px < 0)
		{
			width += px;
			if (width <= 2)
				return;
			newPattern = new float[width][length];
			System.arraycopy(pattern, -px, newPattern, 0, width);
			pattern = newPattern;
			px = 0;
		}
		if (py < 0)
		{
			length += py;
			if (length <= 2)
				return;
			newPattern = new float[width][length];
			for (int x = 0; x < width; x++)
				System.arraycopy(pattern[x], -py, newPattern[x], 0, length);
			pattern = newPattern;
			py = 0;
		}
		if (length + py >= heightmapLength)
			length = heightmapLength - py;
		if (width + px >= heightMapWidth)
			width = heightMapWidth - px;
		if (width <= 2 || length <= 2)
			return;
		smoothedMap = new float[length][width];
		for (int y = 0; y < length; y++)
			for (int x = 0; x < width; x++)
				smoothedMap[y][x] = smooth9HeightmapBorderAware(px + x, py + y);
		/*float yStart = heightMap[py][px + width / 2];
		float xStart = heightMap[py + length / 2][px];
		float yGradient = (heightMap[py + length - 1][px + width / 2] - yStart) / length;
		float xGradient = (heightMap[py + length / 2][px + width - 1] - xStart) / width;*/
		/*for (int x = 0; x < width - 2; x++)
		{
			float xStart = heightMap[py][px + x];
			float xGradient = (heightMap[py + length - 1][px + x] - xStart) / length;
			for (int y = 0; y < length - 2; y++)
				smoothedMap[y][x] += newSmooth(limit, px + x + 1, py + y + 1, xStart + xGradient * (x + 1));
		}*/	
		/*for (int y = 0; y < length - 2; y++)
		{
			float yStart = heightMap[py + y][px];
			float yGradient = (heightMap[py + y][px + width - 1] - yStart) / width;
			for (int x = 0; x < width - 2; x++)
				smoothedMap[y][x] += newSmooth(limit, px + x + 1, py + y + 1, yStart + yGradient * (y + 1));
		}*/
		for (int y = 0; y < length; y++)
			for (int x = 0; x < width; x++) {
				float alpha = pattern[x][y] * strength;
				heightMap[py + y][px + x] = ((1f - alpha) * heightMap[py + y][px + x]) + (alpha * smoothedMap[y][x]);
			}
	}
	
	public void setPrefabHeightMap(int px, int py, PrefabBrush brush, float brushHeightAlign, int maxHeight)
	{
		if (brush.heightmap != null)
		{
			//int level = brush.getStrengthInt() - 1;
			float amount = brush.getStrength() / 1000;
			/*if (brushHeightAlign != 0)
				amount = (level * brushHeightAlign) - (level * brush.prefab.levelOverlap * brush.prefab.heightZ);*/
			float scale = brush.prefab.heightZ;
			float[][] pattern = brush.heightmap.getPattern();
			for (int y = py; y < py + brush.getHeight() && y < brush.heightmap.getPattern().length + py; y++)
				for (int x = px; x < px + brush.getWidth() && x < brush.heightmap.getPattern()[y - py].length + px; x++)
					if ((x >= 0) && (x < heightMapWidth) && (y >= 0) && (y < heightmapLength))
					{
						heightMap[y][x] = amount + (scale * pattern[y - py][x - px]);
						if (heightMap[y][x] > 1)
							heightMap[y][x] = 1;
						else if (heightMap[y][x] < 0)
							heightMap[y][x] = 0;
					}
		}
	}
	
	public void addPrefabHeightMap(int px, int py, PrefabBrush brush, boolean invert) 
	{
		if (brush.heightmap != null)
		{
			float scale = brush.prefab.heightZ;
			float[][] pattern = brush.heightmap.getPattern();
			for (int y = py; y < py + brush.getHeight() && y < brush.heightmap.getPattern().length + py; y++)
				for (int x = px; x < px + brush.getWidth() && x < brush.heightmap.getPattern()[y - py].length + px; x++)
					if ((x >= 0) && (x < heightMapWidth) && (y >= 0) && (y < heightmapLength))
					{
						if (invert)
							heightMap[y][x] = heightMap[y][x] - (scale * pattern[y - py][x - px]);
						else
							heightMap[y][x] = heightMap[y][x] + (scale * pattern[y - py][x - px]);
						if (heightMap[y][x] > 1)
							heightMap[y][x] = 1;
						else if (heightMap[y][x] < 0)
							heightMap[y][x] = 0;
					}
		}
	}
	
	public void makeRamp(int pxs, int pys, int pxe, int pye, int w)
	{
		float start = heightMap[pxs][pys];
		float gradient = start - heightMap[pxe][pye] / pxs - pxe + pys - pye;
		float angle = 0;
		for (int y = pys; y < pye; y++)
			for (int x = pxs; x < pxe; x++)
				heightMap[y][x] = gradient;
}
	
	public void smoothMap(float strength)
	{
		long start = System.nanoTime();
		int x, y;
		float[][] smoothedMap = new float[heightmapLength][heightMapWidth];
		//Center (No Border checks required -> faster)
		for (y = 1; y < heightmapLength - 1; y++)
			for (x = 1; x < heightMapWidth - 1; x++)
				smoothedMap[y][x] = smooth9(strength, heightMap[y][x], heightMap[y - 1][x - 1], heightMap[y - 1][x], heightMap[y - 1][x + 1],
						heightMap[y][x - 1], heightMap[y][x + 1], heightMap[y + 1][x - 1], heightMap[y + 1][x], heightMap[y + 1][x + 1]);
		//Left Border
		for (y = 1; y < heightmapLength - 1; y++)
			smoothedMap[y][0] = ((1f-strength) * heightMap[y][0]) + (strength * smooth9HeightmapBorderAware(0, y));
		//Right Border
		for (y = 1; y < heightmapLength - 1; y++)
			smoothedMap[y][heightMapWidth - 1] = ((1f-strength) * heightMap[y][heightMapWidth - 1]) + (strength * smooth9HeightmapBorderAware(heightMapWidth - 1, y));
		//Upper Border
		for (x = 0; x < heightMapWidth; x++)
			smoothedMap[0][x] = ((1f - strength) * heightMap[0][x]) + (strength * smooth9HeightmapBorderAware(x, 0));
		//Lower Border
		for (x = 0; x < heightMapWidth; x++)
			smoothedMap[heightmapLength - 1][x] = ((1f - strength) * heightMap[heightmapLength - 1][x]) + (strength * smooth9HeightmapBorderAware(x, heightmapLength - 1));
		
		heightMap = smoothedMap;
		System.out.println("Done smoothing heightMap ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	private float newSmooth(float limit, int x, int y, float g)
	{
		if (Math.abs((g - heightMap[y][x]) / 4) < limit)
		{
			if ((g - heightMap[y][x]) / 4 + heightMap[y][x] < 0.0)
				return 0 - heightMap[y][x];
			if ((g - heightMap[y][x]) / 4 + heightMap[y][x] >= 1.0)
				return 1 - heightMap[y][x];
			return (g - heightMap[y][x]) / 4;
		}
		else
			return 0;
	}
	
	private float smooth9HeightmapBorderAware(int x, int y)
	{
		float value = heightMap[y][x];
		int pointsUsed = 1;
		
		if (y > 0) //Upper Border
		{
			value += heightMap[y - 1][x];
			pointsUsed++;
		}
		if (y < (heightmapLength - 1)) //Lower Border
		{
			value += heightMap[y + 1][x];
			pointsUsed++;
		}
		if (x > 0) //Left Border
		{
			value += heightMap[y][x - 1];
			pointsUsed++;
			
			if (y > 0)
			{
				value += heightMap[y - 1][x - 1];
				pointsUsed++;
			}
			if (y < (heightmapLength - 1))
			{
				value += heightMap[y + 1][x - 1];
				pointsUsed++;
			}
		}
		if (x < (heightMapWidth - 1)) //Right Border
		{
			value += heightMap[y][x + 1];
			pointsUsed++;
			
			if (y > 0)
			{
				value += heightMap[y - 1][x + 1];
				pointsUsed++;
			}
			if (y < (heightmapLength - 1))
			{
				value += heightMap[y + 1][x + 1];
				pointsUsed++;
			}
		}
		return (value / pointsUsed) /*- heightMap[y][x]*/;
	}
	
	private float smooth9(float limit, float center, float v2, float v3, float v4, float v5, float v6, float v7, float v8, float v9)
	{
		/*if (limit < Math.abs(center - v2))
			return center;
		if (limit < Math.abs(center - v3))
			return center;
		if (limit < Math.abs(center - v4))
			return center;
		if (limit < Math.abs(center - v5))
			return center;
		if (limit < Math.abs(center - v6))
			return center;
		if (limit < Math.abs(center - v7))
			return center;
		if (limit < Math.abs(center - v8))
			return center;
		if (limit < Math.abs(center - v9))
			return center;*/
		return ((1f - limit) * center) + (limit * ((center + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9) / 9));
	}
	
	public float getSmoothedSteepness(int x, int y, float fractX, float fractY)
	{
		float st1 = 0;
		float st2 = 0;
		float st3 = 0;
		float st4 = 0;
		
		float maxHeight = Math.max(Math.max(Math.max(heightMap[y][x], heightMap[y + 1][x]), heightMap[y + 1][x + 1]), heightMap[y][x + 1]);
		float minHeight = Math.min(Math.min(Math.min(heightMap[y][x], heightMap[y + 1][x]), heightMap[y + 1][x + 1]), heightMap[y][x + 1]);
		st1 = maxHeight - minHeight;

		int xMod = 0;
		int yMod = 0;
		if (y < (heightmapLength - 2))
		{
			yMod = 1;
			maxHeight = Math.max(Math.max(Math.max(heightMap[y + 0 + yMod][x + 0], heightMap[y + 1 + yMod][x + 0]),
					heightMap[y + 1 + yMod][x + 1]), heightMap[y + 0 + yMod][x + 1]);
			minHeight = Math.min(Math.min(Math.min(heightMap[y + 0 + yMod][x + 0], heightMap[y + 1 + yMod][x + 0]),
					heightMap[y + 1 + yMod][x + 1]), heightMap[y + 0 + yMod][x + 1]);
			st2 = maxHeight - minHeight;
		}
		
		if ((x < (heightMapWidth - 2)) && (y < (heightmapLength - 2)))
		{
			xMod = 1;
			yMod = 1;
			maxHeight = Math.max(Math.max(Math.max(heightMap[y + yMod][x + xMod], heightMap[y + 1 + yMod][x + xMod]),
					heightMap[y + 1 + yMod][x + 1 + xMod]), heightMap[y + yMod][x + 1 + xMod]);
			minHeight = Math.min(Math.min(Math.min(heightMap[y + yMod][x + xMod], heightMap[y + 1 + yMod][x + xMod]),
					heightMap[y + 1 + yMod][x + 1 + xMod]), heightMap[y + yMod][x + 1 + xMod]);
			st3 = maxHeight - minHeight;
		}
		
		if (x < (heightMapWidth - 2))
		{
			xMod = 1;
			yMod = 0;
			maxHeight = Math.max(Math.max(Math.max(heightMap[y + 0 + yMod][x + 0 + xMod], heightMap[y + 1 + yMod][x + 0 + xMod]),
					heightMap[y + 1 + yMod][x + 1 + xMod]), heightMap[y + 0 + yMod][x + 1 + xMod]);
			minHeight = Math.min(Math.min(Math.min(heightMap[y + 0 + yMod][x + 0 + xMod], heightMap[y + 1 + yMod][x + 0 + xMod]),
					heightMap[y + 1 + yMod][x + 1 + xMod]), heightMap[y + 0 + yMod][x + 1 + xMod]);
			st4 = maxHeight - minHeight;
		}
		
		float steepYLeft =  ((1 - fractY) * st1) + (fractY * st2);
		float steepYRight = ((1 - fractY) * st4) + (fractY * st3);
		
		return ((1 - fractX) * steepYLeft) + (fractX * steepYRight);
	}
	
	public void erodeMapWet(int px, int py, int width, int height, ErosionSetup setup)
	{
		if (setup == null)
			return;
		if (setup.useAlternativeWetMethod)
		{
			erodeMapDryWet(px, py, width, height, true, setup);
			return;
		}
		
		long start = System.nanoTime();
		
		int iterations = setup.wetIterations;
		float dropletHeight = setup.wetDropletHeight;
		float evaporateAmount = setup.wetEvaporateAmount;
		
		final int oneTenthsOfHeight = Math.max(iterations / 10, 1);
		float[][] waterMap = new float[heightmapLength][heightMapWidth];
		float dropletSolveFactor = 0.05f;
		float tmpDouble, tmpDouble2;
		boolean foundMoveLocation;
		int xMod, yMod;
		
		int bWidth = width;
		int bHeight = height;
		if (px < 0)
		{
			bWidth = bWidth + px;
			px = 0;
		}
		if (py < 0)
		{
			bHeight = bHeight + py;
			py = 0;
		}
		if (bWidth + px >= heightMapWidth)
			bWidth = heightMapWidth - px;
		if (bHeight + py >= heightmapLength)
			bHeight = heightmapLength - py;
			
		for (int i = 0; i < iterations; i++)
		{
			//1. Distribute Water and solve some terrain
			for (int y = py; y < py + bHeight; y++)
				for (int x = px; x < px + bWidth; x++)
					if ((x >= 0) && (x < heightMapWidth) && (y >= 0) && (y < heightmapLength) && (heightMap[y][x] > dropletHeight * dropletSolveFactor))
					{
						waterMap[y][x] += dropletHeight;
						heightMap[y][x] -= dropletHeight * dropletSolveFactor;
					}
			
			//2. Move Water
			for (int y = py; y < py + bHeight; y++)
				for (int x = px; x < px + bWidth; x++)
					if ((x >= 0) && (x < heightMapWidth) && (y >= 0) && (y < heightmapLength))
					{
						xMod = 0;
						yMod = 0;
						foundMoveLocation = false;
						tmpDouble =  heightMap[y][x] + waterMap[y][x]; //Waterheight on center
						
						if ((x - 1 >= px) && (y - 1 >= py))
						{
							tmpDouble2 = heightMap[y - 1][x - 1] + waterMap[y - 1][x - 1];
							if (tmpDouble > tmpDouble2)
							{ 
								xMod = -1; 
								yMod = -1; 
								tmpDouble = tmpDouble2; 
								foundMoveLocation = true;
							}
						}
						
						if ((x + 1 < px + bWidth) && (y - 1 >= py))
						{
							tmpDouble2 = heightMap[y - 1][x + 1] + waterMap[y - 1][x + 1];
							if (tmpDouble > tmpDouble2)
							{
								xMod = 1;
								yMod = -1;
								tmpDouble = tmpDouble2;
								foundMoveLocation = true;
							}
						}
						
						if ((x + 1 < px + bWidth) && (y + 1 < py + bHeight))
						{
							tmpDouble2 = heightMap[y + 1][x + 1] + waterMap[y + 1][x + 1];
							if (tmpDouble > tmpDouble2)
							{
								xMod = 1;
								yMod = 1;
								tmpDouble = tmpDouble2;
								foundMoveLocation = true;
							}
						}
						
						if ((x - 1 >= px) && (y + 1 < py+bHeight))
						{
							tmpDouble2 = heightMap[y + 1][x - 1] + waterMap[y + 1][x - 1];
							if (tmpDouble > tmpDouble2)
							{
								xMod = -1;
								yMod = 1;
								tmpDouble = tmpDouble2;
								foundMoveLocation = true;
							}
						}
						
						//Even out Waterlevels between two locations
						if (foundMoveLocation)
						{
							if (heightMap[y + yMod][x + xMod] < heightMap[y][x])
							{
								//Height difference
								tmpDouble = heightMap[y][x] - heightMap[y + yMod][x + xMod];
								//Available Water
								tmpDouble2 = waterMap[y + yMod][x + xMod] + waterMap[y][x];
								if (tmpDouble > tmpDouble2)
								{
									//All water fits in new location
									waterMap[y + yMod][x + xMod] = tmpDouble2;
									waterMap[y][x] = 0;
								}
								else
								{
									//Distribute evenly
									waterMap[y + yMod][x + xMod] = tmpDouble;
									tmpDouble2 = tmpDouble2 - tmpDouble;
									waterMap[y + yMod][x + xMod] = waterMap[y + yMod][x + xMod] + (tmpDouble2 / 2);
									waterMap[y][x] = (tmpDouble2 / 2);
								}
							}
							else
							{
								//Height difference
								tmpDouble = heightMap[y + yMod][x + xMod] - heightMap[y][x];
								//Available Water
								tmpDouble2 = waterMap[y + yMod][x + xMod] + waterMap[y][x];
								if (tmpDouble > tmpDouble2)
								{
									//All water fits in old location (should never happen)
									waterMap[y + yMod][x + xMod] = 0;
									waterMap[y][x] = tmpDouble2;
								}
								else
								{
									//Distribute evenly
									waterMap[y][x] = tmpDouble;
									tmpDouble2 -= tmpDouble;
									waterMap[y][x] += tmpDouble2 / 2;
									waterMap[y + yMod][x + xMod] = tmpDouble2 / 2;
								}
							}
						}
					}
			
			//3. Evaporate some Water
			for (int y = py - 1; y < py + bHeight + 1; y++)
				for (int x = px - 1; x < px + bWidth + 1; x++)
					if ((x >= 0) && (x < heightMapWidth) && (y >= 0) && (y < heightmapLength))
					{
						tmpDouble = Math.min(waterMap[y][x], evaporateAmount);
						waterMap[y][x] -= tmpDouble;
						heightMap[y][x] += tmpDouble * dropletSolveFactor;
					}
			
			//Status output
			if ((i % oneTenthsOfHeight) == 0)
				System.out.print("#");
		}
		//4. Cleanup: Evaporate all water
		for (int y = py - 1; y < py + bHeight + 1; y++)
			for (int x = px - 1; x < px + bWidth + 1; x++)
				if ((x >= 0) && (x < heightMapWidth) && (y >= 0) && (y < heightmapLength))
					heightMap[y][x] += waterMap[y][x] * dropletSolveFactor;
		System.out.println(" Done eroding heightmap ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	public void erodeMapDryWet(int px, int py, int width, int height, boolean hydroErosion, ErosionSetup setup)
	{
		if (setup == null)
			return;
		
		long start = System.nanoTime();
		
		int iterations = setup.dryIterations;
	    float breakHeight = setup.dryBreakHeight;
	    if (hydroErosion)
	    {
	    	iterations = setup.wet2Iterations;
		    breakHeight = setup.wet2BreakHeight;
	    }
	    
		final int oneTenthsOfHeight = Math.max(iterations / 10, 1);
		
		float tmpDouble;
		int xMod, yMod;
		
		int bWidth = width;
		int bHeight = height;
		if (px < 0)
		{
			bWidth = bWidth + px;
			px = 0;
		}
		if (py < 0)
		{
			bHeight = bHeight + py;
			py = 0;
		}
		if (bWidth + px >= heightMapWidth)
			bWidth = heightMapWidth - px;
		if (bHeight + py >= heightmapLength)
			bHeight = heightmapLength - py;
		for (int i = 0; i < iterations; i++)
		{
			//1. Move soil
			for (int y = py; y < py + bHeight; y++)
				for (int x = px; x < px + bWidth; x++)
					if ((x >= 0) && (x < heightMapWidth) && (y >= 0) && (y < heightmapLength))
					{
						xMod = 0;
						yMod = 0;
						tmpDouble = 0; //max height distance so far
						
						if ((x - 1 >= px) && (y - 1 >= py) && (heightMap[y][x] - heightMap[y - 1][x - 1]) > tmpDouble)
						{
							tmpDouble = heightMap[y][x] - heightMap[y - 1][x - 1];
							xMod = -1; 
							yMod = -1;
						}
						if ((x + 1 < px + bWidth) && (y - 1 >= py && (heightMap[y][x] - heightMap[y - 1][x + 1]) > tmpDouble))
						{
							tmpDouble = heightMap[y][x] - heightMap[y - 1][x + 1];
							xMod = 1; 
							yMod = -1;
						}
						if ((x + 1 < px + bWidth) && (y + 1 < py + bHeight) && (heightMap[y][x] - heightMap[y + 1][x + 1]) > tmpDouble)
						{
							tmpDouble = heightMap[y][x] - heightMap[y + 1][x + 1];
							xMod = 1; 
							yMod = 1;
						}	
						if ((x - 1 >= px) && (y + 1 < py + bHeight) && (heightMap[y][x] - heightMap[y + 1][x - 1]) > tmpDouble)
						{
							tmpDouble = heightMap[y][x] - heightMap[y + 1][x - 1];
							xMod = -1; 
							yMod = 1;
						}
						
						//Even out heightlevels between two locations
						if (hydroErosion)
						{
							if ((tmpDouble > 0) && (tmpDouble < breakHeight))
							{
								tmpDouble = heightMap[y + yMod][x + xMod] + heightMap[y][x];
								heightMap[y + yMod][x + xMod] = tmpDouble / 2;
								heightMap[y][x] = tmpDouble / 2;
							}
						}
						else
						{
							if ((tmpDouble > 0) && (tmpDouble > breakHeight))
							{
								tmpDouble = heightMap[y + yMod][x + xMod] + heightMap[y][x];
								heightMap[y + yMod][x + xMod] = tmpDouble / 2;
								heightMap[y][x] = tmpDouble / 2;
							}
						}
					}
			//Status output
			if ((i % oneTenthsOfHeight) == 0)
				System.out.print("#");
		}
		System.out.println(" Done eroding heightmap ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	public void ttdize(int stepCount)
	{
		long start = System.nanoTime();
		int x, y;
		float steps = stepCount;
		int[][] steppedMap = new int[heightmapLength][heightMapWidth];
		//Round heights to steps
		for (y = 0; y < heightmapLength; y++)
			for (x = 0; x < heightMapWidth; x++)
				steppedMap[y][x] = FastMath.round(heightMap[y][x] * steps);
		
		//enforce max steepness
		for (y = 1; y < heightmapLength; y++)
			for (x = 1; x < heightMapWidth; x++)
			{
				//Clamp to upper
				if (steppedMap[y][x] > steppedMap[y - 1][x])
					steppedMap[y][x] = steppedMap[y - 1][x] + 1;
				else if (steppedMap[y][x] < steppedMap[y - 1][x])
					steppedMap[y][x] = steppedMap[y - 1][x] - 1;
				
				//Clamp to left
				if (steppedMap[y][x] > steppedMap[y][x - 1])
					steppedMap[y][x] = steppedMap[y][x - 1] + 1;
				else if (steppedMap[y][x] < steppedMap[y][x - 1])
					steppedMap[y][x] = steppedMap[y][x - 1] - 1;
			}
		//copy back
		for (y = 0; y < heightmapLength; y++)
			for (x = 0; x < heightMapWidth; x++)
				heightMap[y][x] = steppedMap[y][x] / steps;
		System.out.println("Done ttdizing heightMap ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	public void randomizeHeight(int px, int py, HeightBrush brush, float amount)
	{
		Random r = new Random();
		float amountHalf = amount / 2; //brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		float[][] newPattern;
		int height = brush.getHeight();
		int width = brush.getWidth();
		if (px >= heightMapWidth || py >= heightmapLength)
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
		if (width + px >= heightMapWidth)
			width = heightMapWidth - px;
		if (width <= 0)
			return;
		for (int y = py; y < py + height; y++)
			for (int x = px; x < px + width; x++)
			{
				heightMap[y][x] = heightMap[y][x] + ((r.nextFloat() * amount) - amountHalf) * pattern[x - px][y - py];
				if (heightMap[y][x] > 1)
					heightMap[y][x] = 1;
				else if (heightMap[y][x] < 0)
					heightMap[y][x] = 0;
			}
	}
	
	public void genStartupHeightmap(TerraGenSetup setup)
	{
		if (setup != null)
			genDiamondSquareRandom(heightMap, heightMapWidth, heightmapLength, setup.maxDisplacement, setup.displacementRegression, setup.skipSteps, setup.randomSeed);
	}
	
	public void genRandom(float strength)
	{
		Random r = new Random();
		for (int y = 0; y < heightmapLength; y++)
			for (int x = 0; x < heightMapWidth; x++)
			{
				heightMap[y][x] = r.nextFloat();
				if ((x > 20) && (x < 40) && (y > 20) && (y < 40))
					heightMap[y][x] = 1; //TODO WTF!
			}
	}
	
	public void genDiamondSquareRandom(float[][] map, int width, int height,
			float maxDisplacement, float displacementRegression, int skipSteps, int randomSeed)
	{
		/*
		 * 1. Calculate next larger power of 2 square, which can contain given map.
		 * 2. Allocate new array with this size, copy existing into it, init everything outside via mirror copy
		 * 3. Generate via diamond-square
		 * 4. Copy back
		 */
		
		//Get next larger power of 2 squaresize
		int squareSize = Math.max(width - 1, height - 1);
		squareSize = FastMath.pow(2, (int)Math.ceil(Math.log(squareSize) / Math.log(2))); 
		final int size = squareSize;
		final int arraySize = squareSize + 1;
		
		//Allocate temporary array
		float[][] tempMap = new float[arraySize][arraySize];
		
		//Copy existing map to new map
		for (int y = 0; y < height; y++)
			System.arraycopy(map[y], 0, tempMap[y], 0, width);

		//Mirror-Copy outside area. (This is better than initializing to some fixed value)
		if (width < arraySize)
			for (int x = width; x < arraySize; x++)
				for (int y = 0; y < arraySize; y++)
					if (((x / (width - 1)) % 2) == 0)
						tempMap[y][x] = tempMap[y][x % (width - 1)];
					else
						tempMap[y][x] = tempMap[y][width - (x % (width - 1))];
		if (height < arraySize)
			for (int y = height; y < arraySize; y++)
				for (int x = 0; x < arraySize; x++)
					if (((y / (height - 1)) % 2) == 0)
						tempMap[y][x] = tempMap[y % (height - 1)][x];
					else
						tempMap[y][x] = tempMap[(height - (y % (height - 1)))][x];
		
		//Do Diamond Square
		Random r = new Random();
		if (randomSeed >= 0)
			r = new Random(randomSeed);
		int[] xPos = new int[4];
		int[] yPos = new int[4];
		int halfSize;
		float maxDisplacementHalf = maxDisplacement / 2;
		while (squareSize > 1)
		{
			halfSize = squareSize / 2;
			if (skipSteps <= 0)
				for (int y = 0; y < size; y += squareSize)
				{
					yPos[0] = (y - halfSize);
					if (yPos[0] < 0)
						yPos[0] = size + yPos[0];
					yPos[1] = y + halfSize;
					yPos[2] = y + squareSize;
					yPos[3] = y + squareSize + halfSize;
					if (yPos[3] > size)
						yPos[3] = yPos[3] % size;
					for (int x = 0; x < size; x += squareSize)
					{
						xPos[0] = (x - halfSize);
						if (xPos[0] < 0)
							xPos[0] = size + xPos[0];
						xPos[1] = x + halfSize;
						xPos[2] = x + squareSize;
						xPos[3] = x + squareSize + halfSize;
						if (xPos[3] > size)
							xPos[3] = xPos[3] % size;

						//Square Step
						//Center
						tempMap[yPos[1]][xPos[1]] = Math.min(1, Math.max(0,
								((tempMap[y][x] + tempMap[yPos[2]][x] + tempMap[y][xPos[2]] + tempMap[yPos[2]][xPos[2]]) / 4) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						
						//Diamond(s) Step
						//Left
						//tempMap[yPos[1]][x] = Math.min(1, Math.max(0, ((tempMap[y][x] + tempMap[yPos[2]][x]) / 2)));
						tempMap[yPos[1]][x] = Math.min(1, Math.max(0,
								((tempMap[y][x] + tempMap[yPos[2]][x]) / 2) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[yPos[1]][x] = Math.min(1, Math.max(0, ((tempMap[y][x] + tempMap[yPos[2]][x] + tempMap[yPos[1]][xPos[1]]) / 3) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[yPos[1]][x] = Math.min(1, Math.max(0, ((tempMap[y][x] + tempMap[yPos[2]][x] + tempMap[yPos[1]][xPos[1]] + tempMap[yPos[1]][xPos[0]]) / 4) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						
						//Upper
						//tempMap[y][xPos[1]] = Math.min(1, Math.max(0, ((tempMap[y][x] + tempMap[y][xPos[2]]) / 2)));
						tempMap[y][xPos[1]] = Math.min(1, Math.max(0,
								((tempMap[y][x] + tempMap[y][xPos[2]]) / 2) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[y][xPos[1]] = Math.min(1, Math.max(0, ((tempMap[y][x] + tempMap[yPos[1]][xPos[1]] + tempMap[y][xPos[2]]) / 3) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[y][xPos[1]] = Math.min(1, Math.max(0, ((tempMap[y][x] + tempMap[yPos[0]][xPos[1]] + tempMap[yPos[1]][xPos[1]] + tempMap[y][xPos[2]]) / 4) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						
						//Lower
						//tempMap[yPos[2]][xPos[1]] = Math.min(1, Math.max(0, ((tempMap[yPos[2]][x] + tempMap[yPos[2]][xPos[2]]) / 2)));
						tempMap[yPos[2]][xPos[1]] = Math.min(1, Math.max(0,
								((tempMap[yPos[2]][x] + tempMap[yPos[2]][xPos[2]]) / 2) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[yPos[2]][xPos[1]] = Math.min(1, Math.max(0, ((tempMap[yPos[2]][x] + tempMap[yPos[1]][xPos[1]] + tempMap[yPos[2]][xPos[2]]) / 3) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[yPos[2]][xPos[1]] = Math.min(1, Math.max(0, ((tempMap[yPos[2]][x] + tempMap[yPos[1]][xPos[1]] + tempMap[yPos[2]][xPos[2]] + tempMap[yPos[3]][xPos[1]]) / 4) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						
						//Right
						//tempMap[yPos[1]][xPos[2]] = Math.min(1, Math.max(0, ((tempMap[y][xPos[2]] + tempMap[yPos[2]][xPos[2]]) / 2)));
						tempMap[yPos[1]][xPos[2]] = Math.min(1, Math.max(0,
								((tempMap[y][xPos[2]] + tempMap[yPos[2]][xPos[2]]) / 2) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[yPos[1]][xPos[2]] = Math.min(1, Math.max(0, ((tempMap[y][xPos[2]] + tempMap[yPos[2]][xPos[2]] + tempMap[yPos[1]][xPos[1]]) / 3) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
						//tempMap[yPos[1]][xPos[2]] = Math.min(1, Math.max(0, ((tempMap[y][xPos[2]] + tempMap[yPos[2]][xPos[2]] + tempMap[yPos[1]][xPos[1]] + tempMap[yPos[1]][xPos[3]]) / 4) + ((r.nextFloat() * maxDisplacement) - maxDisplacementHalf)));
					}
				}
			else
				skipSteps--;
			
			squareSize = halfSize;
			maxDisplacement = maxDisplacement * displacementRegression;
			maxDisplacementHalf = maxDisplacement / 2;
		}
		
		for (int y = 0; y < height; y++)
			System.arraycopy(tempMap[y], 0, map[y], 0, width);
	}
}
