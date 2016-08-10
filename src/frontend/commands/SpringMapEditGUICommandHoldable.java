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
 * SpringMapEditGUICommandHoldable.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import frontend.gui.SpringMapEditGUI;

/**
 * @author Heiko Schmitt
 *
 */
public abstract class SpringMapEditGUICommandHoldable extends SpringMapEditGUICommand
{
	protected boolean press;
	
	public SpringMapEditGUICommandHoldable(SpringMapEditGUI smeGUI, boolean press)
	{
		super(smeGUI);
		this.press = press;
	}
}
