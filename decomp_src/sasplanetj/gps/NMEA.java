// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   NMEA.java

package sasplanetj.gps;


// Referenced classes of package sasplanetj.gps:
//   LatLng

public class NMEA
{

 public NMEA()
 {
 }

 static boolean check(String msg)
 {
  int msglen = msg.length();
  if (msglen > 4)
  {
   if (msg.charAt(msglen - 3) == '*')
   {
    String chk_s = checkSum(msg.substring(0, msglen - 3));
    return msg.substring(msglen - 2, msglen).equals(chk_s);
   } else
   {
    return true;
   }
  } else
  {
   return false;
  }
 }

 static String checkSum(String msg)
 {
  int chk = 0;
  for (int i = 1; i < msg.length(); i++)
   chk ^= msg.charAt(i);

  String chk_s;
  for (chk_s = Integer.toHexString(chk).toUpperCase(); chk_s.length() < 2; chk_s = "0" + chk_s);
  return chk_s;
 }

 static String addCheckSum(String msg)
 {
  return msg + "," + checkSum(msg + ",") + "*" + '\r' + '\n';
 }

 public static boolean parse(String msg, LatLng latlng)
  throws Exception
 {
  if (msg.startsWith("$GPRMC"))
  {
   int coma2 = msg.indexOf(',', 8);
   int coma3 = msg.indexOf(',', coma2 + 1);
   int coma4 = msg.indexOf(',', coma3 + 1);
   int coma5 = msg.indexOf(',', coma4 + 1);
   int coma6 = msg.indexOf(',', coma5 + 1);
   if ((coma2 | coma3 | coma4 | coma5 | coma6) < 0)
    return false;
   String latStr = msg.substring(coma3 + 1, coma4);
   String lngStr = msg.substring(coma5 + 1, coma6);
   if (latStr.length() == 0 || lngStr.length() == 0)
   {
    return false;
   } else
   {
    latlng.lat = normLatLong(Double.valueOf(latStr).doubleValue());
    latlng.lng = normLatLong(Double.valueOf(lngStr).doubleValue());
    return true;
   }
  } else
  {
   return false;
  }
 }

 public static double normLatLong(double c)
 {
  double deg = Math.floor(c * 0.01D);
  double min = c - deg * 100D;
  return deg + min / 60D;
 }
}
