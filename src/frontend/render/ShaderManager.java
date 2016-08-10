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
 * ShaderManager.java 
 * Created on 09.07.2008
 * by Heiko Schmitt
 */
package frontend.render;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;

import javax.media.opengl.GL;

/**
 * @author Heiko Schmitt
 *
 */
public class ShaderManager
{
	private GL gl;
	private LinkedHashMap<String, Integer> programTable;
	private LinkedHashMap<String, Integer> uniformLocationTable;
	private int currentProgramID;
	private String currentProgramName;
	private Charset charset = Charset.forName("ASCII");
	
	public ShaderManager(GL gl)
	{
		this.gl = gl;
		this.programTable = new LinkedHashMap<String, Integer>();
		this.uniformLocationTable = new LinkedHashMap<String, Integer>(); 
		this.currentProgramID = 0;
	}

	public void unbindShader()
	{
		currentProgramName = "";
		currentProgramID = 0;
		gl.glUseProgram(0);
	}
	
	private void loadShader(String shaderName)
	{
		String[] vertexShaderString = new String[1];
		String[] fragmentShaderString = new String[1];
		
		try
		{
			//Load vertexShader
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(new File("shaders/" + shaderName + ".vs")));
			String line = reader.readLine();
			while (line != null)
			{
				sb.append(line + "\n");
				line = reader.readLine();
			}
			vertexShaderString[0] = sb.toString();
			
			//Load fragmentShader
			sb = new StringBuilder();
			reader = new BufferedReader(new FileReader(new File("shaders/" + shaderName + ".fs")));
			line = reader.readLine();
			while (line != null)
			{
				sb.append(line + "\n");
				line = reader.readLine();
			}
			fragmentShaderString[0] = sb.toString();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		//Create VertexShader
		int vertexShaderID = gl.glCreateShader(GL.GL_VERTEX_SHADER);
	    gl.glShaderSource(vertexShaderID, 1, vertexShaderString, null);
	    gl.glCompileShader(vertexShaderID);
	    glPrintInfoLog("Vertex: ", vertexShaderID);

	    //Create FragmentShader
	    int fragmentShaderID = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
	    gl.glShaderSource(fragmentShaderID, 1, fragmentShaderString, null);
	    gl.glCompileShader(fragmentShaderID);
	    glPrintInfoLog("Fragment: ", fragmentShaderID);
	    
	    //Create ShaderProgram
	    int shaderProgramID = gl.glCreateProgram();
	    gl.glAttachShader(shaderProgramID, vertexShaderID);
	    gl.glAttachShader(shaderProgramID, fragmentShaderID);
	    gl.glLinkProgram(shaderProgramID);
	    glPrintInfoLog("Link: ", shaderProgramID);
	    gl.glValidateProgram(shaderProgramID);

	    //Insert ShaderProgram into table
	    String key = shaderName;
	    programTable.put(key, shaderProgramID);
	}
	
	private void glPrintInfoLog(String prefix, int objectID)
	{
		//TODO: fix infolog on old ATI cards. (message said success, but no water was drawn)
		int[] idBuf = new int[1];
		gl.glGetObjectParameterivARB(objectID, GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, idBuf, 0);
		if (idBuf[0] > 1)
		{
			ByteBuffer buf = ByteBuffer.allocate(idBuf[0]);
			IntBuffer lenBuf = IntBuffer.allocate(1);
			
			gl.glGetInfoLogARB(objectID, idBuf[0], lenBuf, buf);
			
			System.out.println(prefix); 
			System.out.println(new String(buf.array(), charset));
		}
	}
	
	public void bindShader(String shaderName)
	{
		currentProgramName = shaderName;
		if (!programTable.containsKey(currentProgramName))
			loadShader(currentProgramName);
		currentProgramID = programTable.get(currentProgramName); 
		gl.glUseProgram(currentProgramID);
	}
	
	public int getCurrentProgramID()
	{
		return currentProgramID;
	}
	
	/**
	 * Operates on currently bound shader
	 * @return
	 */
	public int getUniformLocation(String uniformName)
	{
		StringBuilder sb = new StringBuilder(currentProgramName.length() + 1 + uniformName.length());
		sb.append(currentProgramName);
		sb.append("_");
		sb.append(uniformName);
		String key = sb.toString();
		
		if (!uniformLocationTable.containsKey(key))
		{
			uniformLocationTable.put(key, gl.glGetUniformLocation(currentProgramID, uniformName));
		}
		
		return uniformLocationTable.get(key);
	}
}
