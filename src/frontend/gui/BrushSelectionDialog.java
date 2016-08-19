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
 * BrushSelectionDialog.java 
 * Created on 06.10.2008
 * by Heiko Schmitt
 */
        package frontend.gui;

        import org.eclipse.swt.SWT;

        import org.eclipse.swt.custom.ScrolledComposite;
        import org.eclipse.swt.events.ControlAdapter;
        import org.eclipse.swt.events.ControlEvent;
        import org.eclipse.swt.graphics.Rectangle;
        import org.eclipse.swt.layout.FillLayout;
        import org.eclipse.swt.layout.GridData;
        import org.eclipse.swt.layout.GridLayout;
        import org.eclipse.swt.layout.RowLayout;
        import org.eclipse.swt.widgets.Composite;
        import org.eclipse.swt.widgets.Group;
        import org.eclipse.swt.widgets.ScrollBar;

        import frontend.render.brushes.BrushPattern;

/**
 * @author Heiko Schmitt
 *
 */
public class BrushSelectionDialog
{
    private SpringMapEditGUI smeGUI;
    private SpringMapEditDialog smed;
    private Group widget;
    private Composite pane;

    /**
     *
     */
    public BrushSelectionDialog(SpringMapEditGUI smeGUI, SpringMapEditDialog smed, Composite parent)
    {
        this.smeGUI = smeGUI;
        this.smed = smed;
        createDialogArea(parent);
    }

    private void createDialogArea(Composite parent) {
        //Add scrollcomposite
        widget = new Group(parent, SWT.SHADOW_NONE);
        widget.setLayout(new FillLayout());
        widget.setText("Select Brush Pattern:");
        final ScrolledComposite sc = new ScrolledComposite(widget, SWT.V_SCROLL);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.setLayout(new GridLayout(1, false));

        //Add contentpane for scrollcomposite
        pane = new Composite(sc, SWT.NONE);
        RowLayout rl = new RowLayout(SWT.HORIZONTAL);
        rl.wrap = true;
        pane.setLayout(rl);
        pane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        //Add Buttons
        Command selectCommand = new Command(null)
        {
            public void execute(Object[] data2)
            {
                smeGUI.sme.mes.activeBrush.setPattern((Integer) data2[0]);
            }
        };
        ImageSelectButton fsc;
        Command cmd;
        int count = smeGUI.sme.mes.brushPatternManager.getBrushDataCount();
        for (int i = 0; i < count; i++)
        {
            fsc = new ImageSelectButton(pane, i, smeGUI.sme.mes.brushPatternManager.getBrushDataName(i), smeGUI.sme.mes.defaultSize, smeGUI.sme.mes.defaultSize, (smeGUI.sme.mes.activeBrush.getPattern().patternID == i), selectCommand, null);
            fsc.forceFocus();

            //Fetch ImageData (execute loading in another thread. not really needed, but looks better ;) )
            cmd = new Command(new Object[] { fsc })
            {
                public void execute(Object[] data2)
                {
                    //Set ImageData
                    ImageSelectButton fsc = (ImageSelectButton) data[0];
                    int width = fsc.getWidth();
                    int height = fsc.getHeight();
                    byte[] psBuffer = new byte[width * height * 3];

                    //Copy pattern to image
                    BrushPattern bp = smeGUI.sme.mes.brushPatternManager.getScaledBrushData(fsc.getObjectID(), width, height, false);
                    float[][] p = bp.getPattern();
                    int x, y;
                    int scanlineWidth = width * 3;
                    byte value;
                    for (y = 0; y < height; y++)
                    {
                        for (x = 0; x < width; x++)
                        {
                            value = (byte)(p[x][y] * 0xFF);
                            psBuffer[(x*3) + 0 + (y * scanlineWidth)] = value;
                            psBuffer[(x*3) + 1 + (y * scanlineWidth)] = value;
                            psBuffer[(x*3) + 2 + (y * scanlineWidth)] = value;
                        }
                    }
                    fsc.setImageData(psBuffer);
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
                if (sb != null) sb.setIncrement(smeGUI.sme.mes.defaultSize / 8);
            }
        });
    }

    public Group getWidget(){
        return widget;
    }
}