package sasplanetj.util;

import java.io.*;
import java.util.Date;
import java.util.zip.*;

//import sasplanetj.Native;

public class Zip {

	public static byte[] getZipFile(String  zipname, String fileinzip){
		//return !Config.isCE ? getZipFileJava(zipname, fileinzip) : Native.getZipFileNative(zipname, fileinzip); 
		return getZipFileJava(zipname, fileinzip);
	}
	
	private static byte[] getZipFileJava(String  zipname, String fileinzip){
		try {
			final ZipFile zf = new ZipFile(zipname);
			final ZipEntry ze = zf.getEntry(fileinzip);
			if (ze==null){
				System.out.println("File not found in ZIP "+fileinzip);
				return null;
			}
			//System.out.println("ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize());
			
			/*
			//buffer reading method
	        int size = (int)ze.getSize();
	        byte res[] = new byte[size];
	        if (size > 0){
		        final int N = 1024;
		        byte buf[] = new byte[N];
		        int ln = 0;
		        int read = 0;
		        while (size>0 && (ln = zis.read(buf, 0, Math.min(N, size))) != -1) {
		        	System.arraycopy(buf, 0, res, read, ln);
		        	read += ln;
					size -= ln;
				}	
	        }
	        */
			
			
			//http://www.cs.cmu.edu/~jch/java/text/doug_erickson_961119.txt
			DataInputStream zis = new DataInputStream(zf.getInputStream(ze));
			byte res[] = new byte[(int)ze.getSize()];
			zis.readFully(res);

			/*
			InputStream zis = new BufferedInputStream(zf.getInputStream(ze));
			int size = (int)ze.getSize();
			byte res[] = new byte[size];
			zis.read(res, 0, size);
			(/
			
		    zis.close();
		    //zf.close(); //creme compat. no need to close file, i think

		    /*
			FileOutputStream fos = new FileOutputStream("a.jpg");
			fos.write(res);
			fos.close();
			*/
		    
		    return res;
		} catch (IOException e) {
			System.out.println("Zip: IOException while accessing "+zipname+": "+fileinzip);
			e.printStackTrace();
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
		
		//String filename = "Wiki/z10/0/x299/0/y172.kml";
		//String zipname = "cache/Wiki.zip";
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
