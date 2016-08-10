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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;

import backend.FastMath;

/**
 * @author Heiko Schmitt
 *
 */
public class RandomizeTerrainDialog extends Dialog
{
	private SpringMapEditGUI smeGUI;
	private Display display;
	private Shell shell;
	
	private int newWidth;
	private int newHeight;
	
	/**
	 * @param parent
	 */
	public RandomizeTerrainDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		this.display = parent.getDisplay();
		this.shell = new Shell(parent);
		this.shell.setText("Randomize Terrain");
		
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
		
		Label l = new Label(shell, SWT.HORIZONTAL);
		l.setText("Random Seed:");
		GridData gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		l.setLayoutData(gd);
		
		Text t = new Text(shell, SWT.BORDER);
		t.setText(Integer.toString(smeGUI.sme.mes.getTerraGenSetup().randomSeed));
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1);
		gd.widthHint = 100;
		t.setLayoutData(gd);
		t.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				try
				{
					smeGUI.sme.mes.getTerraGenSetup().randomSeed = Integer.parseInt(((Text)e.widget).getText());
				}
				catch (NumberFormatException nfe) { /* Ignore Silently */ }
			}
		});
		
		final Label lSkip = new Label(shell, SWT.NONE);
		lSkip.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lSkip.setText("Skip Steps: " + smeGUI.sme.mes.getTerraGenSetup().skipSteps);
		lSkip.setToolTipText("Skips given amount of randomization Steps. Higher values -> Keep more of original terrain.");
		
		Slider sl = new Slider(shell, SWT.HORIZONTAL);
		sl.setMinimum(0);
		sl.setMaximum(13);
		sl.setThumb(1);
		sl.setSelection(smeGUI.sme.mes.getTerraGenSetup().skipSteps);
		sl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		sl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lSkip.setText("Skip Steps: " + value);
				smeGUI.sme.mes.getTerraGenSetup().skipSteps = value;
			}
		});
		
		final Label lmaxDisplacement = new Label(shell, SWT.NONE);
		lmaxDisplacement.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		lmaxDisplacement.setText("Hilliness: " + FastMath.round((smeGUI.sme.mes.getTerraGenSetup().maxDisplacement * 100)));
		lmaxDisplacement.setToolTipText("Sets the maximum difference between heights.");
		
		sl = new Slider(shell, SWT.HORIZONTAL);
		sl.setMinimum(1);
		sl.setMaximum(5000);
		sl.setThumb(1);
		sl.setSelection(FastMath.round(smeGUI.sme.mes.getTerraGenSetup().maxDisplacement * 100));
		sl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		sl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				lmaxDisplacement.setText("Hilliness: " + value);
				smeGUI.sme.mes.getTerraGenSetup().maxDisplacement = value / 100f;
			}
		});
		
		final Label ldisplacementRegression = new Label(shell, SWT.NONE);
		ldisplacementRegression.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		ldisplacementRegression.setText("Smoothness: " + FastMath.round((smeGUI.sme.mes.getTerraGenSetup().displacementRegression * 1000)));
		
		sl = new Slider(shell, SWT.HORIZONTAL);
		sl.setMinimum(1);
		sl.setMaximum(1000);
		sl.setThumb(1);
		sl.setSelection(FastMath.round(smeGUI.sme.mes.getTerraGenSetup().displacementRegression * 1000));
		sl.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		sl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				ldisplacementRegression.setText("Smoothness: " + value);
				smeGUI.sme.mes.getTerraGenSetup().displacementRegression = value / 1000f;
			}
		});
		
		Button b = new Button(shell, SWT.PUSH);
		b.setText("Generate Map");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { newWidth, newHeight }) 
				{
					public void execute(Object[] data2)
					{
						smeGUI.sme.map.heightmap.genStartupHeightmap(smeGUI.sme.mes.getTerraGenSetup());
						smeGUI.renderer.invalidateAllBlocks(true, false, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Ok");
		gd = new GridData(GridData.CENTER, GridData.CENTER, false, false, 1, 1);
		gd.widthHint = 100;
		b.setLayoutData(gd);
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new ProcessingDialog(shell);
				shell.dispose();
			}
		});
		
		shell.pack();
	}
}
