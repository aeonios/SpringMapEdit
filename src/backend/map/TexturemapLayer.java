package backend.map;

import java.io.File;
import java.util.Arrays;

import frontend.render.brushes.PrefabBrush;
import frontend.render.brushes.TextureBrush;

import backend.FastMath;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;

public class TexturemapLayer {
	public byte[][] texturemapLayer;
	public int width; // The actual array width is * 4 for color channels + alpha
	public int length;

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
	
	public TexturemapLayer(int length, int width)
	{
		this.width = width * springMapsizeTexturemapFactor;
		this.length = length * springMapsizeTexturemapFactor;
		texturemapLayer = new byte[length][width * 4];
	}
	
	public int getMapLength()
	{
		return length;
	}
	
	public int getMapWidth()
	{
		return width;
	}
	
	public void saveMap(File texturemapFile)
	{
		new Bitmap(FileFormat.Bitmap24Bit).saveDataFromTexturemap(texturemapFile, texturemapLayer, width, length);
	}
	
	public void loadDataIntoMap(File texturemapFile)
	{
			/*if ((bitmap.width == width * springMapsizeTexturemapFactor) && (bitmap.height == height * springMapsizeTexturemapFactor))
				throw new IllegalArgumentException("TextureMapsize must be " + heightmapSizeTextureFactor + " * (heightmapsize - 1)");*/
			new Bitmap(texturemapFile).loadDataIntoTexturemap(texturemapLayer);
	}
	
	public void switchMapAxis()
	{
		int t = width;
		width = length;
		length = t;
		byte[][] newMap = new byte[length][width * 4];
		for (int y = 0; y < length; y++)
			for (int x = 0; x < width; x++)
				for (int i = 0; i < 4; i++)
					newMap[y][x * 4 + i] = texturemapLayer[x][y * 4 + i];
		texturemapLayer = newMap;
	}
	
	public void resizeMap(int NewWidth, int NewHeight)
	{
		byte[][] newMap = new byte[NewHeight][NewWidth * 4];
		int width = NewWidth;
		if (NewWidth > this.width)
			width = this.width;
		int length = NewHeight;
		if (length > this.length)
			length = this.length;
		for (int y = 0; y < length; y++)
			System.arraycopy(texturemapLayer[y], 0, newMap[y], 0, width * 4);
		texturemapLayer = newMap;
		width = NewWidth;
		length = NewHeight;
	}
	
