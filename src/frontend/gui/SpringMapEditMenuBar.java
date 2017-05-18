package frontend.gui;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import backend.FileHandler;
import backend.ZipWrapper;
import backend.SpringMapEdit;
import backend.FileHandler.FileFormat;
import backend.image.Bitmap;
import frontend.commands.Edit_Erode_Heightmap_Dry;
import frontend.commands.Edit_Erode_Heightmap_Wet;
import frontend.commands.Edit_Randomize_Heightmap;
import frontend.commands.Edit_Set_Heightmap;
import frontend.commands.Edit_Smooth_Heightmap;
import frontend.commands.RandomizeFeatures;
import frontend.gui.ProcessingDialog;
import frontend.render.MapRenderer;
import frontend.render.AppSettings;
import frontend.render.features.FeatureManager;

public class SpringMapEditMenuBar {
	public Shell shell;
	public Display display;
	
	public MenuItem wireframeButton;
	
	private SpringMapEditGUI smeGUI;
	private SpringMapEdit sme;
	private AppSettings rs;
	private MapRenderer renderer;

	private enum Widgets
	{
		L_FOV, SL_FOV,
		L_WATERLEVEL, SL_WATERLEVEL,
		L_BRUSHSTRENGTH, SL_BRUSHSTRENGTH,
		L_BRUSHSIZE, SL_BRUSHSIZE,
		L_MAXHEIGHT, SL_MAXHEIGHT
	}
	
