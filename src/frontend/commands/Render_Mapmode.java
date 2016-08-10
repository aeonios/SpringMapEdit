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
 * Render_Mapmode.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.SpringMapEditGUI;
import frontend.render.MapRenderer.MapMode;

/**
 * @author Heiko Schmitt
 *
 */
public class Render_Mapmode extends SpringMapEditGUICommand
{
	private MapMode mode;
	
	public Render_Mapmode(SpringMapEditGUI smeGUI, MapMode mode)
	{
		super(smeGUI);
		this.mode = mode;
	}

	@Override
	public void execute(Object[] data2)
	{
		smeGUI.as.mapMode = mode;
		smeGUI.renderer.invalidateAllBlocks(false, true, false);
	}
}
