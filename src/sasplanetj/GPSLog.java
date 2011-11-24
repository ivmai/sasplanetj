package sasplanetj;

import java.awt.TextArea;

import sasplanetj.gps.GPSListener;
import sasplanetj.gps.LatLng;

/**
 * Parsed NMEA log
 */
public class GPSLog extends TextArea implements GPSListener {

	public GPSLog() {
		if (App.serialReader != null)
			App.serialReader.addGPSListener(this);
	}

	public void gpsEvent(LatLng gi) {
		this.append(gi.toString() + "\r\n");
	}

	public void removeListener() {
		if (App.serialReader != null)
			App.serialReader.removeGPSListener(this);
	}

}
