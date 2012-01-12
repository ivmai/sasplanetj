package net.sf.sasplanetj.ui;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import net.sf.sasplanetj.App;

public class CenterOffsetBtn extends Button {

	private static final long serialVersionUID = 6102610282976917560L;

	private static final Dimension size = new Dimension(25, 25);

	private final Image img = loadImageFromFile("offset.png");

	public CenterOffsetBtn() {
		if (img == null) {
			// Do nothing if the resource is missing
			return;
		}

		size.setSize(img.getWidth(null), img.getHeight(null));

		setAllSizes();

		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				App.main.viewOffset0();
			}
		});

	}

	private void setAllSizes() {
		setSize(size);
	}

	private Image loadImageFromFile(String fname) {
		try {
			URL url = CenterOffsetBtn.class.getResource(fname);
			if (url != null) {
				Image img = getToolkit().getImage(url);
				MediaTracker tracker = new MediaTracker(this);
				tracker.addImage(img, 0);
				tracker.waitForAll();
				return img;
			}
		} catch (Exception e) {
			// Ignore.
		}
		System.out.println("Cannot open image: " + fname);
		return null;
	}

	public void repaint(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		if (img != null)
			g.drawImage(img, 0, 0, this);
	}

	public boolean isImageLoaded() {
		return img != null;
	}
}
