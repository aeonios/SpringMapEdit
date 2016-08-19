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
 * SpringMapEditDialog.java 
 * Created on 07.07.2008
 * by Heiko Schmitt
 */
package frontend.gui;

import java.awt.Toolkit;
import java.io.File;
import java.util.EnumMap;

import frontend.render.brushes.BrushPattern;
import frontend.render.brushes.BrushTexture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import backend.SpringMapEdit;
import backend.MapEditSettings.BrushMode;
import frontend.render.MapRenderer;
import frontend.render.AppSettings;
import frontend.render.MapRenderer.MapMode;
import frontend.render.brushes.Brush;
import frontend.render.brushes.TextureBrush;
import frontend.render.brushes.FeatureBrush.FeatureMode;
import frontend.render.brushes.HeightBrush.HeightMode;
import frontend.render.brushes.PrefabBrush.PrefabMode;
import frontend.render.brushes.TextureBrush.TextureMode;

/**
 * @author Heiko Schmitt
 *
 */
public class SpringMapEditDialog 
{	
	public Display display;
	public Group shell;
	private boolean isUsingOwnDisplay;
	
	private SpringMapEditGUI smeGUI;
	private SpringMapEdit sme;
	private AppSettings rs;
	private MapRenderer renderer;
	private byte[] psBuffer;
	private byte[] tsBuffer;
	private byte[] pfbBuffer;

	BrushSelectionDialog brushDialog;
	TextureSelectionDialog texDialog;
	private PrefabSelectionDialog prefabDialog;

	private EnumMap<Widgets, Control> widgetMap;
	
	private enum Widgets
	{
		L_BRUSHSTRENGTH, SL_BRUSHSTRENGTH,
		L_BRUSHSIZE, SL_BRUSHSIZE,
	}
	
	public SpringMapEditDialog(SpringMapEditGUI mainGUI, Group parent)
	{
		this.smeGUI = mainGUI;
		this.sme = smeGUI.sme;
		this.rs = smeGUI.as;
		this.renderer = smeGUI.renderer;
		this.shell = parent;
		//this.featureDialog = new FeatureSelectionDialog(smeGUI, this);
		this.widgetMap = new EnumMap<Widgets, Control>(Widgets.class);
	}
	
	private boolean isAdditionalDisplaySupported()
	{
		final boolean[] result = new boolean[1];
		result[0] = true;
		
		//Needs to be in another thread, as apparently one thread with multiple displays does not work too...
		Thread t = new Thread(new Runnable() 
		{
			@Override
			public void run()
			{
				try
				{
					Display test = new Display();
					test.dispose();
				}
				catch (SWTError e)
				{
					result[0] = false;
					System.out.println("ERROR: " + e.getMessage());
					System.out.println("Trying single threaded(single display) approach...");
				}
			}
		});
		t.start();
		
		//Wait for thread to finish...
		boolean notJoined = true;
		while (notJoined)
		{
			try
			{
				t.join();
				notJoined = false;
			}
			catch (InterruptedException e)
			{
				//ignore...
			}
		}
		return result[0];
	}
	
	public void open()
	{
		createDialogArea();
	}
	
