package jmemorize.gui.swing.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

public class SoundFilePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 555187030751484979L;
	private BasicPlayer player;
	private JButton btnPlay;

	public SoundFilePanel() {
		add(getPlayBtn());
	}

	private JButton getPlayBtn() {
		if (btnPlay == null) {
			btnPlay = new JButton("Play");
			btnPlay.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (player != null) {
						try {
							if (player.getStatus() != BasicPlayer.STOPPED) {
								player.stop();
							}
							player.play();
						} catch (BasicPlayerException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
		}
		return btnPlay;
	}

	public void setSoundToPlay(BasicPlayer snd) {
		player = snd;
	}

}
