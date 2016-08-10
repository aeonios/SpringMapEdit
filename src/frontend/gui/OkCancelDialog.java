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
 * OkCancelDialog.java 
 * Created on 27.02.2009
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Heiko Schmitt
 *
 */
public class OkCancelDialog extends Dialog
{
	private Shell shell;
	private String title;
	private String message;
	private int result;
	
	/**
	 * @param parent
	 */
	public OkCancelDialog(Shell parent, String title, String message)
	{
		super(parent);
		this.title = title;
		this.message = message;
	}

	public int open()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout(3, true));
		int width = 300;
		int height = 100;
		shell.setSize(width, height);
		shell.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (width / 2), (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (height / 2));
		shell.setText(title);
		
		Label label = new Label(shell, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 3, 1));
		label.setText(message);
		
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("OK");
		ok.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 1, 1));
		ok.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				try
				{
					result = SWT.OK;
					shell.dispose();
				}
				catch (NumberFormatException ex)
				{
				}
			}
		});
		
		Label spacer = new Label(shell, SWT.NONE);
		spacer.setVisible(false);
		
		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		cancel.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				result = SWT.CANCEL;
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
