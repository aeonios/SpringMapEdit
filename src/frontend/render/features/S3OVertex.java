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
 * S3OVertex.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package frontend.render.features;

import backend.math.Vector2;
import backend.math.Vector3;

/**
 * @author Heiko Schmitt
 *
 */
public class S3OVertex
{
//	struct S3OVertex{
//	float xpos;		//position of vertex relative piece origin
//	float ypos;
//	float zpos;
//	float xnormal;		//normal of vertex relative piece rotation
//	float ynormal;
//	float znormal;
//	float texu;		//texture offset for vertex
//	float texv;
//};
	
	public Vector3 vertex;
	public Vector3 normal;
	public Vector2 texCoord;
	
	public S3OVertex()
	{
		vertex = new Vector3();
		normal = new Vector3();
		texCoord = new Vector2();
	}
	
	
}
