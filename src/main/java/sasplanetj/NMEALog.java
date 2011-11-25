package sasplanetj;

import java.awt.TextArea;

import sasplanetj.gps.GPSListenerNMEA;

/*
 * Raw NMEA data log
 */
public class NMEALog extends TextArea implements GPSListenerNMEA {

	private static final long serialVersionUID = 8960810619223547067L;

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
