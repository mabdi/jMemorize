package jmemorize.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import jmemorize.core.Card;
import jmemorize.core.Category;
import jmemorize.core.Lesson;
import jmemorize.core.Main;
import jmemorize.gui.swing.widgets.CategoryComboBox;

public class ImportDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5360994489573286160L;
	private List<Card> selectedCards = null;
	private JButton cancelBtn;
	private JButton okBtn;
	private JList<Card> listCards;
	private DefaultListModel<Card> listModel;
	private CategoryComboBox cmbCat;

	public ImportDialog(JFrame parent, Lesson inputLesson) {
		super(parent,true);
		setTitle("Import");
		initComponents();
		for (Card element : inputLesson.getRootCategory().getCards()) {
			getListModel().addElement(element);
		}
		this.pack();
		setLocationRelativeTo(parent);
	}

	public List<Card> getSelectedCards() {
		return selectedCards;
	}
	
	public Category getSelectedCat(){
		return getCat().getSelectedCategory();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		JScrollPane jscroll = new JScrollPane(getList());
		jscroll.setPreferredSize(new Dimension(400, 500));
		JPanel north = new JPanel();
		north.setLayout(new BorderLayout());
		north.add(new JLabel("Target Category:"),BorderLayout.WEST);
		north.add(getCat(),BorderLayout.CENTER);
		add(north,BorderLayout.NORTH);
		add(jscroll, BorderLayout.CENTER);
		JPanel south = new JPanel();
		south.add(getOkBtn());
		south.add(getCancleBtn());
		add(south,BorderLayout.SOUTH);
	}

	private CategoryComboBox getCat() {
		if(cmbCat == null){
			cmbCat = new CategoryComboBox();
			cmbCat.setRootCategory(Main.getInstance().getLesson().getRootCategory());
			cmbCat.setSelectedCategory(Main.getInstance().getLesson().getRootCategory());
		}
		return cmbCat;
	}

	private JButton getCancleBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton();
			cancelBtn.setText("Cancel"); // TODO I18N
			cancelBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		return cancelBtn;
	}

	private JButton getOkBtn() {
		if (okBtn == null) {
			okBtn = new JButton();
			okBtn.setText("Ok");
			okBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setSelectedCards();
					dispose();
				}
			});
		}
		return okBtn;
	}

	protected void setSelectedCards() {
		selectedCards = getList().getSelectedValuesList();
	}

	private JList<Card> getList() {
		if (listCards == null) {
			listCards = new JList<Card>();
			listCards
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			getList().setModel(getListModel());
			// TODO show card details in a dialog when double click
			// listCards.addMouseListener(new MouseAdapter() {
			// public void mouseClicked(MouseEvent evt) {
			// if (evt.getClickCount() == 2) {
			//
			// // Double-click detected
			// int index = listCards.locationToIndex(evt.getPoint());
			// ShowItem(listCards.getModel().getElementAt(index));
			// }
			// }
			// });
		}
		return listCards;
	}

	private DefaultListModel<Card> getListModel() {
		if (listModel == null) {
			listModel = new DefaultListModel<Card>();
		}
		return listModel;
	}

}
