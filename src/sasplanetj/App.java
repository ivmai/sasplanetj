package sasplanetj;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.comm.*;

import sasplanetj.gps.LatLng;
import sasplanetj.gps.SerialReader;
import sasplanetj.ui.ShowMessage;
import sasplanetj.util.*;


public class App extends Frame implements ActionListener, ItemListener{
	private static App self;
	public static String args[];

	public static SerialReader serialReader = null;

	private static final String EXIT_COMMAND = "EXIT_COMMAND";
	private static final String NMEALOG_COMMAND = "NMEALOG_COMMAND";
	private static final String GPSLOG_COMMAND = "GPSLOG_COMMAND";
	private static final String MAIN_COMMAND = "MAIN_COMMAND";
	private static final String ZOOMOUT_COMMAND = "ZOOMOUT_COMMAND";
	private static final String ZOOMIN_COMMAND = "ZOOMIN_COMMAND";
	private static final String ZOOMTO_COMMAND = "ZOOMTO_COMMAND";
	private static final String ZOOMONLYTO_COMMAND = "ZOOMONLYTO_COMMAND";
	private static final String DRAWGRID_COMMAND = "DRAWGRID_COMMAND";
	private static final String DRAWLATLNG_COMMAND = "DRAWLATLNG_COMMAND";
	private static final String DRAWTAIL_COMMAND = "DRAWTAIL_COMMAND";
	private static final String GOTO_COMMAND = "GOTO_COMMAND";

	public static Main main = null;
	private static final MenuBar menuBar = new MenuBar();
	public static CheckboxMenuItem cmiConnectGPS;
	public static CheckboxMenuItem cmiTrackLog;
	public static CheckboxMenuItem cmiWikimapia;
	public static CheckboxMenuItem cmiDrawGrid;
	public static CheckboxMenuItem cmiDrawLatLng;
	public static CheckboxMenuItem cmiDrawTail;

	public static MenuItem miZoomOut;
	public static MenuItem miZoomIn;
	public static Menu menuZoomOnlyTo;
	public static CheckboxMenuItem[] chkMenuZoomOnlyTo;
	public static Menu menuZoomTo;
	public static CheckboxMenuItem[] chkMenuZoomTo;

	public Component currentView = null;

	public static CheckboxMenuItem[] cmiMaps = new CheckboxMenuItem[Config.maps.length];

