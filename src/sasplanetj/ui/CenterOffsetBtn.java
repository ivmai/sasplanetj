package sasplanetj.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import sasplanetj.App;
import sasplanetj.Main;

public class CenterOffsetBtn extends Button {

	private static final Dimension size = new Dimension(25, 25);
	public Image img = null;

	public CenterOffsetBtn(){
		img = loadImageFromFile("offset.png");
		size.setSize(img.getWidth(null), img.getHeight(null));

		//setLabel(">");
		setAllSizes();


		//Main.offsetBtn.setBackground(Color.green);
		this.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				App.main.viewOffset0();
			}
		});

	}

    private void setAllSizes(){
    	setSize(size);
    	//setPreferredSize(size); //1.3 compat
    	//setMinimumSize(size);
    	//setMaximumSize(size);
    }


	private Image loadImageFromFile(String fname) {
		try {
			URL url = CenterOffsetBtn.class.getResource(fname);
			Image img = getToolkit().getImage(url);
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(img, 0);
			tracker.waitForAll();
			return img;
		}catch (Exception e){
			System.out.println("Can not open image: "+fname);
			return null;
		}
	}

	public void repaint(Graphics g){
		paint(g);
	}


	public void paint(Graphics g){
		g.drawImage(img, 0, 0, this);
	}

}
