package org.sf.sasplanetj;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.sf.sasplanetj.gps.LatLng;

public class GoTo extends Dialog {

	private static final long serialVersionUID = -6442614629327052440L;

	private final TextField lat = new TextField(LatLng.formatP7d(Main
			.getLatLng().getLat()), 4);
	private final TextField lng = new TextField(LatLng.formatP7d(Main
			.getLatLng().getLng()), 4);

	public GoTo(Frame owner) {
		super(owner);
		setTitle("Go to...");
		setModal(true);
		this.setSize(220, 150);
		this.setLocation(
				owner.getLocation().x
						+ (owner.getSize().width - this.getSize().width) / 2,
				owner.getLocation().y
						+ (owner.getSize().height - this.getSize().height) / 2);

		setLayout(new GridLayout(3, 2, 8, 8));

		add(new Label("Latitude:"));
		add(lat);
		add(new Label("Longitude:"));
		add(lng);

		add(new Button("OK"));
		add(new Button("Cancel"));

		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	public boolean action(Event e, Object o) {
		if ((e.target instanceof Button && ((String) o).equals("OK"))
				|| e.target instanceof TextField) {
			LatLng latlng = new LatLng(decodeAsDouble(lat), decodeAsDouble(lng));
			App.goTo(latlng);
		}
		dispose();
		return true;
	}

	private static double decodeAsDouble(TextField f) {
		return Double.valueOf(f.getText().replace(',', '.')).doubleValue();
	}
}
