package net.sf.sasplanetj.test;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

public class TestDraw extends Frame implements MouseListener {

	private static final long serialVersionUID = -5244619909817202555L;

	public void paint(Graphics g) {

		int size = 10000;

		int[] x = new int[size];
		int[] y = new int[size];
		x[1] = y[1] = 100;

		Date start = new Date();
		g.drawPolyline(x, y, x.length);
		Date end = new Date();
		System.out.println("drawn polyline in "
				+ (end.getTime() - start.getTime()));

		start = new Date();
		for (int i = 1; i < y.length; i++) {
			g.drawLine(x[i - 1], y[i - 1], x[i], y[i]);
		}
		end = new Date();
		System.out.println("drawn lines in "
				+ (end.getTime() - start.getTime()));
	}

	public static void main(String[] args) {
		TestDraw t = new TestDraw();
		t.addMouseListener(t);
		t.setSize(300, 300);
		t.setVisible(true);
	}

	public void mouseClicked(MouseEvent e) {
		System.exit(0);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

}
