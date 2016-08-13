package backend.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import backend.math.Point;
import backend.sm2.SM2File;
import frontend.render.MapRenderer;
import frontend.render.brushes.Brush;
import frontend.render.brushes.FeatureBrush;
import frontend.render.features.FeatureManager;
import frontend.render.features.FeatureMapContainer;

public class Featuremap {
	public static int tileSize = 2;
	
	public FeatureManager featureManager;
	public final int featureBlockSizeInTiles = 32;
	public int featureMapWidthInBlocks;
	public int featureMapHeightInBlocks;
	public int featureBlockCount;
	public ArrayList<FeatureMapContainer>[] featureList;
	
	ArrayList<FeatureMapContainer> buffer;
	
	public Featuremap (int height, int width)
	{
		featureMapWidthInBlocks = width * Map.springMapSizeHeightmapFactor / featureBlockSizeInTiles;
		featureMapHeightInBlocks = height * Map.springMapSizeHeightmapFactor / featureBlockSizeInTiles;
		featureBlockCount = featureMapWidthInBlocks * featureMapHeightInBlocks;
		featureList = (ArrayList<FeatureMapContainer>[])new ArrayList[featureBlockCount];
		for (int i = 0; i < featureBlockCount; i++)
			this.featureList[i] = new ArrayList<FeatureMapContainer>();
	}
	
	public void saveFeatureMap(File featuremapFile, FeatureManager featureManager)
	{
		new SM2File(null, null).saveSMFFeaturesToFile(featuremapFile, featureManager, this);
	}
	
	public void loadDataIntoFeaturemap(File featuremapFile, FeatureManager featureManager)
	{
		blankFeatureMap();
		new SM2File(null, null).loadSMFFeaturesFromFile(featuremapFile, featureManager, this);
	}
	
	public void resizeMap(int NewWidth, int NewHeight)
	{
		int newFeatureMapWidthInBlocks = NewWidth / featureBlockSizeInTiles;
		int newFeatureMapHeightInBlocks = NewHeight / featureBlockSizeInTiles;
		int newFeatureBlockCount = newFeatureMapWidthInBlocks * newFeatureMapHeightInBlocks;
		int x = newFeatureBlockCount;
		if (x > featureBlockCount)
			x = featureBlockCount;
		ArrayList<FeatureMapContainer>[] newFeatureList = (ArrayList<FeatureMapContainer>[])new ArrayList[newFeatureBlockCount];
		for (int i = 0; i < newFeatureBlockCount; i++)
			newFeatureList[i] = new ArrayList<FeatureMapContainer>();
		for (int i = 0; i < x; i++)
			for (int j = 0; j < featureList[i].size(); j++)
			{
				FeatureMapContainer f = featureList[i].get(j);
				if (f.x < newFeatureMapWidthInBlocks * featureBlockSizeInTiles && f.z < newFeatureMapHeightInBlocks * featureBlockSizeInTiles)
					;//newFeatureList[i].add(f);
			}
		featureMapWidthInBlocks = newFeatureMapWidthInBlocks;
		featureMapHeightInBlocks = newFeatureMapHeightInBlocks;
		featureBlockCount = newFeatureBlockCount;
		featureList = newFeatureList;
	}
	
