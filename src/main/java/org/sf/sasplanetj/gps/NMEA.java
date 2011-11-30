package org.sf.sasplanetj.gps;

public class NMEA {

	private NMEA() {
		// Prohibit instantiation.
	}

	/*
	 * $GPGGA Sentence (Fix data) $GPGLL Sentence (Position) $GPGSV Sentence
	 * (Satellites in view) $GPGSA Sentence (Active satellites) $GPRMC Sentence
	 * (Position and time) $GPVTG Sentence (Course over ground)
	 */

	// calculate checksum of NMEA message and compare
	static boolean check(String msg) {
		int msglen = msg.length();

		if (msglen > 4) {
			if (msg.charAt(msglen - 3) == '*') {
				// perform NMEA checksum calculation
				String chk_s = checkSum(msg.substring(0, msglen - 3));
				// compare checksum to encoded checksum in msg
				return (msg.substring(msglen - 2, msglen).equals(chk_s));
			} else {
				// message doesn't have a checksum: accept it
				return true;
			}
		}

		// don't accept messages without checksum
		return false;
	}

	static String checkSum(String msg) {
		// perform NMEA checksum calculation
		int chk = 0;
		for (int i = 1; i < msg.length(); i++) {
			chk ^= msg.charAt(i);
		}

		String chk_s = Integer.toHexString(chk).toUpperCase();

		// checksum must be 2 characters!
		while (chk_s.length() < 2) {
			chk_s = "0" + chk_s;
		}

		return chk_s;
	}

	/*
	 * Used in GPS receiver confirmation
	 */
	static String appendCheckSum(String msg) {
		return msg + "," + checkSum(msg + ",") + "*\r\n";
	}

	public static boolean parse(String msg, LatLng latlng) throws Exception {
		// Speed optimization, use GPRMC as Ozi does by default
		if (msg.startsWith("$GPRMC")) {
			// "$GPRMC,1utc,2status,3lat,4northHemi,5longitude,6eastHemi,7speedOverGroundKnots,8courseOverGround,9utcdate,magnVariation,magnVarDirection,",
			int coma2 = msg.indexOf(',', 8);
			int coma3 = msg.indexOf(',', coma2 + 1);
			int coma4 = msg.indexOf(',', coma3 + 1);
			int coma5 = msg.indexOf(',', coma4 + 1);
			int coma6 = msg.indexOf(',', coma5 + 1);

			if ((coma2 | coma3 | coma4 | coma5 | coma6) < 0)
				return false;

			String latStr = msg.substring(coma3 + 1, coma4);
			String lngStr = msg.substring(coma5 + 1, coma6);
			if (latStr.length() == 0 || lngStr.length() == 0)
				return false;
			latlng.set(normLatLong(Double.valueOf(latStr).doubleValue()),
					normLatLong(Double.valueOf(lngStr).doubleValue()));
			return true;
		}

		return false;
	}

	public static double normLatLong(double c) {
		double deg = Math.floor(c * 1e-2);
		double min = c - deg * 1e2;
		return deg + min / 60.0;
	}

}
