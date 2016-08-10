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
 * FastMath.java 
 * Created on 06.01.2008
 * by Heiko Schmitt
 */
package backend;

/**
 * @author Heiko Schmitt
 *
 */
public final class FastMath
{
	/**
	 * Faster rounding
	 * @param a
	 * @return
	 */
	public static int round(double a)
	{
		if (a < 0) 
			return (int)(a - 0.5d);
		else 
			return (int)(a + 0.5d);
	}
	
	/**
	 * Faster rounding
	 * @param a
	 * @return
	 */
	public static int round(float a)
	{
		if (a < 0) 
			return (int)(a - 0.5f);
		else 
			return (int)(a + 0.5f);
	}
	
	/**
	 * Faster rounding
	 * @param a
	 * @return
	 */
	public static long roundLong(double a)
	{
		if (a < 0) 
			return (long)(a - 0.5d);
		else 
			return (long)(a + 0.5d);
	}
	
	/**
	 * Simpler power, just for int exponent
	 * @param a base
	 * @param b exponent
	 * @return
	 */
	public static int pow(int a, int b)
	{
		if (b <= 0)
			return 1;
		
		b--;
		int result = a;
		for (int n = 0; n < b; n++)
			result = result * a;
		
		return result;
	}
	
	/**
	 * Simpler power, just for int exponent
	 * @param a base
	 * @param b exponent
	 * @return
	 */
	public static float pow(float a, int b)
	{
		if (b <= 0)
			return 1;
		
		b--;
		float result = a;
		for (int n = 0; n < b; n++)
			result = result * a;
		
		return result;
	}
}
