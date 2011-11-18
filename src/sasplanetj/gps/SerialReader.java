package sasplanetj.gps;

import java.io.*;
import java.util.*;

import javax.comm.*;

import sasplanetj.util.Config;
import sasplanetj.util.StringUtil;


public class SerialReader extends Thread{
	String port = null;
	int baudRate;
	
	SerialPort serialPort = null;
	InputStream inputStream = null;
	OutputStream outputStream = null;

	public static final LatLng prevlatlng = new LatLng();
	public static final LatLng latlng = new LatLng();
	public static String msg = "";
	int gpsParsedMessages;
	
	/*LatLng listeners*/
	ArrayList listeners = new ArrayList(); //<GPSListener>
	/*raw NMEA string listeners*/
	ArrayList listenersNMEA = new ArrayList(); //<GPSListenerNMEA>

	public String simulateFname = StringUtil.normPath(Config.curDir+"/nmealog.txt");
	
	public boolean stopFlag = false; 
	

	
	/*
	 * Simulation creator
	 */
	public SerialReader(){
				
	}
	
	/*
	 * Real port creator
	 */
	public SerialReader(String port, int baudRate) throws Exception{
		this.port = port;
		this.baudRate = baudRate;
		
		try {
			serialPort = (SerialPort) CommPortIdentifier.getPortIdentifier(port).open("sasplanetj", 60);
		} catch (PortInUseException e) {
			System.out.println("SerailReader: PortInUseException");
			throw e;
		} catch (NoSuchPortException e) {
			System.out.println("SerailReader: NoSuchPortException");
			throw e;
		}
		
		try {
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("SerailReader: Error opening streams");
			throw e;
		}		
		
		try {
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			System.out.println("SerailReader: Warning: cannot program serial line parameters!");
		}		
		
		try {
			// GARMIN GPS25: enable all output messages
			// outputStream.write( new
			// String("$PGRMO,,3"+((char)13)+((char)10)).getBytes() );
			outputStream.write(NMEA.addCheckSum("$PGRMO,,3").getBytes());

			// trigger GPS to send current configuration
			// outputStream.write( new
			// String("$PGRMCE,"+((char)13)+((char)10)).getBytes() );
			outputStream.write(NMEA.addCheckSum("$PGRMCE,").getBytes());
			// default configuration string is:
			// $PGRMC,A,218.8,100,6378137.000,298.257223563,0.0,0.0,0.0,A,3,1,1,4,30

			// GARMIN GPS25: set to German earth datum (parameter 3=27)
			outputStream.write(new String("$PGRMC,A,,27,,,,,,A,3,1,1,4,30"
					+ ((char) 13) + ((char) 10)).getBytes());
			// outputStream.write(
			// NMEA.addCheckSum("$PGRMC,A,,100,,,,,,A,3,1,1,4,30").getBytes() );
		} catch (IOException e) {
			System.out.println("SerailReader: Warning: cannot configure GPS");
		}		
		
		System.out.println("SerailReader: setup done");
	}
	

    public void start() {
    	stopFlag = false;
    	super.start();
    }	
	
    public void stopReading() {
    	stopFlag = true;
    }	
	
	public void run() {
		System.out.println("SerailReader: thread is running");
		
		if (port==null){//silmulation
			try {
				simulate();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			return;
		}
		
		System.out.println("SerailReader: using port "+port+" at baudrate "+baudRate);
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		
		//Date start=new Date(), end=null;
		while (stopFlag==false){
			try {
				msg = in.readLine();
				//System.out.println(msg);
				if (msg!=null && NMEA.check(msg)) {
					if (NMEA.parse(msg, latlng)) {
						if (!prevlatlng.equalXY(latlng)){//prevent duplicate coordinates process
							//end = new Date();
							//System.out.println("new position in " + (end.getTime() - start.getTime()));
							//start=new Date();
							latlng.copyTo(prevlatlng);
							processGPSListeners();
						}
					}		
				}				
				processNMEAListeners(msg);
			} catch (Exception e) {
				System.out.println("SerailReader: Exception, msg="+msg);
				e.printStackTrace();
				break;
			}
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("SerailReader: thread stoped");
	}

	private void simulate() throws InterruptedException {
		System.out.println("SerailReader: simulating from " + simulateFname);
		File inFile = new File(simulateFname);
		try {
			//Date start=new Date(), end=null;
			while (true){//infinite loop over log
				FileInputStream fis = new FileInputStream(inFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				while ( (msg = br.readLine())!=null ) {
					if (msg!=null && NMEA.check(msg)) {
						/*
						if (gpsParsedMessages%100000==1) start = new Date();
						gpsParsedMessages++;
						*/
						
						if (NMEA.parse(msg, latlng)) {
							if (!prevlatlng.equalXY(latlng)){//prevent duplicate coordinates process
								latlng.copyTo(prevlatlng);
								//System.out.println("Parsed "+latlng);
								processGPSListeners();
								sleep(200);
							}
						}
						
						/*
						if (gpsParsedMessages%100000==0){
							end = new Date();
							System.out.println("parsed in " + (end.getTime() - start.getTime()));
						}
						*/
					}	
					processNMEAListeners(msg);
					if (stopFlag){ 
						System.out.println("SerailReader: simulating thread stoped");
						return;
					}
				}
				br.close();
			}
		} catch (FileNotFoundException e) {
			System.out.println("SerailReader: FileNotFoundException "+ simulateFname);
		} catch (Exception e) {
			System.out.println("SerailReader: Exception, msg="+msg);
			e.printStackTrace();
		}
	}
	
	void processGPSListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			((GPSListener)listeners.get(i)).gpsEvent(latlng);
		}
	}	
	
	public void addGPSListener(GPSListener gl) {
		synchronized (listeners) {
			if (!listeners.contains(gl)) {
				System.out.println("SerialReader.addGPSListener: adding listener "+gl.getClass().getName());
				listeners.add(gl);
			}
		}
	}
	
	public void removeGPSListener(GPSListener gl) {
		synchronized (listeners) {
			System.out.println("SerialReader.removeGPSListener: removing listener "+gl.getClass().getName());
			listeners.remove(gl);
		}
	}	

	
	void processNMEAListeners(String msg) {
		for (int i = 0; i < listenersNMEA.size(); i++) {
			((GPSListenerNMEA)listenersNMEA.get(i)).gpsEventNMEA(msg);
		}
	}	
	
	public void addNMEAListener(GPSListenerNMEA gl) {
		synchronized (listenersNMEA) {
			if (!listenersNMEA.contains(gl)) {
				System.out.println("SerialReader.addNMEAListener: adding listener "+gl.getClass().getName());
				listenersNMEA.add(gl);
			}
		}
	}
	
	public void removeNMEAListener(GPSListenerNMEA gl) {
		synchronized (listenersNMEA) {
			System.out.println("SerialReader.removeNMEAListener: removing listener "+gl.getClass().getName());
			listenersNMEA.remove(gl);
		}
	}	
	
	public static void main(String[] args) throws Exception {
		System.out.println("Testing SerailReader");
		
		SerialReader serialReader;
	 	if (args.length > 0) {
		    String port = args[0];
		    int baudRate = args.length > 1 ? new Integer(args[1]).intValue() : 9600;
		    serialReader = new SerialReader(port, baudRate);
		}else{
			serialReader = new SerialReader();
		}
		serialReader.start();
	}

}
