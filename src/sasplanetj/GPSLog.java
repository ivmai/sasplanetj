package sasplanetj;

import java.awt.TextArea;

import sasplanetj.gps.*;

/**
 * Parsed NMEA log
 */
public class GPSLog extends TextArea implements GPSListener{

	public GPSLog(){
		App.serialReader.addGPSListener(this);
	}

	public void gpsEvent(LatLng gi) {
		this.append(gi.toString()+"\r\n");
	}

	public void removeListener(){
		App.serialReader.removeGPSListener(this);
	}

}
