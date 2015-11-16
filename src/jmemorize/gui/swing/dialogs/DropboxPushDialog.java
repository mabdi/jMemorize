package jmemorize.gui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

	public DropboxPushDialog(JFrame parent) {
		super(parent, "Sync With Dropbox", true);
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
				layout.show(getContentPane(), "main");
			}
		});
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private void updatePanel() {
		DbxEntry.WithChildren listing;
		try {
			client = new DbxClient(config, accessToken);
			updateUserInfoPanel();
			listing = client.getMetadataWithChildren("/");
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

	}

	private void initComponents() {
		layout = new CardLayout();
		getContentPane().setLayout(layout);
		add("main", getMainPanel());
		add("loading", getLoadingPanel());
	}

	private JPanel getLoadingPanel() {
		JPanel loading = new JPanel();
		loading.setLayout(new BorderLayout());
		// Icon from
		// http://www.ajaxload.info/
		ImageIcon icon = new ImageIcon(getClass().getResource("/resource/icons/ajax-loader.gif"));
		JLabel lbl = new JLabel("Please Wait!", icon, JLabel.CENTER);
		// lbl.setIcon();
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
		panel.add(getPull());
		panel.add(getPush());
		return panel;
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
			topPanel.setLayout(new GridBagLayout());
			topPanel.add(new JLabel("Loading .. "));
		}
		return topPanel;
	}

	private void updateUserInfoPanel() {
		topPanel.removeAll();
		if (accessToken.isEmpty()) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 0;
			topPanel.add(getNewUser(), gbc);
			gbc.weightx = 1;
			gbc.gridx = 1;
			topPanel.add(getTokenTextBox(), gbc);
			gbc.weightx = 0;
			gbc.gridx = 2;
			topPanel.add(getSignInButton(), gbc);
		} else {
			String s = getInfo();
			boolean faied = false;
			if (s.isEmpty()) {
				s = "Connection Failed. click to retry.";
				faied = true;
			}
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1;
			topPanel.add(new JLabel(s), gbc);
			if (!faied) {
				gbc.weightx = 0;
				gbc.gridx++;
				topPanel.add(getSignOutButton(), gbc);
			}
		}
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
					updateUserInfoPanel();
				}
			});
		}
		return btnSignOut;
	}

	private void lock(boolean f) {
		getNewUser().setEnabled(f);
		getPull().setEnabled(f);
		getPush().setEnabled(f);
	}

	private JButton getPull() {
		if (btnPull == null) {
			btnPull = new JButton("Pull");
			btnPull.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (client == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Use recent token or do a login first.");
						return;
					}
					FileOutputStream outputStream = null;
					try {
						lock(false);

						String filename = JOptionPane.showInputDialog(DropboxPushDialog.this, "Enter File Name:");
						File home = new File(System.getProperty("user.home") + "/jMemorize+1");
						if (!home.exists())
							home.mkdir();
						File temp = new File(home, filename);
						outputStream = new FileOutputStream(temp);
						DbxEntry.File downloadedFile = client.getFile("/" + filename, null, outputStream);
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Metadata: " + downloadedFile.toString());
						outputStream.flush();
						outputStream.close();
						Main.getInstance().getFrame().loadLesson(temp);
					} catch (DbxException e0) {
						e0.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} finally {
						lock(true);
					}
				}
			});
		}
		return btnPull;
	}

	private JButton getPush() {
		if (btnPush == null) {
			btnPush = new JButton("Push");
			btnPush.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (client == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Use recent token or do a login first.");
						return;
					}
					if (Main.getInstance().getLesson() == null) {
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "No Lesson is loaded.");
						return;
					}

					File inputFile = Main.getInstance().getLesson().getFile();
					FileInputStream inputStream = null;
					try {
						lock(false);
						inputStream = new FileInputStream(inputFile);
						DbxEntry.File uploadedFile = client.uploadFile("/" + inputFile.getName(),
								DbxWriteMode.update(System.currentTimeMillis() + ""), inputFile.length(), inputStream);
						JOptionPane.showMessageDialog(DropboxPushDialog.this, "Uploaded: " + uploadedFile.toString());

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
			});
		}
		return btnPush;
	}

	private JButton getNewUser() {
		if (btnLogin == null) {
			btnLogin = new JButton("New Login");
			btnLogin.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
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
					String code = JOptionPane.showInputDialog("Please enter the authorization code: ");
					if (code != null && !code.isEmpty()) {
						try {
							DbxAuthFinish authFinish = webAuth.finish(code);
							accessToken = authFinish.accessToken;
							Main.USER_PREFS.put(PREFS_DROPBOX_TOKEN, accessToken);
							getInfo();
						} catch (DbxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			});
		}
		return btnLogin;
	}

	protected String getInfo() {
		try {
			return "Linked account: " + client.getAccountInfo().displayName;
		} catch (DbxException e) {
			return "";
		}
	}
}
