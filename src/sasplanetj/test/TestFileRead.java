// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   TestFileRead.java

package sasplanetj.test;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

public class TestFileRead
{

 public TestFileRead()
 {
 }

 public static void main(String args[])
  throws Exception
 {
  String filename = "trahtemyriv.plt";
  Date start = new Date();
  FileReader fileread = new FileReader(filename);
  BufferedReader bufread = new BufferedReader(fileread);
  for (String str = new String(); (str = bufread.readLine()) != null;);
  Date end = new Date();
  System.out.println("read file with FileReader in " + (end.getTime() - start.getTime()));
  start = new Date();
  int len = (int)(new File(filename)).length();
  FileInputStream fis = new FileInputStream(filename);
  byte buf[] = new byte[len];
  fis.read(buf);
  fis.close();
  String bufs = new String(buf);
  ArrayList file = new ArrayList(100);
  int filepos = 0;
  for (int pos = 0; (pos = bufs.indexOf('\n', filepos)) != -1;)
  {
   file.add(bufs.substring(filepos, pos));
   filepos = pos + 1;
  }

  end = new Date();
  System.out.println("read file with full file read in " + (end.getTime() - start.getTime()));
  System.out.println("file lines size=" + file.size());
 }
}
