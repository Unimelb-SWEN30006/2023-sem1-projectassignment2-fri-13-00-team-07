import checker.GameChecker;
import checker.LevelChecker;
import game.Game;
import game.Maps.EditorMap;
import game.Maps.PacManMap;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TorusVerseApp {
    // default EDIT mode
    private AppMode mode = AppMode.EDIT;

    public TorusVerseApp(String dir) throws IOException, JDOMException { // given folder or file
        EditorAdapter editorAdapter = AppComponentFactory.getInstance().getEditorAdapter();
        if (dir == null) { // edit mode with no current map
            editorAdapter.runEditor(null);
            return;
        }

        File file = new File(dir);
        if (file.isDirectory()) {
            mode = AppMode.TEST;
            GameChecker gameChecker = GameChecker.getInstance();
            if (gameChecker.checkGame(dir)) {
                ArrayList<String> validFiles = gameChecker.getValidMapFiles();
                Collections.sort(validFiles);

                ArrayList<PacManMap> maps = new ArrayList<>();
                for (String f : validFiles) {
                    EditorMap map = new EditorMap((dir + "/" + f));

                    if (!LevelChecker.getInstance().checkLevel(map)) { // can always cast, as it is a xml
                        mode = AppMode.EDIT;
                        editorAdapter.runEditor(dir + "/" + f);
                        return;
                    }
                    maps.add(map);
                }

                Game game = new Game(maps);
            } else {
                throw new IOException("Game check failed");
            }
        } else {
            mode = AppMode.EDIT;
            editorAdapter.runEditor(dir);
        }
    }

    private boolean isMap(File f) {
        return Character.isDigit(f.getName().charAt(0)) && f.getName().endsWith(".xml");
    }
}
