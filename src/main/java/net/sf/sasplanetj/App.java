package net.sf.sasplanetj;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;

import net.sf.sasplanetj.gps.LatLng;
import net.sf.sasplanetj.gps.SerialReader;
import net.sf.sasplanetj.ui.ShowMessage;
import net.sf.sasplanetj.util.Cache;
import net.sf.sasplanetj.util.Config;
import net.sf.sasplanetj.util.TilesUtil;
import net.sf.sasplanetj.util.TrackLogger;
import net.sf.sasplanetj.util.Tracks;
import net.sf.sasplanetj.util.Waypoint;
import net.sf.sasplanetj.util.Waypoints;
import net.sf.sasplanetj.util.Wikimapia;
import net.sf.sasplanetj.util.Zip;

public class App extends Frame implements ActionListener, ItemListener {

	private static final long serialVersionUID = -5248004842918375714L;

	public static Main main;
	private static App self;
	private String args[];

	public static SerialReader serialReader;
	private static final TrackLogger trackLogger = new TrackLogger();

	private static final String EXIT_COMMAND = "EXIT_COMMAND";
	private static final String NMEALOG_COMMAND = "NMEALOG_COMMAND";
	private static final String GPSLOG_COMMAND = "GPSLOG_COMMAND";
	private static final String MAIN_COMMAND = "MAIN_COMMAND";
	private static final String ZOOMOUT_COMMAND = "ZOOMOUT_COMMAND";
	private static final String ZOOMIN_COMMAND = "ZOOMIN_COMMAND";
	private static final String ZOOMTO_COMMAND = "ZOOMTO_COMMAND";
	private static final String ZOOMONLYTO_COMMAND = "ZOOMONLYTO_COMMAND";
	private static final String CENTER_COMMAND = "CENTER_COMMAND";
	private static final String DRAWGRID_COMMAND = "DRAWGRID_COMMAND";
	private static final String DRAWLATLNG_COMMAND = "DRAWLATLNG_COMMAND";
	private static final String DRAWTAIL_COMMAND = "DRAWTAIL_COMMAND";
	private static final String GOTO_COMMAND = "GOTO_COMMAND";

	private static MenuBar menuBar;
	private Menu mapsMenu;
	private MenuItem cmiMapView;
	private MenuItem cmiCoords;
	private MenuItem cmiNmea;
	private MenuItem cmiConnectGPS;
	private MenuItem cmiTrackLog;
	private MenuItem cmiWikimapia;
	private MenuItem cmiDrawGrid;
	private MenuItem cmiDrawLatLng;
	private MenuItem cmiDrawTail;
	private final MenuItem chkMenuZoomOnlyTo[] = new MenuItem[TilesUtil.ZOOM_MAX];
	private final MenuItem chkMenuZoomTo[] = new MenuItem[TilesUtil.ZOOM_MAX];
	private static final MenuItem cmiMaps[] = new MenuItem[Config.maps.length];

	private MenuItem miZoomOut;
	private MenuItem miZoomIn;
	private Menu menuZoomOnlyTo;

	private Menu menuZoomToHigh;
	private Menu menuZoomToLow;
	private MenuItem miCenter;

	private Component currentView;

	private static Class miCheckboxClass; // null if missing
	private static Method cmiSetStateMethod; // null if missing

	static {
		try {
			miCheckboxClass = Class.forName("java.awt.CheckboxMenuItem");
			// Check whether the class is present in JVM class library
			if (!MenuItem.class.isAssignableFrom(miCheckboxClass)) {
				miCheckboxClass = null;
			} else {
				// Check whether JRE CheckboxMenuItem has setState()
				cmiSetStateMethod = miCheckboxClass.getMethod("setState",
						new Class[] { boolean.class });
			}
		} catch (Exception e) {
			miCheckboxClass = null;
		}
	}

