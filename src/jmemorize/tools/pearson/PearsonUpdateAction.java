package jmemorize.tools.pearson;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JOptionPane;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.SelectionProvider;
import jmemorize.gui.swing.SelectionProvider.SelectionObserver;
import jmemorize.gui.swing.actions.AbstractAction2;

/**
 * An action that show the window for editting cards with the currently selected
 * card.
 * 
 * @author djemili
 */
public class PearsonUpdateAction extends AbstractAction2 implements SelectionObserver {
	private SelectionProvider m_selectionProvider;

	public PearsonUpdateAction(SelectionProvider selectionProvider) {
		m_selectionProvider = selectionProvider;
		m_selectionProvider.addSelectionObserver(this);
		selectionChanged(m_selectionProvider);

		setValues();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener
	 */
	public void actionPerformed(java.awt.event.ActionEvent e) {
		List<Card> selectedCards = m_selectionProvider.getSelectedCards();

		if (selectedCards != null && selectedCards.size() > 0) {
			for (Card card : selectedCards) {
				new PearsonHelper().applyOnCard(card);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jmemorize.gui.swing.SelectionProvider.SelectionObserver#selectionChanged
	 */
	public void selectionChanged(SelectionProvider source) {
		Category rootCategory = Main.getInstance().getLesson().getRootCategory();

		setEnabled((source.getSelectedCards() != null && source.getSelectedCards().size() > 0)
				|| (source.getSelectedCategories() != null && !source.getSelectedCategories().contains(rootCategory)));
	}

	private void setValues() {
		setName("Do Pearson"); //$NON-NLS-1$
		setIcon("/resource/icons/pearson.png"); //$NON-NLS-1$
		setAccelerator(KeyEvent.VK_P, SHORTCUT_KEY + InputEvent.SHIFT_MASK);
		setMnemonic(1);
	}
}
