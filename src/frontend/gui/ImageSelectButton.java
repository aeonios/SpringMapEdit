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
 * ImageSelectButton.java 
 * Created on 06.10.2008
 * by Heiko Schmitt
 */
package frontend.gui;

import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Heiko Schmitt
 *
 */
public class ImageSelectButton extends Canvas
{
	private Observer obs;
	private boolean isSelected;
	private boolean imageDataReady;
	private ImageData imageData;
	private Image image;
	
	private int objectID;
	private String objectName;
	
	private int w;
	private int h;
	
	private Font font;
	
	private Command selectCommand;
	
	public long currentAnimationFrame = 210;
	
	public ImageSelectButton(Composite parent, int featureID, String featureName, int width, int height, boolean selected, Command selectionCommand, Observer observer)
	{
		super(parent, SWT.NO_BACKGROUND);
		
		if (font == null) font = new Font(this.getDisplay(), "arial", 10, SWT.BOLD);
		
		this.w = width;
		this.h = height;
		
		this.imageData = new ImageData(width, height, 24, new PaletteData(0xFF , 0xFF00 , 0xFF0000));
		this.imageDataReady = false;
		this.image = null;
		this.objectID = featureID;
		this.objectName = featureName;
		this.setSize(w, h);
		this.isSelected = selected;
		this.selectCommand = selectionCommand;
		this.obs = observer;
		
		RowData rd = new RowData(w, h);
		this.setLayoutData(rd);
		
		//add repaint listener
		this.addPaintListener(new PaintListener() 
		{
			public void paintControl(PaintEvent e)
			{
				((ImageSelectButton)(e.widget)).draw(e.gc);
			}
		});
		
		//add click listener
		final ImageSelectButton thisPointer = this;
		this.addMouseListener(new MouseAdapter() 
		{
			public void mouseDown(MouseEvent e)
			{
				deselectAllInGroup();
				
				setSelected(true);
				
				selectCommand.execute(new Object[] { objectID });
				
				if (obs != null) obs.update(null, thisPointer);
			}
		});
	}
	
	public void setSelected(boolean selected)
	{
		isSelected = selected;
		redraw();
	}
	
	private void deselectAllInGroup()
	{
		Composite c = this.getParent();
		Control[] children = c.getChildren();
		int childrenCount = children.length;
		for (int i = 0; i < childrenCount; i++)
		{
			((ImageSelectButton)children[i]).setSelected(false);
		}
	}
	
	/**
	 * Set Image data, which will be displayed.<BR>
	 * NOTE: Image data is expected in RGB Format,<BR>
	 * one Byte for each color channel.<BR>
	 * NOTE: This may be called from another Thread.
	 * @param dataArray
	 */
	public void setImageData(byte[] dataArray)
	{
		//Copy dataArray into imageData
		int byteScanlineWidth = w * 3;
		int[] scanline = new int[w];
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				scanline[x] = (dataArray[(x * 3) + 0 + (y * byteScanlineWidth)] & 0xFF) + ((dataArray[(x * 3) + 1 + (y * byteScanlineWidth)] & 0xFF) << 8) + ((dataArray[(x * 3) + 2 + (y * byteScanlineWidth)] & 0xFF) << 16);
			}
			imageData.setPixels(0, y, w, scanline, 0);
		}
		
		//Indicate data is ready for painting
		imageDataReady = true;
		
		//Issue Redraw in our thread
		if (!isDisposed())
		{
			this.getDisplay().asyncExec(new Runnable() 
			{
				public void run()
				{
					if (!isDisposed())
						redraw();
				}
			});
		}
	}

	private void draw(GC gc)
	{
		if (imageDataReady)
		{
			//Create image if it does not exist
			if (image != null) 
				image.dispose();
			image = new Image(this.getDisplay(), imageData);
			
			//Draw image
			gc.drawImage(image, 0, 0);
			
			//Draw Selection
			if (isSelected)
			{
				gc.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_RED));
				gc.drawRectangle(0, 0, w-1, h-1);
				gc.drawRectangle(1, 1, w-3, h-3);
				gc.drawRectangle(2, 2, w-5, h-5);
			}
			
			//Draw Description
			gc.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			gc.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.setFont(font);
			Point p = gc.stringExtent(objectName);
			gc.drawString(objectName, (w / 2) - (p.x / 2), h - p.y, false);
		}
	}
	
	public void dispose()
	{
		if (image != null) image.dispose();
		if (font != null) font.dispose();
		super.dispose();
	}
	
	public int getObjectID()
	{
		return objectID;
	}
	
	public int getWidth()
	{
		return w;
	}
	
	public int getHeight()
	{
		return h;
	}
}
