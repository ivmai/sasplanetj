package org.sf.sasplanetj.gps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.comm.*;

import org.sf.sasplanetj.util.Config;
import org.sf.sasplanetj.util.StringUtil;

public class SerialReader extends Thread {

	private static final byte[] PGRMO_BYTES = toAsciiBytes(NMEA
			.appendCheckSum("$PGRMO,,3"));
	private static final byte[] PGRMCE_BYTES = toAsciiBytes(NMEA
			.appendCheckSum("$PGRMCE,"));
	private static final byte[] PGRMC_CMD_BYTES = toAsciiBytes("$PGRMC,A,,27,,,,,,A,3,1,1,4,30\r\n");

	private String port;
	private int baudRate;
	private SerialPort serialPort;
	private InputStream inputStream;

	private LatLng prevlatlng = new LatLng();
	private final LatLng latlng = new LatLng();

	/* LatLng listeners */
	final private ArrayList listeners = new ArrayList(); // <GPSListener>
	/* raw NMEA string listeners */
	final private ArrayList listenersNMEA = new ArrayList(); // <GPSListenerNMEA>

	private String simulateFname;

	private boolean stopFlag;
	private boolean suspendFlag;

	private static byte[] toAsciiBytes(String str) {
		try {
			return str.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// Cannot happen.
			throw (Error) new InternalError().initCause(e);
		}
	}

	/*
	 * Simulation creator
	 */
	public SerialReader() {
		simulateFname = StringUtil.normPath(Config.curDir + File.separator
				+ "nmealog.txt");
	}

	/*
	 * Real port creator
	 */
	public SerialReader(String port, int baudRate) throws Exception {
		this.port = port;
		this.baudRate = baudRate;

		try {
			serialPort = (SerialPort) CommPortIdentifier
					.getPortIdentifier(port).open("org.sf.sasplanetj", 1000);
		} catch (PortInUseException e) {
			System.err.println("SerialReader: PortInUseException");
			throw e;
		} catch (NoSuchPortException e) {
			System.err.println("SerialReader: NoSuchPortException");
			throw e;
		} catch (ClassCastException e) {
			System.err.println("SerialReader: not a serial port");
			throw e;
		}

		OutputStream outputStream;
		try {
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.err.println("SerialReader: Error opening streams");
			throw e;
		}

		try {
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			System.err
					.println("SerialReader: Warning: cannot program serial line parameters!");
		}

		try {
			// GARMIN GPS25: enable all output messages
			outputStream.write(PGRMO_BYTES);

			// trigger GPS to send current configuration
			outputStream.write(PGRMCE_BYTES);
			// default configuration string is:
			// $PGRMC,A,218.8,100,6378137.000,298.257223563,0.0,0.0,0.0,A,3,1,1,4,30

			// GARMIN GPS25: set to German earth datum (parameter 3=27)
			outputStream.write(PGRMC_CMD_BYTES);

			outputStream.close();
		} catch (IOException e) {
			System.out.println("SerialReader: Warning: cannot configure GPS");
		}

		System.out.println("SerialReader: setup done");
	}

	public static void listPorts() {
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		System.out.println("Available ports:");
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList
					.nextElement();
			System.out.println("\t" + portId.getName());
		}
	}

	public void stopReading() {
		stopFlag = true;
		resumeReading();
	}

	public void suspendReading() {
		suspendFlag = true;
	}

	public synchronized void resumeReading() {
		suspendFlag = false;
		notifyAll();
	}

	private synchronized void waitOnSuspend() {
		try {
			while (suspendFlag) {
				wait();
			}
		} catch (InterruptedException e) {
			// Ignore.
		}
	}

	public void run() {
		System.out.println("SerialReader: thread is running");

		if (port == null) { // simulation
			try {
				simulate();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}

		System.out.println("SerialReader: using port " + port + " at baudrate "
				+ baudRate);
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(inputStream,
					"US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		while (stopFlag == false) {
			String msg = null;
			try {
				msg = in.readLine();
				if (msg == null || msg.length() == 0) {
					sleep(100); // prevent busy waiting
					continue;
				}
				if (NMEA.check(msg)) {
					if (NMEA.parse(msg, latlng)) {
						if (!prevlatlng.equalXY(latlng)) {
							// prevent duplicate coordinates process
							latlng.copyTo(prevlatlng);
							processGPSListeners();
						}
					}
				}
				processNMEAListeners(msg);
				if (suspendFlag) {
					waitOnSuspend();
					prevlatlng = new LatLng();
				}
			} catch (Exception e) {
				System.err.println("SerialReader: Exception, msg=" + msg);
				e.printStackTrace();
				break;
			} catch (OutOfMemoryError e) {
				try {
					System.err.println("SerialReader: " + e);
				} catch (OutOfMemoryError e2) {
					// Ignore.
				}
			}
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("SerialReader: thread stopped");
	}

	private void simulate() throws InterruptedException {
		System.out.println("SerialReader: simulating from " + simulateFname);
		File inFile = new File(simulateFname);
		String msg = null;
		try {
			while (true) {// infinite loop over log
				FileInputStream fis = new FileInputStream(inFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis));
				while ((msg = br.readLine()) != null) {
					if (msg != null && NMEA.check(msg)) {

						if (NMEA.parse(msg, latlng)) {
							if (!prevlatlng.equalXY(latlng)) {
								// prevent duplicate coordinates process
								latlng.copyTo(prevlatlng);
								processGPSListeners();
								sleep(500);
							}
						}

					}
					processNMEAListeners(msg);
					if (stopFlag) {
						System.out
								.println("SerialReader: simulating thread stopped");
						br.close();
						return;
					}
					if (suspendFlag) {
						waitOnSuspend();
					}
				}
				br.close();
				sleep(3000); // simulation delay (3 seconds)
			}
		} catch (FileNotFoundException e) {
			System.err.println("SerialReader: FileNotFoundException "
					+ simulateFname);
		} catch (Exception e) {
			System.err.println("SerialReader: Exception, msg=" + msg);
			e.printStackTrace();
		}
	}

	private void processGPSListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			((GPSListener) listeners.get(i)).gpsEvent(latlng);
		}
	}

	public void addGPSListener(GPSListener gl) {
		synchronized (listeners) {
			if (!listeners.contains(gl)) {
				System.out
						.println("SerialReader.addGPSListener: adding listener "
								+ gl.getClass().getName());
				listeners.add(gl);
			}
		}
	}

	public void removeGPSListener(GPSListener gl) {
		synchronized (listeners) {
			System.out
					.println("SerialReader.removeGPSListener: removing listener "
							+ gl.getClass().getName());
			listeners.remove(gl);
		}
	}

	private void processNMEAListeners(String msg) {
		for (int i = 0; i < listenersNMEA.size(); i++) {
			((GPSListenerNMEA) listenersNMEA.get(i)).gpsEventNMEA(msg);
		}
	}

	public void addNMEAListener(GPSListenerNMEA gl) {
		synchronized (listenersNMEA) {
			if (!listenersNMEA.contains(gl)) {
				System.out
						.println("SerialReader.addNMEAListener: adding listener "
								+ gl.getClass().getName());
				listenersNMEA.add(gl);
			}
		}
	}

	public void removeNMEAListener(GPSListenerNMEA gl) {
		synchronized (listenersNMEA) {
			System.out
					.println("SerialReader.removeNMEAListener: removing listener "
							+ gl.getClass().getName());
			listenersNMEA.remove(gl);
		}
	}
}
