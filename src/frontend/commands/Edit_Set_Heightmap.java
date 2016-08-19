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
 * Edit_Set_Heightmap.java 
 * Created on 11.02.2009
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.SpringMapEditGUI;
import frontend.render.brushes.HeightBrush.HeightMode;

/**
 * @author Heiko Schmitt
 *
 */
public class Edit_Set_Heightmap extends SpringMapEditGUICommand
{
	public Edit_Set_Heightmap(SpringMapEditGUI smeGUI)
	{
		super(smeGUI);
	}

	@Override
	public void execute(Object[] data2)
	{
		//Store current mode
		int hm = smeGUI.sme.mes.getHeightBrush().brushMode;
		
		//Change to setMode, retrieve value, and restore mode
		smeGUI.sme.mes.getHeightBrush().brushMode = 2;//HeightMode.Set;
		float strength = smeGUI.sme.mes.getHeightBrush().getStrength();
		smeGUI.sme.mes.getHeightBrush().brushMode = hm;
		
		smeGUI.sme.map.heightmap.setHeightToMap(strength);
		smeGUI.renderer.invalidateAllBlocks(true, false, false);
	}
}
