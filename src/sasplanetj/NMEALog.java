package sasplanetj;

import java.awt.TextArea;

import sasplanetj.gps.*;

/*
 * Raw NMEA data log
 */
public class NMEALog extends TextArea implements GPSListenerNMEA{

	public NMEALog(){
		App.serialReader.addNMEAListener(this);
	}

	public void gpsEventNMEA(String msg) {
		this.append(msg+"\n");
	}

	public void removeListener(){
		App.serialReader.removeNMEAListener(this);
	}

}
