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
 * Edit_Autogen_Texture.java 
 * Created on 21.12.2008
 * by Heiko Schmitt
 */
package frontend.commands;

import org.eclipse.swt.widgets.Shell;

import frontend.gui.ProcessingDialog;
import frontend.gui.SpringMapEditGUI;

/**
 * @author Heiko Schmitt
 *
 */
public class Edit_Autogen_Texture extends SpringMapEditGUICommand
{
	public Edit_Autogen_Texture(SpringMapEditGUI smeGUI)
	{
		super(smeGUI);
		//shell = new Shell(parent);
	}

	@Override
	public void execute(Object[] data2)
	{
		//new ProcessingDialog(shell).close();
		smeGUI.sme.genColorsByHeight(0, 0, null);
		smeGUI.renderer.invalidateAllBlocks(false, true, false);
	}
}
