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
 * MapRenderer.java 
 * Created on 03.07.2008
 * by Heiko Schmitt
 */
package frontend.render;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import backend.FastMath;
import backend.SpringMapEdit;
import backend.map.Featuremap;
import backend.math.Vector3;
import backend.math.Vector3Math;
import backend.sm2.SM2File;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import frontend.gui.CameraPosition;
import frontend.render.brushes.Brush;
import frontend.render.brushes.BrushPattern;
import frontend.render.brushes.BrushTexture;
import frontend.render.features.FeatureManager;
import frontend.render.features.FeatureMapContainer;

/**
 * @author Heiko Schmitt
 *
 */
public class MapRenderer
{
	//Global stuff
	private SpringMapEdit sme;
	private GL staticGl;
	private GLU glu;
	private GLUT glut;
	private AppSettings rs;
	private ShaderManager shaderManager;
	private FeatureManager featureManager;
	
	//Buffer vars
	private int blockSizeinTiles;
	private int blockSizeinPixels;
	private int blockSizeinMetalmapPixels;
	private int blockSizeinTypemapPixels;
	private int blockSizeinVegetationmapPixels;
	private int blockSizeinSlopemapPixels;
	private int pixelCountPerBlock;
	private int blockCount;
	private int mapWidthInBlocks;
	private int mapHeightInBlocks;
	
	private boolean[][] isGeometryCached;
	private boolean[] isTextureCached;
	private boolean[] isFeatureCached;
	private int[][] vboID;
	private int[][] displayListID;
	private int[] textureID;
	private ArrayList<FeatureMapContainer>[] featureList;
	
	private int LODLEVELS;
	private int[] triCountPerBlock;
	private int[] verticesPerBlock;
		
	//VBO cache
	private FloatBuffer[] vbo;
	private Vector3 nullVector;
	
	//Feature sorting
	private int featuresToRenderCount;
	private FeatureMapContainer[] featuresToRender;
	
	//Textures
	private ByteBuffer textureData;
	private byte[] tmpScanline;
	private int tempTexture;
	
	//Framebuffer for reflection
	private int frameBufferObjectID = -1;
	private int renderBufferObjectID = -1;
	private int reflectionMapID = -1;
	private int refractionMapID = -1;
	private int normalMapID = -1;
	private int dudvMapID = -1;
	
	//Framebuffer for features
	private int featureFBOID = -1;
	private int featureRBOID = -1;
	private int featureTexID = -1;
	
	//Skybox
	private int skyboxTexID = -1; 
		
	//Renderer state
	private boolean switchPolyMode = false;
	private boolean wireFrameMode = false;
	private int blocksCreatedThisFrame;
	private int texturesCreatedThisFrame;
	private int featureBlocksCreatedThisFrame;
	private int featuresCreatedThisFrame;
	
	//Texture mode
	public enum MapMode
	{
		TextureMap,
		SlopeMap,
		MetalMap,
		FeatureMap,
		TypeMap,
		VegetationMap
	}
	
	//animation
	private Vector3 sunPosition;
		
	public MapRenderer(SpringMapEdit sme, AppSettings as) throws IllegalArgumentException
	{
		this.sme = null;
		this.rs = as;
		setMaxFeaturesToDisplay(rs.maxFeaturesToDisplay);
		setSpringMapEdit(sme);
	}
	
	public void camPosChangedNotify()
	{

	}
	
	public void camViewChangedNotify()
	{

	}
	
	public void setMaxFeaturesToDisplay(int maxFeaturesToDisplay)
	{
		this.featuresToRender = new FeatureMapContainer[maxFeaturesToDisplay];
	}
	
	public void setSpringMapEdit(SpringMapEdit sme)
	{
		if (sme == null)
			return;
		releaseSpringMapEdit();
		this.sme = sme;
		featureManager = sme.featureManager;
		this.setBlockSizeinTiles(rs.blockSize);
	}
	
	private void releaseSpringMapEdit()
	{
		for (int l = 0; l < LODLEVELS; l++)
		{
			//OpenGL Buffers
			for (int i = 0; i < blockCount; i++)
			{
				//Vertexbuffer Objects
				if (vboID[l][i] >= 0)
			    	staticGl.glDeleteBuffers(1, vboID[l], i);
				if (displayListID[l][i] >= 0)
					staticGl.glDeleteLists(displayListID[l][i], 1);
			}
			//Temporary Buffers
			this.vbo[l] = null;
		}
		
		for (int i = 0; i < blockCount; i++)
		{
			if (textureID[i] >= 0)
				staticGl.glDeleteTextures(1, textureID, i);
			featureList[i] = null;
		}
		
		this.vbo = null;
		this.textureData = null;
		this.tmpScanline = null;
						
		this.isGeometryCached = null;
		this.isTextureCached = null;
		this.isFeatureCached = null;
		this.vboID = null;
		this.displayListID = null;
		this.textureID = null;
		this.featureList = null;
		
		System.gc();
		System.gc();
	}
	
	public int getBlockSizeinTiles()
	{
		return blockSizeinTiles;
	}
	
	@SuppressWarnings("unchecked")
	public void setBlockSizeinTiles(int blockSizeinTiles) throws IllegalArgumentException
	{
		this.blockSizeinTiles = blockSizeinTiles;
		
		int mapWidthInTiles = sme.map.heightmap.getHeightmapWidth() - 1;
		int mapHeightInTiles = sme.map.heightmap.getHeightmapLength() - 1;
		if ((mapWidthInTiles % blockSizeinTiles) != 0)
			throw new IllegalArgumentException("(MapWidth-1) not dividable by " + blockSizeinTiles);
		if ((mapHeightInTiles % blockSizeinTiles) != 0)
			throw new IllegalArgumentException("(MapHeight-1) not dividable by " + blockSizeinTiles);
		
		this.mapWidthInBlocks = mapWidthInTiles / blockSizeinTiles;
		this.mapHeightInBlocks = mapHeightInTiles / blockSizeinTiles;
		this.blockCount = mapWidthInBlocks * mapHeightInBlocks;
		
		this.blockSizeinPixels = sme.map.textureMap.getWidth() / mapWidthInBlocks;
		this.blockSizeinMetalmapPixels = sme.map.metalmap.getMapWidth() / mapWidthInBlocks;
		this.blockSizeinTypemapPixels = sme.map.typemap.getMapWidth() / mapWidthInBlocks;
		this.blockSizeinVegetationmapPixels = sme.map.vegetationMapWidth / mapWidthInBlocks;
		this.blockSizeinSlopemapPixels = sme.map.slopeMapWidth / mapWidthInBlocks;
		this.pixelCountPerBlock = blockSizeinPixels * blockSizeinPixels;
		
		int maxScanlineSize = Math.max(Math.max(Math.max(blockSizeinMetalmapPixels, blockSizeinTypemapPixels), blockSizeinVegetationmapPixels), blockSizeinSlopemapPixels);
		this.tmpScanline = new byte[maxScanlineSize * 3];
		
		LODLEVELS = 4;
		triCountPerBlock = new int[LODLEVELS];
		verticesPerBlock = new int[LODLEVELS];
		
		this.vbo = new FloatBuffer[LODLEVELS];
		for (int l = 0; l < LODLEVELS; l++)
		{
			int blockSize = rs.blockSize / FastMath.pow(2, l);
			triCountPerBlock[l] = blockSize * blockSize * 2;
			
			//Initialize temp buffer
			verticesPerBlock[l] = triCountPerBlock[l] + (4 * blockSize);
			this.vbo[l] = FloatBuffer.allocate(verticesPerBlock[l] * 8); //3Vertex 3Normal 2TexCoord
		}
		
		this.nullVector = new Vector3(0, 1, 0); 
		this.textureData = ByteBuffer.allocate(pixelCountPerBlock * 3);
				
		//Initialize caches
		this.isGeometryCached = new boolean[LODLEVELS][blockCount];
		this.isTextureCached = new boolean[blockCount];
		this.isFeatureCached = new boolean[blockCount];
		this.vboID = new int[LODLEVELS][blockCount];
		this.displayListID = new int[LODLEVELS][blockCount];
		this.textureID = new int[blockCount];
		this.featureList = (ArrayList<FeatureMapContainer>[])new ArrayList[blockCount];
		
		for (int l = 0; l < LODLEVELS; l++)
			for (int i = 0; i < blockCount; i++)
			{
				this.isGeometryCached[l][i] = false;
				this.vboID[l][i] = -1;
				this.displayListID[l][i] = -1;
			}
		for (int i = 0; i < blockCount; i++)
		{
			this.isTextureCached[i] = false;
			this.isFeatureCached[i] = false;
			this.textureID[i] = -1;
			this.featureList[i] = new ArrayList<FeatureMapContainer>();
		}
	}
	
	public void toggleWireframeMode()
	{
		switchPolyMode = true;
	}
	
	public void setVsync(boolean vsync)
	{
		staticGl.setSwapInterval(vsync ? 1 : 0);
	}
	