	public SpringMapEditMenuBar(SpringMapEditGUI mainGUI)
	{
		shell = mainGUI.shell;
		Menu mainMenuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(mainMenuBar);
		
		this.smeGUI = mainGUI;
		this.sme = smeGUI.sme;
		this.rs = smeGUI.as;
		this.renderer = smeGUI.renderer;
		
		display = smeGUI.display;
		
		/////////////////////////////
		// MAP Menu
		/////////////////////////////
		MenuItem menuItem = new MenuItem(mainMenuBar, SWT.CASCADE);
		menuItem.setText("File");
		Menu menu = new Menu(menuItem);
		menuItem.setMenu(menu);
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("New");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				if (quitConfirmed())
				{
					new NewMapDialog(shell, smeGUI).open();
					sme.CurrentMap = "";
					shell.setText("Spring Map Edit 1.4.3 - New Map");
				}
			}
		});
		
		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Open");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (quitConfirmed())
				{
					FileDialog fd = new FileDialog(shell, SWT.OPEN);
					fd.setFilterExtensions(new String[] { "*.smf;"}); //*.sdz;" });
					if (fd.open() != null)
					{
						final ProcessingDialog pd = new ProcessingDialog(shell);
						Object[] data = new Object[]{new File(fd.getFilterPath(), fd.getFileNames()[0]), (Object)renderer};
						/*Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer }) 
						{
							public void execute(Object[] data2)
							{*/
						try {
							if (((File)data[0]).getName().endsWith(".sdz"))
							{
								File files[];
									try {
										ZipWrapper handler = new ZipWrapper((File)data[0]);
										files = handler.read();
										if (files == null)
											return;
										data[0] = handler.smf;
										if (files[0] == null || files[1] == null)
											return;
									} catch (IOException e)
									{
										e.printStackTrace();
											return;
									}
									sme.loadSM2Map(files[0], files[1], (MapRenderer) data[1]);
									sme.CurrentMap = "";
									smeGUI.resetCamera();
							}
							else
							{
								sme.loadSM2Map((File) data[0], (MapRenderer) data[1]);
								sme.CurrentMap = data[0].toString();
								smeGUI.resetCamera();
							}
							pd.close();
						} catch (Exception e)
						{
							e.printStackTrace();
							return;
						}
						/*	}
						};
						smeGUI.messageQueue.offer(cmd);*/
						renderer.setSpringMapEdit(sme);
						shell.setText("Spring Map Edit 1.4.3 - " + sme.CurrentMap);
					}
				}
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Save");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (sme.CurrentMap == "") {
					FileDialog fd = new FileDialog(shell, SWT.SAVE);
					fd.setFilterExtensions(new String[] { "*.smf;" });
					if (fd.open() != null)
					{
						final ProcessingDialog pd = new ProcessingDialog(shell);
						Object[] data = new Object[]{new File(fd.getFilterPath(), fd.getFileNames()[0]), (Object)renderer};
						/*Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer }) 
						{
							public void execute(Object[] data2)
							{*/
						try
						{
							sme.saveSM2Map((File) data[0], (MapRenderer) data[1]);
							sme.CurrentMap = data[0].toString();
							shell.setText("Spring Map Edit 1.4.3 - " + sme.CurrentMap);
						}
						catch (Exception e)
						{
							new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
						}
						/*}
						};
						smeGUI.messageQueue.offer(cmd);*/
						pd.close();
					}
				}
				else
				{
					final ProcessingDialog pd = new ProcessingDialog(shell);
					/*Command cmd = new Command(new Object[] { new File(CurrentMap), renderer }) 
					{
						public void execute(Object[] data2)
						{*/
							sme.saveSM2Map(new File(sme.CurrentMap), renderer); //(File) data[0], (MapRenderer) data[1]);
					/*	}
					};
					smeGUI.messageQueue.offer(cmd);	*/
					pd.close();
				}
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Save As");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.smf;" });
				if (fd.open() != null)
				{
					final ProcessingDialog pd = new ProcessingDialog(shell);
					Object[] data = new Object[]{new File(fd.getFilterPath(), fd.getFileNames()[0]), (Object)renderer};
					/*Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer }) 
					{
						public void execute(Object[] data2)
						{*/
					try
					{
						File file = new File(fd.getFilterPath(), fd.getFileNames()[0]);
						sme.saveSM2Map(file, renderer); //(File) data[0], (MapRenderer) data[1]);
						sme.CurrentMap = file.toString(); //data[0].toString();
						shell.setText("Spring Map Edit 1.4.3 - " + sme.CurrentMap);
					}
					catch (Exception e)
					{
						new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
					}
						/*}
					};
					smeGUI.messageQueue.offer(cmd);*/
					pd.close();
				}
			}
		});
		/*
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Change Tile File");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.smt;" });
				if (fd.open() != null)
				{
					final ProcessingDialog pd = new ProcessingDialog(shell);
					Object[] data = new Object[]{new File(fd.getFilterPath(), fd.getFileNames()[0]), (Object)renderer};
					/*Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer }) 
					{
						public void execute(Object[] data2)
						{*/
					/*try
					{
						sme.map.smtFile = new File(fd.getFilterPath(), fd.getFileNames()[0]).getName();
					}
					catch (Exception e)
					{
						new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
					}
						/*}
					};
					smeGUI.messageQueue.offer(cmd);*/
					/*pd.close();
				}
			}
		});*/
		
		/////////////////////////////
		// Import Submenu
		/////////////////////////////
		menuItem = new MenuItem(menu, SWT.CASCADE);
		menuItem.setText("Import");
		Menu subMenu = new Menu(menuItem);
		menuItem.setMenu(subMenu);
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Load All Maps");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer }) 
					{
						public void execute(Object[] data2)
						{
							try {
								sme.loadAllMaps((File) data[0], (MapRenderer) data[1]);
							}
							catch (Exception e)
							{
								//new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
							}
						}
					};
					smeGUI.messageQueue.offer(cmd);
					renderer.setSpringMapEdit(sme);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.SEPARATOR);
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Import HeightMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.bmp;*.raw;*.png;*.tga;*.tif;" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							try 
							{
							File filename = (File) data[0];
							if (FileHandler.isHandledByBitmap(filename))
							{
								boolean ok = true;
								Bitmap bitmap = new Bitmap(filename);
								if (bitmap.requiresSizeInfo())
								{
									int result = new InputBox(smeGUI.shell, "Please enter width of RAW Image", "513").open();
									if (result > 0)
										ok = bitmap.setSizeInfo(result);
									else
										ok = false;
								}
								if (ok)
									sme.map.heightmap.loadDataIntoHeightmap(bitmap);
							}	
							else
								sme.map.heightmap.loadDataIntoHeightmap(filename);
							//sme.map.heightMap.loadDataIntoHeightmap(filename);
							}
							catch (Exception e)
							{
								//new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
							}
						}
					};
					smeGUI.messageQueue.offer(cmd);
					renderer.invalidateAllBlocks(true, false, false);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Import TextureMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							try 
							{
								sme.map.textureMap.loadDataIntoTexturemap((File) data[0]);
							}
							catch (Exception e)
							{
								//new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
							}
						}
					};
					smeGUI.messageQueue.offer(cmd);
					renderer.invalidateAllBlocks(false, true, false);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Import MetalMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							try 
							{
								sme.map.metalmap.loadDataIntoMap((File) data[0]);
							}
							catch (Exception e)
							{
								//new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
							}
						}
					};	
					smeGUI.messageQueue.offer(cmd);
					renderer.invalidateAllBlocks(false, true, false);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Import FeatureMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.fmf" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer }) 
					{
						public void execute(Object[] data2)
						{
							try
							{
								sme.map.featuremap.loadDataIntoFeaturemap((File) data[0], ((MapRenderer) data[1]).getFeatureManager());
							}
							catch (Exception e)
							{
								//new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
							}
						}
					};
					smeGUI.messageQueue.offer(cmd);
					renderer.invalidateAllBlocks(false, false, true);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Import TypeMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							try
							{
								sme.map.loadDataIntoTypemap((File) data[0]);
							}
							catch (Exception e)
							{
								//new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
							}
						}
					};
					smeGUI.messageQueue.offer(cmd);
					renderer.invalidateAllBlocks(false, true, false);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Import VegetationMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							try
							{
								sme.map.loadDataIntoVegetationmap((File) data[0]);
							}
							catch (Exception e)
							{
								//new DialogBox(shell, "Error during loading", "Error: " + e.getMessage());
							}
						}
					};
					smeGUI.messageQueue.offer(cmd);
					renderer.invalidateAllBlocks(false, true, false);
				}
			}
		});
		
		/////////////////////////////
		// Export Submenu
		/////////////////////////////
		menuItem = new MenuItem(menu, SWT.CASCADE);
		menuItem.setText("Export");
		subMenu = new Menu(menuItem);
		menuItem.setMenu(subMenu);
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Export All Maps");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				final FileDialog fd = new FileDialog(shell, SWT.SAVE);
				if (fd.open() != null)
				{
					Thread t = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							sme.saveAllMaps(new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer);//(File) data[0], (MapRenderer) data[1]);
						}
					});
					t.start();
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.SEPARATOR);
		/*
		menuItem = new MenuItem(subMenu, SWT.PUSH); // We do not want to encourage people to save as uncompressed bitmaps
		menuItem.setText("Save HeightMap (8bit)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							sme.heightMap.saveHeightMap((File) data[0], FileFormat.Bitmap8Bit);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});*/
				
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save HeightMap (RAW 16bit)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				final FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.raw" });
				if (fd.open() != null)
				{
					Thread t = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							sme.map.heightmap.saveHeightMap(new File(fd.getFilterPath(), fd.getFileNames()[0]), FileFormat.Raw16Bit);
						}
					});
					t.start();
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save HeightMap (RAW 16bit + SimpleHeader)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.raw" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							Thread t = new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									sme.map.heightmap.saveHeightMap((File) data[0], FileFormat.Raw16BitSM);
								}
							});
							t.start();
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save HeightMap (PNG 16bit)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.png" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							Thread t = new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									sme.map.heightmap.saveHeightMap((File) data[0], FileFormat.PNG16Bit);
								}
							});
							t.start();
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save TextureMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							Thread t = new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									sme.map.textureMap.saveTextureMap((File) data[0]);
								}
							});
							t.start();
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save MetalMap (8bit)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				final FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Thread t = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							sme.map.metalmap.saveMetalMap(new File(fd.getFilterPath(), fd.getFileNames()[0]), false);
						}
					});
					t.start();
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save MetalMap (24bit)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				final FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Thread t = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							sme.map.metalmap.saveMetalMap(new File (fd.getFilterPath(), fd.getFileNames()[0]), true);
						}
					});
					t.start();
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save FeatureMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.fmf" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]), renderer.getFeatureManager()}) 
					{
						public void execute(Object[] data2)
						{
							sme.map.featuremap.saveFeatureMap((File) data[0], (FeatureManager) data[1]);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save TypeMap (8bit)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							sme.map.saveTypeMap((File) data[0], false);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save TypeMap (24bit)");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							sme.map.saveTypeMap((File) data[0], true);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
			
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save VegetationMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							sme.map.saveVegetationMap((File) data[0]);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.PUSH);
		menuItem.setText("Save SlopeMap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				fd.setFilterExtensions(new String[] { "*.bmp" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							sme.saveSlopeMap((File) data[0]);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		/////////////////////
		// File Menu Continue
		/////////////////////
		menuItem = new MenuItem(menu, SWT.SEPARATOR);
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Exit");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				if ((!sme.as.quitWithoutAsking) && (!sme.as.quietExit) && quitConfirmed())
				{
					smeGUI.kill = true;
					synchronized (smeGUI.messageQueue)
					{
						smeGUI.messageQueue.notify();
					}
					shell.dispose();
					System.out.println("Exiting normally");
				}
			}
		});
		
		/////////////////////////////
		// EDIT Menu
		/////////////////////////////
		menuItem = new MenuItem(mainMenuBar, SWT.CASCADE);
		menuItem.setText("Edit");
		menu = new Menu(menuItem);
		menuItem.setMenu(menu);
		
		/*menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Undo");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(null) 
				{
					public void execute(Object[] data2)
					{
						smeGUI.undo();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Redo");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(null) 
				{
					public void execute(Object[] data2)
					{
						smeGUI.redo();
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});*/
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Mirror Map");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new MirrorMapDialog(shell, smeGUI).open();
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Flip Map");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new FlipMapDialog(shell, smeGUI).open();
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Shift Map");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new ShiftMapDialog(shell, smeGUI).open();
			}
		});
		/*
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Scale Map");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{

			}
		});
		*/
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Randomize Heightmap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new RandomizeTerrainDialog(shell, smeGUI).open();
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Smooth Heightmap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new Edit_Smooth_Heightmap(smeGUI).execute(null);
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Clear Heightmap");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new Edit_Set_Heightmap(smeGUI).execute(null);
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Randomize Features");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new RandomizeFeatures(smeGUI).execute(null);
			}
		});
		
		/////////////////////////////
		// VIEW Menu
		/////////////////////////////
		menuItem = new MenuItem(mainMenuBar, SWT.CASCADE);
		menuItem.setText("View");
		menu = new Menu(menuItem);
		menuItem.setMenu(menu);
		
		menuItem = new MenuItem(menu, SWT.CHECK);
		menuItem.setText("Blend in Texturemap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.blendTextureMap = (Boolean) data[0];
						renderer.invalidateAllBlocks(false, true, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Show Texturemap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() })
				{
					public void execute(Object[] data2)
					{
						rs.mapMode = MapRenderer.MapMode.TextureMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Show Metalmap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() })
				{
					public void execute(Object[] data2)
					{
						rs.mapMode = MapRenderer.MapMode.MetalMap;
						renderer.invalidateAllBlocks(false, true, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		wireframeButton = new MenuItem(menu, SWT.CHECK);
		menuItem = wireframeButton;
		menuItem.setText("Show Wireframe");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				smeGUI.renderer.toggleWireframeMode();
				smeGUI.renderer.camViewChangedNotify();
			}
		});
		
		/*menuItem = new MenuItem(menu, SWT.CHECK);
		menuItem.setText("Show Features");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						
						renderer.invalidateAllBlocks(false, false, true);
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});*/
		
		/////////////////////////////
		// TexGen Menu
		/////////////////////////////
		menuItem = new MenuItem(mainMenuBar, SWT.CASCADE);
		menuItem.setText("TexGen");
		menu = new Menu(menuItem);
		menuItem.setMenu(menu);
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Load TexGen settings");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.tdf" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							sme.mes.setTextureGeneratorSetupByFile((File) data[0]);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(menu, SWT.SEPARATOR);
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("TexGen whole map");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(null) 
				{
					public void execute(Object[] data2)
					{
						smeGUI.sme.genColorsByHeight(0, 0, null);
						smeGUI.renderer.invalidateAllBlocks(false, true, false);
					}
				};
				smeGUI.messageQueue.offer(cmd);
				new ProcessingDialog(shell).close();
			}
		});
		
		/////////////////////////////
		// Erosion Menu
		/////////////////////////////
		menuItem = new MenuItem(mainMenuBar, SWT.CASCADE);
		menuItem.setText("Erosion");
		menu = new Menu(menuItem);
		menuItem.setMenu(menu);
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Load erosion settings");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setFilterExtensions(new String[] { "*.tdf" });
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							sme.mes.setErosionSetupByFile((File) data[0]);
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Save erosion settings");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog fd = new FileDialog(shell, SWT.SAVE);
				if (fd.open() != null)
				{
					Command cmd = new Command(new Object[] { new File(fd.getFilterPath(), fd.getFileNames()[0]) }) 
					{
						public void execute(Object[] data2)
						{
							String path = ((File) data[0]).getAbsolutePath();
							String ext = ".tdf";
							if (!path.endsWith(ext))
								path = path + ext;
							sme.mes.getErosionSetup().saveToFile(new File(path));
						}
					};
					smeGUI.messageQueue.offer(cmd);
				}
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Erosion Settings");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				new ErosionSettingsDialog(shell, smeGUI).open();
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Erode whole map");
		menuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e)
			{
				new ErodeMapDialog(shell, smeGUI).open();
				//smeGUI.messageQueue.offer(new Edit_Erode_Heightmap_Dry(smeGUI));
			}
		});
		
		/////////////////////////////
		// SETTINGS Menu
		/////////////////////////////
		menuItem = new MenuItem(mainMenuBar, SWT.CASCADE);
		menuItem.setText("Settings");
		menu = new Menu(menuItem);
		menuItem.setMenu(menu);
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("SpringMapEdit Settings");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				new RenderSettingsDialog(shell, smeGUI).open();
			}
		});
		
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("SM2 Compile Settings");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				new CompileSettingsDialog(shell, smeGUI).open();
			}
		});
		
		//////////////////////////////
		////Quicksave submenu
		//////////////////////////////
		menuItem = new MenuItem(menu, SWT.CASCADE);
		menuItem.setText("Quicksave");
		subMenu = new Menu(menuItem);
		menuItem.setMenu(subMenu);
		
		menuItem = new MenuItem(subMenu, SWT.CHECK);
		menuItem.setSelection(rs.quicksave_compress);
		menuItem.setText("Compress Quicksaves (slower, saves RAM)");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quicksave_compress = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.CHECK);
		menuItem.setSelection(rs.quicksave_heightmap);
		menuItem.setText("Store Heightmap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quicksave_heightmap = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.CHECK);
		menuItem.setSelection(rs.quicksave_texturemap);
		menuItem.setText("Store Texturemap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quicksave_texturemap = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.CHECK);
		menuItem.setSelection(rs.quicksave_metalmap);
		menuItem.setText("Store Metalmap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quicksave_metalmap = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.CHECK);
		menuItem.setSelection(rs.quicksave_typemap);
		menuItem.setText("Store Typemap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quicksave_typemap = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.CHECK);
		menuItem.setSelection(rs.quicksave_vegetationmap);
		menuItem.setText("Store Vegetationmap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quicksave_vegetationmap = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		menuItem = new MenuItem(subMenu, SWT.CHECK);
		menuItem.setSelection(rs.quicksave_featuremap);
		menuItem.setText("Store Featuremap");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quicksave_featuremap = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});
		
		///////////////////////////
		//SETTINGS Menu Continue
		///////////////////////////
		
		menuItem = new MenuItem(menu, SWT.CHECK);
		menuItem.setSelection(rs.quitWithoutAsking);
		menuItem.setText("Quit without asking");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				Command cmd = new Command(new Object[] { ((MenuItem)e.widget).getSelection() }) 
				{
					public void execute(Object[] data2)
					{
						rs.quitWithoutAsking = (Boolean) data[0];
					}
				};
				smeGUI.messageQueue.offer(cmd);
			}
		});

		//spacer
		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Change Map Properties");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				new EditMapDialog(shell, smeGUI).open();
			}
		});
		
	}
	
	private boolean quitConfirmed()
	{
		if (rs.quitWithoutAsking || rs.quietExit)
			return true;
		else
			return new OkCancelDialog(shell, "Confirm Action", "Really Do This? Unsaved changes will be lost.").open() == SWT.OK;
	}
	
}
