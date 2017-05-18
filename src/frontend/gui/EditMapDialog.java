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
 * NewMapDialog.java 
 * Created on 01.10.2008
 * by Heiko Schmitt
 */
package frontend.gui;

import java.awt.Toolkit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

import backend.SpringMapEdit;

/**
 * @author Heiko Schmitt
 *
 */
public class EditMapDialog extends Dialog
{
	private SpringMapEditGUI smeGUI;
	private Display display;
	private Shell shell;
	private boolean switchMapAxis = false;
	
	private SpringMapEdit sme;
	private int newMaxHeight;
	private float newWaterHeight;
	private int newWidth, newLength;
	/**
	 * @param parent
	 */
	public EditMapDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		this.display = parent.getDisplay();
		this.shell = new Shell(parent);
		this.shell.setText("Map Properties");
		this.sme = smeGUI.sme;
		
		newMaxHeight = sme.map.maxHeight;
		newWaterHeight = sme.map.waterHeight;
		newWidth = sme.width;
		newLength = sme.height;
		
		createDialogArea();
	}

	public void open()
	{
		shell.setVisible(true);
		
		//Message Loop
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}

	private void createDialogArea()
	{
		shell.setLayout(new GridLayout(2, true));
		int width = 320;
		int height = 240;
		shell.setSize(width, height);
		shell.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (width / 2), (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (height / 2));
		
		Label l = new Label(shell, SWT.HORIZONTAL);
		l.setText("Width: " + sme.width + "\t Length: " + sme.height);
		GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		//gd.widthHint = 100;
		l.setLayoutData(gd);

		//SPACER
		Button b;
		//b.setVisible(false);
		
		/*final Combo cw = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		cw.setItems(new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36", "38", "40", "42", "44", "46", "48", "50", "52", "54", "56", "58", "60", "62", "64" });
		cw.setVisibleItemCount(20);
		cw.setText(Integer.toString(sme.width));
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		cw.setLayoutData(gd);
		cw.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				newWidth = Integer.parseInt(((Combo)e.widget).getText());
				switchMapAxis = false;
			}
		});

		l = new Label(shell, SWT.HORIZONTAL);
		l.setText("Length:");
		gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		l.setLayoutData(gd);
		
		final Combo cl = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		cl.setItems(new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36", "38", "40", "42", "44", "46", "48", "50", "52", "54", "56", "58", "60", "62", "64" });
		cl.setVisibleItemCount(20);
		cl.setText(Integer.toString(sme.height));
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		cl.setLayoutData(gd);
		cl.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				newLength = Integer.parseInt(((Combo)e.widget).getText());
				switchMapAxis = false;
			}
		});*/
		
		final Label lHeight = new Label(shell, SWT.NONE);
		lHeight.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lHeight.setText("Set Height Level (in elmos): " + sme.map.maxHeight);
		lHeight.setToolTipText("Sets the height contrast level of the map.");
		
		final Slider sl = new Slider(shell, SWT.HORIZONTAL);
		
		final Label lWater = new Label(shell, SWT.NONE);
		lWater.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lWater.setText("Set Water Level (in elmos): " + (int)sme.map.waterHeight);
		lWater.setToolTipText("Sets the water level of the map.");
		
		final Slider wl = new Slider(shell, SWT.HORIZONTAL);
		sl.setMinimum(1);
		sl.setMaximum(5001);
		sl.setThumb(1);
		sl.setSelection(sme.map.maxHeight);
		sl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		sl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lHeight.setText("Set Height Level (in elmos): " + value);
				newMaxHeight = value;
				
				wl.setMinimum(-2);
				wl.setMaximum(sme.map.maxHeight + 1);
				wl.setThumb(1);
				if (sme.map.waterHeight >= sme.map.maxHeight) {
					wl.setSelection(sme.map.maxHeight - 1);
					lWater.setText("Set Water Level (in elmos): " + (sme.map.maxHeight*2 - 2));
				}
			}
		});
		
		wl.setMinimum(-1);
		wl.setMaximum(sme.map.maxHeight + 1);
		wl.setThumb(1);
		wl.setSelection((int)Math.max(sme.map.waterHeight*2, -1));
		wl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		wl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				newWaterHeight = ((Slider)e.widget).getSelection();
				if (newWaterHeight == 0){
					newWaterHeight = -1;
				}
				lWater.setText("Set Water Level (in elmos): " + newWaterHeight);
			}
		});
		
		/*l = new Label(shell, SWT.HORIZONTAL);
		l.setText("Switch x and y axis preserving all map properties:");
		gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		l.setLayoutData(gd);
		
		Button b = new Button(shell, SWT.PUSH);
		b.setText("Apply");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				switchMapAxis = true;
				//cw.select(sme.width / 2 - 1);
				//cl.select(sme.height / 2 - 1);
			}
		});*/
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Ok");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				/*Command cmd = new Command(new Object[] { sme.waterHeight, sme.maxHeight }) 
				{
					public void execute(Object[] data2)
					{*/
						if (true)
						{
							if (switchMapAxis)
								sme.map.switchMapAxis();
							else
								sme.map.resizeMap(newLength, newWidth);
							sme.width = sme.map.width;
							sme.height = sme.map.height;
							sme.diag = (float) Math.sqrt((newWidth * newWidth * 128 * 128) + (newLength * newLength * 128 * 128));
						}
						sme.map.waterHeight = newWaterHeight;
						sme.map.maxHeight = newMaxHeight;
				/*	}
				};
				smeGUI.messageQueue.offer(cmd);*/
				new ProcessingDialog(shell).close();
				shell.dispose();
				smeGUI.renderer.setSpringMapEdit(sme);
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Cancel");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		
		shell.pack();
	}
}
