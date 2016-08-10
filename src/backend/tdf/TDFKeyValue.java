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
 * TDFKeyValue.java 
 * Created on 05.10.2008
 * by Heiko Schmitt
 */
package backend.tdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author Heiko Schmitt
 *
 */
public class TDFKeyValue
{
	private String key;
	private String value;
	
	public TDFKeyValue(String key, String value)
	{
		this.key = key;
		this.value = value;
	}
	
	public TDFKeyValue(BufferedReader reader, char firstChar) throws IOException
	{
		parse(reader, firstChar);
	}

	public String getKey()
	{
		return key;
	}
	
	public String getValue()
	{
		return value;
	}
	
	private enum ParserState
	{
		readKey,
		readValue
	}
	
	private void parse(BufferedReader reader, char firstChar) throws IOException
	{
		ParserState state = ParserState.readKey;
		
		boolean notFinished = true;
		char[] c = new char[1];
		int check = reader.read(c, 0, 1);
		StringBuilder sb = new StringBuilder();
		sb.append(firstChar);
		while ((check >= 0) && (notFinished))
		{
			switch (state)
			{
				case readKey:
				{
					if (c[0] != '=')
					{
						sb.append(c[0]);
					}
					else
					{
						key = sb.toString();
						sb.setLength(0);
						state = ParserState.readValue;
					}
					break;
				}
				case readValue:
				{
					if (c[0] != ';')
					{
						sb.append(c[0]);
					}
					else
					{
						value = sb.toString();
						notFinished = false;
					}
					break;
				}
				default: break;
			}
			if (notFinished) check = reader.read(c, 0, 1);
		}
	}
	
	public void write(BufferedWriter writer, String prefix) throws IOException
	{
		writer.write(prefix);
		writer.write(key);
		writer.write("=");
		writer.write(value);
		writer.write(";\n");
	}
}
