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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

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
	public Shell shell;
	private boolean isUsingOwnDisplay;
	
	private SpringMapEditGUI smeGUI;
	private SpringMapEdit sme;
	private AppSettings rs;
	private MapRenderer renderer;
	private FeatureSelectionDialog featureDialog;
	private BrushSelectionDialog brushDialog;
	private TextureSelectionDialog textureDialog;
	private PrefabSelectionDialog prefabDialog;
	
	private EnumMap<Widgets, Control> widgetMap;
	
	private enum Widgets
	{
		L_FOV, SL_FOV,
		L_BRUSHSTRENGTH, SL_BRUSHSTRENGTH,
		L_BRUSHSIZE, SL_BRUSHSIZE,
	}
	
	public SpringMapEditDialog(SpringMapEditGUI mainGUI)
	{
		this.smeGUI = mainGUI;
		this.sme = smeGUI.sme;
		this.rs = smeGUI.as;
		this.renderer = smeGUI.renderer;
		this.featureDialog = new FeatureSelectionDialog(smeGUI, this);
		this.brushDialog = new BrushSelectionDialog(smeGUI, this);
		this.textureDialog = new TextureSelectionDialog(smeGUI, this);
		this.prefabDialog = new PrefabSelectionDialog(smeGUI, this);
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
		//Check if we may create a secondary Display:
		isUsingOwnDisplay = isAdditionalDisplaySupported();
		
		if (isUsingOwnDisplay)
		{
			Thread t = new Thread(new Runnable() 
			{	
				public void run()
				{
					display = new Display();
					
					//Execute main loop
					open_main();
				}
			});
			t.start();
		}
		else
		{
			//Re-use main display
			display = smeGUI.display;
			
			//Execute main loop
			open_main();
		}
	}
	
	public void setAlwaysOnTop(boolean alwaysOnTop)
	{
		rs.dialogAlwaysOnTop = alwaysOnTop;
	}
	
	private void open_main()
	{
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.RESIZE | (rs.dialogAlwaysOnTop ? SWT.ON_TOP : SWT.NONE));
		shell.setText("Spring Map Editor Setup");
		int width = 400;
		shell.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width - width, 44);	
		shell.setSize(width, 600);	
		
		createDialogArea();
		
		//Open it
		shell.layout();
		shell.setVisible(true);
		
		if (isUsingOwnDisplay)
		{
			//Run message loop
			while (!shell.isDisposed())
				if (!display.readAndDispatch())
					display.sleep();
			
			//Dispose Display
			display.dispose();
		}
	}
	
	private void createDialogArea()
	{
		////Layout
		shell.setLayout(new GridLayout(4, false));
		
		////Controls
		
		addSlider(Widgets.L_BRUSHSTRENGTH, Widgets.SL_BRUSHSTRENGTH, shell, "Brush Strength: " + sme.mes.activeBrush.getStrengthInt(), sme.mes.activeBrush.getStrengthInt(), 1, 1000, 
				new Command(null) { public void execute(Object[] data2)
				{
					sme.mes.activeBrush.setStrengthInt((Integer) data2[0]);
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Brush Strength: " + (Integer) data2[0];
				}});
		
		addSlider(Widgets.L_BRUSHSIZE, Widgets.SL_BRUSHSIZE, shell, "Brush Size: " + sme.mes.activeBrush.width, sme.mes.activeBrush.getWidth(), 1, 4000,
				new Command(null) { public void execute(Object[] data2)
				{
					sme.mes.activeBrush.setSize((Integer) data2[0], (Integer) data2[0]);
				}},
				new Getter(null) { public Object getValue(Object[] data2)
				{
					return "Brush Size: " + (Integer) data2[0];
				}});
		
		final Group hg, tg, fg, pfg, cpg;
		
		Button b = new Button(shell, SWT.PUSH);
		b.setText("Select Brush");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				brushDialog.open();
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Rotate CCW");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.rotate(true);
				updateWidgets();
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Rotate CW");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.rotate(false);
				updateWidgets();
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Mirror horiz");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.mirror(true);
				updateWidgets();
			}
		});
		
		b = new Button(shell, SWT.PUSH);
		b.setText("Mirror vert");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.activeBrush.mirror(false);
				updateWidgets();
			}
		});
		
		hg = addRadioGroup(shell, "Heightmode", sme.mes.getHeightBrush().brushMode, 3, 5,
				new String[] { "Raise/Lower", "Smooth", "Set", "Erode", "Randomize", "Copy/Paste" }, new SelectionAdapter[] {
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getHeightBrush().brushMode = HeightMode.Raise.ordinal();
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
					updateWidgets();
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
								sme.mes.getHeightBrush().brushMode = HeightMode.Smooth.ordinal();
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
					updateWidgets();
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
									sme.mes.getHeightBrush().brushMode = HeightMode.Set.ordinal();
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
						updateWidgets();
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
									sme.mes.getHeightBrush().brushMode = HeightMode.Erode.ordinal();
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
						updateWidgets();
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
									sme.mes.getHeightBrush().brushMode = HeightMode.Randomize.ordinal();
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
					updateWidgets();
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
								//sme.mes.getHeightBrush().brushMode = HeightMode.CopyPaste.ordinal();
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
					updateWidgets();
				}
			},
		});
		
		tg = addRadioGroup(shell, "Texturemode", sme.mes.getTextureBrush().brushMode, 4, 5,
				new String[] { "Set", "Add", "Blend", "Stamp", "TexGen" }, new SelectionAdapter[] {
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getTextureBrush().brushMode = TextureMode.Set.ordinal();
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
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{
							if ((Boolean)data2[0])
							{
								sme.mes.getTextureBrush().brushMode = TextureMode.TexGen.ordinal();
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
		b.setText("Select Texture");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				textureDialog.open();
			}
		});
		
		b = new Button(tg, SWT.PUSH);
		b.setText("Select Color");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				ColorDialog cd = new ColorDialog(shell);
				if (cd.open() != null)
				{
					Command cmd = new Command(new Object[] { cd.getRGB().red, cd.getRGB().green, cd.getRGB().blue })
					{
						public void execute(Object[] data2)
						{
							sme.mes.getTextureBrush().setTextureToColor((Integer)data[0], (Integer)data[1], (Integer)data[2]);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		b = new Button(tg, SWT.PUSH);
		b.setText("Import Texture");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.png;*.tga;*.bmp;*.jpg" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0])}) 
					{
						public void execute(Object[] data2)
						{
							sme.mes.brushTextureManager.addBrushData((File)data[0]);
							((TextureBrush)(smeGUI.sme.mes.activeBrush)).setTexture(sme.mes.brushTextureManager.getBrushDataCount() - 1);
							/*display.syncExec(new Runnable()
							{
								public void run()
								{
								}
							});*/
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		b = new Button(tg, SWT.PUSH);
		b.setText("Flip Cclockwise");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
					Command cmd = new Command(new Object[] { null })
					{
						public void execute(Object[] data2)
						{
							sme.mes.getTextureBrush().rotateTexture(true);
						}
					};
					smeGUI.messageQueue.offer(cmd);
			}
		});
		
		b = new Button(tg, SWT.PUSH);
		b.setText("Flip Clockwise");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { null })
				{
					public void execute(Object[] data2)
					{
						sme.mes.getTextureBrush().rotateTexture(false);
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
		
		tg.setVisible(false);
		
		fg = addRadioGroup(shell, "Featuremode", sme.mes.getFeatureBrush().mode.ordinal(), 4, 3, 
				new String[] { "Add", "Rotate", "Rotate Same Random", "Move Features" }, new SelectionAdapter[] {
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					/*Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{*/
							if (((Button)e.widget).getSelection())//(Boolean)data2[0])
							{
								sme.mes.getFeatureBrush().mode = FeatureMode.Add;
								updateWidgets();
							}
						/*}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);*/
				}
			},
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent event)
				{
					/*Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{*/
							if (((Button)event.widget).getSelection())//(Boolean)data2[0])
							{
								sme.mes.getFeatureBrush().mode = FeatureMode.Rotate;
								updateWidgets();
							}
						/*}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);*/
				}
			},
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					/*Command newCmd = new Command(new Object[] { ((Button)e.widget).getSelection(), new Command(null) {
						public void execute(Object[] data2)
						{*/
							if (((Button)e.widget).getSelection())//(Boolean)data2[0])
							{
								sme.mes.getFeatureBrush().mode = FeatureMode.RotateSameRandom;
								updateWidgets();
							}
						/*}},
					})
					{
						public void execute(Object[] data2)
						{
							((Command) data[1]).execute(new Object[] { data[0] });
						}
					};
					smeGUI.messageQueue.offer(newCmd);*/
				}
			},
		});
		
		fg.setVisible(false);
		
		b = new Button(fg, SWT.PUSH);
		b.setText("Select Feature");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				featureDialog.open();
			}
		});
		
		pfg = addRadioGroup(shell, "Prefabmode", sme.mes.getPrefabBrush().mode.ordinal(), 3, 2, new String[] { "Set", "Add" }, new SelectionAdapter[] {
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
		b.setText("Select Prefab");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				prefabDialog.open();
			}
		});
		
		b = new Button(pfg, SWT.PUSH);
		b.setText("Align 1x1 Prefab");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.brushAlign.set(sme.mes.getPrefabBrush().getWidth() - 1, sme.mes.getPrefabBrush().getHeight() - 1);
				sme.mes.brushHeightAlign = sme.mes.getPrefabBrush().prefab.heightZ;
			}
		});
		
		b = new Button(pfg, SWT.PUSH);
		b.setText("UnAlign Prefab");
		b.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				sme.mes.brushAlign.set(1, 1);
				sme.mes.brushHeightAlign = 0;
			}
		});
		
		pfg.setVisible(false);
		
		cpg = addRadioGroup(shell, "Copypastemode", sme.mes.getCopypasteBrush().mode.ordinal(), 1, 0, new String[] { }, new SelectionAdapter[] {
			
		});
		
		cpg.setVisible(false);
		
		addRadioGroup(shell, "Brushmode", sme.mes.getBrushMode().ordinal(), 4, 8, //10,
				new String[] { "Height", "Texture", "Metal", "Type", "Vegetation", /*"Diffuse", "Decal",*/ "Feature", "Prefab", "Copy/Paste" }, new SelectionAdapter[] {
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(true);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Height);
						rs.mapMode = MapMode.SlopeMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(true);
						fg.setVisible(false);
						pfg.setVisible(false);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Texture);
						rs.mapMode = MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Metal);
						rs.mapMode = MapMode.MetalMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Type);
						rs.mapMode = MapMode.TypeMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Vegetation);
						rs.mapMode = MapMode.VegetationMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
				/*new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
		cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Diffuse);
						rs.mapMode = MapMode.DiffuseMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},*/
				/*new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
		cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Decal);
						rs.mapMode = MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},*/
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(true);
						pfg.setVisible(false);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Feature);
						rs.mapMode = MapMode.TextureMap;//FeatureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(true);
						cpg.setVisible(false);
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Prefab);
						rs.mapMode = MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						hg.setVisible(false);
						tg.setVisible(false);
						fg.setVisible(false);
						pfg.setVisible(false);
						cpg.setVisible(false);//true
						updateWidgets();
						sme.mes.setBrushMode(BrushMode.Copypaste);
						rs.mapMode = MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				},
			});
	}

	/**
	 * Synchronized update method
	 */
	public void updateWidgets()
	{
		display.syncExec(new Runnable()
		{
			public void run()
			{
				Brush brush = sme.mes.activeBrush;
				
				setWidgetValue(Widgets.SL_BRUSHSIZE, Integer.toString(brush.getWidth()));
				setWidgetValue(Widgets.L_BRUSHSIZE, "Brush Size: " + brush.getWidth());
				
				setWidgetValue(Widgets.SL_BRUSHSTRENGTH, Integer.toString(brush.getStrengthInt()));
				setWidgetValue(Widgets.L_BRUSHSTRENGTH, "Brush Strength: " + brush.getStrengthInt());
				
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
		gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1);
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
	
	private Group addRadioGroup(Composite parent, String caption, int initialSelection, int columns, int count, String[] names, final SelectionAdapter[] cmds)
	{
		Group group = new Group(shell, SWT.SHADOW_NONE);
		group.setText(caption);
		GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4, 1);
		group.setLayoutData(gd);
		group.setLayout(new GridLayout(columns, false));
		
		for (int i = 0; i < count; i++)
		{
			Button b = new Button(group, SWT.RADIO);
			b.setText(names[i]);
			b.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1, 1));
			
			if (i == initialSelection)
				b.setSelection(true);
			final int fi = i;
			
			b.addSelectionListener(cmds[fi]);
		}
		
		return group;
	}
	
	public void dispose()
	{
		//Check if we use own display&thread. If yes, close it. If no, do nothing (we rely on parent display)
		if (isUsingOwnDisplay)
		{
			if (!display.isDisposed())
			{
				display.syncExec(new Runnable()
				{
					public void run()
					{
						if (!shell.isDisposed())
							shell.dispose();
					}
				});
			}
		}
	}
	
	public void animate()
	{
		featureDialog.animate();
		prefabDialog.animate();
	}
}
