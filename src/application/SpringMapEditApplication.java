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
 * SpringMapEditApplication.java 
 * Created on 03.07.2008
 * by Heiko Schmitt
 */
package application;

import java.awt.Toolkit;
import java.io.File;

import frontend.gui.SpringMapEditGUI;
import frontend.render.AppSettings;

/**
 * @author Heiko Schmitt
 *
 */
public class SpringMapEditApplication
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int width = 1024;
		int height = 768;
		
		//Check available screen resolution
		if (Toolkit.getDefaultToolkit().getScreenSize().width <= width)
		{
			width = 800;
			height = 600;
		}
		
		//Load settings
		AppSettings settings = new AppSettings();
		settings.displayWidth = width;
		settings.displayHeight = height;
		settings.loadFromFile(new File("config/settings.cfg"));
		
		//Parse params which have immediate effect (settings related)
		CommandlineHandler clh = new CommandlineHandler(args, settings);
		
		//Create GUI
		SpringMapEditGUI smeGUI = new SpringMapEditGUI(settings);
		
		//Execute delayed commandline commands
		clh.addDelayedCommandsToQueue(smeGUI.glMessageQueue, smeGUI);
		
		//Run
		try 
		{
			smeGUI.run();
		}
		catch(InterruptedException e)
		{
		};
	}

}
