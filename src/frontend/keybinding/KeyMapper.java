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
 * KeyMapper.java 
 * Created on 20.12.2008
 * by Heiko Schmitt
 */
package frontend.keybinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import frontend.commands.*;
import org.eclipse.swt.SWT;

import frontend.gui.Command;
import frontend.gui.SpringMapEditGUI;
import frontend.gui.SpringMapEditGUI.HoldableKeys;
import frontend.render.MapRenderer.MapMode;

/**
 * @author Heiko Schmitt
 *
 */
public class KeyMapper
{
	private boolean containedUnbindAll = false;
	/**
	 * Contains a keycode -> commandName mapping
	 * (This will be customizable per User)
	 */
	private LinkedHashMap<String, String> keyMapPress;
	private LinkedHashMap<String, String> keyMapRelease;
	
	/**
	 * Contains a commandName -> Command mapping
	 * (This stays static)
	 */
	private LinkedHashMap<String, Command> commandMap;
	
	private static final String altString = "ALT";
	private static final String ctrlString = "CTRL";
	private static final String shiftString = "SHIFT";
	
	/**
	 * 
	 */
	public KeyMapper(SpringMapEditGUI smeGUI)
	{
		this.keyMapPress = new LinkedHashMap<String, String>();
		this.keyMapRelease = new LinkedHashMap<String, String>();
		this.commandMap = new LinkedHashMap<String, Command>();
		
		commandMap.put("+MOVE_LEFT", new Holdable_Handler(smeGUI, true, HoldableKeys.ARROW_LEFT));
		commandMap.put("-MOVE_LEFT", new Holdable_Handler(smeGUI, false, HoldableKeys.ARROW_LEFT));
		commandMap.put("+MOVE_RIGHT", new Holdable_Handler(smeGUI, true, HoldableKeys.ARROW_RIGHT));
		commandMap.put("-MOVE_RIGHT", new Holdable_Handler(smeGUI, false, HoldableKeys.ARROW_RIGHT));
		commandMap.put("+MOVE_FORWARD", new Holdable_Handler(smeGUI, true, HoldableKeys.ARROW_UP));
		commandMap.put("-MOVE_FORWARD", new Holdable_Handler(smeGUI, false, HoldableKeys.ARROW_UP));
		commandMap.put("+MOVE_BACKWARD", new Holdable_Handler(smeGUI, true, HoldableKeys.ARROW_DOWN));
		commandMap.put("-MOVE_BACKWARD", new Holdable_Handler(smeGUI, false, HoldableKeys.ARROW_DOWN));
		commandMap.put("+ZOOM_IN", new Holdable_Handler(smeGUI, true, HoldableKeys.PAGE_UP));
		commandMap.put("-ZOOM_IN", new Holdable_Handler(smeGUI, false, HoldableKeys.PAGE_UP));
		commandMap.put("+ZOOM_OUT", new Holdable_Handler(smeGUI, true, HoldableKeys.PAGE_DOWN));
		commandMap.put("-ZOOM_OUT", new Holdable_Handler(smeGUI, false, HoldableKeys.PAGE_DOWN));
		
		commandMap.put("+PRESS_SHIFT", new Holdable_Handler(smeGUI, true, HoldableKeys.SHIFT));
		commandMap.put("-PRESS_SHIFT", new Holdable_Handler(smeGUI, false, HoldableKeys.SHIFT));
		commandMap.put("+PRESS_CTRL", new Holdable_Handler(smeGUI, true, HoldableKeys.CTRL));
		commandMap.put("-PRESS_CTRL", new Holdable_Handler(smeGUI, false, HoldableKeys.CTRL));
		commandMap.put("+PRESS_ALT", new Holdable_Handler(smeGUI, true, HoldableKeys.ALT));
		commandMap.put("-PRESS_ALT", new Holdable_Handler(smeGUI, false, HoldableKeys.ALT));
		
		commandMap.put("+PRESS_MOUSE_1", new Holdable_Handler(smeGUI, true, HoldableKeys.MOUSE_1));
		commandMap.put("-PRESS_MOUSE_1", new Holdable_Handler(smeGUI, false, HoldableKeys.MOUSE_1));
		commandMap.put("+PRESS_MOUSE_2", new Holdable_Handler(smeGUI, true, HoldableKeys.MOUSE_2));
		commandMap.put("-PRESS_MOUSE_2", new Holdable_Handler(smeGUI, false, HoldableKeys.MOUSE_2));
		commandMap.put("+PRESS_MOUSE_3", new Holdable_Handler(smeGUI, true, HoldableKeys.MOUSE_3));
		commandMap.put("-PRESS_MOUSE_3", new Holdable_Handler(smeGUI, false, HoldableKeys.MOUSE_3));

		commandMap.put("SCROLL_UP", new Scroll(smeGUI, true));
		commandMap.put("SCROLL_DOWN", new Scroll(smeGUI, false));

		commandMap.put("RESET_CAMERA", new Reset_Camera(smeGUI));
		commandMap.put("VIEW_MOUSELOOK", new View_Mouselook(smeGUI));
		commandMap.put("VIEW_INVERT_MOUSE_Y", new View_Invert_Mouse_Y(smeGUI));
		
		commandMap.put("RENDER_WIREFRAMEMODE", new Render_WireframeMode(smeGUI));
		commandMap.put("RENDER_SHADERWATER", new Render_Shaderwater(smeGUI));
		commandMap.put("RENDER_FEATURE_LIGHTING", new Render_Feature_Lighting(smeGUI));
		commandMap.put("RENDER_LIGHTING", new Render_Lighting(smeGUI));
		
		commandMap.put("RENDER_DISABLE_LOD", new Render_Disable_LOD(smeGUI));
		commandMap.put("RENDER_SMOOTH_NORMALS", new Render_Smooth_Normals(smeGUI));
		commandMap.put("RENDER_FAST_SMOOTH_NORMALS", new Render_Fast_Smooth_Normals(smeGUI));
		commandMap.put("RENDER_FILTER_TEXTURES", new Render_Filter_Textures(smeGUI));
		
		commandMap.put("RENDER_VIEW_SLOPEMAP", new Render_Mapmode(smeGUI, MapMode.SlopeMap));
		commandMap.put("RENDER_VIEW_TYPEMAP", new Render_Mapmode(smeGUI, MapMode.TypeMap));
		commandMap.put("RENDER_VIEW_VEGETATIONMAP", new Render_Mapmode(smeGUI, MapMode.VegetationMap));
		commandMap.put("RENDER_VIEW_METALMAP", new Render_Mapmode(smeGUI, MapMode.MetalMap));
		commandMap.put("RENDER_VIEW_TEXTUREMAP", new Render_Mapmode(smeGUI, MapMode.TextureMap));
		commandMap.put("RENDER_VIEW_BLEND_TEXTUREMAP", new Render_BlendTextureMap(smeGUI));
		
		commandMap.put("EDIT_SET_HEIGHTMAP", new Edit_Set_Heightmap(smeGUI));
		commandMap.put("EDIT_RANDOMIZE_HEIGHTMAP", new Edit_Randomize_Heightmap(smeGUI));
		commandMap.put("EDIT_BLANK_TEXTURE", new Edit_Blank_Texture(smeGUI));
		commandMap.put("EDIT_SMOOTH_HEIGHTMAP", new Edit_Smooth_Heightmap(smeGUI));
		commandMap.put("EDIT_ERODE_HEIGHTMAP_WET", new Edit_Erode_Heightmap_Wet(smeGUI));
		commandMap.put("EDIT_ERODE_HEIGHTMAP_DRY", new Edit_Erode_Heightmap_Dry(smeGUI));
		commandMap.put("EDIT_TTDIZE_HEIGHTMAP", new Edit_TTDIZE_Heightmap(smeGUI));
		commandMap.put("EDIT_AUTOGEN_TEXTURE", new Edit_Autogen_Texture(smeGUI));
		commandMap.put("EDIT_QUICKSAVE_MAP", new Edit_Quicksave_Map(smeGUI));
		commandMap.put("EDIT_QUICKLOAD_MAP", new Edit_Quickload_Map(smeGUI));
		
		createDefaultMapping();
	}
	