	public App(String appArgs[]) {
		args = appArgs;
		self = this;
		setTitle("SAS.Planet.J");

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				quit();
			}
		});

		Config.load();
	}

	private void quit() {
		updateConfigWinSize();
		trackLogger.loggerStop();
		if (serialReader != null)
			serialReader.stopReading();
		Config.save();

		if (getToolkit().getClass().getName().endsWith(".BBToolkit")) {
			// workaround for "BB" AWT implementation
			Runtime.getRuntime().halt(0);
		}
		System.exit(0);
	}

	public static boolean useAwtWorkaround() {
		return miCheckboxClass == null;
	}

	public static void cmiTrackLogSetState() {
		menuCheckboxSetState(self.cmiTrackLog, Config.trackLog);
	}

	public static void cmiCurMapSetState(boolean state) {
		menuCheckboxSetState(cmiMaps[Config.getCurMapIndex()], state);
		if (state) {
			self.mapsMenu.setLabel("Map ("
					+ Config.maps[Config.getCurMapIndex()].key + ")");
		}
	}

	private static void menuCheckboxSetState(MenuItem item, boolean state) {
		if (miCheckboxClass != null) {
			try {
				cmiSetStateMethod.invoke(item,
						new Object[] { new Boolean(state) });
			} catch (Exception e) {
				throw new RuntimeException(e.toString());
			}
		} else {
			String label = item.getLabel();
			if (label.startsWith(">>")) {
				label = label.substring(2);
			}
			item.setLabel(state ? ">>" + label : label);
		}
	}

	private MenuItem menuAddNewCheckbox(String name, String cmd, Menu menu) {
		MenuItem cmi;
		if (miCheckboxClass != null) {
			try {
				cmi = (MenuItem) miCheckboxClass.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e.toString());
			}
			((ItemSelectable) cmi).addItemListener(this);
			/* Workaround for some Linux Mobile devices: limit name length */
			name = name.substring(0, Math.min(name.length(), 14));
			cmi.setLabel(name);
		} else {
			cmi = new MenuItem(name);
			cmi.addActionListener(this);
		}
		cmi.setActionCommand(cmd);
		menu.add(cmi);
		return cmi;
	}

	public void addComponents() {

		MenuItem mi;

		Menu menu = new Menu("Menu");
		menu.addActionListener(this);
		mi = new MenuItem("Go to...");
		mi.setActionCommand(GOTO_COMMAND);
		menu.add(mi);
		menu.addSeparator();

		cmiMapView = menuAddNewCheckbox("Map view", MAIN_COMMAND, menu);
		menuCheckboxSetState(cmiMapView, true);
		cmiCoords = menuAddNewCheckbox("GPS coordinates", GPSLOG_COMMAND, menu);
		cmiNmea = menuAddNewCheckbox("NMEA", NMEALOG_COMMAND, menu);
		menu.addSeparator();

		Menu menuWaypoints = new Menu("Waypoints");
		menuWaypoints.addActionListener(this);
		mi = new MenuItem("Open waypoints...");
		mi.setActionCommand("WAYPOINTS_OPEN");
		menuWaypoints.add(mi);
		mi = new MenuItem("Clear waypoints");
		mi.setActionCommand("WAYPOINTS_CLEAR");
		menuWaypoints.add(mi);
		mi = new MenuItem("Save waypoints");
		mi.setActionCommand("WAYPOINTS_SAVE");
		menuWaypoints.add(mi);
		menu.add(menuWaypoints);

		Menu menuTracks = new Menu("Tracks");
		menuTracks.addActionListener(this);
		mi = new MenuItem("Open track...");
		mi.setActionCommand("TRACK_OPEN");
		menuTracks.add(mi);
		mi = new MenuItem("Clear tracks");
		mi.setActionCommand("TRACK_CLEAR");
		menuTracks.add(mi);
		cmiTrackLog = menuAddNewCheckbox("Log to \"" + TrackLogger.logFilename
				+ "\"", "TRACKLOG", menuTracks);
		mi = new MenuItem("Delete \"track.plt\"");
		mi.setActionCommand("TRACKLOG_DELETE");
		menuTracks.add(mi);
		menu.add(menuTracks);

		menu.addSeparator();
		cmiConnectGPS = menuAddNewCheckbox("Connect to GPS", "CONNECT_GPS",
				menu);
		menu.addSeparator();
		mi = new MenuItem("Exit");
		mi.setActionCommand(EXIT_COMMAND);
		menu.add(mi);

		menuBar.add(menu);

		menu = new Menu("Opts");
		cmiDrawGrid = menuAddNewCheckbox("Draw grid", DRAWGRID_COMMAND, menu);
		cmiDrawLatLng = menuAddNewCheckbox("Draw LatLng", DRAWLATLNG_COMMAND,
				menu);
		cmiDrawTail = menuAddNewCheckbox("Draw track tail", DRAWTAIL_COMMAND,
				menu);
		menuBar.add(menu);

		menu = new Menu();
		for (int i = 0; i < Config.maps.length; i++) {
			cmiMaps[i] = menuAddNewCheckbox(Config.maps[i].name, "MAPSWITCH"
					+ i, menu);
		}
		menu.addSeparator();
		Menu menuLayers = new Menu("Layers");
		cmiWikimapia = menuAddNewCheckbox("Wikimapia KML", "WIKIMAPIA",
				menuLayers);
		menu.add(menuLayers);
		mapsMenu = menu;
		menuBar.add(menu);

		miZoomOut = new MenuItem("Zoom out");
		miZoomOut.setActionCommand(ZOOMOUT_COMMAND);
		miZoomIn = new MenuItem("Zoom in");
		miZoomIn.setActionCommand(ZOOMIN_COMMAND);

		menuZoomOnlyTo = new Menu("Zoom in/out...");
		menuZoomOnlyTo.addActionListener(this);
		menuZoomToHigh = new Menu("Zoom to (high)...");
		menuZoomToHigh.addActionListener(this);
		menuZoomToLow = new Menu("Zoom to (low)...");
		menuZoomToLow.addActionListener(this);
		miCenter = new MenuItem("Center");
		miCenter.setActionCommand(CENTER_COMMAND);
		for (int i = TilesUtil.ZOOM_MAX; i >= 1; i--) {
			chkMenuZoomOnlyTo[i - 1] = menuAddNewCheckbox("level " + i,
					ZOOMONLYTO_COMMAND + i, menuZoomOnlyTo);
			chkMenuZoomTo[i - 1] = menuAddNewCheckbox("level " + i,
					ZOOMTO_COMMAND + i,
					i > TilesUtil.ZOOM_MAX / 2 ? menuZoomToHigh : menuZoomToLow);
		}

		setMenuBar(menuBar);

		Zip.zipsCache = new Cache(
				Config.useSoftRefs ? Config.zipCacheSize * 2 + 1
						: Config.zipCacheSize);
		TilesUtil.tilesCache = new Cache(Config.imageCacheSize); // <String,Image>
		Wikimapia.kmlCache = new Cache(Config.wikikmlCacheSize); // <String,ArrayList<WikiMapKML>>

		if (Config.connectGPS) {
			createSerialReader();
			if (serialReader == null) {
				serialReader = new SerialReader();
			}
			serialReader.start();
			if (Config.trackLog)
				trackLogger.loggerStart();
		} else {
			Config.trackLog = false;
		}

		if (main == null)
			main = new Main();
		changeView(main);
	}

	private void redrawMenuCheckboxes() {
		menuCheckboxSetState(cmiConnectGPS, Config.connectGPS);
		menuCheckboxSetState(cmiDrawGrid, Config.drawGrid);
		menuCheckboxSetState(cmiDrawLatLng, Config.drawLatLng);
		menuCheckboxSetState(cmiDrawTail, Config.drawTail);
		menuCheckboxSetState(cmiWikimapia, Config.drawWikimapia);
	}

	// Mysaifu bug. set states only after setVisible of frame
	private void afterSetVisible() {
		redrawMenuCheckboxes();
		cmiCurMapSetState(true);
		zoomMenu();
	}

	public void changeView(Component newView) {
		if (currentView != null) {
			if (currentView.getClass() == NMEALog.class) {
				((NMEALog) currentView).removeListener();
			} else if (currentView.getClass() == GPSLog.class) {
				((GPSLog) currentView).removeListener();
			} else if (currentView.getClass() == Main.class) {
				((Main) currentView).removeListener();
			}
			remove(currentView);
			menuCheckboxSetState(cmiMapView, false);
			menuCheckboxSetState(cmiCoords, false);
			menuCheckboxSetState(cmiNmea, false);
		}
		add(newView);
		currentView = newView;
		currentView.requestFocus();
		validate();

		if (currentView == main) {
			menuCheckboxSetState(cmiMapView, true);
			if (Config.connectGPS) {
				((Main) currentView).registerListener();
			}
		} else if (currentView.getClass() == GPSLog.class) {
			menuCheckboxSetState(cmiCoords, true);
		} else if (currentView.getClass() == NMEALog.class) {
			menuCheckboxSetState(cmiNmea, true);
		}
		// other windows register themselves
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command == EXIT_COMMAND) {
			quit();
		} else if (command == ZOOMIN_COMMAND) {
			zoomIn();
		} else if (command == ZOOMOUT_COMMAND) {
			zoomOut();
		} else if (command == CENTER_COMMAND) {
			main.viewOffset0();
			return;
		} else if (command == GOTO_COMMAND) {
			new GoTo(this).setVisible(true);
		} else if (command.equals("WAYPOINTS_OPEN")) {
			FileDialog d = new FileDialog(this, "Open waypoints file",
					FileDialog.LOAD);
			d.setFile("*.wpt");
			d.setVisible(true);
			if (d.getFile() != null) {
				String filename = d.getDirectory();
				// Mysaifu bug
				if (filename.charAt(filename.length() - 1) != File.separatorChar)
					filename += File.separatorChar;
				filename += d.getFile();
				Waypoints.load(filename);
			}
		} else if (command.equals("TRACK_OPEN")) {
			FileDialog d = new FileDialog(this, "Open track file",
					FileDialog.LOAD);
			d.setFile("*.plt");
			d.setVisible(true);
			if (d.getFile() != null) {
				String filename = d.getDirectory();
				// Mysaifu bug
				if (filename.charAt(filename.length() - 1) != File.separatorChar)
					filename += File.separatorChar;
				filename += d.getFile();
				Tracks.load(filename);
			}
		} else if (command.equals("WAYPOINTS_CLEAR")) {
			Waypoints.points = null;
			main.repaint();
		} else if (command.equals("TRACK_CLEAR")) {
			Tracks.tracks = null;
			main.repaint();
		} else if (command.equals("TRACKLOG_DELETE")) {
			trackLogger.deleteFile();
		} else if (command.equals("WAYPOINTS_SAVE")) {
			if (Waypoints.points == null || Waypoints.points.size() == 0) {
				new ShowMessage("Waypoint list is empty");
				return;
			}
			FileDialog d = new FileDialog(this, "Save waypoints to file",
					FileDialog.SAVE);
			d.setFile("*.wpt");
			d.setVisible(true);
			if (d.getFile() != null) {
				String filename = d.getDirectory();
				// Mysaifu bug
				if (filename.charAt(filename.length() - 1) != File.separatorChar)
					filename += File.separatorChar;
				filename += d.getFile();
				Waypoints.save(filename);
			}
		}

		if (miCheckboxClass != null) {
			currentView.requestFocus(); // for CreME
		} else {
			processCheckboxToggled(command);
			redrawMenuCheckboxes();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		processCheckboxToggled(((MenuItem) e.getSource()).getActionCommand());
	}

	private void processCheckboxToggled(String command) {
		if (command == MAIN_COMMAND) {
			changeView(main);
		} else if (command == GPSLOG_COMMAND) {
			changeView(new GPSLog());
		} else if (command == NMEALOG_COMMAND) {
			changeView(new NMEALog());
		} else if (command == DRAWGRID_COMMAND) {
			Config.drawGrid = !Config.drawGrid;
			main.repaint();
		} else if (command == DRAWLATLNG_COMMAND) {
			Config.drawLatLng = !Config.drawLatLng;
			main.repaint();
		} else if (command == "WIKIMAPIA") {
			Config.drawWikimapia = !Config.drawWikimapia;
			main.repaint();
		} else if (command.startsWith("MAPSWITCH")) {
			cmiCurMapSetState(false);

			Config.switchMapTo(Integer.valueOf(
					command.substring("MAPSWITCH".length())).intValue());
			cmiCurMapSetState(true);
			main.repaint();
		} else if (command.startsWith(ZOOMTO_COMMAND)) {
			main.zoomTo(new Integer(command.substring(ZOOMTO_COMMAND.length()))
					.intValue());
		} else if (command.startsWith(ZOOMONLYTO_COMMAND)) {
			Integer z = Integer.valueOf(command.substring(ZOOMONLYTO_COMMAND
					.length()));
			if (!Config.zoomsAvail.contains(z)) {
				Config.zoomsAvail.add(z);
			} else {
				Config.zoomsAvail.remove(z);
			}
		} else if (command == DRAWTAIL_COMMAND) {
			Config.drawTail = !Config.drawTail;
			if (Config.drawTail)
				Main.clearTrackTail(); // clear to prevent jump in tail
			main.repaint();
		} else if (command.equals("CONNECT_GPS")) {
			Config.connectGPS = !Config.connectGPS;
			if (!Config.connectGPS) {
				Config.trackLog = false;
				trackLogger.loggerStop();
				main.removeListener();
				if (serialReader != null) {
					serialReader.suspendReading();
				}
			} else {
				if (Config.drawTail) {
					Main.clearTrackTail();
				}

				if (serialReader != null && serialReader.isAlive()) {
					main.registerListener();
					serialReader.resumeReading();
				} else {
					createSerialReader();
					if (serialReader != null) {
						main.registerListener();
						serialReader.start();
					} else {
						Config.connectGPS = false;
						redrawMenuCheckboxes();
						getToolkit().beep();
					}
				}
			}
		} else if (command.equals("TRACKLOG")) {
			Config.trackLog = !Config.trackLog;
			if (!Config.connectGPS)
				Config.trackLog = false;
			if (Config.trackLog)
				trackLogger.loggerStart();
			else
				trackLogger.loggerStop();
		}

		currentView.requestFocus(); // for CreME
	}

	public static void zoomIn() {
		if (Config.zoom + 1 > TilesUtil.ZOOM_MAX) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		int zoom = Config.zoom + 1;
		for (int i = zoom; i <= TilesUtil.ZOOM_MAX; i++) {
			if (Config.zoomsAvail.contains(new Integer(zoom)))
				break;
			else
				zoom++;
		}
		if (zoom > TilesUtil.ZOOM_MAX) {
			Toolkit.getDefaultToolkit().beep();
			System.out.println("This is the maximum zoom allowed");
			return;
		}

		main.zoomTo(zoom);
	}

	public static void zoomOut() {
		if (Config.zoom <= 1) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		int zoom = Config.zoom - 1;
		for (int i = zoom; i >= 1; i--) {
			if (Config.zoomsAvail.contains(new Integer(zoom)))
				break;
			else
				zoom--;
		}
		if (zoom == 0) {
			Toolkit.getDefaultToolkit().beep();
			System.out.println("This is the minimum zoom allowed");
			return;
		}

		main.zoomTo(zoom);
	}

	public void zoomMenu() {
		// Such a trick because of Mysaifu bug with menuitem.setLabel.
		if (menuBar.getMenuCount() >= 4)
			menuBar.remove(3);

		Menu menuZoom = new Menu("Zoom " + Config.zoom);
		menuZoom.addActionListener(this);
		menuZoom.add(miZoomOut);
		menuZoom.add(miZoomIn);
		menuZoom.addSeparator();
		menuZoom.add(menuZoomToHigh);
		menuZoom.add(menuZoomToLow);
		menuZoom.add(menuZoomOnlyTo);
		menuZoom.addSeparator();
		menuZoom.add(miCenter);

		menuBar.add(menuZoom);

		for (int i = TilesUtil.ZOOM_MAX; i >= 1; i--) {
			menuCheckboxSetState(chkMenuZoomTo[i - 1], false);
			menuCheckboxSetState(chkMenuZoomOnlyTo[i - 1],
					Config.zoomsAvail.contains(new Integer(i)));
		}

		menuCheckboxSetState(chkMenuZoomTo[Config.zoom - 1], true);
	}

	public void createSerialReader() {
		if (args.length > 0) {
			String port = args[0];
			try {
				int baudRate = args.length > 1 ? new Integer(args[1])
						.intValue() : 9600;
				serialReader = new SerialReader(port, baudRate);
			} catch (Exception e) {
				serialReader = null;
			}
		} else {
			serialReader = new SerialReader();
		}

	}

	/**
	 * Moves map position to given coordinates
	 */
	public static void goTo(LatLng latlng) {
		Config.connectGPS = false;
		menuCheckboxSetState(self.cmiConnectGPS, false);
		main.removeListener();
		Main.clearTrackTail();
		latlng.copyTo(Main.getLatLng());
		main.viewOffset0(); // it will repaint also
	}

	/**
	 * Moves map position to given coordinates
	 */
	public static void createWaypoint(LatLng latlng, String name) {
		Waypoints.points.add(new Waypoint(latlng, name));
		main.repaint();
	}

	public static void main(String args[]) throws Exception {
		System.out.println("SAS.Planet.J v0.0.7");

		System.out.println("Working directory: " + Config.curDir);
		if (args.length > 0 && args[0].equals("list")) {
			SerialReader.listPorts();
			return;
		}

		if (miCheckboxClass == null) {
			System.out.println("No CheckboxMenuItem class");
		}
		menuBar = new MenuBar();
		App app = new App(args);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		System.out.println("Screen size=" + screenSize);
		int width = Config.windowWidth != 0 ? Config.windowWidth
				: screenSize.width;
		int height = Config.windowHeight != 0 ? Config.windowHeight
				: screenSize.height;
		int x = screenSize.width / 2;
		if (x + width > screenSize.width)
			x = 0;
		int y = screenSize.height / 10;
		if (y + height > screenSize.height)
			y = 0;
		app.setLocation(x, y);
		app.setSize(new Dimension(width, height));
		app.addComponents();
		app.setVisible(true);
		app.afterSetVisible();
		app.currentView.requestFocus();
	}

	private void updateConfigWinSize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension winSize = getSize();
		Config.windowWidth = winSize.width != screenSize.width ? winSize.width
				: 0;
		Config.windowHeight = winSize.height != screenSize.height ? winSize.height
				: 0;
	}

	public static App getSelf() {
		return self;
	}
}
