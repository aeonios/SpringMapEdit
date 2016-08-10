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
public class FlipMapDialog extends Dialog
{
	private SpringMapEditGUI smeGUI;
	private Display display;
	private Shell shell;
	
	private SpringMapEdit sme;
	
	private int start = 0;
	private int length;
	private int offset;
	private int max;
	private boolean vertically = false;
	/**
	 * @param parent
	 */
	public FlipMapDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		display = parent.getDisplay();
		shell = new Shell(parent);
		shell.setText("Flip Map");
		
		sme = smeGUI.sme;
		
		createDialogArea();
	}

	public void open()
	{
		shell.setVisible(true);
		
		//Message Loop
		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	private void createDialogArea()
	{
		shell.setLayout(new GridLayout(2, true));
		int width = 320;
		int height = 240;
		shell.setSize(width, height);
		shell.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (width / 2), (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (height / 2));
		max = sme.height;
		length = max / 2;
		offset = max / 2;
		
		Label l = new Label(shell, SWT.HORIZONTAL);
		l.setText("");
		l.setToolTipText("");
		GridData gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		l.setLayoutData(gd);
		
		Button b = new Button(shell, SWT.CHECK);

		final Label lStart = new Label(shell, SWT.NONE);
		final Slider startSlider = new Slider(shell, SWT.HORIZONTAL);
		final Label lLength = new Label(shell, SWT.NONE);
		final Slider lengthSlider = new Slider(shell, SWT.HORIZONTAL);
		final Label lOffset = new Label(shell, SWT.NONE);
		final Slider offsetSlider = new Slider(shell, SWT.HORIZONTAL);
		
		b.setText("Vertically: ");
		b.setToolTipText("Flip vertically");
		b.setSelection(vertically);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				vertically = ((Button)e.widget).getSelection();
				if (vertically)
					max = sme.width;
				else
					max = sme.height;
				startSlider.setMaximum(max);
				if (start > max)
				{
					start = max - 1;
					lStart.setText("Set Start: " + start);
				}
				startSlider.setSelection(start);
				lengthSlider.setMaximum(max - start);
				if (length >= max - start) {
					length = max - start - 1;
					lLength.setText("Set Length: " + length);
				}
				lengthSlider.setSelection(length);
				offsetSlider.setMaximum(max);
				if (offset >= max - start)
				{
					offset = max - start - 1;
					lOffset.setText("Set Offset: " + offset);
				}
				else if (offset < start - max)
				{
					offset = max - start - 1;
					lOffset.setText("Set Offset: " + offset);
				}
				offsetSlider.setSelection(offset + start);
			}
		});
		
		lStart.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lStart.setText("Set Start: " + (start + 1));
		lStart.setToolTipText("Start of flipping.");
		
		startSlider.setMinimum(0);
		startSlider.setMaximum(max);
		startSlider.setThumb(1);
		startSlider.setSelection(start);
		startSlider.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		startSlider.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lStart.setText("Set Start: " + (start + 1)); // Add 1 for normal human counting
				start = value;
				lengthSlider.setMaximum(max - start);
				if (length > max - start)
				{
					length = max - start;
					lengthSlider.setSelection(length);
					lLength.setText("Set Length: " + length);
				}
				if (offset >= max - start)
				{
					offset = max - start - 1;
					lOffset.setText("Set Offset: " + offset);
				}
				else if (offset < -start)
				{
					offset = -start;
					lOffset.setText("Set Offset: " + offset);
				}
				offsetSlider.setSelection(offset + start);
			}
		});

		lLength.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lLength.setText("Set Length: " + length);
		lLength.setToolTipText("Sets the length of flipping.");
		
		lengthSlider.setMinimum(1);
		lengthSlider.setMaximum(max);
		lengthSlider.setThumb(1);
		lengthSlider.setSelection(length);
		lengthSlider.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lengthSlider.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				length = value;
				lLength.setText("Set Length: " + length);
			}
		});
		
		lOffset.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lOffset.setText("Set Offset: " + offset);
		lOffset.setToolTipText("Sets the offset of flipping.");
		
		offsetSlider.setMinimum(0);
		offsetSlider.setMaximum(max);
		offsetSlider.setThumb(1);
		offsetSlider.setSelection(offset);
		offsetSlider.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		offsetSlider.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				offset = value - start;
				lOffset.setText("Set Offset: " + offset);
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Flip Map");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] {}) 
				{
					public void execute(Object[] data2)
					{
						smeGUI.sme.map.flipMap(start, length, offset, vertically);
					}
				};
				smeGUI.messageQueue.offer(cmd);
				new ProcessingDialog(shell).close();
				smeGUI.renderer.setSpringMapEdit(smeGUI.sme);
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
