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
 * FPSMeter.java 
 * Created on 01.10.2007
 * by Heiko Schmitt
 */
package frontend.gui;

/**
 * @author Heiko Schmitt
 *
 */
public class FPSMeter
{	
	private long lastSecond;
	private int fps;
	private int fpsLast;
	
	/**
	 * Hide...
	 */
	public FPSMeter() 
	{
		lastSecond = System.nanoTime();
		fps = 0;
		fpsLast = 0;
	}
	
	public void tick()
	{
		long now = System.nanoTime();
		if (now >= (lastSecond + 1000000000))
	    {
			lastSecond = lastSecond + 1000000000;
	        fpsLast = fps;
	        fps = 1;
	    } 
		else
	    {
	    	fps++;
	    }
	}
	
	/**
	 * @return the fps
	 */
	public int getFps()
	{
		return fpsLast;
	}
}
