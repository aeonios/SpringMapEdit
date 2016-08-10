package frontend.gui;

import java.awt.Toolkit;

import java.io.OutputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;

import backend.SpringMapEdit;
import frontend.render.MapRenderer;
import frontend.render.AppSettings;

public class Console {
	public Shell shell;
	public Display display;
	
	private SpringMapEditGUI smeGUI;
	private SpringMapEdit sme;
	private AppSettings rs;
	private MapRenderer renderer;
	
	public Console(SpringMapEditGUI mainGUI)
	{
		shell = mainGUI.shell;
		
		this.smeGUI = mainGUI;
		this.sme = smeGUI.sme;
		this.rs = smeGUI.as;
		this.renderer = smeGUI.renderer;
		
		display = smeGUI.display;

		final Text text = new Text(shell, SWT.BORDER);
		text.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, 24);
	}
	
	public void open() {
		shell.setVisible(true);
	}
}
