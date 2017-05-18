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
 * SpringMapEditGUI.java 
 * Created on 03.07.2008
 * by Heiko Schmitt
 */
package frontend.gui;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.LockSupport;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.*;

import backend.SpringMapEdit;
import backend.UndoRedo;
import backend.math.Vector3;
import backend.math.Vector3Math;
import frontend.keybinding.KeyMapper;
import frontend.render.MapRenderer;
import frontend.render.AppSettings;
import frontend.gui.SpringMapEditMenuBar;
import frontend.gui.Console;

/**
 * @author Heiko Schmitt
 *
 */
public class SpringMapEditGUI
{
	public Display display;
	public Shell shell;
	
	private Cursor cursorDefault;
	private Cursor cursorCross;
	
	public SpringMapEdit sme;
	
	public GLContext glContext;
	public GLCanvas glCanvas;
	public AppSettings as;
	public MapRenderer renderer;
	private SpringMapEditDialog setupDialog;
	
	public KeyMapper keyMap;
	public boolean[] holdableKeys;
	
	public boolean lockCameraY;
	
	public Deque<UndoRedo> undoQueue;
	
	private Point mousePos, mousePosOld, mousePosCenter;
		
	public Queue<Command> glMessageQueue;
	public Queue<Command> messageQueue;
	
	public enum HoldableKeys
	{
		SHIFT,
		CTRL,
		ALT,
		ARROW_LEFT,
		ARROW_RIGHT,
		ARROW_DOWN,
		ARROW_UP,
		PAGE_UP,
		PAGE_DOWN,
		MOUSE_1,
		MOUSE_2,
		MOUSE_3,
		
