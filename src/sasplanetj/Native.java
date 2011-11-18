package sasplanetj;

import sasplanetj.util.Config;

public class Native {

	static {
		if (Config.isCE)
			System.loadLibrary("sasplanetj");
	}
	
	public native static byte[] getZipFileNative(String  zipname, String fileinzip);
	
}
