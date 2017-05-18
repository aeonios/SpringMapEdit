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
 * SM2File.java 
 * Created on 28.09.2008
 * by Heiko Schmitt
 */
package backend.sm2;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

import backend.FastMath;
import backend.FileHandler;
import backend.SpringMapEdit;
import backend.dds.DDSFile;
import backend.io.LERandomAccessFile;
import backend.map.Featuremap;

import frontend.gui.ErrorHandler;
import frontend.render.MapRenderer;
import frontend.render.features.FeatureManager;
import frontend.render.features.FeatureMapContainer;

/**
 * @author Heiko Schmitt
 *
 */
public class SM2File
{
	private SpringMapEdit sme;
	private MapRenderer renderer;
	
	private LERandomAccessFile outStream;
	private LERandomAccessFile inStream;
	
	//file data
	private String filename;
	
	//External data
	private String externalMinimapFilename;
	private String externalTexturemapFilename;
	
	//static data
	private final int squareSize = 8;
	private final int texelPerSquare = 8;
	private final int tileSize = 32;
	public static final int compressedMinimapSize = 699048;
	public static final int compressedTileSize = 680;
	
	//File locations we need later...
	private long heightmapPtr;
	private long typemapPtr;
	private long tilemapPtr;
	private long minimapPtr;
	private long metalmapPtr;
	private long vegetationmapPtr;
	private long featurePtr;
	private long numTilesPtr;
	
	//data we need multiple times
	private int numTileFiles;
	private int numDifferentTiles;
	private int[] tileIDs;
	private int[] tileCountPerFile;
	private List<Integer>[] tileIDmap; //contains all tiles which use given tileID.  "tileIDmap[tileID]"
	private String[] tileFilenames;
	private int curTileID;
	private File tileFileDir;
	
	public SM2File(SpringMapEdit sme, MapRenderer renderer)
	{
		this.sme = sme;
		this.renderer = renderer;
		externalMinimapFilename = null;
		externalTexturemapFilename = null;
	}
	
	/**
	 * Saves the map to specified smt and smf files.
	 * @param smfName
	 */
	public void save(File smfName, File smtName)
	{
		filename = smtName.getName();
		File smdFilename = new File(FileHandler.removeExtension(new File (smfName.getAbsolutePath())) + ".smd");
		smfName = new File(smfName.getAbsolutePath());
		smtName = new File(smtName.getAbsolutePath());

		//NOTE: order is important!
		saveSMD(smdFilename);
		if (smtName.exists())
			smtName.delete();
		saveSMT(smtName); //SMT needs to be saved before SMF
		if (smfName.exists())
			smfName.delete();
		saveSMF(smfName);
		//7zip up files (need to look up how to build 7zip archives. Compressing a single filestream is easy...)
	}
	
	/**
	 * Loads the map with smf and smt files specified.
	 * @param smfName
 	 * @param smtName
	 */
	public void load(File smfName, File smtName)
	{
		filename = smtName.getName();
		tileFileDir = smtName.getParentFile();
		
		//NOTE: order is important!
		loadSMF(new File(smfName.getAbsolutePath()));
		loadSMT(new File(smtName.getAbsolutePath()));
	}

	public void loadSMD(File filename)
	{
		
	}
	
	public void loadSMF(File filename)
	{
		try
		{
			inStream = new LERandomAccessFile(filename, "r");
			
			loadSMFHeader();
			loadSMFHeightmap();
			loadSMFTypemap();
			loadSMFMetalmap();
			loadSMFTilemap();
			loadSMFFeatures(renderer.getFeatureManager(), sme.map.featuremap);
//			loadSMFMinimap(); //not needed currently
			
			inStream.close();
			inStream = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			//new ErrorHandler();
		}
	}
	
