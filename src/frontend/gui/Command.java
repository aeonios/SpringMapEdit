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
 * Command.java 
 * Created on 28.09.2008
 * by Heiko Schmitt
 */
package frontend.gui;

/**
 * @author Heiko Schmitt
 *
 */
public abstract class Command
{
	public Object[] data;
	
	public Command(Object[] data)
	{
		this.data = data;
	}

	public abstract void execute(Object[] data2);
}
