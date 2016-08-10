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
 * LogoDialog.java 
 * Created on 20.01.2009
 * by Heiko Schmitt
 */
package frontend.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Heiko Schmitt
 *
 */
public class LogoDialog
{
	private Shell parentShell;
	private Shell shell;
	private Point imageSize;

	/**
	 * Creates a new Logo dialog
	 */
	public LogoDialog(Shell parentShell, int openTime, File imageFile)
	{
		this.parentShell = parentShell;
		
		open(openTime, imageFile);
	}

	public void open(int openTime, File imageFile)
	{
		if (shell == null)
		{
			shell = new Shell(parentShell, SWT.NO_BACKGROUND);
			shell.setText("Spring Map Edit");
						
			shell.addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent e)
				{
					shell = null;
				}
			});
			
			createDialogArea(imageFile);
		}
		
		//Show it & center
		Dimension dim =  Toolkit.getDefaultToolkit().getScreenSize();
		shell.setLocation((dim.width / 2) - (imageSize.x / 2), (dim.height / 2) - (imageSize.y / 2));
		shell.pack();
		shell.setVisible(true);
		shell.forceActive();
		
		final int waitTillClose = openTime;
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try { Thread.sleep(waitTillClose); } catch (InterruptedException e) {}
				if (shell != null)
				{
					shell.getDisplay().asyncExec(new Runnable()
					{
						@Override
						public void run()
						{
							shell.dispose();
						}
					});
				}
			}
		});
		t.start();
	}
	
	private void createDialogArea(File imageFile)
	{		
		Canvas canvas = new Canvas(shell, SWT.NONE);
		File imgFile = imageFile;
		final Image img = new Image(shell.getDisplay(), imgFile.getAbsolutePath());
		imageSize = new Point(img.getBounds().width, img.getBounds().height);
		canvas.setSize(imageSize);
		canvas.addPaintListener(new PaintListener() 
		{
			@Override
			public void paintControl(PaintEvent e)
			{
				GC gc = e.gc;
				
				gc.drawImage(img, 0, 0);
			}
		});
	}
}
