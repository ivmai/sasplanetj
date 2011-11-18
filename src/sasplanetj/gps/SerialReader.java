// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   SerialReader.java

package sasplanetj.gps;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
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
 private LatLng prevlatlng = new LatLng();
 private final LatLng latlng = new LatLng();
 int gpsParsedMessages;
 ArrayList listeners;
 ArrayList listenersNMEA;
 public String simulateFname;
 public boolean stopFlag;
 private boolean suspendFlag;

 public SerialReader()
 {
  port = null;
  serialPort = null;
  inputStream = null;
  listeners = new ArrayList();
  listenersNMEA = new ArrayList();
  simulateFname = StringUtil.normPath(Config.curDir + File.separator +
                   "nmealog.txt");
  stopFlag = false;
 }

 public SerialReader(String port, int baudRate)
  throws Exception
 {
  this.port = null;
  serialPort = null;
  inputStream = null;
  listeners = new ArrayList();
  listenersNMEA = new ArrayList();
  stopFlag = false;
  this.port = port;
  this.baudRate = baudRate;
  try
  {
   serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier(
                 port).open("sasplanetj", 1000);
  }
  catch (PortInUseException e)
  {
   System.err.println("SerialReader: PortInUseException");
   throw e;
  }
  catch (NoSuchPortException e)
  {
   System.err.println("SerialReader: NoSuchPortException");
   throw e;
  }
  catch (ClassCastException e)
  {
   System.err.println("SerialReader: not a serial port");
   throw e;
  }
  OutputStream outputStream;
  try
  {
   inputStream = serialPort.getInputStream();
   outputStream = serialPort.getOutputStream();
  }
  catch (IOException e)
  {
   System.err.println("SerialReader: Error opening streams");
   throw e;
  }
  try
  {
   serialPort.setSerialPortParams(baudRate, 8, 1, 0);
  }
  catch (UnsupportedCommOperationException e)
  {
   System.err.println(
    "SerialReader: Warning: cannot program serial line parameters!");
  }
  try
  {
   outputStream.write(NMEA.addCheckSum("$PGRMO,,3").getBytes("US-ASCII"));
   outputStream.write(NMEA.addCheckSum("$PGRMCE,").getBytes("US-ASCII"));
   outputStream.write(
    (new String("$PGRMC,A,,27,,,,,,A,3,1,1,4,30\r\n")).getBytes("US-ASCII"));
   outputStream.close();
  }
  catch (IOException e)
  {
   System.out.println("SerialReader: Warning: cannot configure GPS");
  }
  System.out.println("SerialReader: setup done");
 }

 public static void listPorts()
 {
  Enumeration portList = CommPortIdentifier.getPortIdentifiers();
  System.out.println("Available ports:");
  while (portList.hasMoreElements())
   System.out.println("\t" +
    ((CommPortIdentifier)portList.nextElement()).getName());
 }

 public void stopReading()
 {
  stopFlag = true;
  resumeReading();
 }

 public void suspendReading()
 {
  suspendFlag = true;
 }

 public synchronized void resumeReading()
 {
  suspendFlag = false;
  notifyAll();
 }

 private synchronized void waitOnSuspend()
 {
  try
  {
   while (suspendFlag)
    wait();
  }
  catch (InterruptedException e) {}
 }

 public void run()
 {
  System.out.println("SerialReader: thread is running");
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
  System.out.println("SerialReader: using port " + port + " at baudrate " + baudRate);
  BufferedReader in;
  try
  {
   in = new BufferedReader(new InputStreamReader(inputStream, "US-ASCII"));
  }
  catch (UnsupportedEncodingException e)
  {
   e.printStackTrace();
   return;
  }
  while (!stopFlag)
  {
   String msg = null;
   try
   {
    msg = in.readLine();
    if (msg == null || msg.length() == 0)
    {
     sleep(100);
     continue;
    }
    if (NMEA.check(msg) && NMEA.parse(msg, latlng) &&
        !prevlatlng.equalXY(latlng))
    {
     latlng.copyTo(prevlatlng);
     processGPSListeners();
    }
    processNMEAListeners(msg);
    if (suspendFlag)
    {
     waitOnSuspend();
     prevlatlng = new LatLng();
    }
    continue;
   }
   catch (Exception e)
   {
    System.err.println("SerialReader: Exception, msg=" + msg);
    e.printStackTrace();
   }
   catch (OutOfMemoryError e)
   {
    try
    {
     System.err.println("SerialReader: " + e);
    }
    catch (OutOfMemoryError e2) {}
    continue;
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
  System.out.println("SerialReader: thread stopped");
 }

 private void simulate()
  throws InterruptedException
 {
  System.out.println("SerialReader: simulating from " + simulateFname);
  File inFile = new File(simulateFname);
  String msg = null;
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
      sleep(500);
     }
     processNMEAListeners(msg);
     if (stopFlag)
     {
      System.out.println("SerialReader: simulating thread stopped");
      return;
     }
     if (suspendFlag)
      waitOnSuspend();
    }
    br.close();
    sleep(3000);
   } while (true);
  }
  catch (FileNotFoundException e)
  {
   System.err.println("SerialReader: FileNotFoundException " + simulateFname);
  }
  catch (Exception e)
  {
   System.err.println("SerialReader: Exception, msg=" + msg);
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

 /* public static void main(String args[])
  throws Exception
 {
  System.out.println("Testing SerialReader");
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
 } */

}
