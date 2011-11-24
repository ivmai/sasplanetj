package sasplanetj.util;

import java.io.*;
import java.util.Date;
import java.util.zip.*;

public class Zip {

	public static Cache zipsCache;

	public static byte[] getZipFile(String  zipname, String fileinzip){
		try {
			ZipFile zf = (ZipFile)zipsCache.get(zipname);
			if (zf == null) {
				Wikimapia.StopWatch watch = new Wikimapia.StopWatch();
				watch.start();
				Runtime runtime = Runtime.getRuntime();
				long memSizeInUse = runtime.totalMemory() - runtime.freeMemory();

			        zf = new ZipFile(zipname);
				long entriesCnt = zf.size();
				memSizeInUse = runtime.totalMemory() - runtime.freeMemory() - memSizeInUse;
				System.out.println("Zip: opened ("
					+ (new File(zipname).length() >> 10)
					+ " KiB, " + entriesCnt + " files) in "
					+ watch.currentMillis() + "ms"
					+ (memSizeInUse >= 0x400 ? " (" + (memSizeInUse >> 10)
								+ " KiB of RAM used)" : ""));
				zipsCache.put(zipname, zf, Config.useSoftRefs);
			}

			final ZipEntry ze = zf.getEntry(fileinzip);
			if (ze==null){
				return null;
			}

			// http://www.cs.cmu.edu/~jch/java/text/doug_erickson_961119.txt
			DataInputStream zis = new DataInputStream(zf.getInputStream(ze));
			byte res[] = new byte[(int)ze.getSize()];
			zis.readFully(res);
			zis.close();
		        return res;
		} catch (IOException e) {
			System.err.println("Zip: exception while accessing " + zipname + ": " + fileinzip + ": " + e);
		}
		return null;
	}



	public static void main(String args[]) throws Exception {
		test();
		test();
		test();
	}

	private static void test() {
		System.out.println("Working directory: "+Config.curDir);

		System.out.println("====================");
		Date start = new Date();

		String filename = "Wiki/z10/0/x299/0/y172.kml";
		String zipname = StringUtil.normPath(Config.curDir+"/cache/Wiki.zip");

		byte[] z = getZipFile(zipname, filename);
		Date end = new Date();
		System.out.println("Unpacked  in " + (end.getTime() - start.getTime())+"ms");

		start = new Date();
		String s = new String(z);
		System.out.println("First symbols="+s.substring(1, 50));


		end = new Date();
		System.out.println("Decoded  in " + (end.getTime() - start.getTime())+"ms");
	}

}
