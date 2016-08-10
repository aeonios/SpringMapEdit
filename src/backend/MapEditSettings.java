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
 * MapEditSettings.java 
 * Created on 10.10.2008
 * by Heiko Schmitt
 */
package backend;

import java.io.File;
import java.io.IOException;

import backend.math.Vector2Int;

import frontend.render.AppSettings;
import frontend.render.brushes.Brush;
import frontend.render.brushes.BrushDataManager;
import frontend.render.brushes.BrushPattern;
import frontend.render.brushes.BrushPatternFactory;
import frontend.render.brushes.BrushTexture;
import frontend.render.brushes.BrushTextureFactory;
import frontend.render.brushes.FeatureBrush;
import frontend.render.brushes.HeightBrush;
import frontend.render.brushes.MetalBrush;
import frontend.render.brushes.PrefabBrush;
import frontend.render.brushes.PrefabManager;
import frontend.render.brushes.TextureBrush;
import frontend.render.brushes.TypeBrush;
import frontend.render.brushes.VegetationBrush;

/**
 * @author Heiko Schmitt
 *
 */
public class MapEditSettings
{
	//Brush Managers
	public BrushDataManager<BrushPattern> brushPatternManager;
	public BrushDataManager<BrushTexture> brushTextureManager;
	public PrefabManager prefabManager;
	
	//TODO: maybe make this float?
	public Vector2Int brushPos;
	public Vector2Int brushAlign;
	public float brushHeightAlign;
	public boolean useAlign;
	
	//Brush currently in use
	public Brush activeBrush;
	private BrushMode brushMode;
	
	private HeightBrush heightBrush;
	private TextureBrush textureBrush;
	private MetalBrush metalBrush; 
	private TypeBrush typeBrush;
	private VegetationBrush vegetationBrush;
	private FeatureBrush featureBrush;
	private PrefabBrush prefabBrush;
	private PrefabBrush copypasteBrush;
	
	private TextureGeneratorSetup textureGeneratorSetup;
	
	private ErosionSetup erosionSetup;
	
	//Terra Generator
	private TerraGenSetup terraGenSetup;
	
	//UI Info (default brush pattern/texture icon size)
	public final int defaultSize = 100;
	
	//Settings
	private AppSettings as;
	
	public SpringMapEdit sme;
	
	public enum BrushMode
	{
		Height,
		Texture,
		Metal,
		Type,
		Vegetation,
		Diffuse,
		Feature,
		Decal,
		Prefab,
		Copypaste,
	}
	
	public MapEditSettings(AppSettings as, SpringMapEdit smeX)
	{
		this.as = as;
		this.sme = smeX;
		init();
	}

	private void init()
	{
		brushPatternManager = new BrushDataManager<BrushPattern>(BrushDataManager.brushPatternPath, new BrushPatternFactory(), "Pattern", as);
		brushTextureManager = new BrushDataManager<BrushTexture>(BrushDataManager.brushTexturePath, new BrushTextureFactory(), "Texture", as);
		prefabManager = new PrefabManager(PrefabManager.defaultPrefabPath, as);
		
		//Initial Position
		brushPos = new Vector2Int(5, 5);
		useAlign = false;
		brushAlign = new Vector2Int(1, 1);
		brushHeightAlign = 0.001f;
		
		//initialize all brushes
		heightBrush = new HeightBrush(brushPatternManager, 0, 20, 20, sme);
		textureBrush = new TextureBrush(brushPatternManager, brushTextureManager, 0, 0, 20, 20, -1, -1, sme);
		metalBrush = new MetalBrush(brushPatternManager, 0, 20, 20, sme);
		typeBrush = new TypeBrush(brushPatternManager, 1, 0, 20, 20);
		vegetationBrush = new VegetationBrush(brushPatternManager, 1, 0, 20, 20);
		featureBrush = new FeatureBrush(brushPatternManager, 0, 10, 10, sme);
		prefabBrush = new PrefabBrush(prefabManager, brushPatternManager, 0, -1, -1, sme);
		copypasteBrush = new PrefabBrush(prefabManager, brushPatternManager, 0, -1, -1, sme);
		
		//Setup main mode
		setBrushMode(BrushMode.Height);
		
		//Setup Texture Generator
		setTextureGeneratorSetupByFile(new File(TextureGeneratorSetup.textureScriptPath + "default.tdf"));
		
		//Setup Erosion
		setErosionSetupByFile(new File(ErosionSetup.erosionScriptPath + "default.tdf"));
		
		//Setup TerraGen
		terraGenSetup = new TerraGenSetup();
	}
	
	public BrushMode getBrushMode()
	{
		return brushMode;
	}
	
	public void setBrushMode(BrushMode brushMode)
	{
		this.brushMode = brushMode;
		useAlign = false;
		switch (brushMode)
		{
			case Height: activeBrush = heightBrush; break;
			case Texture: activeBrush = textureBrush; break;
			case Metal: activeBrush = metalBrush; break;
			case Type: activeBrush = typeBrush; break;
			case Vegetation: activeBrush = vegetationBrush; break;
			case Feature: activeBrush = featureBrush; break;
			case Prefab: activeBrush = prefabBrush; useAlign = true; break;
			case Copypaste: activeBrush = copypasteBrush; break;
		}
	}
	
	public void setTextureGeneratorSetupByFile(File tgsFile)
	{
		try
		{
			textureGeneratorSetup = new TextureGeneratorSetup(tgsFile);
			textureGeneratorSetup.loadBrushTextures();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			textureGeneratorSetup = null;
		}
	}
	
	public void setErosionSetupByFile(File esFile)
	{
		try
		{
			erosionSetup = new ErosionSetup(esFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			erosionSetup = null;
		}
	}
	
	//////////////////
	//Brush Getters
	//////////////////
	public HeightBrush getHeightBrush()
	{
		return heightBrush;
	}
	
	public TextureBrush getTextureBrush()
	{
		return textureBrush;
	}
	
	public MetalBrush getMetalBrush()
	{
		return metalBrush;
	}
	
	public TypeBrush getTypeBrush()
	{
		return typeBrush;
	}
	
	public VegetationBrush getVegetationBrush()
	{
		return vegetationBrush;
	}
	
	public FeatureBrush getFeatureBrush()
	{
		return featureBrush;
	}
	
	public PrefabBrush getPrefabBrush()
	{
		return prefabBrush;
	}
	
	public PrefabBrush getCopypasteBrush()
	{
		return copypasteBrush;
	}
	
	public TextureGeneratorSetup getTextureGeneratorSetup()
	{
		return textureGeneratorSetup;
	}
	
	public ErosionSetup getErosionSetup()
	{
		return erosionSetup;
	}
	
	public TerraGenSetup getTerraGenSetup()
	{
		return terraGenSetup;
	}
	
	public void setBrushPos(int x, int y)
	{
		if (useAlign)
			brushPos.set(FastMath.round(x / (double)brushAlign.x()) * brushAlign.x(), FastMath.round(y / (double)brushAlign.y()) * brushAlign.y());
		else
			brushPos.set(x, y);
	}
}
