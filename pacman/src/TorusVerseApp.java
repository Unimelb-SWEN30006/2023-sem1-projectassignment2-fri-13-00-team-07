import game.Game;
import game.Maps.EditorMap;
import game.Maps.PacManMap;

import java.io.File;
import java.util.ArrayList;

public class TorusVerseApp {
    // default EDIT mode
    private AppMode mode = AppMode.EDIT;
    private Game game;
    private EditorAdapter editorAdapter;

    public TorusVerseApp(String dir) { // given folder or file
        editorAdapter = AppComponentFactory.getInstance().getEditorAdapter();
        if (dir == null) { // edit mode with no current map
            editorAdapter.runEditor(null);
            return;
        }

        File file = new File(dir);
        if (file.isDirectory()) {
            mode = AppMode.TEST;
            game = new Game(getMaps(dir));
        } else {
            // returning to edit mode with no current map
            mode = AppMode.EDIT;
            editorAdapter.runEditor(dir);
        }
    }

    private ArrayList<PacManMap> getMaps(String dir) {
        File file = new File(dir);
        File[] allFiles = file.listFiles();
        ArrayList<PacManMap> maps = new ArrayList<>();
        for (File f : allFiles) {
            maps.add(getMap(f.getName()));
        }
        return maps;
    }

    /**
     * Gets the map from the given map file name.
     * @param mapFile
     * @return
     */
    private PacManMap getMap(String mapFile) {
        return new EditorMap(editorAdapter.getMap(mapFile));
    }

    private boolean isMap(File f) {
        return Character.isDigit(f.getName().charAt(0)) && f.getName().endsWith(".xml");
    }
}
