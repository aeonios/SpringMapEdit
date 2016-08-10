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
 * Vector3Math.java 
 * Created on 05.07.2008
 * by Heiko Schmitt
 */
package backend.math;

/**
 * @author Heiko Schmitt
 *
 */
public class Vector3Math
{
	/**
	 * adds vectors
	 * @param v
	 * @param x
	 */
	public static Vector3 addVectors(Vector3 v, Vector3 x)
	{
		return new Vector3(v.vector[0] + x.vector[0], v.vector[1] + x.vector[1], v.vector[2] + x.vector[2]);
	}
	
	/**
	 * subtracts vectors
	 * @param v
	 * @param x
	 */
	public static Vector3 subVectors(Vector3 v, Vector3 x)
	{
		return new Vector3(v.vector[0] - x.vector[0], v.vector[1] - x.vector[1], v.vector[2] - x.vector[2]);
	}
	
	/**
	 * invert vector
	 * @param v
	 */
	public static Vector3 inverseVector(Vector3 v)
	{
		return new Vector3(-v.vector[0], -v.vector[1], -v.vector[2]);
	}
	
	/**
	 * scales vector
	 * @param v
	 * @param x
	 */
	public static Vector3 scaleVector(Vector3 v, float factor)
	{
		return new Vector3(v.vector[0] * factor, v.vector[1] * factor, v.vector[2] * factor);
	}
	
	/**
	 * Returns CrossProduct of given (normalized) Vectors
	 * @param v
	 * @param x
	 */
	public static Vector3 crossProduct(Vector3 v, Vector3 x)
	{
		return new Vector3
		( 
				(v.vector[1] * x.vector[2]) - (v.vector[2] * x.vector[1]),
				(v.vector[2] * x.vector[0]) - (v.vector[0] * x.vector[2]),
				(v.vector[0] * x.vector[1]) - (v.vector[1] * x.vector[0])
		);
	}
	
	/**
	 * Returns DotProduct of given (normalized) Vectors
	 * @param v
	 * @param x
	 */
	public static float dotProduct(Vector3 v, Vector3 x)
	{
		return (v.vector[0] * x.vector[0]) + (v.vector[1] * x.vector[1]) + (v.vector[2] * x.vector[2]);
	}
	
	/**
	 * Calculates intersectionpoint of given plane and ray
	 * @param planeOrigin plane Origin (normalize!)
	 * @param planeNormal plane Normal (normalize!)
	 * @param rayOrigin ray Origin (normalize!)
	 * @param rayDirection
	 * @return CollisionPoint or null, if no intersection happens
	 */
	public static Vector3 planeIntersectPoint(Vector3 planeOrigin, Vector3 planeNormal, Vector3 rayOrigin, Vector3 rayDirection)
	{
		Vector3 normalizedPlaneNormal = planeNormal.getCopy().normalize();
		float d = -dotProduct(normalizedPlaneNormal, planeOrigin);
		float num = dotProduct(normalizedPlaneNormal, rayOrigin) + d;
		float denom = dotProduct(normalizedPlaneNormal, rayDirection.getCopy().normalize());
		float dist = -(num / denom);
		//Positive means: wrong direction
		if (dist > 0) 
			return null;
		else
			return addVectors(rayOrigin, scaleVector(rayDirection.getCopy().normalize(), dist));
	}
	
	/**
	 * Rotates given vector by given angle (in radians)
	 * @param v
	 * @param angle in radians
	 * @return
	 */
	public static Vector3 rotateZ(Vector3 v, double angle)
	{
		return new Vector3(
			(float)((v.x() * Math.cos(angle)) - (v.y() * Math.sin(angle))),
			(float)((v.x() * Math.sin(angle)) + (v.y() * Math.cos(angle))),
			v.z()
		);
	}
	
	/**
	 * Rotates given vector by given angle (in radians)
	 * @param v
	 * @param angle in radians
	 * @return
	 */
	public static Vector3 rotateY(Vector3 v, double angle)
	{
		return new Vector3(
			(float)((v.z() * Math.sin(angle)) + (v.x() * Math.cos(angle))),
			v.y(),
			(float)((v.z() * Math.cos(angle)) - (v.x() * Math.sin(angle)))
		);
	}
	
	/**
	 * Rotates given vector by given angle (in radians)
	 * @param v
	 * @param angle in radians
	 * @return
	 */
	public static Vector3 rotateX(Vector3 v, double angle)
	{
		return new Vector3(
			v.x(),
			(float)((v.y() * Math.cos(angle)) - (v.z() * Math.sin(angle))),
			(float)((v.y() * Math.sin(angle)) + (v.z() * Math.cos(angle)))
		);
	}

}
