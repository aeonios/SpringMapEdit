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
 * RenderSettings.java 
 * Created on 06.07.2008
 * by Heiko Schmitt
 */
package frontend.render;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import backend.FastMath;
import backend.SpringMapEdit;

import frontend.gui.CameraPosition;
import frontend.render.MapRenderer.MapMode;
import frontend.gui.DialogBox;

/**
 * @author Heiko Schmitt
 *
 */
public class AppSettings
{
	//Tile info
	public int quadHalfSize;
	public int quadSize;
	
	//Display window info
	public int displayWidth = 800;
	public int displayHeight = 600;
	public int featureTexSize = 128;
	public int prefabSize = 32;
	
	//Ram usage
	public boolean keepBruhesInRam = true;
	
	//Camera
	public CameraPosition cameraPosition;
	public CameraPosition featureCameraPosition;
	public CameraPosition prefabCameraPosition;
	public float fov = 60;
	
	//Initialization
	public int initialMapWidth = 8;
	public int initialMapHeight = 8;
	
	//Level of Detail
	public boolean batchMode = false;
	public boolean smoothNormals = true;
	public int maxFeatureBlocksPerFrame = 16;
	public int maxFeaturesPerFrame = 32;
	public boolean fancyWater = false;
	public boolean useLighting = true;
	public int waterMapExtend = 200;
	public boolean moveSun = false;
	public boolean drawSun = false;
	public boolean onlyOutlineBrush = true;
	public boolean filterTextures = true;
	public MapMode mapMode = MapMode.TextureMap;
	public boolean blendTextureMap = true;
	public boolean compressTextures = true;
	public boolean fastNormals = false;
	public int maxFeaturesToDisplay = 10240;
	public boolean vsync = true;
	public boolean featureLighting = false;
	
	public int renderFeatureLOD = 3;
	public boolean useLOD = true;
	public float lodDist = 1000f;
	public int blockSize = 64;
	
	//Frame statistics
	public int trisRendered;
	public int fps;
	
	//Animation
	public float time = 0;
	public long gameFrame = 0;
	public boolean animateGUI = true;
	
	//Controls
	public boolean mouseLook = false;
	public boolean invertY = true;
	public float sensitivity = 1.5f;
	public float slowSpeed = 8;
	public float normalSpeed = 16;
	public float fastSpeed = 32;
	
	//Debugging
	public boolean outputPerfDebug = false;

	//Quicksave
	public boolean quicksave_compress = false;
	public boolean quicksave_heightmap = true;
	public boolean quicksave_texturemap = true;
	public boolean quicksave_metalmap = true;
	public boolean quicksave_typemap = true;
	public boolean quicksave_vegetationmap = true;
	public boolean quicksave_featuremap = true;

	public boolean quitWithoutAsking = false;
	public boolean quietExit = false; //Prevents quit message dialog
	
	//SM2 Compile Settings
	public String minimapFilename = "";
	public String texturemapFilename = "";
	
	//SM3 Editing Mode
	public boolean mapTypeSM3 = false;
	
	/**
	 * 
	 */
	public AppSettings()
	{
		quadSize = SpringMapEdit.tileSize;
		quadHalfSize = quadSize / 2;
		
		cameraPosition = new CameraPosition();
		featureCameraPosition = new CameraPosition();
		featureCameraPosition.camRotZ = 180;
		featureCameraPosition.camRotY = 180;
		prefabCameraPosition = new CameraPosition();
		prefabCameraPosition.camRotZ = 180;
		prefabCameraPosition.camRotX = 45;
	}
	
