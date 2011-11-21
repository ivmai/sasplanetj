package sasplanetj;

import java.awt.*;

import sasplanetj.gps.LatLng;

public class GoTo extends Dialog {

	TextField lat = new TextField(LatLng.latlngFormat7.format(Main.latlng.lat), 4);
	TextField lng = new TextField(LatLng.latlngFormat7.format(Main.latlng.lng), 4);

	public GoTo(Frame owner){
		super(owner);
		//this.setSize(App.main.getSize());
		setTitle("Go to...");
		//setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		this.setSize(220, 150);
		this.setLocation(
				owner.getLocation().x+(owner.getSize().width-this.getSize().width)/2,
				owner.getLocation().y+(owner.getSize().height-this.getSize().height)/2
		);


		setLayout(new GridLayout(3, 2, 8, 8));

		add(new Label("Latitude:"));
		add(lat);
		add(new Label("Longitude:"));
		add(lng);

		//Panel p = new Panel();
		//p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		add(new Button("OK"));
		add(new Button("Cancel"));
		//add(p);
	}

	public boolean action(Event e, Object o) {
		if (e.target instanceof Button) {
			//System.out.println(((String) o));
			if (((String) o).equals("OK")) {
				LatLng latlng = new LatLng();
				latlng.lat = Double.valueOf(lat.getText()).doubleValue();
				latlng.lng = Double.valueOf(lng.getText()).doubleValue();
				App.Goto(latlng);
			}
		}
		dispose();
		return true;
	}

}
