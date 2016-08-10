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
 * Render_Renderradius.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.SpringMapEditGUI;

/**
 * @author Heiko Schmitt
 *
 */
public class Render_Renderradius extends SpringMapEditGUICommand
{
	private int amount;

	public Render_Renderradius(SpringMapEditGUI smeGUI, int amount)
	{
		super(smeGUI);
		this.amount = amount;
	}

	@Override
	public void execute(Object[] data2)
	{
		smeGUI.as.renderRadius += (amount *  (smeGUI.renderer.getBlockSizeinTiles() / 2));
		if (smeGUI.as.renderRadius < smeGUI.renderer.getBlockSizeinTiles()) smeGUI.as.renderRadius = smeGUI.renderer.getBlockSizeinTiles();
		smeGUI.as.farClip = (smeGUI.as.renderRadius - smeGUI.renderer.getBlockSizeinTiles()) * smeGUI.as.quadSize;
		smeGUI.renderer.camViewChangedNotify();
	}

}
