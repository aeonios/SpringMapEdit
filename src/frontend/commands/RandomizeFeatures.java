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
 * Edit_Smooth_Heightmap.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import backend.SpringMapEdit;
import backend.math.Point;

import frontend.gui.SpringMapEditGUI;
import frontend.render.brushes.FeatureBrush.FeatureMode;
import frontend.render.features.FeatureMapContainer;

/**
 * @author Heiko Schmitt
 *
 */
public class RandomizeFeatures extends SpringMapEditGUICommand
{
	public RandomizeFeatures(SpringMapEditGUI smeGUI)
	{
		super(smeGUI);
	}

	@Override
	public void execute(Object[] data2)
	{
		ArrayList<FeatureMapContainer> features = new ArrayList<FeatureMapContainer>();
		Random r = new Random();
		for (int i = 0; i < smeGUI.sme.map.featuremap.featureBlockCount; i++)
		{
			Iterator<FeatureMapContainer> it = smeGUI.sme.map.featuremap.featureList[i].iterator();
			while (it.hasNext())
				features.add(it.next());
			smeGUI.sme.map.featuremap.featureList[i].clear();
		}
		for (int i = 0; i < features.size(); i++)
		{
			Point block;
			FeatureMapContainer feature = features.remove(i);
			do
			{
				float x = r.nextFloat() * (float)smeGUI.sme.map.heightmap.getHeightmapWidth() * SpringMapEdit.tileSize;
				float z = r.nextFloat() * (float)smeGUI.sme.map.heightmap.getHeightmapLength() * SpringMapEdit.tileSize;
				feature.x = x;
				feature.z = z;
				feature.rotY = r.nextFloat() * 360;
				block = smeGUI.sme.map.featuremap.getFeatureBlockByCoords(x, z);
			} while (block == null);
			smeGUI.sme.map.featuremap.featureList[block.x + (block.y * smeGUI.sme.map.featuremap.featureMapWidthInBlocks)].add(feature);
		}
		smeGUI.renderer.invalidateAllBlocks(false, false, true);
	}
}