	private void createDialogArea()
	{
		final Group brushmode, controls, texSelection, ptsel, patternsel, texsel, prefabsel, brushSettings, heightGroup, tg, fg, pfg, cpg;
		final StackLayout brushLayout, texLayout;

		brushmode = addGroup(shell, 3);
		brushmode.setText("Brush Mode");

		// Controls
		controls = addGroup(shell, 3);
		controls.setText("Brush Controls");
		
		addSlider(Widgets.L_BRUSHSTRENGTH, Widgets.SL_BRUSHSTRENGTH, controls, "Brush Strength: " + sme.mes.activeBrush.getStrengthInt(), sme.mes.activeBrush.getStrengthInt(), 1, 1000,
				new Command(null) {
					public void execute(Object[] data2) {
						sme.mes.activeBrush.setStrengthInt((Integer) data2[0]);
					}
				},
				new Getter(null) {
					public Object getValue(Object[] data2) {
						return "Brush Strength: " + (Integer) data2[0];
					}
				});
		
		addSlider(Widgets.L_BRUSHSIZE, Widgets.SL_BRUSHSIZE, controls, "Brush Size: " + sme.mes.activeBrush.width, sme.mes.activeBrush.getWidth(), 1, 4000,
				new Command(null) { public void execute(Object[] data2)
				{
					sme.mes.activeBrush.setSize((Integer) data2[0], (Integer) data2[0]);
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Brush Size: " + (Integer) data2[0];
				}});
		
		Button b = new Button(controls, SWT.PUSH);
		b.setText("Rotate CCW");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.rotate(true);
				updateWidgets();
			}
		});

		b = new Button(controls, SWT.PUSH);
		b.setText("Mirror horiz");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.mirror(true);
				updateWidgets();
			}
		});

		// spacer
		b = new Button(controls, SWT.NONE);
		b.setVisible(false);
		
		b = new Button(controls, SWT.PUSH);
		b.setText("Rotate CW");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.rotate(false);
				updateWidgets();
			}
		});
		
		b = new Button(controls, SWT.PUSH);
		b.setText("Mirror vert");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.mirror(false);
				updateWidgets();
			}
		});

		brushSettings = new Group(shell, SWT.SHADOW_NONE);
		brushLayout = new StackLayout();
		brushSettings.setLayout(brushLayout);
		brushSettings.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		brushSettings.setText("Heightmode");

		heightGroup = addGroup(brushSettings, 4);
		tg = addGroup(brushSettings, 4);
		fg = addGroup(brushSettings, 4);
		pfg = addGroup(brushSettings, 2);

		texSelection = new Group(shell, SWT.SHADOW_NONE);
		texLayout = new StackLayout();
		texSelection.setLayout(texLayout);
		texSelection.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

		ptsel = new Group(texSelection, SWT.SHADOW_NONE);
		ptsel.setLayout(new FillLayout(SWT.VERTICAL));
		ptsel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

		brushDialog = new BrushSelectionDialog(smeGUI, this, ptsel);
		patternsel = brushDialog.getWidget();

		texDialog = new TextureSelectionDialog(smeGUI, this, ptsel);
		texsel = texDialog.getWidget();

		prefabDialog = new PrefabSelectionDialog(smeGUI, this, texSelection);
		prefabsel = prefabDialog.getWidget();

		texLayout.topControl = ptsel;
		texSelection.layout();
		texsel.setVisible(false);
		
		addRadioGroup(heightGroup, sme.mes.getHeightBrush().brushMode,
				new String[]{"Raise/Lower", "Stamp", "Set", "Smooth"}, new SelectionAdapter[]{
						new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
								Command newCmd = new Command(new Object[]{((Button) e.widget).getSelection(), new Command(null) {
									public void execute(Object[] data2) {
										if ((Boolean) data2[0]) {
											sme.mes.getHeightBrush().brushMode = HeightMode.Raise.ordinal();
											updateWidgets();
										}
									}
								},
								}) {
									public void execute(Object[] data2) {
										((Command) data[1]).execute(new Object[]{data[0]});
									}
								};
								smeGUI.messageQueue.offer(newCmd);
							}
						},
						new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
								Command newCmd = new Command(new Object[]{((Button) e.widget).getSelection(), new Command(null) {
									public void execute(Object[] data2) {
										if ((Boolean) data2[0]) {
											sme.mes.getHeightBrush().brushMode = HeightMode.Stamp.ordinal();
											updateWidgets();
										}
									}
								},
								}) {
									public void execute(Object[] data2) {
										((Command) data[1]).execute(new Object[]{data[0]});
									}
								};
								smeGUI.messageQueue.offer(newCmd);
							}
						},
						new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
								Command newCmd = new Command(new Object[]{((Button) e.widget).getSelection(), new Command(null) {
									public void execute(Object[] data2) {
										if ((Boolean) data2[0]) {
											sme.mes.getHeightBrush().brushMode = HeightMode.Set.ordinal();
											updateWidgets();
										}
									}
								},
								}) {
									public void execute(Object[] data2) {
										((Command) data[1]).execute(new Object[]{data[0]});
									}
								};
								smeGUI.messageQueue.offer(newCmd);
							}
						},
						new SelectionAdapter() {
							public void widgetSelected(SelectionEvent e) {
								Command newCmd = new Command(new Object[]{((Button) e.widget).getSelection(), new Command(null) {
									public void execute(Object[] data2) {
										if ((Boolean) data2[0]) {
											sme.mes.getHeightBrush().brushMode = HeightMode.Smooth.ordinal();
											updateWidgets();
										}
									}
								},
								}) {
									public void execute(Object[] data2) {
										((Command) data[1]).execute(new Object[]{data[0]});
									}
								};
								smeGUI.messageQueue.offer(newCmd);
							}
						},
				});
		
		addRadioGroup(tg, sme.mes.getTextureBrush().brushMode,
				new String[] { "Blend", "Add", "Multiply", "Stamp" }, new SelectionAdapter[] {
						new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent e)
							{
								Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
									public void execute(Object[] data2)
									{
										if ((Boolean)data2[0])
										{
											sme.mes.getTextureBrush().brushMode = TextureMode.Blend.ordinal();
											updateWidgets();
										}
									}},
								})
								{
									public void execute(Object[] data2)
									{
										((Command) data[1]).execute(new Object[] { data[0] });
									}
								};
								smeGUI.messageQueue.offer(newCmd);
							}
						},
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getTextureBrush().brushMode = TextureMode.Add.ordinal();
								updateWidgets();
							}
						}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);
				}
			},
						new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent e)
							{
								Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
									public void execute(Object[] data2)
									{
										if ((Boolean)data2[0])
										{
											sme.mes.getTextureBrush().brushMode = TextureMode.Multiply.ordinal();
											updateWidgets();
										}
									}},
								})
								{
									public void execute(Object[] data2)
									{
										((Command) data[1]).execute(new Object[] { data[0] });
									}
								};
								smeGUI.messageQueue.offer(newCmd);
							}
						},
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
					sme.mes.getTextureBrush().brushMode = TextureMode.Stamp.ordinal();
					updateWidgets();
							}
						}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);
				}
			},
		});
		
		b = new Button(tg, SWT.PUSH);
		b.setText("Select Color");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				ColorDialog cd = new ColorDialog(smeGUI.shell);
				if (cd.open() != null)
				{
					Command cmd = new Command(new Object[] { cd.getRGB().red, cd.getRGB().green, cd.getRGB().blue })
					{
						public void execute(Object[] data2)
						{
							sme.mes.getTextureBrush().setTextureToColor((Integer)data[0], (Integer)data[1], (Integer)data[2]);
							updateWidgets();
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});

		//spacer
		b = new Button(tg, SWT.NONE);
		b.setVisible(false);

		b = new Button(tg, SWT.PUSH);
		b.setText("Rotate CCW");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { null })
				{
					public void execute(Object[] data2)
					{
						sme.mes.getTextureBrush().rotateTexture(true);
						updateWidgets();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});

		//spacer
		b = new Button(tg, SWT.NONE);
		b.setVisible(false);

		b = new Button(tg, SWT.PUSH);
		b.setText("Import Texture");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(smeGUI.shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.png;*.tga;*.bmp;*.jpg" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0])})
					{
						public void execute(Object[] data2)
						{
							sme.mes.brushTextureManager.addBrushData((File)data[0]);
							((TextureBrush)(smeGUI.sme.mes.activeBrush)).setTexture(sme.mes.brushTextureManager.getBrushDataCount() - 1);
							updateWidgets();
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}else{
					updateWidgets();
				}
			}
		});

		//spacer
		b = new Button(tg, SWT.NONE);
		b.setVisible(false);
		
		b = new Button(tg, SWT.PUSH);
		b.setText("Rotate CW");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { null })
				{
					public void execute(Object[] data2)
					{
						sme.mes.getTextureBrush().rotateTexture(false);
						updateWidgets();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		/*b = new Button(tg, SWT.PUSH);
		b.setText("Toggle Pri/Sec");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
					Command cmd = new Command(new Object[] { null })
					{
						public void execute(Object[] data2)
						{
							sme.mes.getTextureBrush().swapPrimarySecondary();
						}
					};
					smeGUI.messageQueue.offer(cmd);
			}
		});*/
		
		/*addRadioGroup(fg, sme.mes.getFeatureBrush().mode.ordinal(),
				new String[] { "Add", "Rotate", "Rotate Same Random", "Move Features" }, new SelectionAdapter[] {
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getFeatureBrush().mode = FeatureMode.Add;
								updateWidgets();
							}
						}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);
				}
			},
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent event)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getFeatureBrush().mode = FeatureMode.Rotate;
								updateWidgets();
							}
						}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);
				}
			},
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getFeatureBrush().mode = FeatureMode.RotateSameRandom;
								updateWidgets();
							}
						}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);
				}
			},
		});
		
		b = new Button(fg, SWT.PUSH);
		b.setText("Select Feature");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				featureDialog.open();
			}
		});*/
		
		addRadioGroup(pfg, sme.mes.getPrefabBrush().mode.ordinal(), new String[] { "Set", "Add" }, new SelectionAdapter[] {
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getPrefabBrush().mode = PrefabMode.Set;
								updateWidgets();
							}
						}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);
				}
			},
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getPrefabBrush().mode = PrefabMode.Add;
								updateWidgets();
							}
						}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);
				}
			},
		});
		
		b = new Button(pfg, SWT.PUSH);
		b.setText("Align 1x1 Prefab");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.brushAlign.set(sme.mes.getPrefabBrush().getWidth() - 1, sme.mes.getPrefabBrush().getHeight() - 1);
				sme.mes.brushHeightAlign = sme.mes.getPrefabBrush().prefab.heightZ;
			}
		});

		b = new Button(pfg, SWT.NONE);
		b.setVisible(false);
		
		b = new Button(pfg, SWT.PUSH);
		b.setText("UnAlign Prefab");
		b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.brushAlign.set(1, 1);
				sme.mes.brushHeightAlign = 0;
			}
		});

		brushLayout.topControl = heightGroup;
		brushSettings.layout();

		addRadioGroup(brushmode, sme.mes.getBrushMode().ordinal(),
				new String[] { "Height", "Texture", "Prefab", "Metal", "Type", "Vegetation" /*"Feature",*/  /*"Copy/Paste"*/ }, new SelectionAdapter[] {
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						brushLayout.topControl = heightGroup;
						brushSettings.layout();
						brushSettings.setVisible(true);
						brushSettings.setText("Heightmode");
						texLayout.topControl = ptsel;
						texSelection.layout();
						texsel.setVisible(false);
						sme.mes.setBrushMode(BrushMode.Height);
						rs.mapMode = MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
						updateWidgets();
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						brushLayout.topControl = tg;
						brushSettings.layout();
						brushSettings.setVisible(true);
						brushSettings.setText("Texturemode");
						texLayout.topControl = ptsel;
						texSelection.layout();
						texsel.setVisible(true);
						sme.mes.setBrushMode(BrushMode.Texture);
						rs.mapMode = MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
						updateWidgets();
					}
				},
						new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent e)
							{
								brushLayout.topControl = pfg;
								brushSettings.layout();
								brushSettings.setVisible(true);
								brushSettings.setText("Prefabmode");
								texLayout.topControl = prefabsel;
								texSelection.layout();
								texsel.setVisible(false);
								sme.mes.setBrushMode(BrushMode.Prefab);
								rs.mapMode = MapMode.TextureMap;
								renderer.invalidateAllBlocks(false, true, false);
								updateWidgets();
							}
						},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						brushSettings.setVisible(false);
						texLayout.topControl = ptsel;
						texSelection.layout();
						texsel.setVisible(false);
						sme.mes.setBrushMode(BrushMode.Metal);
						rs.mapMode = MapMode.MetalMap;
						renderer.invalidateAllBlocks(false, true, false);
						updateWidgets();
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						brushSettings.setVisible(false);
						texLayout.topControl = ptsel;
						texSelection.layout();
						texsel.setVisible(false);
						sme.mes.setBrushMode(BrushMode.Type);
						rs.mapMode = MapMode.TypeMap;
						renderer.invalidateAllBlocks(false, true, false);
						updateWidgets();
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						brushSettings.setVisible(false);
						texLayout.topControl = ptsel;
						texSelection.layout();
						texsel.setVisible(false);
						sme.mes.setBrushMode(BrushMode.Vegetation);
						rs.mapMode = MapMode.VegetationMap;
						renderer.invalidateAllBlocks(false, true, false);
						updateWidgets();
					}
				},/*
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						heightGroup.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(true);
						pfg.setVisible(false);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Feature);
						rs.mapMode = MapMode.TextureMap;//FeatureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},*/

				/*new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						heightGroup.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
						cpg.setVisible(false);//true
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Copypaste);
						rs.mapMode = MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},*/
			});

		updateWidgets();
	}

	/**
	 * Synchronized update method
	 */
	public void updateWidgets()
	{
		smeGUI.glMessageQueue.offer(new Command(null) {
			public void execute(Object[] data2) {
				Brush brush = sme.mes.activeBrush;

				setWidgetValue(Widgets.SL_BRUSHSIZE, Integer.toString(brush.getWidth()));
				setWidgetValue(Widgets.L_BRUSHSIZE, "Brush Size: " + brush.getWidth());

				setWidgetValue(Widgets.SL_BRUSHSTRENGTH, Integer.toString(brush.getStrengthInt()));
				setWidgetValue(Widgets.L_BRUSHSTRENGTH, "Brush Strength: " + brush.getStrengthInt());

				Slider str = (Slider) widgetMap.get(Widgets.SL_BRUSHSTRENGTH);
				str.setMaximum(brush.getMaxStrengthInt() + 1);

				smeGUI.glCanvas.forceFocus();

				//TODO Update Brush/Texture Windows too
			}
		});
	}
	
	private void setWidgetValue(Widgets widget, String value)
	{
		Control c = widgetMap.get(widget);
		if (c != null)
		{
			if (c.getClass().equals(Label.class))
				((Label)c).setText(value);
			else if (c.getClass().equals(Slider.class))
				((Slider)c).setSelection(Integer.parseInt(value));
		}
	}

	public void animate(){
		if (prefabDialog != null)
			prefabDialog.animate();
	}
	
	private Slider addSlider(Widgets wLabel, Widgets wSlider, Composite parent, String initialText, int initialValue, int min, int max, final Command cmd, final Getter getText)
	{
		final Label label = new Label(parent, SWT.HORIZONTAL);
		GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1);
		gd.widthHint = 100;
		label.setLayoutData(gd);
		label.setText(initialText);
		
		Slider sl = new Slider(parent, SWT.HORIZONTAL);
		sl.setMinimum(min);
		sl.setMaximum(max + 1);
		sl.setThumb(1);
		sl.setSelection(initialValue);
		gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1);
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

	private Group addGroup(Composite parent, int columns){
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
		group.setLayout(new GridLayout(columns, false));
		return group;
	}
	
	private void addRadioGroup(Group group, int initialSelection, String[] names, final SelectionAdapter[] cmds)
	{
		for (int i = 0; i < names.length; i++)
		{
			Button b = new Button(group, SWT.RADIO);
			b.setText(names[i]);
			b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
			
			if (i == initialSelection)
				b.setSelection(true);
			
			b.addSelectionListener(cmds[i]);
		}
	}
}
