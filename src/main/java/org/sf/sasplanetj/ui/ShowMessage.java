package org.sf.sasplanetj.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.sf.sasplanetj.App;

public class ShowMessage extends Dialog {

	private static final long serialVersionUID = 4896260607587042949L;

	private Frame owner = App.getSelf();

	public ShowMessage(String msg) {
		super(App.getSelf());

		this.setTitle(owner.getTitle());
		this.setSize(220, 220);
		this.setLocation(
				owner.getLocation().x
						+ (owner.getSize().width - this.getSize().width) / 2,
				owner.getLocation().y
						+ (owner.getSize().height - this.getSize().height) / 2);

		setLayout(new BorderLayout(8, 8));

		TextArea msgObj = new TextArea(msg);
		add(msgObj, BorderLayout.CENTER);
		add(new Button("OK"), BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		setVisible(true);
	}

	public boolean action(Event e, Object o) {
		dispose();
		return true;
	}

}
