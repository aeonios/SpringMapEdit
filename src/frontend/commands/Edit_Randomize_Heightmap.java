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
 * Edit_Randomize_Heightmap.java 
 * Created on 11.02.2009
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.ProcessingDialog;
import frontend.gui.SpringMapEditGUI;

/**
 * @author Heiko Schmitt
 *
 */
public class Edit_Randomize_Heightmap extends SpringMapEditGUICommand
{
	public Edit_Randomize_Heightmap(SpringMapEditGUI smeGUI)
	{
		super(smeGUI);
	}

	@Override
	public void execute(Object[] data2)
	{
		int hm = smeGUI.sme.mes.getHeightBrush().brushMode;
		smeGUI.sme.mes.getHeightBrush().brushMode = 1;//HeightMode.Smooth;
		float strength = smeGUI.sme.mes.getHeightBrush().getStrength();
		smeGUI.sme.mes.getHeightBrush().brushMode = hm;
		smeGUI.sme.map.heightmap.genRandom(strength);
		smeGUI.renderer.invalidateAllBlocks(true, false, false);
	}
}
