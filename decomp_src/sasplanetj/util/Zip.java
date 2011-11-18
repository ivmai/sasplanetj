// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Zip.java

package sasplanetj.util;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Referenced classes of package sasplanetj.util:
//   Config, StringUtil

public class Zip
{

 public Zip()
 {
 }

 public static byte[] getZipFile(String zipname, String fileinzip)
 {
  ZipFile zf;
  ZipEntry ze;
  try
  {
   zf = new ZipFile(zipname);
   ze = zf.getEntry(fileinzip);
   if (ze == null)
   {
    System.out.println("File not found in ZIP " + fileinzip);
    return null;
   }
   DataInputStream zis = new DataInputStream(zf.getInputStream(ze));
   byte res[] = new byte[(int)ze.getSize()];
   zis.readFully(res);
   return res;
  }
  catch (IOException e)
  {
   System.out.println("Zip: IOException while accessing " + zipname + ": " + fileinzip);
   e.printStackTrace();
   return null;
  }
 }

 public static void main(String args[])
  throws Exception
 {
  test();
  test();
  test();
 }

 private static void test()
 {
  System.out.println("Working directory: " + Config.curDir);
  System.out.println("====================");
  Date start = new Date();
  String filename = "Wiki/z10/0/x299/0/y172.kml";
  String zipname = StringUtil.normPath(Config.curDir + "/cache/Wiki.zip");
  byte z[] = getZipFile(zipname, filename);
  Date end = new Date();
  System.out.println("Unpacked  in " + (end.getTime() - start.getTime()) + "ms");
  start = new Date();
  String s = new String(z);
  System.out.println("First symbols=" + s.substring(1, 50));
  end = new Date();
  System.out.println("Decoded  in " + (end.getTime() - start.getTime()) + "ms");
 }
}
