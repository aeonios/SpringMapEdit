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
 * FeatureComparator.java 
 * Created on 18.08.2008
 * by Heiko Schmitt
 */
package frontend.render.features;

import java.util.Comparator;

import frontend.gui.CameraPosition;

/**
 * @author Heiko Schmitt
 *
 */
public class FeatureComparator implements Comparator<FeatureMapContainer>
{
	private CameraPosition camPos;
	
	public FeatureComparator(CameraPosition camPos)
	{
		this.camPos = camPos;
	}
	
	public int compare(FeatureMapContainer cont1, FeatureMapContainer cont2)
	{
		//Calculate distance for given objects to cam
		float dx1 = cont1.x - camPos.camX;
		float dy1 = cont1.y - camPos.camY;
		float dz1 = cont1.z - camPos.camZ;
		float dx2 = cont2.x - camPos.camX;
		float dy2 = cont2.y - camPos.camY;
		float dz2 = cont2.z - camPos.camZ;
		
		float d = (dx2*dx2 + dy2*dy2 + dz2*dz2) - (dx1*dx1 + dy1*dy1 + dz1*dz1);
		if (d < 0)
			return -1;
		else if (d > 0)
			return 1;
		else
			return 0;
	}
}
