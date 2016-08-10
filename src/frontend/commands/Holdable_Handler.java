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
 * Holdable_Handler.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.SpringMapEditGUI;
import frontend.gui.SpringMapEditGUI.HoldableKeys;

/**
 * @author Heiko Schmitt
 *
 */
public class Holdable_Handler extends SpringMapEditGUICommandHoldable
{
	private HoldableKeys key;
	
	public Holdable_Handler(SpringMapEditGUI smeGUI, boolean press, HoldableKeys key)
	{
		super(smeGUI, press);
		this.key = key;
	}

	@Override
	public void execute(Object[] data2)
	{
		smeGUI.holdableKeys[key.ordinal()] = press;
		smeGUI.renderer.camViewChangedNotify();
	}
}
