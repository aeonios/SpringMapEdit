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
 * FeatureMapContainer.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package frontend.render.features;

/**
 * @author Heiko Schmitt
 *
 */
public class FeatureMapContainer
implements Cloneable
{
	public float x;
	public float y;
	public float z;
	
	public float rotX;
	public float rotY;
	public float rotZ;
	
	public int featureID;
	
	public FeatureMapContainer(float x, float y, float z, float rotY, int featureID)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.rotX = 0;
		this.rotY = rotY;
		this.rotZ = 0;
		this.featureID = featureID;
	}
	
	@Override
	public FeatureMapContainer clone() throws CloneNotSupportedException
	{
		return (FeatureMapContainer)super.clone();
	}
}
