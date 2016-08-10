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
 * TDFDocument.java 
 * Created on 05.10.2008
 * by Heiko Schmitt
 */
package backend.tdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Heiko Schmitt
 *
 */
public class TDFDocument
{
	private LinkedHashMap<String, TDFSection> sections; 
	
	public TDFDocument()
	{
		init();
	}
	
	public TDFDocument(File tdfFile)
	{
		init();
		
		try
		{
			parse(new BufferedReader(new InputStreamReader(new FileInputStream(tdfFile))));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void init()
	{
		sections = new LinkedHashMap<String, TDFSection>();
	}

	public void addSection(TDFSection parent, TDFSection newSection)
	{
		if (parent == null)
			sections.put(newSection.getName(), newSection);
		else
			parent.addSection(newSection);
	}
	
	public TDFSection getSection(String name)
	{
		return sections.get(name);
	}
	
	public Iterator<TDFSection> iteratorSection()
	{
		return sections.values().iterator();
	}
	
	private void parse(BufferedReader reader) throws IOException
	{
		char[] c = new char[1];
		int check = reader.read(c, 0, 1);
		while (check >= 0)
		{
			if (c[0] == '[')
			{
				addSection(null, new TDFSection(reader)); 
			}
			check = reader.read(c, 0, 1);
		}
	}
	
	private void write(BufferedWriter writer) throws IOException
	{
		Iterator<TDFSection> it = sections.values().iterator();
		while (it.hasNext())
		{
			it.next().write(writer, "");
		}
	}
	
	public void save(File tdfFile)
	{
		try
		{
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tdfFile)));
			write(bw);
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
