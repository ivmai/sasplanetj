// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   StringUtil.java

package sasplanetj.util;

import java.util.StringTokenizer;

public class StringUtil
{

 public static final String fileSep = System.getProperty("file.separator");
 public static final char fileSepChar = fileSep.charAt(0);

 public StringUtil()
 {
 }

 public static String[] split(String s, String delimeter)
 {
  StringTokenizer st1 = new StringTokenizer(s, delimeter);
  String res[] = new String[st1.countTokens()];
  for (int i = 0; i < res.length; i++)
   res[i] = st1.nextToken();

  return res;
 }

 public static String replace(String source, String pattern, String replace)
 {
  if (source == null)
   return null;
  int len = pattern.length();
  StringBuffer sb = new StringBuffer();
  int found = -1;
  int start;
  for (start = 0; (found = source.indexOf(pattern, start)) != -1; start = found + len)
  {
   sb.append(source.substring(start, found));
   sb.append(replace);
  }

  sb.append(source.substring(start));
  return sb.toString();
 }

 public static String normPath(String path)
 {
  if (fileSepChar == '/')
   return path;
  else
   return path.replace('/', fileSepChar);
 }

}
