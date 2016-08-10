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
 * BrushData.java 
 * Created on 28.09.2009
 * by Heiko Schmitt
 */
package frontend.render.brushes;

import backend.math.Vector2Int;

/**
 * @author Heiko Schmitt
 *
 */
public interface BrushData
{
	public boolean isLoaded();
	public boolean loadFromFile();
	public BrushData getScaledChild(int width, int height, boolean keepAspect);
	public void rotate(boolean counterClockWise);
	public void mirror(boolean horizontal);
	public Vector2Int getDimension(); 
	public void unload();
}
