package sasplanetj.ui;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import sasplanetj.App;
import sasplanetj.gps.LatLng;
import sasplanetj.util.Waypoints;

public class NewWaypointDialog extends Dialog {

	private static final long serialVersionUID = 823995977304341944L;

	TextField lat = new TextField();
	TextField lng = new TextField();
	TextField name = new TextField();

	public NewWaypointDialog(Frame owner, LatLng latlng) {
		super(owner);
		setTitle("Create waypoint");
		setModal(true);
		this.setSize(220, 180);
		this.setLocation(
				owner.getLocation().x
						+ (owner.getSize().width - this.getSize().width) / 2,
				owner.getLocation().y
						+ (owner.getSize().height - this.getSize().height) / 2);

		setLayout(new GridLayout(4, 2, 8, 8));

		add(new Label("Latitude:"));
		lat.setText(LatLng.latlngFormat7.format(latlng.lat));
		add(lat);

		add(new Label("Longitude:"));
		lng.setText(LatLng.latlngFormat7.format(latlng.lng));
		add(lng);

		add(new Label("Waypoint name:"));
		name.setText("wp"
				+ (Waypoints.points == null ? "1" : ""
						+ (Waypoints.points.size() + 1)));
		add(name);

		add(new Button("OK"));
		add(new Button("Cancel"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	public boolean action(Event e, Object o) {
		if (((e.target instanceof Button) && ((String) o).equals("OK"))
				|| e.target instanceof TextField) {
			LatLng latlng = new LatLng(decodeAsDouble(lat), decodeAsDouble(lng));
			App.CreateWaypoint(latlng, name.getText());
		}
		dispose();
		return true;
	}

	private static double decodeAsDouble(TextField f) {
		return Double.valueOf(f.getText().replace(',', '.')).doubleValue();
	}
}
