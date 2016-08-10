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
 * Matrix3.java 
 * Created on 05.07.2008
 * by Heiko Schmitt
 */
package backend.math;

/**
 * @author Heiko Schmitt
 *
 */
public class Matrix3
{
	private static final int dimension = 3;
	public double[][] matrix;
	
	private static final double[][] UNITY_MATRIX3_MATRIX = new double[][] {
	/* x=0 */	{1, 0, 0},
	/* x=1 */	{0, 1, 0},
	/* x=2 */	{0, 0, 1}
	};	  /* y=  0  1  2 */
	private static final Matrix3 UNITY_MATRIX = new Matrix3(UNITY_MATRIX3_MATRIX);
	public static Matrix3 UNITY_MATRIX() { return UNITY_MATRIX.getCopy(); }
	
	public Matrix3()
	{
		matrix = new double[dimension][dimension];
	}
	
	public Matrix3(double[][] matrix)
	{
		this.matrix = matrix;
	}
	
	public Matrix3(double x0y0, double x1y0, double x2y0,
				  double x0y1, double x1y1, double x2y1,
				  double x0y2, double x1y2, double x2y2)
	{
		matrix = new double[][] 
		{
				{x0y0, x0y1, x0y2},
				{x1y0, x1y1, x1y2},
				{x2y0, x2y1, x2y2}
		};
	}
	
	public Matrix3 getCopy()
	{
		Matrix3 copy = new Matrix3();
		for (int y = 0; y < dimension; y++)
		{
			for (int x = 0; x < dimension; x++)
			{
				copy.matrix[x][y] = matrix[x][y];
			}
		}
		return copy;
	}
}