	public void moveMap(int start, int length, int amount, boolean vertically)
	{
		byte[][] t;
		if (amount <= 0 || amount >= length)
			return;
		if (vertically)
		{
			if (start >= width)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > width)
				length = width - start;
			if (length <= 0)
				return;
			t = new byte[this.length][amount * 4];
			for (int y = 0; y < this.length; y++)
				for (int x = 0; x < amount * 4; x++)
					t[y][x] = texturemapLayer[y][(start + length - amount) * 4 + x];
			for (int y = 0; y < this.length; y++)
				for (int x = (length - amount) * 4 - 1; x >= 0; x--)
					texturemapLayer[y][(start + amount) * 4 + x] = texturemapLayer[y][start * 4 + x];
			for (int y = 0; y < this.length; y++)
				for (int x = 0; x < amount * 4; x++)
					texturemapLayer[y][start * 4 + x] = t[y][x];
		}
		else
		{
			if (start >= this.length)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > this.length)
				length = this.length - start;
			if (length <= 0)
				return;
			t = new byte[amount][width * 4];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < width * 4; x++)
					t[y][x] = texturemapLayer[start + length - amount + y][x];
			for (int y = length - amount - 1; y >= 0; y++)
				for (int x = 0; x < width * 4; x++)
					texturemapLayer[start + amount + y][x] = texturemapLayer[start + y][x];
			for (int y = 0; y < amount; y++)
				for (int x = 0; x < width * 4; x++)
					texturemapLayer[start + y][x] = t[y][x];
		}
	}
	
	public void mirrorMap(int start, int length, int offset, boolean vertically)
	{
		if (vertically)
		{
			if (start >= width)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > width)
				length = width - start;
			if (start + offset >= width)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > width)
				length = width - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < this.length; y++)
				for (int x = 0; x < length; x++)
					for (int c = 0; c < 4; c++)
						texturemapLayer[y][(offset - x) * 4 + c] = texturemapLayer[y][(start + x) * 4 + c];
		}
		else
		{
			if (start >= this.length)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > this.length)
				length = this.length - start;
			if (offset + start >= this.length)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > this.length)
				length = this.length - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < this.length; x++)
					for (int c = 0; c < 4; c++)
						texturemapLayer[offset - y][x * 4 + c] = texturemapLayer[start + y][x * 4 + c];
		}
	}
	
	public void flipMap(int start, int length, int offset, boolean vertically)
	{
		byte t;
		if (vertically)
		{
			if (start >= width)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > width)
				length = width - start;
			if (start + offset >= width)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > width)
				length = width - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < this.length; y++)
				for (int x = 0; x < length; x++)
					for (int c = 0; c < 4; c++)
					{
						t = texturemapLayer[y][(offset - x) * 4 + c];
						texturemapLayer[y][(offset - x) * 4 + c] = texturemapLayer[y][(start + x) * 4 + c];
						texturemapLayer[y][(start + x) * 4 + c] = t;
					}
		}
		else
		{
			if (start >= this.length)
				return;
			if (start < 0)
			{
				length += start;
				start = 0;
			}
			if (start + length > this.length)
				length = this.length - start;
			if (offset + start >= this.length)
				return;
			if (offset + start < 0)
			{
				length += offset + start;
				start -= start + offset;
				offset = -start;
			}
			if (offset + start + length > this.length)
				length = this.length - start - offset;
			if (length <= 0)
				return;
			offset += start + length - 1;
			for (int y = 0; y < length; y++)
				for (int x = 0; x < width; x++)
					for (int c = 0; c < 4; c++)
					{
						t = texturemapLayer[offset - y][x * 4 + c];
						texturemapLayer[offset - y][x * 4 + c] = texturemapLayer[start + y][x * 4 + c];
						texturemapLayer[start + y][x * 4 + c] = t;
					}
		}
	}
	
	public void copy(int px, int py, PrefabBrush brush)
	{
		int length = brush.getHeight() * heightmapSizeTextureFactor;
		int width = brush.getWidth() * heightmapSizeTextureFactor;
		if (px >= this.width || py >= this.length)
			return;
		if (px < 0)
		{
			width =+ px;
			px = 0;
		}
		if (py < 0)
		{
			length += py;
			py = 0;
		}
		if (py + length >= this.length)
			length = this.length - py;
		if (px + width >= this.width)
			width = this.width - px;
		if (length <= 0 || width <= 0)
			return;
		buffer = new byte[length * width * 4];
		bufferWidth = width;
		for (int y = 0; y < length; y++)
			for (int x = 0; x < width * 4; x++)
				buffer[y * width * 4 + x] = texturemapLayer[py + y][px * 4 + x];
	}
	
	public void paste(int px, int py, PrefabBrush brush)
	{
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		byte[] tempBuffer = buffer;
		int tempBufferWidth = bufferWidth;
		int height = brush.getHeight() * heightmapSizeTextureFactor;
		int width = brush.getWidth() * heightmapSizeTextureFactor;
		if (px >= this.width || py >= this.length)
			return;
		if (px < 0)
		{
			width += px;
			tempBufferWidth += px;
			if (width <= 0 || tempBufferWidth <= 0)
				return;
			tempBuffer = new byte[buffer.length / bufferWidth * tempBufferWidth];
			for (int y = 0; y < buffer.length / bufferWidth / 4; y++)
				System.arraycopy(buffer, (y * bufferWidth - px) * 4, tempBuffer, y * tempBufferWidth * 4, tempBufferWidth * 4);
			px = 0;
		}
		if (py < 0)
		{
			height += py;
			if (height <= 0 || (buffer.length / tempBufferWidth / 4) + py <= 0)
				return;
			tempBuffer = new byte[((buffer.length / tempBufferWidth / 4) + py) * tempBufferWidth * 4];
			System.arraycopy(buffer, -py * tempBufferWidth * 4, tempBuffer, 0, ((buffer.length / tempBufferWidth / 4) + py) * tempBufferWidth * 4);
			py = 0;
		}
		if (height + py >= this.length)
			height = this.length - py;
		if (width + px >= this.width)
			width = this.width - px;
		if (width > tempBufferWidth)
			width = tempBufferWidth;
		if (height > buffer.length / tempBufferWidth / 4)
			height = buffer.length / tempBufferWidth / 4;
		if (width <= 0)
			return;
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width * 4; x++)
				texturemapLayer[py + y][px * 4 + x] = tempBuffer[y * tempBufferWidth * 4 + x];// + (amount * pattern[x - px][y - py]);
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
				if ((x >= 0) && (x < width) && (y >= 0) && (y < length))
				{
					r = FastMath.round((amount * (textureR[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					g = FastMath.round((amount * (textureG[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					b = FastMath.round((amount * (textureB[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
							
					texturemapLayer[y][(x * 4) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					texturemapLayer[y][(x * 4) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					texturemapLayer[y][(x * 4) + 2] = (byte)Math.min(Math.max(b, 0), 255);
					texturemapLayer[y][(x * 4) + 3] = (byte)255;
				}
	}
	
	public void addColorToTexture(int px, int py, TextureBrush brush)
	{
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		px *= heightmapSizeTextureFactor;
		py *= heightmapSizeTextureFactor;
		int r, g, b;
		for (int y = py; y < py + (brush.getHeight() * heightmapSizeTextureFactor); y++)
			for (int x = px; x < px + (brush.getWidth() * heightmapSizeTextureFactor); x++)
				if ((x >= 0) && (x < width) && (y >= 0) && (y < length))
				{
					r = FastMath.round((texturemapLayer[y][(x * 4) + 0] & 0xFF) + (amount * pattern[((x - px) / heightmapSizeTextureFactor)][((y - py) / heightmapSizeTextureFactor)] 
							* (textureR[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					g = FastMath.round((texturemapLayer[y][(x * 4) + 1] & 0xFF) + (amount * pattern[((x - px) / heightmapSizeTextureFactor)][((y - py) / heightmapSizeTextureFactor)] 
							* (textureG[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					b = FastMath.round((texturemapLayer[y][(x * 4) + 2] & 0xFF) + (amount * pattern[((x - px) / heightmapSizeTextureFactor)][((y - py) / heightmapSizeTextureFactor)] 
							* (textureB[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					
					texturemapLayer[y][(x * 4) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					texturemapLayer[y][(x * 4) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					texturemapLayer[y][(x * 4) + 2] = (byte)Math.min(Math.max(b, 0), 255);
					texturemapLayer[y][(x * 4) + 3] = (byte)255;
				}
	}
	
	public void blendColorToTexture(int px, int py, TextureBrush brush)
	{
		int r, g, b;
		int patternX, patternY;
		float blendFactorX, blendFactorY, leftVal, rightVal;
		float newTexAmountOrigin, newTexAmountLower, newTexAmountRight, newTexAmountLowerRight, newTexAmount, invNewTexAmount;
		float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();
		int patternWidth = brush.getPattern().width;
		int patternHeight = brush.getPattern().height;
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		byte[][] textureA = brush.texture.getTextureA();
		px = (px * heightmapSizeTextureFactor);
		py = (py * heightmapSizeTextureFactor);
		for (int y = py; y < py + (brush.getHeight() * heightmapSizeTextureFactor); y++)
			for (int x = px; x < px + (brush.getWidth() * heightmapSizeTextureFactor); x++)
				if ((x >= 0) && (x < width) && (y >= 0) && (y < this.length))
				{
					patternX = ((x - px) / heightmapSizeTextureFactor);
					patternY = ((y - py) / heightmapSizeTextureFactor);
					newTexAmountOrigin = amount * pattern[patternX][patternY];
					newTexAmountRight = newTexAmountOrigin;
					newTexAmountLower = newTexAmountOrigin;
					newTexAmountLowerRight = newTexAmountOrigin;
					if ((patternX + 1) < patternWidth)
						newTexAmountRight = amount * pattern[patternX + 1][patternY];
					if ((patternY + 1) < patternHeight)
						newTexAmountLower = amount * pattern[patternX][patternY + 1];
					if (((patternX + 1) < patternWidth) && ((patternY + 1) < patternHeight))
						newTexAmountLowerRight = amount * pattern[patternX + 1][patternY + 1];
					blendFactorX = ((x - px) % heightmapSizeTextureFactor) / (float)heightmapSizeTextureFactor;
					blendFactorY = ((y - py) % heightmapSizeTextureFactor) / (float)heightmapSizeTextureFactor;
					leftVal = (newTexAmountOrigin * (1 - blendFactorY)) + (newTexAmountLower * blendFactorY);
					rightVal = (newTexAmountRight * (1 - blendFactorY)) + (newTexAmountLowerRight * blendFactorY);
					newTexAmount = (leftVal * (1 - blendFactorX)) + (rightVal * blendFactorX);

					invNewTexAmount = 1 - newTexAmount;
					r = FastMath.round((invNewTexAmount * (texturemapLayer[y][(x * 4) + 0] & 0xFF))
							+ (newTexAmount * (textureR[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					g = FastMath.round((invNewTexAmount * (texturemapLayer[y][(x * 4) + 1] & 0xFF))
							+ (newTexAmount * (textureG[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					b = FastMath.round((invNewTexAmount * (texturemapLayer[y][(x * 4) + 2] & 0xFF))
							+ (newTexAmount * (textureB[x % brush.texture.width][y % brush.texture.height] & 0xFF)));
					
					texturemapLayer[y][(x * 4) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					texturemapLayer[y][(x * 4) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					texturemapLayer[y][(x * 4) + 2] = (byte)Math.min(Math.max(b, 0), 255);
					texturemapLayer[y][(x * 4) + 3] = (byte)255;
				}
	}
	
	public void stampColorToTexture(int px, int py, TextureBrush brush)
	{
		float amount = brush.getStrength();
		byte[][] textureR = brush.texture.getTextureR();
		byte[][] textureG = brush.texture.getTextureG();
		byte[][] textureB = brush.texture.getTextureB();
		byte[][] textureA = brush.texture.getTextureA();
		px = (px * heightmapSizeTextureFactor);
		py = (py * heightmapSizeTextureFactor);
		int r, g, b;
		for (int y = py; y < py + (brush.getHeight() * heightmapSizeTextureFactor); y++)
			for (int x = px; x < px + (brush.getWidth() * heightmapSizeTextureFactor); x++)
				if ((x >= 0) && (x < width) && (y >= 0) && (y < length))
				{
					amount = (textureA[(x - px) % brush.texture.width][(y - py) % brush.texture.height] & 0xFF) / (float)0xFF;
					r = FastMath.round(((1 - amount) * (texturemapLayer[y][(x * 4) + 0] & 0xFF))
							+ (amount * (textureR[(x - px) % brush.texture.width][(y - py) % brush.texture.height] & 0xFF)));
					g = FastMath.round(((1 - amount) * (texturemapLayer[y][(x * 4) + 1] & 0xFF))
							+ (amount * (textureG[(x - px) % brush.texture.width][(y - py) % brush.texture.height] & 0xFF)));
					b = FastMath.round(((1 - amount) * (texturemapLayer[y][(x * 4) + 2] & 0xFF))
							+ (amount * (textureB[(x - px) % brush.texture.width][(y - py) % brush.texture.height] & 0xFF)));
							
					texturemapLayer[y][(x * 4) + 0] = (byte)Math.min(Math.max(r, 0), 255);
					texturemapLayer[y][(x * 4) + 1] = (byte)Math.min(Math.max(g, 0), 255);
					texturemapLayer[y][(x * 4) + 2] = (byte)Math.min(Math.max(b, 0), 255);
					texturemapLayer[y][(x * 4) + 3] = (byte)255;
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
					if ((x >= 0) && (x < width) && (y >= 0) && (y < length))
					{
						r = textureR[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						g = textureG[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						b = textureB[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
								
						texturemapLayer[y][(x * 4) + 0] = (byte)Math.min(Math.max(r, 0), 255);
						texturemapLayer[y][(x * 4) + 1] = (byte)Math.min(Math.max(g, 0), 255);
						texturemapLayer[y][(x * 4) + 2] = (byte)Math.min(Math.max(b, 0), 255);
						texturemapLayer[y][(x * 4) + 3] = (byte)255;
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
				for (int x = px; x < px + ((brush.width - 1) * heightmapSizeTextureFactor); x++)
				{
					if ((x >= 0) && (x < width) && (y >= 0) && (y < this.length))
					{
						r = textureR[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						g = textureG[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
						b = textureB[(x - px) % brush.texturemap.width][(y - py) % brush.texturemap.height] & 0xFF;
								
						texturemapLayer[y][(x * 4) + 0] = (byte)Math.min(Math.max(r, 0), 255);
						texturemapLayer[y][(x * 4) + 1] = (byte)Math.min(Math.max(g, 0), 255);
						texturemapLayer[y][(x * 4) + 2] = (byte)Math.min(Math.max(b, 0), 255);
						texturemapLayer[y][(x * 4) + 3] = (byte)255;
					}
				}
			}
	}
	
	public void whiteOutTextureMap()
	{
		long start = System.nanoTime();
		for (int y = 0; y < length; y++)
			Arrays.fill(texturemapLayer[y], (byte)0);
		System.out.println("Done blanking texture ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
}
