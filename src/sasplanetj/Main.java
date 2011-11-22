package sasplanetj;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import sasplanetj.gps.*;
import sasplanetj.ui.CenterOffsetBtn;
import sasplanetj.ui.ColorsAndFonts;
import sasplanetj.ui.NewWaypointDialog;
import sasplanetj.ui.ShowMessage;
import sasplanetj.ui.XYConstraints;
import sasplanetj.ui.XYLayout;
import sasplanetj.util.Config;
import sasplanetj.util.TilesUtil;
import sasplanetj.util.TrackTail;
import sasplanetj.util.Tracks;
import sasplanetj.util.Waypoints;
import sasplanetj.util.Wikimapia;
import sasplanetj.util.XYint;
import sasplanetj.util.Wikimapia.KML;

/**
 * Panel. In Creme panel does not gain focus.
 * But Container or Component flash during repaint
 */
public class Main extends Panel implements GPSListener, MouseListener, MouseMotionListener, KeyListener, ActionListener{

	public static LatLng latlng = new LatLng();
	private static LatLng clickLatlng; //hold last click coordinates

    public static TrackTail trackTail;

    public static final XYint viewOffset = new XYint(0, 0);
    public static int viewOffsetStep = 50; //change view offset when pressing keys
    public static final CenterOffsetBtn offsetBtn = new CenterOffsetBtn();


    public static final PopupMenu popup = new PopupMenu();
	public static final ArrayList popupWiki = new ArrayList(); //ArrayList<MenuItem>

    Graphics dbf; //double buffer
    Image offscreen;

	private static int skipCounter = 0;


	public Main(){
		popup.addActionListener(this);
		add(popup);

		offsetBtn.setVisible(false);
		trackTail = new TrackTail(Config.trackTailSize);

		setLayout(new XYLayout());

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);

