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
 * CompileSettingsDialog.java 
 * Created on 27.02.2009
 * by Heiko Schmitt
 */
package frontend.gui;

import java.awt.Toolkit;
import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import frontend.render.AppSettings;

/**
 * @author Heiko Schmitt
 *
 */
public class CompileSettingsDialog extends Dialog
{
	private Display display;
	private Shell shell;
	
	private SpringMapEditGUI smeGUI;
	private AppSettings as;
	
	/**
	 * @param parent
	 */
	public CompileSettingsDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		this.display = parent.getDisplay();
		this.shell = new Shell(parent);
		this.shell.setText("SM2 Compile Settings");
		
		this.as = this.smeGUI.as;
		
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
		shell.setLayout(new GridLayout(4, true));
		shell.setSize(320, 240);
		
		//Minimap
		Label l = new Label(shell, SWT.NONE);
		l.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 4, 1));
		l.setText("If Filename is NOT empty, the specified .dds will be used as the minimap.\n" +
		          "(Instead of auto-generating one)\n" + 
		          "NOTE:\n" +
		          "1024x1024 DXT1 compressed DDS File with mipmaps expected.");
		
		l = new Label(shell, SWT.NONE);
		l.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 1, 1));
		l.setText("Minimap Filename: ");
		
		final Text tMinimap = new Text(shell, SWT.BORDER);
		tMinimap.setSize(150, 0);
		tMinimap.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 3, 1));
		tMinimap.setText(as.minimapFilename);
		tMinimap.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				as.minimapFilename = ((Text)e.widget).getText();
			}
		});
		
		Label spacer = new Label(shell, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 2, 1));
		spacer.setVisible(false);
		
		Button b = new Button(shell, SWT.PUSH);
		b.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 1, 1));
		b.setText("Clear");
		b.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				as.minimapFilename = "";
				tMinimap.setText(as.minimapFilename);
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 1, 1));
		b.setText("Browse...");
		b.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.dds" });
				if (fd.open() != null)
				{
					as.minimapFilename = (new File(fd.getFilterPath(), fd.getFileNames()[0])).getAbsolutePath();
					tMinimap.setText(as.minimapFilename);
				}
			}
		});
		
		//Texturemap
		l = new Label(shell, SWT.NONE);
		l.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 4, 1));
		l.setText("If Filename is NOT empty, the specified .dds will be used as the texturemap.\n" +
				  "(Instead of using what you see inside the Renderwindow)\n" +
				  "NOTE:\n" +
				  "DXT1 compressed DDS file with mipmaps expected. Make sure the size matches the map!\n" +
				  "TextureWidth(or Height) = SpringUnits * 512");
		
		l = new Label(shell, SWT.NONE);
		l.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 1, 1));
		l.setText("Texturemap Filename: ");
		
		final Text tTexturemap = new Text(shell, SWT.BORDER);
		tTexturemap.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 3, 1));
		tTexturemap.setText(as.texturemapFilename);
		tTexturemap.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				as.texturemapFilename = ((Text)e.widget).getText();
			}
		});
		
		spacer = new Label(shell, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 2, 1));
		spacer.setVisible(false);
		
		b = new Button(shell, SWT.PUSH);
		b.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 1, 1));
		b.setText("Clear");
		b.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				as.texturemapFilename = "";
				tTexturemap.setText(as.texturemapFilename);
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 1, 1));
		b.setText("Browse...");
		b.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.dds" });
				if (fd.open() != null)
				{
					as.texturemapFilename = (new File(fd.getFilterPath(), fd.getFileNames()[0])).getAbsolutePath();
					tTexturemap.setText(as.texturemapFilename);
				}
			}
		});
		
		shell.pack();
		shell.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (shell.getSize().x / 2), (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (shell.getSize().y / 2));
	}
}
