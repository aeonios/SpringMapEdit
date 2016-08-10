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
public class ShiftMapDialog extends Dialog
{
	private SpringMapEditGUI smeGUI;
	private Display display;
	private Shell shell;
	
	private SpringMapEdit sme;
	
	private boolean vertically = false;
	private int start = 0;
	private int length;
	private int max;
	private int amount;
	/**
	 * @param parent
	 */
	public ShiftMapDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		display = parent.getDisplay();
		shell = new Shell(parent);
		shell.setText("Shift Map");
		
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
		length = max;
		amount = max / 2;
		
		GridData gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		
		Button b = new Button(shell, SWT.CHECK);
		
		new Label(shell, SWT.NONE); // Padding
		final Label lStart = new Label(shell, SWT.NONE);
		final Slider startSlider = new Slider(shell, SWT.HORIZONTAL);
		final Label lLength = new Label(shell, SWT.NONE);
		final Slider lengthSlider = new Slider(shell, SWT.HORIZONTAL);
		final Label lAmount = new Label(shell, SWT.NONE);
		final Slider amountSlider = new Slider(shell, SWT.HORIZONTAL);
		b.setText("Vertically: ");
		b.setToolTipText("Mirror vertically");
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
				startSlider.setMaximum(max - 1);
				if (start > max)
				{
					start = max - 1;
					lStart.setText("Set Start: " + (start + 1));
				}
				startSlider.setSelection(start);
				lengthSlider.setMaximum(max - start + 1);
				if (length > max - start)
				{
					length = max - start;
					lLength.setText("Set Length: " + length);
				}
				lengthSlider.setSelection(length);
				amountSlider.setMaximum(length);
				if (amount >= length)
				{
					amount = length - 1;
					lAmount.setText("Set Amount: " + amount);
				}
				amountSlider.setSelection(amount);
			}
		});
		
		lStart.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lStart.setText("Set Start: " + (start + 1));
		lStart.setToolTipText("Start of mirroring.");
		
		startSlider.setMinimum(0);
		startSlider.setMaximum(max - 1);
		startSlider.setThumb(1);
		startSlider.setSelection(start);
		startSlider.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		startSlider.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				start = ((Slider)e.widget).getSelection();
				lStart.setText("Set Start: " + (start + 1)); // Add 1 for normal human counting
				lengthSlider.setMaximum(max - start + 1);
				if (length > max - start)
				{
					length = max - start;
					lLength.setText("Set Length: " + length);
					lengthSlider.setSelection(length);
					amountSlider.setMaximum(length);
				}
				if (amount >= length)
				{
					amount = length - 1;
					lAmount.setText("Set Amount: " + amount);
					amountSlider.setSelection(amount);
				}
			}
		});

		lLength.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lLength.setText("Set Length: " + length);
		lLength.setToolTipText("Sets the length of mirroring.");
		
		lengthSlider.setMinimum(2);
		lengthSlider.setMaximum(max + 1);
		lengthSlider.setThumb(1);
		lengthSlider.setSelection(length);
		lengthSlider.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lengthSlider.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				length = ((Slider)e.widget).getSelection();
				lLength.setText("Set Length: " + length);
				amountSlider.setMaximum(length);
				if (amount >= length)
				{
					amount = length - 1;
					lAmount.setText("Set Amount: " + amount);
					amountSlider.setSelection(amount);
				}
			}
		});
		
		lAmount.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lAmount.setText("Set Amount: " + amount);
		lAmount.setToolTipText("Sets the amount of mirroring.");
		
		amountSlider.setMinimum(1);
		amountSlider.setMaximum(max);
		amountSlider.setThumb(1);
		amountSlider.setSelection(amount);
		amountSlider.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		amountSlider.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				amount = ((Slider)e.widget).getSelection();
				lAmount.setText("Set Amount: " + amount);
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Shift Map");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { start, length }) 
				{
					public void execute(Object[] data2)
					{
						smeGUI.sme.map.moveMap(start, length, amount, vertically);
					}
				};
				smeGUI.messageQueue.offer(cmd);
				smeGUI.renderer.setSpringMapEdit(smeGUI.sme);
				new ProcessingDialog(shell).close();
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
