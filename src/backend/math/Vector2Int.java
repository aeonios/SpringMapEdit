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
 * Vector2Int.java 
 * Created on 29.09.2009
 * by Heiko Schmitt
 */
package backend.math;

/**
 * @author Heiko Schmitt
 *
 */
public class Vector2Int
{
	private static final int dimension = 2;
	public final int[] vector;
	
	public Vector2Int()
	{
		vector = new int[dimension];
	}
	
	public Vector2Int(int x0, int x1)
	{
		vector = new int[] { x0, x1 };
	}
	
	public int x() { return vector[0]; }
	public int y() { return vector[1]; }
	
	public void set(int x0, int x1)
	{
		vector[0] = x0;
		vector[1] = x1;
	}
	
	public float getLength()
	{
		return (float)Math.sqrt((vector[0] * vector[0]) + (vector[1] * vector[1]));
	}
	
	public Vector2Int getCopy()
	{
		return new Vector2Int(vector[0], vector[1]);
	}
}
