package sasplanetj;

import java.awt.TextArea;

import sasplanetj.gps.GPSListenerNMEA;

/*
 * Raw NMEA data log
 */
public class NMEALog extends TextArea implements GPSListenerNMEA {

	public NMEALog() {
		if (App.serialReader != null)
			App.serialReader.addNMEAListener(this);
	}

	public void gpsEventNMEA(String msg) {
		this.append(msg + "\n");
	}

	public void removeListener() {
		if (App.serialReader != null)
			App.serialReader.removeNMEAListener(this);
	}

}
