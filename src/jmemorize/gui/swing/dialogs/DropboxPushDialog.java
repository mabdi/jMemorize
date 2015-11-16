package jmemorize.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;

import jmemorize.core.Main;
import jmemorize.gui.Localization;

public class DropboxPushDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String PREFS_DROPBOX_TOKEN = "dropbox.token";
	final String APP_KEY;
	final String APP_SECRET;

	private JButton btnPull;
	private JButton btnPush;
	private JButton btnLogin;
	private DbxAppInfo appInfo;
	private DbxRequestConfig config;
	protected String accessToken;
	private DbxClient client;
	private JPanel topPanel;
	private JButton btnSignOut;
	private JTextField txtToken;
	private JButton btnSignIn;
	private JPanel middlePanel;
	private JTable dataTable;
	private DefaultTableModel tableModel;
	private CardLayout layout;
	private JButton btnCancel;
	private JButton btnRefresh;
	protected DbxWebAuthNoRedirect webAuth;
	private CardLayout layTopPanel;
	private JLabel lblTop;

	public DropboxPushDialog(JFrame parent) {
		super(parent, "Sync With Dropbox", true);
		setIconImage(new ImageIcon(getClass().getResource("/resource/icons/Dropbox-icon.png")).getImage());
		BufferedReader in = null;
		List<String> lines = new ArrayList<>();
		try {
			in = new BufferedReader(
					new InputStreamReader(Localization.class.getResourceAsStream("/resource/text/dropbox.txt")));
			String line;
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (lines.size() == 2) {
			APP_KEY = lines.get(0).trim();
			APP_SECRET = lines.get(1).trim();
		} else {
			APP_KEY = "";
			APP_SECRET = "";
		}
		appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		config = new DbxRequestConfig("jMemmorizePlus1/1.0", Locale.getDefault().toString());
		accessToken = Main.USER_PREFS.get(PREFS_DROPBOX_TOKEN, "");
		initComponents();
		pack();
		layout.show(getContentPane(), "loading");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updatePanel();
			}
		});
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private void updatePanel() {
		lock(true);
		if (accessToken.length() == 0) {
			updateUserInfoPanel();
			lock(false);
			return;
		}

		DbxEntry.WithChildren listing;
		try {
			client = new DbxClient(config, accessToken);
			listing = client.getMetadataWithChildren("/");
			updateUserInfoPanel();
			getTableModel().setRowCount(0);
			for (DbxEntry child : listing.children) {
				if (child.isFile() && child.asFile().name.toLowerCase().endsWith(".jml")) {
					getTableModel().addRow(new String[] { child.asFile().name, child.asFile().humanSize,
							Localization.SHORT_DATE_FORMATER.format(child.asFile().lastModified) });
				}
			}
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lock(false);

	}

	private void initComponents() {
		layout = new CardLayout();
		getContentPane().setLayout(layout);
		add("main", getMainPanel());
		add("loading", getLoadingPanel());
		validate();
		repaint();
	}

	private JPanel getLoadingPanel() {
		JPanel loading = new JPanel();
		loading.setLayout(new BorderLayout());
		// Icon from
		// http://www.ajaxload.info/
		// ImageIcon icon = new
		// ImageIcon(getClass().getResource("/resource/icons/spinner.gif"));
		JLabel lbl = new JLabel("Loading ...");
		lbl.setHorizontalAlignment(JLabel.CENTER);
		loading.add(lbl, BorderLayout.CENTER);
		return loading;
	}

	private JPanel getMainPanel() {
		JPanel main = new JPanel();
		main.setLayout(new BorderLayout());
		main.add(getUserInfoPanel(), BorderLayout.NORTH);
		main.add(getFilesListPanel(), BorderLayout.CENTER);
		main.add(getButtonsPanel(), BorderLayout.SOUTH);
		return main;
	}

	private Component getButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		panel.add(getSave());
		panel.add(getLoad());
		panel.add(new JLabel("   "));
		panel.add(getRefresh());
		panel.add(getCancel());

		return panel;
	}

	private JButton getCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}
		return btnCancel;
	}

	private JButton getRefresh() {
		if (btnRefresh == null) {
			btnRefresh = new JButton("Refresh");
			btnRefresh.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							updatePanel();
						}
					});
				}
			});
		}
		return btnRefresh;
	}

	private JPanel getFilesListPanel() {
		if (middlePanel == null) {
			middlePanel = new JPanel();
			middlePanel.setLayout(new BorderLayout());
			middlePanel.add(new JScrollPane(getDataTable()), BorderLayout.CENTER);
		}
		return middlePanel;
	}

	private JTable getDataTable() {
		if (dataTable == null) {
			dataTable = new JTable();
			dataTable.setModel(getTableModel());
			dataTable.getColumnModel().getColumn(1).setMaxWidth(70);
			dataTable.getColumnModel().getColumn(2).setMaxWidth(100);
		}
		return dataTable;
	}

	private DefaultTableModel getTableModel() {
		if (tableModel == null) {
			tableModel = new DefaultTableModel(new String[] { "File Name", "Size", "lastModified" }, 0);
		}
		return tableModel;
	}

	private Component getUserInfoPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			layTopPanel = new CardLayout();
			topPanel.setLayout(layTopPanel);

			JPanel p0 = new JPanel();
			p0.add(new JLabel("Loading .. "));

			JPanel p1 = new JPanel();
			p1.setLayout(new BorderLayout());
			p1.add(getNewUser(), BorderLayout.WEST);
			p1.add(getTokenTextBox(), BorderLayout.CENTER);
			p1.add(getSignInButton(), BorderLayout.EAST);

			JPanel p2 = new JPanel();
			lblTop = new JLabel("Try again");
			lblTop.setHorizontalTextPosition(JLabel.LEFT);
			topPanel.add(lblTop, BorderLayout.CENTER);
			topPanel.add(getSignOutButton(), BorderLayout.EAST);

			topPanel.add("p0", p0);
			topPanel.add("p1", p1);
			topPanel.add("p2", p2);
		}
		return topPanel;
	}

	private void updateUserInfoPanel() {
		if (accessToken.isEmpty()) {
			layTopPanel.show(topPanel, "p1");
		} else {
			String s = getInfo();
			if (s.isEmpty()) {
				s = "Connection Failed.";
				lblTop.setText(s);
				getSignOutButton().setEnabled(false);
			}else{
				lblTop.setText(s);
				getSignOutButton().setEnabled(true);
			}
			layTopPanel.show(topPanel, "p2");
		}
//		topPanel.invalidate();
	}

	private JTextField getTokenTextBox() {
		if (txtToken == null) {
			txtToken = new JTextField();
		}
		return txtToken;
	}

	private JButton getSignInButton() {
		if (btnSignIn == null) {
			btnSignIn = new JButton("Log In");
			btnSignIn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (webAuth == null) {
						return;
					}
					String code = getTokenTextBox().getText().trim();
					if (code != null && !code.isEmpty()) {
						try {
							DbxAuthFinish authFinish = webAuth.finish(code);
							accessToken = authFinish.accessToken;
							Main.USER_PREFS.put(PREFS_DROPBOX_TOKEN, accessToken);
							updateUserInfoPanel();
						} catch (DbxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}
			});
		}
		return btnSignIn;
	}

	private JButton getSignOutButton() {
		if (btnSignOut == null) {
			btnSignOut = new JButton("Log Out");
			btnSignOut.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					accessToken = "";
					Main.USER_PREFS.put(PREFS_DROPBOX_TOKEN, accessToken);
					updateUserInfoPanel();
				}
			});
		}
		return btnSignOut;
	}

	private void lock(boolean f) {
		if (f) {
			layout.show(getContentPane(), "loading");
		} else {
			layout.show(getContentPane(), "main");
		}
	}

	private JButton getSave() {
		if (btnPull == null) {
			btnPull = new JButton("Save to Dropbox");
			btnPull.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (Main.getInstance().getLesson() == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "No Lesson is loaded.");
						return;
					}
					if (client == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Use recent token or do a login first.");
						return;
					}
					if (Main.getInstance().getLesson().canSave() && Main.getInstance().getLesson().getFile() == null) {
						String filename = JOptionPane.showInputDialog(DropboxPushDialog.this, "Enter File Name:");
						if (filename.isEmpty()) {
							filename = "noname.jml";
						}
						File home = new File(System.getProperty("user.home") + "/jMemorize+1");
						if (!home.exists())
							home.mkdir();
						File temp = new File(home, filename);
						try {
							Main.getInstance().saveLesson(Main.getInstance().getLesson(), temp);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

					lock(false);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							doUpload();
							dispose();
						}
					});
				}
			});
		}
		return btnPull;
	}

	private void doUpload() {
		lock(false);
		File inputFile = Main.getInstance().getLesson().getFile();
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(inputFile);
			DbxEntry.File uploadedFile = client.uploadFile("/" + inputFile.getName(), DbxWriteMode.force(),
					inputFile.length(), inputStream);
			// JOptionPane.showMessageDialog(DropboxPushDialog.this, "Uploaded:
			// " + uploadedFile.toString());

		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			lock(true);
		}
	}

	private JButton getLoad() {
		if (btnPush == null) {
			btnPush = new JButton("Load from Dropbox");
			btnPush.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (client == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Use recent token or do a login first.");
						return;
					}
					if (getDataTable().getSelectedRow() < 0) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Select a file from table.");
						return;
					}

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							doDownload();
							dispose();
						}
					});

				}
			});
		}
		return btnPush;
	}

	private void doDownload() {
		FileOutputStream outputStream = null;
		try {
			String filename = "";
			if (getDataTable().getSelectedRow() >= 0) {
				filename = getTableModel().getValueAt(getDataTable().getSelectedRow(), 0).toString();
			}
			if (filename.isEmpty()) {
				filename = "noname.jml";
			}
			File home = new File(System.getProperty("user.home") + "/jMemorize+1");
			if (!home.exists())
				home.mkdir();
			File temp = new File(home, filename);
			outputStream = new FileOutputStream(temp);
			DbxEntry.File downloadedFile = client.getFile("/" + filename, null, outputStream);
			// JOptionPane.showMessageDialog(DropboxPushDialog.this, "Metadata:
			// " + downloadedFile.toString());
			outputStream.flush();
			outputStream.close();
			Main.getInstance().getFrame().loadLesson(temp);
		} catch (DbxException e0) {
			e0.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			lock(true);
		}
	}

	private JButton getNewUser() {
		if (btnLogin == null) {
			btnLogin = new JButton("New Login");
			btnLogin.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					webAuth = new DbxWebAuthNoRedirect(config, appInfo);
					String authorizeUrl = webAuth.start();
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(authorizeUrl));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					// String code = JOptionPane.showInputDialog("Please enter
					// the authorization code: ");

				}
			});
		}
		return btnLogin;
	}

	protected String getInfo() {
		try {
			return "Linked account: " + client.getAccountInfo().displayName;
		} catch (Exception e) {
			return "";
		}
	}
}
