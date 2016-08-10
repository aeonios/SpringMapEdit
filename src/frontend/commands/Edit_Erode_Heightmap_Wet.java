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
 * Edit_Erode_Heightmap_Wet.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.SpringMapEditGUI;
import backend.map.Heightmap;

/**
 * @author Heiko Schmitt
 *
 */
public class Edit_Erode_Heightmap_Wet extends SpringMapEditGUICommand
{
	public Edit_Erode_Heightmap_Wet(SpringMapEditGUI smeGUI)
	{
		super(smeGUI);
	}

	@Override
	public void execute(Object[] data2)
	{
		smeGUI.sme.map.heightmap.erodeMapWet(0, 0, smeGUI.sme.map.heightmap.getHeightmapWidth(), smeGUI.sme.map.heightmap.getHeightmapLength(), smeGUI.sme.mes.getErosionSetup());
		smeGUI.renderer.invalidateAllBlocks(true, false, false);
	}
}
