package mapeditor.editor;

import checker.LevelChecker;
import game.Game;
import game.Maps.EditorMap;
import mapeditor.grid.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Controller of the application.
 * 
 * @author Daniel "MaTachi" Jonsson
 * @version 1
 * @since v0.0.5
 * 
 */
public class Controller implements ActionListener, GUIInformation {

	/**
	 * The model of the map editor.
	 */
	private Grid model;

	private Tile selectedTile;
	private Camera camera;

	private List<Tile> tiles;

	private GridView grid;
	private View view;

	private int gridWith = Constants.MAP_WIDTH;
	private int gridHeight = Constants.MAP_HEIGHT;

	private HashMap<Character, String> CHAR_TO_STR_DICT = new HashMap<>();
	private HashMap<String, Character> STR_TO_CHAR_DICT = new HashMap<>();
	private static final char[] TILE_CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l'}; // 'a' is default
	private static final String[] TILE_TYPES = {"PathTile", "WallTile", "PillTile",
												"GoldTile", "IceTile", "PacTile",
												"TrollTile", "TX5Tile", "PortalWhiteTile",
												"PortalYellowTile", "PortalDarkGoldTile",
												"PortalDarkGrayTile"
												};
	private String dataDir = "pacman/sprites/editor data/"; // default
	/**
	 * Construct the controller.
	 */
	public Controller() {
		setUpDicts();
	}

	public void run() {
		init(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
	}

	private void setUpDicts() {
		for (int i = 0; i < TILE_CHARS.length; i++) {
			CHAR_TO_STR_DICT.put(TILE_CHARS[i], TILE_TYPES[i]);
			STR_TO_CHAR_DICT.put(TILE_TYPES[i], TILE_CHARS[i]);
		}
	}

	public void init(int width, int height) {
		this.tiles = TileManager.getTilesFromFolder(dataDir);
		this.model = new GridModel(width, height, tiles.get(0).getCharacter());
		this.camera = new GridCamera(model, Constants.GRID_WIDTH,
				Constants.GRID_HEIGHT);

		grid = new GridView(this, camera, tiles); // Every tile is
													// 30x30 pixels

		this.view = new View(this, camera, grid, tiles);
	}

	/**
	 * Different commands that comes from the view.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		for (Tile t : tiles) {
			if (e.getActionCommand().equals(
					Character.toString(t.getCharacter()))) {
				selectedTile = t;
				break;
			}
		}
		if (e.getActionCommand().equals("flipGrid")) {
			// view.flipGrid();
		} else if (e.getActionCommand().equals("save")) {
			// LevelChecker.getInstance().check();
			saveFile();
		} else if (e.getActionCommand().equals("load")) {
			// LevelChecker.getInstance().check();
			loadFile(null);
		} else if (e.getActionCommand().equals("update")) {
			updateGrid(gridWith, gridHeight);
		} else if (e.getActionCommand().equals("start_game")) {
			// Code to switch to pacman game
			EditorMap map = new EditorMap(model.getMap());
			if (LevelChecker.getInstance().checkLevel(map)) {
				Game game = new Game(map);
				game.run();
			} else {
				JOptionPane.showMessageDialog(null, "The map check failed", "Cannot run", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	public void updateGrid(int width, int height) {
		view.close();
		init(width, height);
		view.setSize(width, height);
	}

	DocumentListener updateSizeFields = new DocumentListener() {

		public void changedUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}

		public void removeUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}

		public void insertUpdate(DocumentEvent e) {
			gridWith = view.getWidth();
			gridHeight = view.getHeight();
		}
	};

	private void saveFile() {

		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"xml files", "xml");
		chooser.setFileFilter(filter);
		File workingDirectory = new File(System.getProperty("user.dir"));
		chooser.setCurrentDirectory(workingDirectory);

		int returnVal = chooser.showSaveDialog(null);
		try {
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				Element level = new Element("level");
				Document doc = new Document(level);
				doc.setRootElement(level);

				Element size = new Element("size");
				int height = model.getHeight();
				int width = model.getWidth();
				size.addContent(new Element("width").setText(width + ""));
				size.addContent(new Element("height").setText(height + ""));
				doc.getRootElement().addContent(size);

				for (int y = 0; y < height; y++) {
					Element row = new Element("row");
					for (int x = 0; x < width; x++) {
						char tileChar = model.getTile(x,y);
						String type = CHAR_TO_STR_DICT.getOrDefault(tileChar, "PathTile");
						Element e = new Element("cell");
						row.addContent(e.setText(type));
					}
					doc.getRootElement().addContent(row);
				}
				XMLOutputter xmlOutput = new XMLOutputter();
				xmlOutput.setFormat(Format.getPrettyFormat());
				xmlOutput
						.output(doc, new FileWriter(chooser.getSelectedFile()));
			}
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Invalid file!", "error",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
		}
	}

	/**
	 * Load the map at path of `currentMap`, or ask user to select one if the argument is null;
	 */
	public void loadFile(String currentMap) {
		SAXBuilder builder = new SAXBuilder();
		try {
			File selectedFile;
			Document document;
			if (currentMap == null) {
				JFileChooser chooser = new JFileChooser();
				File workingDirectory = new File(System.getProperty("user.dir"));
				chooser.setCurrentDirectory(workingDirectory);

				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					selectedFile = chooser.getSelectedFile();
				} else {
					return; // fixme: maybe throw some error?
				}
			} else {
				selectedFile = new File(currentMap);
			}

			if (selectedFile.canRead() && selectedFile.exists()) {
				document = (Document) builder.build(selectedFile);

				Element rootNode = document.getRootElement();

				List sizeList = rootNode.getChildren("size");
				Element sizeElem = (Element) sizeList.get(0);
				int height = Integer.parseInt(sizeElem
						.getChildText("height"));
				int width = Integer
						.parseInt(sizeElem.getChildText("width"));
				if (grid != null) {
					updateGrid(width, height);
				}

				List rows = rootNode.getChildren("row");

				if (model == null) {
					this.model = new GridModel(((Element) rows.get(0)).getChildren("cell").size(), rows.size(), 'a');
				}

 				for (int y = 0; y < rows.size(); y++) {
					Element cellsElem = (Element) rows.get(y);
					List cells = cellsElem.getChildren("cell");

					for (int x = 0; x < cells.size(); x++) {
						Element cell = (Element) cells.get(x);
						String cellValue = cell.getText();

						char tileNr = STR_TO_CHAR_DICT.getOrDefault(cellValue, 'a');
						model.setTile(x, y, tileNr);
					}
				}

				if (grid != null) {
					grid.redrawGrid();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tile getSelectedTile() {
		return selectedTile;
	}

	public char[][] getMap() {
		return model.getMap();
	}

	public HashMap<Character, String> getCharToStrDict() {
		return CHAR_TO_STR_DICT;
	}
}