		LAST
	}
	private static final int KEYCOUNT = HoldableKeys.LAST.ordinal();
	public boolean kill = false;
	Thread worker;		
	/**
	 * @throws HeadlessException
	 */
	public SpringMapEditGUI(AppSettings settings)
	{
		this.as = settings;
		this.display = new Display();
		this.shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));
		undoQueue = new ConcurrentLinkedDeque<UndoRedo>();
		//Show Logo Dialog
		try
		{
			new LogoDialog(shell, 3000, new File("textures/logo.png"));
		}
		catch (Exception e)
		{
			//ignore...
		}
		ErrorHandler errorHandler = new ErrorHandler(shell);

		Group dialogpanel = new Group(shell, SWT.SHADOW_NONE);
		dialogpanel.setLayout(new GridLayout(1, false));
		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true, 1, 1);
		gd.widthHint = 375;
		dialogpanel.setLayoutData(gd);

		GLData data = new GLData();
		data.doubleBuffer = true;

		glCanvas = new GLCanvas(shell, SWT.NO_BACKGROUND, data);
		gd = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1);
		//gd.widthHint = as.displayWidth;
		//gd.heightHint = as.displayHeight;
		glCanvas.setLayoutData(gd);
		
		cursorDefault = new Cursor(display, SWT.CURSOR_ARROW);
		cursorCross = new Cursor(display, SWT.CURSOR_CROSS);

		this.messageQueue = new ConcurrentLinkedQueue<Command>();
		this.glMessageQueue = new ConcurrentLinkedQueue<Command>();
		
		this.holdableKeys = new boolean[KEYCOUNT];
		for (int i = 0; i < KEYCOUNT; i++)
			this.holdableKeys[i] = false;
		mousePosCenter = new Point(as.displayWidth / 2, as.displayHeight / 2);
		mousePos = new Point(as.displayWidth / 2, as.displayHeight / 2);
		this.keyMap = new KeyMapper(this);
		keyMap.loadFromFile(new File("config/keys.cfg"));
		shell.setText("Spring Map Edit 1.4.3 - New Map");
		
		shell.addShellListener(new ShellAdapter() 
		{
			public void shellActivated(ShellEvent e)
			{
				super.shellActivated(e);
				glCanvas.forceFocus();
			}
			
			@Override
			public void shellClosed(ShellEvent e)
			{
				if ((!as.quitWithoutAsking) && (!as.quietExit) && (new OkCancelDialog(shell, "Confirm Exit", "Really Exit? Unsaved changes will be lost.").open() != SWT.OK))
					e.doit = false;
				else
				{
					System.out.println("Exiting normally");
					kill = true;
					synchronized (messageQueue)
					{
						messageQueue.notify();
					}
				}
			}
			
		});
		
		shell.addListener(SWT.Resize, new Listener() 
		{
			public void handleEvent(Event event)
			{
				setMouseCenter();
				//if (glCanvas != null)
				//	glCanvas.setSize(shell.getClientArea().width, shell.getClientArea().height);
			}
		});
		
		shell.addListener(SWT.Deactivate, new Listener() 
		{
			public void handleEvent(Event event)
			{
				deactivateMouseLook();
				deactivateKeys();
			}
		});
		
		shell.addListener(SWT.Move, new Listener() 
		{
			public void handleEvent(Event event)
			{
				setMouseCenter();
			}
		});
		
		glCanvas.addListener(SWT.Resize, new Listener()
		{
			public void handleEvent(Event event)
			{
				if (renderer != null)
				{
					Command cmd = new Command(new Object[] { shell.getClientArea().width, shell.getClientArea().height }) 
					{
						public void execute(Object[] data2)
						{
							renderer.reshape((Integer)data[0], (Integer)data[1]);
						}
					};
					glMessageQueue.offer(cmd);
				}
			}
		});
		
		glCanvas.addKeyListener(new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e)
			{
				Command cmd = keyMap.getCommandByKeyCode(true, e.keyCode, holdableKeys[HoldableKeys.SHIFT.ordinal()],
						holdableKeys[HoldableKeys.CTRL.ordinal()], holdableKeys[HoldableKeys.ALT.ordinal()]);
				if (cmd != null)
					messageQueue.offer(cmd);
			}
			
			public void keyReleased(KeyEvent e)
			{
				super.keyReleased(e);
				
				Command cmd = keyMap.getCommandByKeyCode(false, e.keyCode, holdableKeys[HoldableKeys.SHIFT.ordinal()],
						holdableKeys[HoldableKeys.CTRL.ordinal()], holdableKeys[HoldableKeys.ALT.ordinal()]);
				if (cmd != null)
					messageQueue.offer(cmd);
			}
		});
		
		glCanvas.addMouseListener(new MouseAdapter() 
		{
			public void mouseDown(MouseEvent e)
			{
				Command cmd = keyMap.getCommandByKeyCode(true, e.button, holdableKeys[HoldableKeys.SHIFT.ordinal()],
						holdableKeys[HoldableKeys.CTRL.ordinal()], holdableKeys[HoldableKeys.ALT.ordinal()]);
				if (cmd != null)
					messageQueue.offer(cmd);
			}
			public void mouseUp(MouseEvent e)
			{
				Command cmd = keyMap.getCommandByKeyCode(false, e.button, holdableKeys[HoldableKeys.SHIFT.ordinal()],
						holdableKeys[HoldableKeys.CTRL.ordinal()], holdableKeys[HoldableKeys.ALT.ordinal()]);
				if (cmd != null)
					messageQueue.offer(cmd);
			}

		});

		glCanvas.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent e) {
				Command cmd;
				if (e.count > 0) {
					cmd = keyMap.getScrollCommand(true);
				}else{
					cmd = keyMap.getScrollCommand(false);
				}
				if (cmd != null)
					messageQueue.offer(cmd);
			}
		});
		
		glCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				Object[] data = new Object[]{e.x, e.y};
				/*Command cmd = new Command(new Object[] { e.x, e.y }) 
				{
					public void execute(Object[] data2)
					{*/
				mousePos = new Point((Integer) data[0], (Integer) data[1]);
					/*}
				};
				messageQueue.offer(cmd);*/
			}
		});
		
		this.sme = new SpringMapEdit(as.initialMapWidth, as.initialMapHeight, null, as); //spring units... 8x8 equals 512x512
		
		//Setup initial position
		as.cameraPosition.camX = as.initialMapWidth*64;//-50;
		as.cameraPosition.camY = sme.diag;
		as.cameraPosition.camZ = as.initialMapHeight*128;//-50;
		as.cameraPosition.camRotX = -70;
		as.cameraPosition.camRotY = 0;
		as.cameraPosition.camRotZ = 0;
		
		this.renderer = new MapRenderer(this.sme, as);

		glCanvas.setCurrent();
		glContext = GLDrawableFactory.getFactory().createExternalGLContext();
		glContext.makeCurrent();
		renderer.init(glContext.getGL());
		glCanvas.addListener(SWT.Resize, new Listener()
		{
			public void handleEvent(Event event)
			{
				Command cmd = new Command(null)
				{
					public void execute(Object[] data2)
					{
						renderer.reshape(glCanvas.getSize().x, glCanvas.getSize().y);
					}
				};
				glMessageQueue.offer(cmd);
			}
		});
		glContext.release();

		shell.setLocation(0, 0);
		shell.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
		shell.setMaximized(true);
		shell.open();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);
		worker = new Thread(new Runnable()
		{
			public void run()
			{
				while (!kill)
				{
					//Process render independent Message Queue (does not depend on GL)
					//Do work which relies on this thread as executor. (everything which modifies some state)
					synchronized (messageQueue)
					{
						Command cmd = messageQueue.poll();
						while (cmd != null)
						{
							cmd.execute(null);
							cmd = messageQueue.poll();
						}
						try {
							messageQueue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, "worker");
		worker.start();
		new SpringMapEditMenuBar(this);
		
		this.setupDialog = new SpringMapEditDialog(this, dialogpanel);
		
		//new Console(this);
	}
		
	public void toggleMouseLook()
	{
		as.mouseLook = !as.mouseLook;
		/*
		if (as.mouseLook)
		{
			shell.setCursor(cursorCross);
			try
			{
				new Robot().mouseMove(mousePosCenter.x, mousePosCenter.y);
			}
			catch (AWTException e) 
			{
			}
		}	
		else
		{
			shell.setCursor(cursorDefault);
		}*/
	}
	
	public void insertUndo(UndoRedo o)
	{
		if (undoQueue.size() > 50)
			undoQueue.pollLast();
		undoQueue.push(o);
	}
	
	public void undo()
	{
		if (undoQueue.peek() != null)
			undoQueue.peek().applyUndo();
	}
	
	public void redo()
	{
		if (undoQueue.peek() != null)
			undoQueue.peek().applyUndo();
	}
	
	private void deactivateMouseLook()
	{
		as.mouseLook = false;
		shell.setCursor(cursorDefault);
	}
	
	private void setMouseCenter()
	{
		int x = shell.getLocation().x;
		int y = shell.getLocation().y;
		int w = shell.getSize().x / 2;
		int h = shell.getSize().y / 2;
		mousePosCenter = new Point(w + x, h + y);
	}
	
	public void deactivateKeys()
	{
		for (int i = 0; i < KEYCOUNT; i++)
			this.holdableKeys[i] = false;
	}
	
	public void resetCamera()
	{
		as.cameraPosition.camX = sme.width * 64;//-50;
		as.cameraPosition.camY = sme.diag;
		as.cameraPosition.camZ = sme.height * 128;//-50;
		as.cameraPosition.camRotX = -70;
		as.cameraPosition.camRotY = 0;
		as.cameraPosition.camRotZ = 0;
	}

	public void zoom(boolean zoomIn){
		float curMoveSpeed = (holdableKeys[HoldableKeys.SHIFT.ordinal()] ? as.fastSpeed : as.normalSpeed);
		curMoveSpeed = (holdableKeys[HoldableKeys.CTRL.ordinal()] ? as.slowSpeed : curMoveSpeed);

		double upDownAmount = Math.cos(as.cameraPosition.camRotX * Math.PI / 180);

		if (zoomIn){
			if (as.cameraPosition.camY + (curMoveSpeed * 10 * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180)) >= 0)
				as.cameraPosition.camY += curMoveSpeed * 10 * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180);
			as.cameraPosition.camX = as.cameraPosition.camX + (float)(Math.abs(upDownAmount) * curMoveSpeed * 10 * Math.sin(-as.cameraPosition.camRotY * Math.PI / 180));
			as.cameraPosition.camZ = as.cameraPosition.camZ - (float)(Math.abs(upDownAmount) * curMoveSpeed * 10 * Math.cos(-as.cameraPosition.camRotY * Math.PI / 180));
			renderer.camPosChangedNotify();
		}else{
			if (as.cameraPosition.camY - (curMoveSpeed * 10 * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180)) >= 0)
				as.cameraPosition.camY -= curMoveSpeed * 10 * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180);
			as.cameraPosition.camX = as.cameraPosition.camX - (float)(Math.abs(upDownAmount) * curMoveSpeed * 10 * Math.sin(-as.cameraPosition.camRotY * Math.PI / 180));
			as.cameraPosition.camZ = as.cameraPosition.camZ + (float)(Math.abs(upDownAmount) * curMoveSpeed * 10 * Math.cos(-as.cameraPosition.camRotY * Math.PI / 180));
			renderer.camPosChangedNotify();
		}
	}
	
	public void run() throws InterruptedException
	{
		//Show Windows
		if (!as.batchMode)
		{
			shell.setVisible(true);
			setupDialog.open();
		}
		
		//Get Focus into this window
		shell.forceActive();
		glCanvas.forceFocus();
		
		final FPSMeter fps = new FPSMeter();
		int ticksSinceLastRender = 0;
		boolean makeTickThisLoop = false;
		final int worldFPS = 60; //set wanted fps to 60 FPS
		final long animationTimeStep = 1000000000L / worldFPS;
		long lastAnimation = System.nanoTime();
		long sleepTime;
		final long minSleepTime = 16000000L; //set to minimum Thread.sleep granularity
		while (!shell.isDisposed())
		{
			/* Make world animation/step every 1/50 second */
			makeTickThisLoop = (System.nanoTime() - lastAnimation) > animationTimeStep;
			if (makeTickThisLoop) 
			{
				lastAnimation = lastAnimation + animationTimeStep;
				
				//Animation
				as.time = as.time + 0.002f;
				as.gameFrame++;
				
				float curMoveSpeed = (holdableKeys[HoldableKeys.SHIFT.ordinal()] ? as.fastSpeed : as.normalSpeed);
				curMoveSpeed = (holdableKeys[HoldableKeys.CTRL.ordinal()] ? as.slowSpeed : curMoveSpeed);
				
				/*as.fps = fps.getFps();
				shell.setText("FPS: " + as.fps + "  Tris rendered: " + as.trisRendered + " viewing: " + as.mapMode.toString());*/
	
				// Handle Camera
				if (holdableKeys[HoldableKeys.ARROW_LEFT.ordinal()])
				{
					as.cameraPosition.camX -= curMoveSpeed * (float)Math.cos(-as.cameraPosition.camRotY * Math.PI / 180);
					as.cameraPosition.camZ -= curMoveSpeed * (float)Math.sin(-as.cameraPosition.camRotY * Math.PI / 180);
					renderer.camPosChangedNotify();
				}
				if (holdableKeys[HoldableKeys.ARROW_RIGHT.ordinal()])
				{
					as.cameraPosition.camX += curMoveSpeed * (float)Math.cos(-as.cameraPosition.camRotY * Math.PI / 180);
					as.cameraPosition.camZ += curMoveSpeed * (float)Math.sin(-as.cameraPosition.camRotY * Math.PI / 180);
					renderer.camPosChangedNotify();
				}
				if (holdableKeys[HoldableKeys.ARROW_UP.ordinal()])
				{
					double upDownAmount = 1;

					as.cameraPosition.camX = as.cameraPosition.camX + (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.sin(-as.cameraPosition.camRotY * Math.PI / 180));
					as.cameraPosition.camZ = as.cameraPosition.camZ - (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.cos(-as.cameraPosition.camRotY * Math.PI / 180));
					renderer.camPosChangedNotify();
				}
				if (holdableKeys[HoldableKeys.ARROW_DOWN.ordinal()])
				{
					double upDownAmount = 1;

					as.cameraPosition.camX = as.cameraPosition.camX - (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.sin(-as.cameraPosition.camRotY * Math.PI / 180));
					as.cameraPosition.camZ = as.cameraPosition.camZ + (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.cos(-as.cameraPosition.camRotY * Math.PI / 180));
					renderer.camPosChangedNotify();
				}
				if (holdableKeys[HoldableKeys.PAGE_UP.ordinal()])
				{
					// zoom in
					double upDownAmount = Math.cos(as.cameraPosition.camRotX * Math.PI / 180);
					if (as.cameraPosition.camY + (curMoveSpeed * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180)) >= 0)
						as.cameraPosition.camY += curMoveSpeed * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180);
					as.cameraPosition.camX = as.cameraPosition.camX + (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.sin(-as.cameraPosition.camRotY * Math.PI / 180));
					as.cameraPosition.camZ = as.cameraPosition.camZ - (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.cos(-as.cameraPosition.camRotY * Math.PI / 180));
					renderer.camPosChangedNotify();
				}
				if (holdableKeys[HoldableKeys.PAGE_DOWN.ordinal()])
				{
					// zoom out
					double upDownAmount = Math.cos(as.cameraPosition.camRotX * Math.PI / 180);
					if (as.cameraPosition.camY - (curMoveSpeed * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180)) >= 0)
						as.cameraPosition.camY -= curMoveSpeed * (float)Math.sin(as.cameraPosition.camRotX * Math.PI / 180);
					as.cameraPosition.camX = as.cameraPosition.camX - (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.sin(-as.cameraPosition.camRotY * Math.PI / 180));
					as.cameraPosition.camZ = as.cameraPosition.camZ + (float)(Math.abs(upDownAmount) * curMoveSpeed * Math.cos(-as.cameraPosition.camRotY * Math.PI / 180));
					renderer.camPosChangedNotify();
				}
				if (holdableKeys[HoldableKeys.MOUSE_2.ordinal()] || as.mouseLook) // for mouse look
				{
					Point mousePosScreen = MouseInfo.getPointerInfo().getLocation();
					
					int dxi = mousePosCenter.x - mousePosScreen.x;
					int dyi = mousePosCenter.y - mousePosScreen.y;
					if (as.invertY)
						dyi = -dyi;
					
					if (!((dxi == 0) && (dyi == 0)))
					{
						float dx = dxi;
						float dy = dyi;
						as.cameraPosition.camRotX = as.cameraPosition.camRotX + ((dy / 20) * as.sensitivity);
						as.cameraPosition.camRotY = as.cameraPosition.camRotY + ((dx / 20) * as.sensitivity);
						if (as.cameraPosition.camRotX > 360)
							as.cameraPosition.camRotX = as.cameraPosition.camRotX - 360;
						else if (as.cameraPosition.camRotX < 0)
							as.cameraPosition.camRotX = 360 + as.cameraPosition.camRotX;
						if (as.cameraPosition.camRotY > 360)
							as.cameraPosition.camRotY = as.cameraPosition.camRotY - 360;
						else if (as.cameraPosition.camRotY < 0)
							as.cameraPosition.camRotY = 360 + as.cameraPosition.camRotY;
						try
						{
							new Robot().mouseMove(mousePosCenter.x, mousePosCenter.y);
						}
						catch (AWTException e) 
						{
						}
						
						//Select the tile we are looking at
						//Create view vector, by rotating default (0,0,1) the same way as we do in opengl
						Vector3 viewVector = Vector3Math.rotateZ(Vector3Math.rotateY(Vector3Math.rotateX(new Vector3(0, 0, 1),
								as.cameraPosition.camRotX * Math.PI / 180), as.cameraPosition.camRotY * Math.PI / 180),
								as.cameraPosition.camRotZ * Math.PI / 180);
						Vector3 intersectPoint = Vector3Math.planeIntersectPoint(new Vector3(0, (sme.map.maxHeight / 8) - (sme.diag - as.cameraPosition.camY), 0),
								new Vector3(0, 1, 0),
								new Vector3(as.cameraPosition.camX, as.cameraPosition.camY, as.cameraPosition.camZ), viewVector);
						if (intersectPoint != null)
						{
							//System.out.println("X:" + intersectPoint.x() + "  Y: " + intersectPoint.y() +"  Z: " + intersectPoint.z());
							sme.mes.setBrushPos((int)(intersectPoint.x() / as.quadSize) - (sme.mes.activeBrush.width / 2) - 1,
												(int)(intersectPoint.z() / as.quadSize) - (sme.mes.activeBrush.getHeight() / 2) - 1);
						}
						
						renderer.camViewChangedNotify();
					}
				}
				else
				{
					if (!mousePos.equals(mousePosOld))
					{
						//Modify basic viewVector
						float modRotX = -((((float)mousePos.y / as.displayHeight) - 0.5f) * (as.fov / (as.displayWidth / as.displayHeight)));
						float modRotY = -((((float)mousePos.x / as.displayWidth) - 0.5f) * as.fov);
						Vector3 mouseVector = new Vector3(0, 0, 1);
						mouseVector = Vector3Math.rotateY(Vector3Math.rotateX(mouseVector, modRotX * Math.PI / 180), modRotY * Math.PI / 180);
						//Create view vector, by rotating default (0,0,1) the same way as we do in opengl
						Vector3 viewVector = Vector3Math.rotateZ(Vector3Math.rotateY(Vector3Math.rotateX(mouseVector,
								as.cameraPosition.camRotX * Math.PI / 180), as.cameraPosition.camRotY * Math.PI / 180),
								as.cameraPosition.camRotZ * Math.PI / 180);
						Vector3 intersectPoint = Vector3Math.planeIntersectPoint(new Vector3(0, sme.map.maxHeight / 2, 0),
								new Vector3(0, 1, 0),
								new Vector3(as.cameraPosition.camX, as.cameraPosition.camY, as.cameraPosition.camZ),
								viewVector);
						if (intersectPoint != null)
						{
							//System.out.println("X:" + intersectPoint.x() + "  Y: " + intersectPoint.y() +"  Z: " + intersectPoint.z());
							sme.mes.setBrushPos((int)(intersectPoint.x() / as.quadSize) - (sme.mes.activeBrush.width / 2) - 1,
												(int)(intersectPoint.z() / as.quadSize) - (sme.mes.activeBrush.getHeight() / 2) - 1);
						}
						mousePosOld = mousePos;
						renderer.camViewChangedNotify();
					}
				}
				//Handle edit
				if (holdableKeys[HoldableKeys.MOUSE_1.ordinal()])
				{
					switch (sme.mes.getBrushMode())
					{
					case Height:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getHeightBrush().applyBrush(sme.mes.brushPos, false);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getHeightBrush(), true, false, false);
							}
						});
						if (false)
							insertUndo(new UndoRedo(sme.mes.brushPos, sme.mes.getHeightBrush().getHeight(), sme.mes.getHeightBrush().getWidth(), sme.map.heightmap.getHeightMap(), sme));
						if (sme.mes.getHeightBrush().brushMode == 1) { // Height stamp mode should not continue placing after click
							holdableKeys[HoldableKeys.MOUSE_1.ordinal()] = false;
						}
						break;
					case Texture:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getTextureBrush().applyBrush(sme.mes.brushPos, sme.mes.getTextureBrush(), false);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getTextureBrush(), false, true, false);
							}
						});
						if (sme.mes.getTextureBrush().brushMode == 3) {
							holdableKeys[HoldableKeys.MOUSE_1.ordinal()] = false;
						}
						break;
					case Metal:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getMetalBrush().applyBrush(sme.mes.brushPos, false);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getMetalBrush(), false, true, false);
							}
						});
						break;
					case Type:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.map.setToTypemap(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getTypeBrush(), false);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getTypeBrush(), false, true, false);
							}
						});
						break;
					case Vegetation:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.map.setToVegetationmap(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getVegetationBrush(), false);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getVegetationBrush(), false, true, false);
							}
						});
						break;
					case Feature:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getFeatureBrush().applyBrush(sme.mes.brushPos, sme.mes.getFeatureBrush(), false);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getFeatureBrush(), false, false, true);
							}
						});
						//holdableKeys[HoldableKeys.MOUSE_1.ordinal()] = false;
						break;	
					case Prefab:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getPrefabBrush().applybrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getPrefabBrush(), false);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getPrefabBrush(), true, true, false);
							}
						});
						holdableKeys[HoldableKeys.MOUSE_1.ordinal()] = false;
						break;
					case Copypaste:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getCopypasteBrush().paste(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getPrefabBrush());
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getCopypasteBrush(), true, true, false);
							}
						});
						holdableKeys[HoldableKeys.MOUSE_1.ordinal()] = false;
						break;
					}
				}
				if (holdableKeys[HoldableKeys.MOUSE_3.ordinal()])
				{
					switch (sme.mes.getBrushMode())
					{
					case Height:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getHeightBrush().applyBrush(sme.mes.brushPos, true);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getHeightBrush(), true, false, false);
							}
						});
						if (sme.mes.getHeightBrush().brushMode == 1) // Height stamp mode should not continue placing after click
							holdableKeys[HoldableKeys.MOUSE_3.ordinal()] = false;
						break;
					case Texture:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getTextureBrush().applyBrush(sme.mes.brushPos, sme.mes.getTextureBrush(), true);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getTextureBrush(), false, true, false);
							}
						});
						if (sme.mes.getTextureBrush().brushMode == 3)//sme.mes.getTextureBrush().TexturetMode.Stamp.ordinal()) 
							holdableKeys[HoldableKeys.MOUSE_1.ordinal()] = false;
						break;
					case Metal:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getMetalBrush().applyBrush(sme.mes.brushPos, true);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getMetalBrush(), false, true, false);
							}
						});
						break;
					case Type:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.map.setToTypemap(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getTypeBrush(), true);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getTypeBrush(), false, true, false);
							}
						});
						break;
					case Vegetation:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.map.setToVegetationmap(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getVegetationBrush(), true);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getVegetationBrush(), false, true, false);
							}
						});
						break;
					case Feature:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getFeatureBrush().applyBrush(sme.mes.brushPos, sme.mes.getFeatureBrush(), true);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getFeatureBrush(), false, false, true);
							}
						});
						break;
					case Prefab:
						messageQueue.offer(new Command(null)
						{	
							public void execute(Object[] data2)
							{
								sme.mes.getPrefabBrush().applybrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getPrefabBrush(), true);
								renderer.invalidateBlocksByBrush(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getPrefabBrush(), true, true, false);
							}
						});
						holdableKeys[HoldableKeys.MOUSE_3.ordinal()] = false;
						break;
					case Copypaste:
						messageQueue.offer(new Command(null) {
							public void execute(Object[] data2)
							{
								sme.mes.getCopypasteBrush().copy(sme.mes.brushPos.x(), sme.mes.brushPos.y(), sme.mes.getPrefabBrush());
							}
						});
						holdableKeys[HoldableKeys.MOUSE_3.ordinal()] = false;
						break;
					}
				}
				synchronized (messageQueue)
				{
					if (messageQueue.peek() != null)
						messageQueue.notify();
				}
				ticksSinceLastRender++;
			}
			//Ensure we render at least once every 100 world ticks.
			if ((!makeTickThisLoop) || (ticksSinceLastRender > 100))
			{
				if (!as.vsync || (ticksSinceLastRender > 0))
				{
					//Activate glContext 
					glCanvas.setCurrent();
					glContext.makeCurrent();
					
					//Render
					renderer.display(glContext.getGL());
					glCanvas.swapBuffers();

					//Animate some GUI components
					if (as.animateGUI && (ticksSinceLastRender > 0))
						setupDialog.animate();
					
					//Process GLMessage Queue within glContext
					//Do work which relies on this thread as executor. (everything which modifies some state or uses the GL is)
					{
						Command cmd = glMessageQueue.poll();
						while (cmd != null)
						{
							cmd.execute(null);
							cmd = glMessageQueue.poll();
						}
					}
					
					glContext.release();

					ticksSinceLastRender = 0;
					fps.tick();
				}
				if (as.vsync)
				{
					/* Here we calculate how long we need to sleep.
					 * Only do the sleep, if the sleeptime is higher than our minimum.
					 * This minimum usually is around 1-16ms
					 * Note the minimum should not be higher than 1 / worldFPS,
					 * which is currently 33. 1/33 = 30ms
					 * 
					 * This means if minSleepTime is higher than 30ms, the System is never going to sleep.
					 * This also means if (worldtick + rendering) takes more than 30-minSleep = 14ms, we never sleep.
					 * 14ms equals a theoretical fps of 1/0.014 = 72 !!!
					 * Which is relatively high.
					 * So only on highspec systems there will be some cpu preservation.
					 * (I guess most slower systems will be cpu bound anyway)
					 */
					sleepTime = animationTimeStep - (System.nanoTime() - lastAnimation);
					if (sleepTime > minSleepTime)
						LockSupport.parkNanos(sleepTime);
				}	
			}
			//Process GUI
			display.readAndDispatch();
		}
		//MAIN LOOP END
		
		//Save settings
		keyMap.saveToFile(new File("config/keys.cfg"));
		as.saveToFile(new File("config/settings.cfg"));
		
		//Dispose Display
		display.dispose();
	}
	
}
