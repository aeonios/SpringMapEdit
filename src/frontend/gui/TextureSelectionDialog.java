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
 * TextureSelectionDialog.java 
 * Created on 06.10.2008
 * by Heiko Schmitt
 */
package frontend.gui;

import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import frontend.render.brushes.BrushTexture;
import frontend.render.brushes.TextureBrush;

/**
 * @author Heiko Schmitt
 *
 */
public class TextureSelectionDialog
{
	private SpringMapEditGUI smeGUI;
	private SpringMapEditDialog smed;
	public Shell shell;

	private byte[] animationBuffer;
	
	/**
	 * 
	 */
	public TextureSelectionDialog(SpringMapEditGUI smeGUI, SpringMapEditDialog smed)
	{
		this.smeGUI = smeGUI;
		this.smed = smed;
	}
	
	public void open()
	{
		if (shell == null)
		{
			shell = new Shell(smed.shell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.NO_BACKGROUND);
			shell.setText("Texture Selector");
			shell.setSize(370, 350);
			
			shell.addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent e)
				{
					shell = null;
				}
			});
			createDialogArea();
		}
		
		//Show it
		shell.setLocation(smed.shell.getLocation().x, smed.shell.getLocation().y + smed.shell.getSize().y);
		shell.layout();
		shell.setVisible(true);
		shell.forceActive();
	}
	
	private void createDialogArea()
	{
		shell.setLayout(new FillLayout());
		
		//Add scrollcomposite
		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
				
		//Add contentpane for scrollcomposite
		final Composite pane = new Composite(sc, SWT.NONE);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		pane.setLayout(rl);
		
		//Add Buttons
		Command selectCommand = new Command(null)
		{
			public void execute(Object[] data2)
			{
				smeGUI.sme.mes.getTextureBrush().setTexture((Integer) data2[0]);
			}
		};
		ImageSelectButton fsc;
		Command cmd;
		int count = smeGUI.sme.mes.brushTextureManager.getBrushDataCount();
		for (int i = 0; i < count; i++)
		{
			fsc = new ImageSelectButton(pane, i, smeGUI.sme.mes.brushTextureManager.getBrushDataName(i), smeGUI.sme.mes.defaultSize, smeGUI.sme.mes.defaultSize, (((TextureBrush)(smeGUI.sme.mes.activeBrush)).getTexture().textureID == i), selectCommand, null);
			fsc.forceFocus();
			
			//Fetch ImageData (execute loading in another thread. not really needed, but looks better ;) )
			cmd = new Command(new Object[] { fsc }) 
			{
				public void execute(Object[] data2)
				{
					//Set ImageData
					ImageSelectButton fsc = (ImageSelectButton) data[0];
					int width = fsc.getWidth();
					int height = fsc.getHeight();
					if (animationBuffer == null) animationBuffer = new byte[width * height * 3];
					
					//Copy pattern to image
					BrushTexture bp = smeGUI.sme.mes.brushTextureManager.getScaledBrushData(fsc.getObjectID(), width, height, false);
					byte[][] r = bp.getTextureR();
					byte[][] g = bp.getTextureG();
					byte[][] b = bp.getTextureB();
					int x, y;
					int scanlineWidth = width * 3;
					for (y = 0; y < height; y++)
					{
						for (x = 0; x < width; x++)
						{
							animationBuffer[(x*3) + 0 + (y * scanlineWidth)] = r[x][y];
							animationBuffer[(x*3) + 1 + (y * scanlineWidth)] = g[x][y];
							animationBuffer[(x*3) + 2 + (y * scanlineWidth)] = b[x][y];
						}
					}
					fsc.setImageData(animationBuffer);
				}
			};
			smeGUI.glMessageQueue.offer(cmd);
		}
				
		//Add pane to scrollcomposite
		sc.setContent(pane);
		
		//Set pane size on resize
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = sc.getClientArea();
				sc.setMinSize(pane.computeSize(r.width, SWT.DEFAULT));
				
				//Setup scroll amount
				ScrollBar sb = sc.getVerticalBar();
				if (sb != null) sb.setIncrement(smeGUI.sme.mes.defaultSize / 8);
			}
		});
	}
}
