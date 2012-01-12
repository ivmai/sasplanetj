package net.sf.sasplanetj.util;

import java.util.Date;

public class ZipTest {

	public static void main(String args[]) throws Exception {
		test();
		test();
		test();
	}

	private static void test() {
		System.out.println("Working directory: " + Config.curDir);

		System.out.println("====================");
		Date start = new Date();

		String filename = "Wiki/z10/0/x299/0/y172.kml";
		String zipname = StringUtil.normPath(Config.curDir + "/cache/Wiki.zip");

		byte[] z = Zip.getZipFile(zipname, filename);
		Date end = new Date();
		System.out.println("Unpacked  in " + (end.getTime() - start.getTime())
				+ "ms");

		start = new Date();
		String s = new String(z);
		System.out.println("First symbols=" + s.substring(1, 50));

		end = new Date();
		System.out.println("Decoded  in " + (end.getTime() - start.getTime())
				+ "ms");
	}
}
