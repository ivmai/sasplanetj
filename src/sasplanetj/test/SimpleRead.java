// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   SimpleRead.java

package sasplanetj.test;

import java.io.*;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import javax.comm.*;

public class SimpleRead
 implements Runnable, SerialPortEventListener
{

 static CommPortIdentifier portId;
 static Enumeration portList;
 InputStream inputStream;
 SerialPort serialPort;
 Thread readThread;

 public static void main(String args[])
 {
  boolean portFound = false;
  String defaultPort = "COM4:";
  if (args.length > 0)
   defaultPort = args[0];
  for (portList = CommPortIdentifier.getPortIdentifiers(); portList.hasMoreElements();)
  {
   portId = (CommPortIdentifier)portList.nextElement();
   if (portId.getPortType() == 1 && portId.getName().equals(defaultPort))
   {
    System.out.println("Found port: " + defaultPort);
    portFound = true;
    SimpleRead simpleread = new SimpleRead();
   }
  }

  if (!portFound)
   System.out.println("port " + defaultPort + " not found.");
 }

 public SimpleRead()
 {
  try
  {
   serialPort = (SerialPort)portId.open("SimpleReadApp", 2000);
  }
  catch (PortInUseException portinuseexception) { }
  System.out.println("Created serialPort");
  try
  {
   inputStream = serialPort.getInputStream();
  }
  catch (IOException ioexception) { }
  System.out.println("Got inputStream");
  try
  {
   serialPort.addEventListener(this);
  }
  catch (TooManyListenersException toomanylistenersexception) { }
  System.out.println("Added listener");
  serialPort.notifyOnDataAvailable(true);
  try
  {
   serialPort.setSerialPortParams(9600, 8, 1, 0);
  }
  catch (UnsupportedCommOperationException unsupportedcommoperationexception) { }
  System.out.println("Setted serial params");
  readThread = new Thread(this);
  readThread.start();
  System.out.println("Started thread");
 }

 public void run()
 {
  System.out.println("Thread running");
  try
  {
   do
    Thread.sleep(1000L);
   while (true);
  }
  catch (InterruptedException interruptedexception)
  {
   System.out.println("Thread stopped");
  }
 }

 public void serialEvent(SerialPortEvent event)
 {
  System.out.println("Got SerialPortEvent");
  switch (event.getEventType())
  {
  case 1: // '\001'
   byte readBuffer[] = new byte[20];
   try
   {
    int i;
    while (inputStream.available() > 0) 
     i = inputStream.read(readBuffer);
    System.out.print(new String(readBuffer));
   }
   catch (IOException ioexception) { }
   break;
  }
 }
}
