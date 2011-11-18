// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst space 
// Source File Name:   Cache.java

package sasplanetj.util;


public class Cache
{

 Object keys[];
 Object values[];
 public int size;
 public int pos;

 public Cache(int size)
 {
  pos = 0;
  this.size = size;
  keys = new Object[size];
  values = new Object[size];
 }

 public int containsKey(Object key)
 {
  for (int i = 0; i < keys.length; i++)
   if (keys[i] != null && keys[i].equals(key))
    return i;

  return -1;
 }

 public Object get(Object key)
 {
  for (int i = 0; i < keys.length; i++)
   if (keys[i] != null && keys[i].equals(key))
    return values[i];

  return null;
 }

 public Object get(int i)
 {
  return values[i];
 }

 public void put(Object key, Object value)
 {
  keys[pos] = key;
  values[pos] = value;
  pos = pos != size - 1 ? pos + 1 : 0;
 }
}
