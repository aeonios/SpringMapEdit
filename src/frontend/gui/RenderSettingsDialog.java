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
 * RenderSettingsDialog.java 
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

import backend.FastMath;

import frontend.render.MapRenderer;
import frontend.render.AppSettings;

/**
 * @author Heiko Schmitt
 *
 */
public class RenderSettingsDialog extends Dialog
{
	private Display display;
	private Shell shell;
	
	private SpringMapEditGUI smeGUI;
	private AppSettings rs;
	private MapRenderer renderer;
		
	/**
	 * @param parent
	 */
	public RenderSettingsDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		this.display = parent.getDisplay();
		this.shell = new Shell(parent);
		this.shell.setText("Render Settings");
		
		this.rs = smeGUI.as;
		this.renderer = smeGUI.renderer;
		
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
		shell.setSize(320, 240);
		
		Button b = new Button(shell, SWT.CHECK);
		b.setText("VSync");
		b.setToolTipText("Limits FPS to monitor refresh rate, saving gpu power and reducing tearing artifacts.");
		b.setSelection(rs.vsync);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.vsync = (Boolean) data[0];
						renderer.setVsync(rs.vsync);
					}
				};
				smeGUI.glMessageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("smooth Normal calulation");
		b.setSelection(rs.smoothNormals);
		b.setToolTipText("CPU: slower heightmap editing, but gives nice shading");
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.smoothNormals = (Boolean) data[0];
						renderer.invalidateAllBlocks(true, false, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("faster smooth Normal calculation");
		b.setToolTipText("CPU: faster heightmap editing, but slightly uglier lighting");
		b.setSelection(rs.fastNormals);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.fastNormals = (Boolean) data[0];
						renderer.invalidateAllBlocks(true, false, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("use TextureCompression");
		b.setToolTipText("slower texture editing, but uses less VRAM");
		b.setSelection(rs.compressTextures);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.compressTextures = (Boolean) data[0];
						renderer.invalidateAllBlocks(false, true, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("nice Shader Water");
		b.setToolTipText("CPU+GPU: much slower (requires extra renderpass)");
		b.setSelection(rs.fancyWater);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.fancyWater = (Boolean) data[0];
						renderer.camViewChangedNotify();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("Feature lighting");
		b.setToolTipText("Will be removed when feature-shader is written. (cannot use self-illumination map in FFP)");
		b.setSelection(rs.featureLighting);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.featureLighting = (Boolean) data[0];
						renderer.camViewChangedNotify();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("draw Sun");
		b.setSelection(rs.drawSun);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.drawSun = (Boolean) data[0];
						renderer.camViewChangedNotify();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("move Sun");
		b.setSelection(rs.moveSun);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.moveSun = (Boolean) data[0];
						renderer.camViewChangedNotify();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("show Performance debug info");
		b.setSelection(rs.outputPerfDebug);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.outputPerfDebug = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.CHECK);
		b.setText("animate GUI");
		b.setSelection(rs.animateGUI);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.animateGUI = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});

		b = new Button(shell, SWT.CHECK);
		b.setText("use LOD");
		b.setToolTipText("Trades quality for performance.");
		b.setSelection(rs.useLOD);
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() })
				{
					public void execute(Object[] data2)
					{
						rs.useLOD = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});

		final Label lLOD = new Label(shell, SWT.NONE);
		lLOD.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END, GridData.VERTICAL_ALIGN_END, false, false, 1, 1));
		lLOD.setText("LOD: " + FastMath.round(rs.lodDist / 10));

		//SPACER
		b = new Button(shell, SWT.NONE);
		b.setVisible(false);
		
		Slider sl = new Slider(shell, SWT.HORIZONTAL);
		sl.setMinimum(1);
		sl.setMaximum(101);
		sl.setThumb(1);
		sl.setSelection(FastMath.round(rs.lodDist / 10));
		sl.setLayoutData(new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 1, 1));
		sl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lLOD.setText("LOD: " + value);
				
				//calculate lod dists
				rs.lodDist = value * 10;
			}
		});
		
		shell.pack();
		shell.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (shell.getSize().x / 2), (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (shell.getSize().y / 2));
	}
}
