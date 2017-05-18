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
 * SpringFeatureSelectionDialog.java 
 * Created on 06.10.2008
 * by Heiko Schmitt
 */
package frontend.gui;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Heiko Schmitt
 *
 */
public class FeatureSelectionDialog implements Observer
{
	private SpringMapEditGUI smeGUI;
	private SpringMapEditDialog smed;
	private ImageSelectButton selectedButton;
	public Shell shell;
	
	private long lastAnimationFrame;
	private byte[] animationBuffer;
	
	/**
	 * 
	 */
	public FeatureSelectionDialog(SpringMapEditGUI smeGUI, SpringMapEditDialog smed)
	{
		this.smeGUI = smeGUI;
		this.smed = smed;
	}
	
	public void open()
	{
		if (shell == null)
		{
			//shell = new Shell(smed.shell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.NO_BACKGROUND);
			shell.setText("Feature Selector");
			
			shell.setSize(370, 350);
			
			shell.addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent e)
				{
					shell = null;
				}
			});
			
			createDialogArea();
		}
		
		//Show it
		shell.setLocation(smed.shell.getLocation().x, smed.shell.getLocation().y + smed.shell.getSize().y);
		shell.layout();
		shell.setVisible(true);
		shell.forceActive();
	}
	
	private void createDialogArea()
	{
		shell.setLayout(new FillLayout());
		
		//Add scrollcomposite
		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
				
		//Add contentpane for scrollcomposite
		final Composite pane = new Composite(sc, SWT.NONE);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		pane.setLayout(rl);
		
		//Add Buttons
		Command selectCommand = new Command(null)
		{
			public void execute(Object[] data2)
			{
				smeGUI.sme.mes.getFeatureBrush().featureID = (Integer) data2[0];
				//smed.updateWidgets();
			}
		};
		ImageSelectButton fsc;
		Command cmd;
		int featureCount = smeGUI.renderer.getFeatureManager().getFeatureCount();
		for (int i = 0; i < featureCount; i++)
		{
			boolean selected = (smeGUI.sme.mes.getFeatureBrush().featureID == i);
			fsc = new ImageSelectButton(pane, i, smeGUI.renderer.getFeatureManager().getFeatureName(i), smeGUI.as.featureTexSize, smeGUI.as.featureTexSize, selected, selectCommand, this);
			if (selected)
				selectedButton = fsc;
			fsc.forceFocus();
			
			//Fetch ImageData
			cmd = new Command(new Object[] { fsc }) 
			{
				public void execute(Object[] data2)
				{
					ImageSelectButton fsc = (ImageSelectButton) data[0];
					if (animationBuffer == null)
						animationBuffer = new byte[selectedButton.getWidth() * selectedButton.getHeight() * 3];
					smeGUI.renderer.getFeatureImageData(animationBuffer, fsc.getObjectID(), 210);
					fsc.setImageData(animationBuffer);
				}
			};
			smeGUI.glMessageQueue.offer(cmd);
		}
				
		//Add pane to scrollcomposite
		sc.setContent(pane);
		
		//Set pane size on resize
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = sc.getClientArea();
				sc.setMinSize(pane.computeSize(r.width, SWT.DEFAULT));
				
				//Setup scroll amount
				ScrollBar sb = sc.getVerticalBar();
				if (sb != null)
					sb.setIncrement(smeGUI.as.featureTexSize / 8);
			}
		});
	}
	
	/**
	 * NOTE: Only call within active openGL context!!!
	 */
	public void animate()
	{
		if (shell != null)
		{
			if (selectedButton != null)
			{
				if (animationBuffer == null)
					animationBuffer = new byte[selectedButton.getWidth() * selectedButton.getHeight() * 3];
				ImageSelectButton selectedButton = this.selectedButton;
				
				selectedButton.currentAnimationFrame = selectedButton.currentAnimationFrame + (smeGUI.as.gameFrame - lastAnimationFrame);
				smeGUI.renderer.getFeatureImageData(animationBuffer, selectedButton.getObjectID(), selectedButton.currentAnimationFrame);
				selectedButton.setImageData(animationBuffer);
				
				lastAnimationFrame = smeGUI.as.gameFrame;
			}
		}
	}

	@Override
	public void update(Observable o, Object arg)
	{
		selectedButton = (ImageSelectButton) arg;
	}
}
