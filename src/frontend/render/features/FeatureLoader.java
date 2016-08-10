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
 * FeatureLoader.java 
 * Created on 17.08.2008
 * by Heiko Schmitt
 */
package frontend.render.features;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.media.opengl.GL;

import backend.io.LERandomAccessFile;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.TextureIO;

import frontend.gui.DialogBox;
import frontend.render.TextureLoader;

/**
 * @author Heiko Schmitt
 *
 */
public class FeatureLoader
{
	/*
	 * Spring maptiles are 8 openGL-units large.
	 * Since out maptiles are 2 openGL units large, we need to divide the modelsizes by 4
	 */
	private static final float scaleFactor = 1 / 4f;
	
	private LERandomAccessFile inStream;
	
	//Data associated with current File
	private int rootPieceOffset;
	//private int collisionDataOffset;
	private int texture1Offset;
	private int texture2Offset;
	
	private String featureFileName;
	private String texture1Filename;
	private String texture2Filename;
	
	private Hashtable<String, Integer> textureCache;
	
	/**
	 * 
	 */
	public FeatureLoader()
	{
		textureCache = new Hashtable<String, Integer>();
	}
		
	public boolean loadFeature(GL gl, String objectPath, String texturePath, Feature feature)
	{
		boolean result = false;
		try
		{
			featureFileName = feature.featureFileName;
			feature.displayListID = gl.glGenLists(1);
			
			inStream = new LERandomAccessFile(new File(objectPath, featureFileName), "r");
			loadHeader();
			
			gl.glNewList(feature.displayListID, GL.GL_COMPILE);
			loadPiece(gl, rootPieceOffset, 0, 0, 0, feature);
			gl.glEndList();
			
			//Initialize TextureIDs
			feature.textureID = 0;
			feature.alphaTextureID = 0;
			
			//Load BaseTexture
			if (texture1Filename != null)
			{
				if (textureCache.contains(texture1Filename.toLowerCase()))
					feature.textureID = textureCache.get(texture1Filename.toLowerCase());
				else
					try
					{
						File texFile = new File(texturePath, texture1Filename);
						if (texFile.exists())
						{
							int texID = 0;
							if (texFile.getName().toLowerCase().endsWith(".png"))
								//TextureIO loads PNG with premultiplied alpha, so we must do it ourself
								texID = TextureLoader.getTextureFromToolkit(gl, texFile);
//							else if (texFile.getName().toLowerCase().endsWith(".tga"))
//								//TextureIO cannot handle compressed TGA, so we must do it ourself
//								texID = TextureLoader.getTextureFromToolkit(gl, texFile);
							else
								texID = TextureIO.newTexture(texFile, false).getTextureObject();
							feature.textureID = texID;
							textureCache.put(texture1Filename.toLowerCase(), texID);
						}
						else
							System.out.println("FeatureLoader.loadFeature: " + feature.stringID + "-> Could not find tex1 file: " + texture1Filename);
					}
					catch (Exception e)
					{
						//If image loading fails, just set empty texture
						System.err.println("Feature loading error: " + e.getMessage());
						feature.textureID = 0;
					}
			}
			//Load AlphaTexture
			if (texture2Filename != null)
			{
				if (textureCache.contains(texture2Filename.toLowerCase()))
					feature.alphaTextureID = textureCache.get(texture2Filename.toLowerCase());
				else
					try
					{
						File texFile = new File(texturePath, texture2Filename);
						if (texFile.exists())
						{
							int texID = 0;
							if (texFile.getName().toLowerCase().endsWith(".png"))
								//TextureIO loads PNG with premultiplied alpha, so we must do it ourself
								texID = TextureLoader.getTextureFromToolkit(gl, texFile);
//							else if (texFile.getName().toLowerCase().endsWith(".tga"))
//								//TextureIO cannot handle compressed TGA, so we must do it ourself
//								texID = TextureLoader.getTextureFromToolkit(gl, texFile);
							else
								texID = TextureIO.newTexture(texFile, false).getTextureObject();
							feature.alphaTextureID = texID;
							textureCache.put(texture2Filename.toLowerCase(), texID);
						}
						else
							System.out.println("FeatureLoader.loadFeature: " + feature.stringID + "-> Could not find tex2 file: " + texture2Filename);
					}
					catch (Exception e)
					{
						//If image loading fails, just set empty texture
						System.err.println("Feature loading error: " + e.getMessage());
						feature.alphaTextureID = 0;
					}
			}
			
			result = true;
		}
		catch (IOException e)
		{
			System.err.println("Feature loading error: " + e.getMessage());
		}
		finally
		{
			try
			{
				if (inStream != null)
					inStream.close();
			}
			catch (IOException e)
			{
				System.err.println("Feature loading error: " + e.getMessage());
			}
		}
		feature.isLoaded = result;
		return result;
	}
		
	
//	char magic[12];		//"Spring unit\0"
//	uint version;		//0 for this version
//	float radius;		//radius of collision sphere
//	float height;		//height of whole object
//	float midx;		//these give the offset from origin(which is supposed to lay in the ground plane) to the middle of the unit collision sphere
//	float midy;
//	float midz;
//	uint rootPiece;		//offset in file to root piece
//	uint collisionData;	//offset in file to collision data, must be 0 for now (no collision data)
//	uint texture1;		//offset in file to char* filename of first texture
//	uint texture2;		//offset in file to char* filename of second texture
	private void loadHeader() throws IOException
	{
		//magic
		if (!inStream.readString(11).equals("Spring unit"))
			throw new IOException(".S3O load: Did not find magic \"Spring unit\" marker");
		
		//Skip \0 at string end
		inStream.skip(1);
		
		//version
		int version = inStream.readInt(); 
		if (version != 0)
			throw new IOException("Can only load S3O Version 0, found: " + version);
		
		//radius
		inStream.skip(4);
		
		//height
		inStream.skip(4);
		
		//mid x y z 
		inStream.skip(4);
		inStream.skip(4);
		inStream.skip(4);
		
		//File offsets
		rootPieceOffset = inStream.readInt();
		/*collisionDataOffset = */inStream.readInt();
		texture1Offset = inStream.readInt();
		texture2Offset = inStream.readInt();
		
		//Texture filenames
		if (texture1Offset > 0)
		{
			inStream.seek(texture1Offset);
			texture1Filename = inStream.readString();
		}
		else
			texture1Filename = null;
		
		if (texture2Offset > 0)
		{
			inStream.seek(texture2Offset);
			texture2Filename = inStream.readString();
		}
		else
			texture2Filename = null;
	}
	
//	uint name;		//offset in file to char* name of this piece
//	uint numChilds;		//number of sub pieces this piece has
//	uint childs;		//file offset to table of dwords containing offsets to child pieces
//	uint numVertices;	//number of vertices in this piece
//	uint vertices;		//file offset to vertices in this piece
//	uint vertexType;	//0 for now
//	uint primitiveType;	//type of primitives for this piece, 0=triangles,1 triangle strips,2=quads
//	uint vertexTableSize;	//number of indexes in vertice table
//	uint vertexTable;	//file offset to vertice table, vertice table is made up of dwords indicating vertices for this piece, to indicate end of a triangle strip use 0xffffffff
//	uint collisionData;	//offset in file to collision data, must be 0 for now (no collision data)
//	float xoffset;		//offset from parent piece
//	float yoffset;
//	float zoffset;
	private void loadPiece(GL gl, int offset, float xOff, float yOff, float zOff, Feature feature) throws IOException
	{
		inStream.seek(offset);
		/*int nameOffset = */inStream.readInt();
		int numChilds = inStream.readInt();
		int childsOffset = inStream.readInt();
		int numVertices = inStream.readInt();
		int verticesOffset = inStream.readInt();
		/*int vertexType = */inStream.readInt();
		int primitiveType = inStream.readInt();
		int vertexTableSize = inStream.readInt();
		int vertexTableOffset = inStream.readInt();
		/*int collisionDataOffset = */inStream.readInt();
		float xOffset = inStream.readFloat() + xOff;
		float yOffset = inStream.readFloat() + yOff;
		float zOffset = inStream.readFloat() + zOff;
		
		//Load piece name
		//inStream.seek(nameOffset);
		//String name = inStream.readString();
		//System.out.println(featureFileName + ": " + name);
		
		//Load vertices
		S3OVertex[] vertices = loadVertices(numVertices, verticesOffset, xOffset, yOffset, zOffset, feature);
		
		//Build Mesh
		inStream.seek(vertexTableOffset);
		S3OVertex vertex;
		if (primitiveType == 0)
		{
			for (int i = 0; i < vertexTableSize; i += 3)
			{
				gl.glBegin(GL.GL_TRIANGLES);
					vertex = vertices[inStream.readInt()];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
					
					vertex = vertices[inStream.readInt()];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
					
					vertex = vertices[inStream.readInt()];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
				gl.glEnd();
			}
			feature.triangleCount += (vertexTableSize / 3);
		}
		else if (primitiveType == 1)
		{
			int vertexNumber;
			gl.glBegin(GL.GL_TRIANGLE_STRIP);
			for (int i = 0; i < vertexTableSize; i++)
			{
				vertexNumber = inStream.readInt();
				if (vertexNumber == 0xFFFFFFFF)
				{
					gl.glEnd();
					gl.glBegin(GL.GL_TRIANGLE_STRIP);
				}
				else
				{
					vertex = vertices[vertexNumber];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
				}
			}
			gl.glEnd();
			feature.triangleCount += (vertexTableSize - 2);
		}
		else //if (primitiveType == 2)
		{
			for (int i = 0; i < vertexTableSize; i += 4)
			{
				gl.glBegin(GL.GL_QUADS);
					vertex = vertices[inStream.readInt()];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
					
					vertex = vertices[inStream.readInt()];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
					
					vertex = vertices[inStream.readInt()];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
					
					vertex = vertices[inStream.readInt()];
					gl.glNormal3fv(vertex.normal.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE0, vertex.texCoord.vector, 0);
					gl.glMultiTexCoord2fv(GL.GL_TEXTURE1, vertex.texCoord.vector, 0);
					gl.glVertex3fv(vertex.vertex.vector, 0);
				gl.glEnd();
			}
			feature.triangleCount += (vertexTableSize / 2);
		}
		
		
		
		//Load child pieces
		int childOffset;
		for (int i = 0; i < numChilds; i++)
		{
			inStream.seek(childsOffset + (i * BufferUtil.SIZEOF_INT));
			childOffset = inStream.readInt();
			
			loadPiece(gl, childOffset, xOffset, yOffset, zOffset, feature);
		}
	}
	
//	float xpos;		//position of vertex relative piece origin
//	float ypos;
//	float zpos;
//	float xnormal;		//normal of vertex relative piece rotation
//	float ynormal;
//	float znormal;
//	float texu;		//texture offset for vertex
//	float texv;
	private S3OVertex[] loadVertices(int numVertices, int verticesOffset, float xOff, float yOff, float zOff, Feature feature) throws IOException
	{
		S3OVertex[] result = new S3OVertex[numVertices];
		
		inStream.seek(verticesOffset);
		
		S3OVertex newVertex;
		for (int i = 0; i < numVertices; i++)
		{
			newVertex = new S3OVertex();
			
			newVertex.vertex.vector[0] = (inStream.readFloat() + xOff) * scaleFactor;
			newVertex.vertex.vector[1] = (inStream.readFloat() + yOff) * scaleFactor;
			newVertex.vertex.vector[2] = (inStream.readFloat() + zOff) * scaleFactor;
			newVertex.normal.vector[0] = inStream.readFloat();
			newVertex.normal.vector[1] = inStream.readFloat();
			newVertex.normal.vector[2] = inStream.readFloat();
			newVertex.texCoord.vector[0] = inStream.readFloat();
			newVertex.texCoord.vector[1] = 1 - inStream.readFloat(); //Somehow texCoords are treated upside down
			
			//Retrieve highest vertex
			feature.maxHeight = Math.max(feature.maxHeight, newVertex.vertex.vector[1]);
			feature.maxWidth = Math.max(Math.max(feature.maxWidth, Math.abs(newVertex.vertex.vector[0])), Math.abs(newVertex.vertex.vector[2]));
			
			//Some normals do not seem to be normalized...fixing here
			newVertex.normal.normalize();
			
			result[i] = newVertex;
		}
		
		return result;
	}
}
