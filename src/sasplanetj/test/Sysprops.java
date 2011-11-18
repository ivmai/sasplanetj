// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Sysprops.java

package sasplanetj.test;

import java.io.PrintStream;
import java.util.*;

public class Sysprops
{

 public Sysprops()
 {
 }

 public static void main(String args[])
 {
  System.out.println("Environment:");
  Map e = System.getenv();
  String k;
  for (Iterator iterator = e.keySet().iterator(); iterator.hasNext(); System.out.println(k + "=" + e.get(k)))
   k = (String)iterator.next();

  System.out.println("\n\nProperties:");
  java.util.Properties pr = System.getProperties();
  String key;
  for (Iterator iterator = pr.keySet().iterator(); iterator.hasNext(); System.out.println(key + "=" + System.getProperty(key)))
   key = (String)iterator.next();

 }
}
