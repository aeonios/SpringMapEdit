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
 * ErosionSettingsDialog.java 
 * Created on 27.02.2009
 * by Heiko Schmitt
 */
package frontend.gui;

import java.awt.Toolkit;
import java.util.EnumMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

/**
 * @author Heiko Schmitt
 *
 */
public class ErosionSettingsDialog extends Dialog
{
	private Display display;
	private Shell shell;
	
	private SpringMapEditGUI smeGUI;
	
	private EnumMap<Widgets, Control> widgetMap;
	
	private enum Widgets
	{
		L_WET_ITERATIONS, SL_WET_ITERATIONS,
		L_WET_DROPLET_HEIGHT, SL_WET_DROPLET_HEIGHT,
		L_WET_EVAPORATE_AMOUNT, SL_WET_EVAPORATE_AMOUNT,
		L_WET2_ITERATIONS, SL_WET2_ITERATIONS,
		L_WET2_BREAK_HEIGHT, SL_WET2_BREAK_HEIGHT,
		L_DRY_ITERATIONS, SL_DRY_ITERATIONS,
		L_DRY_BREAK_HEIGHT, SL_DRY_BREAK_HEIGHT
	}
	
	/**
	 * @param parent
	 */
	public ErosionSettingsDialog(Shell parent, SpringMapEditGUI smeGUI)
	{
		super(parent, SWT.DIALOG_TRIM);
		this.smeGUI = smeGUI;
		this.display = parent.getDisplay();
		this.shell = new Shell(parent);
		this.shell.setText("Erosion Settings");
		this.widgetMap = new EnumMap<Widgets, Control>(Widgets.class);
		
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
		b.setText("Use alternative WET erosion");
		b.setToolTipText("This uses a faster implementation, based on DRY erosion.");
		b.setSelection(smeGUI.sme.mes.getErosionSetup().useAlternativeWetMethod);
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((Button)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						smeGUI.sme.mes.getErosionSetup().useAlternativeWetMethod = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		//WET
		Label l = new Label(shell, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
		l.setText("--- WET Erosion ---");
		addSlider(Widgets.L_WET_ITERATIONS, Widgets.SL_WET_ITERATIONS, shell, smeGUI.sme.mes.getErosionSetup().wetIterations, 1, 1000, 
				new Command(null) { public void execute(Object[] data2)
				{
					smeGUI.sme.mes.getErosionSetup().wetIterations = (Integer) data2[0];
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Iterations: " + (Integer) data2[0];
				}});
		
		addSlider(Widgets.L_WET_DROPLET_HEIGHT, Widgets.SL_WET_DROPLET_HEIGHT, shell, (int)(smeGUI.sme.mes.getErosionSetup().wetDropletHeight * 1000), 1, 1000, 
				new Command(null) { public void execute(Object[] data2)
				{
					smeGUI.sme.mes.getErosionSetup().wetDropletHeight = ((Integer) data2[0]) / 1000f;
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Raindrop Size: " + (Integer) data2[0];
				}});
		
		addSlider(Widgets.L_WET_EVAPORATE_AMOUNT, Widgets.SL_WET_EVAPORATE_AMOUNT, shell, (int)(smeGUI.sme.mes.getErosionSetup().wetEvaporateAmount * 10000), 1, 10000, 
				new Command(null) { public void execute(Object[] data2)
				{
					smeGUI.sme.mes.getErosionSetup().wetEvaporateAmount = ((Integer) data2[0]) / 10000f;
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Evaporation Speed: " + (Integer) data2[0];
				}});
		
		//WET2
		l = new Label(shell, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
		l.setText("--- Alternative WET Erosion ---");
		addSlider(Widgets.L_WET2_ITERATIONS, Widgets.SL_WET2_ITERATIONS, shell, smeGUI.sme.mes.getErosionSetup().wet2Iterations, 1, 1000, 
				new Command(null) { public void execute(Object[] data2)
				{
					smeGUI.sme.mes.getErosionSetup().wet2Iterations = (Integer) data2[0];
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Iterations: " + (Integer) data2[0];
				}});
		
		addSlider(Widgets.L_WET2_BREAK_HEIGHT, Widgets.SL_WET2_BREAK_HEIGHT, shell, (int)(smeGUI.sme.mes.getErosionSetup().wet2BreakHeight * 1000), 1, 1000, 
				new Command(null) { public void execute(Object[] data2)
				{
					smeGUI.sme.mes.getErosionSetup().wet2BreakHeight = ((Integer) data2[0]) / 1000f;
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Break Height: " + (Integer) data2[0];
				}});
		
		//DRY
		l = new Label(shell, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
		l.setText("--- DRY Erosion ---");
		addSlider(Widgets.L_DRY_ITERATIONS, Widgets.SL_DRY_ITERATIONS, shell, smeGUI.sme.mes.getErosionSetup().dryIterations, 1, 1000, 
				new Command(null) { public void execute(Object[] data2)
				{
					smeGUI.sme.mes.getErosionSetup().dryIterations = (Integer) data2[0];
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Iterations: " + (Integer) data2[0];
				}});
		
		addSlider(Widgets.L_DRY_BREAK_HEIGHT, Widgets.SL_DRY_BREAK_HEIGHT, shell, (int)(smeGUI.sme.mes.getErosionSetup().dryBreakHeight * 1000), 1, 1000, 
				new Command(null) { public void execute(Object[] data2)
				{
					smeGUI.sme.mes.getErosionSetup().dryBreakHeight = ((Integer) data2[0]) / 1000f;
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Break Height: " + (Integer) data2[0];
				}});

		shell.pack();
		shell.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (shell.getSize().x / 2), (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (shell.getSize().y / 2));
	}
	
	private Slider addSlider(Widgets wLabel, Widgets wSlider, Composite parent, int initialValue, int min, int max, final Command cmd, final Getter getText)
	{
		final Label label = new Label(parent, SWT.HORIZONTAL);
		GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		label.setLayoutData(gd);
		label.setText((String)getText.getValue(new Object[] { initialValue }));
		
		Slider sl = new Slider(parent, SWT.HORIZONTAL);
		sl.setMinimum(min);
		sl.setMaximum(max + 1);
		sl.setThumb(1);
		sl.setSelection(initialValue);
		gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1);
		sl.setLayoutData(gd);
		sl.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				int value = ((Slider)e.widget).getSelection();
				label.setText((String)getText.getValue(new Object[] { value }));
				
				Command newCmd = new Command(new Object[] { value })
				{
					public void execute(Object[] data2)
					{
						cmd.execute(new Object[] { data[0] });
					}
				};
				smeGUI.messageQueue.offer(newCmd);
			}
		});
		
		widgetMap.put(wLabel, label);
		widgetMap.put(wSlider, sl);
		
		return sl;
	}
}
