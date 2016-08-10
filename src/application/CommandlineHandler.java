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
 * CommandlineHandler.java 
 * Created on 21.04.2009
 * by Heiko Schmitt
 */
package application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import frontend.gui.Command;
import frontend.gui.SpringMapEditGUI;
import frontend.render.AppSettings;

/**
 * @author Heiko Schmitt
 *
 */
public class CommandlineHandler
{
	private List<Command> commandList;
	private AppSettings settings;
	
	/**
	 * 
	 */
	public CommandlineHandler(String[] args, AppSettings settings)
	{
		this.commandList = new ArrayList<Command>();
		this.settings = settings;
		parse(args);
	}
	
	private void parse(String[] args)
	{
		/*
		 * Immediate Settings (order does not matter):
		 * 
		 * -batchmode						//Disables rendering and hides GUI. Optimizes memory usage,
		 * 									//speeds up batch processing, quits after executing all given commands.
		 * -mapwidth <N>					//initial mapsize in spring units 2-32
		 * -mapheight <N>					//saves some time if set to same values as to be loaded heightmap
		 * -texturemapdds <xyz.dds>			//path to precompressed DXT1 file (only for sm2 exporting)
		 * -mc_featurelist <xyz.tdf>		//path to mapconv style featurelist
		 * 
		 * 
		 * Batch commands (executed in given order):
		 * 
		 * -load_heightmap <xyz.bmp>		//load heightmapfile
		 * -load_texturemap <xyz.bmp>		//load texturemap
		 * -load_metalmap <xyz.bmp>			//load metalmap
		 * -load_typemap <xyz.bmp>			//load typemap
		 * -load_featuremap <xyz.fmf>		//load featuremap
		 * -load_mc_featuremap <xyz.bmp>	//load mapconv style featuremap (requires mc_featurelist)
		 * -load_vegetationmap <xyz.bmp>	//load vegetationmap
		 * -load_allmaps <xyz>				//load all maps
		 * -load_sm2 <xyz.smf>				//load sm2 map
		 * 
		 * -save_heightmap <xyz.bmp>		//save heightmapfile
		 * -save_texturemap <xyz.bmp>		//save texturemap
		 * -save_metalmap <xyz.bmp>			//save metalmap
		 * -save_typemap <xyz.bmp>			//save typemap
		 * -save_featuremap <xyz.fmf>		//save featuremap
		 * -save_mc_featuremap <xyz.bmp>	//save mapconv style featuremap (requires mc_featurelist)
		 * -save_vegetationmap <xyz.bmp>	//save vegetationmap
		 * -save_allmaps <xyz>				//save all maps in default format
		 * -save_sm2 <xyz.smf>				//save sm2 map
		 * 
		 * -terrainheight <MIN> <MAX>		//set terrainheight. (waterlevel is at 0)
		 * -erode <script.tdf> <type>   	//erode heightmap with given script. (type = WET, WET2 or DRY)
		 * -texgen <script.tdf>				//autotexture with given script
		 * -smooth <N>						//smooth heightmap. (N = iterations)
		 * 
		 */
	}

	public void addDelayedCommandsToQueue(Queue<Command> queue, final SpringMapEditGUI smeGUI)
	{
		//Add all given commands
		Iterator<Command> it = commandList.iterator();
		while (it.hasNext())
		{
			//TODO actual command creation takes place here...
			queue.offer(it.next());
		}
		
		//Add Quit Command if in batchmode
		if (settings.batchMode)
		{
			settings.quietExit = true; //Disable quit confirm dialog
			queue.offer(new Command(null)
			{
				@Override
				public void execute(Object[] data2)
				{
					smeGUI.shell.dispose();
				}
			});
		}
	}
}