	private void createTexture(GL gl, int index)
	{
		/*if (texturesCreatedThisFrame >= rs.maxTexturesPerFrame)
			return;*/
		
		long start = System.nanoTime();
		
		//Remove old texture
		if (textureID[index] >= 0)
			gl.glDeleteTextures(1, textureID, index);
		
		//Copy texture data from map
		textureData.clear();

		byte[][] textureMap = sme.map.textureMap.getMap();
		switch (rs.mapMode)
		{
			case SlopeMap:
			{
				int factor = blockSizeinPixels / blockSizeinSlopemapPixels;
				byte[][] slopeMap = sme.map.slopeMap;
				byte[][] slopeColors = sme.slopeMapColorTable;
				int[] slopes = sme.slopes;
				final int scanlineSize = blockSizeinSlopemapPixels * 3;
				int x, y, currentByte;
				int xStart = ((index % mapWidthInBlocks) * blockSizeinSlopemapPixels);
		    	int yStart = ((index / mapWidthInBlocks) * blockSizeinSlopemapPixels);
		    	int slopeType;
				for (y = yStart; y < (yStart + blockSizeinSlopemapPixels); y++)
				{
					//Create scanline
					currentByte = 0;
					for (x = xStart; x < (xStart + blockSizeinSlopemapPixels); x++)
					{
						slopeType = 3;
						if ((slopeMap[y][x] & 0xFF) <= slopes[0])
							slopeType = 0;
						else if ((slopeMap[y][x] & 0xFF) <= slopes[1])
							slopeType = 1;
						else if ((slopeMap[y][x] & 0xFF) <= slopes[2])
							slopeType = 2;
						if (rs.blendTextureMap)
						{
							tmpScanline[currentByte + 0] = (byte)(((textureMap[y * factor][(x * factor * 3) + 0] & 0xFF) + (slopeColors[slopeType][0] & 0xFF)) / 2);
							tmpScanline[currentByte + 1] = (byte)(((textureMap[y * factor][(x * factor * 3) + 1] & 0xFF) + (slopeColors[slopeType][1] & 0xFF)) / 2);
							tmpScanline[currentByte + 2] = (byte)(((textureMap[y * factor][(x * factor * 3) + 2] & 0xFF) + (slopeColors[slopeType][2] & 0xFF)) / 2);
						}
						else
						{
							tmpScanline[currentByte + 0] = slopeColors[slopeType][0];
							tmpScanline[currentByte + 1] = slopeColors[slopeType][1];
							tmpScanline[currentByte + 2] = slopeColors[slopeType][2];
						}
						currentByte += 3;
					}
					//Copy whole scanline at once
					textureData.put(tmpScanline, 0, scanlineSize);
				}
				break;
			}
			case VegetationMap:
			{
				int factor = blockSizeinPixels / blockSizeinVegetationmapPixels;
				byte[][] vegetationMap = sme.map.vegetationMap;
				int x, y, currentByte;
				int xStart = ((index % mapWidthInBlocks) * blockSizeinVegetationmapPixels);
		    	int yStart = ((index / mapWidthInBlocks) * blockSizeinVegetationmapPixels);
				for (y = yStart; y < (yStart + blockSizeinVegetationmapPixels); y++)
				{
					//Create scanline
					currentByte = 0;
					for (x = xStart; x < (xStart + blockSizeinVegetationmapPixels); x++)
					{
						if (rs.blendTextureMap)
						{
							tmpScanline[currentByte + 0] = (byte)((textureMap[y * factor][(x * factor * 3) + 0] & 0xFF) / 2);
							tmpScanline[currentByte + 1] = (byte)(((textureMap[y * factor][(x * factor * 3) + 1] & 0xFF) + (((vegetationMap[x][y] & 0xFF) == 1) ? 255 : 0)) / 2);
							tmpScanline[currentByte + 2] = (byte)((textureMap[y * factor][(x * factor * 3) + 2] & 0xFF) / 2);
						}
						else
						{
							tmpScanline[currentByte + 0] = (byte) 0;
							tmpScanline[currentByte + 1] = (((vegetationMap[x][y] & 0xFF) == 1) ? (byte) 255 : (byte) 0);
							tmpScanline[currentByte + 2] = (byte) 0;
						}
						currentByte += 3;
					}
					//Copy whole scanline at once
					textureData.put(tmpScanline, 0, blockSizeinVegetationmapPixels * 3);
				}
				break;
			}
			case TypeMap:
			{
				int factor = blockSizeinPixels / blockSizeinTypemapPixels;
				byte[][] typeMap = sme.map.typemap.getMap();
				byte[][] typeMapColors = sme.typeMapColorTable;
				int currentByte;
				int xStart = ((index % mapWidthInBlocks) * blockSizeinTypemapPixels);
		    	int yStart = ((index / mapWidthInBlocks) * blockSizeinTypemapPixels);
				for (int y = yStart; y < (yStart + blockSizeinTypemapPixels); y++)
				{
					//Create scanline
					currentByte = 0;
					for (int x = xStart; x < (xStart + blockSizeinTypemapPixels); x++)
					{
						if (rs.blendTextureMap)
						{
							tmpScanline[currentByte + 0] = (byte)(((textureMap[y * factor][(x * factor * 3) + 0] & 0xFF) + (typeMapColors[typeMap[y][x] & 0xFF][0] & 0xFF)) / 2);
							tmpScanline[currentByte + 1] = (byte)(((textureMap[y * factor][(x * factor * 3) + 1] & 0xFF) + (typeMapColors[typeMap[y][x] & 0xFF][1] & 0xFF)) / 2);
							tmpScanline[currentByte + 2] = (byte)(((textureMap[y * factor][(x * factor * 3) + 2] & 0xFF) + (typeMapColors[typeMap[y][x] & 0xFF][2] & 0xFF)) / 2);
						}
						else
						{
							tmpScanline[currentByte + 0] = typeMapColors[typeMap[y][x] & 0xFF][0];
							tmpScanline[currentByte + 1] = typeMapColors[typeMap[y][x] & 0xFF][1];
							tmpScanline[currentByte + 2] = typeMapColors[typeMap[y][x] & 0xFF][2];
						}
						currentByte += 3;
					}
					//Copy whole scanline at once
					textureData.put(tmpScanline, 0, blockSizeinTypemapPixels * 3);
				}
				break;
			}
			case MetalMap:
			{
				int factor = blockSizeinPixels / blockSizeinMetalmapPixels;
				byte[][] metalMap = sme.map.metalmap.getMap();
				int x, y, currentByte;
				int xStart = ((index % mapWidthInBlocks) * blockSizeinMetalmapPixels);
		    	int yStart = ((index / mapWidthInBlocks) * blockSizeinMetalmapPixels);
				for (y = yStart; y < (yStart + blockSizeinMetalmapPixels); y++)
				{
					//Create scanline
					currentByte = 0;
					for (x = xStart; x < (xStart + blockSizeinMetalmapPixels); x++)
					{
						if (rs.blendTextureMap)
						{
							tmpScanline[currentByte + 0] = (byte)((textureMap[y * factor][(x * factor * 3) + 0] & 0xFF) / 2);
							tmpScanline[currentByte + 1] = (byte)(((textureMap[y * factor][(x * factor * 3) + 1] & 0xFF) + (metalMap[y][x] & 0xFF)) / 2);
							tmpScanline[currentByte + 2] = (byte)((textureMap[y * factor][(x * factor * 3) + 2] & 0xFF) / 2);
						}
						else
						{
							tmpScanline[currentByte + 0] = (byte) 0;
							tmpScanline[currentByte + 1] = (byte) metalMap[y][x];
							tmpScanline[currentByte + 2] = (byte) 0;
						}
						currentByte += 3;
					}
					//Copy whole scanline at once
					textureData.put(tmpScanline, 0, blockSizeinMetalmapPixels * 3);
				}
				break;
			}
			/*case FeatureMap:
			{
				int factor = blockSizeinPixels / blockSizeinMetalmapPixels;
				byte[][] metalMap = sme.metalmap.getMap();
				final int scanlineSize = blockSizeinMetalmapPixels * 3;
				int x, y, currentByte;
				int xStart = ((index % mapWidthInBlocks) * blockSizeinMetalmapPixels);
		    	int yStart = ((index / mapWidthInBlocks) * blockSizeinMetalmapPixels);
				for (y = yStart; y < (yStart + blockSizeinMetalmapPixels); y++)
				{
					//Create scanline
					currentByte = 0;
					for (x = xStart; x < (xStart + blockSizeinMetalmapPixels); x++)
					{
						if (rs.blendTextureMap)
						{
							tmpScanline[currentByte + 0] = (byte)((textureMap[y * factor][(x * factor * 3) + 0] & 0xFF) / 2);
							tmpScanline[currentByte + 1] = (byte)(((textureMap[y * factor][(x * factor * 3) + 1] & 0xFF) + (metalMap[x][y] & 0xFF)) / 2);
							tmpScanline[currentByte + 2] = (byte)((textureMap[y * factor][(x * factor * 3) + 2] & 0xFF) / 2);
						}
						else
						{
							tmpScanline[currentByte + 0] = (byte) 0;
							tmpScanline[currentByte + 1] = (byte) metalMap[x][y];
							tmpScanline[currentByte + 2] = (byte) 0;
						}
						currentByte += 3;
					}
					//Copy whole scanline at once
					textureData.put(tmpScanline, 0, scanlineSize);
				}
				break;
			}*/
			default: //case TextureMap
			{
				int y;
				int xStart = ((index % mapWidthInBlocks) * blockSizeinPixels);
		    	int yStart = ((index / mapWidthInBlocks) * blockSizeinPixels);
				for (y = yStart; y < yStart + blockSizeinPixels; y++)
				{
					//Copy whole scanline at once
					try { // TODO Temporary hack for a problem that should not happen
						textureData.put(textureMap[y], xStart * 3, blockSizeinPixels * 3);
					} catch (IndexOutOfBoundsException e) {
						break;
					}
				}
				break;
			}
		}
		textureData.flip();
		
		//Generate new texture
		gl.glGenTextures(1, textureID, index);
	    gl.glBindTexture(GL.GL_TEXTURE_2D, textureID[index]);
	    if (rs.filterTextures)
	    {
	    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
	    }
	    else
	    {
	    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	    }
		
	    int size;
	    switch (rs.mapMode)
	    {
	    	case SlopeMap:      
	    		size = blockSizeinSlopemapPixels;
	    		break;
	    	case VegetationMap: 
	    		size = blockSizeinVegetationmapPixels;
	    		break;
	    	case TypeMap:
	    		size = blockSizeinTypemapPixels;
	    		break;
	    	case MetalMap:
	    		size = blockSizeinMetalmapPixels;
	    		break;
	    	case FeatureMap:
	    		size = blockSizeinMetalmapPixels;
	    		break;
	    	default:
	    		size = blockSizeinPixels;
	    	break; //TextureMap
	    }
	    
	    if (rs.compressTextures && (rs.mapMode == MapMode.TextureMap))
			try {
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT, size, size, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, textureData);
				texturesCreatedThisFrame++;
				isTextureCached[index] = true;
			} catch (IndexOutOfBoundsException e) {
				 // TODO Temporary hack for a problem that should not happen
			}
	    else
			try {
				gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB8, size, size, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, textureData);
				texturesCreatedThisFrame++;
				isTextureCached[index] = true;
			} catch (IndexOutOfBoundsException e) {

			}
		
		if (rs.outputPerfDebug)
			System.out.println("Done creating Texture Block ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	private void createFeatureBlock(int index)
	{
		if (featureBlocksCreatedThisFrame >= rs.maxFeatureBlocksPerFrame)
			return;
		
		long start = System.nanoTime();
		Featuremap featuremap = sme.map.featuremap;
		
		//Remove old Feature
		featureList[index].clear();
		
		//Calculate boundaries in Tiles
		int xStartInTiles = ((index % mapWidthInBlocks) * blockSizeinTiles);
    	int yStartInTiles = ((index / mapWidthInBlocks) * blockSizeinTiles);
		int xEndInTiles = xStartInTiles + blockSizeinTiles - 1;
		int yEndInTiles = yStartInTiles + blockSizeinTiles - 1;
		
		//Calculate boundaries in SME-Blocks
		int xStartInBlocksSME = xStartInTiles / featuremap.featureBlockSizeInTiles;
		int xEndInBlocksSME = xEndInTiles / featuremap.featureBlockSizeInTiles;
		
		//Calculate boundaries in tilesize-space
		float minX = xStartInTiles * rs.quadSize;
		float minY = yStartInTiles * rs.quadSize;
		float maxX = ((xEndInTiles + 1) * rs.quadSize);
		float maxY = ((yEndInTiles + 1) * rs.quadSize);
		
		for (int y = yStartInTiles / featuremap.featureBlockSizeInTiles; y <= yEndInTiles / featuremap.featureBlockSizeInTiles; y++)
    		for (int x = xStartInBlocksSME; x <= xEndInBlocksSME; x++)
    		{
    			Iterator<FeatureMapContainer> it = featuremap.featureList[y * featuremap.featureMapWidthInBlocks + x].iterator();
    			while (it.hasNext())
    			{
    				FeatureMapContainer feature = it.next();
    				if ((feature.x >= minX) && (feature.z >= minY) && (feature.x < maxX) && (feature.z < maxY))
    				{
    					//Set Height appropriate to heightmap
    					feature.y = sme.map.heightmap.getHeightMap()[FastMath.round(feature.z / rs.quadSize)][FastMath.round(feature.x / rs.quadSize)] * sme.map.maxHeight/4f;
    					featureList[index].add(feature);
    				}
    			}
    		}
		
    	featureBlocksCreatedThisFrame++;
    	isFeatureCached[index] = true;
    	
    	if (rs.outputPerfDebug)
    		System.out.println("Done creating Feature Block ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	private void updateFeatureBlockHeights(int index)
	{
		float[][] heightMap = sme.map.heightmap.getHeightMap();
		Iterator<FeatureMapContainer> it = featureList[index].iterator();
		FeatureMapContainer cont;
		while (it.hasNext())
		{
			cont = it.next();
			cont.y = heightMap[FastMath.round(cont.z / rs.quadSize)][FastMath.round(cont.x / rs.quadSize)] * sme.map.maxHeight/4f;
		}
	}
	
	private void updateSlopemapBlock(int index)
	{
		float[][] map = sme.map.heightmap.getHeightMap();
		int x, y;
		Vector3 v1, v2, v3, v4, vN1, vN2;
		int lodSkip = 2;
		int lodNegativeExtendTileSize = (lodSkip-1) * rs.quadSize;
		float maxHeight = sme.map.maxHeight/4f; //
		long start = System.nanoTime();
			    	
		int xStart = ((index % mapWidthInBlocks) * blockSizeinTiles) + lodSkip;
    	int yStart = ((index / mapWidthInBlocks) * blockSizeinTiles) + lodSkip;
		for (y = yStart; y < (yStart + blockSizeinTiles); y += lodSkip)
		{
			x = xStart;
			
			try { // TODO Temporary hack for a problem that should not happen
				v1 = new Vector3(-rs.quadHalfSize - lodNegativeExtendTileSize + (x * rs.quadSize),
						map[y - lodSkip][x - lodSkip] * maxHeight, -rs.quadHalfSize - lodNegativeExtendTileSize + (y * rs.quadSize));
				v2 = new Vector3(-rs.quadHalfSize - lodNegativeExtendTileSize + (x * rs.quadSize),
						map[y][x - lodSkip] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));
			} catch (ArrayIndexOutOfBoundsException e) {
				break;
			}
			for (; x < (xStart + blockSizeinTiles); x += lodSkip)
			{
				v3 = new Vector3(rs.quadHalfSize + (x * rs.quadSize),
						map[y - lodSkip][x] * maxHeight, -rs.quadHalfSize - lodNegativeExtendTileSize + (y * rs.quadSize));
				v4 = new Vector3(rs.quadHalfSize + (x * rs.quadSize), map[y][x] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));
				
				vN1 = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3)).normalize();
				vN2 = Vector3Math.crossProduct(Vector3Math.subVectors(v2, v4), Vector3Math.subVectors(v4, v3)).normalize();
				
				//Update Slopemap
				sme.map.slopeMap[(y - lodSkip) / 2][(x - lodSkip) / 2] = (byte)((1 - ((vN1.vector[1] + vN2.vector[1]) / 2)) * 255);
			    
				//Copy last 2 vectors to new first ones
				v1 = v3;
				v2 = v4;
			}
		}
		if (rs.outputPerfDebug)
			System.out.println("Done updating complete Slopemap ( " + ((System.nanoTime() - start) / 1000000) + " ms )");
	}
	
	private void createBlock(GL gl, int index)
	{
		/*if (blocksCreatedThisFrame >= rs.maxBlocksPerFrame)
			return;*/
		float[][] heightmap = sme.map.heightmap.getHeightMap();
		int x, xStart, yStart;
		int width = sme.map.heightmap.getHeightmapWidth();
		int height = sme.map.heightmap.getHeightmapLength();
		float maxHeight = sme.map.maxHeight/4f;

		for (int lodLevel = 0; lodLevel < 4; lodLevel++) {
			int lodSkip = FastMath.pow(2, lodLevel);
			int lodNegativeExtendTileSize = (lodSkip - 1) * rs.quadSize;
			int lodSkipTileSize = lodSkip * rs.quadSize;
			float texFraction = lodSkip / (float) blockSizeinTiles;
			long start = System.nanoTime();

			Vector3 v1, v2, v3, v4, vBaseNormal;

			//Generate VertexBuffer Data
			vbo[lodLevel].clear();

			xStart = ((index % mapWidthInBlocks) * blockSizeinTiles) + lodSkip;
			yStart = ((index / mapWidthInBlocks) * blockSizeinTiles) + lodSkip;

			v4 = null;
			int xLocal;
			int yLocal = 0;
			for (int y = yStart; y < (yStart + blockSizeinTiles); y += lodSkip) {
				xLocal = 0;
				x = xStart;

				try {
					v1 = new Vector3(-rs.quadHalfSize - lodNegativeExtendTileSize + (x * rs.quadSize),
							heightmap[y - lodSkip][x - lodSkip] * maxHeight, -rs.quadHalfSize - lodNegativeExtendTileSize + (y * rs.quadSize));
					v2 = new Vector3(-rs.quadHalfSize - lodNegativeExtendTileSize + (x * rs.quadSize),
							heightmap[y][x - lodSkip] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));
				} catch (ArrayIndexOutOfBoundsException e) {
					break; // Temporary hack for a problem that should not happen
				}
				v3 = new Vector3(rs.quadHalfSize + (x * rs.quadSize), heightmap[y - lodSkip][x] * maxHeight,
						-rs.quadHalfSize - lodNegativeExtendTileSize + (y * rs.quadSize));

				vBaseNormal = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3)).normalize();
				
			/* TEXCOORD */
				vbo[lodLevel].put(0 + (xLocal / (float) blockSizeinTiles));
				vbo[lodLevel].put(0 + (yLocal / (float) blockSizeinTiles));
			/* NORMAL   */
				vbo[lodLevel].put(getSmoothedNormal(heightmap, x - lodSkip, y - lodSkip, width,
						height, vBaseNormal, lodSkip, lodSkipTileSize, maxHeight).vector, 0, 3);
			/* VERTEX   */
				vbo[lodLevel].put(v1.vector, 0, 3);
				
			/* TEXCOORD */
				vbo[lodLevel].put(0 + (xLocal / (float) blockSizeinTiles));
				vbo[lodLevel].put(texFraction + (yLocal / (float) blockSizeinTiles));
			/* NORMAL   */
				vbo[lodLevel].put(getSmoothedNormal(heightmap, x - lodSkip, y, width, height, vBaseNormal, lodSkip, lodSkipTileSize, maxHeight).vector, 0, 3);
			/* VERTEX   */
				vbo[lodLevel].put(v2.vector, 0, 3);

				for (; x < (xStart + blockSizeinTiles); x += lodSkip) {
					v3 = new Vector3(rs.quadHalfSize + (x * rs.quadSize),
							heightmap[y - lodSkip][x] * maxHeight, -rs.quadHalfSize - lodNegativeExtendTileSize + (y * rs.quadSize));
					v4 = new Vector3(rs.quadHalfSize + (x * rs.quadSize), heightmap[y][x] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));

					vBaseNormal = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3)).normalize();
					
				/* TEXCOORD */
					vbo[lodLevel].put(texFraction + (xLocal / (float) blockSizeinTiles));
					vbo[lodLevel].put(0 + (yLocal / (float) blockSizeinTiles));
				/* NORMAL   */
					vbo[lodLevel].put(getSmoothedNormal(heightmap, x, y - lodSkip, width, height, vBaseNormal, lodSkip, lodSkipTileSize, maxHeight).vector, 0, 3);
				/* VERTEX   */
					vbo[lodLevel].put(v3.vector, 0, 3);
					
				/* TEXCOORD */
					vbo[lodLevel].put(texFraction + (xLocal / (float) blockSizeinTiles));
					vbo[lodLevel].put(texFraction + (yLocal / (float) blockSizeinTiles));
				/* NORMAL   */
					vbo[lodLevel].put(getSmoothedNormal(heightmap, x, y, width, height, vBaseNormal, lodSkip, lodSkipTileSize, maxHeight).vector, 0, 3);
				/* VERTEX   */
					vbo[lodLevel].put(v4.vector, 0, 3);

					//Copy last 2 vectors to new first ones
					v1 = v3;
					v2 = v4;

					xLocal += lodSkip;
				}

				//We need to insert null triangles here, for lf+cr
				//Last point again
			/* TEXCOORD */
				vbo[lodLevel].put(0);
				vbo[lodLevel].put(0);
			/* NORMAL   */
				vbo[lodLevel].put(nullVector.vector, 0, 3);
			/* VERTEX   */
				vbo[lodLevel].put(v4.vector, 0, 3);

				//First point of next row again (=second point of this row)
				v2 = new Vector3(-rs.quadHalfSize - lodNegativeExtendTileSize + (xStart * rs.quadSize),
						heightmap[y][xStart - lodSkip] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));
			/* TEXCOORD */
				vbo[lodLevel].put(0);
				vbo[lodLevel].put(0);
			/* NORMAL   */
				vbo[lodLevel].put(nullVector.vector, 0, 3);
			/* VERTEX   */
				vbo[lodLevel].put(v2.vector, 0, 3);

				yLocal += lodSkip;
			}

			vbo[lodLevel].flip();

			//Upload our interleaved Array
			boolean isNewArray = (vboID[lodLevel][index] < 0);

			if (isNewArray)
				gl.glGenBuffers(1, vboID[lodLevel], index);

			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboID[lodLevel][index]);

			if (isNewArray)
				gl.glBufferData(GL.GL_ARRAY_BUFFER, verticesPerBlock[lodLevel] * 8 * BufferUtil.SIZEOF_FLOAT, vbo[lodLevel], GL.GL_DYNAMIC_DRAW);
			else
				gl.glBufferSubData(GL.GL_ARRAY_BUFFER, 0, verticesPerBlock[lodLevel] * 8 * BufferUtil.SIZEOF_FLOAT, vbo[lodLevel]);

			if (rs.outputPerfDebug)
				System.out.println("Done creating VBO Block. LOD: " + lodLevel + " ( " + ((System.nanoTime() - start) / 1000000) + " ms )");


			//If map heights changed, we need to adopt the feature heights and slopemap
			updateFeatureBlockHeights(index);
			updateSlopemapBlock(index);

			//invalidate slope texture
			if (rs.mapMode == MapMode.SlopeMap)
				isTextureCached[index] = false;

			blocksCreatedThisFrame++;
			isGeometryCached[lodLevel][index] = true;
		}
	}
		
	private Vector3 getSmoothedNormal(float map[][], int x, int y, int width, int height, Vector3 baseVector, int lodSkip, int lodSkipTileSize, float maxHeight)
	{
		maxHeight *= 4f;
		if (!rs.smoothNormals)
			return baseVector;
		
		if ((x >= lodSkip) && (x < width-lodSkip) && (y >= lodSkip) && (y < height-lodSkip))
		{
			if (rs.fastNormals)
			{
				/*
				 * Arrangement of vertices:
				 *  	2
				 * 
				 * 3	1	
				 * 
				 * 1 Triangle used:
				 * 1:	1, 2, 3 
				 */
				
				Vector3 v1, v2, v3;
				v1 = new Vector3(0, map[y][x] * maxHeight, 0);
				v2 = new Vector3(0, map[y - lodSkip][x] * maxHeight, -lodSkipTileSize);
				v3 = new Vector3(-lodSkipTileSize, map[y][x - lodSkip] * maxHeight, 0);
				return Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3)).normalize();
			}
			else
			{
				/*
				 * Arrangement of vertices:
				 *  	4
				 * 
				 * 5	1	3
				 * 
				 *  	2
				 *  
				 * 4 Triangles used:
				 * 1:	1, 2, 3
				 * 2:	1, 3, 4
				 * 3:	1, 4, 5
				 * 4:	1, 5, 2
				 * 
				 */
				Vector3 vN1, vN2, vN3, vN4, v1, v2, v3;
				v1 = new Vector3(0, map[y][x] * maxHeight, 0);
				v2 = new Vector3(0, map[y + lodSkip][x] * maxHeight, lodSkipTileSize);
				v3 = new Vector3(lodSkipTileSize, map[y][x + lodSkip] * maxHeight, 0);
				vN1 = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3));
				
				v2 = new Vector3(0, map[y - lodSkip][x] * maxHeight, -lodSkipTileSize); //4
				vN2 = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v3), Vector3Math.subVectors(v3, v2));
	
				v3 = new Vector3(-lodSkipTileSize, map[y][x - lodSkip] * maxHeight, 0); //5
				vN3 = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3));
				
				v2 = new Vector3(0, map[y + lodSkip][x] * maxHeight, lodSkipTileSize); //2 again
				vN4 = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v3), Vector3Math.subVectors(v3, v2));
				
				return Vector3Math.addVectors(Vector3Math.addVectors(Vector3Math.addVectors(vN1, vN2), vN3), vN4).normalize();
			}
		}
		else
			return baseVector;
	}
	
	public void invalidateBlocksByBrush(int tileX, int tileY, Brush brush, boolean geometry, boolean texture, boolean feature)
	{
		for (int y = tileY - 4; y < (tileY + brush.getHeight() + 4); y++)
			for (int x = tileX - 4; x < (tileX + brush.getWidth() + 4); x++)
				if ((x >= 0) && (x < sme.map.heightmap.getHeightmapWidth() - 1) && (y >= 0) && (y < sme.map.heightmap.getHeightmapLength() - 1))
				{
					if (geometry)
						for (int l = 0; l < LODLEVELS; l++)	
							isGeometryCached[l][(x / blockSizeinTiles) + (mapWidthInBlocks * (y / blockSizeinTiles))] = false;
					if (texture)
						isTextureCached[(x / blockSizeinTiles) + (mapWidthInBlocks * (y / blockSizeinTiles))] = false;
					if (feature)
						isFeatureCached[(x / blockSizeinTiles) + (mapWidthInBlocks * (y / blockSizeinTiles))] = false;
				}
		camPosChangedNotify();
	}
	
	public void invalidateAllBlocks(boolean geometry, boolean texture, boolean feature)
	{
		for (int i = 0; i < blockCount; i++)
		{
			if (geometry)
				for (int l = 0; l < LODLEVELS; l++)
					isGeometryCached[l][i] = false;
			if (texture)
				isTextureCached[i] = false;
			if (feature)
				isFeatureCached[i] = false;
		}
		camPosChangedNotify();
	}
	
	public void invalidateBlocksAround(int tileX, int tileY, boolean geometry, boolean texture, boolean feature)
	{
		int xPosinBlocks = tileX / blockSizeinTiles;
		int yPosinBlocks = tileY / blockSizeinTiles;
		xPosinBlocks--;
		yPosinBlocks--;
		for (int y = yPosinBlocks; y < yPosinBlocks + 3; y++)
			for (int x = xPosinBlocks; x < xPosinBlocks + 3; x++)
				if ((x >= 0) && (x < mapWidthInBlocks) && (y >= 0) && (y < mapHeightInBlocks))
				{
					if (geometry)
						for (int l = 0; l < LODLEVELS; l++)
							isGeometryCached[l][x + (mapWidthInBlocks * y)] = false;
					if (texture)
						isTextureCached[x + (mapWidthInBlocks * y)] = false;
					if (feature)
						isFeatureCached[x + (mapWidthInBlocks * y)] = false;
				}
		camPosChangedNotify();
	}
	
	private void renderWater(GL gl)
	{
		float currentWaterlevel = sme.map.waterHeight/4f;
		int width = sme.map.heightmap.getHeightmapWidth();
		int length = sme.map.heightmap.getHeightmapLength();
		if (rs.fancyWater) {
			//Bind Reflectionmap
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, reflectionMapID);
	
			//Bind Refractionmap
			gl.glActiveTexture(GL.GL_TEXTURE1);
			gl.glBindTexture(GL.GL_TEXTURE_2D, refractionMapID);
			
			//Bind Normalmap
			gl.glActiveTexture(GL.GL_TEXTURE2);
			gl.glBindTexture(GL.GL_TEXTURE_2D, normalMapID);
			
			//Bind Distortionmap
			gl.glActiveTexture(GL.GL_TEXTURE3);
			gl.glBindTexture(GL.GL_TEXTURE_2D, dudvMapID);
			
			//Enable ShaderProgram
			shaderManager.bindShader("water");
			
			//Set Camera Position
			gl.glUniform4f(shaderManager.getUniformLocation("viewpos"), rs.cameraPosition.camX, rs.cameraPosition.camY, rs.cameraPosition.camZ, 1.0f);
			
			//Set Water Color
			gl.glUniform4f(shaderManager.getUniformLocation("waterColor"), 0.8f , 0.1f, 0.1f, 1.0f);
			
			//Set Light(Sun) Position
			gl.glUniform4f(shaderManager.getUniformLocation("lightpos"), sunPosition.x(), sunPosition.y(), sunPosition.z(), 0f);
			
			//Set Animation
			gl.glUniform1f(shaderManager.getUniformLocation("time"), rs.time);
			gl.glUniform1f(shaderManager.getUniformLocation("time2"), -rs.time);
			
			//Set Textures
			gl.glUniform1i(shaderManager.getUniformLocation("water_reflection"), 0);
			gl.glUniform1i(shaderManager.getUniformLocation("water_refraction"), 1);
			gl.glUniform1i(shaderManager.getUniformLocation("water_normalmap"), 2);
			gl.glUniform1i(shaderManager.getUniformLocation("water_dudvmap"), 3);
			gl.glUniform1i(shaderManager.getUniformLocation("water_depthmap"), 4);
			
			//Calculate scale factor according to water plane size
			float waterScaleFactorX = ((2 * rs.waterMapExtend) + width * rs.quadSize) / 30f;
			float waterScaleFactorY = ((2 * rs.waterMapExtend) + length * rs.quadSize) / 30f;
					
			gl.glEnable(GL.GL_BLEND);
	    	gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glBegin(GL.GL_QUADS);
			    
			//gl.glTexCoord2i(0, 0);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, 0.0f, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE1, 0.0f, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE2, 0.0f, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE3, 0.0f, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE4, 0.0f, 0.0f);
			gl.glVertex3f(-rs.waterMapExtend, currentWaterlevel, -rs.waterMapExtend);
			    					    
			//gl.glTexCoord2f(0, 5);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, 0.0f, waterScaleFactorY);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE1, 0.0f, 1.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE2, 0.0f, 1.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE3, 0.0f, 1.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE4, 0.0f, 1.0f);
			gl.glVertex3f(-rs.waterMapExtend,  currentWaterlevel, rs.waterMapExtend + (length * rs.quadSize));
			    					    	
			//gl.glTexCoord2f(5, 5);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, waterScaleFactorX, waterScaleFactorY);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE1, 1.0f, 1.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE2, 1.0f, 1.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE3, 1.0f, 1.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE4, 1.0f, 1.0f);
			gl.glVertex3f(rs.waterMapExtend + (width * rs.quadSize),
					currentWaterlevel, rs.waterMapExtend + (length * rs.quadSize));
			    					    
			//gl.glTexCoord2i(5, 0);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE0, waterScaleFactorX, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE1, 1.0f, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE2, 1.0f, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE3, 1.0f, 0.0f);
			gl.glMultiTexCoord2f(GL.GL_TEXTURE4, 1.0f, 0.0f);
			gl.glVertex3f(rs.waterMapExtend + (width * rs.quadSize),  currentWaterlevel, -rs.waterMapExtend);
		    gl.glEnd();
		    gl.glDisable(GL.GL_BLEND);
		    
			//Reset Texture unit to 0
			gl.glActiveTexture(GL.GL_TEXTURE0);
			
		    shaderManager.unbindShader();
		}else{
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glColor4f(0.1f, 0.1f, 0.8f, 0.5f);
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3f(-100, currentWaterlevel, -100);
			gl.glVertex3f(-100,  currentWaterlevel, 100 + (length * rs.quadSize));
			gl.glVertex3f(100 + (width * rs.quadSize), currentWaterlevel, 100 + (length * rs.quadSize));
			gl.glVertex3f(100 + (width * rs.quadSize), currentWaterlevel, -100);
			gl.glEnd();
			gl.glDisable(GL.GL_BLEND);
		}
	}
	
	private void renderBrush(GL gl)
	{
		float[][] map = sme.map.heightmap.getHeightMap();
		int width = sme.map.heightmap.getHeightmapWidth();
		int length = sme.map.heightmap.getHeightmapLength();
		float maxHeight = sme.map.maxHeight/4f; //
		int xStart, yStart, brushWidth, brushHeight;
    	if (sme.mes.activeBrush.isVertexOriented())
    	{
    		xStart = sme.mes.brushPos.x();
        	yStart = sme.mes.brushPos.y();
        	brushWidth = sme.mes.activeBrush.getWidth() + 1;
        	brushHeight = sme.mes.activeBrush.getHeight() + 1;
    	}
    	else
    	{
    		xStart = sme.mes.brushPos.x() + 1;
        	yStart = sme.mes.brushPos.y() + 1;
        	brushWidth = sme.mes.activeBrush.getWidth();
        	brushHeight = sme.mes.activeBrush.getHeight();
    	}
    	float yHeightOffset = 1;
		
    	if (rs.onlyOutlineBrush)
    	{
    		gl.glColor4f(1f, 0f, 0f, 1f);
    		
			gl.glBegin(GL.GL_LINE_LOOP);
			int x = xStart;
			int y = yStart;
			for (; y < (yStart + brushHeight); y++)
			{
				if ((x > 0) && (x <= width) && (y > 0) && (y <= length))
					gl.glVertex3f(-rs.quadHalfSize + (x * rs.quadSize), (map[y - 1][x - 1] * maxHeight) + yHeightOffset, -rs.quadHalfSize + (y * rs.quadSize));
			}
			for (; x < (xStart + brushWidth); x++)
			{
				if ((x > 0) && (x <= width) && (y > 0) && (y <= length))
					gl.glVertex3f(-rs.quadHalfSize + (x * rs.quadSize), (map[y - 1][x - 1] * maxHeight) + yHeightOffset, -rs.quadHalfSize + (y * rs.quadSize));
			}
			for (; y > yStart; y--)
			{
				if ((x > 0) && (x <= width) && (y > 0) && (y <= length))
					gl.glVertex3f(-rs.quadHalfSize + (x * rs.quadSize), (map[y - 1][x - 1] * maxHeight) + yHeightOffset, -rs.quadHalfSize + (y * rs.quadSize));
			}
			for (; x > xStart; x--)
			{
				if ((x > 0) && (x <= width) && (y > 0) && (y <= length))
					gl.glVertex3f(-rs.quadHalfSize + (x * rs.quadSize), (map[y - 1][x - 1] * maxHeight) + yHeightOffset, -rs.quadHalfSize + (y * rs.quadSize));
			}
		    gl.glEnd();

			//draw brush center dot
		    int xCenter = (x + (brushWidth / 2));
		    int yCenter = (y + (brushHeight / 2));
		    if ((xCenter > 0) && (xCenter < width) && (yCenter > 0) && (yCenter < length))
		    {
		    	float height = Math.max(Math.max(Math.max(map[yCenter][xCenter], map[yCenter - 1][xCenter]),
		    			map[yCenter][xCenter - 1]), map[yCenter - 1][xCenter - 1]);
			    gl.glPushMatrix();
				float centerScale = Math.max(1, (brushWidth + brushHeight) / 40);
			    gl.glTranslatef(xCenter * rs.quadSize, (height * maxHeight) + yHeightOffset, yCenter * rs.quadSize);
				gl.glScalef(centerScale, centerScale, centerScale);
				glut.glutSolidOctahedron();
			    gl.glPopMatrix();
		    }
    	}
    	else
    	{
    		//TODO create interface for brushes with pattern/with texture 
    		
	    	gl.glEnable(GL.GL_BLEND);
	    	gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			float[][] pattern = sme.mes.activeBrush.getPattern().getPattern();
			for (int y = yStart+1; y < (yStart + brushHeight); y++)
			{
				for (int x = xStart+1; x < (xStart + brushWidth); x++)
				{
					if ((x > 0) && (x < sme.width) && (y > 0) && (y < sme.height))
					{
						gl.glColor4f(1f, 1f, 1f, pattern[x - xStart - 1][y - yStart - 1]);
						gl.glBegin(GL.GL_QUADS);
						    //gl.glTexCoord2f(0, 0);
						    gl.glVertex3f(-rs.quadHalfSize + (x * rs.quadSize), (map[x-1][y-1] * sme.map.maxHeight) + yHeightOffset, -rs.quadHalfSize + (y * rs.quadSize));

						    //gl.glTexCoord2f(0, 1);
						    gl.glVertex3f(-rs.quadHalfSize + (x * rs.quadSize), (map[x-1][y] * sme.map.maxHeight) + yHeightOffset, rs.quadHalfSize + (y * rs.quadSize));

						    //gl.glTexCoord2f(1, 1);
						    gl.glVertex3f(rs.quadHalfSize + (x * rs.quadSize), (map[x][y] * sme.map.maxHeight) + yHeightOffset, rs.quadHalfSize + (y * rs.quadSize));

						    //gl.glTexCoord2f(1, 0);
						    gl.glVertex3f(rs.quadHalfSize + (x * rs.quadSize), (map[x][y-1] * sme.map.maxHeight) + yHeightOffset, -rs.quadHalfSize + (y * rs.quadSize));
					    gl.glEnd();
					}
				}
			}
			gl.glDisable(GL.GL_BLEND);
    	}
	}
	
	private boolean isBlockVisible(int index)
	{
		return true;
		/*
		//Retrieve camera
		CameraPosition cam = rs.cameraPosition;
		
		//Determine current position
		int xPosInTiles = FastMath.round(cam.camX / rs.quadSize);
		int yPosInTiles = FastMath.round(cam.camZ / rs.quadSize);
				
		int blockXPosInTiles = ((index % mapWidthInBlocks) * blockSizeinTiles) + (blockSizeinTiles / 2);
		int blockYPosInTiles = ((index / mapWidthInBlocks) * blockSizeinTiles) + (blockSizeinTiles / 2);
		
		int distXinTiles = xPosInTiles - blockXPosInTiles;
		int distYinTiles = yPosInTiles - blockYPosInTiles;
		float distInTiles = (float)Math.sqrt((distXinTiles * distXinTiles) + (distYinTiles * distYinTiles));
		*/
		
		//TODO: cull non visible blocks. make real frustrum culling
	}
	
	private void renderReflectionMap(GL gl)
	{
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferObjectID);
	    gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, reflectionMapID, 0);
	    
		setupProjection(gl);
		setCameraPosition(gl, rs.cameraPosition);
		
		gl.glScaled(1, -1, 1);
		gl.glTranslatef(0, -2 * sme.map.waterHeight, 0);
		gl.glCullFace(GL.GL_FRONT); //Invert culling
		
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glLoadIdentity();
		
		gl.glEnable(GL.GL_CLIP_PLANE0);
		gl.glClipPlane(GL.GL_CLIP_PLANE0, new double[] { 0d, 1d, 0d, -sme.map.waterHeight + 1}, 0);
		
		renderScene(gl);
		
		gl.glCullFace(GL.GL_BACK);
		gl.glDisable(GL.GL_CLIP_PLANE0);
		
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
	}
	
	private void renderRefractionMap(GL gl)
	{
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferObjectID);
	    gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, refractionMapID, 0);
	    
		setupProjection(gl);
		setCameraPosition(gl, rs.cameraPosition);
		
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glLoadIdentity();
		
		gl.glEnable(GL.GL_CLIP_PLANE0);
		gl.glClipPlane(GL.GL_CLIP_PLANE0, new double[] { 0d, -1d, 0d, sme.map.waterHeight + 1}, 0);
		
		renderScene(gl);
		renderBrush(gl);
		
		gl.glDisable(GL.GL_CLIP_PLANE0);
		
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
	}
		
	private void renderFeatureList(GL gl, FeatureMapContainer[] renderList, int count)
	{
		int currentTexture0 = -1;
		int currentTexture1 = -1;
		int textureID;
		int displayListID;
		
		//Enable AlphaMasking
		gl.glAlphaFunc(GL.GL_GREATER, 0.5f);
		gl.glEnable(GL.GL_ALPHA_TEST);
		
		//Setup Stage0
		gl.glActiveTexture(GL.GL_TEXTURE0);
		
		//Setup lighting mode
		if (rs.featureLighting)
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		else
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		
		//Setup Stage1
		gl.glActiveTexture(GL.GL_TEXTURE1);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
		//RGB (take from previous)
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_RGB, GL.GL_PREVIOUS);
		//Alpha (replace with tex2 alpha)
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_ALPHA, GL.GL_REPLACE);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_ALPHA, GL.GL_TEXTURE1);
		
		FeatureMapContainer cont;
		for (int i = 0; i < count; i++)
		{
			cont = renderList[i];
			
			//Try to load feature
			if (!featureManager.isFeatureLoaded(cont.featureID))
			{
				if (featuresCreatedThisFrame < rs.maxFeaturesPerFrame)
				{
					//TODO stop forcing feature loading 
					if (featureManager.loadFeature(gl, cont.featureID))
						featuresCreatedThisFrame++;
					else
						cont.featureID = 0;
				}
			}
			
			//Render if loaded
			if (featureManager.isFeatureLoaded(cont.featureID))
			{				
				gl.glPushMatrix();
				gl.glTranslatef(cont.x, cont.y, cont.z);
				gl.glRotatef(cont.rotX, 0, 1, 0);
				gl.glRotatef(cont.rotY, 0, 1, 0);
				gl.glRotatef(cont.rotZ, 0, 1, 0);
				
				//Bind texture0 (RGB) if necessary
				textureID = featureManager.getBaseTextureID(cont.featureID);
				if ((textureID >= 0) && (currentTexture0 != textureID))
				{
					currentTexture0 = textureID;
					gl.glActiveTexture(GL.GL_TEXTURE0);
					gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
				}
				
				//Bind texture1 (alpha) if necessary. (red channel is self-illumination)
				textureID = featureManager.getAlphaTextureID(cont.featureID);
				if ((textureID >= 0) && (currentTexture1 != textureID))
				{
					currentTexture1 = textureID;
					gl.glActiveTexture(GL.GL_TEXTURE1);
					gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
				}
				
				displayListID = featureManager.getDisplayListID(cont.featureID);
				if (displayListID >= 0)
				{
					gl.glCallList(displayListID);
					rs.trisRendered += featureManager.getTriangleCount(cont.featureID);
				}
				
				gl.glPopMatrix();
			}
		}
		
		//Clear Stage1
		gl.glActiveTexture(GL.GL_TEXTURE1);
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		
		//Clear Stage0
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		
		//Disable Alphatest
		gl.glDisable(GL.GL_ALPHA_TEST);
	}
		
	private void renderSkyBox(GL gl)
	{
		/*
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, skyboxTexID);

		// tell openGL to generate the texture coords for a sphere map
		gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
		gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
		//gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);

		// enable automatic texture coordinate generation
		gl.glEnable(GL.GL_TEXTURE_GEN_S);
		gl.glEnable(GL.GL_TEXTURE_GEN_T);
		//gl.glEnable(GL.GL_TEXTURE_GEN_R);
		
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		//gl.glDisable(GL.GL_DEPTH_TEST);
		//gl.glDepthMask(false);
		
		
		gl.glBegin(GL.GL_QUADS);
			gl.glTexCoord2f(0, 1);
			gl.glVertex3f(0, 0, 0);
			
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(0, 500, 0);
			
			gl.glTexCoord2f(1, 0);
			gl.glVertex3f(500, 500, 0);
			
			gl.glTexCoord2f(1, 1);
			gl.glVertex3f(500, 0, 0);
		gl.glEnd();
		
		//GLUT glut = new GLUT();
		//glut.glutSolidCube(-500);// SolidSphere(-500, 50, 50);
		
		//gl.glDepthMask(true);
		//gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		
		gl.glDisable(GL.GL_TEXTURE_GEN_S);
		gl.glDisable(GL.GL_TEXTURE_GEN_T);
		gl.glDisable(GL.GL_TEXTURE_GEN_R);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
//		gl.glDisable(GL.GL_TEXTURE_2D);
//		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
//		gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, 0);
//		gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		*/
	}
	
	private void renderScene(GL gl)
	{
		//Set current texture to "not initialized"
		int currentTexture = -1;
		int lodLevel = 0;
		Iterator<FeatureMapContainer> it;
		
		//Clear feature sort list
		featuresToRenderCount = 0;
		
		//Clear
		gl.glClearColor(0f, 0f, 0f, 1f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		//Enable Buffers
		gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
						
		//Enable texturing and lighting
		gl.glEnable(GL.GL_TEXTURE_2D);
		if (rs.useLighting)
			gl.glEnable(GL.GL_LIGHTING);
		
		//Render visible blocks
		if (rs.mapTypeSM3)
		{
			//Bind and setup textures, activate shader.
			/*
			 * 
			 */
			
			//Render Blocks
			int i;
			for (i = 0; i < blockCount; i++)
			{
				if (isBlockVisible(i))
				{
					//Calculate lodLevel
					lodLevel = getLODLevel(i);
					
					if (!isGeometryCached[lodLevel][i])
						createBlock(gl, i);
					
					//Render Block
					//VBO
					if (vboID[lodLevel][i] >= 0)
						//Set Buffer
						gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboID[lodLevel][i]);
							
					//Format:
					gl.glInterleavedArrays(GL.GL_T2F_N3F_V3F, 0, 0);
							
					//Render Buffer
					gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, verticesPerBlock[lodLevel]);
					rs.trisRendered += triCountPerBlock[lodLevel];

					if (lodLevel <= rs.renderFeatureLOD) {
						if (!isFeatureCached[i])
							createFeatureBlock(i);
								
						it = featureList[i].iterator();
						while (it.hasNext())
						{
							featuresToRender[featuresToRenderCount] = it.next();
							featuresToRenderCount++;
						}
					}

				}
			}
		}
		else
		{
			int i;
			for (i = 0; i < blockCount; i++)
			{
				if (isBlockVisible(i))
				{
					//Calculate lodLevel
					lodLevel = getLODLevel(i);
					
					if (!isGeometryCached[lodLevel][i])
						createBlock(gl, i);
					if (!isTextureCached[i])
						createTexture(gl, i);
					
					//Bind block texture
					if ((textureID[i] >= 0) && (currentTexture != textureID[i]))
					{
						currentTexture = textureID[i];
						gl.glBindTexture(GL.GL_TEXTURE_2D, textureID[i]);
					}
					
					//Render Block
					//VBO
					if (vboID[lodLevel][i] >= 0) {
						//Set Buffer
						gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboID[lodLevel][i]);
							
						//Format:
						gl.glInterleavedArrays(GL.GL_T2F_N3F_V3F, 0, 0);
							
						//Render Buffer
						gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, verticesPerBlock[lodLevel]);
						rs.trisRendered += triCountPerBlock[lodLevel];
					}

					
					//add features from block to sortable list
					if (lodLevel <= rs.renderFeatureLOD) {
						if (!isFeatureCached[i])
							createFeatureBlock(i);
								
						it = featureList[i].iterator();
						while (it.hasNext())
						{
							featuresToRender[featuresToRenderCount] = it.next();
							featuresToRenderCount++;
						}
					}

				}
			}
		}
		
		/* Sorting not needed anymore (alphatest, instead of blending)
		//Sort features by distance
		if (camPosChanged)
			Arrays.sort(featuresToRender, 0, featuresToRenderCount, featureComparator);
		*/
		
		//Render visible features
		renderFeatureList(gl, featuresToRender, featuresToRenderCount);
		
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_TEXTURE_2D);
	}
	
	public void display(GL gl)
	{
		staticGl = gl;
		blocksCreatedThisFrame = 0;
		texturesCreatedThisFrame = 0;
		featureBlocksCreatedThisFrame = 0;
		featuresCreatedThisFrame = 0;
		
		//Exit, if invisible anyway
		if (rs.batchMode)
			return;


		rs.trisRendered = 0;
		
		//Switch between wireframe and normal mode
		if (switchPolyMode)
		{
			switchPolyMode = false;
			wireFrameMode = !wireFrameMode;
		}
		
		//Setup lighting
		setupLight(gl);
		
		//Render the Scene
		setupProjection(gl);
		setCameraPosition(gl, rs.cameraPosition);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

		if (wireFrameMode) {
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
			renderScene(gl);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		}else{
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			renderScene(gl);
		}


		renderBrush(gl);
		renderSun(gl);

		//Render water, after the scene is rendered.
		if (rs.fancyWater && (sme.map.waterHeight >= 0))
		{
			//Render ReflectionMap
			renderReflectionMap(gl);
			
			//Render RefractionMap
			renderRefractionMap(gl);
		}
		
		setupProjection(gl);
		setCameraPosition(gl, rs.cameraPosition);
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glLoadIdentity();
		//Render Water Plane
		if (sme.map.waterHeight >= 0)
			renderWater(gl);

		//Render the skybox (currently disabled)
		renderSkyBox(gl);
	}
	
	private int getLODLevel(int index)
	{
		if (!rs.useLOD)
			return 0;

		float dist;
		
		double x = (((index % mapWidthInBlocks) * blockSizeinTiles) * rs.quadSize) + (rs.quadHalfSize * blockSizeinTiles);
		double z = (((index / mapWidthInBlocks) * blockSizeinTiles) * rs.quadSize) + (rs.quadHalfSize * blockSizeinTiles);

		dist = (float) Math.sqrt(
				((x - rs.cameraPosition.camX) * (x - rs.cameraPosition.camX)) +
						((z - rs.cameraPosition.camZ) * (z - rs.cameraPosition.camZ)));

		float n = rs.lodDist;
		if (dist > n * 4){
			return 3;
		}else if (dist > n * 2){
			return 2;
		}else if (dist > n){
			return 1;
		}else{
			return 0;
		}
	}
		
	public boolean checkHardware(GL gl)
	{
		boolean result = true;
		
		//Check openGL Version
		String openGLVersion = gl.glGetString( GL.GL_VERSION );
	    System.out.print("OpenGL version: " + openGLVersion);
	    if (Integer.parseInt(openGLVersion.substring(0, 1)) < 2)
	    {
	    	System.out.println(" -> (failed)");
	    	System.out.println("Requires OpenGL 2.0 or greater!");
	    	result = false;
	    }
	    else
	    	System.out.println(" -> (ok)");
	    	
	    String[] neededFunctions = {
    		"glGenFramebuffersEXT",
    		"glBindFramebufferEXT",
    		"glGenRenderbuffersEXT",
    		"glBindRenderbufferEXT",
    		"glRenderbufferStorageEXT",
    		"glFramebufferRenderbufferEXT",
    		"glBindFramebufferEXT",
    		"glFramebufferTexture2DEXT"	
	    };
	    
	    //check needed Functions
	    for (int i = 0; i < neededFunctions.length; i++)
	    	if (!gl.isFunctionAvailable(neededFunctions[i]))
	    	{
	    		System.out.println("OpenGL Function missing: " + neededFunctions[i]);
	    		result = false;
	    	}
	    
		return result;
	}

	public void init(GL gl)
	{
		glut = new GLUT();
		glu = new GLU();
		
		if (!checkHardware(gl))
		{
			//Dialog d = new Dialog()
			System.out.println("There are missing extensions... aborting.");
			System.exit(1);
		}
		
		/* Enable Depth test. For auto-sorting heightMapHeight levels */
	    gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		//gl.glClearDepth(-100);
	    
	    /* Setup lighting */
	    gl.glEnable(GL.GL_LIGHT0);
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT,  new float[] { 0.2f, 0.2f, 0.2f, 1f }, 0);
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE,  new float[] { 0.8f, 0.8f, 0.8f, 1f }, 0);
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, new float[] { 0.3f, 1f, 0.3f, 0f }, 0);
	    
	    gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR,  new float[] { 0f, 0f, 0f, 0f }, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, new float[] { 100f }, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT,   new float[] { 1f, 1f, 1f, 1f }, 0);
	    gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE,   new float[] { 1f, 1f, 1f, 1f }, 0);

	    /* Interpolation mode: nicest */
	    gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
	    
	    /* Set texture mode */
		gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
				
		try
		{
			Texture tex = TextureIO.newTexture(new File("textures/water_normal.png"), false);
			normalMapID = tex.getTextureObject();
			gl.glBindTexture(GL.GL_TEXTURE_2D, normalMapID);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			
			tex = TextureIO.newTexture(new File("textures/water_distortion.png"), false);
			dudvMapID = tex.getTextureObject();
			gl.glBindTexture(GL.GL_TEXTURE_2D, dudvMapID);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			
			tex = TextureIO.newTexture(new File("textures/cloudysunset.png"), false);
			skyboxTexID = tex.getTextureObject();
			gl.glBindTexture(GL.GL_TEXTURE_2D, skyboxTexID);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	    /* Set vsync */
	    gl.setSwapInterval(rs.vsync ? 1 : 0);
	    
	    /* Activate Backface culling */
	    gl.glEnable(GL.GL_CULL_FACE);
	    gl.glFrontFace(GL.GL_CCW);
	    gl.glCullFace(GL.GL_BACK);
	    
	    shaderManager = new ShaderManager(gl);
	    //featureManager = new FeatureManager();
	    
	    /* FBO Setup */
	    setupOffscreenBuffers(gl);
	}

	private void setupOffscreenBuffers(GL gl)
	{
		//Delete old stuff
		if (frameBufferObjectID >= 0)
		{
			//Delete refraction + reflection textures
			int[] delID = new int[] { reflectionMapID, refractionMapID, featureTexID };
			gl.glDeleteTextures(3, delID, 0);
			
			//Delete RenderBuffer
			delID = new int[] { renderBufferObjectID, featureRBOID };
			gl.glDeleteRenderbuffersEXT(2, delID, 0);
			
			//Delete Framebuffer
			delID = new int[] { frameBufferObjectID, featureFBOID };
			gl.glDeleteFramebuffersEXT(2, delID, 0);
		}
		
		////////////////////////////////////////
		//Generate Reflection/Refraction FBO
		////////////////////////////////////////
		int[] fboIDs = new int[1];
	    gl.glGenFramebuffersEXT(1, fboIDs, 0);
	    frameBufferObjectID = fboIDs[0];
	    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferObjectID);
	    
	    //Attach a depthbuffer
	    int[] renderBufferIDs = new int[1];
	    gl.glGenRenderbuffersEXT(1, renderBufferIDs, 0);
	    renderBufferObjectID = renderBufferIDs[0];
	    gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, renderBufferObjectID);
	    gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT, rs.displayWidth, rs.displayHeight);
	    gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, renderBufferObjectID);

	    //Create reflection and refraction textures
	    int[] reflectionMapIDs = new int[1];
	    gl.glGenTextures(1, reflectionMapIDs, 0);
	    reflectionMapID = reflectionMapIDs[0];
	    gl.glBindTexture(GL.GL_TEXTURE_2D, reflectionMapID);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
	    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, rs.displayWidth, rs.displayHeight, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
	    
	    int[] refractionMapIDs = new int[1];
	    gl.glGenTextures(1, refractionMapIDs, 0);
	    refractionMapID = refractionMapIDs[0];
	    gl.glBindTexture(GL.GL_TEXTURE_2D, refractionMapID);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
	    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, rs.displayWidth, rs.displayHeight, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
	    
	    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
	    
	    ////////////////////////////////////////
		//Generate Feature FBO
		////////////////////////////////////////
		fboIDs = new int[1];
	    gl.glGenFramebuffersEXT(1, fboIDs, 0);
	    featureFBOID = fboIDs[0];
	    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, featureFBOID);
	    
	    //Attach a depthbuffer
	    renderBufferIDs = new int[1];
	    gl.glGenRenderbuffersEXT(1, renderBufferIDs, 0);
	    featureRBOID = renderBufferIDs[0];
	    gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, featureRBOID);
	    gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT, rs.featureTexSize, rs.featureTexSize);
	    gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, featureRBOID);

	    //Create reflection and refraction textures
	    reflectionMapIDs = new int[1];
	    gl.glGenTextures(1, reflectionMapIDs, 0);
	    featureTexID = reflectionMapIDs[0];
	    gl.glBindTexture(GL.GL_TEXTURE_2D, featureTexID);
	    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
	    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, rs.featureTexSize, rs.featureTexSize, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, null);
	    
	    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
	}
	
	private void setupLight(GL gl)
	{
		sunPosition = new Vector3(rs.waterMapExtend + (sme.map.heightmap.getHeightmapWidth() * rs.quadSize), sme.map.maxHeight * 10,
				rs.waterMapExtend + (sme.map.heightmap.getHeightmapLength() * rs.quadSize));
		if (rs.moveSun)
		{
			sunPosition.vector[0] = sunPosition.vector[0] * (float)((Math.sin(rs.time * 10) / 2) + 0.5);
			sunPosition.vector[2] = sunPosition.vector[2] * (float)((Math.cos(rs.time * 10) / 2) + 0.5);
			
			Vector3 center = new Vector3(rs.quadHalfSize * sme.map.heightmap.getHeightmapWidth(), 0, rs.quadHalfSize * sme.map.heightmap.getHeightmapLength());
			Vector3 sunDirection = Vector3Math.subVectors(sunPosition, center).normalize();
			
			//setup sunlight direction
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, new float[] { (float)sunDirection.x(), (float)sunDirection.y(), (float)sunDirection.z(), 0f }, 0);
			
			camViewChangedNotify();
		}
	}
	
	private void renderSun(GL gl)
	{
		if (rs.drawSun)
		{
			GLUT glut = new GLUT();
			gl.glPushMatrix();
				gl.glDisable(GL.GL_TEXTURE_2D);
				gl.glTranslatef(sunPosition.x(), sunPosition.y(), sunPosition.z());
				gl.glColor3f(1.0f, 1.0f, 0.1f);
				glut.glutSolidSphere(30, 10, 10);
			gl.glPopMatrix();
		}
	}
	
	private void setupProjection(GL gl)
	{
		gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluPerspective(rs.fov, rs.displayWidth / rs.displayHeight, 1, sme.diag * 3.0f);
	}
	
	private void setCameraPosition(GL gl, CameraPosition cameraPosition)
	{
		gl.glRotatef(-cameraPosition.camRotX, 1, 0, 0);
		gl.glRotatef(-cameraPosition.camRotY, 0, 1, 0);
		gl.glRotatef(-cameraPosition.camRotZ, 0, 0, 1);
		gl.glTranslatef(-cameraPosition.camX, -cameraPosition.camY, -cameraPosition.camZ);
	}
	
	public void reshape(int width, int height)
	{
		staticGl.glViewport(0, 0, width, height);
		setupProjection(staticGl);
		
		rs.displayWidth = width;
	    rs.displayHeight = height;
	    
	    //Reinitialise offscreen buffers
	    setupOffscreenBuffers(staticGl);
	    
	    //rerender maps
	    camViewChangedNotify();
	}

	
	////////////////////////////
	//Utility methods
	////////////////////////////
	
	/**
	 * Generate a minimap from current map-data.<BR>
	 * NOTE: This should be inside SpringMapEdit,<BR>
	 * but since we need the GL for DXT1 compression, it was moved here.
	 */
	public byte[] getCompressedMinimapData()
	{
		/*
		 * Creates a 1024x1024 DXT1 compressed image data, with 8 mipmap sublevels.
		 *
		 * 2 Springmapsize equals 1024 texsize,
		 * so our downscale factor is:
		 * (springmapsize / 2)
		 * We are lucky, since we only have non-fractal values :)
		 */
		if (((sme.width % 2) != 0) || ((sme.height % 2) != 0))
		{
			(new Exception("Map Width or Height not divisable by 2 (Spring units). This corrupts minimap.")).printStackTrace();
		}
		
		//Minimap size
		final int height = 1024;
		final int width = 1024;
		final int oneTenthsOfHeight = Math.max(height / 10, 1);
		final int scanlineSize = width * 3;
		byte[][] minimapData = new byte[height][scanlineSize];
		
		//Downscale factor
		final int downscaleX = sme.width / 2;
		final int downscaleY = sme.height / 2;
		final int normalizeFactor = downscaleX * downscaleY;
		final int xStep = 3;
		final int xScaledStep = 3 * downscaleX;
		final byte[][] texture = sme.map.textureMap.textureMap;
		
		int minimapx, minimapy, texturex, texturey, filterx, filtery;
		int r, g, b;
		
		//TODO set minimap color to blue, where height < 0
		texturey = 0;
		for (minimapy = 0; minimapy < height; minimapy++)
		{
			texturex = 0;
			for (minimapx = 0; minimapx < scanlineSize; minimapx += xStep)
			{
				r = 0;
				g = 0;
				b = 0;
				for (filtery = 0; filtery < downscaleY; filtery++)
				{
					for (filterx = 0; filterx < xScaledStep; filterx += xStep)
					{
						r += (texture[texturey + filtery][(texturex + filterx) + 0] & 0xFF);
						g += (texture[texturey + filtery][(texturex + filterx) + 1] & 0xFF);
						b += (texture[texturey + filtery][(texturex + filterx) + 2] & 0xFF);
					}
				}
				minimapData[minimapy][minimapx + 0] = (byte)(r / normalizeFactor);
				minimapData[minimapy][minimapx + 1] = (byte)(g / normalizeFactor);
				minimapData[minimapy][minimapx + 2] = (byte)(b / normalizeFactor);
				
				texturex += xScaledStep;
			}
			texturey += downscaleY;
			
			//Status output
			if ((minimapy % oneTenthsOfHeight) == 0) System.out.print("#");
		}
		
		//Copy minimap data into buffer
		ByteBuffer uncompressed = ByteBuffer.allocate(width * height * 3);
		for (int y = 0; y < height; y++)
		{
			//Copy whole scanline at once
			uncompressed.put(minimapData[y], 0, scanlineSize);
		}
		uncompressed.flip();
		
		//Generate new texture
		int[] tmp = new int[1];
		staticGl.glGenTextures(1, tmp, 0);
		staticGl.glBindTexture(GL.GL_TEXTURE_2D, tmp[0]);
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_TRUE); //Enable mipmap generation
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		staticGl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT, width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, uncompressed);
		
		//Get back compressed data
		int miplevels = 9;
		int[] sizes = new int[] { width, width/2, width/4, width/8, width/16, width/32, width/64, width/128, width/256 };
		for (int i = 0; i < miplevels; i++)
			sizes[i] = (sizes[i] * sizes[i]) * 3 / 6; //6 is compression ratio for DXT1. 24bit to 4bit per Pixel
		ByteBuffer compressed = ByteBuffer.allocate(sizes[0]);
		byte[] compressedArray = new byte[SM2File.compressedMinimapSize];
		int curIndex = 0;
		for (int i = 0; i < miplevels; i++)
		{
			compressed.clear();
			staticGl.glGetCompressedTexImage(GL.GL_TEXTURE_2D, i, compressed);
			compressed.get(compressedArray, curIndex, sizes[i]);
			curIndex += sizes[i];
		}
		
		//Delete texture
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_FALSE); //Disable mipmap generation
		staticGl.glDeleteTextures(1, tmp, 0);
		
		return compressedArray;
	}
	
	public void initCompressedTileData()
	{
		//Generate new texture
		int[] tmp = new int[1];
		staticGl.glGenTextures(1, tmp, 0);
		staticGl.glBindTexture(GL.GL_TEXTURE_2D, tmp[0]);
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_TRUE); //Enable mipmap generation
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	}
	
	public void getCompressedTileData(byte[] compressedArray, int pixelX, int pixelY, int tileSizeInPixels)
	{
		final int height = tileSizeInPixels;
		final int width = tileSizeInPixels;
		int scanlineSize = width * 3;
		ByteBuffer uncompressed = ByteBuffer.allocate(width * height * 3);
				
		//copy image from texturemap
		for (int y = 0; y < height; y++)
		{
			//Copy whole scanline at once
			uncompressed.put(sme.map.textureMap.textureMap[pixelY + y], pixelX * 3, scanlineSize);
		}
		uncompressed.flip();
		
		//Load uncompressed data into openGL
		staticGl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_COMPRESSED_RGB_S3TC_DXT1_EXT, width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, uncompressed);
		
		//Get back compressed data
		int miplevels = 4;
		int[] sizes = new int[] { width, width/2, width/4, width/8 };
		for (int i = 0; i < miplevels; i++)
			sizes[i] = (sizes[i] * sizes[i]) * 3 / 6; //6 is compression ratio for DXT1. 24bit to 4bit per Pixel
		ByteBuffer compressed = ByteBuffer.allocate(sizes[0]);
		int curIndex = 0;
		for (int i = 0; i < miplevels; i++)
		{
			compressed.clear();
			staticGl.glGetCompressedTexImage(GL.GL_TEXTURE_2D, i, compressed);
			compressed.get(compressedArray, curIndex, sizes[i]);
			curIndex += sizes[i];
		}
	}
	
	public void cleanCompressTileData()
	{
		//Disable mipmap generation
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_FALSE);
		
		//Delete texture
		int[] tmp = new int[1];
		tmp[0] = tempTexture;
		tempTexture = 0;
		staticGl.glDeleteTextures(1, tmp, 0);
	}
	
	public void initDecompressTileData()
	{
		//Generate new texture
		int[] tmp = new int[1];
		tempTexture = tmp[0];
		staticGl.glGenTextures(1, tmp, 0);
		staticGl.glBindTexture(GL.GL_TEXTURE_2D, tempTexture);
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	}
	
	/**
	 * NOTE: You need to call initDecompressTileData() before using this method,<BR>
	 * and cleanDecompressTileData() after you have finished using this method.<BR>
	 * For example:<BR>
	 * <BR>
	 * initDecompressTileData<BR>
	 * getDecompressedTileData<BR>
	 * getDecompressedTileData<BR>
	 * getDecompressedTileData<BR>
	 * cleanDecompressTileData<BR>
	 * <BR>
	 * @param compressedData
	 * @param uncompressedData
	 * @param tileSizeInPixels
	 */
	public void getDecompressedTileData(ByteBuffer compressed, ByteBuffer uncompressed, int tileSizeInPixels)
	{
		final int height = tileSizeInPixels;
		final int width = tileSizeInPixels;
		
		//Load compressed data to openGL
		staticGl.glCompressedTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_COMPRESSED_RGBA_S3TC_DXT1_EXT, width, height, 0, (width / 4) * (height / 4) * 8, compressed);
		
		//Get back uncompressed data
		staticGl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, uncompressed);
	}
	
	public void cleanDecompressTileData()
	{
		//Delete texture
		int[] tmp = new int[1];
		tmp[0] = tempTexture;
		tempTexture = 0;
		staticGl.glDeleteTextures(1, tmp, 0);
	}
	
	public FeatureManager getFeatureManager()
	{
		return featureManager;
	}
	
	public void getFeatureImageData(byte[] dataArray, int featureID, long gameFrame)
	{
		//Bind Framebuffer
		staticGl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, featureFBOID);
		staticGl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, featureTexID, 0);
	    
		staticGl.glViewport(0, 0, rs.featureTexSize, rs.featureTexSize);
		staticGl.glMatrixMode(GL.GL_PROJECTION);
		staticGl.glLoadIdentity();
	    glu.gluPerspective(60, 1, 1, 1000);
		setCameraPosition(staticGl, rs.featureCameraPosition);
		
		staticGl.glMatrixMode(GL.GL_MODELVIEW);
		staticGl.glLoadIdentity();
		
		//TODO Do not force feature loading every frame
		if (!featureManager.isFeatureLoaded(featureID))
		{
			featureManager.loadFeature(staticGl, featureID);
		}
		
		//Render Feature
		staticGl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
		staticGl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		staticGl.glColor3f(1, 1, 1);
		staticGl.glEnable(GL.GL_TEXTURE_2D);
		FeatureMapContainer[] renderList = new FeatureMapContainer[1];
		float height = featureManager.getMaxHeight(featureID);
		float width = featureManager.getMaxWidth(featureID) * 2;
		if (height > width)
			renderList[0] = new FeatureMapContainer(0, -height/2, height, gameFrame % 360, featureID); //210
		else
			renderList[0] = new FeatureMapContainer(0, -height/2, width, gameFrame % 360, featureID);
		renderFeatureList(staticGl, renderList, 1);
		
		staticGl.glDisable(GL.GL_TEXTURE_2D);
		
		//Unbind Framebuffer
		staticGl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
		staticGl.glViewport(0, 0, rs.displayWidth, rs.displayHeight);
		
		//Read out texture
		staticGl.glBindTexture(GL.GL_TEXTURE_2D, featureTexID);
		
		int size = rs.featureTexSize * rs.featureTexSize * 3;
		ByteBuffer data = ByteBuffer.allocate(size);
		
		staticGl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data);
		data.get(dataArray, 0, size);
	}
	
	private void createVBOData(FloatBuffer vbo, float map[][], int xOffset, int yOffset, int width, int height, float maxHeight)
	{
		//Clear Buffer
		vbo.clear();
		
		int widthInTiles = width - 1;
		int heightInTiles = height - 1;
		float texFractionX = 1 / (float)widthInTiles;
		float texFractionY = 1 / (float)heightInTiles;
		int tileSize = rs.quadSize;
		
		int x, y;
		Vector3 v1, v2, v3, v4, vBaseNormal;
		v4 = null;
    	int xLocal;
    	int yLocal = 0;
		for (y = yOffset; y < (yOffset + heightInTiles); y++)
		{
			xLocal = 0;
			x = xOffset;
			
			v1 = new Vector3(-rs.quadHalfSize + (x * rs.quadSize), map[x-1][y-1] * maxHeight, -rs.quadHalfSize + (y * rs.quadSize));
			v2 = new Vector3(-rs.quadHalfSize + (x * rs.quadSize), map[x-1][y] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));
			v3 = new Vector3(rs.quadHalfSize + (x * rs.quadSize), map[x][y-1] * maxHeight, -rs.quadHalfSize + (y * rs.quadSize));
			
			vBaseNormal = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3)).normalize();
			
			/* TEXCOORD */vbo.put(0 + (xLocal / (float)widthInTiles)); vbo.put(0 + (yLocal / (float)heightInTiles));
			/* NORMAL   */vbo.put(getSmoothedNormal(map, x-1, y-1, width, height, vBaseNormal, 1, tileSize, maxHeight).vector, 0, 3);
			/* VERTEX   */vbo.put(v1.vector, 0, 3);
			
			/* TEXCOORD */vbo.put(0 + (xLocal / (float)widthInTiles)); vbo.put(texFractionY + (yLocal / (float)heightInTiles));
			/* NORMAL   */vbo.put(getSmoothedNormal(map, x-1, y, width, height, vBaseNormal, 1, tileSize, maxHeight).vector, 0, 3);
			/* VERTEX   */vbo.put(v2.vector, 0, 3);
			
			for (; x < (xOffset + widthInTiles); x++)
			{
				v3 = new Vector3(rs.quadHalfSize + (x * rs.quadSize), map[x][y-1] * maxHeight, -rs.quadHalfSize + (y * rs.quadSize));
				v4 = new Vector3(rs.quadHalfSize + (x * rs.quadSize), map[x][y] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));
				
				vBaseNormal = Vector3Math.crossProduct(Vector3Math.subVectors(v1, v2), Vector3Math.subVectors(v2, v3)).normalize();
				
				/* TEXCOORD */vbo.put(texFractionX + (xLocal / (float)widthInTiles)); vbo.put(0 + (yLocal / (float)heightInTiles));
				/* NORMAL   */vbo.put(getSmoothedNormal(map, x, y-1, width, height, vBaseNormal, 1, tileSize, maxHeight).vector, 0, 3);
				/* VERTEX   */vbo.put(v3.vector, 0, 3);
				
				/* TEXCOORD */vbo.put(texFractionX + (xLocal / (float)widthInTiles)); vbo.put(texFractionY + (yLocal / (float)heightInTiles));
				/* NORMAL   */vbo.put(getSmoothedNormal(map, x, y, width, height, vBaseNormal, 1, tileSize, maxHeight).vector, 0, 3);
				/* VERTEX   */vbo.put(v4.vector, 0, 3);
			    
				//Copy last 2 vectors to new first ones
				v1 = v3;
				v2 = v4;
				
			    xLocal++;
			}
			
			//We need to insert null triangles here, for lf+cr
			//Last point again
			/* TEXCOORD */vbo.put(0); vbo.put(0);
			/* NORMAL   */vbo.put(nullVector.vector, 0, 3);
			/* VERTEX   */vbo.put(v4.vector, 0, 3);
			
			//First point of next row again (=second point of this row)
			v2 = new Vector3(-rs.quadHalfSize + (xOffset * rs.quadSize), map[xOffset-1][y] * maxHeight, rs.quadHalfSize + (y * rs.quadSize));
			/* TEXCOORD */vbo.put(0); vbo.put(0);
			/* NORMAL   */vbo.put(nullVector.vector, 0, 3);
			/* VERTEX   */vbo.put(v2.vector, 0, 3);
			yLocal++;
		}
		vbo.flip();
	}
	
	private void createTextureData(byte[][] r, byte[][] g, byte[][] b, ByteBuffer textureData, int xOffset, int yOffset, int width, int height)
	{
		textureData.clear();
		
		final int scanlineSize = width * 3;
		byte[] tmpScanline = new byte[scanlineSize];
		int x, y, currentByte;
		for (y = yOffset; y < (yOffset + height); y++)
		{
			//Create scanline
			currentByte = 0;
			for (x = xOffset; x < (xOffset + width); x++)
			{
				tmpScanline[currentByte + 0] = (byte)(r[x][y] & 0xFF);
				tmpScanline[currentByte + 1] = (byte)(g[x][y] & 0xFF);
				tmpScanline[currentByte + 2] = (byte)(b[x][y] & 0xFF);
				
				currentByte += 3;
			}
			//Copy whole scanline at once
			textureData.put(tmpScanline, 0, scanlineSize);
		}
		
		textureData.flip();
	}
	
	public void getPrefabImageData(byte[] dataArray, BrushPattern heightmap, BrushTexture texturemap, long gameFrame, float scaleHeight)
	{
		//Calculate size
		int hmWidth;
		int hmHeight;
		float[][] heightmapData;
		if (heightmap == null)
		{
			hmWidth = (texturemap.width / 8) + 1;
			hmHeight = (texturemap.height / 8) + 1;
			heightmapData = new float[hmWidth][hmHeight];
			for (int x = 0; x < hmWidth; x++)
				Arrays.fill(heightmapData[x], 0.01f);
		}
		else
		{
			hmWidth = heightmap.width;
			hmHeight = heightmap.height;
			heightmapData = heightmap.getPattern();
		}
		
		//Bind Framebuffer
		staticGl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, featureFBOID);
		staticGl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, featureTexID, 0);
	    
		staticGl.glViewport(0, 0, rs.featureTexSize, rs.featureTexSize);
		staticGl.glMatrixMode(GL.GL_PROJECTION);
		staticGl.glLoadIdentity();
	    glu.gluPerspective(60, 1, 1, 1000);
		setCameraPosition(staticGl, rs.prefabCameraPosition);
		
		staticGl.glMatrixMode(GL.GL_MODELVIEW);
		staticGl.glLoadIdentity();
		int prefabSize = Math.max(hmHeight, hmWidth);
		staticGl.glTranslatef(0, -(rs.quadSize * prefabSize), -(rs.quadSize * (prefabSize)));
		staticGl.glRotatef(gameFrame % 360, 0, 1, 0);
		staticGl.glTranslatef(-(rs.quadSize * (hmWidth / 2)), 0, -(rs.quadSize * (hmHeight / 2)));
		
		//Create VBO Data
		
		int triangleCount = ((hmWidth - 1) * (hmHeight - 1) * 2) + ((hmHeight - 1) * 2);
		int vertexCount = triangleCount + (2 * (hmHeight - 1));
		FloatBuffer vbo = FloatBuffer.allocate(vertexCount * 8); //3Vertex 3Normal 2TexCoord
		createVBOData(vbo, heightmapData, 1, 1, hmWidth, hmHeight, scaleHeight * sme.map.maxHeight/4f);
		
		int[] tmp = new int[1];
		staticGl.glGenBuffers(1, tmp, 0);
		staticGl.glBindBuffer(GL.GL_ARRAY_BUFFER, tmp[0]);
		staticGl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount * 8 * BufferUtil.SIZEOF_FLOAT, vbo, GL.GL_STREAM_DRAW);

		//Create Texture Data
		int[] texID = new int[1];
		if (texturemap != null)
		{
			ByteBuffer textureData = ByteBuffer.allocate(texturemap.width * texturemap.height * 3);
			createTextureData(texturemap.getTextureR(), texturemap.getTextureG(), texturemap.getTextureB(), textureData, 0, 0, texturemap.width, texturemap.height);
			
			staticGl.glGenTextures(1, texID, 0);
			staticGl.glBindTexture(GL.GL_TEXTURE_2D, texID[0]);
			staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
			staticGl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
			staticGl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB8, texturemap.width, texturemap.height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, textureData);
			staticGl.glEnable(GL.GL_TEXTURE_2D);
		}
		
		//Render Prefab
		staticGl.glClearColor(0.3f, 0.3f, 0.3f, 1f);
		staticGl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		staticGl.glColor3f(1, 1, 1);
		staticGl.glEnable(GL.GL_LIGHTING);
		
		//Set Format:
		staticGl.glInterleavedArrays(GL.GL_T2F_N3F_V3F, 0, 0);
        //Render Buffer
		staticGl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, vertexCount);		
		
		staticGl.glDisable(GL.GL_LIGHTING);
		if (texturemap != null) staticGl.glDisable(GL.GL_TEXTURE_2D);
		
		//Unbind Framebuffer
		staticGl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
		staticGl.glViewport(0, 0, rs.displayWidth, rs.displayHeight);
		
		//Delete VBO
		staticGl.glDeleteBuffers(1, tmp, 0);
		
		//Delete VBOTexture
		if (texturemap != null) staticGl.glDeleteTextures(1, texID, 0);
		
		//Read out texture
		staticGl.glBindTexture(GL.GL_TEXTURE_2D, featureTexID);
		
		int size = rs.featureTexSize * rs.featureTexSize * 3;
		ByteBuffer data = ByteBuffer.allocate(size);
		
		staticGl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, data);
		data.get(dataArray, 0, size);
	}
}
