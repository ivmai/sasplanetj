package net.sf.sasplanetj.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.*;

public class SimpleRead implements Runnable, SerialPortEventListener {

	private static CommPortIdentifier portId;
	private static Enumeration portList;

	private InputStream inputStream;
	private SerialPort serialPort;
	private Thread readThread;

	public static void main(String[] args) {
		boolean portFound = false;
		String defaultPort = "COM4:";

		if (args.length > 0) {
			defaultPort = args[0];
		}

		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals(defaultPort)) {
					System.out.println("Found port: " + defaultPort);
					portFound = true;
					new SimpleRead();
				}
			}
		}
		if (!portFound) {
			System.out.println("port " + defaultPort + " not found.");
		}

	}

	public SimpleRead() {
		try {
			serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
		} catch (PortInUseException e) {
		}
		System.out.println("Created serialPort");

		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
		}
		System.out.println("Got inputStream");

		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
		}
		System.out.println("Added listener");

		serialPort.notifyOnDataAvailable(true);

		try {
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
		}
		System.out.println("Setted serial params");

		readThread = new Thread(this);

		readThread.start();
		System.out.println("Started thread");
	}

	public void run() {
		System.out.println("Thread running");
		try {
			while (true) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
		}
		System.out.println("Thread stopped");
	}

	public void serialEvent(SerialPortEvent event) {
		System.out.println("Got SerialPortEvent");
		switch (event.getEventType()) {

		case SerialPortEvent.BI:

		case SerialPortEvent.OE:

		case SerialPortEvent.FE:

		case SerialPortEvent.PE:

		case SerialPortEvent.CD:

		case SerialPortEvent.CTS:

		case SerialPortEvent.DSR:

		case SerialPortEvent.RI:

		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;

		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[20];

			try {
				while (inputStream.available() > 0) {
					int res = inputStream.read(readBuffer);
					if (res < 1)
						break;
				}

				System.out.print(new String(readBuffer));
			} catch (IOException e) {
				// Ignore.
			}

			break;
		}
	}

}
