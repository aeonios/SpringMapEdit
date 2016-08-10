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
 * Vector3.java 
 * Created on 05.07.2008
 * by Heiko Schmitt
 */
package backend.math;

/**
 * @author Heiko Schmitt
 *
 */
public class Vector3
{
	private static final int dimension = 3;
	public final float[] vector;
	
	public Vector3()
	{
		vector = new float[dimension];
	}
	
	public Vector3(float x0, float x1, float x2)
	{
		vector = new float[] { x0, x1, x2 };
	}
	
	public float x() { return vector[0]; }
	public float y() { return vector[1]; }
	public float z() { return vector[2]; }
	
	public float getLength()
	{
		return (float)Math.sqrt((vector[0] * vector[0]) + (vector[1] * vector[1]) + (vector[2] * vector[2]));
	}
	
	public Vector3 normalize()
	{
		float length = getLength();
		if (length > 0)
		{
			for (int i = 0; i < dimension; i++)
				vector[i] = vector[i] / length;
		}
		return this;
	}
	
	public Vector3 getCopy()
	{
		return new Vector3(vector[0], vector[1], vector[2]);
	}
}