	public void loadSMT(File filename)
	{
		try
		{
			long start = System.nanoTime();
			
			//Start at tileID 0
			curTileID = 0;
			
			for (int i = 0; i < numTileFiles; i++)
			{
				inStream = new LERandomAccessFile(new File(tileFileDir, tileFilenames[i]), "r");
				
				loadSMTHeader(i);
				loadSMTTiles(i);
				
				inStream.close();
				inStream = null;
			}
			
			System.out.println(" Done loading " + numDifferentTiles + " tiles from " + numTileFiles + " files in " + ((System.nanoTime() - start) / 1000000) + " ms )");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveSMFFeaturesToFile(File featureFile, FeatureManager featureManager, Featuremap map)
	{
		String filename = featureFile.getName();
		File dir = featureFile.getParentFile();
		if (!filename.endsWith(".fmf"))
			filename = filename + ".fmf";
		
		File file = new File(dir, filename);
		
		if (file.exists())
			file.delete();
		
		//Setup pointers
		featurePtr = 0;
		
		//Save
		try
		{
			outStream = new LERandomAccessFile(file, "rw");
			
			saveSMFFeatures(featureManager, map);
			
			outStream.close();
			outStream = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadSMFFeaturesFromFile(File featureFile, FeatureManager featureManager, Featuremap map)
	{
		//Setup pointers
		featurePtr = 0;
		
		//Load
		try
		{
			inStream = new LERandomAccessFile(featureFile, "r");
			
			loadSMFFeatures(featureManager, map);
			
			inStream.close();
			inStream = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveSMD(File filename)
	{
		/*
		 * save minimal default smd? we will make some parameters adjustable from editor.
		 * especially sunlight direction will be cool
		 * 
		 * TODO Write a "TDF"-Parser, then use it here...
		 * 
		 */
		
	}
	
	private void saveSMT(File filename)
	{
		try
		{
			outStream = new LERandomAccessFile(filename, "rw");
			
			saveSMTHeader();
			saveSMTTiles();
			
			outStream.close();
			outStream = null;
			System.out.println("Done saving map tiles to file: " + filename);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void saveSMF(File filename)
	{
		try
		{
			outStream = new LERandomAccessFile(filename, "rw");
			
			saveSMFHeader();
			saveSMFHeightmap();
			saveSMFTypemap();
			saveSMFMetalmap();
			saveSMFTilemap();
			saveSMFFeatures(renderer.getFeatureManager(), sme.map.featuremap);
			saveSMFMinimap();
			
			outStream.close();
			outStream = null;
			System.out.println("Done saving map to file: " + filename);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
//	struct SMFHeader {
//		char magic[16];      ///< "spring map file\0"
//		int version;         ///< Must be 1 for now
//		int mapid;           ///< Sort of a GUID of the file, just set to a random value when writing a map
//
//		int mapx;            ///< Must be divisible by 128
//		int mapy;            ///< Must be divisible by 128
//		int squareSize;      ///< Distance between vertices. Must be 8
//		int texelPerSquare;  ///< Number of texels per square, must be 8 for now
//		int tilesize;        ///< Number of texels in a tile, must be 32 for now
//		float minHeight;     ///< Height value that 0 in the heightmap corresponds to	
//		float maxHeight;     ///< Height value that 0xffff in the heightmap corresponds to
//
//		int heightmapPtr;    ///< File offset to elevation data (short int[(mapy+1)*(mapx+1)])
//		int typeMapPtr;      ///< File offset to typedata (unsigned char[mapy/2 * mapx/2])
//		int tilesPtr;        ///< File offset to tile data (see MapTileHeader)
//		int minimapPtr;      ///< File offset to minimap (always 1024*1024 dxt1 compresed data with 9 mipmap sublevels)
//		int metalmapPtr;     ///< File offset to metalmap (unsigned char[mapx/2 * mapy/2])
//		int featurePtr;      ///< File offset to feature data (see MapFeatureHeader)
//
//		int numExtraHeaders; ///< Numbers of extra headers following main header
//	};
	
//	struct ExtraHeader {
//		int size; ///< Size of extra header
//		int type; ///< Type of extra header
//	};
	private void saveSMFHeader()
	{
		try
		{
			//Magic Bytes
			outStream.writeString("spring map file", true);
			
			//version
			outStream.writeInt(1);
			
			//mapid
			//TODO maybe make it settable inside GUI
			Random r = new Random();
			outStream.writeInt(r.nextInt());
			
			outStream.writeInt(sme.map.width * SpringMapEdit.springMapsizeHeightmapFactor);
			
			outStream.writeInt(sme.map.height * SpringMapEdit.springMapsizeHeightmapFactor);
			
			outStream.writeInt(squareSize);
			outStream.writeInt(texelPerSquare);
			outStream.writeInt(tileSize);
			
			//If water is disabled, offset it quite high, to disable water in Spring
			float heightOffset = (sme.map.waterHeight >= 0) ? 0 : 500;
			
			//minHeight
			outStream.writeFloat(0 - (sme.map.waterHeight) + heightOffset);
			
			//maxHeight
			outStream.writeFloat((sme.map.maxHeight) - (sme.map.waterHeight) + heightOffset);
			
			//Map Pointers... (will be set later, store location)
			heightmapPtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			typemapPtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			tilemapPtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			minimapPtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			metalmapPtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			featurePtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			//Number of Extra Headers
			outStream.writeInt(1);
			
			////////
			//Extra Header for vegetation/grass:
			////////
			//Size of data
			outStream.writeInt(((sme.map.width * SpringMapEdit.springMapsizeVegetationmapFactor) * (sme.height * SpringMapEdit.springMapsizeVegetationmapFactor)) + 4);
			
			//Type of extra header
			outStream.writeInt(1);
			
			//Pointer to Vegetation Data (this is already part of the data...)
			vegetationmapPtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			saveSMFVegetationMap();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveSMFVegetationMap()
	{
		try
		{
			//Setup start pointer
			int currentPos = (int) outStream.getFilePointer();
			outStream.seek(vegetationmapPtr);
			outStream.writeInt(currentPos);
			outStream.seek(currentPos);
			
			int width = sme.map.vegetationMapWidth;
			int height = sme.map.vegetationMapHeight;
			byte[] scanline = new byte[width];
			byte[][] vegetationMap = sme.map.vegetationMap;
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
					scanline[x] = (byte) vegetationMap[x][y];
				outStream.write(scanline, 0, width);
				
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done saving vegetationmap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveSMFHeightmap()
	{
		try
		{
			//Setup start pointer
			int currentPos = (int) outStream.getFilePointer();
			outStream.seek(heightmapPtr);
			outStream.writeInt(currentPos);
			outStream.seek(currentPos);
			
			int width = (sme.width * SpringMapEdit.springMapsizeHeightmapFactor) + 1;
			int height = (sme.height * SpringMapEdit.springMapsizeHeightmapFactor) + 1;
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			float[][] map = sme.map.heightmap.getHeightMap();
			int scanlineSize = width * 2;
			byte[] scanline = new byte[scanlineSize];
			for (int y = 0; y < height; y++)
			{
				int currentByte = 0;
				for (int x = 0; x < width; x++)
				{
					int v = FastMath.round(0xFFFF * map[y][x]);
					scanline[currentByte] = (byte)(v & 0xFF);
					scanline[currentByte + 1] = (byte)((v >> 8) & 0xFF);
					
					currentByte += 2;
				}
				outStream.write(scanline, 0, scanlineSize);
				
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done saving heightmap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveSMFTypemap()
	{
		try
		{
			//Setup start pointer
			int currentPos = (int) outStream.getFilePointer();
			outStream.seek(typemapPtr);
			outStream.writeInt(currentPos);
			outStream.seek(currentPos);
			
			int width = sme.map.typemap.getMapWidth();
			int height = sme.map.typemap.getMapLength();
			byte[] scanline = new byte[width];
			byte[][] typemap = sme.map.typemap.getMap();
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
					scanline[x] = typemap[y][x];
				outStream.write(scanline, 0, width);

				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done saving typemap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveSMFMetalmap()
	{
		try
		{
			//Setup start pointer
			int currentPos = (int) outStream.getFilePointer();
			outStream.seek(metalmapPtr);
			outStream.writeInt(currentPos);
			outStream.seek(currentPos);
			
			int width = sme.map.metalmap.getMapWidth();
			int height = sme.map.metalmap.getMapLength();
			byte[] scanline = new byte[width];
			byte[][] metalMap = sme.map.metalmap.getMap();
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
					scanline[x] = metalMap[y][x];
				outStream.write(scanline, 0, width);
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done saving metalmap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
//	struct MapTileHeader
//	{
//		int numTileFiles; ///< Number of tile files to read in (usually 1)
//		int numTiles;     ///< Total number of tiles
//	};
	private void saveSMFTilemap()
	{
		try
		{
			//Setup start pointer
			int currentPos = (int) outStream.getFilePointer();
			outStream.seek(tilemapPtr);
			outStream.writeInt(currentPos);
			outStream.seek(currentPos);
			
			//NOTE: SMT save code creates the table we store here. (also does duplicate check)
			
			int width = (sme.width * SpringMapEdit.springMapsizeHeightmapFactor) * texelPerSquare / tileSize;
			int height = (sme.height * SpringMapEdit.springMapsizeHeightmapFactor) * texelPerSquare / tileSize;
			
			//NumTileFiles (always write everything into one file)
			outStream.writeInt(1);
			//Total Number of tiles
			outStream.writeInt(numDifferentTiles);
			
			//Number of tiles in our only tilefile
			outStream.writeInt(numDifferentTiles);
			//tile filename
			outStream.writeString(filename, true);
			
			//tilemap data
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int scanlineSize = width * 4;
			byte[] scanline = new byte[scanlineSize];
			int x,y, currentByte;
			int currentTile = 0;
			for (y = 0; y < height; y++)
			{
				currentByte = 0;
				for (x = 0; x < width; x++)
				{
					scanline[currentByte] = (byte)(tileIDs[currentTile] & 0xFF);
					scanline[currentByte + 1] = (byte)((tileIDs[currentTile] >> 8) & 0xFF);
					scanline[currentByte + 2] = (byte)((tileIDs[currentTile] >> 16) & 0xFF);
					scanline[currentByte + 3] = (byte)((tileIDs[currentTile] >> 24) & 0xFF);
					
					currentByte += 4;
					currentTile++;
				}
				outStream.write(scanline, 0, scanlineSize);
				
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done saving tilemap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

//	struct MapFeatureHeader 
//	{
//		int numFeatureType;
//		int numFeatures;
//	};
	
//	struct MapFeatureStruct
//	{
//		int featureType;    ///< Index to one of the strings above
//		float xpos;         ///< X coordinate of the feature
//		float ypos;         ///< Y coordinate of the feature (height)
//		float zpos;         ///< Z coordinate of the feature
//
//		float rotation;     ///< Orientation of this feature (-32768..32767 for full circle)
//		float relativeSize; ///< Not used at the moment keep 1
//	};

	private void saveSMFFeatures(FeatureManager featureManager, Featuremap map)
	{
		try
		{
			//Setup start pointer
			int currentPos = (int) outStream.getFilePointer();
			outStream.seek(featurePtr);
			outStream.writeInt(currentPos);
			outStream.seek(currentPos);
			
			//Count features & featureTypes
			int numFeatures = 0;
			int numFeatureTypes = 0;
			LinkedHashMap<Integer, Integer> types = new LinkedHashMap<Integer, Integer>();
			ArrayList<FeatureMapContainer> features;
			Iterator<FeatureMapContainer> it;
			FeatureMapContainer feature;
			for (int i = 0; i < map.featureBlockCount; i++)
			{
				features = map.featureList[i];
				
				it = features.iterator();
				while (it.hasNext())
				{
					feature = it.next();
					if (!types.containsKey(feature.featureID))
						types.put(feature.featureID, numFeatureTypes++);
					numFeatures++;
				}
			}
			
			outStream.writeInt(numFeatureTypes);
			outStream.writeInt(numFeatures);
			
			Iterator<Integer> it2 = types.keySet().iterator();
			while (it2.hasNext())
				outStream.writeString(featureManager.getFeatureName(it2.next()), true);
			
			//Features
			final int oneTenthsOfHeight = Math.max(map.featureBlockCount / 10, 1);
			for (int i = 0; i < map.featureBlockCount; i++)
			{
				features = map.featureList[i];
				
				it = features.iterator();
				while (it.hasNext())
				{
					feature = it.next();
					
					//FeatureType (index into table above)
					outStream.writeInt(types.get(feature.featureID));
					
					//X, Y, Z
					outStream.writeFloat(feature.x / SpringMapEdit.tileSize * squareSize);
					outStream.writeFloat(feature.y / SpringMapEdit.tileSize * squareSize);
					outStream.writeFloat(feature.z / SpringMapEdit.tileSize * squareSize);
					
					//Rotation (-32768..32767 for full circle)
					outStream.writeFloat((feature.rotY / 360) * 0xFFFF);
					
					//Size (not used)
					outStream.writeFloat(1);
				}
				
				if ((i % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			
			System.out.println(" Done saving feature data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//1024*1024 dxt1 compresed data with 9 mipmap sublevels
	private void saveSMFMinimap()
	{
		try
		{
			//Setup start pointer
			int currentPos = (int) outStream.getFilePointer();
			outStream.seek(minimapPtr);
			outStream.writeInt(currentPos);
			outStream.seek(currentPos);
			
			DDSFile ddsFile = null;
			if (externalMinimapFilename != null)
			{
				ddsFile = new DDSFile();
				ddsFile.open(new File(externalMinimapFilename));
				
				//Check size of DDS
				if ((1024 != ddsFile.getWidth()) || (1024 != ddsFile.getHeight()))
				{
					System.out.println("External DDS has wrong size. Expected 1024x1024, found: " + ddsFile.getWidth() + "x" + ddsFile.getHeight() + ". Using internal minimap now.");
					ddsFile.close();
					ddsFile = null;
				}
			}
			
			if (ddsFile == null)
				outStream.write(renderer.getCompressedMinimapData(), 0, compressedMinimapSize);
			else
				ddsFile.writeDataToStream(outStream, compressedMinimapSize);
			
			System.out.println(" Done saving minimap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
//	struct TileFileHeader
//	{
//		char magic[16];      ///< "spring tilefile\0"
//		int version;         ///< Must be 1 for now
//
//		int numTiles;        ///< Total number of tiles in this file
//		int tileSize;        ///< Must be 32 for now
//		int compressionType; ///< Must be 1 (= dxt1) for now
//	};
	private void saveSMTHeader()
	{
		try
		{
			//Magic Bytes
			outStream.writeString("spring tilefile", true);
			
			//version
			outStream.writeInt(1);
			
			//numTiles
			numTilesPtr = outStream.getFilePointer();
			outStream.writeInt(0);
			
			//tileSize
			outStream.writeInt(tileSize);
			
			//compressionType
			outStream.writeInt(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveSMTTiles()
	{
		try
		{
			long start = System.nanoTime();
			/*
			 * Store tiledata, and generate the tileid-lookuptable for saveSMF
			 * 
			 * 1. get compressed data for tile
			 * 2. generate checlsum for tile
			 * 3. check if identical tile is already in hashmap
			 * 3.yes: set ID of identical tile in tileIDs array
			 * 3.no:  use nextTileID as ID in tileIDs array, and increment nextTileID
			 */
			DDSFile ddsFile = null;
			if (externalTexturemapFilename != null)
			{
				ddsFile = new DDSFile();
				ddsFile.open(new File(externalTexturemapFilename));
				
				//Check size of DDS
				if ((sme.map.textureMap.getWidth() != ddsFile.getWidth()) || (sme.map.textureMap.getLength() != ddsFile.getHeight()))
				{
					System.out.println("External DDS has wrong size. Expected " + sme.map.textureMap.getWidth() + "x" + sme.map.textureMap.getLength() + "," +
							" found: " + ddsFile.getWidth() + "x" + ddsFile.getHeight() + ". Using internal texture now.");
					ddsFile.close();
					ddsFile = null;
				}
			}
			
			//Used for creating cheksums/id's for each tile.
			CRC32 crc32 = new CRC32();
			
			//This map uses above id to check for duplicates
			LinkedHashMap<Long, ArrayList<TileBucket>> checksumMap = new LinkedHashMap<Long, ArrayList<TileBucket>>();
			
			int width = sme.map.textureMap.getWidth() / tileSize;
			int height = sme.map.textureMap.getLength() / tileSize;
			tileIDs = new int[width * height];
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int nextTileTextureID = 0;
			int currentTilePosition = 0;
			byte[] data = new byte[compressedTileSize];
			byte[] data2 = new byte[compressedTileSize];
			int x, y;
			long crcValue;
			TileBucket bucket = null;
			ArrayList<TileBucket> newList;
			boolean equalTileFound;
			Iterator<TileBucket> it;
			renderer.initCompressedTileData();
			for (y = 0; y < height; y++)
			{
				for (x = 0; x < width; x++)
				{
					if (ddsFile == null)
						renderer.getCompressedTileData(data, x * tileSize, y * tileSize, tileSize);
					else
						ddsFile.getCompressedTileData(data, x * tileSize, y * tileSize, tileSize);
						
					crc32.reset();
					crc32.update(data, 0, compressedTileSize);
					crcValue = crc32.getValue();
					equalTileFound = false;
					if (checksumMap.containsKey(crcValue))
					{
						//Compare tiles directly, instead of relying on no-collision crc32 ;)
						it = checksumMap.get(crcValue).iterator();
						while (!equalTileFound && it.hasNext())
						{
							bucket = it.next();
							if (ddsFile == null)
								renderer.getCompressedTileData(data2, (bucket.tilePosition % width) * tileSize, (bucket.tilePosition / width) * tileSize, tileSize);
							else
								ddsFile.getCompressedTileData(data2, (bucket.tilePosition % width) * tileSize, (bucket.tilePosition / width) * tileSize, tileSize);
							
							equalTileFound = Arrays.equals(data, data2);
						}
						if (equalTileFound)
						{
							//Same CRC, same content
							tileIDs[currentTilePosition] = bucket.tileTextureID;
						}
						else
						{
							//Same CRC, but different content
							outStream.write(data, 0, compressedTileSize);
							checksumMap.get(crcValue).add(new TileBucket(currentTilePosition, nextTileTextureID));
							tileIDs[currentTilePosition] = nextTileTextureID;
							nextTileTextureID++;							
						}
					}
					else
					{
						outStream.write(data, 0, compressedTileSize);
						newList = new ArrayList<TileBucket>(2);
						newList.add(new TileBucket(currentTilePosition, nextTileTextureID));
						checksumMap.put(crcValue, newList);
						tileIDs[currentTilePosition] = nextTileTextureID;
						nextTileTextureID++;
					}
					currentTilePosition++;
				}
				
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			renderer.cleanCompressTileData();
			if (ddsFile != null)
				ddsFile.close();
			
			//Write total number of tiles
			numDifferentTiles = nextTileTextureID;
			outStream.seek(numTilesPtr);
			outStream.writeInt(numDifferentTiles);
			
			System.out.println(" Done saving tiledata (" + ((width * height) - nextTileTextureID) + " identical from " + (width * height) + " tiles) ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	////////////////////////////
	//Loading Routines
	////////////////////////////
	private void loadSMFHeader()
	{
		try
		{
			String magic = inStream.readString();
			if (!magic.equals("spring map file"))
				throw new IOException("header wrong inside .smf: not \"spring map file\" Was: " + magic);
			
			int version = inStream.readInt();
			if (version != 1)
				throw new IOException("version wrong inside .smf: not 1. Was: " + version);
			
			//mapid
			inStream.skip(4);
			
			int width = inStream.readInt() / SpringMapEdit.springMapsizeHeightmapFactor;
			int height = inStream.readInt() / SpringMapEdit.springMapsizeHeightmapFactor;
			
			sme.newMap(width, height, 0, false);
			
			//TODO different squaresize/texelPerSquare/tileSize should be no problem... maybe do not quit?
			int _squareSize = inStream.readInt();
			if (_squareSize != squareSize)
				throw new IOException("squareSize wrong inside .smf: not " + squareSize + " Was: " + _squareSize);
			
			int _texelPerSquare = inStream.readInt();
			if (_texelPerSquare != texelPerSquare)
				throw new IOException("texelPerSquare wrong inside .smf: not " + texelPerSquare + " Was: " + _texelPerSquare);
			
			int _tileSize = inStream.readInt();
			if (_tileSize != tileSize)
				throw new IOException("tileSize wrong inside .smf: not " + tileSize + " Was: " + _tileSize);
			
			float minHeight = inStream.readFloat();
			float maxHeight = inStream.readFloat();

			sme.map.maxHeight = FastMath.round((maxHeight - minHeight));
			
			float waterHeight = -1;
			if (minHeight <= 0)
				waterHeight = ((0 - minHeight) / (maxHeight - minHeight)) * sme.map.maxHeight;
			sme.map.waterHeight = waterHeight;
			
			//Map Pointers...
			heightmapPtr = inStream.readInt();
			typemapPtr = inStream.readInt();
			tilemapPtr = inStream.readInt();
			minimapPtr = inStream.readInt();
			metalmapPtr = inStream.readInt();
			featurePtr = inStream.readInt();
			
			////////
			//Extra Header for vegetation/grass:
			////////
			
			int extraHeaderCount = inStream.readInt();
			for (int i = 0; i < extraHeaderCount; i++)
			{
				int size = inStream.readInt();
				if (inStream.readInt() == 1)
				{
					vegetationmapPtr = inStream.readInt();
					loadSMFVegetationMap();
				}
				else
					inStream.skip(size);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMFVegetationMap()
	{
		try
		{
			//Go to vegetation map data
			inStream.seek(vegetationmapPtr);
			
			int width = sme.map.vegetationMapWidth;
			int height = sme.map.vegetationMapHeight;
			
			byte[] scanline = new byte[width];
			byte[][] vegetationMap = sme.map.vegetationMap;
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int x, y;
			for (y = 0; y < height; y++)
			{
				inStream.read(scanline, 0, width);
				
				for (x = 0; x < width; x++)
					vegetationMap[x][y] = scanline[x];
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done loading vegetationmap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMFHeightmap()
	{
		try
		{
			//Go to heightmap data
			inStream.seek(heightmapPtr);
			
			int width = (sme.width * SpringMapEdit.springMapsizeHeightmapFactor) + 1;
			int height = (sme.height * SpringMapEdit.springMapsizeHeightmapFactor) + 1;
			
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			float[][] map = sme.map.heightmap.getHeightMap();
			int scanlineSize = width * 2;
			byte[] scanline = new byte[scanlineSize];
			int x,y, currentByte;
			for (y = 0; y < height; y++)
			{
				inStream.read(scanline, 0, scanlineSize);
				
				currentByte = 0;
				for (x = 0; x < width; x++)
				{
					map[y][x] = ((float)(scanline[currentByte + 0] & 0xFF) + ((scanline[currentByte + 1] & 0xFF) << 8)) / 0xFFFF;
					currentByte += 2;
				}
				
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done loading heightmap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMFTypemap()
	{
		try
		{
			//Go to Typemap data
			inStream.seek(typemapPtr);
			
			int width = sme.map.typemap.getMapWidth();
			int height = sme.map.typemap.getMapLength();
			byte[] scanline = new byte[width];
			byte[][] typeMap = sme.map.typemap.getMap();
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			for (int y = 0; y < height; y++)
			{
				inStream.read(scanline, 0, width);
				
				for (int x = 0; x < width; x++)
					typeMap[y][x] = scanline[x];
					
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done loading typemap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMFMetalmap()
	{
		try
		{
			//Go to metalmap data
			inStream.seek(metalmapPtr);
			
			int width = sme.map.metalmap.getMapWidth();
			int height = sme.map.metalmap.getMapLength();
			byte[] scanline = new byte[width];
			byte[][] metalMap = sme.map.metalmap.getMap();
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int y;
			for (y = 0; y < height; y++)
			{
				inStream.read(scanline, 0, width);
				System.arraycopy(scanline, 0, metalMap[y], 0, width);
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			System.out.println(" Done loading metalmap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMFTilemap()
	{
		try
		{
			//Go to tilemap data
			inStream.seek(tilemapPtr);
			
			int width = (sme.map.width * SpringMapEdit.springMapsizeHeightmapFactor) * texelPerSquare / tileSize;
			int height = (sme.map.height * SpringMapEdit.springMapsizeHeightmapFactor) * texelPerSquare / tileSize;
			
			numTileFiles = inStream.readInt();
			//Total Number of tiles
			numDifferentTiles = inStream.readInt();
			
			//TileFile Headers
			tileCountPerFile = new int[numTileFiles];
			tileFilenames = new String[numTileFiles];
			for (int i = 0; i < numTileFiles; i++)
			{
				//Number of tiles in this tilefile
				tileCountPerFile[i] = inStream.readInt(); 
				
				//tile filename
				tileFilenames[i] = inStream.readString();
			}
			
			//tilemap data
			tileIDs = new int[width * height];
			tileIDmap = (List<Integer>[])new ArrayList[numDifferentTiles];
			final int oneTenthsOfHeight = Math.max(height / 10, 1);
			int scanlineSize = width * 4;
			byte[] scanline = new byte[scanlineSize];
			int currentTile = 0;
			for (int y = 0; y < height; y++)
			{
				inStream.read(scanline, 0, scanlineSize);
				
				int currentByte = 0;
				for (int x = 0; x < width; x++)
				{
					tileIDs[currentTile] = (scanline[currentByte] & 0xFF) + ((scanline[currentByte + 1] & 0xFF) << 8) + ((scanline[currentByte + 2] & 0xFF) << 16) + ((scanline[currentByte + 3] & 0xFF) << 24);
					
					if (tileIDmap[tileIDs[currentTile]] == null)
						tileIDmap[tileIDs[currentTile]] = new ArrayList<Integer>();
					tileIDmap[tileIDs[currentTile]].add(currentTile);
					
					currentByte += 4;
					currentTile++;
				}
				
				if ((y % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			
			System.out.println(" Done loading tilemap data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMFFeatures(FeatureManager featureManager, Featuremap featuremap)
	{
		try
		{
			//Go to feature Header
			inStream.seek(featurePtr);
			
			//Number of FeatureTypes
			int numFeatureTypes = inStream.readInt();
			
			//Number of Features
			int numFeatures = inStream.readInt();
			
			//Read featureNames
			String[] typeNames = new String[numFeatureTypes]; 
			for (int i = 0; i < numFeatureTypes; i++)
				typeNames[i] = inStream.readString();

			final int oneTenthsOfHeight = Math.max(numFeatures / 10, 1);
			float x, z, rotY;
			int featureIndex, featureID;
			for (int i = 0; i < numFeatures; i++)
			{
				featureIndex = inStream.readInt();
				
				//Convert FeatureName to FeatureID
				featureID = featureManager.getFeatureID(typeNames[featureIndex]);
				
				//Feature Data
				x = inStream.readFloat() * SpringMapEdit.tileSize / squareSize;
				inStream.readFloat();
				z = inStream.readFloat() * SpringMapEdit.tileSize / squareSize;
				rotY = ((inStream.readFloat() / 0xFFFF) * 360);
				
				//Size (not used)
				inStream.readFloat();
				
				if (featureID >= 0)	
					featuremap.addFeature(x, z, rotY, featureID);
				else
					System.out.println("SM2File.loadSMFFeatures: Could not find feature with name: " + typeNames[featureIndex]);
				
				if ((i % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			
			System.out.println(" Done loading feature data");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMTHeader(int tileFileIndex)
	{
		try
		{
			//Magic Bytes
			String magic = inStream.readString();
			if (!magic.equals("spring tilefile"))
				throw new IOException("header wrong inside " + tileFilenames[tileFileIndex] + ": not \"spring tilefile\" Was: " + magic);
			
			//version
			int version = inStream.readInt();
			if (version != 1)
				throw new IOException("version wrong inside " + tileFilenames[tileFileIndex] + ": not 1. Was: " + version);
			
			//numTiles
			int tileCount = inStream.readInt();
			if (version != 1)
				throw new IOException("tileCount wrong inside " + tileFilenames[tileFileIndex] + ": not " + tileCountPerFile[tileFileIndex] + ". Was: " + tileCount);
			
			//tileSize
			int _tileSize = inStream.readInt();
			if (_tileSize != tileSize)
				throw new IOException("tileSize wrong inside " + tileFilenames[tileFileIndex] + ": not " + tileSize + " Was: " + _tileSize);
			
			//compressionType
			int compressionType = inStream.readInt();
			if (compressionType != 1)
				throw new IOException("compressionType wrong inside " + tileFilenames[tileFileIndex] + ": not 1 Was: " + compressionType);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadSMTTiles(int tileFileIndex)
	{
		try
		{
			/*
			 * 1. Load a tile
			 * 2. copy tile into texture-array to all positions given by tileIDs
			 */
			int width = sme.map.textureMap.getWidth() / tileSize;
			//int height = sme.textureMapHeight / tileSize;
			final int oneTenthsOfHeight = Math.max(numDifferentTiles / 10, 1);
			byte[] compressedData = new byte[compressedTileSize];
			byte[] uncompressedData = new byte[tileSize * tileSize * 3];
			ByteBuffer compressed = ByteBuffer.wrap(compressedData);
			ByteBuffer uncompressed = ByteBuffer.wrap(uncompressedData);
			int tileScanlineSize = tileSize * 3;
			
			renderer.initDecompressTileData();
			for (int i = 0; i < tileCountPerFile[tileFileIndex]; i++)
			{
				//Retrieve data
				inStream.read(compressedData, 0, compressedTileSize);
				
				//Decompress data
				renderer.getDecompressedTileData(compressed, uncompressed, tileSize);
				
				//Now copy data to all tiles which use this ID
				List<Integer> tileList = tileIDmap[curTileID];
				Iterator<Integer> it = tileList.iterator();
				while (it.hasNext())
				{
					int tile = it.next();
					
					int yDst = (tile / width) * tileSize;
					int offsetSrc = 0;
					int xOffsetDst = ((tile % width) * tileSize) * 3;
					for (int y = 0; y < tileSize; y++)
					{
						System.arraycopy(uncompressedData, offsetSrc, sme.map.textureMap.textureMap[yDst], xOffsetDst, tileScanlineSize);
						offsetSrc += tileScanlineSize;
						yDst++;
					}
				}
				curTileID++;
				
				//Status output
				if ((curTileID % oneTenthsOfHeight) == 0)
					System.out.print("#");
			}
			renderer.cleanDecompressTileData();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void setExternalMinimap(String minimapFilename)
	{
		externalMinimapFilename = minimapFilename;
	}

	public void setExternalTexturemap(String texturemapFilename)
	{
		externalTexturemapFilename = texturemapFilename;
	}
	
}
