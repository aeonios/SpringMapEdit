package backend;

import java.io.File;
import frontend.render.MapRenderer;

public class SaverThread {
	SaverThread(File file, MapRenderer renderer, int format)
	{
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{

			}
		});
		t.start();
	}
}
