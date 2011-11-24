package sasplanetj.util;

import java.util.StringTokenizer;

public class StringUtil {
	public static final String fileSep = System.getProperty("file.separator");
	public static final char fileSepChar = fileSep.charAt(0);

	/**
	 * Much fater String.split using StringTokenizer
	 */
	public static String[] split(String s, String delimiter) {
		StringTokenizer st1 = new StringTokenizer(s, delimiter);
		String[] res = new String[st1.countTokens()];
		for (int i = 0; i < res.length; i++) {
			res[i] = st1.nextToken();
		}
		return res;
	}

	/**
	 * 1.3 compat
	 * 
	 * @param source
	 * @param pattern
	 * @param replace
	 * @return
	 */
	public static String replace(String source, String pattern, String replace) {
		if (source == null)
			return null;

		final int len = pattern.length();
		StringBuffer sb = new StringBuffer();
		int found = -1;
		int start = 0;

		while ((found = source.indexOf(pattern, start)) != -1) {
			sb.append(source.substring(start, found));
			sb.append(replace);
			start = found + len;
		}

		sb.append(source.substring(start));

		return sb.toString();

	}

	/**
	 * Normalizes path to with system's file.separator. Out internal paths use
	 * Unix slash /
	 * 
	 * @param path
	 * @return
	 */
	public static String normPath(String path) {
		if (fileSepChar == '/')
			return path;
		return path.replace('/', fileSepChar);
	}
}
