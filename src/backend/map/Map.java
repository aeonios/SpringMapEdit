package backend.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import org.eclipse.swt.graphics.Point;

import frontend.render.MapRenderer;
import frontend.render.brushes.BrushTexture;
import frontend.render.brushes.FeatureBrush;
import frontend.render.brushes.HeightBrush;
import frontend.render.brushes.PrefabBrush;
import frontend.render.brushes.TextureBrush;
import frontend.render.brushes.TypeBrush;
import frontend.render.brushes.VegetationBrush;
import frontend.render.features.FeatureMapContainer;
import backend.TerraGenSetup;
import backend.TextureGeneratorSetup;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;
import backend.map.Heightmap;
import backend.map.Texturemap;
import backend.math.Vector2Int;
import backend.sm2.SM2File;

public class Map {
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual heightmap-size. (do not forget to add 1 pixel to heightmap)
	 */
	public static final int springMapSizeHeightmapFactor = 64;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual texture-size.
	 */
	public static final int springMapSizeTexturemapFactor = springMapSizeHeightmapFactor * 8;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual metalmap-size.
	 */
	public static final int springMapSizeMetalmapFactor = springMapSizeHeightmapFactor / 2;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual typemap-size.
	 */
	public static final int springMapSizeTypemapFactor = springMapSizeHeightmapFactor / 2;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual vegetationmap-size.
	 */
	public static final int springMapsizeVegetationmapFactor = springMapSizeHeightmapFactor / 4;
	
	/**
	 * This is the factor we need to multiply the spring mapsize with,<BR>
	 * to get the actual slopemap-size.
	 */
	public static final int springMapsizeSlopemapFactor = springMapSizeHeightmapFactor / 2;
	
	/**
	 * This is the factor we need to multiply the mapsize with,<BR>
	 * to get the actual texture-size.
	 */
	public static final int heightmapSizeTextureFactor = springMapSizeTexturemapFactor / springMapSizeHeightmapFactor;
	
	/**
	 * This is the divisor we need to divide the mapsize by,<BR>
	 * to get the actual metalmap-size.
	 */
	public static final int heightmapSizeMetalmapDivisor = springMapSizeHeightmapFactor / springMapSizeMetalmapFactor;
	
	/**
	 * This is the divisor we need to divide the mapsize by,<BR>
	 * to get the actual typemap-size.
	 */
	public static final int heightmapSizeTypemapDivisor = springMapSizeHeightmapFactor / springMapSizeTypemapFactor;
	
	/**
	 * This is the divisor we need to divide the mapsize by,<BR>
	 * to get the actual typemap-size.
	 */
	public static final int heightmapSizeVegetationmapDivisor = springMapSizeHeightmapFactor / springMapsizeVegetationmapFactor;
	
	/**
	 * This is the virtual (openGL) size of one Tile.<BR>
	 * This is needed for feature Placement.
	 */
	public static int tileSize = 2;
	
	/**
	 * Width and Height in spring units
	 */
	public int width, height;
	public float waterHeight;
	public int maxHeight;
	private String mapName;
	private String mapAuthor;
	private String mapDescription;
	private Vector2Int[] startPostions;
	private String smtFile;
	
	public Heightmap heightmap;
	public Texturemap textureMap;
	public Featuremap featuremap;
	public Metalmap metalmap;
	public Metalmap typemap;
	public byte[][] typeMapBuffer;
	public byte[][] vegetationMap; 
	public int vegetationMapWidth;
	public int vegetationMapHeight;
	public byte[][] slopeMap; 
	public int slopeMapWidth;
	public int slopeMapHeight;

	byte[] metalBuffer = new byte[0]; // So length is readable
	int metalBufferWidth = 0;
	byte[] typeBuffer = new byte[0]; // So length is readable
	int typeBufferWidth = 0;
	byte[] vegetetationBuffer = new byte[0]; // So length is readable
	int vegetetationBufferWidth = 0;
	
	private void resetMap()
	{
		width = 0;
		height = 0;
		heightmap = null;
		metalmap = null;
		typemap = null;
		vegetationMap = null;
		vegetationMapWidth = 0;
		vegetationMapHeight = 0;
		slopeMap = null;
		slopeMapWidth = 0;
		slopeMapHeight = 0;
		textureMap = null;
		featuremap = null;
		
		//quicksave.free();
		
		//Free Resources
		System.gc();
		System.gc();
	}
	
