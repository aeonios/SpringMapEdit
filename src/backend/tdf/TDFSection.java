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
 * TDFSection.java 
 * Created on 05.10.2008
 * by Heiko Schmitt
 */
package backend.tdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Heiko Schmitt
 *
 */
public class TDFSection
{
	private String name;
	private LinkedHashMap<String, TDFSection> sections;
	private LinkedHashMap<String, TDFKeyValue> data;
	
	public TDFSection(String name)
	{
		init();
		this.name = name;
	}
	
	public TDFSection(BufferedReader reader) throws IOException
	{
		init();
		parse(reader);
	}
	
	private void init()
	{
		sections = new LinkedHashMap<String, TDFSection>();
		data = new LinkedHashMap<String, TDFKeyValue>();
	}

	public String getName()
	{
		return name;
	}
	
	public void addSection(TDFSection newSection)
	{
		sections.put(newSection.getName(), newSection);
	}
	
	public void addKeyValue(TDFKeyValue newKeyValue)
	{
		data.put(newKeyValue.getKey(), newKeyValue);
	}
	
	public TDFSection getSection(String name)
	{
		return sections.get(name);
	}
	
	public TDFKeyValue getKeyValue(String key)
	{
		return data.get(key);
	}
	
	public Iterator<TDFSection> iteratorSection()
	{
		return sections.values().iterator();
	}
	
	public Iterator<TDFKeyValue> iteratorData()
	{
		return data.values().iterator();
	}
	
	private enum ParserState
	{
		undecided,
		readSectionName
	}
	
	private void parse(BufferedReader reader) throws IOException
	{
		ParserState state = ParserState.readSectionName;
		
		boolean notFinished = true;
		char[] c = new char[1];
		int check = reader.read(c, 0, 1);
		StringBuilder sb = new StringBuilder("");
		while ((check >= 0) && (notFinished))
		{
			switch (state)
			{
				case readSectionName:
				{
					if (c[0] != ']')
					{
						sb.append(c[0]);
					}
					else
					{
						name = sb.toString();
						sb.setLength(0);
						state = ParserState.undecided;
					}
					break;
				}
				case undecided:
				{
					switch (c[0])
					{
						case '[': addSection(new TDFSection(reader)); break;
						case '}': notFinished = false; break;
						case '{': break;
						default:
						{
							if (!Character.isWhitespace(c[0]))
							{
								addKeyValue(new TDFKeyValue(reader, c[0]));
							}
							break;
						}
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
		//Section start
		writer.write(prefix);
		writer.write("[");
		writer.write(name);
		writer.write("]\n");
		writer.write(prefix);
		writer.write("{\n");
		
		//Write KeyValues
		Iterator<TDFKeyValue> it = data.values().iterator();
		while (it.hasNext())
		{
			TDFKeyValue kv = it.next();
			kv.write(writer, prefix + "\t");
		}
		
		//Write SubSections
		Iterator<TDFSection> it2 = sections.values().iterator();
		while (it2.hasNext())
		{
			TDFSection section = it2.next();
			section.write(writer, prefix + "\t");
		}
		
		//Section end
		writer.write(prefix);
		writer.write("}\n");
	}
}
