// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Cache.java

package sasplanetj.util;

import java.lang.ref.SoftReference;

public class Cache
{

 private Object keys[];
 private Object values[];
 private int pos;

 public Cache(int size)
 {
  keys = new Object[size];
  values = new Object[size];
 }

 public Object get(Object key)
 {
  if (key != null)
   for (int i = 0; i < keys.length; i++)
    if (key.equals(keys[i]))
    {
     Object obj = values[i];
     if (obj != null)
     {
      if (!(obj instanceof SoftReference) ||
          (obj = ((SoftReference)obj).get()) != null)
       return obj;
      System.out.println("Cache SoftRef cleared for: " + key);
     }
     if (keys[pos] == null)
      pos = (pos > 0 ? pos : keys.length) - 1;
     keys[i] = keys[pos];
     values[i] = values[pos];
     keys[pos] = null;
     break;
    }
  return null;
 }

 public void put(Object key, Object value, boolean useSoftRefs)
 {
  if (keys.length == 0)
   return;
  keys[pos] = key;
  values[pos] = value != null && useSoftRefs ?
                 new SoftReference(value) : value;
  pos = pos < keys.length - 1 ? pos + 1 : 0;
 }

 public void clearAll()
 {
  for (int i = 0; i < keys.length; i++)
  {
   keys[i] = null;
   values[i] = null;
  }
  pos = 0;
 }
}