	public void switchMapAxis()
	{
		if (featureList.length > 0) {
			ArrayList<FeatureMapContainer> temp = new ArrayList<FeatureMapContainer>();
			for (int i = 0; i < featureBlockCount; i++) {
				temp = featureList[i];
				featureList[i] = featureList[i % featureMapWidthInBlocks * featureMapWidthInBlocks + i / featureMapWidthInBlocks];
				featureList[i % featureMapWidthInBlocks * featureMapWidthInBlocks + i / featureMapWidthInBlocks] = temp;
				for (int j = 0; j < featureList.length; j++) {
					FeatureMapContainer f = featureList[i].remove(j);
					float tv = f.x;
					f.x = f.z;
					f.z = tv;
					featureList[i].add(j, f);
				}
			}
		}
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
		FeatureMapContainer f;
		ArrayList<FeatureMapContainer> temp = new ArrayList<FeatureMapContainer>();
		if (amount <= 0 || amount >= length)
			return;
		if (vertically)
		{
			if (start >= featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
				length = featureMapWidthInBlocks * featureBlockSizeInTiles + 1 - start;
			if (length <= 0)
				return;
			start *= tileSize;
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.x >= start && f.x < start + length * tileSize)
					{
						featureList[i].remove(j--);
						f.x += amount * tileSize;
						if (f.x >= start + length * tileSize)
							f.x -= length * tileSize;
						temp.add(f);
					}
				}
			while (temp.size() != 0)
			{
				f = temp.remove(0);
				Point p = getFeatureBlockByCoords(f.x, f.z);
				featureList[p.x + p.y * featureMapWidthInBlocks].add(f);
			}
		}
		else
		{
			if (start >= featureMapHeightInBlocks * featureBlockSizeInTiles + 1)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > featureMapHeightInBlocks * featureBlockSizeInTiles + 1)
				length = featureMapHeightInBlocks * featureBlockSizeInTiles + 1 - start;
			if (length <= 0)
				return;
			start *= tileSize;
			length *= tileSize;
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.z >= start && f.z < start + length)
					{
						featureList[i].remove(j--);
						f.z += amount * tileSize;
						if (f.z >= start + length)
							f.z -= length;
						temp.add(f);
					}
				}
			while (temp.size() != 0)
			{
				f = temp.remove(0);
				Point p = getFeatureBlockByCoords(f.x, f.z);
				featureList[p.x + p.y * featureMapWidthInBlocks].add(f);
			}
		}
	}
	
	public void mirrorMap(int start, int length, int offset, boolean vertically)
	{
		FeatureMapContainer f;
		ArrayList<FeatureMapContainer> temp = new ArrayList<FeatureMapContainer>();
		int size;
		if (vertically)
		{
			if (start >= featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
				return;

			start *= tileSize;
			length *= tileSize;
			offset *= tileSize;
			size = (featureMapHeightInBlocks * featureBlockSizeInTiles + 1) * tileSize;
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.x >= start && f.x < start + length)
						temp.add(f);
				}
			if (length <= size)
			{
				for (int i = 0; i < size / length; i++)
					removeAllFeaturesInRange(start + offset + length / 2, length * i + length / 2, length / 2);
				removeAllFeaturesInRange(start + offset + length / 2, size - length / 2, length / 2);
			}
			else
			{
				for (int i = 0; i < length / size; i++)
					removeAllFeaturesInRange(start + offset + i * size + size / 2, size / 2, size / 2);
				removeAllFeaturesInRange(start + length + offset - size / 2, size / 2, size / 2);
			}
			while (temp.size() != 0)
			{
				f = temp.remove(0);
				if (f.x >= start + offset && f.x < start + offset + length) // Re-add features that were removed in overlapping area
					addFeature (f.x, f.z, f.rotY, f.featureID);
				addFeature (length - f.x + offset, f.z, f.rotY, f.featureID);
			}
		}
		else
		{
			if (start >= featureMapHeightInBlocks * featureBlockSizeInTiles + 1)
				return;
			
			start *= tileSize;
			length *= tileSize;
			offset *= tileSize;
			size = (featureMapWidthInBlocks * featureBlockSizeInTiles + 1) * tileSize;
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.z >= start && f.z < start + length)
						temp.add(f);
				}
			if (length <= size)
			{
				for (int i = 0; i < size / length; i++)
					removeAllFeaturesInRange(length * i + length / 2, start + offset + length / 2, length / 2);
				removeAllFeaturesInRange(size - length / 2, start + offset + length / 2, length / 2);
			}
			else
			{
				for (int i = 0; i < length / size; i++)
					removeAllFeaturesInRange(size / 2, start + offset + i * size + size / 2, size / 2);
				removeAllFeaturesInRange(size / 2, start + length + offset - size / 2, size / 2);
			}
			while (temp.size() != 0)
			{
				f = temp.remove(0);
				if (f.z >= start + offset && f.z < start + offset + length)
					addFeature (f.x, f.z, f.rotY, f.featureID);
				addFeature (f.x, length - f.z + offset, f.rotY, f.featureID);
			}
		}

	}
	
	public void flipMap(int start, int length, int offset, boolean vertically)
	{
		FeatureMapContainer f;
		ArrayList<FeatureMapContainer> temp = new ArrayList<FeatureMapContainer>();
		ArrayList<FeatureMapContainer> temp2 = new ArrayList<FeatureMapContainer>();
		if (vertically)
		{
			if (start >= featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
				return;
			/*if (start + length >= featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
				return;*/
			start *= tileSize;
			length *= tileSize;
			offset *= tileSize;
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.x >= start && f.x < start + length)
						temp.add(featureList[i].remove(j--));
				}
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.x >= start + offset && f.x < start + length + offset)
						temp2.add(featureList[i].remove(j--));
				}
			while (temp.size() != 0)
			{
				f = temp.remove(0);
				f.x = length - f.x + offset;
				addFeature(f.x, f.z, f.rotY, f.featureID);
			}
			while (temp2.size() != 0)
			{
				f = temp2.remove(0);
				f.x = start + length - (f.x - start - offset);
				addFeature(f.x, f.z, f.rotY, f.featureID);
			}
		}
		else
		{
			if (start >= featureMapHeightInBlocks * featureBlockSizeInTiles + 1)
				return;
			/*if (start + length >= featureMapHeightInBlocks * featureBlockSizeInTiles + 1)
				return;*/
			start *= tileSize;
			length *= tileSize;
			offset *= tileSize;
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.z >= start && f.z < start + length)
						temp.add(featureList[i].remove(j--));
				}
			for (int i = 0; i < featureBlockCount; i++)
				for (int j = 0; j < featureList[i].size(); j++)
				{
					f = featureList[i].get(j);
					if (f.z >= start + offset && f.z < start + length + offset)
						temp2.add(featureList[i].remove(j--));
				}
			while (temp.size() != 0)
			{
				f = temp.remove(0);
				f.z = length - f.z + offset;
				addFeature(f.x, f.z, f.rotY, f.featureID);
			}
			while (temp2.size() != 0)
			{
				f = temp2.remove(0);
				f.z = start + length - (f.z - start - offset);
				addFeature(f.x, f.z, f.rotY, f.featureID);
			}
		}
	}
	
	public void copy(int px, int py, int height, int width)
	{
		FeatureMapContainer f;
		if (py >= featureMapHeightInBlocks * featureBlockSizeInTiles + 1 || px >= featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
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
		if (py + height >= featureMapHeightInBlocks * featureBlockSizeInTiles )
			height = featureMapHeightInBlocks * featureBlockSizeInTiles  - py;
		if (px + width  >= featureMapWidthInBlocks * featureBlockSizeInTiles)
			width = featureMapWidthInBlocks * featureBlockSizeInTiles - px;
		if (height <= 0 || width <= 0)
			return;
		buffer = new ArrayList<FeatureMapContainer>();
		px *= tileSize;
		py *= tileSize;
		for (int i = 0; i < featureBlockCount; i++)
			for (int j = 0; j < featureList[i].size(); j++)
			{
				f = featureList[i].get(j);
				if (f.x >= px && f.x < px + width * tileSize && f.z >= py && f.z < py + height * tileSize)
					buffer.add(f);
			}
	}
	
	public void paste(int px, int py, Brush brush)
	{
		float[][] pattern = brush.getPattern().getPattern();
		int height = brush.getHeight();
		int width = brush.getWidth();
		/*if (py >= featureMapHeightInBlocks * featureBlockSizeInTiles + 1 || px >= featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
			return;
		if (px < 0)
		{
			width += px;
			if (width <= 0)
				return;
			px = 0;
		}
		if (py < 0)
		{
			height += py;
			if (height <= 0)
				return;
			py = 0;
		}
		if (height + py >= featureMapHeightInBlocks * featureBlockSizeInTiles + 1)
			height = featureMapHeightInBlocks * featureBlockSizeInTiles + 1 - py;
		if (width + px >= featureMapWidthInBlocks * featureBlockSizeInTiles + 1)
			width = featureMapWidthInBlocks * featureBlockSizeInTiles + 1 - px;
		if (width <= 0 || height <= 0)
			return;*/
		removeAllFeaturesInRange((px + (width / 2)) * tileSize, (py + (height / 2)) * tileSize, Math.min(width, height) / 2 * tileSize);
		//removeFromFeaturemap(px, py, new FeatureBrush(brush.brushPatternManager, 0, brush.width, brush.height, brush.sme));
		for (int i = 0; i < buffer.size(); i++)
		{
			/*FeatureMapContainer f = buffer.remove(i);
			float x = f.x;
			float z = f.z;
			if (x / tileSize < featureMapWidthInBlocks * featureBlockSizeInTiles + 1 && z / tileSize < featureMapHeightInBlocks * featureBlockSizeInTiles + 1)
			{
				addFeature(px + x, py + z, f.rotY, f.featureID);
			}*/
		}
	}
	
	public void addToFeaturemap(int px, int py, FeatureBrush brush)
	{
		int type = brush.featureID;
		int height = brush.getHeight();
		int width = brush.getWidth();
		int x = px + (width / 2);
		int y = py + (height / 2);
		if (x < 0 || (x >= (featureMapWidthInBlocks * featureBlockSizeInTiles) + 1) || y < 0 || (y >= (featureMapHeightInBlocks * featureBlockSizeInTiles) + 1))
			return;
		/*for (int i = 0; i < brush.getStrengthInt() / 10; i++)
		{*/
				addFeature(x * tileSize, y * tileSize, 0, type);
			/*x = px + (int)Math.round((Math.random() + 0.00000000001) * brush.width);
			y = py + (int)Math.round((Math.random() + 0.00000000001) * brush.height);
		}*/
	}
	
	/**
	 * Deletes all features
	 * @return
	 */
	public void blankFeatureMap()
	{
		long start = System.nanoTime();
		for (int i = 0; i < featureBlockCount; i++)
			featureList[i].clear();
		System.out.println("Done blanking featureMap ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	/**
	 * Deletes all features from px, py position by brush length and width
	 * @return
	 */
	public void removeFromFeaturemap(int px, int py, FeatureBrush brush)
	{
		if (px < 0 || (px >= (featureMapWidthInBlocks * featureBlockSizeInTiles) + 1) || py < 0 || (py >= (featureMapHeightInBlocks * featureBlockSizeInTiles) + 1))
			return;
		int x = px + (brush.width / 2);
		int y = py + (brush.height / 2);
		if (x < 0 || (x >= (featureMapWidthInBlocks * featureBlockSizeInTiles) + 1) || y < 0 || (y >= (featureMapHeightInBlocks * featureBlockSizeInTiles) + 1))
			return;
		removeAllFeaturesInRange(x * tileSize, y * tileSize, (Math.min(brush.width, brush.height) / 2f) * tileSize);
	}
	
	public void rotateFeaturemap(int px, int py, FeatureBrush brush, boolean invert)
	{
		float amount = brush.rotateStrength;
		if (invert)
			amount = -amount;
		int x = px + (brush.width / 2);
		int y = py + (brush.height / 2);
		if ((x >= 0) && (x < (featureMapWidthInBlocks * featureBlockSizeInTiles) + 1) && (y >= 0) && (y < (featureMapHeightInBlocks * featureBlockSizeInTiles) + 1))
			rotateClosestFeature(x * tileSize, y * tileSize, ((brush.width / 2f) * tileSize), amount);
	}
	
	public void rotateSameRandom(int px, int py, FeatureBrush brush)
	{
		int xmiddle = px + (brush.width / 2);
		int ymiddle = py + (brush.height / 2);
		if ((xmiddle >= 0) && (xmiddle < (featureMapWidthInBlocks * featureBlockSizeInTiles) + 1) && (ymiddle >= 0) && (ymiddle < (featureMapHeightInBlocks * featureBlockSizeInTiles) + 1))
		{
			float x = xmiddle * tileSize;
			float z = ymiddle * tileSize;
			FeatureMapContainer f = findClosestFeature(x, z, ((brush.width / 2f) * tileSize));
			if (f != null)
			{
				int featureID = f.featureID;
				
				//Rotate all features with same id
				Random r = new Random();
				Iterator<FeatureMapContainer> it;
				for (int i = 0; i < featureBlockCount; i++)
				{
					it = featureList[i].iterator();
					while (it.hasNext())
					{
						f = it.next();
						if (f.featureID == featureID)
							f.rotY = r.nextFloat() * 360;
					}
				}
			}
		}
	}
	
	/**
	 * Returns block coordinates at given x/z or null if outside bounds
	 * @param x
	 * @param z
	 * @return
	 */
	public Point getFeatureBlockByCoords(float x, float z)
	{
		int bx = (int)((x / tileSize) / featureBlockSizeInTiles);
		int by = (int)((z / tileSize) / featureBlockSizeInTiles);
		if ((bx >= 0) && (bx < featureMapWidthInBlocks) && (by >= 0) && (by < featureMapHeightInBlocks))
			return new Point(bx, by);
		else
			return null;
	}
	
	/**
	 * Add a new feature with id featureID to featuremap at given x and z position with rotY rotation
	 * @param x
	 * @param z
	 * @param rotY
	 * @param featureID
	 * @return
	 */
	public void addFeature(float x, float z, float rotY, int featureID)
	{
		Point block = getFeatureBlockByCoords(x, z); // Z is length in opengl coordinate system while y is height
		if (block != null)
			featureList[block.y * featureMapWidthInBlocks + block.x].add(new FeatureMapContainer(x, 0, z, rotY, featureID));
	}
	
	/**
	 * returns FeatureMapContainer at given location or null if outside map
	 * @param x
	 * @param z
	 * @param maxDist
	 * @return
	 */
	private FeatureMapContainer findClosestFeature(float x, float z, float maxDist)
	{
		float left = x - maxDist;
		float top = z - maxDist;
		float right = x + maxDist;
		float bottom = z + maxDist;
		Point leftUpperBlock = getFeatureBlockByCoords(left, top);
		Point rightLowerBlock = getFeatureBlockByCoords(right, bottom);
		
		// Check for valid block
		if (leftUpperBlock == null)
			return null;
		if (rightLowerBlock == null)
			return null;
		
		int blockX, blockY;
		Iterator<FeatureMapContainer> it;
		FeatureMapContainer f;
		float dist;
		float curMinDist = maxDist * 2;
		FeatureMapContainer result = null;
		for (blockY = leftUpperBlock.y; blockY <= rightLowerBlock.y; blockY++)
			for (blockX = leftUpperBlock.x; blockX <= rightLowerBlock.x; blockX++)
			{
				it = featureList[blockX + (blockY * featureMapWidthInBlocks)].iterator();
				while (it.hasNext())
				{
					f = it.next();
					dist = Math.abs(f.x - x) + Math.abs(f.z - z);
					if ((dist < curMinDist) && (Math.abs(f.x - x) <= maxDist) && (Math.abs(f.z - z) <= maxDist))
					{
						curMinDist = dist;
						result = f;
					}
				}
			}
		return result;
	}
	
	public void removeAllFeaturesInRange(float x, float z, float maxDist)
	{
		FeatureMapContainer f = findClosestFeature(x, z, maxDist);
		while (f != null)
		{
			Point block = getFeatureBlockByCoords(f.x, f.z);
			featureList[block.x + (block.y * featureMapWidthInBlocks)].remove(f);
			f = findClosestFeature(x, z, maxDist);
		}
	}
	
	public void removeClosestFeature(float x, float z, float maxDist)
	{
		FeatureMapContainer f = findClosestFeature(x, z, maxDist);
		if (f != null)
		{
			Point block = getFeatureBlockByCoords(f.x, f.z);
			featureList[block.x + (block.y * featureMapWidthInBlocks)].remove(f);
		}
	}
	
	public void rotateClosestFeature(float x, float z, float maxDist, float amount)
	{
		FeatureMapContainer f = findClosestFeature(x, z, maxDist);
		if (f != null)
		{
			f.rotY += amount;
			if (f.rotY > 360)
				f.rotY -= 360;
			if (f.rotY < 0)
				f.rotY += 360;
		}
	}
	
}