	private void createDefaultMapping()
	{
		setMapping(Integer.toString(SWT.ARROW_LEFT), "+MOVE_LEFT");
		setMapping(Integer.toString(SWT.ARROW_RIGHT), "+MOVE_RIGHT");
		setMapping(Integer.toString(SWT.ARROW_UP), "+MOVE_FORWARD");
		setMapping(Integer.toString(SWT.ARROW_DOWN), "+MOVE_BACKWARD");
		setMapping(Integer.toString(SWT.PAGE_UP), "+ZOOM_IN");
		setMapping(Integer.toString(SWT.PAGE_DOWN), "+ZOOM_OUT");
		setMapping(Integer.toString(SWT.TAB), "RESET_CAMERA");
		setMapping(Integer.toString((int)'l'), "VIEW_MOUSELOOK");
		
		setMapping(Integer.toString(SWT.SHIFT), "+PRESS_SHIFT");
		setMapping(Integer.toString(SWT.CTRL), "+PRESS_CTRL");
		setMapping(Integer.toString(SWT.ALT), "+PRESS_ALT");
		
		setMapping(Integer.toString(1), "+PRESS_MOUSE_1");
		setMapping(Integer.toString(2), "+PRESS_MOUSE_2");
		setMapping(Integer.toString(3), "+PRESS_MOUSE_3");

		setMapping(Integer.toString((int)'b'), "VIEW_INVERT_MOUSE_Y");
		setMapping(Integer.toString((int)'w'), "RENDER_WIREFRAMEMODE");

		setMapping(Integer.toString((int)'q'), "RENDER_SHADERWATER");
		setMapping(Integer.toString((int)'v'), "RENDER_FEATURE_LIGHTING");
		setMapping(Integer.toString((int)'h'), "RENDER_LIGHTING");
		setMapping(Integer.toString((int)'o'), "RENDER_DISABLE_LOD");
		setMapping(Integer.toString((int)'n'), "RENDER_SMOOTH_NORMALS");
		setMapping(Integer.toString((int)'m'), "RENDER_FAST_SMOOTH_NORMALS");
		setMapping(Integer.toString((int)'f'), "RENDER_FILTER_TEXTURES");
		
		setMapping(Integer.toString(SWT.F1), "RENDER_VIEW_SLOPEMAP");
		setMapping(Integer.toString(SWT.F2), "RENDER_VIEW_TYPEMAP");
		setMapping(Integer.toString(SWT.F3), "RENDER_VIEW_VEGETATIONMAP");
		setMapping(Integer.toString(SWT.F4), "RENDER_VIEW_METALMAP");
		setMapping(Integer.toString(SWT.F5), "RENDER_VIEW_TEXTUREMAP");
		setMapping(Integer.toString(SWT.F6), "RENDER_VIEW_BLEND_TEXTUREMAP");
		
		setMapping(Integer.toString(SWT.F8), "EDIT_QUICKSAVE_MAP");
		setMapping(Integer.toString(SWT.F12), "EDIT_QUICKLOAD_MAP");
		
		setMapping(Integer.toString((int)'x'), "EDIT_SET_HEIGHTMAP");
		setMapping(Integer.toString((int)'c'), "EDIT_RANDOMIZE_HEIGHTMAP");
		setMapping(Integer.toString((int)'z'), "EDIT_BLANK_TEXTURE");
		setMapping(Integer.toString((int)'s'), "EDIT_SMOOTH_HEIGHTMAP");
		setMapping(Integer.toString((int)'e'), "EDIT_ERODE_HEIGHTMAP_WET");
		setMapping(Integer.toString((int)'t'), "EDIT_ERODE_HEIGHTMAP_DRY");
		setMapping(Integer.toString((int)'p'), "EDIT_TTDIZE_HEIGHTMAP");
		setMapping(Integer.toString((int)'g'), "EDIT_AUTOGEN_TEXTURE");
	}

