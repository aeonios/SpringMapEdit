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

import backend.SpringMapEdit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Label;

import java.awt.*;

/**
 * @author Heiko Schmitt
 *
 */
public class ErodeMapDialog extends Dialog
{
	private SpringMapEditGUI smeGUI;
	private Display display;
	private Shell shell;
	private boolean wetErode = true;

	private SpringMapEdit sme;
	private int erosionReps = 1;
	/**
	 * @param parent
	 */
	public ErodeMapDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		this.display = parent.getDisplay();
		this.shell = new Shell(parent);
		this.shell.setText("Erode Map");
		this.sme = smeGUI.sme;
		
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
		
		Button b = new Button(shell, SWT.RADIO);
		b.setText("Wet Erode");
		b.setSelection(true);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				wetErode = true;
			}
		});

		b = new Button(shell, SWT.RADIO);
		b.setText("Dry Erode");
		b.setSelection(false);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				wetErode = false;
			}
		});
		
		final Label lReps = new Label(shell, SWT.NONE);
		lReps.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lReps.setText("Number of Repetitions: " + 1);
		
		final Slider wl = new Slider(shell, SWT.HORIZONTAL);
		wl.setMinimum(1);
		wl.setMaximum(100);
		wl.setThumb(1);
		wl.setSelection(1);
		wl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		wl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				erosionReps = ((Slider)e.widget).getSelection();
				lReps.setText("Number of Repetitions: " + erosionReps);
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Ok");
		GridData gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				Command cmd = new Command(null)
				{
					public void execute(Object[] data2)
					{
						if (wetErode){
							for (int i = 0; i < erosionReps; i++){
								smeGUI.sme.map.heightmap.erodeMapWet(0, 0, smeGUI.sme.map.heightmap.getHeightmapWidth(), smeGUI.sme.map.heightmap.getHeightmapLength(), smeGUI.sme.mes.getErosionSetup());
							}
							smeGUI.renderer.invalidateAllBlocks(true, false, false);
						}else{
							for (int i = 0; i < erosionReps; i++){
								smeGUI.sme.erodeMapDryWet(0, 0, smeGUI.sme.map.heightmap.getHeightmapWidth(), smeGUI.sme.map.heightmap.getHeightmapLength(), false);
							}
							smeGUI.renderer.invalidateAllBlocks(true, false, false);
						}
					}
				};
				smeGUI.messageQueue.offer(cmd);
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
