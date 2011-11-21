package sasplanetj.ui;

import java.awt.*;

import sasplanetj.App;

public class ShowMessage extends Dialog {

	private Frame owner = App.getSelf();

	public ShowMessage(String msg){
		super(App.getSelf());

		this.setTitle(owner.getTitle());
		this.setSize(220, 220);
		this.setLocation(
				owner.getLocation().x+(owner.getSize().width-this.getSize().width)/2,
				owner.getLocation().y+(owner.getSize().height-this.getSize().height)/2
		);


		setLayout(new BorderLayout(8, 8));

		TextArea msgObj = new TextArea(msg);
		add(msgObj, BorderLayout.CENTER);
		add(new Button("OK"), BorderLayout.SOUTH);

		setVisible(true);
	}

	public boolean action(Event e, Object o) {
		/*
		if (e.target instanceof Button) {
			//System.out.println(((String) o));
			if (((String) o).equals("OK")) {
			}
		}
		*/
		dispose();
		return true;
	}

}
