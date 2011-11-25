package org.sf.sasplanetj.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;

public class TestFileRead {

	public static void main(String[] args) throws Exception {
		String filename = "trahtemyriv.plt";
		Date start, end;

		start = new Date();
		FileReader fileread = new FileReader(filename);
		BufferedReader bufread = new BufferedReader(fileread);
		/* while loop that reads the file line after line until finished. */
		while (bufread.readLine() != null) {
			// Empty
		}
		end = new Date();
		System.out.println("read file with FileReader in "
				+ (end.getTime() - start.getTime()));

		start = new Date();
		int len = (int) (new File(filename).length());
		FileInputStream fis = new FileInputStream(filename);
		byte buf[] = new byte[len];
		fis.read(buf);
		fis.close();
		String bufs = new String(buf);
		ArrayList file = new ArrayList(100);
		int filepos = 0, pos = 0;
		while ((pos = bufs.indexOf('\n', filepos)) != -1) {
			file.add(bufs.substring(filepos, pos));
			filepos = pos + 1;
		}
		end = new Date();
		System.out.println("read file with full file read in "
				+ (end.getTime() - start.getTime()));
		System.out.println("file lines size=" + file.size());

	}

}
