package backend.map;

import java.io.File;

import backend.FastMath;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;
import frontend.render.brushes.MetalBrush;

public class Metalmap extends AbstractMap {
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual heightmap-size. (do not forget to add 1 pixel to heightmap)
	 */
	public static final int springMapsizeHeightmapFactor = 64;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual metalmap-size.
	 */
	public static final int springMapsizeMetalmapFactor = springMapsizeHeightmapFactor / 2;
	
	/**
	 * This is the factor we need to divide the mapsize by,<BR>
	 * to get the actual metalmap-size.
	 */
	public static final int heightmapSizeMetalmapDivisor = springMapsizeHeightmapFactor / springMapsizeMetalmapFactor;
	
	private final int mapSizeFactor = springMapsizeMetalmapFactor; // Ratio to which a map size corresponds to internal size

	public Metalmap(int length, int width)
	{
		super(length, width);
	}
	
	public void saveMetalMap(File metalmapFile, boolean use24Bit)
	{
		new Bitmap(use24Bit ? FileFormat.Bitmap24Bit : FileFormat.Bitmap8Bit).saveDataFromMetalmap(metalmapFile, map, mapWidth, mapLength);
	}
	
	public void loadDataIntoMap(File metalmapFile)
	{
		try
		{
			Bitmap bitmap = new Bitmap(metalmapFile);
			if (bitmap.width == mapWidth && bitmap.height == mapLength)
				bitmap.loadDataIntoMetalmap(map);
			else
				throw new IllegalArgumentException("Metalmap Size must be (heightmapsize - 1) / 2");
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	
	public void addToMetalmap(int px, int py, MetalBrush brush, boolean invert)
	{
		float amount = brush.getStrength();
		if (invert)
			amount = -amount;
		float[][] pattern = brush.getPattern().getPattern();
		px = (px / heightmapSizeMetalmapDivisor);
		py = (py / heightmapSizeMetalmapDivisor);
		int val;
		for (int y = py; y < py + (brush.height / heightmapSizeMetalmapDivisor); y++)
			for (int x = px; x < px + (brush.width / heightmapSizeMetalmapDivisor); x++)
				if ((x >= 0) && (x < mapWidth) && (y >= 0) && (y < mapLength))
				{
					val = FastMath.round((map[y][x] & 0xFF) + (amount * pattern[((x - px) * heightmapSizeMetalmapDivisor)][((y - py) * heightmapSizeMetalmapDivisor)]));
					map[y][x] = (byte)Math.min(Math.max(val, 0), 255);
				}
		}
	
	public void setToMetalmap(int px, int py, MetalBrush brush)
	{
		float amount = brush.getStrength();
		px = (px / heightmapSizeMetalmapDivisor);
		py = (py / heightmapSizeMetalmapDivisor);
		for (int y = py; y < py + (brush.height / heightmapSizeMetalmapDivisor); y++)
			for (int x = px; x < px + (brush.width / heightmapSizeMetalmapDivisor); x++)
				if ((x >= 0) && (x < mapWidth) && (y >= 0) && (y < mapLength))
					map[y][x] = (byte)Math.min(Math.max(amount * 255, 0), 255);
		}
}
