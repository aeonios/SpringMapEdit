package backend.map;

import java.io.File;
import java.util.Arrays;

import frontend.render.brushes.PrefabBrush;
import frontend.render.brushes.TextureBrush;

import backend.FastMath;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;

public class Texturemap {
	public byte[][] textureMap;
	private int textureMapWidth; // The actual array width is * 3 for color channels
	public int texturemapLength;

	public TexturemapLayer[] layers;
	
	private int bufferWidth;
	private byte[] buffer = new byte[0];
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual heightmap-size. (do not forget to add 1 pixel to heightmap)
	 */
	public static final int springMapsizeHeightmapFactor = 64;
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual texture-size.
	 */
	public static final int springMapsizeTexturemapFactor = springMapsizeHeightmapFactor * 8;
	/**
	 * This is the factor we need to multiply the mapsize with,<BR>
	 * to get the actual texture-size.
	 */
	public static final int heightmapSizeTextureFactor = springMapsizeTexturemapFactor / springMapsizeHeightmapFactor;
	
	public Texturemap(int height, int width)
	{
		textureMapWidth = width * springMapsizeTexturemapFactor;
		texturemapLength = height * springMapsizeTexturemapFactor;
		textureMap = new byte[texturemapLength][textureMapWidth * 3]; // The whole texturemap
		layers = new TexturemapLayer[1]; // Layers that can be edited separately and will be compiled to the texturemap
		//layers[0] = new TexturemapLayer(height * springMapsizeTexturemapFactor, width * springMapsizeTexturemapFactor);
	}
	
	public int getLength()
	{
		return texturemapLength;
	}
	
	public int getWidth()
	{
		return textureMapWidth;
	}
	
	public byte[][] getMap()
	{
		return textureMap;
	}
	
	public void saveTextureMap(File texturemapFile)
	{
		compileLayers();
		new Bitmap(FileFormat.Bitmap24Bit).saveDataFromTexturemap(texturemapFile, textureMap, textureMapWidth, texturemapLength);
	}
	
	public void loadDataIntoTexturemap(File texturemapFile)
	{
			/*if ((bitmap.width == width * springMapsizeTexturemapFactor) && (bitmap.height == height * springMapsizeTexturemapFactor))
				throw new IllegalArgumentException("TextureMapsize must be " + heightmapSizeTextureFactor + " * (heightmapsize - 1)");*/
			new Bitmap(texturemapFile).loadDataIntoTexturemap(textureMap);
	}
	
	public void switchMapAxis()
	{
		int t = textureMapWidth;
		textureMapWidth = texturemapLength;
		texturemapLength = t;
		byte[][] newMap = new byte[texturemapLength][textureMapWidth * 3];
		for (int y = 0; y < texturemapLength; y++)
			for (int x = 0; x < textureMapWidth; x++)
				for (int i = 0; i < 3; i++)
					newMap[y][x * 3 + i] = textureMap[x][y * 3 + i];
		textureMap = newMap;
	}
	
	public void resizeMap(int NewHeight, int NewWidth)
	{
		byte[][] newMap = new byte[NewHeight][NewWidth * 3];
		int width = NewWidth;
		if (width > textureMapWidth)
			width = textureMapWidth;
		int height = NewHeight;
		if (height > texturemapLength)
			height = texturemapLength;
		for (int y = 0; y < height; y++)
			System.arraycopy(textureMap[y], 0, newMap[y], 0, width * 3);
		textureMap = newMap;
		textureMapWidth = NewWidth;
		texturemapLength = NewHeight;
	}
	
