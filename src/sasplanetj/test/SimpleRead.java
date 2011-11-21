package sasplanetj.test;



import java.io.*;
import java.util.*;
import javax.comm.*;

/**
 * Class declaration
 *
 *
 * @author
 * @version 1.8, 08/03/00
 */
public class SimpleRead implements Runnable, SerialPortEventListener {
	static CommPortIdentifier portId;
	static Enumeration portList;
	InputStream inputStream;
	SerialPort serialPort;
	Thread readThread;

	/**
	 * Method declaration
	 *
	 *
	 * @param args
	 *
	 * @see
	 */
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
					SimpleRead reader = new SimpleRead();
				}
			}
		}
		if (!portFound) {
			System.out.println("port " + defaultPort + " not found.");
		}

	}

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
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
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
		}
		System.out.println("Setted serial params");

		readThread = new Thread(this);

		readThread.start();
		System.out.println("Started thread");
	}

	/**
	 * Method declaration
	 *
	 *
	 * @see
	 */
	public void run() {
		System.out.println("Thread running");
		try {
			while (true){
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
		}
		System.out.println("Thread stopped");
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param event
	 *
	 * @see
	 */
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
					int numBytes = inputStream.read(readBuffer);
				}

				System.out.print(new String(readBuffer));
			} catch (IOException e) {
			}

			break;
		}
	}

}