	private void setProperty(String propertyName, String[] values)
	{
		try
		{
			if (propertyName.equals("displayWidth")) displayWidth = Integer.parseInt(values[0]);
			else if (propertyName.equals("displayHeight")) displayHeight = Integer.parseInt(values[0]);
			else if (propertyName.equals("featureTexSize")) featureTexSize = Integer.parseInt(values[0]);
			else if (propertyName.equals("prefabSize")) prefabSize = Integer.parseInt(values[0]);
			else if (propertyName.equals("waterMapExtend")) waterMapExtend = Integer.parseInt(values[0]);
			else if (propertyName.equals("maxFeaturesToDisplay")) maxFeaturesToDisplay = Integer.parseInt(values[0]);
			else if (propertyName.equals("renderFeatureLOD")) renderFeatureLOD = Integer.parseInt(values[0]);
			
			//Boolean props
			else if (propertyName.equals("smoothNormals")) smoothNormals = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("fancyWater")) fancyWater = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("useLighting")) useLighting = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("moveSun")) moveSun = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("drawSun")) drawSun = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("onlyOutlineBrush")) onlyOutlineBrush = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("filterTextures")) filterTextures = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("compressTextures")) compressTextures = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("fastNormals")) fastNormals = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("vsync")) vsync = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("featureLighting")) featureLighting = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("useLOD")) useLOD = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("outputPerfDebug")) outputPerfDebug = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("invertY")) invertY = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("animateGUI")) animateGUI = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("keepBruhesInRam")) keepBruhesInRam = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_compress")) quicksave_compress = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_heightmap")) quicksave_heightmap = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_texturemap")) quicksave_texturemap = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_metalmap")) quicksave_metalmap = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_typemap")) quicksave_typemap = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_vegetationmap")) quicksave_vegetationmap = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_featuremap")) quicksave_featuremap = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quicksave_compress")) quicksave_compress = Boolean.parseBoolean(values[0]);
			else if (propertyName.equals("quitWithoutAsking")) quitWithoutAsking = Boolean.parseBoolean(values[0]);
			
			//Float props
			else if (propertyName.equals("sensitivity")) sensitivity = Float.parseFloat(values[0]);
			else if (propertyName.equals("slowSpeed")) slowSpeed = Float.parseFloat(values[0]);
			else if (propertyName.equals("normalSpeed")) normalSpeed = Float.parseFloat(values[0]);
			else if (propertyName.equals("fastSpeed")) fastSpeed = Float.parseFloat(values[0]);
			else if (propertyName.equals("lodDist")) lodDist = Float.parseFloat(values[0]);
		}	
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadFromFile(File file)
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			String[] splittedLine;
			int lineLength;
			while (line != null)
			{
				splittedLine = line.split("\t");
				lineLength = splittedLine.length;
				if (lineLength >= 2)
				{
					setProperty(splittedLine[0], Arrays.copyOfRange(splittedLine, 1, lineLength));
				}
				line = br.readLine();
			}
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					System.err.println(e.getMessage());
				}
			}
		}
	}

	public void saveToFile(File file)
	{
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file));

			bw.write("displayWidth" + "\t" + displayWidth + "\n");
			bw.write("displayHeight" + "\t" + displayHeight + "\n");
			bw.write("featureTexSize" + "\t" + featureTexSize + "\n");
			bw.write("prefabSize" + "\t" + prefabSize + "\n");
			bw.write("smoothNormals" + "\t" + smoothNormals + "\n");
			bw.write("fastNormals" + "\t" + fastNormals + "\n");
			bw.write("fancyWater" + "\t" + fancyWater + "\n");
			bw.write("useLighting" + "\t" + useLighting + "\n");
			bw.write("waterMapExtend" + "\t" + waterMapExtend + "\n");
			bw.write("moveSun" + "\t" + moveSun + "\n");
			bw.write("drawSun" + "\t" + drawSun + "\n");
			bw.write("onlyOutlineBrush" + "\t" + onlyOutlineBrush + "\n");
			bw.write("filterTextures" + "\t" + filterTextures + "\n");
			bw.write("compressTextures" + "\t" + compressTextures + "\n");
			bw.write("maxFeaturesToDisplay" + "\t" + maxFeaturesToDisplay + "\n");
			bw.write("vsync" + "\t" + vsync + "\n");
			bw.write("featureLighting" + "\t" + featureLighting + "\n");
			bw.write("renderFeatureLOD" + "\t" + renderFeatureLOD + "\n");
			bw.write("useLOD" + "\t" + useLOD + "\n");
			bw.write("outputPerfDebug" + "\t" + outputPerfDebug + "\n");
			bw.write("invertY" + "\t" + invertY + "\n");
			bw.write("sensitivity" + "\t" + sensitivity + "\n");
			bw.write("slowSpeed" + "\t" + slowSpeed + "\n");
			bw.write("normalSpeed" + "\t" + normalSpeed + "\n");
			bw.write("fastSpeed" + "\t" + fastSpeed + "\n");
			bw.write("animateGUI" + "\t" + animateGUI + "\n");
			bw.write("keepBruhesInRam" + "\t" + keepBruhesInRam + "\n");
			bw.write("quicksave_compress" + "\t" + quicksave_compress + "\n");
			bw.write("quicksave_heightmap" + "\t" + quicksave_heightmap + "\n");
			bw.write("quicksave_texturemap" + "\t" + quicksave_texturemap + "\n");
			bw.write("quicksave_metalmap" + "\t" + quicksave_metalmap + "\n");
			bw.write("quicksave_typemap" + "\t" + quicksave_typemap + "\n");
			bw.write("quicksave_vegetationmap" + "\t" + quicksave_vegetationmap + "\n");
			bw.write("quicksave_featuremap" + "\t" + quicksave_featuremap + "\n");
			bw.write("quitWithoutAsking" + "\t" + quitWithoutAsking + "\n");
			bw.write("lodDist" + "\t" + lodDist + "\n");

			bw.write("\n");
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
		finally
		{
			if (bw != null)
			{
				try
				{
					bw.close();
				}
				catch (IOException e)
				{
					System.err.println(e.getMessage());
				}
			}
		}
	}
}
