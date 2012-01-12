package net.sf.sasplanetj.test;

import java.awt.CheckboxMenuItem;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;

public class TestMenu extends Frame {

	private static final long serialVersionUID = 7770522675456988394L;

	public TestMenu() {
		setSize(200, 200);

		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Opts");

		CheckboxMenuItem cmi = new CheckboxMenuItem("Draw grid", true);
		menu.add(cmi);
		menuBar.add(menu);

		cmi.setState(true);
		setMenuBar(menuBar);
		cmi.setState(true);
	}

	public static void main(String[] args) {
		TestMenu t = new TestMenu();
		t.validate();
		t.setVisible(true);
	}

}
