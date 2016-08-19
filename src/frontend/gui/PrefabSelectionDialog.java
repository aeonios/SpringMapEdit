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
 * PrefabSelectionDialog.java 
 * Created on 29.09.2009
 * by Heiko Schmitt
 */
package frontend.gui;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import frontend.render.brushes.BrushPattern;
import frontend.render.brushes.BrushTexture;

/**
 * @author Heiko Schmitt
 *
 */
public class PrefabSelectionDialog implements Observer
{
	private SpringMapEditGUI smeGUI;
	private SpringMapEditDialog smed;
	private ImageSelectButton selectedButton;
	private int currentPrefabID;
	private String currentCategoryName;
	
	private long lastAnimationFrame;
	private byte[] animationBuffer;
	
	private BrushPattern heightmap;
	private BrushTexture texturemap;
	
	private Composite buttonPane;
	private Group widget;
	
	public PrefabSelectionDialog(SpringMapEditGUI smeGUI, SpringMapEditDialog smed, Composite parent)
	{
		this.smeGUI = smeGUI;
		this.smed = smed;
		currentPrefabID = -1;
		currentCategoryName = "";
		createDialogArea(parent);
	}
	
	private void setHeightTexturemap(int prefabID)
	{
		if (currentPrefabID == prefabID) return;
		currentPrefabID = prefabID;
		
		int dataWidth = 1;
		int dataHeight = 1;
		int texDetailFactor = 2; //SpringMapEdit.heightmapSizeTextureFactor
		heightmap = smeGUI.sme.mes.prefabManager.getScaledPrefabHeightmap(prefabID, smeGUI.as.prefabSize + 1, -1, true);
		if (heightmap != null)
		{
			dataWidth = heightmap.width;
			dataHeight = heightmap.height;
			texturemap = smeGUI.sme.mes.prefabManager.getScaledPrefabTexturemap(prefabID, (dataWidth - 1) * texDetailFactor, (dataHeight - 1) * texDetailFactor, false);
		}
		else
		{
			texturemap = smeGUI.sme.mes.prefabManager.getScaledPrefabTexturemap(prefabID, smeGUI.as.prefabSize * texDetailFactor, -1, true);
			if (texturemap != null)
			{
				dataWidth = (texturemap.width / 8) + 1;
				dataHeight = (texturemap.height / 8) + 1;
			}
		}
	}
	
