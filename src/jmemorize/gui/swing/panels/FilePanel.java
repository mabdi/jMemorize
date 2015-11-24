package jmemorize.gui.swing.panels;

import java.awt.CardLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import javazoom.jlgui.basicplayer.BasicPlayer;
import jmemorize.core.FileRepository.FileItem;

public class FilePanel extends JPanel {
	/**
	* 
	*/
	private static final long serialVersionUID = -3212468986945815253L;
	private CardLayout card;
	private ScaledImagePanel img_panel;
	private ImageIcon img;
	private BasicPlayer snd;
	private String txt;
	private TextFilePanel txt_panel;
	private SoundFilePanel snd_panel;

	public FilePanel() {
		card = new CardLayout();
		this.setLayout(card);
		add("img",getImagePanel());
		add("snd",getSoundPanel());
		add("txt",getTextPanel());
	}
	
	
	private TextFilePanel getTextPanel() {
		if(txt_panel == null){
			txt_panel = new TextFilePanel();
		}
		return txt_panel;
	}


	private SoundFilePanel getSoundPanel() {
		if(snd_panel == null){
			snd_panel = new SoundFilePanel();
		}
		return snd_panel;
	}


	private ScaledImagePanel getImagePanel() {
		if(img_panel == null){
			img_panel = new ScaledImagePanel();
		}
		return img_panel;
	}


	public void setItem(FileItem f) {
		if (f.getType() == FileItem.TYPE_IMAGE) {
			img = f.getImage();
			getImagePanel().setImageToDisplay(img.getImage());
			card.show(this, "img");
		}
		if (f.getType() == FileItem.TYPE_SOUND) {
			snd = f.getSound();
			getSoundPanel().setSoundToPlay(snd);
			card.show(this, "snd");
		}
		if (f.getType() == FileItem.TYPE_TEXT) {
			txt = f.getText();
			getTextPanel().setTextToDisplay(txt);
			card.show(this, "txt");
		}
	}
}
