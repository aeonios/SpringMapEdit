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
 * InputBox.java 
 * Created on 07.10.2008
 * by Heiko Schmitt
 */
package frontend.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Heiko Schmitt
 *
 */
public class InputBox extends Dialog
{
	private Shell shell;
	
	private String title;
	private String initialValue;
	private int result;
	
	/**
	 * @param parent
	 */
	public InputBox(Shell parent, String title, String initialValue)
	{
		super(parent);
		
		this.title = title;
		this.initialValue = initialValue;
	}

	public int open()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout(2, true));
		shell.setSize(300, 100);
		shell.setText(title);
		
		final Text input = new Text(shell, SWT.BORDER | SWT.SINGLE);
		input.setText(initialValue);
		input.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 2, 1));
		
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("OK");
		ok.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 1, 1));
		ok.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					result = Integer.parseInt(input.getText());
					shell.dispose();
				}
				catch (NumberFormatException ex)
				{
				}
			}
		});
		
		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		cancel.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				result = -1;
				shell.dispose();
			}
		});
		
		shell.layout();
		shell.setVisible(true);
		
		while (!shell.isDisposed())
		{
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		
		return result;
	}
}