	private void allocateMap()
	{
		heightmap = new Heightmap(height, width);
		metalmap = new Metalmap(height * springMapSizeMetalmapFactor, width * springMapSizeMetalmapFactor);
		typemap = new Metalmap(height * springMapSizeTypemapFactor, width * springMapSizeTypemapFactor);
		
		vegetationMapWidth = width * springMapsizeVegetationmapFactor;
		vegetationMapHeight = height * springMapsizeVegetationmapFactor;
		vegetationMap = new byte[vegetationMapWidth][vegetationMapHeight];
		
		slopeMapWidth = width * springMapsizeSlopemapFactor;
		slopeMapHeight = height * springMapsizeSlopemapFactor;
		slopeMap = new byte[slopeMapHeight][slopeMapWidth];
		
		textureMap = new Texturemap(height, width);
		featuremap = new Featuremap(height, width);
	}
	
	public Map(int height, int width)
	{
		resetMap();
		this.width = width;
		this.height = height;
		allocateMap();
		waterHeight = -1; //maxHeight / 4f;
		mapName = "Unknown Map";
		mapAuthor = "Unknown Author";
		mapDescription = "Empty Description";
		startPostions = new Vector2Int[0];
		textureMap.whiteOutTextureMap();
		featuremap.blankFeatureMap();
	}
	