	public void moveMap(int start, int length, int amount, boolean vertically)
	{
		byte[][] t;
		if (amount <= 0 || amount >= length)
			return;
		if (vertically)
		{
			if (start >= textureMapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > textureMapWidth)
				length = textureMapWidth - start;
			if (length <= 0)
				return;
			t = new byte[texturemapLength][amount * 3];
			for (int y = 0; y < texturemapLength; y++)
				for (int x = 0; x < amount * 3; x++)
					t[y][x] = textureMap[y][(start + length - amount) * 3 + x];
			for (int y = 0; y < texturemapLength; y++)
				for (int x = (length - amount - 1) * 3; x >= 0; x--)
					textureMap[y][(start + amount) * 3 + x] = textureMap[y][start * 3 + x];
			for (int y = 0; y < texturemapLength; y++)
				for (int x = 0; x < amount * 3; x++)
					textureMap[y][start * 3 + x] = t[y][x];
		}
		else
		{
			if (start >= texturemapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > texturemapLength)
				length = texturemapLength - start;
			if (length <= 0)
				return;
			t = new byte[amount][textureMapWidth * 3];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < textureMapWidth * 3; x++)
					t[y][x] = textureMap[start + length - amount + y][x];
			for (int y = length - amount - 1; y >= 0; y--)
				for (int x = 0; x < textureMapWidth * 3; x++)
					textureMap[start + amount + y][x] = textureMap[start + y][x];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < textureMapWidth * 3; x++)
					textureMap[start + y][x] = t[y][x];
		}
	}
	
	public void mirrorMap(int start, int length, int offset, boolean vertically)
	{
		if (vertically)
		{
			if (start >= textureMapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > textureMapWidth)
				length = textureMapWidth - start;
			if (start + offset >= textureMapWidth)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > textureMapWidth)
				length = textureMapWidth - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < texturemapLength; y++)
				for (int x = 0; x < length; x++)
					for (int c = 0; c < 3; c++)
						textureMap[y][(offset - x) * 3 + c] = textureMap[y][(start + x) * 3 + c];
		}
		else
		{
			if (start >= texturemapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > texturemapLength)
				length = texturemapLength - start;
			if (offset + start >= texturemapLength)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > texturemapLength)
				length = texturemapLength - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < textureMapWidth; x++)
					for (int c = 0; c < 3; c++)
						textureMap[offset - y][x * 3 + c] = textureMap[start + y][x * 3 + c];
		}
	}
	
	public void flipMap(int start, int length, int offset, boolean vertically)
	{
		byte t;
		if (vertically)
		{
			if (start >= textureMapWidth)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > textureMapWidth)
				length = textureMapWidth - start;
			if (start + offset >= textureMapWidth)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > textureMapWidth)
				length = textureMapWidth - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < texturemapLength; y++)
				for (int x = 0; x < length; x++)
					for (int c = 0; c < 3; c++)
					{
						t = textureMap[y][(offset - x) * 3 + c];
						textureMap[y][(offset - x) * 3 + c] = textureMap[y][(start + x) * 3 + c];
						textureMap[y][(start + x) * 3 + c] = t;
					}
		}
		else
		{
			if (start >= texturemapLength)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > texturemapLength)
				length = texturemapLength - start;
			if (offset + start >= texturemapLength)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > texturemapLength)
				length = texturemapLength - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < textureMapWidth; x++)
					for (int c = 0; c < 3; c++)
					{
						t = textureMap[offset - y][x * 3 + c];
						textureMap[offset - y][x * 3 + c] = textureMap[start + y][x * 3 + c];
						textureMap[start + y][x * 3 + c] = t;
					}
		}
	}
	
	public void copy(int px, int py, PrefabBrush brush)
	{
		int height = brush.getHeight() * heightmapSizeTextureFactor;
		int width = brush.getWidth() * heightmapSizeTextureFactor;
		if (px >= textureMapWidth || py >= texturemapLength)
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
		if (py + height >= texturemapLength)
			height = texturemapLength - py;
		if (px + width >= textureMapWidth)
			width = textureMapWidth - px;
		if (height <= 0 || width <= 0)
			return;
		buffer = new byte[height * width * 3];
		bufferWidth = width;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width * 3; x++)
				buffer[y * width * 3 + x] = textureMap[py + y][px * 3 + x];
	}
	
	public void paste(int py, int px, PrefabBrush brush)
	{
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		byte[] tempBuffer = buffer;
		int tempBufferWidth = bufferWidth;
		int height = brush.getHeight() * heightmapSizeTextureFactor;
		int width = brush.getWidth() * heightmapSizeTextureFactor;
		if (px >= textureMapWidth || py >= texturemapLength)
			return;
		if (px < 0)
		{
			width += px;
			tempBufferWidth += px;
			if (width <= 0 || tempBufferWidth <= 0)
				return;
			tempBuffer = new byte[buffer.length / bufferWidth * tempBufferWidth];
			for (int y = 0; y < buffer.length / bufferWidth / 3; y++)
				System.arraycopy(buffer, (y * bufferWidth - px) * 3, tempBuffer, y * tempBufferWidth * 3, tempBufferWidth * 3);
			px = 0;
		}
		if (py < 0)
		{
			height += py;
			if (height <= 0 || tempBufferWidth <= 0 || (buffer.length / tempBufferWidth / 3) + py <= 0)
				return;
			tempBuffer = new byte[((buffer.length / tempBufferWidth / 3) + py) * tempBufferWidth * 3];
			System.arraycopy(buffer, -py * tempBufferWidth * 3, tempBuffer, 0, ((buffer.length / tempBufferWidth / 3) + py) * tempBufferWidth * 3);
			py = 0;
		}
		if (height + py >= texturemapLength)
			height = texturemapLength - py;
		if (width + px >= textureMapWidth)
			width = textureMapWidth - px;
		if (width > tempBufferWidth)
			width = tempBufferWidth;
		if (width <= 0 || tempBufferWidth <= 0)
			return;
		if (height > buffer.length / tempBufferWidth / 3)
			height = buffer.length / tempBufferWidth / 3;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width * 3; x++)
				textureMap[py + y][px * 3 + x] = tempBuffer[y * tempBufferWidth * 3 + x];// + (amount * pattern[x - px][y - py]);
	}
	
	public void compileLayers()
	{
		for (int i = 0; i < layers.length; i++)
			for (int y = 0; y < texturemapLength; y++)
				for (int x = 0; x < textureMapWidth; x++)
					for (int c = 0; c < 3; c++)
						;//textureMap[y][x * 3 + c] += (layers[i].texturemapLayer[y][x * 4 + c] - textureMap[y][x * 3 + c]) * (layers[i].texturemapLayer[y][x * 4 + 3] / 255);
	}
	
	public void setColorToTexture(int px, int py, TextureBrush brush)
	{
		float amount = brush.getStrength();
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		px *= heightmapSizeTextureFactor;
		py *= heightmapSizeTextureFactor;
		int r, g, b;
		for (int y = py; y < py + (brush.getHeight() * heightmapSizeTextureFactor); y++)
			for (int x = px; x < px + (brush.getWidth() * heightmapSizeTextureFactor); x++)
				if ((x >= 0) && (x < textureMapWidth) && (y >= 0) && (y < texturemapLength))
				{
					r = FastMath.round((amount * (textureR[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					g = FastMath.round((amount * (textureG[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					b = FastMath.round((amount * (textureB[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
							
					textureMap[y][(x * 3) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					textureMap[y][(x * 3) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					textureMap[y][(x * 3) + 2] = (byte)Math.min(Math.max(b, 0), 255);
				}
	}
	
	public void addColorToTexture(int px, int py, TextureBrush brush)
	{
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		byte[][] textureA = brush.texture.getTextureA();
		px *= heightmapSizeTextureFactor;
		py *= heightmapSizeTextureFactor;
		int r, g, b;
		for (int y = py; y < py + (brush.getHeight() * heightmapSizeTextureFactor); y++)
			for (int x = px; x < px + (brush.getWidth() * heightmapSizeTextureFactor); x++)
				if ((x >= 0) && (x < textureMapWidth) && (y >= 0) && (y < texturemapLength))
				{
					float alpha = amount * ((textureA[x % brush.texture.width][y % brush.texture.height] & 0xFF) / (float) 0xFF)
							* pattern[((x - px) / heightmapSizeTextureFactor)][((y - py) / heightmapSizeTextureFactor)];
					r = FastMath.round((textureMap[y][(x * 3) + 0] & 0xFF) + (alpha * (textureR[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					g = FastMath.round((textureMap[y][(x * 3) + 1] & 0xFF) + (alpha * (textureG[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					b = FastMath.round((textureMap[y][(x * 3) + 2] & 0xFF) + (alpha * (textureB[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					
					textureMap[y][(x * 3) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					textureMap[y][(x * 3) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					textureMap[y][(x * 3) + 2] = (byte)Math.min(Math.max(b, 0), 255);
				}
	}

	public void multiplyColorToTexture(int px, int py, TextureBrush brush)
	{
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		byte[][] textureA = brush.texture.getTextureA();
		px *= heightmapSizeTextureFactor;
		py *= heightmapSizeTextureFactor;
		int r, g, b;
		int destr, destg, destb;
		for (int y = py; y < py + (brush.getHeight() * heightmapSizeTextureFactor); y++)
			for (int x = px; x < px + (brush.getWidth() * heightmapSizeTextureFactor); x++)
				if ((x >= 0) && (x < textureMapWidth) && (y >= 0) && (y < texturemapLength))
				{
					destr = (textureMap[y][(x * 3) + 0] & 0xFF);
					destg = (textureMap[y][(x * 3) + 1] & 0xFF);
					destb = (textureMap[y][(x * 3) + 2] & 0xFF);

					float alpha = amount * ((textureA[x % brush.texture.width][y % brush.texture.height] & 0xFF) / (float) 0xFF)
							* pattern[((x - px) / heightmapSizeTextureFactor)][((y - py) / heightmapSizeTextureFactor)];
					r = FastMath.round(((1f - alpha) * destr) + (alpha * (destr/255f) * (textureR[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					g = FastMath.round(((1f - alpha) * destg) + (alpha * (destg/255f) * (textureG[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					b = FastMath.round(((1f - alpha) * destb) + (alpha * (destb/255f) * (textureB[x % brush.texture.width][y % brush.texture.height] & 0xFF)));

					textureMap[y][(x * 3) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					textureMap[y][(x * 3) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					textureMap[y][(x * 3) + 2] = (byte)Math.min(Math.max(b, 0), 255);
				}
	}
	
	public void blendColorToTexture(int px, int py, TextureBrush brush)
	{
		float[][] pattern = brush.getPattern().getPattern();
		float[][] newPattern = pattern;
		int length = brush.getHeight() * heightmapSizeTextureFactor;
		int width = brush.getWidth() * heightmapSizeTextureFactor;
		px *= heightmapSizeTextureFactor;
		py *= heightmapSizeTextureFactor;
		if (px >= textureMapWidth || py >= texturemapLength)
			return;
		if (px < 0)
		{
			width += px;
			if (width <= 0)
				return;
			newPattern = new float[width / heightmapSizeTextureFactor][length / heightmapSizeTextureFactor];
			System.arraycopy(pattern, (-px - 1) / heightmapSizeTextureFactor, newPattern, 0, width / heightmapSizeTextureFactor);
			pattern = newPattern;
			px = 0;
		}
		if (py < 0)
		{
			length += py;
			if (length <= 0)
				return;
			newPattern = new float[width / heightmapSizeTextureFactor][length / heightmapSizeTextureFactor];
			for (int x = 0; x < width / heightmapSizeTextureFactor; x++)
				System.arraycopy(pattern[x], (-py - 1) / heightmapSizeTextureFactor, newPattern[x], 0, length / heightmapSizeTextureFactor);
			pattern = newPattern;
			py = 0;
		}
		if (length + py >= texturemapLength)
			length = texturemapLength - py;
		if (width + px >= textureMapWidth)
			width = textureMapWidth - px;
		if (width <= 0 || length <= 0)
			return;
		int r, g, b;
		int patternX, patternY;
		float blendFactorX, blendFactorY, leftVal, rightVal;
		float newTexAmountOrigin, newTexAmountLower, newTexAmountRight, newTexAmountLowerRight, newTexAmount, invNewTexAmount;
		float amount = brush.getStrength();
		int patternWidth = width / heightmapSizeTextureFactor;//brush.getPattern().width;
		int patternHeight = length / heightmapSizeTextureFactor;//brush.getPattern().height;
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		byte[][] textureA = brush.texture.getTextureA();
		for (int y = py; y < py + length; y++)
			for (int x = px; x < px + width; x++) {
				patternX = (x - px) / heightmapSizeTextureFactor;
				patternY = (y - py) / heightmapSizeTextureFactor;
				newTexAmountOrigin = amount * newPattern[patternX][patternY];
				newTexAmountRight = newTexAmountOrigin;
				newTexAmountLower = newTexAmountOrigin;
				newTexAmountLowerRight = newTexAmountOrigin;
				if ((patternX + 1) < patternWidth)
					newTexAmountRight = amount * newPattern[patternX + 1][patternY];
				if ((patternY + 1) < patternHeight)
					newTexAmountLower = amount * newPattern[patternX][patternY + 1];
				if (((patternX + 1) < patternWidth) && ((patternY + 1) < patternHeight))
					newTexAmountLowerRight = amount * newPattern[patternX + 1][patternY + 1];
				blendFactorX = ((x - px) % heightmapSizeTextureFactor) / (float)heightmapSizeTextureFactor;
				blendFactorY = ((y - py) % heightmapSizeTextureFactor) / (float)heightmapSizeTextureFactor;
				leftVal = (newTexAmountOrigin * (1 - blendFactorY)) + (newTexAmountLower * blendFactorY);
				rightVal = (newTexAmountRight * (1 - blendFactorY)) + (newTexAmountLowerRight * blendFactorY);
				newTexAmount = (leftVal * (1 - blendFactorX)) + (rightVal * blendFactorX);

				newTexAmount *= (textureA[x % brush.texture.width][y % brush.texture.height] & 0xFF) / (float) 0xFF;
				invNewTexAmount = 1 - newTexAmount;
				r = FastMath.round((invNewTexAmount * (textureMap[y][(x * 3) + 0] & 0xFF)) + (newTexAmount * (textureR[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
				g = FastMath.round((invNewTexAmount * (textureMap[y][(x * 3) + 1] & 0xFF)) + (newTexAmount * (textureG[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
				b = FastMath.round((invNewTexAmount * (textureMap[y][(x * 3) + 2] & 0xFF)) + (newTexAmount * (textureB[x % brush.texture.width][y % brush.texture.height] & 0xFF)));

				textureMap[y][(x * 3) + 0] = (byte)Math.min(Math.max(r, 0), 255);
				textureMap[y][(x * 3) + 1] = (byte)Math.min(Math.max(g, 0), 255);
				textureMap[y][(x * 3) + 2] = (byte)Math.min(Math.max(b, 0), 255);
			}
	}
	
	public void stampColorToTexture(int px, int py, TextureBrush brush)
	{
		float strength = brush.getStrength();
		float amount;
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		byte[][] textureA = brush.texture.getTextureA();

		// First get the brush center pos and convert it into texture coords
		px += brush.getWidth()/2;
		py += brush.getHeight()/2;
		px = (px * heightmapSizeTextureFactor);
		py = (py * heightmapSizeTextureFactor);

		// Then offset it by half of the brush texture size to center it.
		px -= brush.texture.width/2;
		py -= brush.texture.height/2;

		int r, g, b;
		for (int y = 0; y < brush.texture.height ; y++)
			for (int x = 0; x < brush.texture.width; x++)
				if ((x+px >= 0) && (x+px < textureMapWidth) && (y+py >= 0) && (y+py < texturemapLength))
				{
					amount = strength * ((textureA[x][y] & 0xFF) / (float) 0xFF);
					r = FastMath.round(((1 - amount) * (textureMap[y+py][((x+px) * 3) + 0] & 0xFF))
							+ (amount * (textureR[x][y] & 0xFF)));
					g = FastMath.round(((1 - amount) * (textureMap[y+py][((x+px) * 3) + 1] & 0xFF))
							+ (amount * (textureG[x][y] & 0xFF)));
					b = FastMath.round(((1 - amount) * (textureMap[y+py][((x+px) * 3) + 2] & 0xFF))
							+ (amount * (textureB[x][y] & 0xFF)));
							
					textureMap[y+py][((x+px) * 3) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					textureMap[y+py][((x+px) * 3) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					textureMap[y+py][((x+px) * 3) + 2] = (byte)Math.min(Math.max(b, 0), 255);
				}
	}
	
	public void setPrefabTextureMap(int px, int py, PrefabBrush brush)
	{
		if (brush.texturemap != null)
		{
			byte[][] textureR = brush.texturemap.getTextureR();
			byte[][] textureG = brush.texturemap.getTextureG();
			byte[][] textureB = brush.texturemap.getTextureB();
			px *= heightmapSizeTextureFactor;
			py *= heightmapSizeTextureFactor;
			int r, g, b;
			for (int y = py; y < py + ((brush.height - 1) * heightmapSizeTextureFactor); y++)
				for (int x = px; x < px + ((brush.width - 1) * heightmapSizeTextureFactor); x++)
					if ((x >= 0) && (x < textureMapWidth) && (y >= 0) && (y < texturemapLength))
					{
						r = textureR[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						g = textureG[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						b = textureB[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
								
						textureMap[y][(x * 3) + 0] = (byte)Math.min(Math.max(r, 0), 255);
						textureMap[y][(x * 3) + 1] = (byte)Math.min(Math.max(g, 0), 255);
						textureMap[y][(x * 3) + 2] = (byte)Math.min(Math.max(b, 0), 255);
					}
		}
	}
	
	public void addPrefabTextureMap(int px, int py, PrefabBrush brush)
	{
		if (brush.texturemap != null)
		{
			byte[][] textureR = brush.texturemap.getTextureR();
			byte[][] textureG = brush.texturemap.getTextureG();
			byte[][] textureB = brush.texturemap.getTextureB();
			px = (px * heightmapSizeTextureFactor);
			py = (py * heightmapSizeTextureFactor);
			int r, g, b;
			for (int y = py; y < py + ((brush.height - 1) * heightmapSizeTextureFactor); y++)
			{
				for (int x = px; x < px + ((brush.width - 1) * heightmapSizeTextureFactor); x++)
				{
					if ((x >= 0) && (x < textureMapWidth) && (y >= 0) && (y < texturemapLength))
					{
						r = textureR[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						g = textureG[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						b = textureB[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
								
						textureMap[y][(x * 3) + 0] = (byte)Math.min(Math.max(r, 0), 255);
						textureMap[y][(x * 3) + 1] = (byte)Math.min(Math.max(g, 0), 255);
						textureMap[y][(x * 3) + 2] = (byte)Math.min(Math.max(b, 0), 255);
					}
				}
			}
		}
	}
	
	public void whiteOutTextureMap()
	{
		long start = System.nanoTime();
		for (int y = 0; y < texturemapLength; y++)
			Arrays.fill(textureMap[y], (byte)127);
		System.out.println("Done blanking texture ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
}