		this.addComponentListener(new ComponentAdapter(){
			public void componentHidden(ComponentEvent e) {
				System.out.println("Hidden");
		    }
		    public void componentShown(ComponentEvent e) {
				System.out.println("Shown");
		    }
		    public void componentMoved(ComponentEvent e) {
		    }
		    public void componentResized(ComponentEvent e) {
		    	initDbf();
		    	if (offsetBtn.isVisible()){
					remove(offsetBtn);
					add(offsetBtn, new XYConstraints(getSize().width-offsetBtn.getSize().width-5, getSize().height-offsetBtn.getSize().height-5, offsetBtn.getSize().width, offsetBtn.getSize().height));
		    	}
		    }
		});
	}

	/**
	 * Initializes double buffer with new size
	 */
	public void initDbf() {
		System.out.println("Main size "+getSize().width+"x"+getSize().height);
		if (this.getSize().width<=0 || this.getSize().height<=0) return;

		if (offscreen!=null){
			if (offscreen.getWidth(null)==this.getWidth() && offscreen.getHeight(null)==this.getHeight())
				return;
		}
		offscreen = createImage(this.getSize().width, this.getSize().height);
		dbf = offscreen.getGraphics();
	}

	public Dimension getPreferredSize() {
		return getSize();
	}

    public void update(Graphics g) {
    	paint(g);
    }

    public void paint(Graphics g) {
    	if (offscreen==null){
    		System.out.println("offscreen==null");
    		return;
    	}
    	final XYint center = new XYint(offscreen.getWidth(null)/2, offscreen.getHeight(null)/2);

    	/*Tile calculation*/
        final XYint displayXY = TilesUtil.coordinateToDisplay(latlng.lat, latlng.lng, Config.zoom, Config.isMapYandex);
        if (Config.drawTail && latlng.lat!=0 && latlng.lng!=0){
    		trackTail.addPoint(new XY(latlng.lat, latlng.lng));
        }
        displayXY.subtract(viewOffset);
        final XYint tileXY = TilesUtil.getTileByDisplayCoord(displayXY);
        final XYint intile = new XYint(displayXY.x%TilesUtil.TILESIZE, displayXY.y%TilesUtil.TILESIZE);
        final XYint centerTileTopLeft = new XYint(center.x-intile.x, center.y-intile.y);

        final XYint[] matrix = TilesUtil.drawTilesArea(offscreen.getWidth(null), offscreen.getHeight(null), centerTileTopLeft, tileXY, tileXY, dbf);

        /*Draw coordinates============================================================================================*/
    	if (Config.drawLatLng){
	    	dbf.setColor(ColorsAndFonts.clLatLng);
	    	dbf.drawString(latlng.toString(), 3, 15);
    	}

        /*Draw track============================================================================================*/
    	if (Config.drawTail){
    		trackTail.draw(dbf, matrix);
    	}


    	if (Tracks.tracks!=null) Tracks.draw(dbf, matrix);
    	if (Waypoints.points!=null) Waypoints.draw(dbf, matrix);

        /*Draw position============================================================================================*/
        drawPosition(dbf, center);


        /*Draw double buffer image to our view=====================================================================*/
        g.drawImage(offscreen,0,0,this);
    }






	private void drawPosition(Graphics g, XYint position) {
		Rectangle r = new Rectangle(viewOffset.x+position.x-3, viewOffset.y+position.y-3, 7, 7);
        g.setColor(ColorsAndFonts.clPositionBrush);
        g.fillOval(r.x, r.y, r.width, r.height);
        g.setColor(ColorsAndFonts.clPositionPen);
        g.drawOval(r.x, r.y, r.width, r.height);
	}

	public void gpsEvent(LatLng gi) {
		if (skipCounter==Config.drawMapSkip){
			skipCounter=0;
		}else{
			skipCounter++;
			return;
		}
		Main.latlng = gi;
		repaint();
	}


	public void registerListener(){
		App.serialReader.addGPSListener(this);
	}
	public void removeListener(){
		App.serialReader.removeGPSListener(this);
	}


	public void mouseClicked(MouseEvent e) {

	}

	private boolean doPopup(MouseEvent e) throws HeadlessException {
		boolean isPopup;
		if (System.getProperty("java.vm.name").startsWith("CrE-ME")){
			isPopup = true;
		}else{
			isPopup = e.isPopupTrigger() || e.getButton()==2 || e.getButton()==3;
		}

		if (isPopup){
			popup.removeAll();
			final XYint displayXY = TilesUtil.coordinateToDisplay(latlng.lat, latlng.lng, Config.zoom, Config.isMapYandex);
	        displayXY.subtract(viewOffset);
	        XYint clickOffset = new XYint(e.getPoint().x-getSize().width/2, e.getPoint().y-getSize().height/2);
	        displayXY.add(clickOffset);
	        clickLatlng = TilesUtil.displayToCoordinate(displayXY, Config.zoom, Config.isMapYandex);

	        MenuItem mi;

	        mi = new MenuItem(clickLatlng.toShortString());
			popup.add(mi);

	        mi = new MenuItem("Go here");
			mi.setActionCommand("GO_HERE");
			popup.add(mi);

			mi = new MenuItem("Create waypoint");
			mi.setActionCommand("CREATE_WAYPOINT");
			popup.add(mi);

			if (Config.drawWikimapia){
				popupWiki.clear();
				int i=0;
				for (Iterator iterator = Wikimapia.drawnKmlsIterator(); iterator.hasNext();) {
					KML kml = (KML) iterator.next();
					if (kml.drawnPoly.contains(e.getPoint())){
						mi = new MenuItem(kml.strip());
						mi.setActionCommand("POPUP_WIKI"+i);
						popupWiki.add(mi);
						i++;
					}
				}
				if (popupWiki.size()>0)
					popup.addSeparator();
				for (i = 0; i < popupWiki.size(); i++) {
					popup.add((MenuItem)popupWiki.get(i));
				}
			}

			popup.show(this, e.getX(), e.getY());
		}
		return isPopup;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	private static final XYint mouseDragPrevXY = new XYint();

	public void mousePressed(MouseEvent e) {
		doPopup(e);
		mouseDragPrevXY.setLocation(e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		XYint deltadrag = new XYint(e.getX()-mouseDragPrevXY.x, e.getY()-mouseDragPrevXY.y);
		viewOffset.add(deltadrag);
		viewOffsetChanged();

		mouseDragPrevXY.setLocation(e.getX(), e.getY());
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_F2:
			App.zoomOut();
			break;
		case KeyEvent.VK_F1:
			App.zoomIn();
			break;
		case KeyEvent.VK_UP:
			viewOffset.y += viewOffsetStep;
			viewOffsetChanged();
			break;
		case KeyEvent.VK_RIGHT:
			viewOffset.x -= viewOffsetStep;
			viewOffsetChanged();
			break;
		case KeyEvent.VK_DOWN:
			viewOffset.y -= viewOffsetStep;
			viewOffsetChanged();
			break;
		case KeyEvent.VK_LEFT:
			viewOffset.x += viewOffsetStep;
			viewOffsetChanged();
			break;
		case KeyEvent.VK_ENTER:
			viewOffset0();
			break;
		default:
			break;
		}
	}

	public void viewOffsetChanged(){
		if (viewOffset.x!=0 || viewOffset.y!=0){
			boolean found = false;
			for (int i = 0; i < this.getComponentCount(); i++) {
				if (this.getComponent(i)==offsetBtn){
					found = true;
					break;
				}
			}
			if (!found){
				add(offsetBtn, new XYConstraints(getSize().width-offsetBtn.getSize().width-5, getSize().height-offsetBtn.getSize().height-5, offsetBtn.getSize().width, offsetBtn.getSize().height));
				offsetBtn.setVisible(true);
			}
		}else{
			boolean found = false;
			for (int i = 0; i < this.getComponentCount(); i++) {
				if (this.getComponent(i)==offsetBtn){
					found = true;
					break;
				}
			}
			if (found){
				remove(offsetBtn);
				offsetBtn.setVisible(false);
			}
		}
		validate();
		repaint();
		this.requestFocus();
	}


	public void viewOffset0(){
		viewOffset.setLocation(0, 0);
		viewOffsetChanged();
	}

	public void keyTyped(KeyEvent e) {
		System.out.println("keyTyped="+e);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.startsWith("POPUP_WIKI")) {
			int i = Integer.valueOf(command.substring("POPUP_WIKI".length())).intValue();
			new ShowMessage(((MenuItem)popupWiki.get(i)).getLabel());
		}else if (command.equals("GO_HERE")) {
			clickLatlng.copyTo(latlng);
			Main.trackTail.clear();
			App.main.viewOffset0(); //it will repaint also
			App.main.repaint();
		}else if (command.equals("CREATE_WAYPOINT")) {
			new NewWaypointDialog(App.getSelf(), clickLatlng).setVisible(true);
		}
	}

}
