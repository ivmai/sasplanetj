package sasplanetj.test;

import java.awt.*;

import sasplanetj.util.Config;

public class TestMenu extends Frame {

	public CheckboxMenuItem cmi;

	public TestMenu(){
		setSize(200, 200);

		MenuBar menuBar = new MenuBar();

		Menu menu = new Menu("Opts");

		cmi = new CheckboxMenuItem("Draw grid", true);
		menu.add(cmi);
		menuBar.add(menu);

		cmi.setState(true);

		setMenuBar(menuBar);

		cmi.setState(true);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestMenu t = new TestMenu();
		t.validate();
		t.setVisible(true);
		//t.cmi.setState(true);

	}

}
