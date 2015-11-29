package jmemorize.gui.swing.panels;

import java.awt.BorderLayout;
import java.nio.charset.StandardCharsets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextFilePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3996571928019452702L;
	private String m_text;
	private JTextArea contentBox;
	
	public TextFilePanel() {
		setLayout(new BorderLayout());
		add(new JScrollPane(getContentText()),BorderLayout.CENTER);
	}
	
	private JTextArea getContentText() {
		if(contentBox == null){
			contentBox = new JTextArea();
			contentBox.setEditable(false);
			
		}
		return contentBox;
	}

	public void setTextToDisplay(String txt) {
		m_text = txt;
		getContentText().setText(m_text);
		getContentText().setCaretPosition(0);
	}

}
