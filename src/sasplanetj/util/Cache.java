package sasplanetj.util;

public class Cache{

	Object[] keys;
	Object[] values;
	
	public int size;
	public int pos = 0;
	
	public Cache(int size){
		this.size = size;
		
		keys = new Object[size];
		values = new Object[size];
	}
	
	/**
	 * Returns index of the key
	 * Returns -1 if cache does not contain key
	 * @param key
	 */
	public int containsKey(Object key){
		for (int i = 0; i < keys.length; i++) {
			if (keys[i]!=null && keys[i].equals(key))
				return i;
		}
		return -1;
	}

	/**
	 * returns null if no object with such key
	 */
	public Object get(Object key){
		for (int i = 0; i < keys.length; i++) {
			if (keys[i]!=null && keys[i].equals(key))
				return values[i];
		}
		return null;
	}
	
	public Object get(int i){
		return values[i];
	}
	
	public void put(Object key, Object value){
		keys[pos] = key;
		values[pos] = value;
		
		//jump to next free position
		pos = pos==size-1 ? 0 : pos+1;
	}
	
}