	public App(String args[]){
		App.args = args;

		self = this;
		setTitle("SAS.Planet.J");

	    this.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){
	        	quit();
	        }
	    });

	    Config.load();
	}

	private void quit() {
		TrackLogger.loggerStop();
		if (serialReader!=null && serialReader.isAlive())
			serialReader.stopReading();
		Config.save();
		System.exit(0);
	}


	public void addComponents(){

		MenuItem mi;

		Menu menu = new Menu("Menu");
		menu.addActionListener(this);
			mi = new MenuItem("Go to...");
			mi.setActionCommand(GOTO_COMMAND);
			menu.add(mi);
			mi = new MenuItem("Map");
			mi.setActionCommand(MAIN_COMMAND);
			menu.add(mi);
			mi = new MenuItem("GPS coordinates");
			mi.setActionCommand(GPSLOG_COMMAND);
			menu.add(mi);
			mi = new MenuItem("NMEA");
			mi.setActionCommand(NMEALOG_COMMAND);
			menu.add(mi);
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
				cmiTrackLog = new CheckboxMenuItem("Log to \"track.plt\"");
				cmiTrackLog.setActionCommand("TRACKLOG");
				cmiTrackLog.addItemListener(this);
				menuTracks.add(cmiTrackLog);
				mi = new MenuItem("Delete \"track.plt\"");
				mi.setActionCommand("TRACKLOG_DELETE");
				menuTracks.add(mi);
			menu.add(menuTracks);

			menu.addSeparator();
			cmiConnectGPS = new CheckboxMenuItem("Connect to GPS");
			cmiConnectGPS.setActionCommand("CONNECT_GPS");
			cmiConnectGPS.addItemListener(this);
			menu.add(cmiConnectGPS);
			menu.addSeparator();
			mi = new MenuItem("Exit");
			mi.setActionCommand(EXIT_COMMAND);
			menu.add(mi);

		menuBar.add(menu);

		menu = new Menu("Opts");
		//menu.addActionListener(this);
			cmiDrawGrid = new CheckboxMenuItem("Draw grid");
			cmiDrawGrid.setActionCommand(DRAWGRID_COMMAND);
			cmiDrawGrid.addItemListener(this);
			menu.add(cmiDrawGrid);
			cmiDrawLatLng = new CheckboxMenuItem("Draw LatLng");
			cmiDrawLatLng.setActionCommand(DRAWLATLNG_COMMAND);
			cmiDrawLatLng.addItemListener(this);
			menu.add(cmiDrawLatLng);
			cmiDrawTail = new CheckboxMenuItem("Draw track tail");
			cmiDrawTail.setActionCommand(DRAWTAIL_COMMAND);
			cmiDrawTail.addItemListener(this);
			menu.add(cmiDrawTail);
		menuBar.add(menu);


		CheckboxMenuItem cmi;
		menu = new Menu("Maps");
		for (int i = 0; i < Config.maps.length; i++) {
			cmi = new CheckboxMenuItem(Config.maps[i].name);
			cmi.setActionCommand("MAPSWITCH"+i);
			cmi.addItemListener(this);
			menu.add(cmi);
			cmiMaps[i] = cmi;
		}
		menu.addSeparator();
		Menu menuLayers = new Menu("Layers");
		cmiWikimapia = new CheckboxMenuItem("Wikimapa KML");
		cmiWikimapia.setActionCommand("WIKIMAPIA");
		cmiWikimapia.addItemListener(this);
		menuLayers.add(cmiWikimapia);
		menu.add(menuLayers);
		menuBar.add(menu);

		miZoomOut = new MenuItem("Zoom out");
		miZoomOut.setActionCommand(ZOOMOUT_COMMAND);
		miZoomIn = new MenuItem("Zoom in");
		miZoomIn.setActionCommand(ZOOMIN_COMMAND);

		menuZoomOnlyTo = new Menu("Zoom in/out...");
		menuZoomOnlyTo.addActionListener(this);
		chkMenuZoomOnlyTo = new CheckboxMenuItem[TilesUtil.ZOOM_MAX - TilesUtil.ZOOM_MIN + 1];
		menuZoomTo = new Menu("Zoom to...");
		menuZoomTo.addActionListener(this);
		chkMenuZoomTo = new CheckboxMenuItem[TilesUtil.ZOOM_MAX - TilesUtil.ZOOM_MIN + 1];
		for (int i = TilesUtil.ZOOM_MAX; i >= TilesUtil.ZOOM_MIN; i--) {
			cmi = new CheckboxMenuItem("level " + i);
			cmi.setActionCommand(ZOOMONLYTO_COMMAND + i);
			cmi.addItemListener(this);
			menuZoomOnlyTo.add(cmi);
			chkMenuZoomOnlyTo[i - TilesUtil.ZOOM_MIN] = cmi;

			cmi = new CheckboxMenuItem("level " + i);
			cmi.setActionCommand(ZOOMTO_COMMAND + i);
			cmi.addItemListener(this);
			menuZoomTo.add(cmi);
			chkMenuZoomTo[i - TilesUtil.ZOOM_MIN] = cmi;
		}

		setMenuBar(menuBar);

		TilesUtil.tilesCache = new Cache(Config.imageCacheSize); //Cache<String, Image>
		Wikimapia.kmlCache = new Cache(Config.wikikmlCacheSize); //Cache<String, ArrayList<KML>>

		if (!Config.connectGPS) Config.trackLog = false;
		//cmiTrackLog.setState(Config.trackLog);

		if (Config.connectGPS){
			createSerialReader();
			serialReader.start();
			if (Config.trackLog) TrackLogger.loggerStart();
		}else{
			serialReader = new SerialReader(); //just a dumb that never runs
		}

		if (main==null) main = new Main();
		changeView(main);
	}

	//Mysaifu bug. set states only after setVisible of frame
	private void afterSetVisible() {
		cmiConnectGPS.setState(Config.connectGPS);
		cmiDrawGrid.setState(Config.drawGrid);
		cmiDrawLatLng.setState(Config.drawLatLng);
		cmiDrawTail.setState(Config.drawTail);
		cmiWikimapia.setState(Config.drawWikimapia);
		cmiMaps[Config.curMapIndex].setState(true);

		zoomMenu();
	}


	public void changeView(Component newView){
		//System.out.println(NMEALog.class.getCanonicalName());
		if (currentView!=null){
			//System.out.println("Current view was: "+currentView.getClass().getSimpleName());
			if (currentView.getClass()==NMEALog.class){
				((NMEALog) currentView).removeListener();
			}else if (currentView.getClass()==GPSLog.class){
				((GPSLog) currentView).removeListener();
			}else if (currentView.getClass()==Main.class){
				((Main) currentView).removeListener();
			}
			remove(currentView);
		}
		add(newView);
		currentView = newView;
		currentView.requestFocus();
		validate();

		if (currentView==main && Config.connectGPS){
			((Main) currentView).registerListener();
		}
		//other windows register themselves
	}


	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		//System.out.println(command);
		if (command == EXIT_COMMAND) {
			quit();
		}else if (command == NMEALOG_COMMAND) {
			changeView(new NMEALog());
		}else if (command == GPSLOG_COMMAND) {
			changeView(new GPSLog());
		}else if (command == MAIN_COMMAND) {
			changeView(main);
		}else if (command == ZOOMIN_COMMAND) {
			zoomIn();
		}else if (command == ZOOMOUT_COMMAND) {
			zoomOut();
		}else if (command == GOTO_COMMAND) {
			new GoTo(this).setVisible(true);
		}else if (command.equals("WAYPOINTS_OPEN")) {
			FileDialog d = new FileDialog(this, "Open waypoints file", FileDialog.LOAD);
			d.setFile("*.wpt");
			d.setVisible(true);
			if (d.getFile()!=null){
				String filename = d.getDirectory();
				//Mysaifu bug
				if (filename.charAt(filename.length()-1)!=File.separatorChar)
					filename += File.separatorChar;
				filename += d.getFile();
				Waypoints.load(filename);
			}
		}else if (command.equals("TRACK_OPEN")) {
			FileDialog d = new FileDialog(this, "Open track file", FileDialog.LOAD);
			d.setFile("*.plt");
			d.setVisible(true);
			if (d.getFile()!=null){
				String filename = d.getDirectory();
				//Mysaifu bug
				if (filename.charAt(filename.length()-1)!=File.separatorChar)
					filename += File.separatorChar;
				filename += d.getFile();
				Tracks.load(filename);
			}
		}else if (command.equals("WAYPOINTS_CLEAR")) {
			Waypoints.points = null;
			App.main.repaint();
		}else if (command.equals("TRACK_CLEAR")) {
			Tracks.tracks = null;
			App.main.repaint();
		}else if (command.equals("TRACKLOG_DELETE")) {
			File tracklog = new File(TrackLogger.logFilename);
			if (tracklog.exists())
				tracklog.delete();
		}else if (command.equals("WAYPOINTS_SAVE")) {
			if (Waypoints.points==null || Waypoints.points.size()==0){
				new ShowMessage("Waypoint list is empty");
				return;
			}
			FileDialog d = new FileDialog(this, "Save waypoints to file", FileDialog.SAVE);
			d.setFile("*.wpt");
			d.setVisible(true);
			if (d.getFile()!=null){
				String filename = d.getDirectory();
				//Mysaifu bug
				if (filename.charAt(filename.length()-1)!=File.separatorChar)
					filename += File.separatorChar;
				filename += d.getFile();
				Waypoints.save(filename);
			}
		}

		currentView.requestFocus(); //for creme
	}

	public void itemStateChanged(ItemEvent e) {
		CheckboxMenuItem chk =  (CheckboxMenuItem) e.getSource();
		String command = chk.getActionCommand();
		if (command == DRAWGRID_COMMAND) {
			Config.drawGrid = e.getStateChange()==ItemEvent.SELECTED;
			main.repaint();
		}else if (command == DRAWLATLNG_COMMAND) {
			Config.drawLatLng = e.getStateChange()==ItemEvent.SELECTED;
			main.repaint();
		}else if (command == "WIKIMAPIA") {
			Config.drawWikimapia = e.getStateChange()==ItemEvent.SELECTED;
			main.repaint();
		}else if (command.startsWith("MAPSWITCH")) {
			for (int i = 0; i < Config.maps.length; i++) {
				cmiMaps[i].setState(false);
			}
			Config.switchMapTo(Integer.valueOf( command.substring("MAPSWITCH".length()) ).intValue() );
			cmiMaps[Config.curMapIndex].setState(true);
			main.repaint();
		}else if (command.startsWith(ZOOMTO_COMMAND)) {
			zoom(new Integer(command.substring(ZOOMTO_COMMAND.length())).intValue());
		}else if (command.startsWith(ZOOMONLYTO_COMMAND)) {
			Integer z = Integer.valueOf( command.substring(ZOOMONLYTO_COMMAND.length()) );
			if (e.getStateChange()==ItemEvent.SELECTED){
				Config.zoomsAvail.add(z);
			}else{
				Config.zoomsAvail.remove(z);
			}
		}else if (command == DRAWTAIL_COMMAND) {
			Config.drawTail = e.getStateChange()==ItemEvent.SELECTED;
			if (Config.drawTail) Main.trackTail.clear(); //clear to prevent jump in tail
			main.repaint();
		}else if (command == "CONNECT_GPS") {
			Config.connectGPS = e.getStateChange()==ItemEvent.SELECTED;
			if (!Config.connectGPS){
				Config.trackLog = false;
				TrackLogger.loggerStop();
				main.removeListener();
				if (serialReader.isAlive())
					serialReader.stopReading();
			}else{
				if (Config.drawTail){
					Main.trackTail.clear();
				}
				if (serialReader==null || !serialReader.isAlive()){
					createSerialReader();
					main.registerListener();
					serialReader.start();
				}
			}
		}else if (command == "TRACKLOG") {
			Config.trackLog = e.getStateChange()==ItemEvent.SELECTED;
			if (!Config.connectGPS) Config.trackLog = false;
			if (Config.trackLog) TrackLogger.loggerStart();
			else TrackLogger.loggerStop();
		}

		currentView.requestFocus(); //for creme
	}

	public static void zoomIn() {
		if (Config.zoom+1 > TilesUtil.ZOOM_MAX){
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		int zoom = Config.zoom+1;
		for (int i = zoom; i <= TilesUtil.ZOOM_MAX; i++) {
			if (Config.zoomsAvail.contains(new Integer(zoom))) break;
			else zoom++;
		}
		if (zoom==TilesUtil.ZOOM_MAX+1){
			Toolkit.getDefaultToolkit().beep();
			System.out.println("This is the maximum zoom allowed");
			return;
		}

		zoom(zoom);
	}

	public static void zoomOut() {
		if (Config.zoom-1 < TilesUtil.ZOOM_MIN){
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		int zoom = Config.zoom-1;
		for (int i = zoom; i >= TilesUtil.ZOOM_MIN; i--) {
			if (Config.zoomsAvail.contains(new Integer(zoom))) break;
			else zoom--;
		}
		if (zoom==TilesUtil.ZOOM_MIN-1){
			Toolkit.getDefaultToolkit().beep();
			System.out.println("This is the minimum zoom allowed");
			return;
		}

		zoom(zoom);
	}

	public static void zoom(int zoom) {
		double deltaview = (double)Math.pow(2, zoom) / Math.pow(2, Config.zoom);
		//System.out.println(deltaview);
		Main.viewOffset.multiply(deltaview);
		//viewOffsetChanged();
		Config.zoom = zoom;
		App.getSelf().zoomMenu();
		main.repaint();
	}

	/*
	 * Such a trick because of Mysaifu bug with menuitem.setLabel
	 */
	public void zoomMenu() {
		if (menuBar.getMenuCount()>=4)
			menuBar.remove(3);


		Menu menuZoom = new Menu("Zoom "+Config.zoom);
		menuZoom.addActionListener(this);
		menuZoom.add(miZoomOut);
		menuZoom.add(miZoomIn);
		menuZoom.addSeparator();
		menuZoom.add(menuZoomTo);
		menuZoom.add(menuZoomOnlyTo);

		menuBar.add(menuZoom);

		for (int i = TilesUtil.ZOOM_MAX; i >= TilesUtil.ZOOM_MIN; i--) {
			chkMenuZoomTo[i-TilesUtil.ZOOM_MIN].setState(false);
			chkMenuZoomOnlyTo[i-TilesUtil.ZOOM_MIN].setState(Config.zoomsAvail.contains(new Integer(i)));
		}

		chkMenuZoomTo[Config.zoom-TilesUtil.ZOOM_MIN].setState(true);
	}

	public static void createSerialReader(){
	 	if (args.length > 0) {
		    String port = args[0];
		    int baudRate = args.length > 1 ? new Integer(args[1]).intValue() : 9600;
		    try {
				serialReader = new SerialReader(port, baudRate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			serialReader = new SerialReader();
		}

	}

	/**
	 * Moves map position to given coordinates
	 * @param latlng
	 */
	public static void Goto(LatLng latlng){
		Config.connectGPS = false;
		App.cmiConnectGPS.setState(false);
		App.main.removeListener();
		Main.trackTail.clear();
		latlng.copyTo(Main.latlng);
		App.main.viewOffset0(); //it will repaint also
		//App.main.repaint();
	}

	/**
	 * Moves map position to given coordinates
	 * @param latlng
	 */
	public static void CreateWaypoint(LatLng latlng, String name){
		//latlng.copyTo(Main.latlng);
		Waypoints.points.add(new Waypoint(latlng, name));
		App.main.repaint();
	}

	public static void main(String args[]) throws Exception {
		System.out.println("Working directory: "+Config.curDir);
		if (args.length>0 && args[0].equals("list")){
			Enumeration portList = CommPortIdentifier.getPortIdentifiers();
			System.out.println("Available ports:");
			while (portList.hasMoreElements()) {
				CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
				System.out.println("\t"+portId.getName());
			}
			return;
		}

		App app = new App(args);

		//app.fullscreen();

		if (Config.isCE){
			System.out.println("Screen size="+Toolkit.getDefaultToolkit().getScreenSize());
			//app.setUndecorated(true); //1.3 compat
			//app.setSize(new Dimension(200, 200));
			app.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		}else{
			app.setLocation(600,100);
			app.setSize(new Dimension(245, 320));
		}

		app.addComponents();
		//app.validate();
		app.setVisible(true);
		app.afterSetVisible();
		app.currentView.requestFocus();
		//app.currentView.repaint();


	}

	public void fullscreen(){
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		gd.setFullScreenWindow(this);

	}

	public static App getSelf() {
		return self;
	}
}
