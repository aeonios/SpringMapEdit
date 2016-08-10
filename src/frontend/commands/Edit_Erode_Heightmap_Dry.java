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
 * Edit_Erode_Heightmap_Dry.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.SpringMapEditGUI;

/**
 * @author Heiko Schmitt
 *
 */
public class Edit_Erode_Heightmap_Dry extends SpringMapEditGUICommand
{
	public Edit_Erode_Heightmap_Dry(SpringMapEditGUI smeGUI)
	{
		super(smeGUI);
	}

	@Override
	public void execute(Object[] data2)
	{
		smeGUI.sme.erodeMapDryWet(0, 0, smeGUI.sme.map.heightmap.getHeightmapWidth(), smeGUI.sme.map.heightmap.getHeightmapLength(), false);
		smeGUI.renderer.invalidateAllBlocks(true, false, false);
	}
}