	public boolean setMapping(String key, String cmdName)
	{
		if (commandMap.containsKey(cmdName))
		{
			keyMapPress.put(key, cmdName);
			if (cmdName.charAt(0) == '+')
				keyMapRelease.put(key, cmdName.replace('+', '-'));
			return true;
		}
		else
		{
			System.out.println("Keymapper could not find command: " + cmdName);
			return false;
		}
	}
	
	public void loadFromFile(File file)
	{
		/*
		 * File Format:
		 * <keycode>#9<commandid>
		 */
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			String[] splittedLine;
			int lineLength;
			int lineNumber = 1;
			while (line != null)
			{
				splittedLine = line.split("\t");
				lineLength = splittedLine.length;
				//Check minimum line length
				if (lineLength >= 2)
				{
					//check for comment
					if (splittedLine[0].charAt(0) != '#')
					{
						if (!setMapping(splittedLine[0], splittedLine[1]))
							System.out.println("Check your config/keys.cfg in line " + lineNumber);	
					}
				}
				else
				{
					if (splittedLine[0].equals("unbindall"))
					{
						containedUnbindAll = true;
						keyMapPress.clear();
						keyMapRelease.clear();
					}
				}
				line = br.readLine();
				lineNumber++;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void saveToFile(File file)
	{
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file, false));
			
			if (containedUnbindAll)
				bw.write("unbindall\n");
			
			Iterator<String> it = keyMapPress.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next();
				bw.write(key + "\t" + keyMapPress.get(key) + "\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (bw != null)
			{
				try
				{
					bw.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Returns appropriate command for given keycode, or null if nothing bound.<BR>
	 * This tries to retrieve the command with all given modifiers first.<BR>
	 * If nothing was found all modifiers are tried alone: 1. SHIFT, 2. CTRL, 3. ALT<BR>
	 * So if you press ALT+CTRL+A it will try CTRL+A first.<BR>
	 * If still nothing is found the keyCode alone will be tried.<BR>
	 * @return
	 */
	public Command getCommandByKeyCode(boolean pressed, int keyCode, boolean shiftPressed, boolean ctrlPressed, boolean altPressed)
	{
		String keyCodeString = Integer.toString(keyCode);
		LinkedHashMap<String, String> keyMap = (pressed ? keyMapPress : keyMapRelease);
		
		//Check with all modifiers
		String key = (shiftPressed ? shiftString : "") + (ctrlPressed ? ctrlString : "") + (altPressed ? altString : "") + keyCodeString;
		String cmdName = keyMap.get(key);
		if (cmdName != null)
			return commandMap.get(cmdName);
		
		//Shift only
		if (shiftPressed)
		{
			key = shiftString + keyCodeString;
			cmdName = keyMap.get(key);
			if (cmdName != null)
				return commandMap.get(cmdName);
		}
		
		//Ctrl only
		if (ctrlPressed)
		{
			key = ctrlString + keyCodeString;
			cmdName = keyMap.get(key);
			if (cmdName != null)
				return commandMap.get(cmdName);
		}
		
		//Alt only
		if (altPressed)
		{
			key = altString + keyCodeString;
			cmdName = keyMap.get(key);
			if (cmdName != null)
				return commandMap.get(cmdName);
		}
		
		//KeyCode only
		key = keyCodeString;
		cmdName = keyMap.get(key);
		if (cmdName != null)
			return commandMap.get(cmdName);
		
		//Nothing found...
		return null;
	}

	public Command getScrollCommand(boolean up){
		if (up){
			return commandMap.get("SCROLL_UP");
		}else{
			return commandMap.get("SCROLL_DOWN");
		}
	}
}
