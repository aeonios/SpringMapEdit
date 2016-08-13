package frontend.commands;

import frontend.gui.SpringMapEditGUI;

/**
 * Created by haplo on 8/11/2016.
 */
public class Reset_Camera extends SpringMapEditGUICommand {
    public Reset_Camera(SpringMapEditGUI smeGUI)
    {
        super(smeGUI);
    }
    @Override
    public void execute(Object[] data2)
    {
        smeGUI.resetCamera();
    }
}
