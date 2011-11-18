// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   SerialReader.java

package sasplanetj.gps;

import java.io.*;
import java.util.ArrayList;
import javax.comm.*;
import sasplanetj.util.Config;
import sasplanetj.util.StringUtil;

// Referenced classes of package sasplanetj.gps:
//   LatLng, NMEA, GPSListener, GPSListenerNMEA

public class SerialReader extends Thread
{

 String port;
 int baudRate;
 SerialPort serialPort;
 InputStream inputStream;
 OutputStream outputStream;
 public static final LatLng prevlatlng = new LatLng();
 public static final LatLng latlng = new LatLng();
 public static String msg = "";
 int gpsParsedMessages;
 ArrayList listeners;
 ArrayList listenersNMEA;
 public String simulateFname;
 public boolean stopFlag;

 public SerialReader()
 {
  port = null;
  serialPort = null;
  inputStream = null;
  outputStream = null;
  listeners = new ArrayList();
  listenersNMEA = new ArrayList();
  simulateFname = StringUtil.normPath(Config.curDir + "/nmealog.txt");
  stopFlag = false;
 }

 public SerialReader(String port, int baudRate)
  throws Exception
 {
  this.port = null;
  serialPort = null;
  inputStream = null;
  outputStream = null;
  listeners = new ArrayList();
  listenersNMEA = new ArrayList();
  simulateFname = StringUtil.normPath(Config.curDir + "/nmealog.txt");
  stopFlag = false;
  this.port = port;
  this.baudRate = baudRate;
  try
  {
   serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier(port).open("sasplanetj", 60);
  }
  catch (PortInUseException e)
  {
   System.out.println("SerailReader: PortInUseException");
   throw e;
  }
  catch (NoSuchPortException e)
  {
   System.out.println("SerailReader: NoSuchPortException");
   throw e;
  }
  try
  {
   inputStream = serialPort.getInputStream();
   outputStream = serialPort.getOutputStream();
  }
  catch (IOException e)
  {
   System.out.println("SerailReader: Error opening streams");
   throw e;
  }
  try
  {
   serialPort.setSerialPortParams(baudRate, 8, 1, 0);
  }
  catch (UnsupportedCommOperationException e)
  {
   System.out.println("SerailReader: Warning: cannot program serial line parameters!");
  }
  try
  {
   outputStream.write(NMEA.addCheckSum("$PGRMO,,3").getBytes());
   outputStream.write(NMEA.addCheckSum("$PGRMCE,").getBytes());
   outputStream.write((new String("$PGRMC,A,,27,,,,,,A,3,1,1,4,30\r\n")).getBytes());
  }
  catch (IOException e)
  {
   System.out.println("SerailReader: Warning: cannot configure GPS");
  }
  System.out.println("SerailReader: setup done");
 }

 public void start()
 {
  stopFlag = false;
  super.start();
 }

 public void stopReading()
 {
  stopFlag = true;
 }

 public void run()
 {
  System.out.println("SerailReader: thread is running");
  if (port == null)
  {
   try
   {
    simulate();
   }
   catch (InterruptedException e)
   {
    e.printStackTrace();
   }
   return;
  }
  System.out.println("SerailReader: using port " + port + " at baudrate " + baudRate);
  BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
  while (!stopFlag) 
  {
   try
   {
    msg = in.readLine();
    if (msg != null && NMEA.check(msg) && NMEA.parse(msg, latlng) && !prevlatlng.equalXY(latlng))
    {
     latlng.copyTo(prevlatlng);
     processGPSListeners();
    }
    processNMEAListeners(msg);
    continue;
   }
   catch (Exception e)
   {
    System.out.println("SerailReader: Exception, msg=" + msg);
    e.printStackTrace();
   }
   break;
  }
  try
  {
   in.close();
  }
  catch (IOException e)
  {
   e.printStackTrace();
  }
  System.out.println("SerailReader: thread stoped");
 }

 private void simulate()
  throws InterruptedException
 {
  System.out.println("SerailReader: simulating from " + simulateFname);
  File inFile = new File(simulateFname);
  try
  {
   do
   {
    FileInputStream fis = new FileInputStream(inFile);
    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
    while ((msg = br.readLine()) != null) 
    {
     if (msg != null && NMEA.check(msg) && NMEA.parse(msg, latlng) && !prevlatlng.equalXY(latlng))
     {
      latlng.copyTo(prevlatlng);
      processGPSListeners();
      Thread.sleep(200L);
     }
     processNMEAListeners(msg);
     if (stopFlag)
     {
      System.out.println("SerailReader: simulating thread stoped");
      return;
     }
    }
    br.close();
   } while (true);
  }
  catch (FileNotFoundException e)
  {
   System.out.println("SerailReader: FileNotFoundException " + simulateFname);
  }
  catch (Exception e)
  {
   System.out.println("SerailReader: Exception, msg=" + msg);
   e.printStackTrace();
  }
 }

 void processGPSListeners()
 {
  for (int i = 0; i < listeners.size(); i++)
   ((GPSListener)listeners.get(i)).gpsEvent(latlng);

 }

 public void addGPSListener(GPSListener gl)
 {
  synchronized (listeners)
  {
   if (!listeners.contains(gl))
   {
    System.out.println("SerialReader.addGPSListener: adding listener " + gl.getClass().getName());
    listeners.add(gl);
   }
  }
 }

 public void removeGPSListener(GPSListener gl)
 {
  synchronized (listeners)
  {
   System.out.println("SerialReader.removeGPSListener: removing listener " + gl.getClass().getName());
   listeners.remove(gl);
  }
 }

 void processNMEAListeners(String msg)
 {
  for (int i = 0; i < listenersNMEA.size(); i++)
   ((GPSListenerNMEA)listenersNMEA.get(i)).gpsEventNMEA(msg);

 }

 public void addNMEAListener(GPSListenerNMEA gl)
 {
  synchronized (listenersNMEA)
  {
   if (!listenersNMEA.contains(gl))
   {
    System.out.println("SerialReader.addNMEAListener: adding listener " + gl.getClass().getName());
    listenersNMEA.add(gl);
   }
  }
 }

 public void removeNMEAListener(GPSListenerNMEA gl)
 {
  synchronized (listenersNMEA)
  {
   System.out.println("SerialReader.removeNMEAListener: removing listener " + gl.getClass().getName());
   listenersNMEA.remove(gl);
  }
 }

 public static void main(String args[])
  throws Exception
 {
  System.out.println("Testing SerailReader");
  SerialReader serialReader;
  if (args.length > 0)
  {
   String port = args[0];
   int baudRate = args.length <= 1 ? 9600 : (new Integer(args[1])).intValue();
   serialReader = new SerialReader(port, baudRate);
  } else
  {
   serialReader = new SerialReader();
  }
  serialReader.start();
 }

}
