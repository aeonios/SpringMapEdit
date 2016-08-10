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
 * FileHandler.java 
 * Created on 10.10.2009
 * by Heiko Schmitt
 */
package backend;

import java.io.File;

/**
 * @author Heiko Schmitt
 *
 */
public abstract class FileHandler
{
	public enum FileFormat
	{
		//Heightmap Formats
		Bitmap8Bit,
		Raw16Bit,
		Raw16BitSM,
		PNG16Bit,
		
		//Texture Formats
		Bitmap24Bit
	}
	
	public static String[] bitmapSuffixs =
	{
		".bmp",
		".raw"
	};
	
	private static boolean checkSuffixInList(String suffix, String[] list)
	{
		int len = list.length;
		for (int i = 0; i < len; i++)
		{
			if (suffix.equalsIgnoreCase(list[i])) return true;
		}
		return false;
	}
	
	public static boolean isHandledByBitmap(File filename)
	{
		String name = filename.getName();
		int len = name.length();
		if (len >= 4)
		{
			String suffix = name.substring(len - 4, len);
			return (checkSuffixInList(suffix, bitmapSuffixs));
		}
		return false;
	}
	
	/**
	 * Removes extension from given filename
	 * @param original
	 * @return
	 */
	public static File removeExtension(File original)
	{
		String orig = original.getAbsolutePath();
		int lastDot = orig.lastIndexOf('.');
		int lastSeparator = Math.max(orig.lastIndexOf('/'), orig.lastIndexOf('\\'));
		if ((lastDot >= 0) && (lastDot > lastSeparator))
			return new File(orig.substring(0, lastDot));
		return original;
	}
	
	/**
	 * Set file extension to given value
	 * @param original
	 * @param wantedExtension e.g. ".bmp"
	 * @return
	 */
	public static File setFileExtension(File original, String wantedExtension)
	{
		if (original.getAbsolutePath().endsWith(wantedExtension))
			return original;
		else
			return new File(removeExtension(original).getAbsolutePath() + wantedExtension);
	}
}