	public Map(int newHeight, int newWidth, float initialHeight, boolean random)
	{
		try
		{
			//Map(newHeight, newWidth);

			textureMap.whiteOutTextureMap();
			heightmap.setHeightToMap(initialHeight);
			if (random)
			{
				//heightMap.randomizeMap(mes.getTerraGenSetup());
				
			}
			featuremap.blankFeatureMap();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	
	/*public void saveSM2Map(File filename, MapRenderer renderer)
	{
		SM2File sm2fh = new SM2File(this, renderer);
		if (!as.minimapFilename.equals(""))
			sm2fh.setExternalMinimap(as.minimapFilename);
		if (!as.texturemapFilename.equals(""))
			sm2fh.setExternalTexturemap(as.texturemapFilename);
		sm2fh.save(filename);
	}*/
	
	public void saveAllMaps(File filename)//, MapRenderer renderer)
	{		
		String name = filename.getName();
		File dir = filename.getParentFile();
		
		heightmap.saveHeightMap(new File(dir, name + "_Height"), FileFormat.PNG16Bit);
		metalmap.saveMetalMap(new File(dir, name + "_Metal"), true);
		saveTypeMap(new File(dir, name + "_Type"), false);
		saveVegetationMap(new File(dir, name + "_Vegetation"));
		//saveFeatureMap(new File(dir, name + "_Feature"), renderer);
		textureMap.saveTextureMap(new File(dir, name + "_Texture"));
	}
	
	public void loadDataIntoVegetationmap(File vegetationFile)
	{
		try
		{
			Bitmap bitmap = new Bitmap(vegetationFile);
			if ((bitmap.width == width * springMapsizeVegetationmapFactor) && (bitmap.height == height * springMapsizeVegetationmapFactor))
				bitmap.loadDataIntoVegetationmap(vegetationMap);
			else
				throw new IllegalArgumentException("VegetationMapsize must be (heightmapsize - 1) / 4");
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveVegetationMap(File vegetationmapFile)
	{
		new Bitmap(FileFormat.Bitmap8Bit).saveDataFromVegetationmap(vegetationmapFile, vegetationMap, vegetationMapWidth, vegetationMapHeight);
	}
	
	public void saveTypeMap(File typemapFile, boolean use24Bit)
	{
		new Bitmap(use24Bit ? FileFormat.Bitmap24Bit : FileFormat.Bitmap8Bit).saveDataFromTypemap(typemapFile, typemap.getMap(), typemap.getMapWidth(), typemap.getMapLength());
	}
	
	public void loadDataIntoTypemap(File typemapFile)
	{
		try
		{
			Bitmap bitmap = new Bitmap(typemapFile);
			if ((bitmap.width == width * springMapSizeTypemapFactor) && (bitmap.height == height * springMapSizeTypemapFactor))
				bitmap.loadDataIntoTypemap(typemap.getMap());
			else
				throw new IllegalArgumentException("TypeMapsize must be (heightmapsize - 1) / 2");
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
	}
	
	/*public void loadSM2Map(File filename, MapRenderer renderer)
	{		
		new SM2File(this, renderer).load(filename);
	}
	*/
	public void loadAllMaps(File filename, MapRenderer renderer)
	{		
		String name = filename.getName();
		File dir = filename.getParentFile();
		
		//remove extension
		if (name.endsWith("_Height.png"))
			name = name.substring(0, name.length() - 11);
		if (name.endsWith("_Metal.bmp"))
			name = name.substring(0, name.length() - 10);
		if (name.endsWith("_Feature.fmf"))
			name = name.substring(0, name.length() - 12);
		if (name.endsWith("_Type.bmp"))
			name = name.substring(0, name.length() - 9);
		if (name.endsWith("_Vegetation.bmp"))
			name = name.substring(0, name.length() - 15);
		if (name.endsWith("_Texture.bmp"))
			name = name.substring(0, name.length() - 12);
		
		heightmap.loadDataIntoHeightmap(new File(dir, name + "_Height.png"));
		metalmap.loadDataIntoMap(new File(dir, name + "_Metal.bmp"));
		//loadDataIntoFeaturemap(new File(dir, name + "_Feature.fmf"), renderer);
		loadDataIntoTypemap(new File(dir, name + "_Type.bmp"));
		loadDataIntoVegetationmap(new File(dir, name + "_Vegetation.bmp"));
		textureMap.loadDataIntoTexturemap(new File(dir, name + "_Texture.bmp"));
	}

	public void randomizeMap(TerraGenSetup setup)
	{
		Random r = new Random();
		setup.skipSteps = r.nextInt (13);
		setup.maxDisplacement = (r.nextInt (5000) + 1) / 100f;
		setup.displacementRegression = (r.nextInt (1000) + 1) / 1000f;
		heightmap.genStartupHeightmap(setup);
		/*new Edit_Erode_Heightmap_Wet(smeGUI);
		new Edit_Erode_Heightmap_Dry(smeGUI);*/
		//genColorsByHeight(0, 0, null, setup);
	}
	
	public void switchMapAxis()
	{
		heightmap.switchMapAxis();
		textureMap.switchMapAxis();
		metalmap.switchMapAxis();
		typemap.switchMapAxis();
		//vegetationmap.switchMapAxis();
		int t = vegetationMapWidth;
		vegetationMapWidth = vegetationMapHeight;
		vegetationMapHeight = t;
		vegetationMap = new byte[vegetationMapWidth][vegetationMapHeight];
		featuremap.switchMapAxis();
		t = height;
		height = width;
		width = t;
	}

	public void resizeMap(int NewHeight, int NewWidth)
	{
		heightmap.resizeMap(NewHeight * springMapSizeHeightmapFactor + 1, NewWidth * springMapSizeHeightmapFactor + 1);
		textureMap.resizeMap(NewHeight * springMapSizeTexturemapFactor, NewWidth * springMapSizeTexturemapFactor);
		metalmap.resizeMap(NewHeight * springMapSizeMetalmapFactor, NewWidth * springMapSizeMetalmapFactor);
		typemap.resizeMap(NewHeight * springMapSizeTypemapFactor, NewWidth * springMapSizeTypemapFactor);
		//vegetationmap.resizeMap(NewHeight * springMapsizeVegetationmapFactor, NewWidth * springMapsizeVegetationmapFactor);
		vegetationMapWidth = NewWidth * springMapsizeVegetationmapFactor;
		vegetationMapHeight = NewHeight * springMapsizeVegetationmapFactor;
		vegetationMap = new byte[vegetationMapWidth][vegetationMapHeight];
		featuremap.resizeMap(NewWidth * springMapSizeHeightmapFactor, NewHeight * springMapSizeHeightmapFactor);
		this.height = NewHeight;
		this.width = NewWidth;
	}
	
	public void moveMap(int start, int length, int amount, boolean vertically)
	{
		if (amount <= 0 || amount >= length)
			return;
		heightmap.moveMap(start * springMapSizeHeightmapFactor, length * springMapSizeHeightmapFactor + 1, amount * springMapSizeHeightmapFactor, vertically);
		textureMap.moveMap(start * springMapSizeTexturemapFactor, length * springMapSizeTexturemapFactor, amount * springMapSizeTexturemapFactor, vertically);
		metalmap.moveMap(start * springMapSizeMetalmapFactor, length * springMapSizeMetalmapFactor, amount * springMapSizeMetalmapFactor, vertically);
		typemap.moveMap(start * springMapSizeTypemapFactor, length * springMapSizeTypemapFactor, amount * springMapSizeTypemapFactor, vertically);
		//vegetationamp.moveMap(start * springMapsizeVegetationmapFactor, length * springMapsizeVegetationmapFactor, amount * springMapsizeVegetationmapFactor, vertically);
		featuremap.moveMap(start * springMapSizeHeightmapFactor, length * springMapSizeHeightmapFactor, amount * springMapSizeHeightmapFactor, vertically);
	}
	
	public void mirrorMap(int start, int length, int offset, boolean vertically)
	{
		if (true)
			heightmap.mirrorMap(start * springMapSizeHeightmapFactor, length * springMapSizeHeightmapFactor, offset * springMapSizeHeightmapFactor + 1, vertically);
		if (true)
			textureMap.mirrorMap(start * springMapSizeTexturemapFactor, length * springMapSizeTexturemapFactor, offset * springMapSizeTexturemapFactor, vertically);
		if (true)
			metalmap.mirrorMap(start * springMapSizeMetalmapFactor, length * springMapSizeMetalmapFactor, offset * springMapSizeMetalmapFactor, vertically);
		if (true)
			typemap.mirrorMap(start * springMapSizeTypemapFactor, length * springMapSizeTypemapFactor, offset * springMapSizeTypemapFactor, vertically);
		/*if (true)
		vegetationmap.flipMap(start * springMapsizeVegetationmapFactor, length * springMapsizeVegetationmapFactor, offset * springMapsizeVegetationmapFactor, vertically);*/
		if (true)
			featuremap.mirrorMap(start * springMapSizeHeightmapFactor, length * springMapSizeHeightmapFactor, offset * springMapSizeHeightmapFactor, vertically);
	}
	
	public void flipMap(int start, int length, int offset, boolean vertically)
	{
		if (true)
			heightmap.flipMap(start * springMapSizeHeightmapFactor, length * springMapSizeHeightmapFactor, offset * springMapSizeHeightmapFactor + 1, vertically);
		if (true)
			textureMap.flipMap(start * springMapSizeTexturemapFactor, length * springMapSizeTexturemapFactor, offset * springMapSizeTexturemapFactor, vertically);
		if (true)
			metalmap.flipMap(start * springMapSizeMetalmapFactor, length * springMapSizeMetalmapFactor, offset * springMapSizeMetalmapFactor, vertically);
		if (true)
			typemap.flipMap(start * springMapSizeTypemapFactor, length * springMapSizeTypemapFactor, offset * springMapSizeTypemapFactor, vertically);
		/*if (true)
			vegetationmap.flipMap(start * springMapSizeMetalmapFactor, length * springMapSizeMetalmapFactor, offset * springMapSizeMetalmapFactor, vertically);*/
		if (true)
			featuremap.flipMap(start * springMapSizeHeightmapFactor, length * springMapSizeHeightmapFactor, offset * springMapSizeHeightmapFactor, vertically);
	}
	
	public void copy(int px, int py, PrefabBrush brush)
	{
		int height = brush.getHeight();
		int width = brush.getWidth();
		if (true)
			heightmap.copy(px, py, (height * springMapSizeHeightmapFactor) + 1, (width * springMapSizeHeightmapFactor) + 1);
		if (true)
			textureMap.copy(px * heightmapSizeTextureFactor, py * heightmapSizeTextureFactor, brush);
		if (true)
			metalmap.copy(py / heightmapSizeMetalmapDivisor, px / heightmapSizeMetalmapDivisor, height / heightmapSizeMetalmapDivisor, width / heightmapSizeMetalmapDivisor);
		if (true)
			typemap.copy(py / heightmapSizeTypemapDivisor, px / heightmapSizeTypemapDivisor, height / heightmapSizeTypemapDivisor, width / heightmapSizeTypemapDivisor);
		/*if (true)
			vegetationMap.copy(py / heightmapSizeVegetationmapDivisor, px / heightmapSizeVegetationmapDivisor, height / heightmapSizeVegetationmapDivisor, width / heightmapSizeVegetationmapDivisor);*/
		//if (true)
		//	featuremap.copy(px, py, (height * springMapSizeHeightmapFactor) + 1, (width * springMapSizeHeightmapFactor) + 1);
	}
	
	public void paste(int px, int py, PrefabBrush brush)
	{
		int height = brush.getHeight();
		int width = brush.getWidth();
		/*float amount = brush.getStrength();
		float[][] pattern = brush.getPattern().getPattern();*/
		if (true)
			heightmap.paste(px, py, brush);
		if (true)
			textureMap.paste(py * heightmapSizeTextureFactor, px * heightmapSizeTextureFactor, brush);
		if (true)
			metalmap.paste(py / heightmapSizeMetalmapDivisor, px / heightmapSizeMetalmapDivisor, height / heightmapSizeMetalmapDivisor, width / heightmapSizeMetalmapDivisor);
		if (true)
			typemap.paste(py / heightmapSizeTypemapDivisor, px / heightmapSizeTypemapDivisor, height / heightmapSizeTypemapDivisor, width / heightmapSizeTypemapDivisor);
		
		//if (true)
		//	featuremap.paste(px, py, brush);
	}
	
	public void setToTypemap(int px, int py, TypeBrush brush, boolean invert)
	{
		int type = brush.typeID;
		if (invert)
			type = 0;
		px = (px / heightmapSizeTypemapDivisor);
		py = (py / heightmapSizeTypemapDivisor);
		for (int y = py; y < py + (brush.height / heightmapSizeTypemapDivisor); y++)
			for (int x = px; x < px + (brush.width / heightmapSizeTypemapDivisor); x++)
				if ((x >= 0) && (x < typemap.getMapWidth()) && (y >= 0) && (y < typemap.getMapLength()))
					typemap.getMap()[y][x] = (byte)Math.min(Math.max(type, 0), 255);
	}
	
	public void setToVegetationmap(int px, int py, VegetationBrush brush, boolean invert)
	{
		int type = brush.typeID;
		if (invert)
			type = 0;
		px = (px / heightmapSizeVegetationmapDivisor);
		py = (py / heightmapSizeVegetationmapDivisor);
		for (int y = py; y < py + (brush.height / heightmapSizeVegetationmapDivisor); y++)
			for (int x = px; x < px + (brush.width / heightmapSizeVegetationmapDivisor); x++)
				if ((x >= 0) && (x < vegetationMapWidth) && (y >= 0) && (y < vegetationMapHeight))
					vegetationMap[x][y] = (byte)Math.min(Math.max(type, 0), 1);
	}
	
	/**
	 * Levels gives the number of brushes used.<BR>
	 * transitions should be:<BR>
	 * level0 end height,<BR>
	 * level1 start height, level1 end height,<BR>
	 * level2 start height, level2 end height,<BR>
	 * level3 start height, level3 end height,<BR>
	 * level4 start height<BR>
	 * <BR>
	 * So there should be 2 * (levels - 1) entries.<BR>
	 * If level end and next level start do not match,<BR>
	 * both levels will be blended within this margin.<BR>
	 * brushedFlat will be used if steepNess of triangle is smaller than given steepNess for Level<BR>
	 * otherwise brushesSteep will be used.<BR>
	 * level1 flatSteepness<BR>
	 * level1 steepSteepness<BR>
	 * level2 flatSteepnes<BR>
	 * level2 steepSteepness<BR>
	 * steepnesses between flatSteepness and steepSteepness will be blended<BR>
	 * @param brushesFlat
	 * @param brushesSteep
	 * @param transitions
	 * @param steepNess
	 * @param levels
	 */
	public void genColorsByHeight(int px, int py, TextureBrush brush, TextureGeneratorSetup setup)
	{
		//TextureGeneratorSetup setup = mes.getTextureGeneratorSetup();
		if (setup == null)
			return;
		
		long start = System.nanoTime();
		final int oneTenthsOfHeight = Math.max(textureMap.texturemapLength / 10, 1);
		
		int levels = setup.levels;
		BrushTexture[] brushesFlat = setup.brushesFlat;
		BrushTexture[] brushesSteep = setup.brushesSteep;
		float[] heightTransitions = setup.heightTransitions;
		float[] steepTransitions = setup.steepTransitions;
		BrushTexture brush1Flat = brushesFlat[0];
		BrushTexture brush2Flat = brushesFlat[0];
		BrushTexture brush1Steep = brushesSteep[0];
		BrushTexture brush2Steep = brushesSteep[0];
		int xT, yT;
		int r1, g1, b1, r2, g2, b2;
		int r1s, g1s, b1s, r2s, g2s, b2s;
		float h1, h2, h3, h4, fractX, fractY, heightYLeft, heightYRight, height, steep;
		int transitionCount = (2 * (levels - 1));
		float blendFactorBrush2Flat = 0.5f;
		float blendFactorBrush2Steep = 0;
		int width = textureMap.getWidth(); //heightmap.width;
		int length = textureMap.texturemapLength;
		if (px < 0)
		{
			px = 0;
			if (brush != null)
				width += px;
		}
		px *= heightmapSizeTextureFactor;
		if (py < 0)
		{
			py = 0;
			if (brush != null)
				length += py;
		}
		py *= heightmapSizeTextureFactor;
		if (px >= textureMap.getWidth() || py >= textureMap.texturemapLength)
			return;
		if (brush != null)
		{
			length = brush.getHeight() * heightmapSizeTextureFactor;
			width = brush.getWidth() * heightmapSizeTextureFactor;
			if (length + py > textureMap.texturemapLength)
				length = textureMap.texturemapLength - py;
			if (width + px > textureMap.getWidth())
				width = textureMap.getWidth() - px;
			//if (x < textureMap.textureMapWidth) && (y < textureMap.textureMapHeight))
		}
		for (int y = py; y < py + length; y++)
		{
			fractY = (y % heightmapSizeTextureFactor) / (float)heightmapSizeTextureFactor;
		
			for (int x = px; x < px + width; x++)
			{
				fractX = (x % heightmapSizeTextureFactor) / (float)heightmapSizeTextureFactor;
					
				//calculate height of edge vertices
				xT = x / heightmapSizeTextureFactor;
				yT = y / heightmapSizeTextureFactor;
				
				h1 = heightmap.getHeigth(xT + 0, yT + 0);
				h2 = heightmap.getHeigth(xT + 0, yT + 1);
				h3 = heightmap.getHeigth(xT + 1, yT + 1);
				h4 = heightmap.getHeigth(xT + 1, yT + 0);
								
				//Steepness
				steep = heightmap.getSmoothedSteepness(xT, yT, fractX, fractY);
					
				//calculate height of actual texel
				heightYLeft =  ((1 - fractY) * h1) + (fractY * h2);
				heightYRight = ((1 - fractY) * h4) + (fractY * h3);
				height = ((1 - fractX) * heightYLeft) + (fractX * heightYRight);
					
				//calculate which brushes to use
				if (height <= heightTransitions[0])
				{
					brush1Flat = brushesFlat[0];
					brush2Flat = brushesFlat[0];
					brush1Steep = brushesSteep[0];
					brush2Steep = brushesSteep[0];
					if (steep < steepTransitions[0])
						blendFactorBrush2Steep = 0;
					else if (steep > steepTransitions[1])
						blendFactorBrush2Steep = 1;
					else
						blendFactorBrush2Steep = 1 - (steepTransitions[1] - steep) / (steepTransitions[1] - steepTransitions[0]);
				}
				else if (height >= heightTransitions[transitionCount - 1])
				{
					brush1Flat = brushesFlat[levels - 1];
					brush2Flat = brushesFlat[levels - 1];
					brush1Steep = brushesSteep[levels - 1];
					brush2Steep = brushesSteep[levels - 1];
					if (steep < steepTransitions[((levels - 1) * 2)])
						blendFactorBrush2Steep = 0;
					else if (steep > steepTransitions[((levels - 1) * 2) + 1])
						blendFactorBrush2Steep = 1;
					else
						blendFactorBrush2Steep = 1 - (steepTransitions[((levels - 1) * 2) + 1] - steep) / (steepTransitions[((levels - 1) * 2) + 1] - steepTransitions[((levels - 1) * 2)]);
				}
				else
				{
					boolean notFound = true;
					int lastIndex = 0;
					int index = 1;
					while ((index < transitionCount) && notFound)
					{
						if (height <= heightTransitions[index])
						{
							brush1Flat = brushesFlat[(lastIndex + 1) / 2];
							brush2Flat = brushesFlat[(index + 1) / 2];
							brush1Steep = brushesSteep[(lastIndex + 1) / 2];
							brush2Steep = brushesSteep[(index + 1) / 2];
							blendFactorBrush2Flat = ((height - heightTransitions[lastIndex]) / (heightTransitions[index] - heightTransitions[lastIndex]));
							
							if (steep < steepTransitions[(((index - 1) / 2) * 2)])
								blendFactorBrush2Steep = 0;
							else if (steep > steepTransitions[(((index - 1) / 2) * 2) + 1])
								blendFactorBrush2Steep = 1;
							else
								blendFactorBrush2Steep = 1 - (steepTransitions[(((index - 1) / 2) * 2) + 1] - steep) / (steepTransitions[(((index - 1) / 2) * 2) + 1] - steepTransitions[(((index - 1) / 2) * 2)]);
							
							notFound = false;
						}
						lastIndex++;
						index++;
					}
				}
					
				//TODO: allow scaling of texture
				
				//blend color from brushes
				r1 = (brush1Flat.textureR[x % brush1Flat.width][y % brush1Flat.height] & 0xFF);
				g1 = (brush1Flat.textureG[x % brush1Flat.width][y % brush1Flat.height] & 0xFF);
				b1 = (brush1Flat.textureB[x % brush1Flat.width][y % brush1Flat.height] & 0xFF);
				r1s = (brush1Steep.textureR[x % brush1Steep.width][y % brush1Steep.height] & 0xFF);
				g1s = (brush1Steep.textureG[x % brush1Steep.width][y % brush1Steep.height] & 0xFF);
				b1s = (brush1Steep.textureB[x % brush1Steep.width][y % brush1Steep.height] & 0xFF);
				r1 = (int)(((r1 * (1 - blendFactorBrush2Steep)) + (r1s * blendFactorBrush2Steep)));
				g1 = (int)(((g1 * (1 - blendFactorBrush2Steep)) + (g1s * blendFactorBrush2Steep)));
				b1 = (int)(((b1 * (1 - blendFactorBrush2Steep)) + (b1s * blendFactorBrush2Steep)));
					
				r2 = (brush2Flat.textureR[x % brush2Flat.width][y % brush2Flat.height] & 0xFF);
				g2 = (brush2Flat.textureG[x % brush2Flat.width][y % brush2Flat.height] & 0xFF);
				b2 = (brush2Flat.textureB[x % brush2Flat.width][y % brush2Flat.height] & 0xFF);
				r2s = (brush2Steep.textureR[x % brush2Steep.width][y % brush2Steep.height] & 0xFF);
				g2s = (brush2Steep.textureG[x % brush2Steep.width][y % brush2Steep.height] & 0xFF);
				b2s = (brush2Steep.textureB[x % brush2Steep.width][y % brush2Steep.height] & 0xFF);
				r2 = (int)(((r2 * (1 - blendFactorBrush2Steep)) + (r2s * blendFactorBrush2Steep)));
				g2 = (int)(((g2 * (1 - blendFactorBrush2Steep)) + (g2s * blendFactorBrush2Steep)));
				b2 = (int)(((b2 * (1 - blendFactorBrush2Steep)) + (b2s * blendFactorBrush2Steep)));
					
				//set color to texmap
				textureMap.textureMap[y][(x * 3) + 0] = (byte)(((r1 * (1 - blendFactorBrush2Flat)) + (r2 * blendFactorBrush2Flat)));
				textureMap.textureMap[y][(x * 3) + 1] = (byte)(((g1 * (1 - blendFactorBrush2Flat)) + (g2 * blendFactorBrush2Flat)));
				textureMap.textureMap[y][(x * 3) + 2] = (byte)(((b1 * (1 - blendFactorBrush2Flat)) + (b2 * blendFactorBrush2Flat)));
			}
			//Status output
			if ((y % oneTenthsOfHeight) == 0)
				System.out.print("#");
		}
		System.out.println(" Done generating textureMap ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
}
