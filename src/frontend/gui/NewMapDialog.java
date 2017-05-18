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
public class NewMapDialog extends Dialog
{
	private SpringMapEditGUI smeGUI;
	private Display display;
	private Shell shell;
	
	private SpringMapEdit sme;
	
	private int newWidth;
	private int newHeight;
	private int maxHeight;
	private float startHeight;
	private int waterHeigth = -1;
	private boolean random = false;
	/**
	 * @param parent
	 */
	public NewMapDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		this.display = parent.getDisplay();
		this.shell = new Shell(parent);
		this.shell.setText("New Map");
		
		this.sme = smeGUI.sme;
		
		newWidth = smeGUI.sme.width;
		newHeight = smeGUI.sme.height;
		
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
		
		maxHeight = sme.map.maxHeight;
		
		Label l = new Label(shell, SWT.HORIZONTAL);
		l.setText("Width:");
		GridData gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		l.setLayoutData(gd);
		
		Combo c = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		c.setItems(new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30"});
		c.setVisibleItemCount(15);
		c.setText(Integer.toString(newWidth));
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		c.setLayoutData(gd);
		c.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				newWidth = Integer.parseInt(((Combo)e.widget).getText());
			}
		});
		
		l = new Label(shell, SWT.HORIZONTAL);
		l.setText("Length:");
		gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		l.setLayoutData(gd);
		
		c = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		c.setItems(new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30"});
		c.setVisibleItemCount(15);
		c.setText(Integer.toString(newHeight));
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		c.setLayoutData(gd);
		c.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				newHeight = Integer.parseInt(((Combo)e.widget).getText());
			}
		});
		
		final Label lHeight = new Label(shell, SWT.NONE);
		lHeight.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lHeight.setText("Set Height Contrast Level: " + sme.map.maxHeight);
		lHeight.setToolTipText("Sets the height contrast level of the map.");
		
		final Slider sl = new Slider(shell, SWT.HORIZONTAL);
		final Label lWater = new Label(shell, SWT.NONE);
		final Slider wl = new Slider(shell, SWT.HORIZONTAL);
		final Label lStartHeight = new Label(shell, SWT.NONE);
		final Slider hl = new Slider(shell, SWT.HORIZONTAL);
		sl.setMinimum(10);
		sl.setMaximum(501);
		sl.setThumb(1);
		sl.setSelection(maxHeight);
		sl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		sl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lHeight.setText("Set Height Contrast Level: " + value);
				maxHeight = value;

				hl.setMaximum(maxHeight + 1);
				if (hl.getSelection() > maxHeight)
				{
					startHeight = 1.0f;
					hl.setSelection(maxHeight + 1);
					lStartHeight.setText("Set Starting Height Level: " + maxHeight + 1);
				}
				wl.setMaximum(maxHeight + 2);
				if (wl.getSelection() > maxHeight)
				{
					waterHeigth = maxHeight - 1;
					wl.setSelection(maxHeight + 2);
					lWater.setText("Set Water Level: " + (maxHeight - 1));
				}
			}
		});
		lStartHeight.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lStartHeight.setText("Set Starting Height Level: " + 0);
		lStartHeight.setToolTipText("Sets the initial height level of the map.");
		
		hl.setMinimum(0);
		hl.setMaximum(maxHeight);
		hl.setThumb(1);
		hl.setSelection(0);
		hl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		hl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lStartHeight.setText("Set Starting Height Level: " + value);
				startHeight = value / maxHeight;
			}
		});
		
		lWater.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lWater.setText("Set Water Level: -1");
		lWater.setToolTipText("Sets the initial water level of the map.");
		
		wl.setMinimum(0);
		wl.setMaximum(maxHeight + 1);
		wl.setThumb(1);
		wl.setSelection(0);
		wl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		wl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lWater.setText("Set Water Level: " + (value - 1));
				waterHeigth = value - 1;
			}
		});
		
		new Label(shell, SWT.NONE); // Filler
		Button b = new Button(shell, SWT.CHECK);
		
		b.setText("Random");
		b.setToolTipText("Generate initial random map.");
		b.setSelection(false);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				random = ((Button)e.widget).getSelection();
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Generate Map");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new ProcessingDialog(shell);
				sme.newMap(newWidth, newHeight, startHeight, random);
				sme.map.waterHeight = waterHeigth;
				sme.map.maxHeight = maxHeight;
				smeGUI.renderer.setSpringMapEdit(sme);
				shell.dispose();
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Cancel");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				shell.dispose();
			}
		});
		
		shell.pack();
	}
}
