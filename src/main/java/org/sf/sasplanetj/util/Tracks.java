package org.sf.sasplanetj.util;

import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.sf.sasplanetj.App;
import org.sf.sasplanetj.gps.XY;
import org.sf.sasplanetj.ui.ColorsAndFonts;

public class Tracks {

	public static ArrayList tracks; // ArrayList<XY[]>

	public static void load(String filename) {
		System.out.println("Tracks: loading " + filename);

		if (tracks == null)
			tracks = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			StopWatch watch = new StopWatch();
			watch.start();
			String s;
			// Read File Line By Line
			int lineCount = 0;
			ArrayList track = null;
			while ((s = br.readLine()) != null) {
				lineCount++;
				if (lineCount <= 6)
					continue;
				String[] splits = StringUtil.split(s, ",");
				if (splits.length < 3) {
					System.out.println("Tracks: wrong data on line "
							+ lineCount + ": " + s);
					continue;
				}
				int newTrack = Integer.valueOf(splits[2].trim()).intValue();

				if (newTrack == 1) {
					addTrack(track);
					track = new ArrayList(200);
				}
				if (track == null) {
					System.out
							.println("Tracks: WARNING: No starting point in track");
					track = new ArrayList(200);
				}
				if (track.size() % 200 == 0)
					track.ensureCapacity(track.size() + 200);
				track.add(new XY(
						Double.valueOf(splits[0].trim()).doubleValue(), Double
								.valueOf(splits[1].trim()).doubleValue()));
			}
			// Close the input stream
			br.close();
			addTrack(track);
			if (tracks.size() == 0) {
				System.out.println("Tracks: no tracks found in file");
				tracks = null;
			} else {
				System.out.println("Tracks: now we have " + tracks.size()
						+ " tracks loaded in " + watch.currentMillis() + "ms");
			}
			App.main.repaint();
		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
			System.err.println("Tracks: error reading " + filename);
		}

	}

	/**
	 * @param track
	 *            ArrayList<XY>
	 */
	private static void addTrack(ArrayList track) {
		if (track != null) {
			if (track.size() < 2) {
				System.out
						.println("Tracks: WARNING: track size is < 2, skiping it");
				return;
			}
			XY[] array = new XY[track.size()];
			track.toArray(array);
			tracks.add(array);
			System.out
					.println("Tracks: track " + tracks.size() + " has "
							+ ((XY[]) tracks.get(tracks.size() - 1)).length
							+ " points");
		}
	}

	public static void draw(Graphics dbf, XYint[] matrix) {

		dbf.setColor(ColorsAndFonts.clTrack);
		for (Iterator tr = tracks.iterator(); tr.hasNext();) {
			XY[] track = (XY[]) tr.next();

			int[] x = new int[track.length];
			int[] y = new int[track.length];

			for (int i = 0; i < track.length; i++) {
				final XY latlng = track[i]; // map pixel coordinates
				final XYint inmatrix = TilesUtil.coordinateToDisplay(
						latlng.getX(), latlng.getY(), Config.zoom,
						Config.isMapYandex);
				inmatrix.subtract(matrix[0]); // find point in tile matrix
				inmatrix.add(matrix[1]);
				// point with matrix position drawing offset

				x[i] = inmatrix.x;
				y[i] = inmatrix.y;
			}
			dbf.drawPolyline(x, y, track.length);
		}
	}

}
