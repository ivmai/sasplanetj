package net.sf.sasplanetj.test;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class Sysprops {

	public static void main(String[] args) {
		System.out.println("Environment:");

		Map e = System.getenv();
		for (Iterator iterator = e.keySet().iterator(); iterator.hasNext();) {
			String k = (String) iterator.next();
			System.out.println(k + "=" + e.get(k));
		}

		System.out.println("\n\nProperties:");
		Properties pr = System.getProperties();
		for (Iterator iterator = pr.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			System.out.println(key + "=" + System.getProperty(key));
		}
	}

}
