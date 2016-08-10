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
 * Feature.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package frontend.render.features;

import backend.tdf.TDFKeyValue;
import backend.tdf.TDFSection;

/**
 * @author Heiko Schmitt
 *
 */
public class Feature
{
	public int featureID;
	public int displayListID;
	public int textureID;
	public int alphaTextureID;
	public int triangleCount;
	public float maxHeight;
	public float maxWidth;
	
	public TDFSection featureTDF;
	public String stringID;
	public String featureFileName;
	public String textureFileName;
	
	public boolean isLoaded;
	
	public Feature(TDFSection featureTDF, int featureID)
	{
		this.featureTDF = featureTDF;
		this.featureID = featureID;
		this.isLoaded = false;
		this.triangleCount = 0;
		this.stringID = featureTDF.getName();
		this.featureFileName = getTDFValue("object");
		this.maxHeight = 0;
		this.maxWidth = 0;
	}
		
	public String getTDFValue(String key)
	{
		TDFKeyValue kv = featureTDF.getKeyValue(key);
		if (kv != null)
			return kv.getValue();
		else
			return null;
	}
}