	private void createDialogArea(Composite parent)
	{
		final Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setLayout(new GridLayout(1, false));
		group.setText("Select Prefab:");
		
		//Add Category SelectBox
		final Combo categoryCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		categoryCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1, 1));
		String[] categoryNames = smeGUI.sme.mes.prefabManager.getCategoryNameSet().toArray(new String[0]);
		categoryCombo.setItems(categoryNames);
		currentCategoryName = categoryNames.length > 0 ? categoryNames[0] : "";
		categoryCombo.setText(currentCategoryName);
		
		categoryCombo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				currentCategoryName = categoryCombo.getText();
				categoryChanged();
			}
		});
		
		//Add scrollcomposite
		final ScrolledComposite sc = new ScrolledComposite(group, SWT.V_SCROLL | SWT.BORDER);
		sc.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
				
		//Add contentpane for scrollcomposite
		buttonPane = new Composite(sc, SWT.NONE);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		buttonPane.setLayout(rl);
		
		//Add pane to scrollcomposite
		sc.setContent(buttonPane);
		
		//Set pane size on resize
		sc.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = sc.getClientArea();
				sc.setMinSize(buttonPane.computeSize(r.width, SWT.DEFAULT));
				
				//Setup scroll amount
				ScrollBar sb = sc.getVerticalBar();
				if (sb != null) sb.setIncrement(smeGUI.as.featureTexSize / 8);
			}
		});
		widget = group;
		
		categoryChanged();
	}
	
	private void categoryChanged()
	{
		if (currentCategoryName == "") return;
		selectedButton = null;
		
		//Unload old Buttons
		Control[] buttons = buttonPane.getChildren();
		int buttonCount = buttons.length;
		for (int i = 0; i < buttonCount; i++)
		{
			buttons[i].dispose();
		}
		
		//Add Buttons
		Command selectCommand = new Command(null)
		{
			public void execute(Object[] data2)
			{
				//smeGUI.sme.mes.getPrefabBrush().width = -1; //Keep original size
				smeGUI.sme.mes.getPrefabBrush().setPrefab((Integer) data2[0]);
				smed.updateWidgets();
			}
		};
		ImageSelectButton fsc;
		Command cmd;
		List<Integer> prefabIDList = smeGUI.sme.mes.prefabManager.getCategoryIDList(currentCategoryName);
		Iterator<Integer> it = prefabIDList.iterator();
		int prefabID;
		while (it.hasNext())
		{
			prefabID = it.next();
			boolean selected = (smeGUI.sme.mes.getPrefabBrush().getPrefabID() == prefabID);
			fsc = new ImageSelectButton(buttonPane, prefabID, smeGUI.sme.mes.prefabManager.getPrefabName(prefabID), smeGUI.as.featureTexSize, smeGUI.as.featureTexSize, selected, selectCommand, this);
			if (selected)
			{
				selectedButton = fsc;
			}
			
			//Fetch ImageData
			cmd = new Command(new Object[] { fsc }) 
			{
				public void execute(Object[] data2)
				{
					ImageSelectButton fsc = (ImageSelectButton) data[0];
					int width = fsc.getWidth();
					int height = fsc.getHeight();
					if (animationBuffer == null) animationBuffer = new byte[width * height * 3];
					
					setHeightTexturemap(fsc.getObjectID());
					
					if (animationBuffer == null)
						animationBuffer = new byte[selectedButton.getWidth() * selectedButton.getHeight() * 3];
					
					smeGUI.renderer.getPrefabImageData(animationBuffer, heightmap, texturemap, 210, smeGUI.sme.mes.prefabManager.getPrefabData(fsc.getObjectID()).heightZ * smeGUI.sme.mes.prefabManager.getScaleFactorHeightmap(fsc.getObjectID(), (heightmap != null) ? heightmap.width : 1));
					fsc.setImageData(animationBuffer);
				}
			};
			smeGUI.glMessageQueue.offer(cmd);
		}
		//Reset current height/texturemap
		cmd = new Command(new Object[] { null }) 
		{
			public void execute(Object[] data2)
			{
				if (selectedButton != null)
					setHeightTexturemap(selectedButton.getObjectID());
			}
		};
		smeGUI.glMessageQueue.offer(cmd);
		
		buttonPane.layout();
	}
	
	/**
	 * NOTE: Only call within active openGL context!!!
	 */
	public void animate()
	{
		if (selectedButton != null) {
			if (animationBuffer == null)
				animationBuffer = new byte[selectedButton.getWidth() * selectedButton.getHeight() * 3];
			ImageSelectButton selectedButton = this.selectedButton;
				
			//Update heightmap/texturemap
			setHeightTexturemap(selectedButton.getObjectID());
				
			selectedButton.currentAnimationFrame = selectedButton.currentAnimationFrame + (smeGUI.as.gameFrame - lastAnimationFrame);
			smeGUI.renderer.getPrefabImageData(animationBuffer, heightmap, texturemap, selectedButton.currentAnimationFrame, smeGUI.sme.mes.prefabManager.getPrefabData(selectedButton.getObjectID()).heightZ * smeGUI.sme.mes.prefabManager.getScaleFactorHeightmap(selectedButton.getObjectID(), (heightmap != null) ? heightmap.width : 1));
			selectedButton.setImageData(animationBuffer);
				
			lastAnimationFrame = smeGUI.as.gameFrame;
		}
	}

	public Group getWidget(){
		return widget;
	}
	
	@Override
	public void update(Observable o, Object arg)
	{
		selectedButton = (ImageSelectButton) arg;
	}

}
