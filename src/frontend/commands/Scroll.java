package frontend.commands;

import frontend.gui.SpringMapEditGUI;

/**
 * Created by haplo on 8/12/2016.
 */
public class Scroll extends SpringMapEditGUICommand {
    private boolean up;
    public Scroll(SpringMapEditGUI smeGUI, boolean up){
        super(smeGUI);
        this.up = up;
    }

    @Override
    public void execute(Object[] data2)
    {
        smeGUI.zoom(up);
    }
}
