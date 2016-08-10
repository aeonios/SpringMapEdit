package backend;

import java.util.ArrayList;

import backend.map.Heightmap;
import backend.math.Vector2Int;

public class UndoRedo {
	//private ArrayList<Integer>[] undoRedoQueue = new ArrayList[0];
	private Vector2Int mapStart;
	private int mapType = 0;
	private float[][] oldMap;
	private SpringMapEdit sme;
	
	public UndoRedo (Vector2Int start, int h, int w, float[][] map, SpringMapEdit sme)
	{
		this.sme = sme;
		mapStart = start;
		oldMap = new float[h][w];
		for (int y = 0; y < h; h++)
			System.arraycopy(map[y], start.x(), oldMap[y], 0, w);
	}
	
	public void applyUndo()
	{
		int h = oldMap.length;
		int w = oldMap[0].length;
		Heightmap map = sme.map.heightmap;
		/*for (int y = 0; y < h; h++)
			System.arraycopy(oldMap., 0, map[y], w, w);*/
	}
}
